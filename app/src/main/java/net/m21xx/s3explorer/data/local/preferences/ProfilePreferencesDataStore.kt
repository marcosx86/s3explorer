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
    val storageClass: String = "",
    val skipSameFileUpload: Boolean = false,
    val multipartUploadThresholdMB: Int = 150,
    val multipartConcurrentParts: Int = 5,
    val multipartChunkSizeMB: Int = 10,
    val uploadConcurrency: Int = 2,
    val calculateMD5Enabled: Boolean = false,
    val generateThumbnailsLocally: Boolean = true,
    val uploadThumbnailsRemotely: Boolean = false,
    val uploadTimeoutMs: Long = 300000L,
    val downloadTimeoutMs: Long = 300000L
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
    private val STORAGE_CLASS_KEY = stringPreferencesKey("storage_class")
    private val SKIP_SAME_FILE_UPLOAD_KEY = booleanPreferencesKey("skip_same_file_upload")
    private val MULTIPART_THRESHOLD_KEY = intPreferencesKey("multipart_threshold_mb")
    private val MULTIPART_CONCURRENT_PARTS_KEY = intPreferencesKey("multipart_concurrent_parts")
    private val MULTIPART_CHUNK_SIZE_KEY = intPreferencesKey("multipart_chunk_size_mb")
    private val UPLOAD_CONCURRENCY_KEY = intPreferencesKey("upload_concurrency")
    private val CALCULATE_MD5_KEY = booleanPreferencesKey("calculate_md5")
    private val GENERATE_THUMBNAILS_LOCALLY_KEY = booleanPreferencesKey("generate_thumbnails_locally")
    private val UPLOAD_THUMBNAILS_REMOTELY_KEY = booleanPreferencesKey("upload_thumbnails_remotely")
    private val UPLOAD_TIMEOUT_MS_KEY = longPreferencesKey("upload_timeout_ms")
    private val DOWNLOAD_TIMEOUT_MS_KEY = longPreferencesKey("download_timeout_ms")

    fun getPreferences(profileId: String): Flow<ProfilePreferences> {
        return getDataStore(profileId).data.map { prefs ->
            ProfilePreferences(
                filenameEncryptionEnabled = prefs[FILENAME_ENCRYPTION_KEY] ?: false,
                storageClass = prefs[STORAGE_CLASS_KEY] ?: "",
                skipSameFileUpload = prefs[SKIP_SAME_FILE_UPLOAD_KEY] ?: false,
                multipartUploadThresholdMB = prefs[MULTIPART_THRESHOLD_KEY] ?: 150,
                multipartConcurrentParts = prefs[MULTIPART_CONCURRENT_PARTS_KEY] ?: 5,
                multipartChunkSizeMB = prefs[MULTIPART_CHUNK_SIZE_KEY] ?: 10,
                uploadConcurrency = prefs[UPLOAD_CONCURRENCY_KEY] ?: 2,
                calculateMD5Enabled = prefs[CALCULATE_MD5_KEY] ?: false,
                generateThumbnailsLocally = prefs[GENERATE_THUMBNAILS_LOCALLY_KEY] ?: true,
                uploadThumbnailsRemotely = prefs[UPLOAD_THUMBNAILS_REMOTELY_KEY] ?: false,
                uploadTimeoutMs = prefs[UPLOAD_TIMEOUT_MS_KEY] ?: 300000L,
                downloadTimeoutMs = prefs[DOWNLOAD_TIMEOUT_MS_KEY] ?: 300000L
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

    suspend fun setStorageClass(profileId: String, storageClass: String) = getDataStore(profileId).edit { it[STORAGE_CLASS_KEY] = storageClass }
    suspend fun setSkipSameFileUpload(profileId: String, enabled: Boolean) = getDataStore(profileId).edit { it[SKIP_SAME_FILE_UPLOAD_KEY] = enabled }
    suspend fun setMultipartConcurrentParts(profileId: String, parts: Int) = getDataStore(profileId).edit { it[MULTIPART_CONCURRENT_PARTS_KEY] = parts }
    suspend fun setMultipartChunkSizeMB(profileId: String, sizeMB: Int) = getDataStore(profileId).edit { it[MULTIPART_CHUNK_SIZE_KEY] = sizeMB }
    suspend fun setGenerateThumbnailsLocally(profileId: String, enabled: Boolean) = getDataStore(profileId).edit { it[GENERATE_THUMBNAILS_LOCALLY_KEY] = enabled }
    suspend fun setUploadThumbnailsRemotely(profileId: String, enabled: Boolean) = getDataStore(profileId).edit { it[UPLOAD_THUMBNAILS_REMOTELY_KEY] = enabled }
    suspend fun setUploadTimeoutMs(profileId: String, timeout: Long) = getDataStore(profileId).edit { it[UPLOAD_TIMEOUT_MS_KEY] = timeout }
    suspend fun setDownloadTimeoutMs(profileId: String, timeout: Long) = getDataStore(profileId).edit { it[DOWNLOAD_TIMEOUT_MS_KEY] = timeout }
}
