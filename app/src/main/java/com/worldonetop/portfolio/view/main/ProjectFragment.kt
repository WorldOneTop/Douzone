package com.worldonetop.portfolio.view.main

import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.worldonetop.portfolio.BuildConfig
import com.worldonetop.portfolio.R
import com.worldonetop.portfolio.base.BaseFragment
import com.worldonetop.portfolio.data.model.Activitys
import com.worldonetop.portfolio.data.source.Repository
import com.worldonetop.portfolio.databinding.FragmentPagerBinding
import com.worldonetop.portfolio.databinding.RowProjectBinding
import com.worldonetop.portfolio.util.CustomDialog
import com.worldonetop.portfolio.util.FileUtil
import com.worldonetop.portfolio.view.detail.DetailProjectActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import androidx.core.util.Pair as UtilPair

@AndroidEntryPoint
class ProjectFragment : BaseFragment<FragmentPagerBinding>(R.layout.fragment_pager) {
    companion object {
        @JvmStatic
        fun newInstance() = ProjectFragment()
    }

    private val viewModel: MainViewModel by activityViewModels()
    @Inject lateinit var repository: Repository
    @Inject lateinit var fileUtil: FileUtil
    private lateinit var loadingDialog: Dialog

    private lateinit var rvAdapter: ProjectAdapter
    private lateinit var sharedFileLauncher: ActivityResultLauncher<Intent>

    override fun initData() {
        // paging adapter
        rvAdapter = ProjectAdapter(
            { data, view -> // item click
                if(!rvAdapter.isSelectedMode()){
                    val p1 = UtilPair.create<View,String>(view.category,view.category.transitionName)
                    val p2 = UtilPair.create<View,String>(view.title,view.title.transitionName)
                    val p3 = UtilPair.create<View,String>(view.content,view.content.transitionName)
                    val p4 = UtilPair.create<View,String>(view.startDate,view.startDate.transitionName)
                    val p5 = UtilPair.create<View,String>(view.endDate,view.endDate.transitionName)
                    startActivity(
                        Intent(activity,DetailProjectActivity::class.java)
                            .putExtra("data",data),
                        ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), p1, p2, p3, p4, p5)
                            .toBundle()
                    )
                }

            },{ // item selected mode
                viewModel.selectMode.value = it

            },{ // like click
                CoroutineScope(Dispatchers.IO).launch {
                    repository.updateActivitys(it)
                }

            }
        )
        binding.rv.adapter = rvAdapter

        loadingDialog = CustomDialog.loading(requireContext())
    }

    override fun initView() {
        binding.root.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondaryBackgroundColor))

        viewModel.activitysData.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                rvAdapter.submitData(it)
            }
        }

    }

    override fun initListener() {

        // 페이징 끝날 때 alpha 값 조정해서 보여줌
        rvAdapter.addLoadStateListener {
            if(it.source.refresh is LoadState.NotLoading) {
                (activity as? MainActivity)?.setViewPagerAlpha(1f)
            }
        }
        // 액티비티에서 전한 select mode 종료 이벤트
        viewModel.selectMode.observe(viewLifecycleOwner){
            if(!it)
                rvAdapter.endSelectedMode()
        }
        // delete, share 이벤트 처리
        viewModel.eventFloatingBtn.observe(viewLifecycleOwner){
            if(!rvAdapter.isSelectedMode())
                return@observe
            when(it){
                MainViewModel.Companion.EventType.DELETE ->{
                        loadingDialog.show()
                        CoroutineScope(Dispatchers.IO).launch {
                            val removeData = repository.getActivitysSelected(rvAdapter.getSelectedIds())
                            repository.removeActivitys(rvAdapter.getSelectedIds())
                            for(data in removeData){
                                fileUtil.removeFile(data.files, FileUtil.Companion.Type.Activity, data.activityId)
                                fileUtil.removeFile(listOf(data.title), FileUtil.Companion.Type.Activity)
                            }
                            withContext(Dispatchers.Main){
                                viewModel.selectMode.value = false
                                viewModel.eventFloatingBtn.value = MainViewModel.Companion.EventType.NONE
                                loadingDialog.dismiss()

                            }
                        }
                }
                MainViewModel.Companion.EventType.SHARE ->{
                    loadingDialog.show()
                    CoroutineScope(Dispatchers.IO).launch {
                        val shareData = repository.getActivitysSelected(rvAdapter.getSelectedIds())
                        val file = fileUtil.makeZipFolder(fileUtil.createSharedActivitys(shareData, getString(R.string.tab_activity)))
                        val uriToFile = FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID + ".fileprovider", file)
                        withContext(Dispatchers.Main){
                            loadingDialog.dismiss()
                            viewModel.eventFloatingBtn.value = MainViewModel.Companion.EventType.NONE
                            val shareIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_STREAM, uriToFile)
                                type = "application/zip"
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            sharedFileLauncher.launch(Intent.createChooser(shareIntent, "share"))
                        }
                    }
                }
                else ->{}
            }
        }
        sharedFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            CoroutineScope(Dispatchers.IO).launch{ fileUtil.removeShasredData(getString(R.string.tab_activity)) }
        }
    }
}


