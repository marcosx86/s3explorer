package net.m21xx.s3explorer.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.m21xx.s3explorer.data.local.dao.ConnectionProfileDao
import net.m21xx.s3explorer.data.local.security.SecureStorage
import net.m21xx.s3explorer.data.remote.S3ClientManager
import net.m21xx.s3explorer.data.repository.ConnectionRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideConnectionRepository(
        connectionProfileDao: ConnectionProfileDao,
        secureStorage: SecureStorage,
        s3ClientManager: S3ClientManager
    ): ConnectionRepository {
        return ConnectionRepository(connectionProfileDao, secureStorage, s3ClientManager)
    }
}
