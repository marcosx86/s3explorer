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
    const val FILE_EXPLORER = "file_explorer/{bucketName}"
    fun fileExplorerRoute(bucketName: String) = "file_explorer/$bucketName"
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
                onConnectionSuccess = {
                    val bucketName = it.ifBlank { "unknown" }
                    navController.navigate(Destinations.fileExplorerRoute(bucketName)) {
                        popUpTo(Destinations.NEW_CONNECTION) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = Destinations.FILE_EXPLORER,
            arguments = listOf(navArgument("bucketName") { type = NavType.StringType })
        ) {
            FileExplorerScreen(
                onOpenDrawer = { /* TODO: Open Navigation Drawer */ }
            )
        }
    }
}
