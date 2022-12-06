package com.worldonetop.portfolio.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.worldonetop.portfolio.data.model.Activitys
import com.worldonetop.portfolio.data.model.Portfolio
import com.worldonetop.portfolio.data.model.Question
import com.worldonetop.portfolio.util.Converters

@Database(entities = [Activitys::class, Portfolio::class, Question::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivitysDao
    abstract fun portfolioDao(): PortfolioDao
    abstract fun questionDao(): QuestionDao
}