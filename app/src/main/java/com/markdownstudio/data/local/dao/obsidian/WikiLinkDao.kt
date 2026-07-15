package com.markdownstudio.data.local.dao.obsidian

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.markdownstudio.data.local.entity.obsidian.WikiLinkEntity

@Dao
interface WikiLinkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(links: List<WikiLinkEntity>)

    @Query("SELECT * FROM wiki_links WHERE sourceUri = :sourceUri")
    suspend fun getOutgoingLinks(sourceUri: String): List<WikiLinkEntity>

    @Query("SELECT * FROM wiki_links WHERE targetNormalized = :normalizedTarget")
    suspend fun getBacklinks(normalizedTarget: String): List<WikiLinkEntity>

    @Query("DELETE FROM wiki_links WHERE sourceUri = :sourceUri")
    suspend fun deleteBySource(sourceUri: String)

    @Query("SELECT DISTINCT target FROM wiki_links")
    suspend fun getAllTargets(): List<String>

    @Query("SELECT DISTINCT sourceUri FROM wiki_links WHERE targetNormalized = :normalizedTarget")
    suspend fun getBacklinkSources(normalizedTarget: String): List<String>

    @Query("DELETE FROM wiki_links")
    suspend fun clearAll()
}
