package com.markdownstudio.domain.model.search

data class SearchResult(
    val uri: String,
    val fileName: String,
    val matchType: SearchMatchType,
    val contextLine: String? = null,
    val lineNumber: Int? = null,
    val matchStart: Int? = null,
    val matchEnd: Int? = null,
    val matchCount: Int = 1,
    val relevance: Float = 0f
) {
    val displayName: String get() = when (matchType) {
        SearchMatchType.FILE_NAME -> fileName
        SearchMatchType.FOLDER_NAME -> fileName
        SearchMatchType.CONTENT -> "$fileName:${lineNumber ?: 1}"
        SearchMatchType.TAG -> "#${contextLine ?: fileName}"
        SearchMatchType.WIKI_LINK -> "[[${contextLine ?: fileName}]]"
    }

    val snippet: String? get() = when (matchType) {
        SearchMatchType.CONTENT -> contextLine?.trim()
        else -> null
    }
}
