package com.worldonetop.portfolio.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Question(
    var question:String,
    var answer:String,
    var like:Boolean = false,
): Serializable {
    @PrimaryKey(autoGenerate = true) var questionId: Int = 0

}
