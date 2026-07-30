package net.m21xx.s3explorer.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.m21xx.s3explorer.data.repository.ConnectionRepository
import net.m21xx.s3explorer.data.remote.S3ClientManager
import javax.inject.Inject

class GetFolderStatsUseCase @Inject constructor(
    private val connectionRepository: ConnectionRepository,
    private val s3ClientManager: S3ClientManager
) {
    suspend fun execute(profileId: String, bucketName: String, prefix: String): StorageStatsSummary = withContext(Dispatchers.IO) {
        val profile = connectionRepository.getProfileById(profileId)
            ?: throw IllegalArgumentException("Profile not found")
        val secretKey = connectionRepository.getProfileSecretKey(profileId)
            ?: throw IllegalArgumentException("Secret key not found")

        val s3Client = s3ClientManager.getClient(
            profileId, profile.endpointUrl, profile.accessKey, secretKey, profile.region
        )
        
        var totalSize = 0L
        var totalCount = 0
        var continuationToken: String? = null
        val formattedPrefix = if (prefix.isNotEmpty() && !prefix.endsWith("/")) "$prefix/" else prefix

        do {
            val request = aws.sdk.kotlin.services.s3.model.ListObjectsV2Request {
                bucket = bucketName
                this.prefix = formattedPrefix
                this.continuationToken = continuationToken
            }
            val response = s3Client.listObjectsV2(request)
            
            response.contents?.forEach { obj ->
                totalSize += obj.size ?: 0L
                totalCount += 1
            }
            
            continuationToken = response.nextContinuationToken
        } while (response.isTruncated == true && continuationToken != null)

        StorageStatsSummary(
            sizeBytes = totalSize,
            objectCount = totalCount,
            lastUpdated = System.currentTimeMillis()
        )
    }
}
