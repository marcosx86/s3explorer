package net.m21xx.s3explorer.domain

import net.m21xx.s3explorer.data.local.dao.ConnectionProfileDao
import net.m21xx.s3explorer.data.repository.ConnectionRepository
import javax.inject.Inject

class GenerateRcloneConfigUseCase @Inject constructor(
    private val connectionProfileDao: ConnectionProfileDao,
    private val connectionRepository: ConnectionRepository
) {
    suspend fun execute(profileId: String): String? {
        val profile = connectionProfileDao.getProfileById(profileId) ?: return null
        val secretKey = connectionRepository.getProfileSecretKey(profileId) ?: return null
        
        val sectionName = profile.alias.ifBlank { profile.defaultBucket.ifBlank { "s3-endpoint" } }
        val formattedSection = sectionName.replace(Regex("[^a-zA-Z0-9_-]"), "-")
        
        return """
            [$formattedSection]
            type = s3
            provider = Other
            env_auth = false
            access_key_id = ${profile.accessKey}
            secret_access_key = $secretKey
            endpoint = ${profile.endpointUrl}
            region = ${profile.region}
        """.trimIndent()
    }
}
