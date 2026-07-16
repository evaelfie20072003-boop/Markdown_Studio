package com.markdownstudio.navigation

import java.net.URLEncoder

sealed class Screen(val route: String) {
    data object Main : Screen("main")
    data object Editor : Screen("editor/{fileUri}") {
        fun createRoute(fileUri: String): String =
            "editor/${URLEncoder.encode(fileUri, "UTF-8")}"
    }
    data object Settings : Screen("settings")
    data object Backup : Screen("backup")
}
