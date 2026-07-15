package com.markdownstudio.domain.model

data class BackupReport(
    val totalFiles: Int = 0,
    val successfulFiles: Int = 0,
    val failedFiles: Int = 0,
    val errors: List<String> = emptyList()
)
