package com.markdownstudio.ui.recent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.markdownstudio.domain.model.MarkdownFile
import com.markdownstudio.domain.usecase.FavoriteFilesUseCase
import com.markdownstudio.domain.usecase.FileOperationsUseCase
import com.markdownstudio.domain.usecase.RecentFilesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecentUiState(
    val files: List<MarkdownFile> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class RecentViewModel @Inject constructor(
    private val recentFilesUseCase: RecentFilesUseCase,
    private val favoriteFilesUseCase: FavoriteFilesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecentUiState())
    val uiState: StateFlow<RecentUiState> = _uiState.asStateFlow()

    fun loadRecentFiles() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            recentFilesUseCase.getRecentFiles()
                .onSuccess { files ->
                    _uiState.value = RecentUiState(files = files, isLoading = false)
                }
                .onFailure { e ->
                    _uiState.value = RecentUiState(
                        error = e.message ?: "Failed to load recent files",
                        isLoading = false
                    )
                }
        }
    }

    fun toggleFavorite(file: MarkdownFile) {
        viewModelScope.launch {
            favoriteFilesUseCase.toggleFavorite(file)
            loadRecentFiles()
        }
    }
}
