package com.worldonetop.portfolio.data.source.local

import androidx.paging.PagingSource
import androidx.room.*
import com.worldonetop.portfolio.data.model.Activitys

@Dao
interface ActivitysDao {
    @Query("SELECT * FROM Activitys ORDER BY startDate DESC")
    fun getActivitys(): PagingSource<Int, Activitys>

    @Query("SELECT * FROM Activitys WHERE activityId IN(:id) ORDER BY startDate DESC")
    suspend fun getListActivitys(id: List<Int>?): List<Activitys>

    @Query("SELECT * FROM Activitys WHERE activityId = :id")
    suspend fun getDetailActivity(id:Int):Activitys

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addActivity(data: Activitys)

    @Update
    suspend fun updateActivity(data: Activitys)

    @Delete
    suspend fun removeActivity(data: Activitys)

}