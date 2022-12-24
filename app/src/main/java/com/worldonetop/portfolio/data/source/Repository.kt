package com.worldonetop.portfolio.data.source

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import com.worldonetop.portfolio.data.model.Activitys
import com.worldonetop.portfolio.data.model.Portfolio
import com.worldonetop.portfolio.data.model.Question


interface Repository{



    // 문답 관련
    fun getQuestionAll(): LiveData<PagingData<Question>> // 문답 리스트 전체 반환
    fun getQuestionQuery(query: String): LiveData<PagingData<Question>> // 문답 검색에 따른 리스트 반환
    fun getQuestionQuery(idList: List<Int>): LiveData<PagingData<Question>> // 아이디에 따른 리스트 반환
    suspend fun getQuestionSelected(idList: List<Int>): List<Question>
    suspend fun addQuestion(data: Question)
    suspend fun updateQuestion(data: Question)
    suspend fun removeQuestion(idList:List<Int>)

    // 이력서 관련
    fun getPortfolioAll(): LiveData<PagingData<Portfolio>>
    fun getPortfolioQuery(query: String): LiveData<PagingData<Portfolio>>
    suspend fun getPortfolioSelected(idList: List<Int>): List<Portfolio>
    suspend fun getLastPortfolioId():Int
    suspend fun addPortfolio(data: Portfolio)
    suspend fun updatePortfolio(data: Portfolio)
    suspend fun removePortfolio(idList:List<Int>)


    fun getActivitysAll(): LiveData<PagingData<Activitys>>
    fun getActivitysQuery(query: String): LiveData<PagingData<Activitys>>
    fun getActivitysQuery(idList: List<Int>): LiveData<PagingData<Activitys>>
    suspend fun getLastActivitysId():Int
    suspend fun addActivitys(data: Activitys)
    suspend fun updateActivitys(data: Activitys)
    suspend fun removeActivitys(idList:List<Int>)
    suspend fun getActivitysSelected(idList: List<Int>): List<Activitys>






}