package net.m21xx.s3explorer.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import net.m21xx.s3explorer.data.local.entity.ConnectionProfileEntity

@Dao
interface ConnectionProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ConnectionProfileEntity): Long

    @Query("SELECT * FROM connection_profiles ORDER BY lastUsedAt DESC")
    fun getAllProfiles(): Flow<List<ConnectionProfileEntity>>

    @Query("SELECT * FROM connection_profiles WHERE profileId = :profileId LIMIT 1")
    suspend fun getProfileById(profileId: String): ConnectionProfileEntity?

    @Delete
    suspend fun deleteProfile(profile: ConnectionProfileEntity): Int

    @Query("UPDATE connection_profiles SET storageSizeBytes = :sizeBytes, storageObjectCount = :objectCount, storageLastUpdated = :lastUpdated WHERE profileId = :profileId")
    suspend fun updateStorageStats(profileId: String, sizeBytes: Long, objectCount: Int, lastUpdated: Long): Int
}
