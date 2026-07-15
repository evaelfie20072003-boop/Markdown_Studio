package com.markdownstudio.domain.usecase

import com.markdownstudio.domain.model.MarkdownFile
import com.markdownstudio.domain.repository.FileRepository
import javax.inject.Inject

class FileOperationsUseCase @Inject constructor(
    private val repository: FileRepository
) {
    suspend fun create(directoryUri: String, name: String): Result<MarkdownFile> {
        return repository.createFile(directoryUri, name)
    }

    suspend fun rename(file: MarkdownFile, newName: String): Result<MarkdownFile> {
        return repository.renameFile(file, newName)
    }

    suspend fun delete(file: MarkdownFile): Result<Unit> {
        return repository.deleteFile(file)
    }

    suspend fun duplicate(file: MarkdownFile): Result<MarkdownFile> {
        return repository.duplicateFile(file)
    }

    suspend fun move(file: MarkdownFile, targetDirectoryUri: String): Result<MarkdownFile> {
        return repository.moveFile(file, targetDirectoryUri)
    }

    suspend fun read(file: MarkdownFile): Result<String> {
        return repository.readFile(file)
    }

    suspend fun write(file: MarkdownFile, content: String): Result<Unit> {
        return repository.writeFile(file, content)
    }
}
