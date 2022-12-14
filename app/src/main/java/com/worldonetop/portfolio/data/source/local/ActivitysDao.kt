package com.worldonetop.portfolio.data.source.local

import androidx.paging.PagingSource
import androidx.room.*
import com.worldonetop.portfolio.data.model.Activitys

@Dao
interface ActivitysDao {

    @Query("SELECT * FROM Activitys ORDER BY endDate DESC, startDate DESC, activityId ASC")
    fun getActivitysAll(): PagingSource<Int, Activitys>

    @Query("SELECT * FROM Activitys WHERE title LIKE :query  ORDER BY endDate DESC, startDate DESC, activityId ASC")
    fun getActivitysQuery(query: String): PagingSource<Int, Activitys>

    @Query("SELECT * FROM Activitys WHERE activityId IN (:idList)  ORDER BY endDate DESC, startDate DESC, activityId ASC")
    fun getActivitysQuery(idList: List<Int>): PagingSource<Int, Activitys>

    @Query("SELECT * FROM Activitys WHERE activityId IN (:idList)")
    suspend fun getActivitysSelected(idList: List<Int>): List<Activitys>

    @Query("SELECT * FROM Activitys WHERE activityId = :id")
    suspend fun getActivitysId(id:Int):Activitys

    @Query("SELECT activityId FROM Activitys ORDER BY activityId DESC LIMIT 1")
    suspend fun getLastId():Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addActivity(data: Activitys)

    @Update
    suspend fun updateActivity(data: Activitys)

    @Query("DELETE FROM Activitys WHERE activityId IN (:idList)")
    suspend fun removeActivity(idList:List<Int>)

}