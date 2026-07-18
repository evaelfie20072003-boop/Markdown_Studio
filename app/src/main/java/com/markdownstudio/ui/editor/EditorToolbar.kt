package com.markdownstudio.ui.editor

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.WrapText
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EditorToolbar(
    canUndo: Boolean,
    canRedo: Boolean,
    wordWrap: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onToggleWordWrap: () -> Unit,
    onShowFindReplace: () -> Unit,
    onSave: () -> Unit = {},
    onInsertHeading: () -> Unit,
    onInsertBold: () -> Unit,
    onInsertItalic: () -> Unit,
    onInsertStrikethrough: () -> Unit,
    onInsertCode: () -> Unit,
    onInsertLink: () -> Unit,
    onInsertImage: () -> Unit,
    onInsertBulletList: () -> Unit,
    onInsertNumberedList: () -> Unit,
    onInsertBlockquote: () -> Unit,
    onInsertHorizontalRule: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        FilledTonalIconButton(
            onClick = onUndo,
            enabled = canUndo,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
        }

        FilledTonalIconButton(
            onClick = onRedo,
            enabled = canRedo,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo")
        }

        FilledTonalIconButton(
            onClick = onShowFindReplace,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Icon(Icons.Filled.Search, contentDescription = "Find")
        }

        FilledTonalIconButton(
            onClick = onToggleWordWrap,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = if (wordWrap)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Icon(Icons.Filled.WrapText, contentDescription = "Word wrap")
        }

        FilledTonalIconButton(
            onClick = onSave,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Icon(Icons.Filled.Save, contentDescription = "Save")
        }

        // Formatting
        FilledTonalIconButton(
            onClick = onInsertHeading,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Icon(Icons.Filled.Title, contentDescription = "Heading")
        }

        FilledTonalIconButton(
            onClick = onInsertBold,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Icon(Icons.Filled.FormatBold, contentDescription = "Bold")
        }

        FilledTonalIconButton(
            onClick = onInsertItalic,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Icon(Icons.Filled.FormatItalic, contentDescription = "Italic")
        }

        FilledTonalIconButton(
            onClick = onInsertStrikethrough,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Icon(Icons.Filled.FormatStrikethrough, contentDescription = "Strikethrough")
        }

        FilledTonalIconButton(
            onClick = onInsertCode,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Icon(Icons.Filled.Code, contentDescription = "Code")
        }

        FilledTonalIconButton(
            onClick = onInsertLink,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Icon(Icons.Filled.Link, contentDescription = "Link")
        }

        FilledTonalIconButton(
            onClick = onInsertImage,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Icon(Icons.Filled.Image, contentDescription = "Image")
        }

        FilledTonalIconButton(
            onClick = onInsertBulletList,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Icon(Icons.Filled.FormatListBulleted, contentDescription = "Bullet list")
        }

        FilledTonalIconButton(
            onClick = onInsertNumberedList,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Icon(Icons.Filled.FormatListNumbered, contentDescription = "Numbered list")
        }

        FilledTonalIconButton(
            onClick = onInsertBlockquote,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Icon(Icons.Filled.FormatQuote, contentDescription = "Blockquote")
        }

        FilledTonalIconButton(
            onClick = onInsertHorizontalRule,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Icon(Icons.Filled.HorizontalRule, contentDescription = "Horizontal rule")
        }
    }
}
