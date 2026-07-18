package com.markdownstudio.domain.usecase

import com.markdownstudio.domain.model.MarkdownFile
import com.markdownstudio.domain.repository.FileRepository
import javax.inject.Inject

class RecentFilesUseCase @Inject constructor(
    private val repository: FileRepository
) {
    suspend fun getRecentFiles(): Result<List<MarkdownFile>> {
        return repository.getRecentFiles()
    }

    suspend fun addRecentFile(file: MarkdownFile) {
        repository.addRecentFile(file)
    }
}