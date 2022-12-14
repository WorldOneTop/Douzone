package com.worldonetop.portfolio.di

import android.content.Context
import androidx.room.Room
import com.worldonetop.portfolio.data.source.Repository
import com.worldonetop.portfolio.data.source.RepositoryImpl
import com.worldonetop.portfolio.data.source.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Singleton
    @Provides
    fun provideRepository(
        db: AppDatabase,
    ): Repository = RepositoryImpl(db)
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Singleton
    @Provides
    fun provideDataBase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "smart_portfolio.db"
        ).build()
    }
}