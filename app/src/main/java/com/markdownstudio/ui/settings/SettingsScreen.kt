package com.markdownstudio.ui.settings

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
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.markdownstudio.domain.model.AutoSaveInterval
import com.markdownstudio.domain.model.EditorFont
import com.markdownstudio.domain.model.ThemeMode
import com.markdownstudio.domain.model.ToolbarPosition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBackup: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
            ThemeSection(
                currentTheme = state.settings.themeMode,
                onThemeSelected = viewModel::setThemeMode
            )

            Spacer(Modifier.height(24.dp))

            FontSection(
                currentFont = EditorFont.entries.find { it.fontName == state.settings.fontFamily }
                    ?: EditorFont.MONOSPACE,
                fontSize = state.settings.fontSize,
                lineHeight = state.settings.lineHeight,
                previewFontSize = state.previewFontSize,
                previewLineHeight = state.previewLineHeight,
                onFontSelected = viewModel::setFont,
                onFontSizeChanged = viewModel::setFontSize,
                onLineHeightChanged = viewModel::setLineHeight
            )

            Spacer(Modifier.height(24.dp))

            ToolbarSection(
                currentPosition = state.settings.toolbarPosition,
                onPositionSelected = viewModel::setToolbarPosition
            )

            Spacer(Modifier.height(24.dp))

            AutoSaveSection(
                currentInterval = AutoSaveInterval.entries.find {
                    it.ms == state.settings.autoSaveIntervalMs
                } ?: AutoSaveInterval.SECONDS_10,
                onIntervalSelected = viewModel::setAutoSaveInterval
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = onNavigateToBackup,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(Icons.Filled.Backup, contentDescription = null)
                Spacer(Modifier.padding(8.dp))
                Text("Backup & Restore")
            }
        }
    }
}

@Composable
private fun ThemeSection(
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit
) {
    SectionHeader("Theme")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ThemeMode.entries.forEach { mode ->
            val isSelected = mode == currentTheme
            Card(
                onClick = { onThemeSelected(mode) },
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ),
                border = if (isSelected)
                    CardDefaults.outlinedCardBorder()
                else null
            ) {
                Text(
                    text = mode.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
private fun FontSection(
    currentFont: EditorFont,
    fontSize: Int,
    lineHeight: Float,
    previewFontSize: Int,
    previewLineHeight: Float,
    onFontSelected: (EditorFont) -> Unit,
    onFontSizeChanged: (Int) -> Unit,
    onLineHeightChanged: (Float) -> Unit
) {
    SectionHeader("Editor Font")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        EditorFont.entries.forEach { font ->
            val isSelected = font == currentFont
            Card(
                onClick = { onFontSelected(font) },
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = font.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }

    Spacer(Modifier.height(16.dp))

    SettingsLabel("Font Size: ${previewFontSize}sp")
    Slider(
        value = previewFontSize.toFloat(),
        onValueChange = { onFontSizeChanged(it.toInt()) },
        valueRange = 10f..28f,
        steps = 17,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(8.dp))

    SettingsLabel("Line Height: %.1f".format(previewLineHeight))
    Slider(
        value = previewLineHeight,
        onValueChange = onLineHeightChanged,
        valueRange = 1.0f..2.5f,
        steps = 14,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(8.dp))

    // Preview
    val previewFamily = when (currentFont) {
        EditorFont.MONOSPACE -> FontFamily.Monospace
        EditorFont.SANS_SERIF -> FontFamily.SansSerif
        EditorFont.SERIF -> FontFamily.Serif
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Text(
            text = "The quick brown fox jumps over the lazy dog.\nThis is a preview of the selected font settings.",
            style = TextStyle(
                fontFamily = previewFamily,
                fontSize = previewFontSize.sp,
                lineHeight = (previewFontSize * previewLineHeight).sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun ToolbarSection(
    currentPosition: ToolbarPosition,
    onPositionSelected: (ToolbarPosition) -> Unit
) {
    SectionHeader("Toolbar Position")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ToolbarPosition.entries.forEach { position ->
            val isSelected = position == currentPosition
            Card(
                onClick = { onPositionSelected(position) },
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = position.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
private fun AutoSaveSection(
    currentInterval: AutoSaveInterval,
    onIntervalSelected: (AutoSaveInterval) -> Unit
) {
    SectionHeader("Auto Save Interval")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        AutoSaveInterval.entries.forEach { interval ->
            val isSelected = interval == currentInterval
            Card(
                onClick = { onIntervalSelected(interval) },
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = interval.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun SettingsLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
