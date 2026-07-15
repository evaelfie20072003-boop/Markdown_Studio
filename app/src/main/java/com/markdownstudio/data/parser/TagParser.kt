package com.markdownstudio.data.parser

import com.markdownstudio.domain.model.obsidian.Tag

object TagParser {

    private val tagRegex = Regex("(?:^|\\s)#([a-zA-Z0-9/_-]+)")

    fun parse(content: String, sourceUri: String = ""): List<Tag> {
        val tags = mutableListOf<Tag>()
        val seen = mutableSetOf<String>()

        for (match in tagRegex.findAll(content)) {
            val name = match.groupValues[1].trim()
            if (name.isNotBlank() && !seen.contains(name.lowercase())) {
                seen.add(name.lowercase())
                tags.add(
                    Tag(
                        name = name,
                        sourceUri = sourceUri,
                        startOffset = match.range.first + 1,
                        endOffset = match.range.last + 1
                    )
                )
            }
        }
        return tags
    }
}
