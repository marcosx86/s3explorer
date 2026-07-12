package net.m21xx.s3explorer.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import java.util.concurrent.ConcurrentHashMap

data class ProfilePreferences(
    val filenameEncryptionEnabled: Boolean = false,
    val multipartUploadThresholdMB: Int = 5,
    val uploadConcurrency: Int = 3,
    val calculateMD5Enabled: Boolean = false
)

@Singleton
class ProfilePreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Cache the datastore instances to prevent multiple instances for the same file
    private val dataStoreCache = ConcurrentHashMap<String, DataStore<Preferences>>()

    private fun getDataStore(profileId: String): DataStore<Preferences> {
        return dataStoreCache.getOrPut(profileId) {
            PreferenceDataStoreFactory.create(
                produceFile = { context.preferencesDataStoreFile("profile_$profileId") }
            )
        }
    }

    private val FILENAME_ENCRYPTION_KEY = booleanPreferencesKey("filename_encryption")
    private val MULTIPART_THRESHOLD_KEY = intPreferencesKey("multipart_threshold_mb")
    private val UPLOAD_CONCURRENCY_KEY = intPreferencesKey("upload_concurrency")
    private val CALCULATE_MD5_KEY = booleanPreferencesKey("calculate_md5")

    fun getPreferences(profileId: String): Flow<ProfilePreferences> {
        return getDataStore(profileId).data.map { prefs ->
            ProfilePreferences(
                filenameEncryptionEnabled = prefs[FILENAME_ENCRYPTION_KEY] ?: false,
                multipartUploadThresholdMB = prefs[MULTIPART_THRESHOLD_KEY] ?: 5,
                uploadConcurrency = prefs[UPLOAD_CONCURRENCY_KEY] ?: 3,
                calculateMD5Enabled = prefs[CALCULATE_MD5_KEY] ?: false
            )
        }
    }

    suspend fun setFilenameEncryption(profileId: String, enabled: Boolean) {
        getDataStore(profileId).edit { prefs ->
            prefs[FILENAME_ENCRYPTION_KEY] = enabled
        }
    }

    suspend fun setMultipartThreshold(profileId: String, thresholdMB: Int) {
        getDataStore(profileId).edit { prefs ->
            prefs[MULTIPART_THRESHOLD_KEY] = thresholdMB
        }
    }

    suspend fun setUploadConcurrency(profileId: String, concurrency: Int) {
        getDataStore(profileId).edit { prefs ->
            prefs[UPLOAD_CONCURRENCY_KEY] = concurrency
        }
    }

    suspend fun setCalculateMD5(profileId: String, enabled: Boolean) {
        getDataStore(profileId).edit { prefs ->
            prefs[CALCULATE_MD5_KEY] = enabled
        }
    }
}
