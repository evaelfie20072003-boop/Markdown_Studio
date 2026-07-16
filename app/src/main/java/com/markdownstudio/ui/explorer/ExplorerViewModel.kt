package com.markdownstudio.ui.explorer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.markdownstudio.domain.model.MarkdownFile
import com.markdownstudio.domain.usecase.FavoriteFilesUseCase
import com.markdownstudio.domain.usecase.FileOperationsUseCase
import com.markdownstudio.domain.usecase.GetFilesUseCase
import com.markdownstudio.domain.model.obsidian.Template
import com.markdownstudio.domain.repository.ObsidianRepository
import com.markdownstudio.domain.usecase.GetRootDirectoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExplorerUiState(
    val currentDirectoryUri: String? = null,
    val directoryStack: List<String> = emptyList(),
    val files: List<MarkdownFile> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedFile: MarkdownFile? = null,
    val showCreateDialog: Boolean = false,
    val showRenameDialog: Boolean = false,
    val showDailyNote: Boolean = false,
    val availableTemplates: List<Template> = emptyList(),
    val showTemplateDialog: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val showMoveDialog: Boolean = false
)

@HiltViewModel
class ExplorerViewModel @Inject constructor(
    private val getFilesUseCase: GetFilesUseCase,
    private val fileOperationsUseCase: FileOperationsUseCase,
    private val favoriteFilesUseCase: FavoriteFilesUseCase,
    private val getRootDirectoryUseCase: GetRootDirectoryUseCase,
    private val obsidianRepository: ObsidianRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExplorerUiState())
    val uiState: StateFlow<ExplorerUiState> = _uiState.asStateFlow()

    fun initialize() {
        val rootUri = getRootDirectoryUseCase.execute()
        if (rootUri != null) {
            navigateToDirectory(rootUri)
        }
    }

    fun navigateToDirectory(uri: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = getFilesUseCase.execute(uri)
            result.fold(
                onSuccess = { files ->
                    val sorted = files.sortedWith(
                        compareByDescending<MarkdownFile> { it.isDirectory }
                            .thenBy { it.name.lowercase() }
                    )
                    _uiState.value = _uiState.value.copy(
                        currentDirectoryUri = uri,
                        directoryStack = _uiState.value.directoryStack + uri,
                        files = sorted,
                        isLoading = false
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load files"
                    )
                }
            )
        }
    }

    fun createFile(name: String, content: String = "") {
        val dirUri = _uiState.value.currentDirectoryUri ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, showCreateDialog = false)
            fileOperationsUseCase.create(dirUri, name)
                .onSuccess { file ->
                    if (content.isNotBlank()) {
                        fileOperationsUseCase.write(file, content)
                    }
                    navigateToDirectory(dirUri)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to create file"
                    )
                }
        }
    }

    fun renameFile(newName: String) {
        val file = _uiState.value.selectedFile ?: return
        val dirUri = _uiState.value.currentDirectoryUri ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(showRenameDialog = false)
            fileOperationsUseCase.rename(file, newName)
                .onSuccess { navigateToDirectory(dirUri) }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Failed to rename file"
                    )
                }
        }
    }

    fun deleteFile() {
        val file = _uiState.value.selectedFile ?: return
        val dirUri = _uiState.value.currentDirectoryUri ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(showDeleteConfirm = false)
            fileOperationsUseCase.delete(file)
                .onSuccess { navigateToDirectory(dirUri) }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Failed to delete file"
                    )
                }
        }
    }

    fun duplicateFile(file: MarkdownFile) {
        val dirUri = _uiState.value.currentDirectoryUri ?: return
        viewModelScope.launch {
            fileOperationsUseCase.duplicate(file)
                .onSuccess { navigateToDirectory(dirUri) }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Failed to duplicate file"
                    )
                }
        }
    }

    fun moveFile(targetDirectoryUri: String) {
        val file = _uiState.value.selectedFile ?: return
        val dirUri = _uiState.value.currentDirectoryUri ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(showMoveDialog = false)
            fileOperationsUseCase.move(file, targetDirectoryUri)
                .onSuccess { navigateToDirectory(dirUri) }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Failed to move file"
                    )
                }
        }
    }

    fun toggleFavorite(file: MarkdownFile) {
        viewModelScope.launch {
            favoriteFilesUseCase.toggleFavorite(file)
            _uiState.value.currentDirectoryUri?.let { navigateToDirectory(it) }
        }
    }

    fun selectFile(file: MarkdownFile) {
        _uiState.value = _uiState.value.copy(selectedFile = file)
    }

    fun showCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = true)
    }

    fun showRenameDialog() {
        _uiState.value = _uiState.value.copy(showRenameDialog = true)
    }

    fun showDeleteConfirm() {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = true)
    }

    fun showMoveDialog() {
        _uiState.value = _uiState.value.copy(showMoveDialog = true)
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(
            showCreateDialog = false,
            showRenameDialog = false,
            showDeleteConfirm = false,
            showMoveDialog = false,
            showTemplateDialog = false,
            selectedFile = null
        )
    }

    fun createDailyNote(onCreated: (String) -> Unit) {
        val dirUri = _uiState.value.currentDirectoryUri ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            obsidianRepository.createDailyNote(dirUri)
                .onSuccess { note ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onCreated(note.uri)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to create daily note"
                    )
                }
        }
    }

    fun loadTemplates() {
        val dirUri = _uiState.value.currentDirectoryUri ?: return
        val templates = obsidianRepository.getTemplates(dirUri)
        _uiState.value = _uiState.value.copy(availableTemplates = templates)
    }

    fun createFromTemplate(template: Template, fileName: String, onCreated: (String) -> Unit) {
        val dirUri = _uiState.value.currentDirectoryUri ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, showTemplateDialog = false)
            obsidianRepository.createFromTemplate(template, dirUri, fileName)
                .onSuccess { file ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    navigateToDirectory(dirUri)
                    onCreated(file.uri)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to create from template"
                    )
                }
        }
    }

    fun showTemplateDialog() {
        loadTemplates()
        _uiState.value = _uiState.value.copy(showTemplateDialog = true)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun canNavigateUp(): Boolean {
        return _uiState.value.directoryStack.size > 1
    }

    fun navigateUp() {
        val stack = _uiState.value.directoryStack
        if (stack.size >= 2) {
            val parent = stack[stack.size - 2]
            _uiState.value = _uiState.value.copy(
                directoryStack = stack.dropLast(1)
            )
            navigateToDirectory(parent)
        }
    }
}
