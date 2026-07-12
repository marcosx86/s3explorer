package net.m21xx.s3explorer.domain

import net.m21xx.s3explorer.data.remote.S3NetworkDataSource
import javax.inject.Inject

class FetchAvailableBucketsUseCase @Inject constructor(
    private val s3NetworkDataSource: S3NetworkDataSource
) {
    suspend fun execute(
        endpointUrl: String,
        accessKey: String,
        secretKey: String
    ): Result<List<String>> {
        return try {
            val buckets = s3NetworkDataSource.listBuckets(
                endpoint = endpointUrl,
                accessKey = accessKey,
                secretKey = secretKey
            )
            Result.success(buckets)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
