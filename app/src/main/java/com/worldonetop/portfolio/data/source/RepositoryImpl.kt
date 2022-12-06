package com.worldonetop.portfolio.data.source

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.worldonetop.portfolio.data.model.Activitys
import com.worldonetop.portfolio.data.model.Portfolio
import com.worldonetop.portfolio.data.model.PortfolioDetail
import com.worldonetop.portfolio.data.model.Question
import com.worldonetop.portfolio.data.source.local.ActivitysDao
import com.worldonetop.portfolio.data.source.local.PortfolioDao
import com.worldonetop.portfolio.data.source.local.QuestionDao
import kotlinx.coroutines.flow.Flow

class RepositoryImpl(private val activitysDao: ActivitysDao,private val portfolioDao: PortfolioDao,private val questionDao: QuestionDao):Repository  {

    override suspend fun getActivitys(): Flow<PagingData<Activitys>> {
        return Pager(
            config = PagingConfig(
                pageSize = 30,
                enablePlaceholders = false,
                maxSize = 100
            )
        ) {activitysDao.getActivitys()}.flow
    }
    override suspend fun getPortfolio(): Flow<PagingData<Portfolio>> {
        return Pager(
            config = PagingConfig(
                pageSize = 30,
                enablePlaceholders = false,
                maxSize = 100
            )
        ) {portfolioDao.getPortfolio()}.flow
    }
    override suspend fun getQuestions(): Flow<PagingData<Question>> {
        return Pager(
            config = PagingConfig(
                pageSize = 30,
                enablePlaceholders = false,
                maxSize = 100
            )
        ) {questionDao.getQuestions()}.flow
    }
    override suspend fun getDetailActivity(id: Int):Activitys {
        return activitysDao.getDetailActivity(id)
    }

    override suspend fun getDetailPortfolio(id: Int):PortfolioDetail {
        val portfolio = portfolioDao.getDetailPortfolio(id)
        return PortfolioDetail(
            portfolio,
            activitysDao.getListActivitys(portfolio.activity),
            questionDao.getListQuestion(portfolio.question),
        )
    }

    override suspend fun addActivity(data: Activitys){
        activitysDao.addActivity(data)
    }
    override suspend fun addPortfolio(data: Portfolio){
        portfolioDao.addPortfolio(data)
    }
    override suspend fun addQuestion(data: Question){
        questionDao.addQuestion(data)
    }

    override suspend fun updateActivity(data: Activitys){
        activitysDao.updateActivity(data)
    }
    override suspend fun updatePortfolio(data: Portfolio){
        portfolioDao.updatePortfolio(data)
    }
    override suspend fun updateQuestion(data: Question){
        questionDao.updateQuestion(data)
    }

    override suspend fun removeActivity(data: Activitys){
        activitysDao.removeActivity(data)
    }
    override suspend fun removePortfolio(data: Portfolio){
        portfolioDao.removePortfolio(data)
    }
    override suspend fun removeQuestion(data: Question){
        questionDao.removeQuestion(data)
    }
}