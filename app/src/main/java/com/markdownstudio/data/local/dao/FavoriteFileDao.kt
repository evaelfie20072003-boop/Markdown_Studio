package com.markdownstudio.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.markdownstudio.data.local.entity.FavoriteFileEntity

@Dao
interface FavoriteFileDao {

    @Query("SELECT * FROM favorite_files ORDER BY addedAt DESC")
    suspend fun getFavoriteFiles(): List<FavoriteFileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(file: FavoriteFileEntity)

    @Query("DELETE FROM favorite_files WHERE uri = :uri")
    suspend fun removeFavorite(uri: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_files WHERE uri = :uri)")
    suspend fun isFavorite(uri: String): Boolean
}
