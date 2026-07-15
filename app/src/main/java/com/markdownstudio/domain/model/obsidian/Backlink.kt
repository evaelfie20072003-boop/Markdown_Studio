package com.markdownstudio.domain.model.obsidian

data class Backlink(
    val sourceUri: String,
    val sourceName: String,
    val targetUri: String,
    val targetName: String,
    val displayText: String? = null,
    val contextLine: String = "",
    val lineNumber: Int = 0
)
