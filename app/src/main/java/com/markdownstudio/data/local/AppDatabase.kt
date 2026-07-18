package com.markdownstudio.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

    companion object {
        val MIGRATION_1_2 = Migration(1, 2) { db ->
            db.execSQL("ALTER TABLE recent_files ADD COLUMN parentUri TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE favorite_files ADD COLUMN parentUri TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE wiki_links ADD COLUMN targetNormalized TEXT DEFAULT ''")
            db.execSQL("ALTER TABLE file_tags ADD COLUMN nameNormalized TEXT DEFAULT ''")
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS templates (
                    uri TEXT PRIMARY KEY NOT NULL,
                    name TEXT NOT NULL,
                    content TEXT NOT NULL DEFAULT '',
                    isBuiltIn INTEGER NOT NULL DEFAULT 0,
                    lastModified INTEGER NOT NULL DEFAULT 0
                )
            """)
        }
    }
}
