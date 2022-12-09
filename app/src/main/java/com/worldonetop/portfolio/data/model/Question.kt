package com.worldonetop.portfolio.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Question(
    var question:String,
    var answer:String,
    var like:Boolean = false,
){
    @PrimaryKey(autoGenerate = true) var questionId: Int = 0
}
