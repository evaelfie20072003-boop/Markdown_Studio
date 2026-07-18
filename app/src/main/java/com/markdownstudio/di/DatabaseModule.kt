package com.markdownstudio.di

import android.content.Context
import androidx.room.Room
import com.markdownstudio.data.local.AppDatabase
import com.markdownstudio.data.local.dao.FavoriteFileDao
import com.markdownstudio.data.local.dao.RecentFileDao
import com.markdownstudio.data.local.dao.obsidian.TagDao
import com.markdownstudio.data.local.dao.obsidian.TemplateDao
import com.markdownstudio.data.local.dao.obsidian.WikiLinkDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "markdown_studio.db"
        ).addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
    }

    @Provides
    @Singleton
    fun provideRecentFileDao(
        database: AppDatabase
    ): RecentFileDao = database.recentFileDao()

    @Provides
    @Singleton
    fun provideFavoriteFileDao(
        database: AppDatabase
    ): FavoriteFileDao = database.favoriteFileDao()

    @Provides
    @Singleton
    fun provideWikiLinkDao(
        database: AppDatabase
    ): WikiLinkDao = database.wikiLinkDao()

    @Provides
    @Singleton
    fun provideTagDao(
        database: AppDatabase
    ): TagDao = database.tagDao()

    @Provides
    @Singleton
    fun provideTemplateDao(
        database: AppDatabase
    ): TemplateDao = database.templateDao()
}
