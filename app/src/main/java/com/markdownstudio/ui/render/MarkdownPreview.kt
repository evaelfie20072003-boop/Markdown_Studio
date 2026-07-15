package com.markdownstudio.ui.render

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.markdownstudio.data.render.MarkdownRenderEngineImpl

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MarkdownPreview(
    markdown: String,
    markdownKey: Pair<String, Int>,
    engine: MarkdownRenderEngineImpl,
    externalScrollRatio: Float = 0f,
    isExternalScroll: Boolean = false,
    onScrollChanged: (Float) -> Unit = {},
    onScrollSyncComplete: () -> Unit = {},
    onLinkClicked: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isLoading by remember { mutableStateOf(true) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var templateLoaded by remember { mutableStateOf(false) }

    val currentOnLinkClicked by rememberUpdatedState(onLinkClicked)
    val currentOnScrollChanged by rememberUpdatedState(onScrollChanged)
    val currentOnScrollSyncComplete by rememberUpdatedState(onScrollSyncComplete)

    val htmlTemplate = remember { engine.getHtmlTemplate() }
    val scrollHandler = remember { Handler(Looper.getMainLooper()) }
    var lastReportedRatio by remember { mutableStateOf(-1f) }
    var suppressJsScroll by remember { mutableStateOf(false) }

    // Render content when markdown or epoch changes
    LaunchedEffect(markdownKey) {
        val view = webView
        if (view != null && templateLoaded) {
            val script = "render('${escapeJs(markdown)}')"
            view.evaluateJavascript(script, null)
        } else if (view != null) {
            view.loadDataWithBaseURL(
                "https://markdown-studio.app/",
                htmlTemplate,
                "text/html",
                "UTF-8",
                null
            )
        }
    }

    // Handle external scroll changes
    LaunchedEffect(externalScrollRatio, isExternalScroll) {
        if (isExternalScroll && templateLoaded) {
            suppressJsScroll = true
            val view = webView
            if (view != null) {
                val script = "setScrollRatio($externalScrollRatio)"
                view.evaluateJavascript(script, null)
            }
            scrollHandler.postDelayed({
                suppressJsScroll = false
                currentOnScrollSyncComplete()
            }, 100)
        }
    }

    Box(modifier = modifier) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }

        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = true
                    settings.allowContentAccess = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.builtInZoomControls = false
                    settings.displayZoomControls = false
                    settings.cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE

                    addJavascriptInterface(
                        AndroidScrollBridge(
                            onLink = { url -> currentOnLinkClicked?.invoke(url) },
                            onScroll = { ratio ->
                                if (!suppressJsScroll) {
                                    lastReportedRatio = ratio
                                    currentOnScrollChanged(ratio)
                                }
                            },
                            onImageClick = { url -> currentOnLinkClicked?.invoke(url) }
                        ),
                        "Android"
                    )

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            isLoading = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            templateLoaded = true
                            isLoading = false
                            if (markdown.isNotEmpty()) {
                                val script = "render('${escapeJs(markdown)}')"
                                view?.evaluateJavascript(script, null)
                            }
                        }
                    }

                    loadDataWithBaseURL(
                        "https://markdown-studio.app/",
                        htmlTemplate,
                        "text/html",
                        "UTF-8",
                        null
                    )
                }.also { webView = it }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

private class AndroidScrollBridge(
    private val onLink: (String) -> Unit,
    private val onScroll: (Float) -> Unit,
    private val onImageClick: (String) -> Unit
) {
    @JavascriptInterface
    fun openLink(url: String) = onLink(url)

    @JavascriptInterface
    fun openImage(url: String) = onImageClick(url)

    @JavascriptInterface
    fun onScrollChanged(ratio: Float) = onScroll(ratio)

    @JavascriptInterface
    fun onContentHeightChange(height: Int) {}
}

private fun escapeJs(value: String): String {
    return value
        .replace("\\", "\\\\")
        .replace("'", "\\'")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
}
