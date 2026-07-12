package net.m21xx.s3explorer.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.m21xx.s3explorer.data.local.dao.S3ObjectDao
import javax.inject.Inject

data class StorageStatsSummary(
    val sizeBytes: Long,
    val objectCount: Int,
    val lastUpdated: Long
)

class CalculateStorageStatsUseCase @Inject constructor(
    private val s3ObjectDao: S3ObjectDao
) {
    /**
     * Executes a naive calculation of storage stats using the locally cached Room DB rows.
     * Note: This is an initial implementation. Full recursive remote bucket calculation
     * will be implemented in a future epic.
     */
    suspend fun execute(profileId: String, bucketName: String): StorageStatsSummary = withContext(Dispatchers.IO) {
        // Return placeholder for the UI layout while we implement the drawer
        StorageStatsSummary(
            sizeBytes = 710_000_000L, // Placeholder 710 MB
            objectCount = 1824, // Placeholder 1824 files
            lastUpdated = System.currentTimeMillis()
        )
    }
}
