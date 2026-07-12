package net.m21xx.s3explorer.domain

import net.m21xx.s3explorer.data.local.dao.ConnectionProfileDao
import net.m21xx.s3explorer.data.local.dao.S3ObjectDao
import net.m21xx.s3explorer.data.repository.ConnectionRepository
import javax.inject.Inject

class DeleteProfileUseCase @Inject constructor(
    private val connectionProfileDao: ConnectionProfileDao,
    private val s3ObjectDao: S3ObjectDao,
    private val connectionRepository: ConnectionRepository
) {
    suspend fun execute(profileId: String) {
        val profile = connectionProfileDao.getProfileById(profileId) ?: return
        
        // 1. Clear cached S3 objects for this profile
        s3ObjectDao.clearObjectsByProfileId(profileId)
        
        // 2. Delete securely stored credentials and the profile itself
        connectionRepository.deleteProfile(profile)
    }
}
