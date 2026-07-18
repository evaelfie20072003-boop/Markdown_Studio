package com.markdownstudio.domain.usecase

import com.markdownstudio.domain.model.MarkdownFile
import com.markdownstudio.domain.repository.FileRepository
import javax.inject.Inject

class SearchFilesUseCase @Inject constructor(
    private val repository: FileRepository
) {
    suspend fun execute(query: String): Result<List<MarkdownFile>> {
        return repository.searchFiles(query)
    }
}