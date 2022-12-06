package com.worldonetop.portfolio.data.source.local

import androidx.paging.PagingSource
import androidx.room.*
import com.worldonetop.portfolio.data.model.Question

@Dao
interface QuestionDao {
    @Query("SELECT * FROM Question")
    fun getQuestions(): PagingSource<Int, Question>

    @Query("SELECT * FROM Question WHERE questionId IN(:id)")
    suspend fun getListQuestion(id: List<Int>?): List<Question>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addQuestion(data: Question)

    @Update
    suspend fun updateQuestion(data: Question)

    @Delete
    suspend fun removeQuestion(data: Question)
}