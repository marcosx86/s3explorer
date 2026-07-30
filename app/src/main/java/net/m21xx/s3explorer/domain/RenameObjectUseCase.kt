package net.m21xx.s3explorer.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.m21xx.s3explorer.data.remote.S3NetworkDataSource
import net.m21xx.s3explorer.data.repository.ConnectionRepository
import javax.inject.Inject

class RenameObjectUseCase @Inject constructor(
    private val s3NetworkDataSource: S3NetworkDataSource,
    private val connectionRepository: ConnectionRepository
) {
    suspend fun execute(profileId: String, bucketName: String, oldKey: String, newKey: String) = withContext(Dispatchers.IO) {
        val profile = connectionRepository.getProfileById(profileId)
            ?: throw IllegalArgumentException("Profile not found")
        val secretKey = connectionRepository.getProfileSecretKey(profileId)
            ?: throw IllegalArgumentException("Secret key not found")

        if (oldKey.endsWith("/")) {
            throw UnsupportedOperationException("Renaming folders is currently not supported.")
        }

        // Step 1: Copy to new key
        s3NetworkDataSource.copyObject(
            profileId = profileId,
            endpoint = profile.endpointUrl,
            accessKey = profile.accessKey,
            secretKey = secretKey,
            sourceBucket = bucketName,
            sourceKey = oldKey,
            destinationBucket = bucketName,
            destinationKey = newKey,
            regionName = profile.region
        )

        // Step 2: Delete old key
        s3NetworkDataSource.deleteObject(
            profileId = profileId,
            endpoint = profile.endpointUrl,
            accessKey = profile.accessKey,
            secretKey = secretKey,
            bucketName = bucketName,
            objectKey = oldKey,
            regionName = profile.region
        )
    }
}
