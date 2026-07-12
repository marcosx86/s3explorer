package net.m21xx.s3explorer.ui.explorer

data class FileExplorerState(
    val profileId: String = "",
    val bucketName: String = "",
    val currentPrefix: String = "", // e.g. "Documents/Work/"
    val isSyncing: Boolean = false,
    val errorMessage: String? = null
)
