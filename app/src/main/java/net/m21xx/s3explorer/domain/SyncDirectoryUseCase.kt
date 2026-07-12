package net.m21xx.s3explorer.domain

import kotlinx.coroutines.delay
import net.m21xx.s3explorer.data.local.dao.S3ObjectDao
import net.m21xx.s3explorer.data.local.entity.S3ObjectEntity
import javax.inject.Inject

class SyncDirectoryUseCase @Inject constructor(
    private val s3ObjectDao: S3ObjectDao
) {
    // Mock implementation for Phase 4 UI testing
    suspend fun execute(bucketName: String, prefix: String) {
        delay(1000) // Simulate network delay
        
        // Clear old mock data for this prefix
        s3ObjectDao.clearObjectsByPrefix(bucketName, prefix)
        
        val mockData = mutableListOf<S3ObjectEntity>()
        val currentTime = System.currentTimeMillis()
        
        // Add a few folders
        mockData.add(S3ObjectEntity("${prefix}Documents/", bucketName, 0, currentTime, true, prefix))
        mockData.add(S3ObjectEntity("${prefix}Images/", bucketName, 0, currentTime, true, prefix))
        mockData.add(S3ObjectEntity("${prefix}Work/", bucketName, 0, currentTime, true, prefix))
        
        // Add 100 mock files
        for (i in 1..100) {
            mockData.add(
                S3ObjectEntity(
                    objectKey = "${prefix}file_$i.txt",
                    bucketName = bucketName,
                    size = (Math.random() * 1024 * 1024 * 50).toLong(), // Random size up to 50MB
                    lastModified = currentTime - (Math.random() * 86400000 * 30).toLong(), // Random date within last 30 days
                    isDirectory = false,
                    parentPrefix = prefix
                )
            )
        }
        
        s3ObjectDao.insertAll(mockData)
    }
}
