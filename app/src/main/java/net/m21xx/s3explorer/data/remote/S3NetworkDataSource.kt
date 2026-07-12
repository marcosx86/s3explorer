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
        secretKey: String
    ): List<String> {
        val s3Client = S3Client {
            region = "us-east-1" // Dummy region, required by the SDK
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
}
