package com.markdownstudio.data.local.dao.obsidian

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.markdownstudio.data.local.entity.obsidian.TagEntity

@Dao
interface TagDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tags: List<TagEntity>)

    @Query("SELECT * FROM file_tags WHERE sourceUri = :sourceUri")
    suspend fun getTagsForFile(sourceUri: String): List<TagEntity>

    @Query("SELECT * FROM file_tags")
    suspend fun getAllTags(): List<TagEntity>

    @Query("DELETE FROM file_tags WHERE sourceUri = :sourceUri")
    suspend fun deleteBySource(sourceUri: String)

    @Query("SELECT DISTINCT sourceUri FROM file_tags WHERE nameNormalized = :tagName")
    suspend fun getFilesByTag(tagName: String): List<String>

    @Query("DELETE FROM file_tags")
    suspend fun clearAll()
}
