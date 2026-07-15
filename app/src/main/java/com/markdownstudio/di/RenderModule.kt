package com.markdownstudio.di

import com.markdownstudio.data.render.MarkdownRenderEngine
import com.markdownstudio.data.render.MarkdownRenderEngineImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RenderModule {

    @Binds
    @Singleton
    abstract fun bindMarkdownRenderEngine(
        impl: MarkdownRenderEngineImpl
    ): MarkdownRenderEngine
}
