package com.markdownstudio.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.markdownstudio.data.local.dao.FavoriteFileDao
import com.markdownstudio.data.local.dao.RecentFileDao
import com.markdownstudio.data.local.dao.obsidian.TagDao
import com.markdownstudio.data.local.dao.obsidian.TemplateDao
import com.markdownstudio.data.local.dao.obsidian.WikiLinkDao
import com.markdownstudio.data.local.entity.FavoriteFileEntity
import com.markdownstudio.data.local.entity.RecentFileEntity
import com.markdownstudio.data.local.entity.obsidian.TagEntity
import com.markdownstudio.data.local.entity.obsidian.TemplateEntity
import com.markdownstudio.data.local.entity.obsidian.WikiLinkEntity

@Database(
    entities = [
        RecentFileEntity::class,
        FavoriteFileEntity::class,
        WikiLinkEntity::class,
        TagEntity::class,
        TemplateEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recentFileDao(): RecentFileDao
    abstract fun favoriteFileDao(): FavoriteFileDao
    abstract fun wikiLinkDao(): WikiLinkDao
    abstract fun tagDao(): TagDao
    abstract fun templateDao(): TemplateDao
}
