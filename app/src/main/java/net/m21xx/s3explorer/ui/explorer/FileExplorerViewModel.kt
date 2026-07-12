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
import net.m21xx.s3explorer.domain.ObserveDirectoryContentUseCase
import net.m21xx.s3explorer.domain.SyncDirectoryUseCase
import javax.inject.Inject

@HiltViewModel
class FileExplorerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeDirectoryContentUseCase: ObserveDirectoryContentUseCase,
    private val syncDirectoryUseCase: SyncDirectoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FileExplorerState())
    val uiState: StateFlow<FileExplorerState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedObjects: Flow<PagingData<S3ObjectEntity>> = _uiState
        .flatMapLatest { state ->
            observeDirectoryContentUseCase.execute(state.bucketName, state.currentPrefix)
        }
        .cachedIn(viewModelScope)

    init {
        val profileId = savedStateHandle.get<String>("profileId") ?: ""
        val bucketName = savedStateHandle.get<String>("bucketName") ?: "unknown"
        _uiState.update { it.copy(profileId = profileId, bucketName = bucketName) }
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
        _uiState.update { it.copy(currentPrefix = prefix) }
        syncCurrentDirectory()
    }

    fun navigateUp() {
        val current = _uiState.value.currentPrefix
        if (current.isNotEmpty()) {
            val parts = current.dropLast(1).split("/")
            val newPrefix = if (parts.size <= 1) "" else parts.dropLast(1).joinToString("/") + "/"
            _uiState.update { it.copy(currentPrefix = newPrefix) }
            syncCurrentDirectory()
        }
    }
}
