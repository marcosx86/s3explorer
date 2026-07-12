package net.m21xx.s3explorer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import net.m21xx.s3explorer.ui.connection.NewConnectionScreen
import net.m21xx.s3explorer.ui.explorer.FileExplorerScreen

object Destinations {
    const val NEW_CONNECTION = "new_connection"
    const val FILE_EXPLORER = "file_explorer/{profileId}/{bucketName}"
    fun fileExplorerRoute(profileId: String, bucketName: String) = "file_explorer/$profileId/$bucketName"
}

@Composable
fun S3NavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Destinations.NEW_CONNECTION
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Destinations.NEW_CONNECTION) {
            NewConnectionScreen(
                onConnectionSuccess = { profileId, bucketNameRaw ->
                    val bucketName = bucketNameRaw.ifBlank { "unknown" }
                    navController.navigate(Destinations.fileExplorerRoute(profileId, bucketName)) {
                        popUpTo(Destinations.NEW_CONNECTION) { inclusive = true }
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
                onOpenDrawer = { /* TODO: Open Navigation Drawer */ }
            )
        }
    }
}
