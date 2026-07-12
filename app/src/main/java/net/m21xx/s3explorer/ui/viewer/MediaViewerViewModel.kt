package net.m21xx.s3explorer.ui.viewer

import android.webkit.MimeTypeMap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.m21xx.s3explorer.data.local.dao.S3ObjectDao
import net.m21xx.s3explorer.data.local.entity.S3ObjectEntity
import net.m21xx.s3explorer.domain.GetPresignedUrlUseCase
import javax.inject.Inject

data class MediaItem(
    val entity: S3ObjectEntity,
    val mimeType: String,
    val isVideo: Boolean
)

data class MediaViewerState(
    val profileId: String = "",
    val bucketName: String = "",
    val parentPrefix: String = "",
    val mediaItems: List<MediaItem> = emptyList(),
    val initialPage: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class MediaViewerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val s3ObjectDao: S3ObjectDao,
    private val getPresignedUrlUseCase: GetPresignedUrlUseCase
) : ViewModel() {

    private val thumbnailCache = java.util.concurrent.ConcurrentHashMap<String, String>()

    private val _uiState = MutableStateFlow(MediaViewerState())
    val uiState: StateFlow<MediaViewerState> = _uiState.asStateFlow()

    init {
        val profileId = savedStateHandle.get<String>("profileId") ?: ""
        val bucketName = savedStateHandle.get<String>("bucketName") ?: "unknown"
        val parentPrefix = savedStateHandle.get<String>("parentPrefix") ?: ""
        val initialObjectKey = savedStateHandle.get<String>("initialObjectKey") ?: ""

        _uiState.update { it.copy(
            profileId = profileId,
            bucketName = bucketName,
            parentPrefix = parentPrefix,
            isLoading = true
        ) }

        viewModelScope.launch {
            try {
                val allFiles = s3ObjectDao.getAllFilesByPrefix(profileId, bucketName, parentPrefix)
                
                val mediaItems = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                    allFiles.mapNotNull { entity ->
                        val filename = entity.objectKey.substringAfterLast('/')
                        val extension = if (filename.contains('.')) filename.substringAfterLast('.').lowercase() else ""
                        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                        
                        if (mimeType != null && (mimeType.startsWith("image/") || mimeType.startsWith("video/"))) {
                            MediaItem(
                                entity = entity,
                                mimeType = mimeType,
                                isVideo = mimeType.startsWith("video/")
                            )
                        } else {
                            null
                        }
                    }
                }

                val initialPage = mediaItems.indexOfFirst { it.entity.objectKey == initialObjectKey }.coerceAtLeast(0)

                _uiState.update { it.copy(
                    mediaItems = mediaItems,
                    initialPage = initialPage,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load media items"
                ) }
            }
        }
    }

    suspend fun getPresignedUrl(objectKey: String): String? {
        thumbnailCache[objectKey]?.let { return it }

        val state = _uiState.value
        val url = getPresignedUrlUseCase.execute(state.profileId, state.bucketName, objectKey)
        if (url != null) {
            thumbnailCache[objectKey] = url
        }
        return url
    }
}
