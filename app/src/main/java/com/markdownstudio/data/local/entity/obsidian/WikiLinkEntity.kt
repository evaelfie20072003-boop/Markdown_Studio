package com.markdownstudio.data.local.entity.obsidian

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "wiki_links",
    primaryKeys = ["sourceUri", "target"],
    indices = [
        Index("sourceUri"),
        Index("target")
    ]
)
data class WikiLinkEntity(
    val sourceUri: String,
    val target: String,
    val displayText: String? = null,
    val targetNormalized: String = target.trim().lowercase().removeSuffix(".md")
)
