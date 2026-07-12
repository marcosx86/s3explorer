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
import javax.inject.Inject

@HiltViewModel
class NewConnectionViewModel @Inject constructor(
    private val saveConnectionProfileUseCase: SaveConnectionProfileUseCase,
    private val fetchAvailableBucketsUseCase: FetchAvailableBucketsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewConnectionState())
    val uiState: StateFlow<NewConnectionState> = _uiState.asStateFlow()

    fun updateAccessKey(key: String) {
        _uiState.update { it.copy(accessKey = key) }
    }

    fun updateSecretKey(key: String) {
        _uiState.update { it.copy(secretKey = key) }
    }

    fun updateEndpointUrl(url: String) {
        _uiState.update { it.copy(endpointUrl = url) }
    }

    fun updateBucketName(name: String) {
        _uiState.update { it.copy(bucketName = name) }
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
                secretKey = state.secretKey
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

    fun testConnection() {
        if (!_uiState.value.isConnectEnabled) return

        _uiState.update { it.copy(isTestingConnection = true, connectionResult = null) }

        viewModelScope.launch {
            val state = _uiState.value
            val result = fetchAvailableBucketsUseCase.execute(
                endpointUrl = state.endpointUrl,
                accessKey = state.accessKey,
                secretKey = state.secretKey
            )
            
            result.onSuccess {
                try {
                    saveConnectionProfileUseCase.execute(
                        alias = "",
                        endpointUrl = state.endpointUrl,
                        accessKey = state.accessKey,
                        secretKey = state.secretKey,
                        defaultBucket = state.bucketName
                    )
                    _uiState.update {
                        it.copy(
                            isTestingConnection = false,
                            connectionResult = Result.success(Unit)
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
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isTestingConnection = false,
                        connectionResult = Result.failure(error)
                    )
                }
            }
        }
    }
}
