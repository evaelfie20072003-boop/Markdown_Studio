package com.markdownstudio.domain.repository

import android.net.Uri
import com.markdownstudio.domain.model.BackupReport

interface BackupRepository {
    suspend fun exportAll(
        outputTreeUri: Uri,
        onProgress: (completed: Int, total: Int) -> Unit
    ): Result<BackupReport>

    suspend fun importFrom(
        inputTreeUri: Uri,
        onProgress: (completed: Int, total: Int) -> Unit
    ): Result<BackupReport>

    suspend fun createBackupZip(
        outputZipUri: Uri,
        onProgress: (completed: Int, total: Int) -> Unit
    ): Result<BackupReport>

    suspend fun restoreFromZip(
        inputZipUri: Uri,
        onProgress: (completed: Int, total: Int) -> Unit
    ): Result<BackupReport>
}
