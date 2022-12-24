package com.worldonetop.portfolio.view.main

import android.app.Dialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.worldonetop.portfolio.R
import com.worldonetop.portfolio.base.BaseFragment
import com.worldonetop.portfolio.data.model.Portfolio
import com.worldonetop.portfolio.data.source.Repository
import com.worldonetop.portfolio.databinding.FragmentPagerBinding
import com.worldonetop.portfolio.databinding.RowPortfolioBinding
import com.worldonetop.portfolio.util.CustomDialog
import com.worldonetop.portfolio.util.FileUtil
import com.worldonetop.portfolio.view.detail.DetailPortfolioActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import androidx.core.util.Pair as UtilPair

@AndroidEntryPoint
class PortfolioFragment : BaseFragment<FragmentPagerBinding>(R.layout.fragment_pager) {
    companion object {
        @JvmStatic
        fun newInstance() = PortfolioFragment()
    }

    private val viewModel: MainViewModel by activityViewModels()
    @Inject lateinit var repository: Repository
    @Inject lateinit var fileUtil: FileUtil
    private lateinit var loadingDialog: Dialog

    private lateinit var rvAdapter: PortfolioAdapter

    override fun initData() {
        // paging adapter
        rvAdapter = PortfolioAdapter(
            { data, view ->// item click
                if(!rvAdapter.isSelectedMode()) {
                    val p1 = UtilPair.create<View,String>(view.fileName,view.fileName.transitionName)
                    val p2 = UtilPair.create<View,String>(view.content,view.content.transitionName)
                    startActivity(
                        Intent(activity,DetailPortfolioActivity::class.java)
                            .putExtra("data",data),
                        ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), p1, p2)
                            .toBundle()
                    )
                }
            },{ // item selected mode
                viewModel.selectMode.value = it
            },{ // like click
                CoroutineScope(Dispatchers.IO).launch {
                    repository.updatePortfolio(it)
                }
            }
        )
        binding.rv.adapter = rvAdapter

        loadingDialog = CustomDialog.loading(requireContext())
    }

    override fun initView() {
        binding.root.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondaryBackgroundColor))

        viewModel.portfolioData.observe(viewLifecycleOwner) {
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
                MainViewModel.Companion.Type.DELETE ->{
                        loadingDialog.show()
                        CoroutineScope(Dispatchers.IO).launch {
                            val removeData = repository.getPortfolioSelected(rvAdapter.getSelectedIds())
                            repository.removePortfolio(rvAdapter.getSelectedIds())
                            for(data in removeData)
                                fileUtil.removeFile(listOf(data.title), FileUtil.Companion.Type.Resume)

                            withContext(Dispatchers.Main){
                                viewModel.selectMode.value = false
                                viewModel.eventFloatingBtn.value = MainViewModel.Companion.Type.NONE
                                loadingDialog.dismiss()
                            }
                        }
                }
                MainViewModel.Companion.Type.SHARE ->{
                    viewModel.eventFloatingBtn.value = MainViewModel.Companion.Type.NONE
                }
                else ->{}
            }
        }
    }
}


class PortfolioAdapter(private val clickListener:(data:Portfolio, view:RowPortfolioBinding)->Unit,
                       private val selectedModeListener:(isSelectedMode:Boolean)->Unit,
                       private val likeListener:(data:Portfolio)->Unit,) :
    PagingDataAdapter<Portfolio, PortfolioAdapter.PortfolioVH>(
        object : DiffUtil.ItemCallback<Portfolio>() {
            override fun areItemsTheSame(oldItem: Portfolio, newItem: Portfolio): Boolean =
                oldItem.portfolioId == newItem.portfolioId

            override fun areContentsTheSame(oldItem: Portfolio, newItem: Portfolio): Boolean =
                oldItem == newItem && oldItem.portfolioId == newItem.portfolioId
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
        if(selectedIds.size == 0 || id == null)
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


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PortfolioAdapter.PortfolioVH {
        return PortfolioVH(
            RowPortfolioBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), { data, selected, view ->
                clickListener(data, view)
                if(!viewMode && selectedIds.isNotEmpty() && selected != null){
                    if(selected)
                        addSelectedId(data.portfolioId)
                    else
                        deleteSelectedId(data.portfolioId)
                }
            },
            {
                if(!viewMode &&selectedIds.isEmpty()) {
                    startSelectedMode(listOf(it.portfolioId))
                    selectedModeListener(true)
                }
            }, likeListener
        )
    }

    override fun onBindViewHolder(holder: PortfolioAdapter.PortfolioVH, position: Int) {
        holder.bind(getItem(position), isSelected(getItem(position)?.portfolioId))
    }

    inner class PortfolioVH(
        private val binding: RowPortfolioBinding,
        private val clickListener:(data:Portfolio, isSelect:Boolean?, view:RowPortfolioBinding)->Unit,
        private val longClickListener:(data:Portfolio)->Unit,
        private val likeListener:(data:Portfolio)->Unit,
        ) : RecyclerView.ViewHolder(binding.root){
        private var isSelect:Boolean? = null

        fun bind(data:Portfolio?, select:Boolean?){
            data?.let {
                binding.data = it

                setLikeLayout(it.like)

                isSelect = select
                isSelect?.let { s ->
                    setSelectedLayout(s)
                }
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
                binding.like.setOnClickListener{_ ->
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
                binding.rootLayout.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.primaryLightColor))
            }else{
                binding.like.setImageResource(R.drawable.empty_star)
                binding.rootLayout.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.primaryBackgroundColor))
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