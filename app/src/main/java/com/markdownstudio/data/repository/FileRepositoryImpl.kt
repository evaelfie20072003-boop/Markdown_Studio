package com.markdownstudio.data.repository

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.markdownstudio.data.local.dao.FavoriteFileDao
import com.markdownstudio.data.local.dao.RecentFileDao
import com.markdownstudio.data.local.entity.FavoriteFileEntity
import com.markdownstudio.data.local.entity.RecentFileEntity
import com.markdownstudio.domain.model.MarkdownFile
import com.markdownstudio.domain.repository.FileRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val recentFileDao: RecentFileDao,
    private val favoriteFileDao: FavoriteFileDao
) : FileRepository {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getRootDirectoryUri(): String? {
        return prefs.getString(KEY_ROOT_URI, null)
    }

    override suspend fun setRootDirectoryUri(uri: String) {
        prefs.edit().putString(KEY_ROOT_URI, uri).apply()
        try {
            context.contentResolver.takePersistableUriPermission(
                Uri.parse(uri),
                FLAGS
            )
        } catch (_: Exception) {
        }
    }

    override fun getFiles(directoryUri: String): Result<List<MarkdownFile>> {
        return try {
            val uri = Uri.parse(directoryUri)
            val docFile = DocumentFile.fromTreeUri(context, uri)
                ?: DocumentFile.fromSingleUri(context, uri)

            val children = docFile?.listFiles()
                ?.filter { it.isFile && it.name?.endsWith(".md") == true || it.isDirectory }
                ?.map { it.toMarkdownFile(directoryUri) }
                ?: emptyList()

            val favoriteUris = favoriteFileDao.getFavoriteFiles().map { it.uri }.toSet()
            Result.success(children.map { it.copy(isFavorite = it.uri in favoriteUris) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createFile(directoryUri: String, name: String): Result<MarkdownFile> {
        return try {
            val dirDoc = DocumentFile.fromTreeUri(context, Uri.parse(directoryUri))
                ?: return Result.failure(Exception("Cannot access directory"))

            val fileName = if (name.endsWith(".md")) name else "$name.md"
            val file = dirDoc.createFile("text/markdown", fileName.removeSuffix(".md"))
                ?: return Result.failure(Exception("Failed to create file"))

            Result.success(file.toMarkdownFile(directoryUri))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun renameFile(file: MarkdownFile, newName: String): Result<MarkdownFile> {
        return try {
            val docFile = DocumentFile.fromSingleUri(context, Uri.parse(file.uri))
                ?: return Result.failure(Exception("File not found"))

            val newFileName = if (newName.endsWith(".md")) newName else "$newName.md"
            if (!docFile.renameTo(newFileName)) {
                return Result.failure(Exception("Rename failed"))
            }

            Result.success(docFile.toMarkdownFile(file.parentUri ?: ""))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteFile(file: MarkdownFile): Result<Unit> {
        return try {
            val docFile = DocumentFile.fromSingleUri(context, Uri.parse(file.uri))
                ?: return Result.failure(Exception("File not found"))

            if (!docFile.delete()) {
                return Result.failure(Exception("Delete failed"))
            }

            recentFileDao.removeRecentFile(file.uri)
            favoriteFileDao.removeFavorite(file.uri)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun duplicateFile(file: MarkdownFile): Result<MarkdownFile> {
        return try {
            val sourceDoc = DocumentFile.fromSingleUri(context, Uri.parse(file.uri))
                ?: return Result.failure(Exception("File not found"))

            val parentUri = file.parentUri ?: return Result.failure(Exception("No parent"))
            val parentDoc = DocumentFile.fromTreeUri(context, Uri.parse(parentUri))
                ?: return Result.failure(Exception("Cannot access parent"))

            val baseName = file.name.removeSuffix(".md")
            val newName = "${baseName} (copy).md"

            val newFile = parentDoc.createFile(
                "text/markdown",
                newName.removeSuffix(".md")
            ) ?: return Result.failure(Exception("Failed to create copy"))

            context.contentResolver.openInputStream(Uri.parse(file.uri))?.use { input ->
                context.contentResolver.openOutputStream(newFile.uri)?.use { output ->
                    input.copyTo(output)
                }
            }

            Result.success(newFile.toMarkdownFile(parentUri))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun moveFile(
        file: MarkdownFile,
        targetDirectoryUri: String
    ): Result<MarkdownFile> {
        return duplicateFile(file).fold(
            onSuccess = { copy ->
                deleteFile(file)
                Result.success(copy)
            },
            onFailure = { Result.failure(it) }
        )
    }

    override suspend fun readFile(file: MarkdownFile): Result<String> {
        return try {
            val content = context.contentResolver.openInputStream(Uri.parse(file.uri))
                ?.bufferedReader()?.use { it.readText() }
                ?: return Result.failure(Exception("Cannot open file"))

            addRecentFile(file)
            Result.success(content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun writeFile(file: MarkdownFile, content: String): Result<Unit> {
        return try {
            context.contentResolver.openOutputStream(Uri.parse(file.uri), "wt")
                ?.use { it.write(content.toByteArray()) }
                ?: return Result.failure(Exception("Cannot write to file"))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun searchFiles(query: String): Result<List<MarkdownFile>> {
        return try {
            val rootUri = prefs.getString(KEY_ROOT_URI, null)
                ?: return Result.success(emptyList())

            val results = mutableListOf<MarkdownFile>()
            searchRecursive(Uri.parse(rootUri), query.lowercase(), results)

            val favoriteUris = favoriteFileDao.getFavoriteFiles().map { it.uri }.toSet()
            Result.success(results.map { it.copy(isFavorite = it.uri in favoriteUris) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun searchRecursive(
        dirUri: Uri,
        query: String,
        results: MutableList<MarkdownFile>
    ) {
        if (results.size >= MAX_SEARCH_RESULTS) return

        val dirDoc = DocumentFile.fromTreeUri(context, dirUri) ?: return
        for (file in dirDoc.listFiles()) {
            if (results.size >= MAX_SEARCH_RESULTS) break
            if (file.isDirectory) {
                searchRecursive(file.uri, query, results)
            } else if (file.isFile &&
                file.name?.lowercase()?.contains(query) == true &&
                file.name?.endsWith(".md") == true
            ) {
                results.add(file.toMarkdownFile(dirUri.toString()))
            }
        }
    }

    override fun getRecentFiles(): Result<List<MarkdownFile>> {
        return try {
            val entities = recentFileDao.getRecentFiles()
            Result.success(entities.map { entity ->
                MarkdownFile(
                    uri = entity.uri,
                    name = entity.name,
                    size = 0L,
                    lastModified = entity.lastOpenedAt,
                    isDirectory = false,
                    parentUri = entity.parentUri,
                    isFavorite = favoriteFileDao.isFavorite(entity.uri)
                )
            })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addRecentFile(file: MarkdownFile) {
        recentFileDao.addRecentFile(
            RecentFileEntity(
                uri = file.uri,
                name = file.name,
                parentUri = file.parentUri
            )
        )
    }

    override fun getFavoriteFiles(): Result<List<MarkdownFile>> {
        return try {
            val entities = favoriteFileDao.getFavoriteFiles()
            Result.success(entities.map { entity ->
                MarkdownFile(
                    uri = entity.uri,
                    name = entity.name,
                    size = 0L,
                    lastModified = entity.addedAt,
                    isDirectory = false,
                    parentUri = entity.parentUri,
                    isFavorite = true
                )
            })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleFavorite(file: MarkdownFile) {
        if (favoriteFileDao.isFavorite(file.uri)) {
            favoriteFileDao.removeFavorite(file.uri)
        } else {
            favoriteFileDao.addFavorite(
                FavoriteFileEntity(
                    uri = file.uri,
                    name = file.name,
                    parentUri = file.parentUri
                )
            )
        }
    }

    override fun isFavorite(uri: String): Boolean {
        return favoriteFileDao.isFavorite(uri)
    }

    private fun DocumentFile.toMarkdownFile(parentUri: String): MarkdownFile {
        val uriStr = this.uri.toString()
        return MarkdownFile(
            uri = uriStr,
            name = this.name ?: "Unknown",
            size = this.length(),
            lastModified = this.lastModified(),
            isDirectory = this.isDirectory,
            parentUri = parentUri,
            isFavorite = favoriteFileDao.isFavorite(uriStr)
        )
    }

    companion object {
        private const val PREFS_NAME = "markdown_studio_prefs"
        private const val KEY_ROOT_URI = "root_directory_uri"
        private val FLAGS = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
            android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        private const val MAX_SEARCH_RESULTS = 200
    }
}
