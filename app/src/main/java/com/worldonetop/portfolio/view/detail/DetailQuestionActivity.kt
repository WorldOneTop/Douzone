package com.worldonetop.portfolio.view.detail

import android.app.Dialog
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import com.worldonetop.portfolio.R
import com.worldonetop.portfolio.base.BaseActivity
import com.worldonetop.portfolio.data.model.Question
import com.worldonetop.portfolio.data.source.Repository
import com.worldonetop.portfolio.databinding.ActivityDetailQuestionBinding
import com.worldonetop.portfolio.util.CustomDialog
import com.worldonetop.portfolio.util.FileUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class DetailQuestionActivity : BaseActivity<ActivityDetailQuestionBinding>(R.layout.activity_detail_question){
    private val viewModel: DetailViewModel by viewModels()
    private lateinit var loadingDialog: Dialog
    @Inject lateinit var repository: Repository
    @Inject lateinit var fileUtil: FileUtil

    override fun initData() {
        // set view model data
        viewModel.initData(2, questionData = intent.getSerializableExtra("data") as? Question)
        binding.vm = viewModel

        loadingDialog = CustomDialog.loading(this)

    }

    override fun initView() {
        // appbar
        setSupportActionBar(binding.detailToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel.editMode.observe(this){
            binding.appbarMenu.text = if(it)
                getString(R.string.save)
            else
                getString(R.string.edit)
        }

    }

    override fun initListener() {
        // appbar
        binding.appbarMenu.setOnClickListener{
            doubleClick.run {
                viewModel.editMode.value?.let { editMode ->
                    if(editMode) {
                        if (formCheck())
                            formSave()
                    }else{
                        viewModel.setEditMode(true)
                    }
                }
            }
        }
    }
    private fun formCheck():Boolean{
        if(viewModel.questionData.answer.isBlank()){
            binding.title.error = getString(R.string.error_no_input)
            return false
        }

        if(viewModel.questionData.question.isBlank()){
            binding.content.error = getString(R.string.error_no_input)
            return false
        }
        return true
    }
    private fun formSave(){
        loadingDialog.show()
        CoroutineScope(Dispatchers.IO).launch{
            try{
                repository.addQuestion(viewModel.questionData)
                withContext(Dispatchers.Main){
                    Toast.makeText(this@DetailQuestionActivity, getString(R.string.save_complete),Toast.LENGTH_SHORT).show()
                    viewModel.setEditMode(false)
                    loadingDialog.dismiss()
                }
            }catch (e: Exception){
                withContext(Dispatchers.Main){
                    this@DetailQuestionActivity.finish()
                    Toast.makeText(applicationContext, getString(R.string.error_save), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        if(intent.getBooleanExtra("slideAin",false))
            overridePendingTransition(R.anim.fadein, R.anim.slide_right)
    }
}