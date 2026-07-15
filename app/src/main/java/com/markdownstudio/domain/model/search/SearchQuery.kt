package com.markdownstudio.domain.model.search

data class SearchQuery(
    val text: String,
    val matchTypes: Set<SearchMatchType> = SearchMatchType.entries.toSet(),
    val useRegex: Boolean = false,
    val caseSensitive: Boolean = false
) {
    val isValid: Boolean get() = text.isNotBlank()

    fun toRegex(): Regex {
        val pattern = if (useRegex) text else Regex.escape(text)
        val options = if (caseSensitive) setOf<RegexOption>() else setOf(RegexOption.IGNORE_CASE)
        return try {
            Regex(pattern, options)
        } catch (e: Exception) {
            Regex(Regex.escape(text), options)
        }
    }
}
