package com.worldonetop.portfolio.di

import android.content.Context
import com.worldonetop.portfolio.util.FileUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FileUtilModule {
    @Singleton
    @Provides
    fun provideFileUtil(
        @ActivityContext context: Context
    ): FileUtil = FileUtil("download/SmartPortfolio/", context)
}
