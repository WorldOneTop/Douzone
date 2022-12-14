package com.worldonetop.portfolio.view.main

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
class MainViewModel @Inject constructor(var repository: Repository) : ViewModel() {
    companion object{
        enum class Type{
            NONE, DELETE, SHARE
        }
    }
    // 검색어
    private val _query: MutableLiveData<String> = MutableLiveData("")
    val query: LiveData<String>
        get() = _query

    val selectMode: MutableLiveData<Boolean> = MutableLiveData(false)

    val eventFloatingBtn: MutableLiveData<Type> = MutableLiveData(Type.NONE)


    // 각 탭의 리스트 데이터
    val portfolioData: LiveData<PagingData<Portfolio>> = Transformations.switchMap(query){
        if(it.isBlank())
            repository.getPortfolioAll().cachedIn(viewModelScope)
        else
            repository.getPortfolioQuery(it).cachedIn(viewModelScope)
    }
    val activitysData: LiveData<PagingData<Activitys>> = Transformations.switchMap(query){
        if(it.isBlank())
            repository.getActivitysAll().cachedIn(viewModelScope)
        else
            repository.getActivitysQuery(it).cachedIn(viewModelScope)
    }
    val questionData: LiveData<PagingData<Question>> = Transformations.switchMap(query){
        if(it.isBlank())
            repository.getQuestionAll().cachedIn(viewModelScope)
        else
            repository.getQuestionQuery(it).cachedIn(viewModelScope)
    }


    fun setQuery(str: String) {
        _query.value = str
    }

}