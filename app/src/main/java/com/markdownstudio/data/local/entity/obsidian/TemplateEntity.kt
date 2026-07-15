package com.markdownstudio.data.local.entity.obsidian

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey
    val uri: String,
    val name: String,
    val content: String,
    val isBuiltIn: Boolean = false,
    val lastModified: Long = System.currentTimeMillis()
)
