package com.markdownstudio.domain.model

data class MarkdownFile(
    val uri: String,
    val name: String,
    val size: Long,
    val lastModified: Long,
    val isDirectory: Boolean,
    val parentUri: String? = null,
    val isFavorite: Boolean = false
)
