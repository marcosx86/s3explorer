package net.m21xx.s3explorer.data.remote

import aws.sdk.kotlin.services.s3.model.CopyObjectRequest
import aws.sdk.kotlin.services.s3.model.Delete
import aws.sdk.kotlin.services.s3.model.DeleteObjectRequest
import aws.sdk.kotlin.services.s3.model.DeleteObjectsRequest
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.ListBucketsRequest
import aws.sdk.kotlin.services.s3.model.ListObjectsV2Request
import aws.sdk.kotlin.services.s3.model.ObjectIdentifier
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
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

    suspend fun calculateTotalStats(
        profileId: String,
        endpoint: String,
        accessKey: String,
        secretKey: String,
        bucketName: String,
        regionName: String = "us-east-1"
    ): Pair<Long, Int> {
        val s3Client = s3ClientManager.getClient(profileId, endpoint, accessKey, secretKey, regionName)
        
        var totalSize = 0L
        var totalCount = 0
        var continuationToken: String? = null

        do {
            val request = ListObjectsV2Request {
                bucket = bucketName
                this.continuationToken = continuationToken
            }
            val response = s3Client.listObjectsV2(request)
            
            response.contents?.forEach { obj ->
                totalSize += obj.size ?: 0L
                totalCount += 1
            }
            
            continuationToken = response.nextContinuationToken
        } while (response.isTruncated == true && continuationToken != null)

        return Pair(totalSize, totalCount)
    }

    suspend fun createFolder(
        profileId: String,
        endpoint: String,
        accessKey: String,
        secretKey: String,
        bucketName: String,
        folderPath: String,
        regionName: String = "us-east-1"
    ) {
        val s3Client = s3ClientManager.getClient(profileId, endpoint, accessKey, secretKey, regionName)
        val formattedKey = if (folderPath.endsWith("/")) folderPath else "$folderPath/"
        val request = PutObjectRequest {
            bucket = bucketName
            key = formattedKey
            body = ByteStream.fromBytes(ByteArray(0))
        }
        s3Client.putObject(request)
    }

    suspend fun uploadObject(
        profileId: String,
        endpoint: String,
        accessKey: String,
        secretKey: String,
        bucketName: String,
        objectKey: String,
        fileBytes: ByteArray,
        regionName: String = "us-east-1",
        storageClass: String? = null,
        contentMd5: String? = null,
        metadata: Map<String, String>? = null
    ) {
        val s3Client = s3ClientManager.getClient(profileId, endpoint, accessKey, secretKey, regionName)
        val request = PutObjectRequest {
            bucket = bucketName
            key = objectKey
            body = ByteStream.fromBytes(fileBytes)
            if (!storageClass.isNullOrEmpty()) {
                this.storageClass = aws.sdk.kotlin.services.s3.model.StorageClass.fromValue(storageClass)
            }
            if (!contentMd5.isNullOrEmpty()) {
                this.contentMd5 = contentMd5
            }
            if (metadata != null) {
                this.metadata = metadata
            }
        }
        s3Client.putObject(request)
    }

    suspend fun headObject(
        profileId: String,
        endpoint: String,
        accessKey: String,
        secretKey: String,
        bucketName: String,
        objectKey: String,
        regionName: String = "us-east-1"
    ): aws.sdk.kotlin.services.s3.model.HeadObjectResponse? {
        return try {
            val s3Client = s3ClientManager.getClient(profileId, endpoint, accessKey, secretKey, regionName)
            s3Client.headObject(aws.sdk.kotlin.services.s3.model.HeadObjectRequest {
                bucket = bucketName
                key = objectKey
            })
        } catch (e: Exception) {
            // Either object doesn't exist, or no permission
            null
        }
    }

    suspend fun deleteObject(
        profileId: String,
        endpoint: String,
        accessKey: String,
        secretKey: String,
        bucketName: String,
        objectKey: String,
        regionName: String = "us-east-1"
    ) {
        val s3Client = s3ClientManager.getClient(profileId, endpoint, accessKey, secretKey, regionName)
        s3Client.deleteObject(DeleteObjectRequest {
            bucket = bucketName
            key = objectKey
        })
    }

    suspend fun deleteObjects(
        profileId: String,
        endpoint: String,
        accessKey: String,
        secretKey: String,
        bucketName: String,
        objectKeys: List<String>,
        regionName: String = "us-east-1"
    ) {
        if (objectKeys.isEmpty()) return
        val s3Client = s3ClientManager.getClient(profileId, endpoint, accessKey, secretKey, regionName)
        
        // S3 allows deleting up to 1000 objects per request
        objectKeys.chunked(1000).forEach { chunk ->
            s3Client.deleteObjects(DeleteObjectsRequest {
                bucket = bucketName
                delete = Delete {
                    objects = chunk.map { ObjectIdentifier { key = it } }
                }
            })
        }
    }

    suspend fun copyObject(
        profileId: String,
        endpoint: String,
        accessKey: String,
        secretKey: String,
        sourceBucket: String,
        sourceKey: String,
        destinationBucket: String,
        destinationKey: String,
        regionName: String = "us-east-1"
    ) {
        val s3Client = s3ClientManager.getClient(profileId, endpoint, accessKey, secretKey, regionName)
        s3Client.copyObject(CopyObjectRequest {
            copySource = "${android.net.Uri.encode(sourceBucket)}/${android.net.Uri.encode(sourceKey)}"
            bucket = destinationBucket
            key = destinationKey
        })
    }

    suspend fun <T> getObject(
        profileId: String,
        endpoint: String,
        accessKey: String,
        secretKey: String,
        bucketName: String,
        objectKey: String,
        regionName: String = "us-east-1",
        block: suspend (aws.sdk.kotlin.services.s3.model.GetObjectResponse) -> T
    ): T {
        val s3Client = s3ClientManager.getClient(profileId, endpoint, accessKey, secretKey, regionName)
        return s3Client.getObject(GetObjectRequest {
            bucket = bucketName
            key = objectKey
        }, block)
    }
}
