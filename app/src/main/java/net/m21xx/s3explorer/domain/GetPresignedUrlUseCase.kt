package net.m21xx.s3explorer.domain

import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.presigners.presignGetObject
import net.m21xx.s3explorer.data.local.dao.ConnectionProfileDao
import net.m21xx.s3explorer.data.remote.S3ClientManager
import net.m21xx.s3explorer.data.repository.ConnectionRepository
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours

class GetPresignedUrlUseCase @Inject constructor(
    private val connectionProfileDao: ConnectionProfileDao,
    private val connectionRepository: ConnectionRepository,
    private val s3ClientManager: S3ClientManager
) {
    suspend fun execute(profileId: String, bucketName: String, objectKey: String): String? {
        val profile = connectionProfileDao.getProfileById(profileId) ?: return null
        val secretKey = connectionRepository.getProfileSecretKey(profileId) ?: return null

        val s3Client = s3ClientManager.getClient(
            profileId = profileId,
            endpoint = profile.endpointUrl,
            accessKey = profile.accessKey,
            secretKey = secretKey,
            regionName = profile.region
        )

        val request = GetObjectRequest {
            bucket = bucketName
            key = objectKey
        }
        val presignedRequest = s3Client.presignGetObject(request, 1.hours)

        return presignedRequest.url.toString()
    }
}
