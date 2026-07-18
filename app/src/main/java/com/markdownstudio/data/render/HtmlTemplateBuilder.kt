package com.markdownstudio.data.render

import android.content.Context

object HtmlTemplateBuilder {

    fun build(context: Context): String {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1.0,maximum-scale=1.0">
<style>
    :root {
        --bg: #1a1a2e;
        --text: #e0e0e0;
        --heading: #ffffff;
        --link: #64b5f6;
        --code-bg: #2d2d44;
        --code-text: #ff8a80;
        --blockquote-border: #64b5f6;
        --blockquote-bg: #1e2740;
        --table-border: #333;
        --table-header-bg: #252542;
        --table-row-alt: #1f1f3a;
        --hr: #333;
        --inline-code-bg: #2d2d44;
        --callout-info-bg: #1a2a3a;
        --callout-info-border: #2196f3;
        --callout-warning-bg: #2a2518;
        --callout-warning-border: #ff9800;
        --callout-danger-bg: #2a1818;
        --callout-danger-border: #f44336;
        --callout-success-bg: #182a18;
        --callout-success-border: #4caf50;
        --callout-tip-bg: #25182a;
        --callout-tip-border: #9c27b0;
        --task-checked: #66bb6a;
        --footnote-text: #999;
        --mermaid-bg: #1e1e32;
    }

    * { box-sizing: border-box; margin: 0; padding: 0; }
    body {
        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;
        font-size: 16px;
        line-height: 1.6;
        color: var(--text);
        background: var(--bg);
        padding: 24px 28px;
        overflow-x: hidden;
        word-wrap: break-word;
        -webkit-font-smoothing: antialiased;
    }

    h1, h2, h3 { font-weight: 700; letter-spacing: -0.02em; color: var(--heading); }
    h1 { font-size: 1.9em; margin: 0.5em 0 0.3em 0; line-height: 1.3; }
    h2 { font-size: 1.5em; margin: 0.6em 0 0.3em 0; line-height: 1.35; }
    h3 { font-size: 1.25em; margin: 0.7em 0 0.3em 0; line-height: 1.4; }
    h4 { font-size: 1.05em; margin: 0.8em 0 0.2em 0; color: var(--heading); font-weight: 600; }
    h5 { font-size: 0.9em; margin: 0.9em 0 0.2em 0; color: var(--heading); font-weight: 600; opacity: 0.75; }
    h6 { font-size: 0.85em; margin: 1em 0 0.2em 0; color: var(--heading); font-weight: 600; opacity: 0.65; }

    p { margin: 0 0 12px 0; }
    p:last-child { margin-bottom: 0; }

    a { color: #3b82f6; text-decoration: none; font-weight: 500; }
    a:hover { text-decoration: underline; opacity: 0.85; }

    ul, ol { margin: 0 0 12px 0; padding-left: 26px; }
    li { margin: 3px 0; line-height: 1.6; }
    li > ul, li > ol { margin-bottom: 0; }

    blockquote {
        margin: 0 0 12px 0;
        padding: 10px 20px;
        border-left: 4px solid var(--blockquote-border);
        background: var(--blockquote-bg);
        color: var(--text);
        border-radius: 0 6px 6px 0;
    }
    blockquote p:last-child { margin-bottom: 0; }

    code {
        font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
        font-size: 0.85em;
        padding: 3px 6px;
        border-radius: 4px;
        background: var(--inline-code-bg);
        color: var(--code-text);
    }

    pre {
        margin: 0 0 12px 0;
        padding: 16px 20px;
        border-radius: 8px;
        background: var(--code-bg);
        overflow-x: auto;
        border: 1px solid rgba(128,128,128,0.1);
    }
    pre code {
        padding: 0;
        background: transparent;
        color: var(--text);
        font-size: 0.85em;
        line-height: 1.6;
    }

    hr {
        border: none;
        height: 1px;
        background: var(--hr);
        margin: 20px 0;
    }

    table {
        width: 100%;
        border-collapse: collapse;
        margin: 0 0 12px 0;
        overflow-x: auto;
        display: block;
        font-size: 0.95em;
    }
    th, td {
        border: 1px solid var(--table-border);
        padding: 8px 14px;
        text-align: left;
    }
    th {
        background: var(--table-header-bg);
        font-weight: 600;
        font-size: 0.9em;
        text-transform: uppercase;
        letter-spacing: 0.04em;
    }
    tr:nth-child(even) td { background: var(--table-row-alt); }

    img {
        max-width: 100%;
        height: auto;
        border-radius: 8px;
        margin: 8px 0;
    }

    /* Task Lists - Notion style */
    ul li.task-list-item {
        list-style: none;
        margin-left: -26px;
        display: flex;
        align-items: flex-start;
        gap: 8px;
    }
    ul li.task-list-item::before { display: none; }
    .task-checkbox {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        width: 18px;
        height: 18px;
        min-width: 18px;
        margin-top: 3px;
        border: 2px solid var(--text);
        border-radius: 3px;
        background: transparent;
        opacity: 0.8;
    }
    .task-checkbox.checked {
        background: var(--task-checked);
        border-color: var(--task-checked);
        position: relative;
    }
    .task-checkbox.checked::after {
        content: '';
        position: absolute;
        left: 4px;
        top: 1px;
        width: 6px;
        height: 10px;
        border: solid white;
        border-width: 0 2px 2px 0;
        transform: rotate(45deg);
    }

    /* Footnotes */
    .footnotes { margin-top: 32px; padding-top: 16px; border-top: 1px solid var(--hr); font-size: 0.875em; color: var(--footnote-text); }
    .footnotes ol { padding-left: 20px; }
    .footnotes li { margin: 4px 0; }
    .footnote-ref { font-size: 0.75em; vertical-align: super; }
    .footnote-backref { margin-left: 4px; font-size: 0.8em; }

    /* Callouts / Admonitions */
    .callout {
        margin: 16px 0;
        padding: 12px 16px;
        border-radius: 8px;
        border-left: 4px solid;
    }
    .callout-title {
        font-weight: 700;
        margin-bottom: 4px;
        text-transform: uppercase;
        font-size: 0.85em;
        letter-spacing: 0.5px;
    }
    .callout p:last-child { margin-bottom: 0; }
    .callout.info { background: var(--callout-info-bg); border-color: var(--callout-info-border); }
    .callout.info .callout-title { color: var(--callout-info-border); }
    .callout.warning { background: var(--callout-warning-bg); border-color: var(--callout-warning-border); }
    .callout.warning .callout-title { color: var(--callout-warning-border); }
    .callout.danger { background: var(--callout-danger-bg); border-color: var(--callout-danger-border); }
    .callout.danger .callout-title { color: var(--callout-danger-border); }
    .callout.success { background: var(--callout-success-bg); border-color: var(--callout-success-border); }
    .callout.success .callout-title { color: var(--callout-success-border); }
    .callout.tip { background: var(--callout-tip-bg); border-color: var(--callout-tip-border); }
    .callout.tip .callout-title { color: var(--callout-tip-border); }

    /* Mermaid */
    .mermaid-container {
        background: var(--mermaid-bg);
        border-radius: 8px;
        padding: 16px;
        margin: 16px 0;
        overflow-x: auto;
        text-align: center;
    }
    .mermaid-container svg { max-width: 100%; height: auto; }

    /* KaTeX */
    .katex { font-size: 1.1em; }
    .katex-display { margin: 16px 0; overflow-x: auto; overflow-y: hidden; }

    /* Embedded HTML */
    .html-block {
        margin: 16px 0;
        padding: 12px;
        border-radius: 6px;
        background: var(--code-bg);
        border: 1px solid var(--table-border);
    }
</style>
</head>
<body>
<div id="content"></div>

<!-- Math: KaTeX -->
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.css">
<script src="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/contrib/auto-render.min.js"></script>

<!-- Markdown: markdown-it + plugins -->
<script src="https://cdn.jsdelivr.net/npm/markdown-it@14.0.0/dist/markdown-it.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/markdown-it-container@4.0.0/dist/markdown-it-container.min.js"></script>

<!-- Diagrams: Mermaid -->
<script src="https://cdn.jsdelivr.net/npm/mermaid@10.9.0/dist/mermaid.min.js"></script>

<script>
    mermaid.initialize({
        startOnLoad: false,
        theme: 'dark',
        securityLevel: 'strict'
    });

    const md = window.markdownit({
        html: true,
        linkify: true,
        typographer: true,
        breaks: true,
        langPrefix: 'language-',
        highlight: function(str, lang) {
            return '<pre><code class="language-' + lang + '">' +
                md.utils.escapeHtml(str) + '</code></pre>';
        }
    })
    .use(markdownitContainer, 'info', {
        render: function(tokens, idx) {
            if (tokens[idx].nesting === 1) return '<div class="callout info"><div class="callout-title">Info</div>';
            return '</div>';
        }
    })
    .use(markdownitContainer, 'warning', {
        render: function(tokens, idx) {
            if (tokens[idx].nesting === 1) return '<div class="callout warning"><div class="callout-title">Warning</div>';
            return '</div>';
        }
    })
    .use(markdownitContainer, 'danger', {
        render: function(tokens, idx) {
            if (tokens[idx].nesting === 1) return '<div class="callout danger"><div class="callout-title">Danger</div>';
            return '</div>';
        }
    })
    .use(markdownitContainer, 'success', {
        render: function(tokens, idx) {
            if (tokens[idx].nesting === 1) return '<div class="callout success"><div class="callout-title">Success</div>';
            return '</div>';
        }
    })
    .use(markdownitContainer, 'tip', {
        render: function(tokens, idx) {
            if (tokens[idx].nesting === 1) return '<div class="callout tip"><div class="callout-title">Tip</div>';
            return '</div>';
        }
    })
    .use(markdownitContainer, 'note', {
        render: function(tokens, idx) {
            if (tokens[idx].nesting === 1) return '<div class="callout info"><div class="callout-title">Note</div>';
            return '</div>';
        }
    });

    // Override image renderer for click handling
    const defaultImageRender = md.renderer.rules.image || function(tokens, idx, options, env, self) {
        return self.renderToken(tokens, idx, options);
    };
    md.renderer.rules.image = function(tokens, idx, options, env, self) {
        const token = tokens[idx];
        const src = token.attrGet('src') || '';
        const alt = token.content || '';
        return '<div style="text-align:center"><img src="' + src + '" alt="' + alt + '" onclick="window.Android.openImage(\'' + src + '\')" /></div>';
    };

    // Override link renderer for handling
    md.renderer.rules.link_open = function(tokens, idx, options, env, self) {
        const token = tokens[idx];
        const href = token.attrGet('href') || '';
        const isExternal = href.startsWith('http://') || href.startsWith('https://');
        if (isExternal) {
            token.attrSet('target', '_blank');
            token.attrSet('rel', 'noopener noreferrer');
            token.attrSet('onclick', "event.preventDefault(); window.Android.openLink('" + href.replace(/'/g, "\\'") + "')");
        }
        return self.renderToken(tokens, idx, options);
    };

    // Task list processing - Notion style checkboxes
    function processTaskLists(html) {
        return html
            .replace(/<li>\\[ \\]\\s*/g, '<li class="task-list-item"><span class="task-checkbox"></span> ')
            .replace(/<li>\\[x\\]\\s*/gi, '<li class="task-list-item"><span class="task-checkbox checked"></span> ')
            .replace(/<li>\\[ \\]/g, '<li class="task-list-item" style="list-style:none"><span class="task-checkbox"></span> ')
            .replace(/<li>\\[x\\]/gi, '<li class="task-list-item" style="list-style:none"><span class="task-checkbox checked"></span> ');
    }

    // Mermaid processing
    async function renderMermaid(html) {
        const temp = document.createElement('div');
        temp.innerHTML = html;
        const mermaidBlocks = temp.querySelectorAll('.language-mermaid');
        for (const block of mermaidBlocks) {
            const code = block.textContent;
            const container = document.createElement('div');
            container.className = 'mermaid-container';
            const mermaidDiv = document.createElement('div');
            mermaidDiv.className = 'mermaid';
            mermaidDiv.textContent = code;
            container.appendChild(mermaidDiv);
            block.parentElement.replaceWith(container);
        }
        try {
            await mermaid.run({ nodes: temp.querySelectorAll('.mermaid') });
        } catch(e) {}
        return temp.innerHTML;
    }

    // KaTeX rendering
    function renderMath(html) {
        if (typeof renderMathInElement === 'function') {
            const temp = document.createElement('div');
            temp.innerHTML = html;
            try {
                renderMathInElement(temp, {
                    delimiters: [
                        {left: '$$', right: '$$', display: true},
                        {left: '\\(', right: '\\)', display: false},
                        {left: '\\[', right: '\\]', display: true},
                        {left: '$', right: '$', display: false}
                    ],
                    throwOnError: false
                });
            } catch(e) {}
            return temp.innerHTML;
        }
        return html;
    }

    // Main render function
    async function render(markdown) {
        try {
            // Pre-process: preserve mermaid blocks
            const mermaidRegex = /```\\s*mermaid\\s*\\n([\\s\\S]*?)\\n```/g;
            const mermaidPlaceholders = [];
            let processedMD = markdown.replace(mermaidRegex, function(match, code) {
                const idx = mermaidPlaceholders.length;
                mermaidPlaceholders.push(code.trim());
                return '```mermaid\\n' + code.trim() + '\\n```';
            });

            let html = md.render(processedMD);

            // Process task lists
            html = processTaskLists(html);

            // Process math
            html = renderMath(html);

            // Process mermaid
            html = await renderMermaid(html);

            document.getElementById('content').innerHTML = html;

            const height = document.body.scrollHeight;
            window.Android.onContentHeightChange(height);
        } catch(e) {
            document.getElementById('content').innerHTML = '<p style="color:red">Render error: ' + e.message + '</p>';
        }
    }

    // Scroll reporting
    let scrollReportInterval = null;
    function startScrollReporting() {
        if (scrollReportInterval) clearInterval(scrollReportInterval);
        scrollReportInterval = setInterval(function() {
            const maxScroll = document.documentElement.scrollHeight - document.documentElement.clientHeight;
            if (maxScroll > 0) {
                const ratio = window.scrollY / maxScroll;
                window.Android.onScrollChanged(Math.round(ratio * 1000) / 1000);
            } else {
                window.Android.onScrollChanged(0);
            }
        }, 100);
    }

    // Programmatic scroll
    function setScrollRatio(ratio) {
        const maxScroll = document.documentElement.scrollHeight - document.documentElement.clientHeight;
        if (maxScroll > 0) {
            window.scrollTo(0, ratio * maxScroll);
        }
    }

    // Observer to re-report on resize
    if (window.ResizeObserver) {
        const observer = new ResizeObserver(function() {
            const maxScroll = document.documentElement.scrollHeight - document.documentElement.clientHeight;
            if (maxScroll > 0) {
                const ratio = window.scrollY / maxScroll;
                window.Android.onScrollChanged(Math.round(ratio * 1000) / 1000);
            }
        });
        observer.observe(document.body);
    }

    // Start scroll reporting after render
    const origRender = render;
    render = async function(markdown) {
        await origRender(markdown);
        if (scrollReportInterval) clearInterval(scrollReportInterval);
        startScrollReporting();
    };

    // Initial scroll reporting
    window.addEventListener('load', startScrollReporting);
</script>
</body>
</html>
        """.trimIndent()
    }
}
