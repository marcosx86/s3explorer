package net.m21xx.s3explorer.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import net.m21xx.s3explorer.ui.connection.NewConnectionScreen
import net.m21xx.s3explorer.ui.connection.ConnectionsListScreen
import net.m21xx.s3explorer.ui.explorer.FileExplorerScreen

object Destinations {
    const val CONNECTIONS_LIST = "connections_list"
    const val NEW_CONNECTION = "new_connection?reuseProfileId={reuseProfileId}"
    fun newConnectionRoute(reuseProfileId: String? = null) = if (reuseProfileId != null) "new_connection?reuseProfileId=$reuseProfileId" else "new_connection"
    const val FILE_EXPLORER = "file_explorer/{profileId}/{bucketName}"
    fun fileExplorerRoute(profileId: String, bucketName: String) = "file_explorer/$profileId/$bucketName"

    const val MEDIA_VIEWER = "media_viewer/{profileId}/{bucketName}?parentPrefix={parentPrefix}&initialObjectKey={initialObjectKey}"
    fun mediaViewerRoute(profileId: String, bucketName: String, parentPrefix: String, initialObjectKey: String) =
        "media_viewer/$profileId/$bucketName?parentPrefix=${android.net.Uri.encode(parentPrefix)}&initialObjectKey=${android.net.Uri.encode(initialObjectKey)}"

    const val ACCOUNT_SETTINGS = "account_settings/{profileId}"
    fun accountSettingsRoute(profileId: String) = "account_settings/$profileId"

    const val TRANSFERS = "transfers/{profileId}"
    fun transfersRoute(profileId: String) = "transfers/$profileId"

    const val TRASH = "trash/{profileId}"
    fun trashRoute(profileId: String) = "trash/$profileId"

    const val GLOBAL_SETTINGS = "global_settings"
    const val MEDIA_BACKUP = "media_backup"
    const val ABOUT = "about"
}

@Composable
fun S3NavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Destinations.CONNECTIONS_LIST
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Destinations.CONNECTIONS_LIST) {
            ConnectionsListScreen(
                onNavigateToNewConnection = { reuseProfileId ->
                    navController.navigate(Destinations.newConnectionRoute(reuseProfileId))
                },
                onNavigateToExplorer = { profileId, bucketNameRaw ->
                    val bucketName = bucketNameRaw.ifBlank { "unknown" }
                    navController.navigate(Destinations.fileExplorerRoute(profileId, bucketName)) {
                        popUpTo(Destinations.CONNECTIONS_LIST) { inclusive = false }
                    }
                }
            )
        }

        composable(
            route = Destinations.NEW_CONNECTION,
            arguments = listOf(
                navArgument("reuseProfileId") { 
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) {
            NewConnectionScreen(
                onConnectionSuccess = { profileId, bucketNameRaw ->
                    val bucketName = bucketNameRaw.ifBlank { "unknown" }
                    navController.navigate(Destinations.fileExplorerRoute(profileId, bucketName)) {
                        popUpTo(Destinations.CONNECTIONS_LIST) { inclusive = false }
                    }
                }
            )
        }
        
        composable(
            route = Destinations.FILE_EXPLORER,
            arguments = listOf(
                navArgument("profileId") { type = NavType.StringType },
                navArgument("bucketName") { type = NavType.StringType }
            )
        ) {
            FileExplorerScreen(
                onNavigateToConnections = {
                    navController.popBackStack(Destinations.CONNECTIONS_LIST, false)
                },
                onNavigateToMediaViewer = { profileId, bucketName, parentPrefix, initialObjectKey ->
                    navController.navigate(
                        Destinations.mediaViewerRoute(profileId, bucketName, parentPrefix, initialObjectKey)
                    )
                },
                onNavigateToAccountSettings = { profileId ->
                    navController.navigate(Destinations.accountSettingsRoute(profileId))
                },
                onNavigateToTransfers = { profileId ->
                    navController.navigate(Destinations.transfersRoute(profileId))
                },
                onNavigateToSettings = {
                    navController.navigate(Destinations.GLOBAL_SETTINGS)
                },
                onNavigateToMediaBackup = {
                    navController.navigate(Destinations.MEDIA_BACKUP)
                },
                onNavigateToTrash = { profileId ->
                    navController.navigate(Destinations.trashRoute(profileId))
                },
                onNavigateToAbout = {
                    navController.navigate(Destinations.ABOUT)
                }
            )
        }

        composable(
            route = Destinations.MEDIA_VIEWER,
            arguments = listOf(
                navArgument("profileId") { type = NavType.StringType },
                navArgument("bucketName") { type = NavType.StringType },
                navArgument("parentPrefix") { type = NavType.StringType; defaultValue = "" },
                navArgument("initialObjectKey") { type = NavType.StringType; defaultValue = "" }
            )
        ) {
            net.m21xx.s3explorer.ui.viewer.MediaViewerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Settings & Configurations
        composable(
            route = Destinations.ACCOUNT_SETTINGS,
            arguments = listOf(navArgument("profileId") { type = NavType.StringType })
        ) {
            net.m21xx.s3explorer.ui.settings.AccountSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Destinations.TRANSFERS,
            arguments = listOf(navArgument("profileId") { type = NavType.StringType })
        ) {
            PlaceholderScreen("Transfers", onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Destinations.TRASH,
            arguments = listOf(navArgument("profileId") { type = NavType.StringType })
        ) {
            PlaceholderScreen("Trash", onNavigateBack = { navController.popBackStack() })
        }

        composable(route = Destinations.GLOBAL_SETTINGS) {
            net.m21xx.s3explorer.ui.settings.GlobalSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Destinations.MEDIA_BACKUP) {
            PlaceholderScreen("Media Backup", onNavigateBack = { navController.popBackStack() })
        }
        
        composable(Destinations.ABOUT) {
            net.m21xx.s3explorer.ui.settings.AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceholderScreen(title: String, onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Coming soon...")
        }
    }
}
