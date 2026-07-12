package net.m21xx.s3explorer.data.repository

import kotlinx.coroutines.flow.Flow
import net.m21xx.s3explorer.data.local.dao.ConnectionProfileDao
import net.m21xx.s3explorer.data.local.entity.ConnectionProfileEntity
import net.m21xx.s3explorer.data.local.security.SecureStorage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectionRepository @Inject constructor(
    private val connectionProfileDao: ConnectionProfileDao,
    private val secureStorage: SecureStorage
) {
    val allProfiles: Flow<List<ConnectionProfileEntity>> = connectionProfileDao.getAllProfiles()

    suspend fun saveProfile(profile: ConnectionProfileEntity, secretKey: String) {
        // Save the sensitive key in EncryptedSharedPreferences
        secureStorage.saveSecretKey(profile.profileId, secretKey)
        
        // Save the metadata in Room
        connectionProfileDao.insertProfile(profile)
    }

    suspend fun getProfileSecretKey(profileId: String): String? {
        return secureStorage.getSecretKey(profileId)
    }

    suspend fun deleteProfile(profile: ConnectionProfileEntity) {
        secureStorage.deleteSecretKey(profile.profileId)
        connectionProfileDao.deleteProfile(profile)
    }
}
