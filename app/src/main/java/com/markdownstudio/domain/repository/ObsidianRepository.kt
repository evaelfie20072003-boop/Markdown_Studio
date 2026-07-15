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
    fun getOutgoingLinks(uri: String): List<WikiLink>
    fun getBacklinks(uri: String): List<Backlink>
    fun getTags(uri: String): List<Tag>
    fun getAllTags(): List<Tag>
    suspend fun removeFileLinks(uri: String)
    suspend fun removeFileTags(uri: String)
    fun getLinkGraph(): LinkGraph
    suspend fun rebuildGraph()
    suspend fun createDailyNote(directoryUri: String): Result<DailyNote>
    fun getDailyNote(date: String, directoryUri: String): DailyNote?
    fun getTemplates(directoryUri: String): List<Template>
    suspend fun createFromTemplate(
        template: Template,
        targetDirectoryUri: String,
        fileName: String,
        variables: Map<String, String> = emptyMap()
    ): Result<com.markdownstudio.domain.model.MarkdownFile>
    suspend fun saveTemplate(template: Template)
    fun searchByTag(tag: String): List<String>
    fun resolveWikiLink(target: String): String?
}
