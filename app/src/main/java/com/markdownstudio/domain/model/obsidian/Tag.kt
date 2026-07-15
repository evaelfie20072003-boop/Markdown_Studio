package com.markdownstudio.domain.model.obsidian

data class Tag(
    val name: String,
    val sourceUri: String = "",
    val startOffset: Int = 0,
    val endOffset: Int = 0
) {
    val normalizedName: String get() = name.trim().lowercase()
}
