package net.m21xx.s3explorer.domain

import net.m21xx.s3explorer.data.local.dao.ConnectionProfileDao
import javax.inject.Inject

class SetCustomNameUseCase @Inject constructor(
    private val connectionProfileDao: ConnectionProfileDao
) {
    suspend fun execute(profileId: String, newName: String) {
        val profile = connectionProfileDao.getProfileById(profileId) ?: return
        val updatedProfile = profile.copy(alias = newName)
        connectionProfileDao.insertProfile(updatedProfile)
    }
}
