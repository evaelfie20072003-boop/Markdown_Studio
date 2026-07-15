package com.markdownstudio.ui.explorer

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.markdownstudio.ui.explorer.components.CreateFileDialog
import com.markdownstudio.ui.explorer.components.DeleteConfirmDialog
import com.markdownstudio.ui.explorer.components.FileItemRow
import com.markdownstudio.ui.explorer.components.MoveDialog
import com.markdownstudio.ui.explorer.components.RenameDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorerScreen(
    onOpenEditor: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExplorerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val treePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            val prefs = context.getSharedPreferences(
                "markdown_studio_prefs",
                android.content.Context.MODE_PRIVATE
            )
            prefs.edit().putString("root_directory_uri", uri.toString()).apply()
            context.contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            viewModel.navigateToDirectory(uri.toString())
        }
    }

    LaunchedEffect(Unit) { viewModel.initialize() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = getCurrentDirectoryName(state.currentDirectoryUri),
                        maxLines = 1,
                        modifier = Modifier.semantics { contentDescription = "Current directory" }
                    )
                },
                navigationIcon = {
                    if (state.currentDirectoryUri != null && viewModel.canNavigateUp()) {
                        IconButton(onClick = { viewModel.navigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Navigate to parent directory"
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = state.currentDirectoryUri != null,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = { viewModel.showCreateDialog() },
                    modifier = Modifier.semantics { contentDescription = "Create new markdown file" }
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Create file")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.currentDirectoryUri == null -> NoRootView(
                    onSelectFolder = { treePickerLauncher.launch(null) },
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                )
                state.isLoading -> CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .semantics { contentDescription = "Loading files" }
                )
                state.files.isEmpty() -> EmptyView(
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                )
                else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.files, key = { it.uri }) { file ->
                        FileItemRow(
                            file = file,
                            onClick = {
                                if (file.isDirectory) viewModel.navigateToDirectory(file.uri)
                                else onOpenEditor(file.uri)
                            },
                            onRename = { viewModel.selectFile(file); viewModel.showRenameDialog() },
                            onDelete = { viewModel.selectFile(file); viewModel.showDeleteConfirm() },
                            onDuplicate = { viewModel.duplicateFile(file) },
                            onMove = { viewModel.selectFile(file); viewModel.showMoveDialog() },
                            onToggleFavorite = { viewModel.toggleFavorite(file) }
                        )
                    }
                }
            }
        }
    }

    if (state.showCreateDialog) {
        CreateFileDialog(
            onDismiss = viewModel::dismissDialog,
            onCreate = viewModel::createFile
        )
    }

    if (state.showRenameDialog && state.selectedFile != null) {
        RenameDialog(
            currentName = state.selectedFile!!.name,
            onDismiss = viewModel::dismissDialog,
            onRename = viewModel::renameFile
        )
    }

    if (state.showDeleteConfirm && state.selectedFile != null) {
        DeleteConfirmDialog(
            file = state.selectedFile!!,
            onDismiss = viewModel::dismissDialog,
            onConfirm = viewModel::deleteFile
        )
    }

    if (state.showMoveDialog && state.selectedFile != null) {
        MoveDialog(
            directories = emptyList(),
            onDismiss = viewModel::dismissDialog,
            onMove = viewModel::moveFile
        )
    }
}

@Composable
private fun NoRootView(
    onSelectFolder: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No folder selected",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Tap the button below to select a folder",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
            androidx.compose.material3.Button(
                onClick = onSelectFolder,
                modifier = Modifier
                    .padding(top = 24.dp)
                    .semantics { contentDescription = "Select markdown folder" }
            ) {
                Text("Select Folder")
            }
        }
    }
}

@Composable
private fun EmptyView(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No markdown files",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Create a new file using the + button",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

private fun getCurrentDirectoryName(uri: String?): String {
    if (uri == null) return "Markdown Studio"
    return Uri.parse(uri).lastPathSegment ?: "Root"
}
