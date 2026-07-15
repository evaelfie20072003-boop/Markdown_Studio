package com.markdownstudio.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.markdownstudio.ui.editor.EditorScreen
import com.markdownstudio.ui.main.MainScreen
import com.markdownstudio.ui.backup.BackupScreen
import com.markdownstudio.ui.settings.SettingsScreen

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(route = Screen.Main.route) {
            MainScreen(
                onOpenEditor = { fileUri ->
                    navController.navigate(Screen.Editor.createRoute(fileUri))
                },
                onOpenSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.Editor.route,
            arguments = listOf(
                navArgument("fileUri") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val fileUri = backStackEntry.arguments?.getString("fileUri") ?: return@composable
            EditorScreen(
                fileUri = fileUri,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBackup = { navController.navigate(Screen.Backup.route) }
            )
        }

        composable(route = Screen.Backup.route) {
            BackupScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
