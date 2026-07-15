package com.markdownstudio.ui.backup

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onNavigateBack: () -> Unit,
    viewModel: BackupViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let { viewModel.exportAll(it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let { viewModel.importFrom(it) }
    }

    val zipBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: Uri? ->
        uri?.let { viewModel.createBackupZip(it) }
    }

    val zipRestoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.restoreFromZip(it) }
    }

    LaunchedEffect(state.report) {
        state.report?.let { report ->
            snackbarHostState.showSnackbar(
                "Done: ${report.successfulFiles}/${report.totalFiles} files"
            )
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (state.operation != BackupOperation.IDLE) {
                ProgressSection(state)
            }

            BackupActionCard(
                icon = Icons.Filled.FileDownload,
                title = "Export All Markdown",
                description = "Copy all .md files to a selected folder",
                enabled = state.operation == BackupOperation.IDLE,
                onClick = { exportLauncher.launch(null) }
            )

            Spacer(Modifier.height(12.dp))

            BackupActionCard(
                icon = Icons.Filled.FileUpload,
                title = "Import Markdown",
                description = "Copy .md files from a folder into the app",
                enabled = state.operation == BackupOperation.IDLE,
                onClick = { importLauncher.launch(null) }
            )

            Spacer(Modifier.height(12.dp))

            BackupActionCard(
                icon = Icons.Filled.FolderZip,
                title = "ZIP Backup",
                description = "Create a .zip archive of all markdown files",
                enabled = state.operation == BackupOperation.IDLE,
                onClick = { zipBackupLauncher.launch("backup.zip") }
            )

            Spacer(Modifier.height(12.dp))

            BackupActionCard(
                icon = Icons.Filled.Unarchive,
                title = "Restore from ZIP",
                description = "Restore markdown files from a .zip archive",
                enabled = state.operation == BackupOperation.IDLE,
                onClick = { zipRestoreLauncher.launch(arrayOf("application/zip")) }
            )

            Spacer(Modifier.height(24.dp))

            state.report?.let { report ->
                ReportCard(report)
            }
        }
    }
}

@Composable
private fun ProgressSection(state: BackupUiState) {
    val label = when (state.operation) {
        BackupOperation.EXPORTING -> "Exporting..."
        BackupOperation.IMPORTING -> "Importing..."
        BackupOperation.ZIPPING -> "Creating ZIP..."
        BackupOperation.RESTORING -> "Restoring..."
        else -> ""
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { if (state.total > 0) state.completed.toFloat() / state.total else 0f },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${state.completed} / ${state.total} files",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun BackupActionCard(
    icon: ImageVector,
    title: String,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Icon(icon, contentDescription = null)
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ReportCard(report: com.markdownstudio.domain.model.BackupReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (report.failedFiles == 0)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Results",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text("Total files: ${report.totalFiles}")
            Text("Successful: ${report.successfulFiles}")
            if (report.failedFiles > 0) {
                Text(
                    text = "Failed: ${report.failedFiles}",
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (report.errors.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                report.errors.take(5).forEach { err ->
                    Text(
                        text = err,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (report.errors.size > 5) {
                    Text(
                        text = "...and ${report.errors.size - 5} more",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
