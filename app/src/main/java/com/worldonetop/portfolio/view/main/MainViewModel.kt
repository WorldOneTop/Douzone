package com.worldonetop.portfolio.view.main

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.worldonetop.portfolio.data.model.Activitys
import com.worldonetop.portfolio.data.model.Portfolio
import com.worldonetop.portfolio.data.model.Question
import com.worldonetop.portfolio.data.source.Repository
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class MainViewModel:ViewModel() {
    @Inject lateinit var repository:Repository

    private val _portfolioData: MutableLiveData<PagingData<Portfolio>> = MutableLiveData()
    private val _activitysData: MutableLiveData<PagingData<Activitys>> = MutableLiveData()
    private val _questionData: MutableLiveData<PagingData<Question>> = MutableLiveData()

    lateinit var portfolioData: LiveData<PagingData<Portfolio>>
    lateinit var activitysData: LiveData<PagingData<Activitys>>
    lateinit var questionData: Flow<PagingData<Question>>


    fun getPortffolioData() {
        viewModelScope.launch {
            portfolioData = repository.getPortfolio().cachedIn(viewModelScope).asLiveData()
        }
    }
    fun getQuestionData(){
        viewModelScope.launch {
            questionData = repository.getQuestions().cachedIn(viewModelScope)
        }
    }

}