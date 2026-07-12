package net.m21xx.s3explorer.ui.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.m21xx.s3explorer.data.local.preferences.ProfilePreferences
import net.m21xx.s3explorer.data.local.preferences.ProfilePreferencesDataStore
import net.m21xx.s3explorer.domain.CacheType
import net.m21xx.s3explorer.domain.ClearCacheUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

data class AccountSettingsUIState(
    val profileId: String = "",
    val preferences: ProfilePreferences = ProfilePreferences(),
    val snackbarMessage: String? = null
)

@HiltViewModel
class AccountSettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val profilePreferencesDataStore: ProfilePreferencesDataStore,
    private val clearCacheUseCase: ClearCacheUseCase
) : ViewModel() {

    private val profileId: String = savedStateHandle.get<String>("profileId") ?: ""

    private val _snackbarMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AccountSettingsUIState> = kotlinx.coroutines.flow.combine(
        profilePreferencesDataStore.getPreferences(profileId),
        _snackbarMessage
    ) { prefs, snackbar ->
        AccountSettingsUIState(
            profileId = profileId,
            preferences = prefs,
            snackbarMessage = snackbar
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AccountSettingsUIState(profileId = profileId)
    )

    fun toggleFilenameEncryption(enabled: Boolean) {
        viewModelScope.launch {
            profilePreferencesDataStore.setFilenameEncryption(profileId, enabled)
        }
    }

    fun updateMultipartThreshold(thresholdMB: Int) {
        viewModelScope.launch {
            profilePreferencesDataStore.setMultipartThreshold(profileId, thresholdMB)
        }
    }

    fun updateUploadConcurrency(concurrency: Int) {
        viewModelScope.launch {
            profilePreferencesDataStore.setUploadConcurrency(profileId, concurrency)
        }
    }

    fun toggleCalculateMD5(enabled: Boolean) {
        viewModelScope.launch {
            profilePreferencesDataStore.setCalculateMD5(profileId, enabled)
        }
    }

    fun clearDocumentCache() {
        viewModelScope.launch {
            val result = clearCacheUseCase.execute(profileId, CacheType.DOCUMENTS)
            if (result.isSuccess) {
                showSnackbar("Document cache cleared successfully.")
            } else {
                showSnackbar("Failed to clear document cache.")
            }
        }
    }

    fun clearThumbnailCache() {
        viewModelScope.launch {
            val result = clearCacheUseCase.execute(profileId, CacheType.THUMBNAILS)
            if (result.isSuccess) {
                showSnackbar("Thumbnail cache cleared successfully.")
            } else {
                showSnackbar("Failed to clear thumbnail cache.")
            }
        }
    }

    private fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}
