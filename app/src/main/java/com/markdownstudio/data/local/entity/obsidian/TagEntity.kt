package com.markdownstudio.data.local.entity.obsidian

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "file_tags",
    primaryKeys = ["sourceUri", "name"],
    indices = [
        Index("sourceUri"),
        Index("name")
    ]
)
data class TagEntity(
    val sourceUri: String,
    val name: String,
    val nameNormalized: String = name.trim().lowercase()
)
