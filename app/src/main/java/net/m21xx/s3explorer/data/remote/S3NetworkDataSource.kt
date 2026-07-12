package net.m21xx.s3explorer.data.remote

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.ListBucketsRequest
import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.net.url.Url
import javax.inject.Inject

class S3NetworkDataSource @Inject constructor() {

    suspend fun listBuckets(
        endpoint: String,
        accessKey: String,
        secretKey: String,
        regionName: String = "us-east-1"
    ): List<String> {
        val s3Client = S3Client {
            region = regionName.ifBlank { "us-east-1" }
            credentialsProvider = StaticCredentialsProvider(
                Credentials(
                    accessKeyId = accessKey,
                    secretAccessKey = secretKey
                )
            )
            endpointUrl = Url.parse(endpoint) // Use parsed URL
            forcePathStyle = true
        }

        return s3Client.use { client ->
            val response = client.listBuckets(ListBucketsRequest {})
            response.buckets?.mapNotNull { it.name } ?: emptyList()
        }
    }

    suspend fun listObjects(
        endpoint: String,
        accessKey: String,
        secretKey: String,
        bucketName: String,
        prefix: String,
        regionName: String = "us-east-1"
    ): S3ListResult {
        val s3Client = S3Client {
            region = regionName.ifBlank { "us-east-1" }
            credentialsProvider = StaticCredentialsProvider(
                Credentials(accessKeyId = accessKey, secretAccessKey = secretKey)
            )
            endpointUrl = Url.parse(endpoint)
            forcePathStyle = true
        }

        return s3Client.use { client ->
            val request = aws.sdk.kotlin.services.s3.model.ListObjectsV2Request {
                bucket = bucketName
                this.prefix = prefix
                delimiter = "/"
                maxKeys = 1000
            }
            
            val response = client.listObjectsV2(request)
            
            val folders = response.commonPrefixes?.mapNotNull { it.prefix } ?: emptyList()
            // Filter out the prefix itself (S3 sometimes returns the folder itself as a 0-byte file)
            val files = response.contents?.filter { it.key != prefix } ?: emptyList()
            
            S3ListResult(folders, files)
        }
    }
}
