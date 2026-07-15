package com.markdownstudio.domain.model.obsidian

data class LinkGraph(
    val nodes: Map<String, LinkNode> = emptyMap(),
    val edges: List<LinkEdge> = emptyList()
)

data class LinkNode(
    val uri: String,
    val name: String,
    val tags: List<String> = emptyList(),
    val outgoingLinks: List<String> = emptyList(),
    val backlinkUris: List<String> = emptyList()
)

data class LinkEdge(
    val source: String,
    val target: String,
    val displayText: String? = null
)
