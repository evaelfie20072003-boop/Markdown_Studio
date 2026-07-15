package com.markdownstudio.ui.explorer.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.markdownstudio.domain.model.MarkdownFile

@Composable
fun CreateFileDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var fileName by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Markdown File") },
        text = {
            OutlinedTextField(
                value = fileName,
                onValueChange = {
                    fileName = it
                    showError = false
                },
                label = { Text("File name") },
                singleLine = true,
                isError = showError,
                supportingText = if (showError) {
                    { Text("Name cannot be empty") }
                } else null
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (fileName.isBlank()) {
                        showError = true
                    } else {
                        onCreate(fileName)
                    }
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RenameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit
) {
    var newName by remember { mutableStateOf(currentName.removeSuffix(".md")) }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename File") },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = {
                    newName = it
                    showError = false
                },
                label = { Text("New name") },
                singleLine = true,
                isError = showError,
                supportingText = if (showError) {
                    { Text("Name cannot be empty") }
                } else null
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newName.isBlank()) {
                        showError = true
                    } else {
                        onRename(newName)
                    }
                }
            ) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteConfirmDialog(
    file: MarkdownFile,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete File") },
        text = {
            Text("Are you sure you want to delete \"${file.name}\"? This action cannot be undone.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun MoveDialog(
    directories: List<String>,
    onDismiss: () -> Unit,
    onMove: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Move File") },
        text = {
            Text("Select target directory (feature coming soon)")
        },
        confirmButton = {
            TextButton(onClick = { onMove("") }) {
                Text("Move")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
