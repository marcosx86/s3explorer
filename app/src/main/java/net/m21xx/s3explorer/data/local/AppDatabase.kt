package net.m21xx.s3explorer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import net.m21xx.s3explorer.data.local.dao.ConnectionProfileDao
import net.m21xx.s3explorer.data.local.dao.S3ObjectDao
import net.m21xx.s3explorer.data.local.entity.ConnectionProfileEntity
import net.m21xx.s3explorer.data.local.entity.S3ObjectEntity

@Database(entities = [ConnectionProfileEntity::class, S3ObjectEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun connectionProfileDao(): ConnectionProfileDao
    abstract fun s3ObjectDao(): S3ObjectDao
}
