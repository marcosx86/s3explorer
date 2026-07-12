package net.m21xx.s3explorer.domain

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.presigners.presignGetObject
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.net.url.Url
import net.m21xx.s3explorer.data.local.dao.ConnectionProfileDao
import net.m21xx.s3explorer.data.repository.ConnectionRepository
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours

class GetPresignedUrlUseCase @Inject constructor(
    private val connectionProfileDao: ConnectionProfileDao,
    private val connectionRepository: ConnectionRepository
) {
    suspend fun execute(profileId: String, bucketName: String, objectKey: String): String? {
        val profile = connectionProfileDao.getProfileById(profileId) ?: return null
        val secretKey = connectionRepository.getProfileSecretKey(profileId) ?: return null

        val s3Client = S3Client {
            region = profile.region.ifBlank { "us-east-1" }
            credentialsProvider = StaticCredentialsProvider(
                Credentials(
                    accessKeyId = profile.accessKey,
                    secretAccessKey = secretKey
                )
            )
            endpointUrl = Url.parse(profile.endpointUrl)
            forcePathStyle = true
        }

        val request = GetObjectRequest {
            bucket = bucketName
            key = objectKey
        }
        val presignedRequest = s3Client.presignGetObject(request, 1.hours)

        return presignedRequest.url.toString()
    }
}
