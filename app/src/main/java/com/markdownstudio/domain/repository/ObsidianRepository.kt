package com.markdownstudio.domain.repository

import com.markdownstudio.domain.model.obsidian.Backlink
import com.markdownstudio.domain.model.obsidian.DailyNote
import com.markdownstudio.domain.model.obsidian.LinkGraph
import com.markdownstudio.domain.model.obsidian.Tag
import com.markdownstudio.domain.model.obsidian.Template
import com.markdownstudio.domain.model.obsidian.WikiLink

interface ObsidianRepository {
    suspend fun parseAndStoreLinks(uri: String, content: String)
    suspend fun parseAndStoreTags(uri: String, content: String)
    suspend fun getOutgoingLinks(uri: String): List<WikiLink>
    suspend fun getBacklinks(uri: String): List<Backlink>
    suspend fun getTags(uri: String): List<Tag>
    suspend fun getAllTags(): List<Tag>
    suspend fun removeFileLinks(uri: String)
    suspend fun removeFileTags(uri: String)
    suspend fun getLinkGraph(): LinkGraph
    suspend fun rebuildGraph()
    suspend fun createDailyNote(directoryUri: String): Result<DailyNote>
    suspend fun getDailyNote(date: String, directoryUri: String): DailyNote?
    suspend fun getTemplates(directoryUri: String): List<Template>
    suspend fun createFromTemplate(
        template: Template,
        targetDirectoryUri: String,
        fileName: String,
        variables: Map<String, String> = emptyMap()
    ): Result<com.markdownstudio.domain.model.MarkdownFile>
    suspend fun saveTemplate(template: Template)
    suspend fun searchByTag(tag: String): List<String>
    suspend fun resolveWikiLink(target: String): String?
}
