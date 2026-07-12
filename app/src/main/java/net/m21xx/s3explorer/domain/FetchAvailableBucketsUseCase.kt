package net.m21xx.s3explorer.domain

import net.m21xx.s3explorer.data.remote.S3ClientManager
import net.m21xx.s3explorer.data.remote.S3NetworkDataSource
import javax.inject.Inject

class FetchAvailableBucketsUseCase @Inject constructor(
    private val s3NetworkDataSource: S3NetworkDataSource,
    private val s3ClientManager: S3ClientManager
) {
    suspend fun execute(
        endpointUrl: String,
        accessKey: String,
        secretKey: String,
        region: String = "us-east-1"
    ): Result<List<String>> {
        val tempProfileId = "TEMP_${accessKey}"
        return try {
            val buckets = s3NetworkDataSource.listBuckets(
                profileId = tempProfileId,
                endpoint = endpointUrl,
                accessKey = accessKey,
                secretKey = secretKey,
                regionName = region
            )
            Result.success(buckets)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            s3ClientManager.invalidateClient(tempProfileId)
        }
    }
}
