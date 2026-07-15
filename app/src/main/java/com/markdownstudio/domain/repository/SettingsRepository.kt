package com.markdownstudio.domain.repository

import com.markdownstudio.domain.model.AppSettings
import com.markdownstudio.domain.model.AutoSaveInterval
import com.markdownstudio.domain.model.EditorFont
import com.markdownstudio.domain.model.ThemeMode
import com.markdownstudio.domain.model.ToolbarPosition
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>
    fun getSettings(): AppSettings
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setFont(font: EditorFont)
    suspend fun setFontSize(size: Int)
    suspend fun setLineHeight(height: Float)
    suspend fun setToolbarPosition(position: ToolbarPosition)
    suspend fun setAutoSaveInterval(interval: AutoSaveInterval)
}
