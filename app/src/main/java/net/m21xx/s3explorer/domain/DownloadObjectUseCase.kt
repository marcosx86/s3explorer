package net.m21xx.s3explorer.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.m21xx.s3explorer.data.remote.S3NetworkDataSource
import net.m21xx.s3explorer.data.repository.ConnectionRepository
import net.m21xx.s3explorer.domain.transfer.TransferManager
import aws.smithy.kotlin.runtime.content.writeToFile
import java.io.File
import javax.inject.Inject

class DownloadObjectUseCase @Inject constructor(
    private val s3NetworkDataSource: S3NetworkDataSource,
    private val connectionRepository: ConnectionRepository,
    private val transferManager: TransferManager
) {
    suspend fun execute(profileId: String, bucketName: String, objectKey: String, downloadDir: File) = withContext(Dispatchers.IO) {
        val profile = connectionRepository.getProfileById(profileId)
            ?: throw IllegalArgumentException("Profile not found")
        val secretKey = connectionRepository.getProfileSecretKey(profileId)
            ?: throw IllegalArgumentException("Secret key not found")
        
        val fileName = objectKey.substringAfterLast("/")
        val destFile = File(downloadDir, fileName)
        
        val transferId = transferManager.addDownload(
            profileId = profileId,
            bucketName = bucketName,
            objectKey = objectKey,
            fileName = fileName
        )
        
        try {
            s3NetworkDataSource.getObject(
                profileId = profileId,
                endpoint = profile.endpointUrl,
                accessKey = profile.accessKey,
                secretKey = secretKey,
                bucketName = bucketName,
                objectKey = objectKey,
                regionName = profile.region
            ) { response ->
                val totalBytes = response.contentLength ?: 0L
                transferManager.updateTransferTotalBytes(transferId, totalBytes)
                
                response.body?.writeToFile(destFile)
                
                transferManager.updateTransferProgress(transferId, totalBytes, 0L)
            }
            transferManager.updateTransferStatus(transferId, net.m21xx.s3explorer.domain.transfer.TransferStatus.COMPLETED)
        } catch (e: Exception) {
            transferManager.updateTransferStatus(transferId, net.m21xx.s3explorer.domain.transfer.TransferStatus.FAILED, e.message)
            if (destFile.exists()) {
                destFile.delete()
            }
            throw e
        }
    }
}
