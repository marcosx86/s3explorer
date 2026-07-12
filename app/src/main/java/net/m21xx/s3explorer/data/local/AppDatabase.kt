package net.m21xx.s3explorer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import net.m21xx.s3explorer.data.local.dao.ConnectionProfileDao
import net.m21xx.s3explorer.data.local.entity.ConnectionProfileEntity

@Database(entities = [ConnectionProfileEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun connectionProfileDao(): ConnectionProfileDao
}
