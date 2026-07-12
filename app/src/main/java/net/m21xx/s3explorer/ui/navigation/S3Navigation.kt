package net.m21xx.s3explorer.ui.navigation

import androidx.compose.runtime.Composable
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
                onOpenDrawer = { /* TODO: Open Navigation Drawer */ },
                onNavigateToConnections = {
                    navController.popBackStack(Destinations.CONNECTIONS_LIST, false)
                }
            )
        }
    }
}
