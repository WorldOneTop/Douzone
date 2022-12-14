package com.worldonetop.portfolio.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import java.util.*

class Converters {
    @TypeConverter
    fun stringListToJson(value: ArrayList<String>?) = Gson().toJson(value)
    @TypeConverter
    fun jsonToStringList(value: String) = Gson().fromJson(value, Array<String>::class.java).toCollection(ArrayList<String>())

    @TypeConverter
    fun intListToJson(value: ArrayList<Int>?) = Gson().toJson(value)
    @TypeConverter
    fun jsonToIntList(value: String) = Gson().fromJson(value, Array<Int>::class.java).toCollection(ArrayList<Int>())

}
