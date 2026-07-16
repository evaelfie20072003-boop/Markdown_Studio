package com.markdownstudio.ui.editor

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.markdownstudio.data.render.MarkdownRenderEngineImpl
import com.markdownstudio.di.RenderEntryPoint
import com.markdownstudio.ui.backlinks.BacklinksPanel
import com.markdownstudio.ui.render.PreviewPanel
import dagger.hilt.android.EntryPointAccessors
import android.content.Intent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    fileUri: String,
    onNavigateBack: () -> Unit,
    viewModel: EditorViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val renderEngine = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            RenderEntryPoint::class.java
        ).markdownRenderEngine()
    }

    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val isDark = MaterialTheme.colorScheme.background.let { color ->
        (0.299f * color.red + 0.587f * color.green + 0.114f * color.blue) < 0.5f
    }

    val onLinkClicked: (String) -> Unit = { url ->
        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    val fontFamily = when (state.fontFamily) {
        "sans-serif" -> FontFamily.SansSerif
        "serif" -> FontFamily.Serif
        else -> FontFamily.Monospace
    }

    LaunchedEffect(fileUri) {
        viewModel.loadFile(fileUri)
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.fileName,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                        statusText(state)
                        if (state.tags.isNotEmpty()) {
                            Text(
                                text = state.tags.joinToString(" ") { "#${it.name}" },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back to file list"
                        )
                    }
                },
                actions = {
                    viewModeActions(state.viewMode, viewModel::setViewMode)
                    overflowMenu(state, viewModel)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            val isToolbarTop = state.toolbarPosition == "TOP"

            AnimatedVisibility(
                visible = isToolbarTop && state.viewMode != ViewMode.PREVIEW,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut()
            ) {
                EditorToolbar(
                    canUndo = state.canUndo, canRedo = state.canRedo,
                    wordWrap = state.wordWrap,
                    onUndo = viewModel::undo, onRedo = viewModel::redo,
                    onToggleWordWrap = viewModel::toggleWordWrap,
                    onShowFindReplace = viewModel::toggleFindReplace,
                    onInsertHeading = { viewModel.insertAtLineStart("## ") },
                    onInsertBold = { viewModel.insertAround("**", "**") },
                    onInsertItalic = { viewModel.insertAround("*", "*") },
                    onInsertStrikethrough = { viewModel.insertAround("~~", "~~") },
                    onInsertCode = { viewModel.insertAround("`", "`") },
                    onInsertLink = { viewModel.insertAround("[", "](url)") },
                    onInsertImage = { viewModel.insertAround("![", "](url)") },
                    onInsertBulletList = { viewModel.insertAtLineStart("- ") },
                    onInsertNumberedList = { viewModel.insertAtLineStart("1. ") },
                    onInsertBlockquote = { viewModel.insertAtLineStart("> ") },
                    onInsertHorizontalRule = { viewModel.insertAtNewLine("---") }
                )
            }

            AnimatedVisibility(
                visible = state.showFindReplace,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut()
            ) {
                FindReplaceDialog(
                    onDismiss = viewModel::toggleFindReplace,
                    onFind = viewModel::updateFindQuery,
                    onReplace = { find, replace ->
                        viewModel.updateReplaceQuery(replace)
                        viewModel.replaceCurrent()
                    },
                    onReplaceAll = { find, replace ->
                        viewModel.updateReplaceQuery(replace)
                        viewModel.replaceAll()
                    },
                    onFindNext = viewModel::findNext,
                    onFindPrevious = viewModel::findPrevious,
                    matchCount = state.matchCount,
                    currentMatch = state.currentMatch
                )
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.semantics { contentDescription = "Loading file content" }
                    )
                }
            } else {
                AnimatedContent(
                    targetState = state.viewMode,
                    transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) }
                ) { mode ->
                    when (mode) {
                        ViewMode.EDITOR -> editorView(state, isDark, fontFamily, viewModel)
                        ViewMode.PREVIEW -> previewView(state, renderEngine, viewModel, onLinkClicked)
                        ViewMode.SPLIT -> splitView(state, isDark, fontFamily, renderEngine, viewModel, onLinkClicked)
                    }
                }

                AnimatedVisibility(
                    visible = state.showBacklinks && state.viewMode != ViewMode.PREVIEW,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    BacklinksPanel(
                        outgoingLinks = state.outgoingLinks,
                        backlinks = state.backlinks,
                        onLinkClicked = { target -> viewModel.navigateToWikiLink(target) }
                    )
                }
            }

            AnimatedVisibility(
                visible = !isToolbarTop && state.viewMode != ViewMode.PREVIEW,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                EditorToolbar(
                    canUndo = state.canUndo, canRedo = state.canRedo,
                    wordWrap = state.wordWrap,
                    onUndo = viewModel::undo, onRedo = viewModel::redo,
                    onToggleWordWrap = viewModel::toggleWordWrap,
                    onShowFindReplace = viewModel::toggleFindReplace,
                    onInsertHeading = { viewModel.insertAtLineStart("## ") },
                    onInsertBold = { viewModel.insertAround("**", "**") },
                    onInsertItalic = { viewModel.insertAround("*", "*") },
                    onInsertStrikethrough = { viewModel.insertAround("~~", "~~") },
                    onInsertCode = { viewModel.insertAround("`", "`") },
                    onInsertLink = { viewModel.insertAround("[", "](url)") },
                    onInsertImage = { viewModel.insertAround("![", "](url)") },
                    onInsertBulletList = { viewModel.insertAtLineStart("- ") },
                    onInsertNumberedList = { viewModel.insertAtLineStart("1. ") },
                    onInsertBlockquote = { viewModel.insertAtLineStart("> ") },
                    onInsertHorizontalRule = { viewModel.insertAtNewLine("---") }
                )
            }
        }
    }
}

