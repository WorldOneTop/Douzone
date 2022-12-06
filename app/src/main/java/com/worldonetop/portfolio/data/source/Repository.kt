package com.worldonetop.portfolio.data.source

import androidx.paging.PagingData
import com.worldonetop.portfolio.data.model.Activitys
import com.worldonetop.portfolio.data.model.Portfolio
import com.worldonetop.portfolio.data.model.PortfolioDetail
import com.worldonetop.portfolio.data.model.Question
import kotlinx.coroutines.flow.Flow


interface Repository{

    suspend fun getPortfolio(): Flow<PagingData<Portfolio>> // 포트폴리오 탭 내용

    suspend fun getActivitys():Flow<PagingData<Activitys>>  // 활동 탭 내용

    suspend fun getQuestions():Flow<PagingData<Question>> // 문답 탭 내용

    suspend fun getDetailPortfolio(id: Int):PortfolioDetail // 포트폴리오 자세한 내용

    suspend fun getDetailActivity(id: Int):Activitys // 활동 자세한 내용

    suspend fun addPortfolio(data: Portfolio)

    suspend fun addActivity(data: Activitys)

    suspend fun addQuestion(data: Question)

    suspend fun updatePortfolio(data: Portfolio)

    suspend fun updateActivity(data: Activitys)

    suspend fun updateQuestion(data: Question)

    suspend fun removePortfolio(data: Portfolio)

    suspend fun removeActivity(data: Activitys)

    suspend fun removeQuestion(data: Question)

}