package com.worldonetop.portfolio.view.detail

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.worldonetop.portfolio.R
import com.worldonetop.portfolio.base.BaseFragment
import com.worldonetop.portfolio.databinding.FragmentDetailBottomBinding
import com.worldonetop.portfolio.databinding.RowLinksBinding
import com.worldonetop.portfolio.util.CustomDialog
import com.worldonetop.portfolio.util.FileUtil
import com.worldonetop.portfolio.view.main.ProjectAdapter
import com.worldonetop.portfolio.view.main.QuestionAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class DetailBottomFragment: BaseFragment<FragmentDetailBottomBinding>(R.layout.fragment_detail_bottom) {
    private val viewModel: DetailViewModel by activityViewModels()
    private var type = -1

    private lateinit var loadingDialog: Dialog
    @Inject lateinit var fileUtil: FileUtil

    private lateinit var addActivityAdapter: AddActivityAdapter
    private lateinit var getFileLauncher: ActivityResultLauncher<String>

    private lateinit var projectAdapter: ProjectAdapter
    private lateinit var questionAdapter: QuestionAdapter

    companion object {
        enum class Type{
            LINKS, FILES, ACTIVITYS, QUESTIONS
        }
        @JvmStatic
        fun newInstance(type:Type) =
            DetailBottomFragment().apply {
                arguments = Bundle().apply {
                    putInt("type", type.ordinal)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            type = it.getInt("type")
        }
    }
    override fun initData() {
        loadingDialog = CustomDialog.loading(requireContext())

        // bottom sheet adapter setting
        when(type){
            Type.LINKS.ordinal ->{ // Start - bottom adapter setting (Link)
                addActivityAdapter = AddActivityAdapter(viewModel.activityData.links,
                    { // 상세보기 리스너
                        CustomDialog.selectWebConnect(requireContext(), it).show()
                    }, { // 삭제 리스너
                        val removeIndex = viewModel.activityData.links.indexOf(it)
                        if(removeIndex != -1) {
                            viewModel.activityData.links.removeAt(removeIndex)
                            addActivityAdapter.notifyItemRemoved(removeIndex)
                        }
                    }
                )
                binding.rvBtm.adapter = addActivityAdapter
            } // End - bottom adapter setting (Link)
            Type.FILES.ordinal ->{ // Start - bottom adapter setting (File)
                addActivityAdapter = AddActivityAdapter(viewModel.activityData.files,
                    {// 상세보기 리스너
                        val fileIntent = if(viewModel.activityData.activityId==0){
                            fileUtil.openFileIntent(it, FileUtil.Companion.Type.Cache, null)
                        }else{
                            fileUtil.openFileIntent(it, FileUtil.Companion.Type.Activity, viewModel.activityData.activityId)
                        }

                        if(fileIntent == null)
                            Toast.makeText(requireContext(), getString(R.string.error_connect_unknown), Toast.LENGTH_LONG).show()
                        else
                            activity?.startActivity(fileIntent)
                    }, { // 삭제 리스너
                        viewModel.removeFiles.add(it)
                        val removeIndex = viewModel.activityData.files.indexOf(it)
                        if(removeIndex != -1){
                            viewModel.activityData.files.removeAt(removeIndex)
                            addActivityAdapter.notifyItemRemoved(removeIndex)
                        }
                    }
                )
                binding.rvBtm.adapter = addActivityAdapter
            } // End - bottom adapter setting (File)
            Type.ACTIVITYS.ordinal ->{ // Start - bottom adapter setting (Activitys)
                projectAdapter = ProjectAdapter(
                    { // item click
                        viewModel.editMode.value?.let { editMode ->
                            if(editMode){
                                if(!projectAdapter.isSelectedMode()) // long click 없이 바로 추가 할 수 있게
                                    projectAdapter.startSelectedMode(listOf(it.activityId))

                                // 선택 or 해제된 데이터 동기화
                                if(projectAdapter.isSelected(it.activityId) == true){
                                    viewModel.portfolioData.activity.add(it.activityId)
                                }else if(projectAdapter.isSelected(it.activityId) == false){
                                    viewModel.portfolioData.activity.remove(it.activityId)
                                }
                            }
                            else{
                                startActivity(Intent(activity,DetailProjectActivity::class.java).putExtra("data",it))
                            }
                        }
                    },{ // item selected mode
                    },{ // like click
                    }
                )
                binding.rvBtm.adapter = projectAdapter
            } // End - bottom adapter setting (Activitys)
            Type.QUESTIONS.ordinal ->{ // Start - bottom adapter setting (Question)
                questionAdapter = QuestionAdapter(
                    { // item click
                        viewModel.editMode.value?.let { editMode ->
                            if(editMode){
                                if(!questionAdapter.isSelectedMode()) // long click 없이 바로 추가 할 수 있게
                                    questionAdapter.startSelectedMode(listOf(it.questionId))

                                // 선택 or 해제된 데이터 동기화
                                if(questionAdapter.isSelected(it.questionId) == true){
                                    viewModel.portfolioData.question.add(it.questionId)
                                }else if(questionAdapter.isSelected(it.questionId) == false){
                                    viewModel.portfolioData.question.remove(it.questionId)
                                }
                            }
                            else{
                                startActivity(Intent(activity,DetailQuestionActivity::class.java).putExtra("data",it))
                            }
                        }
                    },{ // item selected mode
                    },{ // like click
                    }
                )
                binding.rvBtm.adapter = questionAdapter
            } // End - bottom adapter setting (Question)
        }
    }

    override fun initView() {

        // edit mode 에 따른 observe
        viewModel.editMode.observe(viewLifecycleOwner){
            when(type){
                Type.LINKS.ordinal, Type.FILES.ordinal->{
                    addActivityAdapter.setViewMode(!it)
                    binding.add.visibility = if(it) View.VISIBLE else View.GONE
                }
                Type.ACTIVITYS.ordinal->{
                    projectAdapter.viewMode = !it
                    if(it)
                        projectAdapter.startSelectedMode(viewModel.portfolioData.activity)
                }
                Type.QUESTIONS.ordinal->{
                    questionAdapter.viewMode = !it
                    if(it)
                        questionAdapter.startSelectedMode(viewModel.portfolioData.question)
                }
            }
        }
        // paging adapter data setting
        if(type == Type.ACTIVITYS.ordinal){
            viewModel.pagingActivity.observe(viewLifecycleOwner) {
                lifecycleScope.launch {
                    projectAdapter.submitData(it)
                }
            }
        }else if(type== Type.QUESTIONS.ordinal){
            viewModel.pagingQuestion.observe(viewLifecycleOwner) {
                lifecycleScope.launch {
                    questionAdapter.submitData(it)
                }
            }
        }

        // add button invisible
        if(type == Type.QUESTIONS.ordinal || type == Type.ACTIVITYS.ordinal)
            binding.add.visibility = View.GONE
    }

    override fun initListener() {
        // fileLauncher listener (활동 관련 파일 저장)
        if(type == Type.FILES.ordinal){
            getFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()){
                it?.let {
                    loadingDialog.show()
                    CoroutineScope(Dispatchers.IO).launch {
                        try{
                            val activityId = viewModel.activityData.activityId
                            val fileName = if(activityId != 0)  // 편집
                                fileUtil.downloadFile(it,FileUtil.Companion.Type.Activity,activityId) ?: throw Exception()
                            else // 새로
                                fileUtil.downloadFile(it,FileUtil.Companion.Type.Cache) ?: throw Exception()

                            withContext(Dispatchers.Main){ // 저장한 파일 이름 표시 및 데이터로 저장
                                loadingDialog.dismiss()
                                viewModel.activityData.files.add(fileName)
                                addActivityAdapter.notifyItemInserted(viewModel.activityData.files.size -1)
                            }
                        }catch (e: Exception){
                            e.printStackTrace()
                            withContext(Dispatchers.Main){
                                loadingDialog.dismiss()
                                Toast.makeText(requireContext(),getString(R.string.error_save),Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        }

        // 추가 리스너
        binding.add.setOnClickListener{
            if(type == Type.LINKS.ordinal) {
                CustomDialog.addLink(requireContext()) {
                    if (it.isNotBlank()) {
                        viewModel.activityData.links.add(it)
                        addActivityAdapter.notifyItemInserted(viewModel.activityData.links.size -1)
                    }
                }.show()
            }
            else if(type== Type.FILES.ordinal){
                getFileLauncher.launch("*/*")
            }
        }

    }
}

class AddActivityAdapter(
    private val data: ArrayList<String>, // view model 의 데이터와 같음
    private val rootClickListener: (String)->Unit, // 해당 아이템 클릭, 해당 데이터 전달
    private val deleteListener: (String)->Unit, // 해당 아이템 제거, 해당 데이터 전달
): RecyclerView.Adapter<AddActivityAdapter.AddActionVH>() {

    private var viewMode = false


    inner class AddActionVH(
        private val binding: RowLinksBinding,
        private val rootListener: (s:String) -> Unit,
        private val deleteListener: (s: String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(str: String, viewMode: Boolean) {
            binding.linkText.text = str
            if(viewMode)
                binding.delete.visibility = View.GONE
            else
                binding.delete.visibility = View.VISIBLE
            binding.delete.setOnClickListener {
                this.deleteListener(str)
            }
            binding.root.setOnClickListener{
                this.rootListener(str)
            }
        }
    }

    fun setViewMode(viewMode: Boolean){
        this.viewMode =viewMode
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddActivityAdapter.AddActionVH {
        return AddActionVH(
            RowLinksBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            ),rootClickListener,deleteListener)
    }

    override fun onBindViewHolder(holder: AddActionVH, position: Int) {
        holder.bind(data[position], viewMode)
    }

    override fun getItemCount(): Int {
        return data.size
    }
}