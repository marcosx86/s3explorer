package net.m21xx.s3explorer.ui.explorer

data class FileExplorerState(
    val bucketName: String = "",
    val currentPrefix: String = "", // e.g. "Documents/Work/"
    val isSyncing: Boolean = false
)
