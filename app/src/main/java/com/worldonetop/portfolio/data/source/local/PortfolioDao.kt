package com.worldonetop.portfolio.data.source.local

import androidx.paging.PagingSource
import androidx.room.*
import com.worldonetop.portfolio.data.model.Portfolio

@Dao
interface PortfolioDao {
    @Query("SELECT * FROM Portfolio")
    fun getPortfolio(): PagingSource<Int, Portfolio>

    @Query("SELECT * FROM Portfolio WHERE portfolioId = :id")
    suspend fun getDetailPortfolio(id:Int):Portfolio

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addPortfolio(data: Portfolio)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updatePortfolio(data: Portfolio)

    @Delete
    suspend fun removePortfolio(data: Portfolio)
}