@Composable
private fun statusText(state: EditorUiState) {
    when {
        state.isSaving -> Text(
            text = "Saving...",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        state.isModified -> Text(
            text = "Unsaved changes",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun viewModeActions(currentMode: ViewMode, onSetMode: (ViewMode) -> Unit) {
    ViewMode.entries.forEach { mode ->
        val icon = when (mode) {
            ViewMode.EDITOR -> Icons.Filled.Code
            ViewMode.SPLIT -> Icons.Filled.ViewColumn
            ViewMode.PREVIEW -> Icons.Filled.Visibility
        }
        val isSelected = mode == currentMode
        val tint by animateColorAsState(
            targetValue = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            label = "modeTint"
        )
        IconButton(onClick = { onSetMode(mode) }) {
            Icon(
                icon,
                contentDescription = "${mode.name.lowercase()} view mode",
                tint = tint
            )
        }
    }
}

@Composable
private fun overflowMenu(state: EditorUiState, viewModel: EditorViewModel) {
    var showMenu by remember { mutableStateOf(false) }
    IconButton(
        onClick = { showMenu = true },
        modifier = Modifier.semantics { contentDescription = "More options" }
    ) {
        Icon(Icons.Filled.MoreVert, contentDescription = "More options")
    }
    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
        DropdownMenuItem(
            text = { Text("Find & Replace") },
            onClick = { showMenu = false; viewModel.toggleFindReplace() }
        )
        DropdownMenuItem(
            text = { Text(if (state.wordWrap) "Disable Word Wrap" else "Enable Word Wrap") },
            onClick = { showMenu = false; viewModel.toggleWordWrap() }
        )
        DropdownMenuItem(
            text = { Text(if (state.showBacklinks) "Hide Backlinks" else "Show Backlinks") },
            onClick = { showMenu = false; viewModel.toggleBacklinks() }
        )
    }
}

@Composable
private fun editorView(
    state: EditorUiState,
    isDark: Boolean,
    fontFamily: FontFamily,
    viewModel: EditorViewModel
) {
    EditorPanel(
        content = state.content, cursorPosition = state.cursorPosition,
        selectionStart = state.selectionStart, selectionEnd = state.selectionEnd,
        currentLine = state.currentLine, wordWrap = state.wordWrap,
        isDark = isDark, fontFamily = fontFamily,
        fontSize = state.fontSize, lineHeight = state.lineHeight,
        onContentChanged = viewModel::onContentChanged,
        onCursorChanged = viewModel::onCursorChanged
    )
}

@Composable
private fun previewView(
    state: EditorUiState,
    engine: MarkdownRenderEngineImpl,
    viewModel: EditorViewModel,
    onLinkClicked: (String) -> Unit
) {
    PreviewPanel(
        markdown = state.content, renderEpoch = state.renderEpoch, engine = engine,
        scrollRatio = state.previewScrollRatio,
        isExternalScroll = state.isScrollingProgrammatically,
        onScrollChanged = viewModel::onPreviewScroll,
        onScrollSyncComplete = viewModel::onScrollSyncComplete,
        onLinkClicked = onLinkClicked
    )
}

@Composable
private fun splitView(
    state: EditorUiState,
    isDark: Boolean,
    fontFamily: FontFamily,
    engine: MarkdownRenderEngineImpl,
    viewModel: EditorViewModel,
    onLinkClicked: (String) -> Unit
) {
    var dividerRatio by remember { mutableFloatStateOf(0.5f) }
    val density = LocalDensity.current

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth().weight(dividerRatio)) {
            EditorPanel(
                content = state.content, cursorPosition = state.cursorPosition,
                selectionStart = state.selectionStart, selectionEnd = state.selectionEnd,
                currentLine = state.currentLine, wordWrap = state.wordWrap,
                isDark = isDark, fontFamily = fontFamily,
                fontSize = state.fontSize, lineHeight = state.lineHeight,
                onContentChanged = viewModel::onContentChanged,
                onCursorChanged = viewModel::onCursorChanged
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
                .pointerInput(Unit) {
                    detectVerticalDragGestures { change, dragAmount ->
                        change.consume()
                        val parentHeight = with(density) { change.position.y + dragAmount }
                        val totalHeight = size.height.toFloat()
                        if (totalHeight > 0) {
                            dividerRatio = (dividerRatio + dragAmount / totalHeight)
                                .coerceIn(0.15f, 0.85f)
                        }
                    }
                }
                .semantics { contentDescription = "Split view divider" }
        )

        Box(modifier = Modifier.fillMaxWidth().weight(1f - dividerRatio)) {
            PreviewPanel(
                markdown = state.content, renderEpoch = state.renderEpoch, engine = engine,
                scrollRatio = state.previewScrollRatio,
                isExternalScroll = state.isScrollingProgrammatically,
                onScrollChanged = viewModel::onPreviewScroll,
                onScrollSyncComplete = viewModel::onScrollSyncComplete,
                onLinkClicked = onLinkClicked
            )
        }
    }
}
