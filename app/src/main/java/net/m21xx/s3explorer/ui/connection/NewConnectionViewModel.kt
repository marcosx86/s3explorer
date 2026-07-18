package net.m21xx.s3explorer.ui.connection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.m21xx.s3explorer.domain.FetchAvailableBucketsUseCase
import net.m21xx.s3explorer.domain.SaveConnectionProfileUseCase
import net.m21xx.s3explorer.data.local.dao.ConnectionProfileDao
import net.m21xx.s3explorer.data.repository.ConnectionRepository
import androidx.lifecycle.SavedStateHandle
import javax.inject.Inject

@HiltViewModel
class NewConnectionViewModel @Inject constructor(
    private val saveConnectionProfileUseCase: SaveConnectionProfileUseCase,
    private val fetchAvailableBucketsUseCase: FetchAvailableBucketsUseCase,
    private val connectionProfileDao: ConnectionProfileDao,
    private val connectionRepository: ConnectionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewConnectionState())
    val uiState: StateFlow<NewConnectionState> = _uiState.asStateFlow()

    init {
        val reuseProfileId = savedStateHandle.get<String>("reuseProfileId")
        if (!reuseProfileId.isNullOrBlank()) {
            loadReuseProfile(reuseProfileId)
        }
    }

    private fun loadReuseProfile(profileId: String) {
        viewModelScope.launch {
            val profile = connectionProfileDao.getProfileById(profileId) ?: return@launch
            val secretKey = connectionRepository.getProfileSecretKey(profileId) ?: return@launch
            _uiState.update {
                it.copy(
                    accessKey = profile.accessKey,
                    secretKey = secretKey,
                    endpointUrl = profile.endpointUrl,
                    region = profile.region
                )
            }
        }
    }

    fun updateAccessKey(key: String) {
        _uiState.update { it.copy(accessKey = key, isConnectionValidated = false, validationError = null) }
    }

    fun updateSecretKey(key: String) {
        _uiState.update { it.copy(secretKey = key, isConnectionValidated = false, validationError = null) }
    }

    fun updateEndpointUrl(url: String) {
        _uiState.update { it.copy(endpointUrl = url, isConnectionValidated = false, validationError = null) }
    }

    fun updateBucketName(name: String) {
        _uiState.update { it.copy(bucketName = name) }
    }

    fun updateRegion(region: String) {
        _uiState.update { it.copy(region = region, isConnectionValidated = false, validationError = null) }
    }

    fun toggleTermsAccepted(accepted: Boolean) {
        _uiState.update { it.copy(termsAccepted = accepted) }
    }

    fun toggleSecretVisibility() {
        _uiState.update { it.copy(isSecretVisible = !it.isSecretVisible) }
    }

    fun fetchBuckets() {
        val state = _uiState.value
        if (state.endpointUrl.isBlank() || state.accessKey.isBlank() || state.secretKey.isBlank()) {
            _uiState.update { it.copy(fetchBucketsError = "Endpoint and credentials are required.") }
            return
        }

        _uiState.update { it.copy(isFetchingBuckets = true, fetchBucketsError = null) }

        viewModelScope.launch {
            val result = fetchAvailableBucketsUseCase.execute(
                endpointUrl = state.endpointUrl,
                accessKey = state.accessKey,
                secretKey = state.secretKey,
                region = state.region
            )
            
            result.onSuccess { buckets ->
                _uiState.update {
                    it.copy(
                        isFetchingBuckets = false,
                        availableBuckets = buckets,
                        fetchBucketsError = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isFetchingBuckets = false,
                        fetchBucketsError = error.message ?: "Failed to fetch buckets"
                    )
                }
            }
        }
    }

    fun validateConnection() {
        val state = _uiState.value
        if (state.endpointUrl.isBlank() || state.accessKey.isBlank() || state.secretKey.isBlank()) {
            _uiState.update { it.copy(validationError = "Endpoint and credentials are required.") }
            return
        }

        _uiState.update { it.copy(isValidatingConnection = true, validationError = null, isConnectionValidated = false) }

        viewModelScope.launch {
            val result = fetchAvailableBucketsUseCase.execute(
                endpointUrl = state.endpointUrl,
                accessKey = state.accessKey,
                secretKey = state.secretKey,
                region = state.region
            )
            
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isValidatingConnection = false,
                        isConnectionValidated = true,
                        validationError = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isValidatingConnection = false,
                        isConnectionValidated = false,
                        validationError = error.message ?: "Validation failed"
                    )
                }
            }
        }
    }

    fun testConnection() {
        if (!_uiState.value.isConnectEnabled) return

        _uiState.update { it.copy(isTestingConnection = true, connectionResult = null) }

        viewModelScope.launch {
            val state = _uiState.value
            try {
                val profileId = saveConnectionProfileUseCase.execute(
                    alias = "",
                    endpointUrl = state.endpointUrl,
                    accessKey = state.accessKey,
                    secretKey = state.secretKey,
                    defaultBucket = state.bucketName,
                    region = state.region
                )
                _uiState.update {
                    it.copy(
                        isTestingConnection = false,
                        connectionResult = Result.success(profileId)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isTestingConnection = false,
                        connectionResult = Result.failure(e)
                    )
                }
            }
        }
    }
}
