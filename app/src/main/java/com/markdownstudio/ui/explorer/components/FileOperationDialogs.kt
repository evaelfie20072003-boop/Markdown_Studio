package com.markdownstudio.ui.explorer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CreateFileDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var fileName by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Note") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
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
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FormatButton(icon = Icons.Filled.FormatBold, desc = "Bold") {
                        content = insertAround(content, "**", "**")
                    }
                    FormatButton(icon = Icons.Filled.FormatItalic, desc = "Italic") {
                        content = insertAround(content, "*", "*")
                    }
                    FormatButton(icon = Icons.Filled.Link, desc = "Link") {
                        content = insertAround(content, "[", "](url)")
                    }
                    FormatButton(icon = Icons.Filled.FormatListBulleted, desc = "Bullet list") {
                        content = insertAtLineStart(content, "- ")
                    }
                    FormatButton(icon = Icons.Filled.FormatListNumbered, desc = "Numbered list") {
                        content = insertAtLineStart(content, "1. ")
                    }
                    FormatButton(icon = Icons.Filled.Code, desc = "Code") {
                        content = insertAround(content, "`", "`")
                    }
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content (markdown)") },
                    minLines = 6,
                    maxLines = 12,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (fileName.isBlank()) {
                        showError = true
                    } else {
                        onCreate(fileName, content)
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
private fun FormatButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    desc: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(),
        modifier = Modifier.width(36.dp).height(36.dp)
    ) {
        Icon(icon, contentDescription = desc, modifier = Modifier.padding(0.dp))
    }
}

private fun insertAround(text: String, before: String, after: String): String {
    return "$text$before$after"
}

private fun insertAtLineStart(text: String, prefix: String): String {
    return if (text.isBlank()) prefix else "$text\n$prefix"
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
