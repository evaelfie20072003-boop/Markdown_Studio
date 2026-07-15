package com.markdownstudio.data.parser

import com.markdownstudio.domain.model.obsidian.WikiLink

object WikiLinkParser {

    private val wikiLinkRegex = Regex("\\[\\[([^\\[\\]|]+?)(?:\\|([^\\[\\]]+?))?\\]\\]")

    fun parse(content: String, sourceUri: String = ""): List<WikiLink> {
        val links = mutableListOf<WikiLink>()
        for (match in wikiLinkRegex.findAll(content)) {
            val target = match.groupValues[1].trim()
            val display = match.groupValues.getOrNull(2)?.trim()?.ifEmpty { null }
            if (target.isNotBlank()) {
                links.add(
                    WikiLink(
                        target = target,
                        displayText = display,
                        sourceUri = sourceUri,
                        startOffset = match.range.first,
                        endOffset = match.range.last + 1
                    )
                )
            }
        }
        return links
    }

    fun renderWikiLink(link: WikiLink): String {
        return if (link.displayText != null) {
            "[[${link.target}|${link.displayText}]]"
        } else {
            "[[${link.target}]]"
        }
    }

    fun isWikiLink(text: String): Boolean {
        return wikiLinkRegex.matches(text)
    }

    fun extractTarget(text: String): String? {
        val match = wikiLinkRegex.find(text) ?: return null
        return match.groupValues[1].trim()
    }
}
