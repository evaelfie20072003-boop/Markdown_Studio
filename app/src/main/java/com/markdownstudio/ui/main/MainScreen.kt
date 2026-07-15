package com.markdownstudio.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.markdownstudio.ui.explorer.ExplorerScreen
import com.markdownstudio.ui.favorites.FavoritesScreen
import com.markdownstudio.ui.recent.RecentScreen
import com.markdownstudio.ui.search.SearchScreen

private data class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val navItems = listOf(
    BottomNavItem("Explorer", Icons.Filled.Folder, Icons.Outlined.Folder),
    BottomNavItem("Recent", Icons.Filled.History, Icons.Outlined.History),
    BottomNavItem("Favorites", Icons.Filled.Star, Icons.Outlined.Star),
    BottomNavItem("Search", Icons.Filled.Search, Icons.Outlined.Search)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onOpenEditor: (String) -> Unit,
    onOpenSettings: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Markdown Studio") },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == index) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> ExplorerScreen(
                modifier = Modifier.padding(innerPadding),
                onOpenEditor = onOpenEditor
            )
            1 -> RecentScreen(
                modifier = Modifier.padding(innerPadding),
                onOpenEditor = onOpenEditor
            )
            2 -> FavoritesScreen(
                modifier = Modifier.padding(innerPadding),
                onOpenEditor = onOpenEditor
            )
            3 -> SearchScreen(
                modifier = Modifier.padding(innerPadding),
                onOpenEditor = onOpenEditor
            )
        }
    }
}
