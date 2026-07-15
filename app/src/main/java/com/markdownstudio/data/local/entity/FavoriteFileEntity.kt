package com.markdownstudio.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_files")
data class FavoriteFileEntity(
    @PrimaryKey
    val uri: String,
    val name: String,
    val parentUri: String? = null,
    val addedAt: Long = System.currentTimeMillis()
)
