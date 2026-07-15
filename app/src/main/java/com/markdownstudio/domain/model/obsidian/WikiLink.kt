package com.markdownstudio.domain.model.obsidian

data class WikiLink(
    val target: String,
    val displayText: String? = null,
    val sourceUri: String = "",
    val startOffset: Int = 0,
    val endOffset: Int = 0
) {
    val resolvedName: String get() = displayText ?: target
    val normalizedTarget: String get() = target
        .replace(".md", "")
        .trim()
        .lowercase()
}
