package net.m21xx.s3explorer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import net.m21xx.s3explorer.ui.connection.NewConnectionScreen

object Destinations {
    const val NEW_CONNECTION = "new_connection"
    const val FILE_EXPLORER = "file_explorer"
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
                    // Navigate to file explorer upon successful connection
                    navController.navigate(Destinations.FILE_EXPLORER) {
                        popUpTo(Destinations.NEW_CONNECTION) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Destinations.FILE_EXPLORER) {
            // TODO: Implement File Explorer Screen
            androidx.compose.material3.Text(text = "File Explorer (Coming Soon)")
        }
    }
}
