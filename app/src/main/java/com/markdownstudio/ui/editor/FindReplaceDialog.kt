package com.markdownstudio.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FindReplaceDialog(
    onDismiss: () -> Unit,
    onFind: (String) -> Unit,
    onReplace: (String, String) -> Unit,
    onReplaceAll: (String, String) -> Unit,
    onFindNext: () -> Unit,
    onFindPrevious: () -> Unit,
    matchCount: Int,
    currentMatch: Int,
    modifier: Modifier = Modifier
) {
    var findText by remember { mutableStateOf("") }
    var replaceText by remember { mutableStateOf("") }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = findText,
                    onValueChange = {
                        findText = it
                        onFind(it)
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Find") },
                    singleLine = true
                )
                Text(
                    text = if (matchCount > 0) "$currentMatch/$matchCount" else "",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(onClick = onFindPrevious) {
                    Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Previous")
                }
                IconButton(onClick = onFindNext) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Next")
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = replaceText,
                    onValueChange = { replaceText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Replace") },
                    singleLine = true
                )
                FilledTonalButton(
                    onClick = { onReplace(findText, replaceText) },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("Replace")
                }
                FilledTonalButton(
                    onClick = { onReplaceAll(findText, replaceText) },
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text("All")
                }
            }
        }
    }
}
