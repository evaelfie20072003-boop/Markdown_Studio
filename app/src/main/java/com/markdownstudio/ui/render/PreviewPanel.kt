package com.markdownstudio.ui.render

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.markdownstudio.data.render.MarkdownRenderEngineImpl

@Composable
fun PreviewPanel(
    markdown: String,
    renderEpoch: Int,
    engine: MarkdownRenderEngineImpl,
    scrollRatio: Float,
    isExternalScroll: Boolean,
    onScrollChanged: (Float) -> Unit,
    onScrollSyncComplete: () -> Unit,
    onLinkClicked: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var renderKey by remember(markdown, renderEpoch) {
        mutableStateOf(Pair(markdown, renderEpoch))
    }

    LaunchedEffect(renderKey) {
        onScrollChanged(0f)
    }

    MarkdownPreview(
        markdown = markdown,
        markdownKey = renderKey,
        engine = engine,
        externalScrollRatio = scrollRatio,
        isExternalScroll = isExternalScroll,
        onScrollChanged = onScrollChanged,
        onScrollSyncComplete = onScrollSyncComplete,
        onLinkClicked = onLinkClicked,
        modifier = modifier.fillMaxSize()
    )
}
