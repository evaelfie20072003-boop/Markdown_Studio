package com.markdownstudio.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.markdownstudio.domain.model.AppSettings
import com.markdownstudio.domain.model.AutoSaveInterval
import com.markdownstudio.domain.model.EditorFont
import com.markdownstudio.domain.model.ThemeMode
import com.markdownstudio.domain.model.ToolbarPosition
import com.markdownstudio.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
) : SettingsRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("markdown_studio_settings", Context.MODE_PRIVATE)

    override val settings: Flow<AppSettings> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            trySend(getSettings())
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(getSettings())
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override fun getSettings(): AppSettings {
        return AppSettings(
            themeMode = ThemeMode.valueOf(
                prefs.getString(KEY_THEME, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
            ),
            fontFamily = prefs.getString(KEY_FONT, EditorFont.MONOSPACE.fontName) ?: EditorFont.MONOSPACE.fontName,
            fontSize = prefs.getInt(KEY_FONT_SIZE, 14).coerceIn(10, 28),
            lineHeight = prefs.getFloat(KEY_LINE_HEIGHT, 1.5f).coerceIn(1.0f, 2.5f),
            toolbarPosition = ToolbarPosition.valueOf(
                prefs.getString(KEY_TOOLBAR, ToolbarPosition.TOP.name) ?: ToolbarPosition.TOP.name
            ),
            autoSaveIntervalMs = prefs.getLong(KEY_AUTO_SAVE, 1500L).coerceAtLeast(0)
        )
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME, mode.name).apply()
    }

    override suspend fun setFont(font: EditorFont) {
        prefs.edit().putString(KEY_FONT, font.fontName).apply()
    }

    override suspend fun setFontSize(size: Int) {
        prefs.edit().putInt(KEY_FONT_SIZE, size.coerceIn(10, 28)).apply()
    }

    override suspend fun setLineHeight(height: Float) {
        prefs.edit().putFloat(KEY_LINE_HEIGHT, height.coerceIn(1.0f, 2.5f)).apply()
    }

    override suspend fun setToolbarPosition(position: ToolbarPosition) {
        prefs.edit().putString(KEY_TOOLBAR, position.name).apply()
    }

    override suspend fun setAutoSaveInterval(interval: AutoSaveInterval) {
        prefs.edit().putLong(KEY_AUTO_SAVE, interval.ms).apply()
    }

    companion object {
        private const val KEY_THEME = "theme_mode"
        private const val KEY_FONT = "editor_font"
        private const val KEY_FONT_SIZE = "editor_font_size"
        private const val KEY_LINE_HEIGHT = "editor_line_height"
        private const val KEY_TOOLBAR = "toolbar_position"
        private const val KEY_AUTO_SAVE = "auto_save_interval"
    }
}
