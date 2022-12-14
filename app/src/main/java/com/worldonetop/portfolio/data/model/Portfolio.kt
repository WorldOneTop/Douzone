package com.worldonetop.portfolio.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Portfolio(
    var title:String, // 제목, 파일명
    var content:String?=null, // 상세 내용
    var like:Boolean = false,
    val activity: ArrayList<Int> = ArrayList(), // 각 아이디만 저장, 데이터는 따로 참조
    val question: ArrayList<Int> = ArrayList(), // 각 아이디만 저장, 데이터는 따로 참조
): Serializable {
    @PrimaryKey(autoGenerate = true) var portfolioId: Int = 0
}