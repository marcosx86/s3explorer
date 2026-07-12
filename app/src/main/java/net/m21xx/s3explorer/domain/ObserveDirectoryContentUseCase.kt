package net.m21xx.s3explorer.domain

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.m21xx.s3explorer.data.local.dao.S3ObjectDao
import net.m21xx.s3explorer.data.local.entity.S3ObjectEntity
import javax.inject.Inject

class ObserveDirectoryContentUseCase @Inject constructor(
    private val s3ObjectDao: S3ObjectDao
) {
    fun execute(profileId: String, bucketName: String, parentPrefix: String): Flow<PagingData<S3ObjectEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = 50,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { s3ObjectDao.getObjectsByPrefix(profileId, bucketName, parentPrefix) }
        ).flow
    }
}
