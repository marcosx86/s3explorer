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
import javax.inject.Inject

@HiltViewModel
class NewConnectionViewModel @Inject constructor() : ViewModel() {

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

    fun testConnection() {
        if (!_uiState.value.isConnectEnabled) return

        _uiState.update { it.copy(isTestingConnection = true, connectionResult = null) }

        viewModelScope.launch {
            // Mocking a network delay for the S3 connection test
            delay(1500)
            
            // Basic mock validation: if URL starts with http, assume success
            val isSuccess = _uiState.value.endpointUrl.startsWith("http")
            
            _uiState.update {
                it.copy(
                    isTestingConnection = false,
                    connectionResult = if (isSuccess) Result.success(Unit) else Result.failure(Exception("Invalid Endpoint URL"))
                )
            }
        }
    }
}
