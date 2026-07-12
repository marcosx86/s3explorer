package net.m21xx.s3explorer.domain

import aws.smithy.kotlin.runtime.time.epochMilliseconds
import net.m21xx.s3explorer.data.local.dao.ConnectionProfileDao
import net.m21xx.s3explorer.data.local.dao.S3ObjectDao
import net.m21xx.s3explorer.data.local.entity.S3ObjectEntity
import net.m21xx.s3explorer.data.remote.S3NetworkDataSource
import net.m21xx.s3explorer.data.repository.ConnectionRepository
import javax.inject.Inject

class SyncDirectoryUseCase @Inject constructor(
    private val s3ObjectDao: S3ObjectDao,
    private val connectionProfileDao: ConnectionProfileDao,
    private val connectionRepository: ConnectionRepository,
    private val s3NetworkDataSource: S3NetworkDataSource
) {
    suspend fun execute(profileId: String, bucketName: String, prefix: String) {
        // Fetch credentials
        val profile = connectionProfileDao.getProfileById(profileId) ?: return
        val secretKey = connectionRepository.getProfileSecretKey(profileId) ?: return
        
        // Fetch from S3
        val s3Result = s3NetworkDataSource.listObjects(
            profileId = profileId,
            endpoint = profile.endpointUrl,
            accessKey = profile.accessKey,
            secretKey = secretKey,
            bucketName = bucketName,
            prefix = prefix,
            regionName = profile.region
        )
        
        // Clear old local cache for this prefix
        s3ObjectDao.clearObjectsByPrefix(profileId, bucketName, prefix)
        
        val entities = mutableListOf<S3ObjectEntity>()
        
        // Map Folders (CommonPrefixes)
        s3Result.folders.forEach { folderPrefix ->
            entities.add(
                S3ObjectEntity(
                    profileId = profileId,
                    objectKey = folderPrefix,
                    bucketName = bucketName,
                    size = 0,
                    lastModified = System.currentTimeMillis(), // Folders don't have a real last modified
                    isDirectory = true,
                    parentPrefix = prefix
                )
            )
        }
        
        // Map Files (Contents)
        s3Result.files.forEach { file ->
            val fileKey = file.key ?: return@forEach
            entities.add(
                S3ObjectEntity(
                    profileId = profileId,
                    objectKey = fileKey,
                    bucketName = bucketName,
                    size = file.size ?: 0L,
                    lastModified = file.lastModified?.epochMilliseconds ?: 0L,
                    isDirectory = false,
                    parentPrefix = prefix
                )
            )
        }
        
        s3ObjectDao.insertAll(entities)
    }
}
