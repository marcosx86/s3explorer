package net.m21xx.s3explorer.ui.connection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.m21xx.s3explorer.data.local.dao.ConnectionProfileDao
import net.m21xx.s3explorer.data.local.entity.ConnectionProfileEntity
import net.m21xx.s3explorer.domain.DeleteProfileUseCase
import net.m21xx.s3explorer.domain.GenerateRcloneConfigUseCase
import net.m21xx.s3explorer.domain.SetCustomNameUseCase
import javax.inject.Inject

data class ConnectionsListState(
    val profiles: List<ConnectionProfileEntity> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val generatedConfig: String? = null
)

@HiltViewModel
class ConnectionsListViewModel @Inject constructor(
    private val connectionProfileDao: ConnectionProfileDao,
    private val deleteProfileUseCase: DeleteProfileUseCase,
    private val setCustomNameUseCase: SetCustomNameUseCase,
    private val generateRcloneConfigUseCase: GenerateRcloneConfigUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConnectionsListState())
    val uiState: StateFlow<ConnectionsListState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            connectionProfileDao.getAllProfiles()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { profilesList ->
                    _uiState.update { it.copy(profiles = profilesList, isLoading = false, error = null) }
                }
        }
    }

    fun deleteProfile(profileId: String) {
        viewModelScope.launch {
            try {
                deleteProfileUseCase.execute(profileId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to delete: ${e.message}") }
            }
        }
    }

    fun renameProfile(profileId: String, newName: String) {
        viewModelScope.launch {
            try {
                setCustomNameUseCase.execute(profileId, newName)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to rename: ${e.message}") }
            }
        }
    }

    fun generateConfig(profileId: String) {
        viewModelScope.launch {
            try {
                val config = generateRcloneConfigUseCase.execute(profileId)
                _uiState.update { it.copy(generatedConfig = config) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to generate config: ${e.message}") }
            }
        }
    }

    fun clearGeneratedConfig() {
        _uiState.update { it.copy(generatedConfig = null) }
    }

    fun markProfileAsActive(profileId: String) {
        viewModelScope.launch {
            val profile = connectionProfileDao.getProfileById(profileId) ?: return@launch
            val updated = profile.copy(lastUsedAt = System.currentTimeMillis())
            connectionProfileDao.insertProfile(updated)
        }
    }
}
