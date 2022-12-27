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
import com.bumptech.glide.Glide
import com.worldonetop.portfolio.R
import com.worldonetop.portfolio.base.BaseFragment
import com.worldonetop.portfolio.data.model.LinkInfo
import com.worldonetop.portfolio.data.source.Repository
import com.worldonetop.portfolio.databinding.FragmentDetailBottomBinding
import com.worldonetop.portfolio.databinding.RowBottomSheetBinding
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
    @Inject lateinit var repository: Repository

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
                addActivityAdapter = AddActivityAdapter(fileUtil, viewModel.activityData.links,
                    { // 상세보기 리스너
                        CustomDialog.selectWebConnect(requireContext(), it).show()
                    }, { // 삭제 리스너
                    }
                )
                binding.rvBtm.adapter = addActivityAdapter
                try {
                    CoroutineScope(Dispatchers.IO).launch {
                        addActivityAdapter.setLinkInfo()
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }
            } // End - bottom adapter setting (Link)
            Type.FILES.ordinal ->{ // Start - bottom adapter setting (File)
                addActivityAdapter = AddActivityAdapter(fileUtil, viewModel.activityData.files,
                    {// 상세보기 리스너
                        try{
                            val fileIntent = if(viewModel.activityData.activityId==0){
                                fileUtil.openFileIntent(it, FileUtil.Companion.Type.Cache, null)
                            }else{
                                fileUtil.openFileIntent(it, FileUtil.Companion.Type.Activity, viewModel.activityData.activityId)
                            }

                            activity?.startActivity(fileIntent)
                        }catch (e:Exception){
                            Toast.makeText(requireContext(), getString(R.string.error_connect_unknown), Toast.LENGTH_LONG).show()
                        }
                    }, { // 삭제 리스너
                        viewModel.removeFiles.add(it)
                    }
                )
                binding.rvBtm.adapter = addActivityAdapter
            } // End - bottom adapter setting (File)
            Type.ACTIVITYS.ordinal ->{ // Start - bottom adapter setting (Activitys)
                projectAdapter = ProjectAdapter(
                    { data, _ ->// item click
                        viewModel.editMode.value?.let { editMode ->
                            if(editMode){
                                // 선택 or 해제될 데이터 동기화
                                if(projectAdapter.isSelected(data.activityId) == true){
                                    viewModel.portfolioData.activity.remove(data.activityId)
                                }else{
                                    viewModel.portfolioData.activity.add(data.activityId)
                                }

                                // long click 없이 바로 추가 할 수 있게
                                if(!projectAdapter.isSelectedMode())
                                    projectAdapter.startSelectedMode(listOf(data.activityId))

                            }
                            else{
                                startActivity(Intent(activity,DetailProjectActivity::class.java).putExtra("data",data))
                            }
                        }
                    },{ // item selected mode
                    },{ // like click
                        CoroutineScope(Dispatchers.IO).launch {
                            repository.updateActivitys(it)
                        }
                    }
                )
                binding.rvBtm.adapter = projectAdapter
            } // End - bottom adapter setting (Activitys)
            Type.QUESTIONS.ordinal ->{ // Start - bottom adapter setting (Question)
                questionAdapter = QuestionAdapter(
                    { data, _ ->// item click
                        viewModel.editMode.value?.let { editMode ->
                            if(editMode){
                                // 선택 or 해제된 데이터 동기화
                                if(questionAdapter.isSelected(data.questionId) == true){
                                    viewModel.portfolioData.question.remove(data.questionId)
                                }else{
                                    viewModel.portfolioData.question.add(data.questionId)
                                }
                                // long click 없이 바로 추가 할 수 있게
                                if(!questionAdapter.isSelectedMode())
                                    questionAdapter.startSelectedMode(listOf(data.questionId))

                            }
                            else{
                                startActivity(Intent(activity,DetailQuestionActivity::class.java).putExtra("data",data))
                            }
                        }
                    },{ // item selected mode
                    },{ // like click
                        CoroutineScope(Dispatchers.IO).launch {
                            repository.updateQuestion(it)
                        }
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
                    else
                        projectAdapter.endSelectedMode()
                }
                Type.QUESTIONS.ordinal->{
                    questionAdapter.viewMode = !it
                    if(it)
                        questionAdapter.startSelectedMode(viewModel.portfolioData.question)
                    else
                        questionAdapter.endSelectedMode()
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
                                addActivityAdapter.addItem(fileName,false)
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
                        addActivityAdapter.addItem(it, true)
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
    private val fileUtil: FileUtil,
    private val data: ArrayList<String>, // view model 의 데이터와 같음
    private val rootClickListener: (String)->Unit, // 해당 아이템 클릭, 해당 데이터 전달
    private val deleteListener: (String)->Unit, // 해당 아이템 제거, 해당 데이터 전달
): RecyclerView.Adapter<AddActivityAdapter.AddActionVH>() {

    private var viewMode = false
    private var linkInfo = mutableMapOf<String,LinkInfo>()

    inner class AddActionVH(
        private val binding: RowBottomSheetBinding,
        private val rootListener: (s:String) -> Unit,
        private val deleteListener: (s: String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(str: String, viewMode: Boolean, linkInfo: LinkInfo?=null) {
            binding.title.text = str

            if(linkInfo == null){
                binding.linkTitle.visibility = View.GONE
                binding.linkImage.visibility = View.GONE
            }else{
                binding.linkTitle.visibility = View.VISIBLE
                binding.linkImage.visibility = View.VISIBLE
                binding.linkTitle.text = linkInfo.title
                Glide.with(binding.root.context)
                    .load(linkInfo.image)
                    .into(binding.linkImage)
            }
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
    fun addItem(item: String, isLink:Boolean){
        data.add(item)
        notifyItemInserted(data.size -1)
        if(isLink){
            CoroutineScope(Dispatchers.IO).launch {
                fileUtil.getLinkInfo(item)?.let {
                    linkInfo[item] = it
                    withContext(Dispatchers.Main){
                        data.filterIndexed{ index, s ->  
                            if(s==item){
                                notifyItemChanged(index)
                                return@filterIndexed true
                            }
                            false
                        }
                    }
                }
            }
        }
    }
    private fun removeItem(item: String){
        val removeIndex = data.indexOf(item)
        if(removeIndex != -1) {
            CoroutineScope(Dispatchers.IO).launch {
                fileUtil.removeLinkInfo(linkInfo.getOrDefault(data[removeIndex], null))

                withContext(Dispatchers.Main){
                    data.removeAt(removeIndex)
                    notifyItemRemoved(removeIndex)
                }
            }
        }
    }

    fun setViewMode(viewMode: Boolean){
        this.viewMode =viewMode
        notifyDataSetChanged()
    }
    fun setLinkInfo(){
        CoroutineScope(Dispatchers.IO).launch {
            for(i in 0 until data.size){
                fileUtil.getLinkInfo(data[i])?.let {
                    linkInfo[data[i]] = it
                }
                withContext(Dispatchers.Main){
                    notifyItemChanged(i)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddActivityAdapter.AddActionVH {
        return AddActionVH(
            RowBottomSheetBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            ),rootClickListener) {
            removeItem(it)
            deleteListener(it)
        }
    }

    override fun onBindViewHolder(holder: AddActionVH, position: Int) {
        holder.bind(data[position], viewMode, linkInfo.getOrDefault(data[position], null))
    }

    override fun getItemCount(): Int {
        return data.size
    }
}