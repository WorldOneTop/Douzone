package com.worldonetop.portfolio.view.detail

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.worldonetop.portfolio.data.model.Activitys
import com.worldonetop.portfolio.data.model.Portfolio
import com.worldonetop.portfolio.data.model.Question
import com.worldonetop.portfolio.data.source.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(var repository: Repository):ViewModel() {
    lateinit var activityData:Activitys
    lateinit var portfolioData:Portfolio
    lateinit var questionData:Question

    // 바텀 목록 보기로 열어 줄 paging adapter
    lateinit var pagingActivity:LiveData<PagingData<Activitys>>
    lateinit var pagingQuestion:LiveData<PagingData<Question>>

    val removeFiles= ArrayList<String>() // 삭제 해야할 파일 목록

    private val _editMode = MutableLiveData(false)
    val editMode:LiveData<Boolean>
        get() = _editMode

    private lateinit var _startDate: MutableLiveData<String> // 다이얼로그에서 받아와서 view 에 표시하기 위해서
    private lateinit var _endDate:MutableLiveData<String>   // 따로 저장
    val startDate: LiveData<String>
        get() = _startDate
    val endDate:LiveData<String>
        get() = _endDate

    fun initData(
        dataIndex:Int,
        activityData:Activitys?= null,
        portfolioData:Portfolio?= null,
        questionData:Question?= null){
        when(dataIndex){
            0 ->{
                _editMode.value = activityData == null
                this.activityData = activityData ?: Activitys(
                    title="",
                    startDate="",
                    type=-1
                )
                _startDate = MutableLiveData(this.activityData.startDate)
                _endDate = MutableLiveData(this.activityData.endDate)
            }
            1 ->{
                _editMode.value = portfolioData == null
                this.portfolioData = portfolioData ?: Portfolio("")

                pagingActivity = Transformations.switchMap(editMode){
                    if(it)
                        repository.getActivitysAll().cachedIn(viewModelScope)
                    else
                        repository.getActivitysQuery(this.portfolioData.activity).cachedIn(viewModelScope)
                }
                pagingQuestion = Transformations.switchMap(editMode){
                    if(it)
                        repository.getQuestionAll().cachedIn(viewModelScope)
                    else
                        repository.getQuestionQuery(this.portfolioData.question).cachedIn(viewModelScope)
                }
            }
            2 ->{
                _editMode.value = questionData == null
                this.questionData = questionData ?: Question("","")
            }
        }
    }

    fun setEditMode(isEdit:Boolean){
        _editMode.value = isEdit
    }

    fun setStartDate(str:String){
        _startDate.value = str
        activityData.startDate = str
    }
    fun setEndDate(str:String){
        _endDate.value = str
        activityData.endDate = str
    }


}