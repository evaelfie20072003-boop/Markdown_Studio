package com.markdownstudio.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.markdownstudio.domain.model.AppSettings
import com.markdownstudio.domain.model.AutoSaveInterval
import com.markdownstudio.domain.model.EditorFont
import com.markdownstudio.domain.model.ThemeMode
import com.markdownstudio.domain.model.ToolbarPosition
import com.markdownstudio.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val previewFontSize: Int = 14,
    val previewLineHeight: Float = 1.5f
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _uiState.value = SettingsUiState(
                    settings = settings,
                    previewFontSize = settings.fontSize,
                    previewLineHeight = settings.lineHeight
                )
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setFont(font: EditorFont) {
        viewModelScope.launch {
            settingsRepository.setFont(font)
            _uiState.value = _uiState.value.copy(
                previewFontSize = when (font) {
                    EditorFont.MONOSPACE -> _uiState.value.settings.fontSize
                    EditorFont.SANS_SERIF -> _uiState.value.settings.fontSize
                    EditorFont.SERIF -> _uiState.value.settings.fontSize
                }
            )
        }
    }

    fun setFontSize(size: Int) {
        viewModelScope.launch {
            settingsRepository.setFontSize(size)
            _uiState.value = _uiState.value.copy(previewFontSize = size)
        }
    }

    fun setLineHeight(height: Float) {
        viewModelScope.launch {
            settingsRepository.setLineHeight(height)
            _uiState.value = _uiState.value.copy(previewLineHeight = height)
        }
    }

    fun setToolbarPosition(position: ToolbarPosition) {
        viewModelScope.launch { settingsRepository.setToolbarPosition(position) }
    }

    fun setAutoSaveInterval(interval: AutoSaveInterval) {
        viewModelScope.launch { settingsRepository.setAutoSaveInterval(interval) }
    }
}
