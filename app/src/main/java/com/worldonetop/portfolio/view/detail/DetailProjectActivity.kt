package com.worldonetop.portfolio.view.detail

import android.app.DatePickerDialog
import android.app.Dialog
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.worldonetop.portfolio.R
import com.worldonetop.portfolio.base.BaseActivity
import com.worldonetop.portfolio.data.model.Activitys
import com.worldonetop.portfolio.data.source.Repository
import com.worldonetop.portfolio.databinding.ActivityDetailProjectBinding
import com.worldonetop.portfolio.util.CustomDialog
import com.worldonetop.portfolio.util.FileUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class DetailProjectActivity : BaseActivity<ActivityDetailProjectBinding>(R.layout.activity_detail_project){
    private val viewModel: DetailViewModel by viewModels()
    private lateinit var loadingDialog: Dialog
    @Inject lateinit var repository: Repository
    @Inject lateinit var fileUtil: FileUtil

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FragmentContainerView>
    private lateinit var bottomLinks:DetailBottomFragment
    private lateinit var bottomFiles:DetailBottomFragment

    override fun initData() {
        // set view model data
        viewModel.initData(0, activityData = intent.getSerializableExtra("data") as? Activitys)
        binding.vm = viewModel

        // set util
        loadingDialog = CustomDialog.loading(this)

        // set bottom sheet
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomLinks = DetailBottomFragment.newInstance(DetailBottomFragment.Companion.Type.LINKS)
        bottomFiles = DetailBottomFragment.newInstance(DetailBottomFragment.Companion.Type.FILES)

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


        // 카테고리 데이터 설정
        val categoryList =
            arrayOf(getString(R.string.category)).plus(resources.getStringArray(R.array.activityCategoryString))
        binding.category.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categoryList)

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


        // start, end date setting
        binding.startDate.setOnClickListener {
            doubleClick.run {
                val calendar: Calendar = Calendar.getInstance()
                val pYear: Int = calendar.get(Calendar.YEAR) //년
                val pMonth: Int = calendar.get(Calendar.MONTH) //월
                val pDay: Int = calendar.get(Calendar.DAY_OF_MONTH) //일
                DatePickerDialog(
                    this, { _, year, month, day ->
                        val date = "${year % 100}.${month + 1}.$day"
                        viewModel.setStartDate(date)
                    }, pYear, pMonth, pDay // 초기 날짜 설정
                ).show()
            }
        }
        binding.endDate.setOnClickListener {
            doubleClick.run {
                val calendar: Calendar = Calendar.getInstance()
                val pYear: Int = calendar.get(Calendar.YEAR) //년
                val pMonth: Int = calendar.get(Calendar.MONTH) //월
                val pDay: Int = calendar.get(Calendar.DAY_OF_MONTH) //일
                DatePickerDialog(this, { _, year, month, day -> // return listener
                    val date = "${year % 100}.${month + 1}.$day"
                    viewModel.setEndDate(date)
                }, pYear, pMonth, pDay // 초기 날짜 설정
                ).show()
            }
        }

        // open bottom sheet
        binding.links.setOnClickListener{
            changeBottomFragment(bottomLinks)

            // 두 레이아웃 간 높이 차 때문에 새로고침 개념
            BottomSheetBehavior.from(binding.bottomSheet).state = BottomSheetBehavior.STATE_HIDDEN
            BottomSheetBehavior.from(binding.bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED

        }
        binding.files.setOnClickListener{
            changeBottomFragment(bottomFiles)

            // 두 레이아웃 간 높이 차 때문에 새로고침 개념
            BottomSheetBehavior.from(binding.bottomSheet).state = BottomSheetBehavior.STATE_HIDDEN
            BottomSheetBehavior.from(binding.bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
        }
    }
    private fun formCheck():Boolean{
        if(viewModel.activityData.title.isBlank()){
            binding.title.error = getString(R.string.error_no_input)
            return false
        }

        if(viewModel.activityData.type == -1){
            (binding.category.selectedView as TextView).error = getString(R.string.error_no_input)
            return false
        }
        if(viewModel.activityData.startDate.isBlank()){
            binding.startDate.error = getString(R.string.error_no_input)
            return false
        }
        return true
    }
    private fun formSave(){
        loadingDialog.show()
        CoroutineScope(Dispatchers.IO).launch{
            try{
                if(viewModel.activityData.activityId == 0){ // 신규 데이터

                    // DB 저장
                    repository.addActivitys(viewModel.activityData)
                    viewModel.activityData.activityId = repository.getLastActivitysId() // DB id 값 저장
                    // 캐시파일 에서 본 디렉토리로
                    fileUtil.moveCacheTo(FileUtil.Companion.Type.Activity,viewModel.activityData.files, idTo = viewModel.activityData.activityId)
                }else{ // 편집 데이터
                    // 기존 작업 파일에 저장되어 있음
                    // DB 저장
                    repository.updateActivitys(viewModel.activityData)
                }
                // 삭제할 파일이 있으면
                if(viewModel.removeFiles.isNotEmpty()){
                    fileUtil.removeFile(viewModel.removeFiles, FileUtil.Companion.Type.Activity, viewModel.activityData.activityId)
                    viewModel.removeFiles.clear()
                }
                withContext(Dispatchers.Main){
                    Toast.makeText(this@DetailProjectActivity, getString(R.string.save_complete),Toast.LENGTH_SHORT).show()
                    loadingDialog.dismiss()
                    viewModel.setEditMode(false)
                }
            }catch (e: FileNotFoundException){
                // DB 저장 후 파일 저장 시 에러 났다면
                if(viewModel.activityData.activityId == repository.getLastActivitysId())
                    // rollback
                    repository.removeActivitys(listOf(viewModel.activityData.activityId))

                withContext(Dispatchers.Main){
                    loadingDialog.dismiss()
                    Toast.makeText(applicationContext, getString(R.string.error_save), Toast.LENGTH_LONG).show()
                }
            }catch (e: Exception){
                withContext(Dispatchers.Main){
                    e.printStackTrace()
                    loadingDialog.dismiss()
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