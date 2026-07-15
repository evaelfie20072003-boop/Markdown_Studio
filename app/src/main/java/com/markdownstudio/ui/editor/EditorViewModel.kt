package com.markdownstudio.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.markdownstudio.domain.model.MarkdownFile
import com.markdownstudio.domain.model.obsidian.Backlink
import com.markdownstudio.domain.model.obsidian.Tag
import com.markdownstudio.domain.model.obsidian.WikiLink
import com.markdownstudio.domain.repository.ObsidianRepository
import com.markdownstudio.domain.repository.SettingsRepository
import com.markdownstudio.domain.usecase.FileOperationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditorUiState(
    val fileUri: String = "",
    val fileName: String = "Untitled",
    val content: String = "",
    val cursorPosition: Int = 0,
    val selectionStart: Int? = null,
    val selectionEnd: Int? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isModified: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val wordWrap: Boolean = true,
    val viewMode: ViewMode = ViewMode.EDITOR,
    val showFindReplace: Boolean = false,
    val showBacklinks: Boolean = false,
    val findQuery: String = "",
    val replaceQuery: String = "",
    val matchCount: Int = 0,
    val currentMatch: Int = 0,
    val currentLine: Int = 1,
    val totalLines: Int = 0,
    val editorScrollRatio: Float = 0f,
    val previewScrollRatio: Float = 0f,
    val isScrollingProgrammatically: Boolean = false,
    val renderEpoch: Int = 0,
    // Obsidian features
    val outgoingLinks: List<WikiLink> = emptyList(),
    val backlinks: List<Backlink> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val error: String? = null,
    // Settings
    val fontFamily: String = "monospace",
    val fontSize: Int = 14,
    val lineHeight: Float = 1.5f,
    val toolbarPosition: String = "TOP",
    val autoSaveIntervalMs: Long = 1500L
)

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val fileOperationsUseCase: FileOperationsUseCase,
    private val undoRedoManager: UndoRedoManager,
    private val obsidianRepository: ObsidianRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val saveSignal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val renderSignal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val intervalSignal = MutableSharedFlow<Long>(extraBufferCapacity = 1)

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _uiState.value = _uiState.value.copy(
                    fontFamily = settings.fontFamily,
                    fontSize = settings.fontSize,
                    lineHeight = settings.lineHeight,
                    toolbarPosition = settings.toolbarPosition.name,
                    autoSaveIntervalMs = settings.autoSaveIntervalMs
                )
                intervalSignal.emit(settings.autoSaveIntervalMs)
            }
        }
        viewModelScope.launch {
            intervalSignal.collect { interval ->
                if (interval > 0) {
                    saveSignal.emit(Unit) // flush pending save
                }
            }
        }
        viewModelScope.launch {
            intervalSignal.flatMapLatest { interval ->
                if (interval > 0) saveSignal.debounce(interval) else flowOf()
            }.collect { performSave() }
        }
        viewModelScope.launch {
            renderSignal.debounce(300).collect {
                _uiState.value = _uiState.value.copy(renderEpoch = _uiState.value.renderEpoch + 1)
            }
        }
    }

    fun loadFile(uri: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                fileUri = uri, isLoading = true,
                fileName = uri.substringAfterLast("/").ifEmpty { "Untitled" }
            )

            fileOperationsUseCase.read(
                MarkdownFile(uri, uri.substringAfterLast("/").ifEmpty { "Untitled" }, 0L, 0L, false)
            ).onSuccess { content ->
                val lines = content.count { it == '\n' } + 1
                undoRedoManager.reset(content)
                _uiState.value = _uiState.value.copy(
                    content = content, totalLines = lines,
                    isLoading = false, isModified = false, renderEpoch = 1,
                    canUndo = false, canRedo = false
                )
                parseObsidianFeatures(uri, content)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false, error = "Failed to load file: ${e.message}"
                )
            }
        }
    }

    fun onContentChanged(newContent: String) {
        val oldContent = _uiState.value.content
        if (newContent == oldContent) return

        undoRedoManager.pushState(oldContent)
        val lines = newContent.count { it == '\n' } + 1
        _uiState.value = _uiState.value.copy(
            content = newContent, totalLines = lines,
            isModified = true, canUndo = undoRedoManager.canUndo,
            canRedo = undoRedoManager.canRedo
        )
        triggerSave()
        triggerRender()
        parseObsidianFeatures(_uiState.value.fileUri, newContent)
    }

    private fun parseObsidianFeatures(uri: String, content: String) {
        viewModelScope.launch {
            obsidianRepository.parseAndStoreLinks(uri, content)
            obsidianRepository.parseAndStoreTags(uri, content)

            val outgoing = obsidianRepository.getOutgoingLinks(uri)
            val backlinks = obsidianRepository.getBacklinks(uri)
            val tags = obsidianRepository.getTags(uri)

            _uiState.value = _uiState.value.copy(
                outgoingLinks = outgoing,
                backlinks = backlinks,
                tags = tags
            )
        }
    }

    fun navigateToWikiLink(target: String) {
        val resolvedUri = obsidianRepository.resolveWikiLink(target)
        if (resolvedUri != null) {
            loadFile(resolvedUri)
        }
    }

    fun toggleBacklinks() {
        _uiState.value = _uiState.value.copy(
            showBacklinks = !_uiState.value.showBacklinks
        )
    }

    fun onCursorChanged(position: Int, selectionStart: Int?, selectionEnd: Int?) {
        val content = _uiState.value.content
        val clampedPos = position.coerceIn(0, content.length)
        val line = content.substring(0, clampedPos).count { it == '\n' } + 1
        val total = _uiState.value.totalLines.coerceAtLeast(1)
        _uiState.value = _uiState.value.copy(
            cursorPosition = clampedPos, selectionStart = selectionStart,
            selectionEnd = selectionEnd, currentLine = line,
            editorScrollRatio = (line - 1).toFloat() / (total - 1).coerceAtLeast(1)
        )
    }

    fun onEditorScroll(ratio: Float) {
        if (_uiState.value.editorScrollRatio == ratio) return
        _uiState.value = _uiState.value.copy(editorScrollRatio = ratio, isScrollingProgrammatically = true)
        syncPreviewScroll()
    }

    fun onPreviewScroll(ratio: Float) {
        if (_uiState.value.previewScrollRatio == ratio) return
        _uiState.value = _uiState.value.copy(previewScrollRatio = ratio, isScrollingProgrammatically = true)
    }

    private fun syncPreviewScroll() {
        _uiState.value = _uiState.value.copy(previewScrollRatio = _uiState.value.editorScrollRatio, isScrollingProgrammatically = true)
    }

    fun onScrollSyncComplete() {
        _uiState.value = _uiState.value.copy(isScrollingProgrammatically = false)
    }

    fun setViewMode(mode: ViewMode) {
        _uiState.value = _uiState.value.copy(viewMode = mode)
    }

    fun undo() {
        undoRedoManager.undo(_uiState.value.content)?.let { prev ->
            val lines = prev.count { it == '\n' } + 1
            _uiState.value = _uiState.value.copy(content = prev, totalLines = lines, isModified = true,
                canUndo = undoRedoManager.canUndo, canRedo = undoRedoManager.canRedo)
            triggerSave(); triggerRender()
            parseObsidianFeatures(_uiState.value.fileUri, prev)
        }
    }

    fun redo() {
        undoRedoManager.redo(_uiState.value.content)?.let { next ->
            val lines = next.count { it == '\n' } + 1
            _uiState.value = _uiState.value.copy(content = next, totalLines = lines, isModified = true,
                canUndo = undoRedoManager.canUndo, canRedo = undoRedoManager.canRedo)
            triggerSave(); triggerRender()
            parseObsidianFeatures(_uiState.value.fileUri, next)
        }
    }

    fun toggleWordWrap() {
        _uiState.value = _uiState.value.copy(wordWrap = !_uiState.value.wordWrap)
    }

    fun toggleFindReplace() {
        _uiState.value = _uiState.value.copy(
            showFindReplace = !_uiState.value.showFindReplace,
            findQuery = "", replaceQuery = "", matchCount = 0, currentMatch = 0
        )
    }

    fun updateFindQuery(query: String) {
        val count = if (query.isNotBlank()) {
            _uiState.value.content.windowed(query.length, 1).count { it == query }
        } else 0
        _uiState.value = _uiState.value.copy(findQuery = query, matchCount = count, currentMatch = if (count > 0) 1 else 0)
    }

    fun updateReplaceQuery(query: String) {
        _uiState.value = _uiState.value.copy(replaceQuery = query)
    }

    fun findNext() {
        val q = _uiState.value.findQuery; if (q.isBlank()) return
        val i = _uiState.value.content.indexOf(q, (_uiState.value.cursorPosition + 1).coerceAtMost(_uiState.value.content.length))
        if (i >= 0) onCursorChanged(i, i, i + q.length)
    }

    fun findPrevious() {
        val q = _uiState.value.findQuery; if (q.isBlank()) return
        val i = _uiState.value.content.lastIndexOf(q, (_uiState.value.cursorPosition - 1).coerceAtLeast(0))
        if (i >= 0) onCursorChanged(i, i, i + q.length)
    }

    fun replaceCurrent() {
        val q = _uiState.value.findQuery; val r = _uiState.value.replaceQuery
        if (q.isBlank()) return
        val ss = _uiState.value.selectionStart ?: return
        val se = _uiState.value.selectionEnd ?: return
        if (se - ss != q.length || _uiState.value.content.substring(ss, se) != q) return
        val nc = _uiState.value.content.substring(0, ss) + r + _uiState.value.content.substring(se)
        onContentChanged(nc); onCursorChanged(ss + r.length, null, null)
    }

    fun replaceAll() {
        val q = _uiState.value.findQuery; val r = _uiState.value.replaceQuery
        if (q.isBlank()) return
        val nc = _uiState.value.content.replace(q, r)
        if (nc != _uiState.value.content) onContentChanged(nc)
    }

    fun insertText(text: String) {
        val c = _uiState.value.content; val p = _uiState.value.cursorPosition
        onContentChanged(c.substring(0, p) + text + c.substring(p))
        onCursorChanged(p + text.length, null, null)
    }

    fun insertAround(before: String, after: String) {
        val c = _uiState.value.content
        val ss = _uiState.value.selectionStart ?: _uiState.value.cursorPosition
        val se = _uiState.value.selectionEnd ?: _uiState.value.cursorPosition
        val s = minOf(ss, se); val e = maxOf(ss, se)
        val nc = c.substring(0, s) + before + c.substring(s, e) + after + c.substring(e)
        onContentChanged(nc); onCursorChanged(s + before.length + (e - s) + after.length, null, null)
    }

    fun insertAtLineStart(prefix: String) {
        val c = _uiState.value.content; val p = _uiState.value.cursorPosition
        val ls = c.substring(0, p.coerceAtMost(c.length)).lastIndexOf('\n') + 1
        onContentChanged(c.substring(0, ls) + prefix + c.substring(ls))
        onCursorChanged(p + prefix.length, null, null)
    }

    fun insertAtNewLine(prefix: String) {
        val c = _uiState.value.content; val p = _uiState.value.cursorPosition
        onContentChanged(c.substring(0, p) + "\n" + prefix + c.substring(p))
        onCursorChanged(p + 1 + prefix.length, null, null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun triggerSave() { saveSignal.tryEmit(Unit) }
    private fun triggerRender() { renderSignal.tryEmit(Unit) }

    private suspend fun performSave() {
        val s = _uiState.value; if (!s.isModified || s.content.isEmpty()) return
        _uiState.value = _uiState.value.copy(isSaving = true)
        fileOperationsUseCase.write(
            MarkdownFile(s.fileUri, s.fileName, 0L, 0L, false),
            s.content
        ).onSuccess { _uiState.value = _uiState.value.copy(isSaving = false, isModified = false) }
            .onFailure { _uiState.value = _uiState.value.copy(isSaving = false, error = "Auto-save failed: ${it.message}") }
    }
}
