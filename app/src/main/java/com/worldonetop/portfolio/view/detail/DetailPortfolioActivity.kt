package com.worldonetop.portfolio.view.detail

import android.app.Dialog
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.worldonetop.portfolio.R
import com.worldonetop.portfolio.base.BaseActivity
import com.worldonetop.portfolio.data.model.Portfolio
import com.worldonetop.portfolio.data.source.Repository
import com.worldonetop.portfolio.databinding.ActivityDetailPortfolioBinding
import com.worldonetop.portfolio.util.CustomDialog
import com.worldonetop.portfolio.util.FileUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class DetailPortfolioActivity : BaseActivity<ActivityDetailPortfolioBinding>(R.layout.activity_detail_portfolio){
    private val viewModel: DetailViewModel by viewModels()
    private lateinit var loadingDialog: Dialog
    @Inject lateinit var repository: Repository
    @Inject lateinit var fileUtil: FileUtil

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FragmentContainerView>
    private lateinit var bottomActivitys:DetailBottomFragment
    private lateinit var bottomQuestions:DetailBottomFragment

    private lateinit var getFileLauncher: ActivityResultLauncher<String>


    override fun initData() {
        // set view model data
        viewModel.initData(1, portfolioData = intent.getSerializableExtra("data") as? Portfolio)
        binding.vm = viewModel

        // set util
        loadingDialog = CustomDialog.loading(this)

        // set bottom sheet
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomActivitys = DetailBottomFragment.newInstance(DetailBottomFragment.Companion.Type.ACTIVITYS)
        bottomQuestions = DetailBottomFragment.newInstance(DetailBottomFragment.Companion.Type.QUESTIONS)

        // set title click launcher
        getFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()){
            it?.let {
                loadingDialog.show()
                CoroutineScope(Dispatchers.IO).launch {
                    try{
                        val fileName = fileUtil.downloadFile(it,FileUtil.Companion.Type.Cache) ?: throw Exception()
                        if(fileName != binding.title.text.toString())
                            viewModel.removeFiles.add(binding.title.text.toString())

                        withContext(Dispatchers.Main){ // 저장한 파일 이름 표시 및 데이터로 저장
                            loadingDialog.dismiss()
                            viewModel.portfolioData.title = fileName
                            binding.title.setText(fileName)
                        }
                    }catch (e: Exception){
                        e.printStackTrace()
                        withContext(Dispatchers.Main){
                            loadingDialog.dismiss()
                            Toast.makeText(this@DetailPortfolioActivity,getString(R.string.error_save),Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
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

        // bottom sheet 접어놓기
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    override fun initListener() {
        // appbar
        binding.appbarMenu.setOnClickListener{
            doubleClick.run {
                viewModel.editMode.value?.let { editMode ->
                    if(editMode) {
                        if (formCheck()) {
                            formSave()
                        }
                    }else{
                        viewModel.setEditMode(true)
                    }
                }
            }
        }

        binding.title.setOnClickListener {
            viewModel.editMode.value?.let {
                if(it)
                    getFileLauncher.launch("*/*")
                else {
                    val fileIntent = fileUtil.openFileIntent(binding.title.text.toString(), FileUtil.Companion.Type.Resume, null)
                    if(fileIntent == null)
                        Toast.makeText(this, getString(R.string.error_connect_unknown), Toast.LENGTH_LONG).show()
                    else
                        startActivity(fileIntent)
                }
            }

        }

        // open bottom sheet
        binding.activitys.setOnClickListener{
            changeBottomFragment(bottomActivitys)

            // 두 레이아웃 간 높이 차 때문에 새로고침 개념
            BottomSheetBehavior.from(binding.bottomSheet).state = BottomSheetBehavior.STATE_HIDDEN
            BottomSheetBehavior.from(binding.bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED

        }
        binding.quetions.setOnClickListener{
            changeBottomFragment(bottomQuestions)

            // 두 레이아웃 간 높이 차 때문에 새로고침 개념
            BottomSheetBehavior.from(binding.bottomSheet).state = BottomSheetBehavior.STATE_HIDDEN
            BottomSheetBehavior.from(binding.bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
        }
    }
    private fun formCheck():Boolean{
        if(viewModel.portfolioData.title.isBlank()){
            binding.title.error = getString(R.string.error_no_input)
            return false
        }

        return true
    }
    private fun formSave(){
        loadingDialog.show()
        CoroutineScope(Dispatchers.IO).launch{
            try{
                fileUtil.moveCacheTo(FileUtil.Companion.Type.Resume, fromName = listOf(viewModel.portfolioData.title))

                // 삭제할 파일이 있으면
                if(viewModel.removeFiles.isNotEmpty()){
                    fileUtil.removeFile(viewModel.removeFiles, FileUtil.Companion.Type.Cache)
                    viewModel.removeFiles.clear()
                }
                if(viewModel.portfolioData.portfolioId == 0){ // 신규 데이터
                    repository.addPortfolio(viewModel.portfolioData)
                }else{ // 편집 데이터
                    repository.updatePortfolio(viewModel.portfolioData)
                }

                viewModel.portfolioData.portfolioId = repository.getLastPortfolioId()

                withContext(Dispatchers.Main){
                    Toast.makeText(this@DetailPortfolioActivity, getString(R.string.save_complete),Toast.LENGTH_SHORT).show()
                    viewModel.setEditMode(false)
                    loadingDialog.dismiss()
                }
            }catch (e: Exception){
                withContext(Dispatchers.Main){
                    loadingDialog.dismiss()
                    e.printStackTrace()
                    Toast.makeText(applicationContext, getString(R.string.error_save), Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    private fun changeBottomFragment(fragment: Fragment){
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.bottomSheet, fragment)
            .commit()

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
}