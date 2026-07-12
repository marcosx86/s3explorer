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
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "s3explorer_database"
        )
        .fallbackToDestructiveMigration()
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
