package com.markdownstudio.ui.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class MarkdownSyntaxHighlighter(
    private val isDarkTheme: Boolean = false
) : VisualTransformation {

    private val headingColor = if (isDarkTheme) Color(0xFF82AAFF) else Color(0xFF1565C0)
    private val boldColor = if (isDarkTheme) Color(0xFFC792EA) else Color(0xFF7B1FA2)
    private val italicColor = if (isDarkTheme) Color(0xFFC3E88D) else Color(0xFF388E3C)
    private val codeColor = if (isDarkTheme) Color(0xFFF78C6C) else Color(0xFFE65100)
    private val linkColor = if (isDarkTheme) Color(0xFF80CBC4) else Color(0xFF00695C)
    private val quoteColor = if (isDarkTheme) Color(0xFF546E7A) else Color(0xFF78909C)
    private val listColor = if (isDarkTheme) Color(0xFFF07178) else Color(0xFFD32F2F)
    private val hrColor = if (isDarkTheme) Color(0xFF444444) else Color(0xFFBDBDBD)
    private val plainTextColor = if (isDarkTheme) Color(0xFFEEFFFF) else Color(0xFF212121)

    private val codeBlockBackground = Color(0x33263238)

    private val braceSeq = "***" to "___"

    override fun filter(text: AnnotatedString): TransformedText {
        val content = text.text
        val builder = AnnotatedString.Builder(content)
        builder.setSpanStyle(SpanStyle(color = plainTextColor), 0, content.length)

        val lines = content.split('\n')
        var globalStart = 0
        var inCodeBlock = false

        for (line in lines) {
            val lineEnd = globalStart + line.length
            val trimmed = line.trimStart()
            val indent = line.length - trimmed.length

            if (trimmed.startsWith("```")) {
                inCodeBlock = !inCodeBlock
                builder.setSpanStyle(SpanStyle(color = codeColor), lineStart = globalStart, lineStart + minOf(3, line.length))
            } else if (inCodeBlock) {
                builder.setSpanStyle(SpanStyle(color = codeColor, background = codeBlockBackground), globalStart, lineEnd)
            } else {
                applyBlockStyles(builder, trimmed, indent, globalStart, lineEnd)
                codeSpanRegex.findAll(line).forEach { match ->
                    val start = globalStart + match.range.first
                    val end = globalStart + match.range.last + 1
                    builder.setSpanStyle(SpanStyle(color = codeColor, background = codeBlockBackground), start, end)
                }
                boldRegex.findAll(line).forEach { match ->
                    val g = match.groupValues
                    buildBoldStyle(builder, globalStart, match.range, g[1].length)
                }
                italicRegex.findAll(line).forEach { match ->
                    val start = globalStart + match.range.first
                    val end = globalStart + match.range.last + 1
                    builder.setSpanStyle(SpanStyle(color = italicColor, fontStyle = FontStyle.Italic), start, end)
                }
                linkRegex.findAll(line).forEach { match ->
                    buildLinkStyle(builder, globalStart, match)
                }
            }
            globalStart += line.length + 1
        }

        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }

    private fun applyBlockStyles(
        builder: AnnotatedString.Builder,
        trimmed: String,
        indent: Int,
        lineStart: Int,
        lineEnd: Int
    ) {
        when {
            trimmed.startsWith("######") -> setHeading(builder, indent, 6, lineStart, lineEnd)
            trimmed.startsWith("#####") -> setHeading(builder, indent, 5, lineStart, lineEnd)
            trimmed.startsWith("####") -> setHeading(builder, indent, 4, lineStart, lineEnd)
            trimmed.startsWith("###") -> setHeading(builder, indent, 3, lineStart, lineEnd)
            trimmed.startsWith("##") -> setHeading(builder, indent, 2, lineStart, lineEnd)
            trimmed.startsWith("# ") || trimmed.startsWith("#\t") -> setHeading(builder, indent, 1, lineStart, lineEnd)
            trimmed.startsWith(">") -> {
                builder.setSpanStyle(SpanStyle(color = quoteColor, fontStyle = FontStyle.Italic), lineStart + indent, lineStart + indent + 1)
                builder.setSpanStyle(SpanStyle(color = quoteColor), lineStart + indent + 1, lineEnd)
            }
            trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("+ ") -> {
                builder.setSpanStyle(SpanStyle(color = listColor, fontWeight = FontWeight.Bold), lineStart + indent, lineStart + indent + 1)
            }
            trimmed.matches(orderedListRegex) -> {
                val dotIndex = trimmed.indexOf('.')
                builder.setSpanStyle(SpanStyle(color = listColor, fontWeight = FontWeight.Bold), lineStart + indent, lineStart + indent + dotIndex + 1)
            }
            hrRegex.matches(trimmed) -> {
                builder.setSpanStyle(SpanStyle(color = hrColor), lineStart, lineEnd)
            }
        }
    }

    private fun setHeading(builder: AnnotatedString.Builder, indent: Int, level: Int, lineStart: Int, lineEnd: Int) {
        val hashEnd = lineStart + indent + level
        builder.setSpanStyle(SpanStyle(color = headingColor, fontWeight = FontWeight.Bold), lineStart, hashEnd.coerceAtMost(lineEnd))
        builder.setSpanStyle(SpanStyle(color = headingColor, fontWeight = if (level <= 3) FontWeight.Bold else FontWeight.SemiBold), lineStart + indent, lineEnd)
    }

    private fun buildBoldStyle(builder: AnnotatedString.Builder, offset: Int, range: IntRange, delimiterLen: Int) {
        val start = offset + range.first
        val end = offset + range.last + 1
        val contentStart = start + delimiterLen
        val contentEnd = end - delimiterLen
        builder.setSpanStyle(SpanStyle(color = boldColor), start, contentStart)
        builder.setSpanStyle(SpanStyle(color = boldColor, fontWeight = FontWeight.Bold), contentStart, contentEnd)
        builder.setSpanStyle(SpanStyle(color = boldColor), contentEnd, end)
    }

    private fun buildLinkStyle(builder: AnnotatedString.Builder, offset: Int, match: MatchResult) {
        val fullStart = offset + match.range.first
        val fullEnd = offset + match.range.last + 1
        val textStart = offset + match.groups[1]!!.range.first
        val textEnd = offset + match.groups[1]!!.range.last + 1
        val urlStart = offset + match.groups[2]!!.range.first
        val urlEnd = offset + match.groups[2]!!.range.last + 1

        builder.setSpanStyle(SpanStyle(color = linkColor), fullStart, textStart)
        builder.setSpanStyle(SpanStyle(color = linkColor, fontWeight = FontWeight.Medium), textStart, textEnd)
        builder.setSpanStyle(SpanStyle(color = linkColor), textEnd, urlStart)
        builder.setSpanStyle(SpanStyle(color = linkColor, fontStyle = FontStyle.Italic), urlStart, urlEnd)
        builder.setSpanStyle(SpanStyle(color = linkColor), urlEnd, fullEnd)
    }

    companion object {
        private val codeSpanRegex = Regex("`([^`]+)`")
        private val boldRegex = Regex("(\\*\\*|__)(.+?)\\1")
        private val italicRegex = Regex("(?<![\\*])(\\*|_)(?![\\*\\s])(.+?)(?<![\\*\\s])\\1(?![\\*])")
        private val linkRegex = Regex("\\[([^\\]]+)\\]\\(([^)]+)\\)")
        private val orderedListRegex = Regex("^\\d+\\.\\s.*")
        private val hrRegex = Regex("^(-{3,}|\\*{3,}|_{3,})\\s*$")
    }
}
