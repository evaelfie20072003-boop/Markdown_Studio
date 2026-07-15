package com.markdownstudio.navigation

sealed class Screen(val route: String) {
    data object Main : Screen("main")
    data object Editor : Screen("editor/{fileUri}") {
        fun createRoute(fileUri: String): String = "editor/$fileUri"
    }
    data object Settings : Screen("settings")
    data object Backup : Screen("backup")
}
