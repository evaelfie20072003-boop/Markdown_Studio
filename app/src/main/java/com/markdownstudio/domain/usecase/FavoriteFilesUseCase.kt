package com.markdownstudio.domain.usecase

import com.markdownstudio.domain.model.MarkdownFile
import com.markdownstudio.domain.repository.FileRepository
import javax.inject.Inject

class FavoriteFilesUseCase @Inject constructor(
    private val repository: FileRepository
) {
    fun getFavoriteFiles(): Result<List<MarkdownFile>> {
        return repository.getFavoriteFiles()
    }

    suspend fun toggleFavorite(file: MarkdownFile) {
        repository.toggleFavorite(file)
    }

    fun isFavorite(uri: String): Boolean {
        return repository.isFavorite(uri)
    }
}
