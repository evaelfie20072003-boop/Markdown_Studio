package com.markdownstudio.data.render

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarkdownRenderEngineImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : MarkdownRenderEngine {

    val htmlTemplate: String by lazy {
        HtmlTemplateBuilder.build(context)
    }

    override fun renderToHtml(markdown: String): String {
        val escaped = markdown
            .replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\n", "\\n")
            .replace("\r", "")
        return htmlTemplate.replace(
            "document.getElementById('content').innerHTML = '';",
            "render('$escaped');"
        )
    }

    override suspend fun renderToHtmlAsync(markdown: String): String {
        return renderToHtml(markdown)
    }

    fun buildRenderScript(markdown: String): String {
        val escaped = markdown
            .replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\n", "\\n")
            .replace("\r", "")
        return "render('$escaped')"
    }
}
