package com.markdownstudio.di

import com.markdownstudio.data.repository.FileRepositoryImpl
import com.markdownstudio.data.repository.ObsidianRepositoryImpl
import com.markdownstudio.data.search.GlobalSearchRepositoryImpl
import com.markdownstudio.data.repository.BackupRepositoryImpl
import com.markdownstudio.data.repository.SettingsRepositoryImpl
import com.markdownstudio.domain.repository.BackupRepository
import com.markdownstudio.domain.repository.FileRepository
import com.markdownstudio.domain.repository.GlobalSearchRepository
import com.markdownstudio.domain.repository.ObsidianRepository
import com.markdownstudio.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindFileRepository(
        impl: FileRepositoryImpl
    ): FileRepository

    @Binds
    @Singleton
    abstract fun bindObsidianRepository(
        impl: ObsidianRepositoryImpl
    ): ObsidianRepository

    @Binds
    @Singleton
    abstract fun bindGlobalSearchRepository(
        impl: GlobalSearchRepositoryImpl
    ): GlobalSearchRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindBackupRepository(
        impl: BackupRepositoryImpl
    ): BackupRepository
}
