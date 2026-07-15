package com.markdownstudio.data.local.dao.obsidian

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.markdownstudio.data.local.entity.obsidian.TemplateEntity

@Dao
interface TemplateDao {

    @Query("SELECT * FROM templates ORDER BY name ASC")
    suspend fun getAllTemplates(): List<TemplateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(template: TemplateEntity)

    @Query("DELETE FROM templates WHERE uri = :uri")
    suspend fun deleteByUri(uri: String)

    @Query("DELETE FROM templates")
    suspend fun clearAll()
}
