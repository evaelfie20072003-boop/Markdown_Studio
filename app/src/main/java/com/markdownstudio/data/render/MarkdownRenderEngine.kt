package com.markdownstudio.data.render

interface MarkdownRenderEngine {
    fun renderToHtml(markdown: String): String
    suspend fun renderToHtmlAsync(markdown: String): String
}
