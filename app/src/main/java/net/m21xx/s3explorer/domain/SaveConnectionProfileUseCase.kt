package net.m21xx.s3explorer.domain

import net.m21xx.s3explorer.data.local.entity.ConnectionProfileEntity
import net.m21xx.s3explorer.data.repository.ConnectionRepository
import java.util.UUID
import javax.inject.Inject

class SaveConnectionProfileUseCase @Inject constructor(
    private val repository: ConnectionRepository
) {
    suspend fun execute(
        alias: String,
        endpointUrl: String,
        accessKey: String,
        secretKey: String,
        defaultBucket: String,
        region: String = "us-east-1"
    ): String {
        val profile = ConnectionProfileEntity(
            profileId = UUID.randomUUID().toString(),
            alias = alias.ifBlank { endpointUrl },
            endpointUrl = endpointUrl,
            accessKey = accessKey,
            defaultBucket = defaultBucket,
            region = region.ifBlank { "us-east-1" }
        )
        
        repository.saveProfile(profile, secretKey)
        return profile.profileId
    }
}
