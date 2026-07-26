package net.m21xx.s3explorer.domain

import net.m21xx.s3explorer.data.local.dao.ConnectionProfileDao
import net.m21xx.s3explorer.data.remote.S3NetworkDataSource
import net.m21xx.s3explorer.data.repository.ConnectionRepository
import javax.inject.Inject

class CreateFolderUseCase @Inject constructor(
    private val connectionProfileDao: ConnectionProfileDao,
    private val connectionRepository: ConnectionRepository,
    private val s3NetworkDataSource: S3NetworkDataSource
) {
    suspend fun execute(profileId: String, bucketName: String, folderPath: String) {
        val profile = connectionProfileDao.getProfileById(profileId) ?: return
        val secretKey = connectionRepository.getProfileSecretKey(profileId) ?: return
        
        s3NetworkDataSource.createFolder(
            profileId = profileId,
            endpoint = profile.endpointUrl,
            accessKey = profile.accessKey,
            secretKey = secretKey,
            bucketName = bucketName,
            folderPath = folderPath,
            regionName = profile.region
        )
    }
}
