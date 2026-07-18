package com.markdownstudio.domain.repository

import com.markdownstudio.domain.model.MarkdownFile

interface FileRepository {
    fun getRootDirectoryUri(): String?
    suspend fun setRootDirectoryUri(uri: String)
    suspend fun getFiles(directoryUri: String): Result<List<MarkdownFile>>
    suspend fun createFile(directoryUri: String, name: String): Result<MarkdownFile>
    suspend fun renameFile(file: MarkdownFile, newName: String): Result<MarkdownFile>
    suspend fun deleteFile(file: MarkdownFile): Result<Unit>
    suspend fun duplicateFile(file: MarkdownFile): Result<MarkdownFile>
    suspend fun moveFile(file: MarkdownFile, targetDirectoryUri: String): Result<MarkdownFile>
    suspend fun readFile(file: MarkdownFile): Result<String>
    suspend fun writeFile(file: MarkdownFile, content: String): Result<Unit>
    suspend fun searchFiles(query: String): Result<List<MarkdownFile>>
    suspend fun getRecentFiles(): Result<List<MarkdownFile>>
    suspend fun addRecentFile(file: MarkdownFile)
    suspend fun getFavoriteFiles(): Result<List<MarkdownFile>>
    suspend fun toggleFavorite(file: MarkdownFile)
    suspend fun isFavorite(uri: String): Boolean
}
