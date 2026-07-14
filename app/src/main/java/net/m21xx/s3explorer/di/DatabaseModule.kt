package net.m21xx.s3explorer.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.m21xx.s3explorer.data.local.AppDatabase
import net.m21xx.s3explorer.data.local.dao.ConnectionProfileDao
import net.m21xx.s3explorer.data.local.dao.S3ObjectDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        val migration1To2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `s3_objects` (
                        `objectKey` TEXT NOT NULL, 
                        `bucketName` TEXT NOT NULL, 
                        `size` INTEGER NOT NULL, 
                        `lastModified` INTEGER NOT NULL, 
                        `isDirectory` INTEGER NOT NULL, 
                        `parentPrefix` TEXT NOT NULL, 
                        PRIMARY KEY(`objectKey`)
                    )
                """.trimIndent())
            }
        }

        val migration2To3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `connection_profiles` ADD COLUMN `region` TEXT NOT NULL DEFAULT 'us-east-1'")
            }
        }

        val migration3To4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `connection_profiles` ADD COLUMN `lastUsedAt` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("DROP TABLE IF EXISTS `s3_objects`")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `s3_objects` (
                        `profileId` TEXT NOT NULL, 
                        `objectKey` TEXT NOT NULL, 
                        `bucketName` TEXT NOT NULL, 
                        `size` INTEGER NOT NULL, 
                        `lastModified` INTEGER NOT NULL, 
                        `isDirectory` INTEGER NOT NULL, 
                        `parentPrefix` TEXT NOT NULL, 
                        PRIMARY KEY(`profileId`, `bucketName`, `objectKey`)
                    )
                """.trimIndent())
            }
        }

        val migration4To5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `connection_profiles` ADD COLUMN `storageSizeBytes` INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE `connection_profiles` ADD COLUMN `storageObjectCount` INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE `connection_profiles` ADD COLUMN `storageLastUpdated` INTEGER DEFAULT NULL")
            }
        }

        val migration5To6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `s3_objects` ADD COLUMN `extension` TEXT NOT NULL DEFAULT ''")
            }
        }

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "s3explorer_database"
        )
        .addMigrations(
            migration1To2,
            migration2To3,
            migration3To4,
            migration4To5,
            migration5To6
        )
        .build()
    }

    @Provides
    fun provideConnectionProfileDao(database: AppDatabase): ConnectionProfileDao {
        return database.connectionProfileDao()
    }

    @Provides
    fun provideS3ObjectDao(database: AppDatabase): S3ObjectDao {
        return database.s3ObjectDao()
    }
}
