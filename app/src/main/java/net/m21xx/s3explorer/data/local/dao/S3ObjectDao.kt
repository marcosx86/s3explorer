package net.m21xx.s3explorer.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import net.m21xx.s3explorer.data.local.entity.S3ObjectEntity

@Dao
interface S3ObjectDao {
    @RawQuery(observedEntities = [S3ObjectEntity::class])
    fun getObjectsByPrefixDynamic(query: SupportSQLiteQuery): PagingSource<Int, S3ObjectEntity>

    @Query("SELECT * FROM s3_objects WHERE profileId = :profileId AND bucketName = :bucketName AND parentPrefix = :parentPrefix ORDER BY isDirectory DESC, objectKey ASC")
    fun getObjectsByPrefix(profileId: String, bucketName: String, parentPrefix: String): PagingSource<Int, S3ObjectEntity>

    @Query("SELECT * FROM s3_objects WHERE profileId = :profileId AND bucketName = :bucketName AND parentPrefix = :parentPrefix AND isDirectory = 0 ORDER BY objectKey ASC")
    suspend fun getAllFilesByPrefix(profileId: String, bucketName: String, parentPrefix: String): List<S3ObjectEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(objects: List<S3ObjectEntity>): List<Long>

    @Query("DELETE FROM s3_objects WHERE profileId = :profileId AND bucketName = :bucketName AND parentPrefix = :parentPrefix")
    suspend fun clearObjectsByPrefix(profileId: String, bucketName: String, parentPrefix: String): Int
    
    @Query("DELETE FROM s3_objects WHERE profileId = :profileId")
    suspend fun clearObjectsByProfileId(profileId: String): Int
    
    @Query("DELETE FROM s3_objects")
    suspend fun clearAll(): Int
}
