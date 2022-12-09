package com.worldonetop.portfolio.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Portfolio(
    var title:String, // 제목
    var content:String?, // 상세 내용
    var like:Boolean = false,
    var activity: List<Int>? = null, // 각 아이디만 저장, 데이터는 따로 참조
    var question: List<Int>? = null, // 각 아이디만 저장, 데이터는 따로 참조
) {
    @PrimaryKey(autoGenerate = true) var portfolioId: Int = 0
}