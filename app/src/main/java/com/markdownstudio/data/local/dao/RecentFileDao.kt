package com.markdownstudio.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.markdownstudio.data.local.entity.RecentFileEntity

@Dao
interface RecentFileDao {

    @Query("SELECT * FROM recent_files ORDER BY lastOpenedAt DESC LIMIT 50")
    suspend fun getRecentFiles(): List<RecentFileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addRecentFile(file: RecentFileEntity)

    @Query("DELETE FROM recent_files WHERE uri = :uri")
    suspend fun removeRecentFile(uri: String)

    @Query("DELETE FROM recent_files")
    suspend fun clearAll()
}
