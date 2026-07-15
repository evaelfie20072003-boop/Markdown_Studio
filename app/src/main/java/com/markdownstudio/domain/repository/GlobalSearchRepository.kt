package com.markdownstudio.domain.repository

import com.markdownstudio.domain.model.search.SearchQuery
import com.markdownstudio.domain.model.search.SearchResult

interface GlobalSearchRepository {
    suspend fun search(query: SearchQuery): List<SearchResult>
    suspend fun getContentMatches(uri: String, query: SearchQuery): List<SearchResult>
    suspend fun replaceInFile(
        uri: String,
        searchText: String,
        replaceText: String,
        useRegex: Boolean
    ): Result<Int>
    suspend fun replaceAll(
        results: List<SearchResult>,
        searchText: String,
        replaceText: String,
        useRegex: Boolean
    ): Result<Int>
}
