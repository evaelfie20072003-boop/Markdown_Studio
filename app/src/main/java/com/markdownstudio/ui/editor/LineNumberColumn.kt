package com.markdownstudio.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LineNumberColumn(
    lineCount: Int,
    currentLine: Int,
    lineHeight: Int,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val lineHeightPx = with(androidx.compose.ui.platform.LocalDensity.current) { lineHeight.toDp().toPx() }

    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    val highlightColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    val bgColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

    Canvas(
        modifier = modifier
            .width(48.dp)
            .fillMaxHeight()
            .background(bgColor)
    ) {
        val startY = 2.dp.toPx()
        val rightPadding = 8.dp.toPx()
        val fontSizePx = 13.sp.toPx()

        for (lineNum in 1..lineCount) {
            val y = startY + (lineNum - 1) * lineHeightPx

            if (lineNum == currentLine) {
                drawRect(
                    color = highlightColor,
                    topLeft = Offset.Zero,
                    size = Size(size.width, lineHeightPx)
                )
            }

            val text = lineNum.toString()
            val result = textMeasurer.measure(
                text = text,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    textAlign = TextAlign.End,
                    color = if (lineNum == currentLine) activeColor else inactiveColor
                )
            )
            drawText(
                textLayoutResult = result,
                topLeft = Offset(
                    size.width - result.size.width - rightPadding,
                    y + (lineHeightPx - fontSizePx) / 2
                )
            )
        }
    }
}
