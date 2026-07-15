package com.markdownstudio.data.repository

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.markdownstudio.domain.model.BackupReport
import com.markdownstudio.domain.repository.BackupRepository
import com.markdownstudio.domain.repository.FileRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileRepository: FileRepository
) : BackupRepository {

    override suspend fun exportAll(
        outputTreeUri: Uri,
        onProgress: (completed: Int, total: Int) -> Unit
    ): Result<BackupReport> = withContext(Dispatchers.IO) {
        runCatching {
            val rootUri = fileRepository.getRootDirectoryUri()
                ?: return@runCatching Result.failure(Exception("No root directory set"))
            val rootDoc = DocumentFile.fromTreeUri(context, Uri.parse(rootUri))
                ?: return@runCatching Result.failure(Exception("Cannot access root directory"))

            val allMarkdown = collectMarkdownFiles(rootDoc)
            if (allMarkdown.isEmpty()) {
                return@runCatching Result.success(BackupReport())
            }

            val destTree = DocumentFile.fromTreeUri(context, outputTreeUri)
                ?: return@runCatching Result.failure(Exception("Cannot access destination"))

            var successCount = 0
            val errors = mutableListOf<String>()

            allMarkdown.forEachIndexed { index, sourceFile ->
                onProgress(index + 1, allMarkdown.size)
                val content = readContent(sourceFile.uri)
                if (content != null) {
                    val relativePath = getRelativePath(rootDoc.uri, sourceFile.uri)
                    val destFile = createFileRecursive(destTree, relativePath)
                    if (destFile != null && writeContent(destFile.uri, content)) {
                        successCount++
                    } else {
                        errors.add("Failed to write: ${sourceFile.name}")
                    }
                } else {
                    errors.add("Failed to read: ${sourceFile.name}")
                }
            }

            Result.success(
                BackupReport(
                    totalFiles = allMarkdown.size,
                    successfulFiles = successCount,
                    failedFiles = allMarkdown.size - successCount,
                    errors = errors
                )
            )
        }.getOrElse { e ->
            Result.failure(Exception("Export failed: ${e.message}"))
        }
    }

    override suspend fun importFrom(
        inputTreeUri: Uri,
        onProgress: (completed: Int, total: Int) -> Unit
    ): Result<BackupReport> = withContext(Dispatchers.IO) {
        runCatching {
            val rootUri = fileRepository.getRootDirectoryUri()
                ?: return@runCatching Result.failure(Exception("No root directory set"))
            val rootDoc = DocumentFile.fromTreeUri(context, Uri.parse(rootUri))
                ?: return@runCatching Result.failure(Exception("Cannot access root directory"))

            val sourceTree = DocumentFile.fromTreeUri(context, inputTreeUri)
                ?: return@runCatching Result.failure(Exception("Cannot access source"))

            val allMarkdown = collectMarkdownFiles(sourceTree)
            if (allMarkdown.isEmpty()) {
                return@runCatching Result.success(BackupReport(0, 0, 0))
            }

            var successCount = 0
            val errors = mutableListOf<String>()

            allMarkdown.forEachIndexed { index, sourceFile ->
                onProgress(index + 1, allMarkdown.size)
                val content = readContent(sourceFile.uri)
                if (content != null) {
                    val relativePath = getRelativePath(sourceTree.uri, sourceFile.uri)
                    val destFile = createFileRecursive(rootDoc, relativePath)
                    if (destFile != null && writeContent(destFile.uri, content)) {
                        successCount++
                    } else {
                        errors.add("Failed to write: ${sourceFile.name}")
                    }
                } else {
                    errors.add("Failed to read: ${sourceFile.name}")
                }
            }

            Result.success(
                BackupReport(
                    totalFiles = allMarkdown.size,
                    successfulFiles = successCount,
                    failedFiles = allMarkdown.size - successCount,
                    errors = errors
                )
            )
        }.getOrElse { e ->
            Result.failure(Exception("Import failed: ${e.message}"))
        }
    }

    override suspend fun createBackupZip(
        outputZipUri: Uri,
        onProgress: (completed: Int, total: Int) -> Unit
    ): Result<BackupReport> = withContext(Dispatchers.IO) {
        runCatching {
            val rootUri = fileRepository.getRootDirectoryUri()
                ?: return@runCatching Result.failure(Exception("No root directory set"))
            val rootDoc = DocumentFile.fromTreeUri(context, Uri.parse(rootUri))
                ?: return@runCatching Result.failure(Exception("Cannot access root directory"))

            val allMarkdown = collectMarkdownFiles(rootDoc)
            if (allMarkdown.isEmpty()) {
                return@runCatching Result.success(BackupReport(0, 0, 0))
            }

            val tempZip = File(context.cacheDir, "backup_${System.currentTimeMillis()}.zip")
            var successCount = 0
            val errors = mutableListOf<String>()

            try {
                ZipOutputStream(FileOutputStream(tempZip)).use { zos ->
                    allMarkdown.forEachIndexed { index, sourceFile ->
                        onProgress(index + 1, allMarkdown.size)
                        val content = readContent(sourceFile.uri)
                        if (content != null) {
                            val relativePath = getRelativePath(rootDoc.uri, sourceFile.uri)
                            zos.putNextEntry(ZipEntry(relativePath))
                            zos.write(content.toByteArray(Charsets.UTF_8))
                            zos.closeEntry()
                            successCount++
                        } else {
                            errors.add("Failed to read: ${sourceFile.name}")
                        }
                    }
                }

                context.contentResolver.openOutputStream(outputZipUri)?.use { out ->
                    tempZip.inputStream().use { it.copyTo(out) }
                } ?: throw Exception("Cannot write ZIP to destination")

                tempZip.delete()

                Result.success(
                    BackupReport(
                        totalFiles = allMarkdown.size,
                        successfulFiles = successCount,
                        failedFiles = allMarkdown.size - successCount,
                        errors = errors
                    )
                )
            } catch (e: Exception) {
                tempZip.delete()
                throw e
            }
        }.getOrElse { e ->
            Result.failure(Exception("ZIP backup failed: ${e.message}"))
        }
    }

    override suspend fun restoreFromZip(
        inputZipUri: Uri,
        onProgress: (completed: Int, total: Int) -> Unit
    ): Result<BackupReport> = withContext(Dispatchers.IO) {
        runCatching {
            val rootUri = fileRepository.getRootDirectoryUri()
                ?: return@runCatching Result.failure(Exception("No root directory set"))
            val rootDoc = DocumentFile.fromTreeUri(context, Uri.parse(rootUri))
                ?: return@runCatching Result.failure(Exception("Cannot access root directory"))

            val zipBytes = context.contentResolver.openInputStream(inputZipUri)?.use {
                it.readBytes()
            } ?: return@runCatching Result.failure(Exception("Cannot read ZIP file"))

            val entries = mutableListOf<Pair<String, ByteArray>>()
            ZipInputStream(zipBytes.inputStream()).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory && entry.name.endsWith(".md")) {
                        entries.add(entry.name to zis.readBytes())
                    }
                    entry = zis.nextEntry
                }
            }

            if (entries.isEmpty()) {
                return@runCatching Result.success(BackupReport(0, 0, 0))
            }

            var successCount = 0
            val errors = mutableListOf<String>()

            entries.forEachIndexed { index, (path, content) ->
                onProgress(index + 1, entries.size)
                val destFile = createFileRecursive(rootDoc, path)
                if (destFile != null && writeContent(destFile.uri, String(content, Charsets.UTF_8))) {
                    successCount++
                } else {
                    errors.add("Failed to write: $path")
                }
            }

            Result.success(
                BackupReport(
                    totalFiles = entries.size,
                    successfulFiles = successCount,
                    failedFiles = entries.size - successCount,
                    errors = errors
                )
            )
        }.getOrElse { e ->
            Result.failure(Exception("Restore failed: ${e.message}"))
        }
    }

    private fun collectMarkdownFiles(docFile: DocumentFile): List<DocumentFile> {
        val results = mutableListOf<DocumentFile>()
        val children = docFile.listFiles()
        for (child in children) {
            if (child.isDirectory) {
                results.addAll(collectMarkdownFiles(child))
            } else if (child.isFile && child.name?.endsWith(".md") == true) {
                results.add(child)
            }
        }
        return results
    }

    private fun getRelativePath(rootUri: Uri, fileUri: Uri): String {
        val rootStr = rootUri.toString().trimEnd('/')
        val fileStr = fileUri.toString()
        val relative = fileStr.removePrefix(rootStr).trimStart('/')
        return relative.substringAfter('/')
    }

    private fun readContent(uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use {
                it.readText()
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun writeContent(uri: Uri, content: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri, "wt")?.use {
                it.write(content.toByteArray(Charsets.UTF_8))
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun createFileRecursive(tree: DocumentFile, relativePath: String): DocumentFile? {
        val parts = relativePath.split("/")
        if (parts.isEmpty()) return null

        var current = tree
        for (i in 0 until parts.size - 1) {
            val dirName = parts[i]
            val existing = current.findFile(dirName)
            current = existing ?: current.createDirectory(dirName) ?: return null
        }

        val fileName = parts.last()
        val baseName = fileName.removeSuffix(".md")
        return current.findFile(fileName)
            ?: current.createFile("text/markdown", baseName)
    }
}
