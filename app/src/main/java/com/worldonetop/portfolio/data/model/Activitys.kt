package com.worldonetop.portfolio.data.model

import android.annotation.SuppressLint
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.worldonetop.portfolio.util.ActivityType
import java.text.SimpleDateFormat
import java.util.*

@Entity
data class Activitys(
    var title:String, // 활동명
    var content:String?, // 상세 내용
    var startDate:Date, // 시작 날짜
    var endDate: Date?, // 종료 날짜
    var type:ActivityType, // 활동 카테고리
    var like:Boolean = false, // 중요표시
    var links:List<String>? = null, // 관련 링크
    var files:List<String>? = null // 관련 파일
){
    @PrimaryKey(autoGenerate = true) var activityId: Int = 0

    companion object{
        @SuppressLint("SimpleDateFormat")
        val dateFormat = SimpleDateFormat("yy.MM.dd")
    }
}
