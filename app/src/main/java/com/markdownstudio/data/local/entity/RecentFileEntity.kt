package com.markdownstudio.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_files")
data class RecentFileEntity(
    @PrimaryKey
    val uri: String,
    val name: String,
    val lastOpenedAt: Long = System.currentTimeMillis(),
    val parentUri: String? = null
)
