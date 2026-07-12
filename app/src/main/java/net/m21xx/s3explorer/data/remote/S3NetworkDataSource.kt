package net.m21xx.s3explorer.data.remote

import aws.sdk.kotlin.services.s3.model.ListBucketsRequest
import aws.sdk.kotlin.services.s3.model.ListObjectsV2Request
import javax.inject.Inject

class S3NetworkDataSource @Inject constructor(
    private val s3ClientManager: S3ClientManager
) {

    suspend fun listBuckets(
        profileId: String,
        endpoint: String,
        accessKey: String,
        secretKey: String,
        regionName: String = "us-east-1"
    ): List<String> {
        val s3Client = s3ClientManager.getClient(profileId, endpoint, accessKey, secretKey, regionName)

        val response = s3Client.listBuckets(ListBucketsRequest {})
        return response.buckets?.mapNotNull { it.name } ?: emptyList()
    }

    suspend fun listObjects(
        profileId: String,
        endpoint: String,
        accessKey: String,
        secretKey: String,
        bucketName: String,
        prefix: String,
        regionName: String = "us-east-1"
    ): S3ListResult {
        val s3Client = s3ClientManager.getClient(profileId, endpoint, accessKey, secretKey, regionName)

        val request = ListObjectsV2Request {
            bucket = bucketName
            this.prefix = prefix
            delimiter = "/"
            maxKeys = 1000
        }
        
        val response = s3Client.listObjectsV2(request)
        
        val folders = response.commonPrefixes?.mapNotNull { it.prefix } ?: emptyList()
        val files = response.contents?.filter { it.key != prefix } ?: emptyList()
        
        return S3ListResult(folders, files)
    }
}
