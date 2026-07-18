package com.markdownstudio.domain.usecase

import com.markdownstudio.domain.model.MarkdownFile
import com.markdownstudio.domain.repository.FileRepository
import javax.inject.Inject

class GetFilesUseCase @Inject constructor(
    private val repository: FileRepository
) {
    suspend fun execute(directoryUri: String): Result<List<MarkdownFile>> {
        return repository.getFiles(directoryUri)
    }
}