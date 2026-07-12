package net.m21xx.s3explorer.ui.explorer

import net.m21xx.s3explorer.data.local.entity.ConnectionProfileEntity
import net.m21xx.s3explorer.domain.StorageStatsSummary

data class DrawerUIState(
    val activeProfile: ConnectionProfileEntity? = null,
    val storageStats: StorageStatsSummary? = null,
    val showAboutDialog: Boolean = false,
    val showRemoveCredentialsDialog: Boolean = false
)

data class FileExplorerState(
    val profileId: String = "",
    val bucketName: String = "",
    val currentPrefix: String = "", // e.g. "Documents/Work/"
    val isSyncing: Boolean = false,
    val errorMessage: String? = null,
    val viewMode: ExplorerViewMode = ExplorerViewMode.DETAILED_LIST,
    val drawerState: DrawerUIState = DrawerUIState()
)