class ProjectAdapter(private val clickListener:(data:Activitys, View:RowProjectBinding)->Unit,
                     private val selectedModeListener:(isSelectedMode:Boolean)->Unit,
                     private val likeListener:(data:Activitys)->Unit,) :
    PagingDataAdapter<Activitys, ProjectAdapter.ProjectVH>(
        object : DiffUtil.ItemCallback<Activitys>() {
            override fun areItemsTheSame(oldItem: Activitys, newItem: Activitys): Boolean =
                oldItem.activityId == newItem.activityId

            override fun areContentsTheSame(oldItem: Activitys, newItem: Activitys): Boolean =
                oldItem == newItem && oldItem.activityId == newItem.activityId
        }
    ) {
    var viewMode = false // select mode 가 되지 않는 모드
    private var selectedIds:ArrayList<Int> = ArrayList()

    fun getSelectedIds() = selectedIds.toList()
    fun isSelectedMode() = selectedIds.isNotEmpty()

    fun startSelectedMode(firstId:List<Int>){
        selectedIds.clear()
        selectedIds.addAll(firstId)
        notifyItemRangeChanged(0,itemCount)
    }
    fun endSelectedMode(){
        selectedIds.clear()
        notifyItemRangeChanged(0,itemCount)
    }
    fun isSelected(id:Int?):Boolean?{
        if(selectedIds.isEmpty() || id == null)
            return null
        return selectedIds.contains(id)
    }
    private fun addSelectedId(id:Int){
        selectedIds.add(id)
    }
    private fun deleteSelectedId(id:Int){
        selectedIds.remove(id)
        if(selectedIds.isEmpty()) {
            endSelectedMode()
            selectedModeListener(false)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectAdapter.ProjectVH {
        return ProjectVH(
            RowProjectBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), { data, selected, view ->
                clickListener(data, view)
                if(!viewMode &&selectedIds.isNotEmpty() && selected != null){
                    if(selected)
                        addSelectedId(data.activityId)
                    else
                        deleteSelectedId(data.activityId)
                }
            },
            {
                if(!viewMode &&selectedIds.isEmpty()) {
                    startSelectedMode(listOf(it.activityId))
                    selectedModeListener(true)
                }
            }, likeListener
        )
    }

    override fun onBindViewHolder(holder: ProjectAdapter.ProjectVH, position: Int) {
        holder.bind(getItem(position), isSelected(getItem(position)?.activityId))
    }

    inner class ProjectVH(
        private val binding: RowProjectBinding,
        private val clickListener:(data:Activitys, isSelect:Boolean?, view:RowProjectBinding)->Unit,
        private val longClickListener:(data:Activitys)->Unit,
        private val likeListener:(data:Activitys)->Unit,
        ) : RecyclerView.ViewHolder(binding.root){
        private var isSelect:Boolean? = null

        private val categoryStringList = binding.root.context.resources.getStringArray(R.array.activityCategoryString)
        private val categoryColorList = binding.root.context.resources.getIntArray(R.array.activityCategoryColor)

        fun bind(data: Activitys?, select:Boolean?){
            data?.let {
                binding.data = it

                binding.category.text = categoryStringList[it.type]
                val colorTint = ColorStateList.valueOf(Color.parseColor("#"+Integer.toHexString(categoryColorList[it.type])))
                binding.category.backgroundTintList = colorTint
                binding.point.backgroundTintList = colorTint

                setLikeLayout(it.like)

                isSelect = select
                setSelectedLayout(select ?: false)

                // listener
                binding.root.setOnClickListener{_ ->
                    if(isSelect != null){
                        isSelect = !isSelect !!
                        setSelectedLayout(isSelect!!)
                    }
                    clickListener(it, isSelect, binding)
                }
                binding.root.setOnLongClickListener{_ ->
                    longClickListener(it)
                    true
                }
                binding.like.setOnClickListener{ _ ->
                    if(isSelect == null) {
                        it.like = !it.like
                        setLikeLayout(it.like)
                        likeListener(it)
                    }
                }
            }
        }

        private fun setLikeLayout(isLike:Boolean){
            if(isLike){
                binding.like.setImageResource(R.drawable.full_star)
                binding.cardView.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.primaryLightColor))
            }else{
                binding.like.setImageResource(R.drawable.empty_star)
                binding.cardView.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.primaryBackgroundColor))
            }
        }
        private fun setSelectedLayout(isSelected:Boolean){
            if(isSelected){
                binding.selectLayout.root.visibility = View.VISIBLE
            }else{
                binding.selectLayout.root.visibility = View.INVISIBLE
            }
        }
    }
}