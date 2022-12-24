package com.worldonetop.portfolio.data.source

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.worldonetop.portfolio.data.model.Activitys
import com.worldonetop.portfolio.data.model.Portfolio
import com.worldonetop.portfolio.data.model.Question
import com.worldonetop.portfolio.data.source.local.AppDatabase

class RepositoryImpl(private val db:AppDatabase):Repository  {
    private val config = PagingConfig(
        initialLoadSize = 20,
        pageSize = 20,
        prefetchDistance = 15,
        maxSize = 100,
        enablePlaceholders = false,
    )

    /** Questions */
    override fun getQuestionAll(): LiveData<PagingData<Question>> {
        return Pager(config) {
            db.questionDao().getQuestionAll()
        }.liveData
    }

    override fun getQuestionQuery(query: String): LiveData<PagingData<Question>> {
        return Pager(config) {
            db.questionDao().getQuestionQuery("%$query%")
        }.liveData
    }
    override fun getQuestionQuery(idList: List<Int>): LiveData<PagingData<Question>> {
        return Pager(config) {
            db.questionDao().getQuestionQuery(idList)
        }.liveData
    }
    override suspend fun getQuestionSelected(idList: List<Int>): List<Question> {
        return db.questionDao().getQuestionSelected(idList)
    }


    override suspend fun addQuestion(data: Question){
        db.questionDao().addQuestion(data)
    }

    override suspend fun updateQuestion(data: Question){
        db.questionDao().updateQuestion(data)
    }

    override suspend fun removeQuestion(idList:List<Int>){
        db.questionDao().removeQuestion(idList)
    }

    /** Portfolios */
    override fun getPortfolioAll(): LiveData<PagingData<Portfolio>> {
        return Pager(config) {
            db.portfolioDao().getPortfolioAll()
        }.liveData
    }

    override fun getPortfolioQuery(query: String): LiveData<PagingData<Portfolio>> {
        return Pager(config) {
            db.portfolioDao().getPortfolioQuery("%$query%")
        }.liveData
    }
    override suspend fun getPortfolioSelected(idList: List<Int>): List<Portfolio> {
        return db.portfolioDao().getPortfolioSelected(idList)
    }

    override suspend fun getLastPortfolioId(): Int {
        return db.portfolioDao().getLastId()
    }

    override suspend fun addPortfolio(data: Portfolio){
        db.portfolioDao().addPortfolio(data)
    }
    override suspend fun updatePortfolio(data: Portfolio){
        db.portfolioDao().updatePortfolio(data)
    }
    override suspend fun removePortfolio(idList:List<Int>){
        db.portfolioDao().removePortfolio(idList)
    }


    /** Activitys */
    override fun getActivitysAll(): LiveData<PagingData<Activitys>> {
        return Pager(config) {
            db.activityDao().getActivitysAll()
        }.liveData
    }
    override fun getActivitysQuery(query: String): LiveData<PagingData<Activitys>> {
        return Pager(config) {
            db.activityDao().getActivitysQuery("%$query%")
        }.liveData
    }
    override fun getActivitysQuery(idList: List<Int>): LiveData<PagingData<Activitys>> {
        return Pager(config) {
            db.activityDao().getActivitysQuery(idList)
        }.liveData
    }
    override suspend fun getActivitysSelected(idList: List<Int>): List<Activitys> {
        return db.activityDao().getActivitysSelected(idList)
    }

    override suspend fun getLastActivitysId(): Int {
        return db.activityDao().getLastId()
    }

    override suspend fun addActivitys(data: Activitys){
        db.activityDao().addActivity(data)
    }

    override suspend fun updateActivitys(data: Activitys){
        db.activityDao().updateActivity(data)
    }
    override suspend fun removeActivitys(idList:List<Int>){
        db.activityDao().removeActivity(idList)
    }
}