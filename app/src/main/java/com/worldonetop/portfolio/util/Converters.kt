package com.worldonetop.portfolio.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import java.util.*

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time

    @TypeConverter
    fun stringListToJson(value: List<String>?) = Gson().toJson(value)
    @TypeConverter
    fun jsonToStringList(value: String) = Gson().fromJson(value, Array<String>::class.java).toList()

    @TypeConverter
    fun intListToJson(value: List<Int>?) = Gson().toJson(value)
    @TypeConverter
    fun jsonToIntList(value: String) = Gson().fromJson(value, Array<Int>::class.java).toList()

    @TypeConverter
    fun activityTypeToInt(value: ActivityType?) = value?.ordinal
    @TypeConverter
    fun intToactivityType(value: Int) = ActivityType.values().getOrNull(value)
}

enum class ActivityType{
    EXTERNAL, // 대외활동
    PROJECT, // 프로젝트
    CAREER, // 경력
    CERT, // 자격 및 면허
    OTHER, // 기타

}