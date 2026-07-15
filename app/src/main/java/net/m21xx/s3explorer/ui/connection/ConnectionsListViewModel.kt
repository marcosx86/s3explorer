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
import net.m21xx.s3explorer.data.repository.ConnectionRepository
import net.m21xx.s3explorer.data.model.ConnectionExportItem
import android.net.Uri
import android.content.Context
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
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
    private val generateRcloneConfigUseCase: GenerateRcloneConfigUseCase,
    private val connectionRepository: ConnectionRepository
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

    fun clearAllProfiles() {
        viewModelScope.launch {
            try {
                uiState.value.profiles.forEach { profile ->
                    deleteProfileUseCase.execute(profile.profileId)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to clear profiles: ${e.message}") }
            }
        }
    }

    fun exportConnections(uri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                val jsonArray = JSONArray()
                uiState.value.profiles.forEach { profile ->
                    val secretKey = connectionRepository.getProfileSecretKey(profile.profileId) ?: ""
                    val jsonObject = JSONObject().apply {
                        put("profileId", profile.profileId)
                        put("alias", profile.alias)
                        put("endpointUrl", profile.endpointUrl)
                        put("accessKey", profile.accessKey)
                        put("defaultBucket", profile.defaultBucket)
                        put("region", profile.region)
                        put("secretKey", secretKey)
                    }
                    jsonArray.put(jsonObject)
                }
                
                val jsonString = jsonArray.toString()
                val base64String = Base64.encodeToString(jsonString.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
                
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(base64String.toByteArray(Charsets.UTF_8))
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to export: ${e.message}") }
            }
        }
    }

    fun importConnections(uri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                var base64String = ""
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    base64String = inputStream.bufferedReader().readText()
                }
                if (base64String.isNotEmpty()) {
                    val jsonString = String(Base64.decode(base64String, Base64.DEFAULT), Charsets.UTF_8)
                    val jsonArray = JSONArray(jsonString)
                    
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val entity = ConnectionProfileEntity(
                            profileId = jsonObject.getString("profileId"),
                            alias = jsonObject.getString("alias"),
                            endpointUrl = jsonObject.getString("endpointUrl"),
                            accessKey = jsonObject.getString("accessKey"),
                            defaultBucket = jsonObject.getString("defaultBucket"),
                            region = jsonObject.getString("region"),
                            lastUsedAt = System.currentTimeMillis()
                        )
                        val secretKey = jsonObject.getString("secretKey")
                        connectionRepository.saveProfile(entity, secretKey)
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to import: ${e.message}") }
            }
        }
    }
}
