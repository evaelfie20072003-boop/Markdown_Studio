package com.markdownstudio.ui.editor

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EditorPanel(
    content: String,
    cursorPosition: Int,
    selectionStart: Int?,
    selectionEnd: Int?,
    currentLine: Int,
    wordWrap: Boolean,
    isDark: Boolean,
    fontFamily: FontFamily,
    fontSize: Int,
    lineHeight: Float,
    onContentChanged: (String) -> Unit,
    onCursorChanged: (Int, Int?, Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val syntaxHighlighter = MarkdownSyntaxHighlighter(isDarkTheme = isDark)
    val horizontalScroll = rememberScrollState()

    val textFieldValue = TextFieldValue(
        text = content,
        selection = if (selectionStart != null && selectionEnd != null) {
            TextRange(selectionStart, selectionEnd)
        } else {
            TextRange(cursorPosition)
        }
    )

    Row(
        modifier = modifier
            .fillMaxSize()
            .horizontalScroll(horizontalScroll)
    ) {
        LineNumberColumn(
            lineCount = content.count { it == '\n' } + 1,
            currentLine = currentLine,
            lineHeight = (fontSize * lineHeight).toInt()
        )

        BasicTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                onContentChanged(newValue.text)
                onCursorChanged(
                    position = newValue.selection.start,
                    selectionStart = if (newValue.selection.collapsed) null
                        else newValue.selection.min,
                    selectionEnd = if (newValue.selection.collapsed) null
                        else newValue.selection.max
                )
            },
            visualTransformation = syntaxHighlighter,
            modifier = Modifier
                .fillMaxSize()
                .widthIn(min = if (wordWrap) 0.dp else 600.dp)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            textStyle = TextStyle(
                fontFamily = fontFamily,
                fontSize = fontSize.sp,
                lineHeight = (fontSize * lineHeight).sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box {
                    if (content.isEmpty()) {
                        Text(
                            text = "Start typing markdown...",
                            style = TextStyle(
                                fontFamily = fontFamily,
                                fontSize = fontSize.sp,
                                lineHeight = (fontSize * lineHeight).sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}
