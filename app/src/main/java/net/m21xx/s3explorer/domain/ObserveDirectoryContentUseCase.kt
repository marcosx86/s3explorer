package net.m21xx.s3explorer.domain

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.m21xx.s3explorer.data.local.dao.S3ObjectDao
import net.m21xx.s3explorer.data.local.entity.S3ObjectEntity
import javax.inject.Inject

import androidx.sqlite.db.SimpleSQLiteQuery
import net.m21xx.s3explorer.ui.explorer.SortBy
import net.m21xx.s3explorer.ui.explorer.SortDirection

class ObserveDirectoryContentUseCase @Inject constructor(
    private val s3ObjectDao: S3ObjectDao
) {
    fun execute(
        profileId: String, 
        bucketName: String, 
        parentPrefix: String,
        sortBy: SortBy,
        sortDirection: SortDirection,
        showHidden: Boolean
    ): Flow<PagingData<S3ObjectEntity>> {
        val queryString = buildString {
            append("SELECT * FROM s3_objects WHERE profileId = ? AND bucketName = ? AND parentPrefix = ? ")
            if (!showHidden) {
                // objectKey represents the full path. If we want to hide dotfiles at the current directory level, 
                // we should check if the part of objectKey after parentPrefix starts with a dot.
                // In SQLite: objectKey NOT LIKE :parentPrefix || '.%'
                append("AND objectKey NOT LIKE ? ")
            }
            append("ORDER BY isDirectory DESC, ")
            
            val sortField = when (sortBy) {
                SortBy.NAME -> "objectKey"
                SortBy.SIZE -> "size"
                SortBy.TYPE -> "extension"
                SortBy.LAST_MODIFIED -> "lastModified"
            }
            val sortDir = if (sortDirection == SortDirection.ASCENDING) "ASC" else "DESC"
            append("$sortField $sortDir")
            
            // Secondary sort by name
            if (sortBy != SortBy.NAME) {
                append(", objectKey ASC")
            }
        }
        
        val args = mutableListOf<Any>(profileId, bucketName, parentPrefix)
        if (!showHidden) {
            args.add(parentPrefix + ".%")
        }
        
        val query = SimpleSQLiteQuery(queryString, args.toTypedArray())

        return Pager(
            config = PagingConfig(
                pageSize = 50,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { s3ObjectDao.getObjectsByPrefixDynamic(query) }
        ).flow
    }
}
