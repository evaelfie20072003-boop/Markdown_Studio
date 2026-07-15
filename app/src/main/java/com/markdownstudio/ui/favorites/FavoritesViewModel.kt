package com.markdownstudio.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.markdownstudio.domain.model.MarkdownFile
import com.markdownstudio.domain.usecase.FavoriteFilesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val files: List<MarkdownFile> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoriteFilesUseCase: FavoriteFilesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    fun loadFavorites() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            favoriteFilesUseCase.getFavoriteFiles()
                .onSuccess { files ->
                    _uiState.value = FavoritesUiState(files = files, isLoading = false)
                }
                .onFailure { e ->
                    _uiState.value = FavoritesUiState(
                        error = e.message ?: "Failed to load favorites",
                        isLoading = false
                    )
                }
        }
    }

    fun removeFavorite(file: MarkdownFile) {
        viewModelScope.launch {
            favoriteFilesUseCase.toggleFavorite(file)
            loadFavorites()
        }
    }
}
