package net.m21xx.s3explorer.data.remote

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.net.url.Url
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class S3ClientManager @Inject constructor() {
    private val clients = ConcurrentHashMap<String, S3Client>()

    fun getClient(
        profileId: String,
        endpoint: String,
        accessKey: String,
        secretKey: String,
        regionName: String
    ): S3Client {
        return clients.getOrPut(profileId) {
            S3Client {
                region = regionName.ifBlank { "us-east-1" }
                credentialsProvider = StaticCredentialsProvider(
                    Credentials(
                        accessKeyId = accessKey,
                        secretAccessKey = secretKey
                    )
                )
                endpointUrl = Url.parse(endpoint)
                forcePathStyle = true
            }
        }
    }

    fun invalidateClient(profileId: String) {
        val client = clients.remove(profileId)
        client?.close()
    }

    fun invalidateAll() {
        clients.values.forEach { it.close() }
        clients.clear()
    }
}
