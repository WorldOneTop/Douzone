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


        // ???????????? ????????? ??????
        val categoryList =
            arrayOf(getString(R.string.category)).plus(resources.getStringArray(R.array.activityCategoryString))
        binding.category.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categoryList)

        // bottom sheet ????????????
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
                val pYear: Int = calendar.get(Calendar.YEAR) //???
                val pMonth: Int = calendar.get(Calendar.MONTH) //???
                val pDay: Int = calendar.get(Calendar.DAY_OF_MONTH) //???
                DatePickerDialog(
                    this, { _, year, month, day ->
                        val date = "${
                            (year % 100).toString().padStart(2,'0')
                        }.${
                            (month + 1).toString().padStart(2,'0')
                        }.${
                            (day).toString().padStart(2,'0')
                        }"
                        viewModel.setStartDate(date)
                    }, pYear, pMonth, pDay // ?????? ?????? ??????
                ).show()
            }
        }
        binding.endDate.setOnClickListener {
            doubleClick.run {
                val calendar: Calendar = Calendar.getInstance()
                val pYear: Int = calendar.get(Calendar.YEAR) //???
                val pMonth: Int = calendar.get(Calendar.MONTH) //???
                val pDay: Int = calendar.get(Calendar.DAY_OF_MONTH) //???
                DatePickerDialog(this, { _, year, month, day -> // return listener
                    val date = "${
                        (year % 100).toString().padStart(2,'0')
                    }.${
                        (month + 1).toString().padStart(2,'0')
                    }.${
                        (day).toString().padStart(2,'0')
                    }"
                    viewModel.setEndDate(date)
                }, pYear, pMonth, pDay // ?????? ?????? ??????
                ).show()
            }
        }

        // open bottom sheet
        binding.links.setOnClickListener{
            changeBottomFragment(bottomLinks)
            BottomSheetBehavior.from(binding.bottomSheet).state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }
        binding.files.setOnClickListener{
            changeBottomFragment(bottomFiles)
            BottomSheetBehavior.from(binding.bottomSheet).state = BottomSheetBehavior.STATE_HALF_EXPANDED
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
                if(viewModel.activityData.activityId == 0){ // ?????? ?????????

                    // DB ??????
                    repository.addActivitys(viewModel.activityData)
                    viewModel.activityData.activityId = repository.getLastActivitysId() // DB id ??? ??????
                    // ???????????? ?????? ??? ???????????????
                    fileUtil.moveCacheTo(FileUtil.Companion.Type.Activity,viewModel.activityData.files, idTo = viewModel.activityData.activityId)
                }else{ // ?????? ?????????
                    // ?????? ?????? ????????? ???????????? ??????
                    // DB ??????
                    repository.updateActivitys(viewModel.activityData)
                }
                // ????????? ????????? ?????????
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
                // DB ?????? ??? ?????? ?????? ??? ?????? ?????????
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

    override fun onBackPressed() {
        super.onBackPressed()
        if(intent.getBooleanExtra("slideAin",false))
            overridePendingTransition(R.anim.fadein, R.anim.slide_right)
    }
}