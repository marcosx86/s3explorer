package net.m21xx.s3explorer.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ForceSyncUseCase @Inject constructor(
    private val syncDirectoryUseCase: SyncDirectoryUseCase
) {
    /**
     * Bypasses the local cache and forces a fresh network fetch from S3 for the current prefix.
     * In the future, this will explicitly clear the Room cache for this prefix before syncing.
     * For now, we will reuse SyncDirectoryUseCase which currently overrides the cache anyway.
     */
    suspend fun execute(profileId: String, bucketName: String, prefix: String) = withContext(Dispatchers.IO) {
        // TODO: Clear S3ObjectDao cache for this prefix first to ensure a hard remote fetch.
        syncDirectoryUseCase.execute(profileId, bucketName, prefix)
    }
}
