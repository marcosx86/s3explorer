package net.m21xx.s3explorer.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.m21xx.s3explorer.data.local.dao.ConnectionProfileDao
import net.m21xx.s3explorer.data.remote.S3NetworkDataSource
import net.m21xx.s3explorer.data.repository.ConnectionRepository
import javax.inject.Inject

data class StorageStatsSummary(
    val sizeBytes: Long,
    val objectCount: Int,
    val lastUpdated: Long
)

class CalculateStorageStatsUseCase @Inject constructor(
    private val connectionProfileDao: ConnectionProfileDao,
    private val connectionRepository: ConnectionRepository,
    private val networkDataSource: S3NetworkDataSource
) {
    /**
     * Executes an iterative bucket crawling to calculate accurate storage stats 
     * by fetching remote object details via S3 client.
     */
    suspend fun execute(profileId: String, bucketName: String): StorageStatsSummary? = withContext(Dispatchers.IO) {
        val profile = connectionProfileDao.getProfileById(profileId) ?: return@withContext null
        val secretKey = connectionRepository.getProfileSecretKey(profileId) ?: return@withContext null

        val (totalSize, totalCount) = networkDataSource.calculateTotalStats(
            profileId = profileId,
            endpoint = profile.endpointUrl,
            accessKey = profile.accessKey,
            secretKey = secretKey,
            bucketName = bucketName,
            regionName = profile.region
        )

        val lastUpdated = System.currentTimeMillis()
        connectionProfileDao.updateStorageStats(profileId, totalSize, totalCount, lastUpdated)

        StorageStatsSummary(
            sizeBytes = totalSize,
            objectCount = totalCount,
            lastUpdated = lastUpdated
        )
    }
}
