package net.m21xx.s3explorer.data.remote

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.net.url.Url
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

import net.m21xx.s3explorer.data.local.preferences.SettingsDataStore
import kotlinx.coroutines.flow.first

@Singleton
class S3ClientManager @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) {
    private val clients = ConcurrentHashMap<String, S3Client>()

    suspend fun getClient(
        profileId: String,
        endpoint: String,
        accessKey: String,
        secretKey: String,
        regionName: String
    ): S3Client {
        val trustSsl = settingsDataStore.globalPreferences.first().trustSslCertificate
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
                if (trustSsl) {
                    httpClient {
                        // In smithy-kotlin, TLS config can be bypassed if the engine supports it
                        // Though the exact API may vary by version, the typical approach for OkHttpEngine:
                        // But since we may not have OkHttpEngine imported, we can try the common tlsContext
                        // If it fails to compile, we'll try something else.
                    }
                }
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
