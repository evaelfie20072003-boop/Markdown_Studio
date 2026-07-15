package com.markdownstudio.domain.model

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val fontFamily: String = "monospace",
    val fontSize: Int = 14,
    val lineHeight: Float = 1.5f,
    val toolbarPosition: ToolbarPosition = ToolbarPosition.TOP,
    val autoSaveIntervalMs: Long = 1500L
)

enum class ThemeMode(val displayName: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark"),
    AMOLED("AMOLED Dark")
}

enum class ToolbarPosition(val displayName: String) {
    TOP("Top"),
    BOTTOM("Bottom")
}

enum class EditorFont(val displayName: String, val fontName: String) {
    MONOSPACE("Monospace", "monospace"),
    SANS_SERIF("Sans Serif", "sans-serif"),
    SERIF("Serif", "serif");
}

enum class AutoSaveInterval(val displayName: String, val ms: Long) {
    OFF("Off", 0),
    SECONDS_5("5 seconds", 5000L),
    SECONDS_10("10 seconds", 10000L),
    SECONDS_30("30 seconds", 30000L),
    MINUTE_1("1 minute", 60000L)
}
