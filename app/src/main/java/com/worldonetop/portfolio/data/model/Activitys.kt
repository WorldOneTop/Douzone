package com.worldonetop.portfolio.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*

@Entity
data class Activitys(
    var title:String, // 활동명
    var content:String?=null, // 상세 내용
    var startDate:String, // 시작 날짜
    var endDate: String?=null, // 종료 날짜
    var type:Int, // 활동 카테고리, activityCategory(strings array file) 참조
    var like:Boolean = false, // 중요표시
    val links:ArrayList<String> = ArrayList(), // 관련 링크
    val files:ArrayList<String> = ArrayList() // 관련 파일
): Serializable {
    @PrimaryKey(autoGenerate = true) var activityId: Int = 0
}
