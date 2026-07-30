package net.m21xx.s3explorer.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.m21xx.s3explorer.data.remote.S3NetworkDataSource
import net.m21xx.s3explorer.data.repository.ConnectionRepository
import net.m21xx.s3explorer.data.remote.S3ClientManager
import javax.inject.Inject

class DeleteObjectUseCase @Inject constructor(
    private val s3NetworkDataSource: S3NetworkDataSource,
    private val connectionRepository: ConnectionRepository,
    private val s3ClientManager: S3ClientManager
) {
    suspend fun execute(profileId: String, bucketName: String, objectKeys: List<String>) = withContext(Dispatchers.IO) {
        val profile = connectionRepository.getProfileById(profileId)
            ?: throw IllegalArgumentException("Profile not found")
        val secretKey = connectionRepository.getProfileSecretKey(profileId)
            ?: throw IllegalArgumentException("Secret key not found")

        val filesToDelete = mutableListOf<String>()

        for (key in objectKeys) {
            if (key.endsWith("/")) {
                // It's a folder. We must list all objects inside it and delete them.
                var continuationToken: String? = null
                do {
                    val s3Client = s3ClientManager.getClient(
                        profileId, profile.endpointUrl, profile.accessKey, secretKey, profile.region
                    )
                    val request = aws.sdk.kotlin.services.s3.model.ListObjectsV2Request {
                        this.bucket = bucketName
                        this.prefix = key
                        this.continuationToken = continuationToken
                    }
                    val response = s3Client.listObjectsV2(request)
                    response.contents?.forEach { obj ->
                        obj.key?.let { filesToDelete.add(it) }
                    }
                    continuationToken = response.nextContinuationToken
                } while (response.isTruncated == true && continuationToken != null)
            } else {
                filesToDelete.add(key)
            }
        }

        if (filesToDelete.isNotEmpty()) {
            s3NetworkDataSource.deleteObjects(
                profileId = profileId,
                endpoint = profile.endpointUrl,
                accessKey = profile.accessKey,
                secretKey = secretKey,
                bucketName = bucketName,
                objectKeys = filesToDelete,
                regionName = profile.region
            )
        }
    }
}
