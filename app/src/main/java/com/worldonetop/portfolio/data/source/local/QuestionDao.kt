package com.worldonetop.portfolio.data.source.local

import androidx.paging.PagingSource
import androidx.room.*
import com.worldonetop.portfolio.data.model.Question

@Dao
interface QuestionDao {

    @Query("SELECT * FROM Question ORDER BY questionId DESC")
    fun getQuestionAll(): PagingSource<Int, Question>

    @Query("SELECT * FROM Question WHERE answer LIKE :query ORDER BY questionId DESC")
    fun getQuestionQuery(query: String): PagingSource<Int, Question>

    @Query("SELECT * FROM Question WHERE questionId IN (:idList)  ORDER BY questionId DESC")
    fun getQuestionSelected(idList: List<Int>): PagingSource<Int, Question>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addQuestion(data: Question)

    @Update
    suspend fun updateQuestion(data: Question)

    @Query("DELETE FROM Question WHERE questionId IN (:idList)")
    suspend fun removeQuestion(idList:List<Int>)
}