package com.markdownstudio.data.repository

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.markdownstudio.data.local.dao.obsidian.TagDao
import com.markdownstudio.data.local.dao.obsidian.TemplateDao
import com.markdownstudio.data.local.dao.obsidian.WikiLinkDao
import com.markdownstudio.data.local.entity.obsidian.TagEntity
import com.markdownstudio.data.local.entity.obsidian.TemplateEntity
import com.markdownstudio.data.local.entity.obsidian.WikiLinkEntity
import com.markdownstudio.data.parser.TagParser
import com.markdownstudio.data.parser.WikiLinkParser
import com.markdownstudio.domain.model.MarkdownFile
import com.markdownstudio.domain.model.obsidian.Backlink
import com.markdownstudio.domain.model.obsidian.DailyNote
import com.markdownstudio.domain.model.obsidian.LinkEdge
import com.markdownstudio.domain.model.obsidian.LinkGraph
import com.markdownstudio.domain.model.obsidian.LinkNode
import com.markdownstudio.domain.model.obsidian.Tag
import com.markdownstudio.domain.model.obsidian.Template
import com.markdownstudio.domain.model.obsidian.WikiLink
import com.markdownstudio.domain.repository.ObsidianRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObsidianRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wikiLinkDao: WikiLinkDao,
    private val tagDao: TagDao,
    private val templateDao: TemplateDao
) : ObsidianRepository {

    override suspend fun parseAndStoreLinks(uri: String, content: String) {
        val links = WikiLinkParser.parse(content, uri)
        if (links.isNotEmpty()) {
            val entities = links.map { link ->
                WikiLinkEntity(
                    sourceUri = uri,
                    target = link.target,
                    displayText = link.displayText
                )
            }
            wikiLinkDao.deleteBySource(uri)
            wikiLinkDao.insertAll(entities)
        } else {
            wikiLinkDao.deleteBySource(uri)
        }
    }

    override suspend fun parseAndStoreTags(uri: String, content: String) {
        val tags = TagParser.parse(content, uri)
        if (tags.isNotEmpty()) {
            val entities = tags.map { tag ->
                TagEntity(
                    sourceUri = uri,
                    name = tag.name
                )
            }
            tagDao.deleteBySource(uri)
            tagDao.insertAll(entities)
        } else {
            tagDao.deleteBySource(uri)
        }
    }

    override fun getOutgoingLinks(uri: String): List<WikiLink> {
        return runBlocking(Dispatchers.IO) { wikiLinkDao.getOutgoingLinks(uri) }.map { entity ->
            WikiLink(
                target = entity.target,
                displayText = entity.displayText,
                sourceUri = entity.sourceUri
            )
        }
    }

    override fun getBacklinks(uri: String): List<Backlink> {
        val name = uri.substringAfterLast("/").removeSuffix(".md").lowercase()
        val entities = runBlocking(Dispatchers.IO) { wikiLinkDao.getBacklinks(name) }
        return entities.map { entity ->
            Backlink(
                sourceUri = entity.sourceUri,
                sourceName = entity.sourceUri.substringAfterLast("/").removeSuffix(".md"),
                targetUri = uri,
                targetName = name,
                displayText = entity.displayText
            )
        }
    }

    override fun getTags(uri: String): List<Tag> {
        return runBlocking(Dispatchers.IO) { tagDao.getTagsForFile(uri) }.map { entity ->
            Tag(name = entity.name, sourceUri = entity.sourceUri)
        }
    }

    override fun getAllTags(): List<Tag> {
        return runBlocking(Dispatchers.IO) { tagDao.getAllTags() }.map { entity ->
            Tag(name = entity.name, sourceUri = entity.sourceUri)
        }
    }

    override suspend fun removeFileLinks(uri: String) {
        wikiLinkDao.deleteBySource(uri)
    }

    override suspend fun removeFileTags(uri: String) {
        tagDao.deleteBySource(uri)
    }

    override fun getLinkGraph(): LinkGraph {
        val allLinks = runBlocking(Dispatchers.IO) { wikiLinkDao.getAllTargets() }
        val nodes = mutableMapOf<String, LinkNode>()
        val edges = mutableListOf<LinkEdge>()

        for (target in allLinks) {
            val sources = runBlocking(Dispatchers.IO) { wikiLinkDao.getBacklinkSources(target) }
            edges.addAll(sources.map { source ->
                LinkEdge(source = source, target = target)
            })
            nodes.getOrPut(target) {
                LinkNode(uri = "", name = target)
            }.let { node ->
                nodes[target] = node.copy(
                    backlinkUris = sources
                )
            }
            sources.forEach { source ->
                nodes.getOrPut(source) {
                    LinkNode(uri = source, name = source.substringAfterLast("/"))
                }.let { node ->
                    val outgoing = node.outgoingLinks + target
                    nodes[source] = node.copy(outgoingLinks = outgoing)
                }
            }
        }

        return LinkGraph(nodes = nodes, edges = edges)
    }

    override suspend fun rebuildGraph() {
        // Full rebuild - scans all files and re-parses
        // This would be called after initial setup or on demand
    }

    override suspend fun createDailyNote(directoryUri: String): Result<DailyNote> {
        return try {
            val dateStr = DailyNote.today()
            val fileName = "$dateStr.md"
            val dirDoc = DocumentFile.fromTreeUri(context, Uri.parse(directoryUri))
                ?: return Result.failure(Exception("Cannot access directory"))

            val existing = dirDoc.findFile(fileName)
            if (existing != null) {
                return Result.success(
                    DailyNote(
                        date = dateStr,
                        uri = existing.uri.toString(),
                        title = dateStr,
                        exists = true
                    )
                )
            }

            val file = dirDoc.createFile("text/markdown", fileName.removeSuffix(".md"))
                ?: return Result.failure(Exception("Failed to create daily note"))

            val template = getDefaultDailyTemplate(dateStr)
            context.contentResolver.openOutputStream(file.uri)?.use {
                it.write(template.toByteArray())
            }

            Result.success(
                DailyNote(
                    date = dateStr,
                    uri = file.uri.toString(),
                    title = dateStr,
                    exists = true,
                    content = template
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getDailyNote(date: String, directoryUri: String): DailyNote? {
        return try {
            val dirDoc = DocumentFile.fromTreeUri(context, Uri.parse(directoryUri)) ?: return null
            val fileName = "$date.md"
            val file = dirDoc.findFile(fileName) ?: return null
            DailyNote(
                date = date,
                uri = file.uri.toString(),
                title = date,
                exists = true
            )
        } catch (e: Exception) {
            null
        }
    }

    override fun getTemplates(directoryUri: String): List<Template> {
        val templates = mutableListOf<Template>()
        try {
            val dirDoc = DocumentFile.fromTreeUri(context, Uri.parse(directoryUri)) ?: return templates
            val files = dirDoc.listFiles().filter {
                it.isFile && it.name?.endsWith(".md") == true && it.name != null
            }
            templates.addAll(files.map { file ->
                Template(
                    uri = file.uri.toString(),
                    name = file.name!!.removeSuffix(".md"),
                    isBuiltIn = false
                )
            })
        } catch (_: Exception) {}

        val savedTemplates = runBlocking(Dispatchers.IO) {
            runCatching { templateDao.getAllTemplates() }.getOrDefault(emptyList())
        }
        templates.addAll(savedTemplates.filter { t ->
            templates.none { it.uri == t.uri }
        }.map { entity ->
            Template(
                uri = entity.uri,
                name = entity.name,
                content = entity.content,
                isBuiltIn = entity.isBuiltIn
            )
        })

        return templates
    }

    override suspend fun createFromTemplate(
        template: Template,
        targetDirectoryUri: String,
        fileName: String,
        variables: Map<String, String>
    ): Result<MarkdownFile> {
        return try {
            val dirDoc = DocumentFile.fromTreeUri(context, Uri.parse(targetDirectoryUri))
                ?: return Result.failure(Exception("Cannot access directory"))

            val fullName = if (fileName.endsWith(".md")) fileName else "$fileName.md"
            val file = dirDoc.createFile("text/markdown", fullName.removeSuffix(".md"))
                ?: return Result.failure(Exception("Failed to create file"))

            val content = if (template.content.isNotEmpty()) {
                template.apply(variables)
            } else {
                runCatching {
                    context.contentResolver.openInputStream(Uri.parse(template.uri))
                        ?.bufferedReader()?.use { it.readText() } ?: ""
                }.getOrDefault("")
            }

            context.contentResolver.openOutputStream(file.uri)?.use {
                it.write(content.toByteArray())
            }

            Result.success(
                MarkdownFile(
                    uri = file.uri.toString(),
                    name = fullName,
                    size = content.length.toLong(),
                    lastModified = System.currentTimeMillis(),
                    isDirectory = false,
                    parentUri = targetDirectoryUri
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveTemplate(template: Template) {
        templateDao.upsert(
            TemplateEntity(
                uri = template.uri,
                name = template.name,
                content = template.content,
                isBuiltIn = template.isBuiltIn
            )
        )
    }

    override fun searchByTag(tag: String): List<String> {
        return runBlocking(Dispatchers.IO) { tagDao.getFilesByTag(tag.trim().lowercase()) }
    }

    override fun resolveWikiLink(target: String): String? {
        val normalized = target.trim().lowercase().removeSuffix(".md")
        val prefs = context.getSharedPreferences("markdown_studio_prefs", Context.MODE_PRIVATE)
        val rootUri = prefs.getString("root_directory_uri", null) ?: return null

        return try {
            val rootDoc = DocumentFile.fromTreeUri(context, Uri.parse(rootUri)) ?: return null
            resolveInDirectory(rootDoc, normalized)
        } catch (_: Exception) {
            null
        }
    }

    private fun resolveInDirectory(directory: DocumentFile, targetName: String): String? {
        val files = directory.listFiles()
        for (file in files) {
            if (file.isDirectory) {
                resolveInDirectory(file, targetName)?.let { return it }
            } else if (file.isFile) {
                val name = file.name?.removeSuffix(".md")?.lowercase() ?: continue
                if (name == targetName) return file.uri.toString()
            }
        }
        return null
    }

    private fun getDefaultDailyTemplate(date: String): String {
        return """
# $date

## Tasks
- [ ]

## Notes

## Journal

        """.trimIndent()
    }

    companion object {
        private const val PREFS_NAME = "markdown_studio_prefs"
    }
}
