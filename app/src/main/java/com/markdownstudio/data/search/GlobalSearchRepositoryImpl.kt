package com.markdownstudio.data.search

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.markdownstudio.data.local.dao.obsidian.TagDao
import com.markdownstudio.data.local.dao.obsidian.WikiLinkDao
import com.markdownstudio.domain.model.search.SearchMatchType
import com.markdownstudio.domain.model.search.SearchQuery
import com.markdownstudio.domain.model.search.SearchResult
import com.markdownstudio.domain.repository.GlobalSearchRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlobalSearchRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tagDao: TagDao,
    private val wikiLinkDao: WikiLinkDao
) : GlobalSearchRepository {

    override suspend fun search(query: SearchQuery): List<SearchResult> {
        if (!query.isValid) return emptyList()

        val prefs = context.getSharedPreferences("markdown_studio_prefs", Context.MODE_PRIVATE)
        val rootUri = prefs.getString("root_directory_uri", null) ?: return emptyList()

        val results = mutableListOf<SearchResult>()
        val regex = query.toRegex()

        if (SearchMatchType.FILE_NAME in query.matchTypes) {
            results.addAll(searchFileNames(Uri.parse(rootUri), regex))
        }
        if (SearchMatchType.FOLDER_NAME in query.matchTypes) {
            results.addAll(searchFolderNames(Uri.parse(rootUri), regex))
        }
        if (SearchMatchType.CONTENT in query.matchTypes) {
            results.addAll(searchContent(Uri.parse(rootUri), regex, query))
        }
        if (SearchMatchType.TAG in query.matchTypes) {
            results.addAll(searchTags(regex, query))
        }
        if (SearchMatchType.WIKI_LINK in query.matchTypes) {
            results.addAll(searchWikiLinks(regex, query))
        }

        return results.sortedByDescending { it.relevance }
    }

    override suspend fun getContentMatches(
        uri: String,
        query: SearchQuery
    ): List<SearchResult> {
        if (!query.isValid) return emptyList()

        return try {
            val content = context.contentResolver.openInputStream(Uri.parse(uri))
                ?.bufferedReader()?.use { it.readText() } ?: return emptyList()

            val regex = query.toRegex()
            val lines = content.split("\n")
            val results = mutableListOf<SearchResult>()

            lines.forEachIndexed { index, line ->
                regex.findAll(line).forEach { match ->
                    results.add(
                        SearchResult(
                            uri = uri,
                            fileName = uri.substringAfterLast("/"),
                            matchType = SearchMatchType.CONTENT,
                            contextLine = line.trim(),
                            lineNumber = index + 1,
                            matchStart = match.range.first,
                            matchEnd = match.range.last + 1,
                            matchCount = 1,
                            relevance = regex.findAll(line).count().toFloat()
                        )
                    )
                }
            }

            results
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun replaceInFile(
        uri: String,
        searchText: String,
        replaceText: String,
        useRegex: Boolean
    ): Result<Int> {
        return try {
            val input = context.contentResolver.openInputStream(Uri.parse(uri))
                ?.bufferedReader()?.use { it.readText() }
                ?: return Result.failure(Exception("Cannot read file"))

            val pattern = if (useRegex) searchText else Regex.escape(searchText)
            val regex = Regex(pattern)
            val matchCount = regex.findAll(input).count()
            val result = input.replace(regex, replaceText)

            context.contentResolver.openOutputStream(Uri.parse(uri), "wt")?.use {
                it.write(result.toByteArray())
            } ?: return Result.failure(Exception("Cannot write file"))

            Result.success(matchCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun replaceAll(
        results: List<SearchResult>,
        searchText: String,
        replaceText: String,
        useRegex: Boolean
    ): Result<Int> {
        var totalReplaced = 0
        val seenUris = mutableSetOf<String>()

        for (result in results) {
            if (result.uri !in seenUris) {
                seenUris.add(result.uri)
                when (result.matchType) {
                    SearchMatchType.CONTENT -> {
                        replaceInFile(result.uri, searchText, replaceText, useRegex)
                            .onSuccess { totalReplaced += it }
                    }
                    else -> {}
                }
            }
        }

        return Result.success(totalReplaced)
    }

    private fun searchFileNames(uri: Uri, regex: Regex): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        searchFileNamesRecursive(uri, regex, results)
        return results
    }

    private fun searchFileNamesRecursive(
        uri: Uri,
        regex: Regex,
        results: MutableList<SearchResult>
    ) {
        if (results.size >= MAX_RESULTS) return
        try {
            val docFile = DocumentFile.fromTreeUri(context, uri) ?: return
            for (file in docFile.listFiles()) {
                if (results.size >= MAX_RESULTS) break
                if (file.isDirectory) {
                    if (file.name != null && regex.containsMatchIn(file.name!!)) {
                        results.add(
                            SearchResult(
                                uri = file.uri.toString(),
                                fileName = file.name!!,
                                matchType = SearchMatchType.FOLDER_NAME,
                                relevance = 0.8f
                            )
                        )
                    }
                    searchFileNamesRecursive(file.uri, regex, results)
                } else if (file.isFile && (file.name?.endsWith(".md") == true || file.name?.endsWith(".txt") == true)) {
                    if (file.name != null && regex.containsMatchIn(file.name!!)) {
                        results.add(
                            SearchResult(
                                uri = file.uri.toString(),
                                fileName = file.name!!,
                                matchType = SearchMatchType.FILE_NAME,
                                relevance = 1.0f
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {}
    }

    private fun searchFolderNames(uri: Uri, regex: Regex): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        searchFolderNamesRecursive(uri, regex, results)
        return results
    }

    private fun searchFolderNamesRecursive(
        uri: Uri,
        regex: Regex,
        results: MutableList<SearchResult>
    ) {
        if (results.size >= MAX_RESULTS) return
        try {
            val docFile = DocumentFile.fromTreeUri(context, uri) ?: return
            for (file in docFile.listFiles()) {
                if (results.size >= MAX_RESULTS) break
                if (file.isDirectory) {
                    if (file.name != null && regex.containsMatchIn(file.name!!)) {
                        results.add(
                            SearchResult(
                                uri = file.uri.toString(),
                                fileName = file.name!!,
                                matchType = SearchMatchType.FOLDER_NAME,
                                relevance = 0.9f
                            )
                        )
                    }
                    searchFolderNamesRecursive(file.uri, regex, results)
                }
            }
        } catch (_: Exception) {}
    }

    private suspend fun searchContent(
        uri: Uri,
        regex: Regex,
        query: SearchQuery
    ): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        searchContentRecursive(uri, regex, results)
        return results
    }

    private suspend fun searchContentRecursive(
        uri: Uri,
        regex: Regex,
        results: MutableList<SearchResult>
    ) {
        if (results.size >= MAX_RESULTS) return
        try {
            val docFile = DocumentFile.fromTreeUri(context, uri) ?: return
            for (file in docFile.listFiles()) {
                if (results.size >= MAX_RESULTS) break
                if (file.isDirectory) {
                    searchContentRecursive(file.uri, regex, results)
                } else if (file.isFile && (file.name?.endsWith(".md") == true || file.name?.endsWith(".txt") == true)) {
                    try {
                        val content = context.contentResolver.openInputStream(file.uri)
                            ?.bufferedReader()?.use { it.readText() } ?: continue

                        val lines = content.split("\n")
                        var fileMatches = 0
                        for ((index, line) in lines.withIndex()) {
                            if (results.size >= MAX_RESULTS) break
                            regex.findAll(line).forEach { match ->
                                results.add(
                                    SearchResult(
                                        uri = file.uri.toString(),
                                        fileName = file.name ?: "Unknown",
                                        matchType = SearchMatchType.CONTENT,
                                        contextLine = line.trim(),
                                        lineNumber = index + 1,
                                        matchStart = match.range.first,
                                        matchEnd = match.range.last + 1,
                                        relevance = 0.7f + fileMatches * 0.05f
                                    )
                                )
                                fileMatches++
                            }
                        }
                    } catch (_: Exception) {}
                }
            }
        } catch (_: Exception) {}
    }

    private suspend fun searchTags(
        regex: Regex,
        query: SearchQuery
    ): List<SearchResult> {
        val allTags = tagDao.getAllTags()
        val results = mutableListOf<SearchResult>()

        for (tag in allTags) {
            if (results.size >= MAX_RESULTS) break
            if (regex.containsMatchIn(tag.name)) {
                val fileUri = tag.sourceUri
                results.add(
                    SearchResult(
                        uri = fileUri,
                        fileName = fileUri.substringAfterLast("/"),
                        matchType = SearchMatchType.TAG,
                        contextLine = tag.name,
                        relevance = 0.85f
                    )
                )
            }
        }

        return results
    }

    private suspend fun searchWikiLinks(
        regex: Regex,
        query: SearchQuery
    ): List<SearchResult> {
        val allTargets = wikiLinkDao.getAllTargets()
        val results = mutableListOf<SearchResult>()
        val seen = mutableSetOf<String>()

        for (target in allTargets) {
            if (results.size >= MAX_RESULTS) break
            if (regex.containsMatchIn(target)) {
                val sources = wikiLinkDao.getBacklinkSources(target)
                for (source in sources) {
                    val key = "$source->$target"
                    if (key !in seen) {
                        seen.add(key)
                        results.add(
                            SearchResult(
                                uri = source,
                                fileName = source.substringAfterLast("/"),
                                matchType = SearchMatchType.WIKI_LINK,
                                contextLine = target,
                                relevance = 0.75f
                            )
                        )
                    }
                }
            }
        }

        return results
    }

    companion object {
        private const val MAX_RESULTS = 200
    }
}
