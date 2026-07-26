package net.m21xx.s3explorer.ui.explorer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.m21xx.s3explorer.data.local.entity.S3ObjectEntity
import net.m21xx.s3explorer.data.local.preferences.SettingsDataStore
import net.m21xx.s3explorer.data.repository.ConnectionRepository
import net.m21xx.s3explorer.domain.CalculateStorageStatsUseCase
import net.m21xx.s3explorer.domain.StorageStatsSummary
import net.m21xx.s3explorer.domain.ForceSyncUseCase
import net.m21xx.s3explorer.domain.GetPresignedUrlUseCase
import net.m21xx.s3explorer.domain.ObserveDirectoryContentUseCase
import net.m21xx.s3explorer.domain.SyncDirectoryUseCase
import net.m21xx.s3explorer.domain.CreateFolderUseCase
import net.m21xx.s3explorer.domain.UploadObjectUseCase
import android.webkit.MimeTypeMap
import javax.inject.Inject

@HiltViewModel
class FileExplorerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeDirectoryContentUseCase: ObserveDirectoryContentUseCase,
    private val syncDirectoryUseCase: SyncDirectoryUseCase,
    private val forceSyncUseCase: ForceSyncUseCase,
    private val calculateStorageStatsUseCase: CalculateStorageStatsUseCase,
    private val connectionRepository: ConnectionRepository,
    private val settingsDataStore: SettingsDataStore,
    private val getPresignedUrlUseCase: GetPresignedUrlUseCase,
    private val createFolderUseCase: CreateFolderUseCase,
    private val uploadObjectUseCase: UploadObjectUseCase
) : ViewModel() {
 
    private val thumbnailCache = java.util.concurrent.ConcurrentHashMap<String, String>()

    private val _uiState = MutableStateFlow(FileExplorerState())
    val uiState: StateFlow<FileExplorerState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedObjects: Flow<PagingData<S3ObjectEntity>> = _uiState
        .flatMapLatest { state ->
            observeDirectoryContentUseCase.execute(
                profileId = state.profileId, 
                bucketName = state.bucketName, 
                parentPrefix = state.currentPrefix,
                sortBy = state.sortBy,
                sortDirection = state.sortDirection,
                showHidden = state.showHidden,
                searchQuery = state.searchQuery,
                foldersFirst = state.foldersFirst
            )
        }
        .cachedIn(viewModelScope)

    init {
        val profileId = savedStateHandle.get<String>("profileId") ?: ""
        val bucketName = savedStateHandle.get<String>("bucketName") ?: "unknown"
        _uiState.update { it.copy(profileId = profileId, bucketName = bucketName) }
        
        viewModelScope.launch {
            settingsDataStore.viewMode.collect { mode ->
                _uiState.update { it.copy(viewMode = mode) }
            }
        }
        
        viewModelScope.launch {
            settingsDataStore.globalPreferences.collect { prefs ->
                _uiState.update { 
                    it.copy(
                        sortBy = prefs.sortBy,
                        sortDirection = prefs.sortDirection,
                        showHidden = !prefs.hideDotfiles,
                        showImageThumbnails = prefs.showImageThumbnails,
                        showVideoThumbnails = prefs.showVideoThumbnails
                    )
                }
            }
        }
        
        viewModelScope.launch {
            if (profileId.isNotEmpty()) {
                val profile = connectionRepository.getProfileById(profileId)
                val stats = if (profile?.storageLastUpdated != null) {
                    StorageStatsSummary(
                        sizeBytes = profile.storageSizeBytes ?: 0L,
                        objectCount = profile.storageObjectCount ?: 0,
                        lastUpdated = profile.storageLastUpdated
                    )
                } else null

                _uiState.update { 
                    it.copy(drawerState = it.drawerState.copy(activeProfile = profile, storageStats = stats))
                }

                if (stats == null) {
                    refreshStorageStats()
                }
            }
        }
        
        syncCurrentDirectory()
    }

    fun syncCurrentDirectory() {
        val state = _uiState.value
        if (state.profileId.isEmpty()) return
        
        _uiState.update { it.copy(isSyncing = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                syncDirectoryUseCase.execute(state.profileId, state.bucketName, state.currentPrefix)
                _uiState.update { it.copy(errorMessage = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to sync directory") }
            } finally {
                _uiState.update { it.copy(isSyncing = false) }
            }
        }
    }

    fun navigateToFolder(prefix: String) {
        _uiState.update { 
            it.copy(
                currentPrefix = prefix,
                searchActive = false,
                searchQuery = ""
            ) 
        }
        syncCurrentDirectory()
    }

    fun navigateUp() {
        val current = _uiState.value.currentPrefix
        if (current.isNotEmpty()) {
            val parts = current.dropLast(1).split("/")
            val newPrefix = if (parts.size <= 1) "" else parts.dropLast(1).joinToString("/") + "/"
            _uiState.update { 
                it.copy(
                    currentPrefix = newPrefix,
                    searchActive = false,
                    searchQuery = ""
                ) 
            }
            syncCurrentDirectory()
        } else if (_uiState.value.searchActive) {
            _uiState.update { 
                it.copy(
                    searchActive = false,
                    searchQuery = ""
                ) 
            }
        }
    }

    fun toggleViewMode() {
        viewModelScope.launch {
            val nextMode = _uiState.value.viewMode.next()
            settingsDataStore.setViewMode(nextMode)
        }
    }

    fun setSortBy(sortBy: SortBy) {
        viewModelScope.launch {
            settingsDataStore.setSortBy(sortBy)
        }
    }

    fun setSortDirection(sortDirection: SortDirection) {
        viewModelScope.launch {
            settingsDataStore.setSortDirection(sortDirection)
        }
    }

    fun toggleShowHidden() {
        viewModelScope.launch {
            settingsDataStore.setHideDotfiles(_uiState.value.showHidden) // Invert current showHidden to set hideDotfiles
        }
    }

    fun getThumbnailUrlSync(item: S3ObjectEntity): String? {
        return thumbnailCache[item.objectKey]
    }

    suspend fun getThumbnailUrl(item: S3ObjectEntity): String? {
        thumbnailCache[item.objectKey]?.let { return it }

        val filename = item.objectKey.substringAfterLast('/')
        val extension = if (filename.contains('.')) filename.substringAfterLast('.').lowercase() else ""

        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)

        if (mimeType != null && (mimeType.startsWith("image/") || mimeType.startsWith("video/"))) {
            val url = getPresignedUrlUseCase.execute(_uiState.value.profileId, _uiState.value.bucketName, item.objectKey)
            if (url != null) {
                thumbnailCache[item.objectKey] = url
            }
            return url
        }
        return null
    }

    fun refreshStorageStats() {
        val state = _uiState.value
        if (state.profileId.isEmpty() || state.drawerState.isCalculatingStorageStats) return
        
        _uiState.update { 
            it.copy(drawerState = it.drawerState.copy(isCalculatingStorageStats = true)) 
        }

        viewModelScope.launch {
            try {
                val stats = calculateStorageStatsUseCase.execute(state.profileId, state.bucketName)
                _uiState.update { 
                    it.copy(drawerState = it.drawerState.copy(storageStats = stats ?: it.drawerState.storageStats, isCalculatingStorageStats = false)) 
                }
            } catch (e: Exception) {
                // Ignore error but reset loading state
                _uiState.update { 
                    it.copy(drawerState = it.drawerState.copy(isCalculatingStorageStats = false)) 
                }
            }
        }
    }

    fun forceSync() {
        val state = _uiState.value
        if (state.profileId.isEmpty()) return
        
        _uiState.update { it.copy(isSyncing = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                forceSyncUseCase.execute(state.profileId, state.bucketName, state.currentPrefix)
                _uiState.update { it.copy(errorMessage = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to force sync directory") }
            } finally {
                _uiState.update { it.copy(isSyncing = false) }
            }
        }
    }

    fun toggleAboutDialog(show: Boolean) {
        _uiState.update { 
            it.copy(drawerState = it.drawerState.copy(showAboutDialog = show)) 
        }
    }

    fun toggleRemoveCredentialsDialog(show: Boolean) {
        _uiState.update { 
            it.copy(drawerState = it.drawerState.copy(showRemoveCredentialsDialog = show)) 
        }
    }

    fun toggleFloatingActionDrawer(show: Boolean) {
        _uiState.update { it.copy(showFloatingActionDrawer = show) }
    }

    fun toggleSelectionMode() {
        _uiState.update { state ->
            val nextActive = !state.selectionModeActive
            state.copy(
                selectionModeActive = nextActive,
                selectedItems = if (nextActive) state.selectedItems else emptySet()
            )
        }
    }

    fun toggleItemSelection(objectKey: String) {
        _uiState.update { state ->
            val newSelection = state.selectedItems.toMutableSet()
            if (newSelection.contains(objectKey)) {
                newSelection.remove(objectKey)
            } else {
                newSelection.add(objectKey)
            }
            state.copy(selectedItems = newSelection)
        }
    }

    fun toggleSearch() {
        _uiState.update { state ->
            val nextActive = !state.searchActive
            state.copy(
                searchActive = nextActive,
                searchQuery = if (nextActive) state.searchQuery else ""
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleFoldersFirst() {
        _uiState.update { it.copy(foldersFirst = !it.foldersFirst) }
    }

    fun createFolder(folderName: String) {
        val state = _uiState.value
        if (state.profileId.isEmpty() || folderName.isBlank()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            try {
                val fullPath = if (state.currentPrefix.isEmpty()) {
                    "$folderName/"
                } else {
                    "${state.currentPrefix}$folderName/"
                }
                createFolderUseCase.execute(state.profileId, state.bucketName, fullPath)
                syncCurrentDirectory()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to create folder") }
            } finally {
                _uiState.update { it.copy(isSyncing = false) }
            }
        }
    }

    fun uploadFile(fileName: String, fileBytes: ByteArray, lastModified: Long? = null) {
        val state = _uiState.value
        if (state.profileId.isEmpty() || fileName.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            try {
                val fullKey = if (state.currentPrefix.isEmpty()) {
                    fileName
                } else {
                    "${state.currentPrefix}$fileName"
                }
                uploadObjectUseCase.execute(state.profileId, state.bucketName, fullKey, fileBytes, lastModified)
                syncCurrentDirectory()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to upload file") }
            } finally {
                _uiState.update { it.copy(isSyncing = false) }
            }
        }
    }
}
