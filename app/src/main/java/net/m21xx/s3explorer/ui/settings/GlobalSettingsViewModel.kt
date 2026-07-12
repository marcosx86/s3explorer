package net.m21xx.s3explorer.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.m21xx.s3explorer.data.local.preferences.GlobalPreferences
import net.m21xx.s3explorer.data.local.preferences.SettingsDataStore
import javax.inject.Inject

@HiltViewModel
class GlobalSettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val uiState: StateFlow<GlobalPreferences> = settingsDataStore.globalPreferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GlobalPreferences()
    )

    fun toggleSafMount(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setSafMount(enabled)
        }
    }

    fun toggleTrustSsl(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setTrustSsl(enabled)
        }
    }

    fun toggleLockScreen(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setLockScreen(enabled)
        }
    }

    fun toggleLongDateFormat(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setLongDateFormat(enabled)
        }
    }

    fun toggleHideDotfiles(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setHideDotfiles(enabled)
        }
    }

    fun toggleShowThumbnails(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setShowThumbnails(enabled)
        }
    }
}
