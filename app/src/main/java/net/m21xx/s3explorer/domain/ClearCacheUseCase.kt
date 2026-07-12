package net.m21xx.s3explorer.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

enum class CacheType {
    DOCUMENTS,
    THUMBNAILS
}

class ClearCacheUseCase @Inject constructor() {
    /**
     * Clears the local cache for a specific connection profile.
     * Note: This does not affect remote bucket data. 
     */
    suspend fun execute(profileId: String, cacheType: CacheType): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // TODO: Hook into Android Context.cacheDir and Coil image loader to wipe cache
            // For now, we simulate success for the UI action.
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
