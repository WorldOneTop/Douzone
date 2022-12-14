package com.worldonetop.portfolio.data.source.local

import androidx.paging.PagingSource
import androidx.room.*
import com.worldonetop.portfolio.data.model.Portfolio

@Dao
interface PortfolioDao {

    @Query("SELECT * FROM Portfolio ORDER BY portfolioId DESC")
    fun getPortfolioAll(): PagingSource<Int, Portfolio>

    @Query("SELECT * FROM Portfolio WHERE title LIKE :query ORDER BY portfolioId DESC")
    fun getPortfolioQuery(query: String): PagingSource<Int, Portfolio>

    @Query("SELECT * FROM Portfolio WHERE portfolioId IN (:idList)")
    suspend fun getPortfolioSelected(idList: List<Int>): List<Portfolio>

    @Query("SELECT * FROM Portfolio WHERE portfolioId = :id")
    suspend fun getPortfolioId(id:Int):Portfolio

    @Query("SELECT portfolioId FROM Portfolio ORDER BY portfolioId DESC LIMIT 1")
    suspend fun getLastId(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addPortfolio(data: Portfolio)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updatePortfolio(data: Portfolio)

    @Query("DELETE FROM Portfolio WHERE portfolioId IN (:idList)")
    suspend fun removePortfolio(idList:List<Int>)
}