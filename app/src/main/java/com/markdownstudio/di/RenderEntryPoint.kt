package com.markdownstudio.di

import com.markdownstudio.data.render.MarkdownRenderEngineImpl
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface RenderEntryPoint {
    fun markdownRenderEngine(): MarkdownRenderEngineImpl
}
