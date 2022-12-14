package com.worldonetop.portfolio.view.main

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.worldonetop.portfolio.R
import com.worldonetop.portfolio.base.BaseFragment
import com.worldonetop.portfolio.data.model.Question
import com.worldonetop.portfolio.data.source.Repository
import com.worldonetop.portfolio.databinding.FragmentPagerBinding
import com.worldonetop.portfolio.databinding.RowQuestionBinding
import com.worldonetop.portfolio.view.detail.DetailQuestionActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class QuestionFragment : BaseFragment<FragmentPagerBinding>(R.layout.fragment_pager) {
    companion object {
        @JvmStatic
        fun newInstance() = QuestionFragment()
    }

    private val viewModel: MainViewModel by activityViewModels()
    @Inject lateinit var repository: Repository

    private lateinit var rvAdapter: QuestionAdapter

    override fun initData() {
        // paging adapter
        rvAdapter = QuestionAdapter(
            { // item click
                if(!rvAdapter.isSelectedMode())
                    startActivity(Intent(activity,DetailQuestionActivity::class.java).putExtra("data",it))

            },{ // item selected mode
                viewModel.selectMode.value = it

            },{ // like click
                CoroutineScope(Dispatchers.IO).launch {
                    repository.updateQuestion(it)
                }
            }
        )

        binding.rv.adapter = rvAdapter
    }

    override fun initView() {
        binding.divider.visibility = View.GONE
        binding.root.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primaryBackgroundColor))

        viewModel.questionData.observe(viewLifecycleOwner) {
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
            when(it){
                MainViewModel.Companion.Type.DELETE ->{
                    CoroutineScope(Dispatchers.IO).launch {
                        repository.removeQuestion(rvAdapter.getSelectedIds())
                    }
                    viewModel.eventFloatingBtn.value = MainViewModel.Companion.Type.NONE
                    viewModel.selectMode.value = false
                }
                MainViewModel.Companion.Type.SHARE ->{
                    viewModel.eventFloatingBtn.value = MainViewModel.Companion.Type.NONE
                }
                else ->{}
            }
        }
    }
}


class QuestionAdapter(private val clickListener:(data:Question)->Unit,
                      private val selectedModeListener:(isSelectedMode:Boolean)->Unit,
                      private val likeListener:(data:Question)->Unit,) :
    PagingDataAdapter<Question, QuestionAdapter.QuestionVH>(
        object: DiffUtil.ItemCallback<Question>(){
            override fun areItemsTheSame(oldItem: Question, newItem: Question): Boolean {
                return oldItem.questionId == newItem.questionId
            }

            override fun areContentsTheSame(oldItem: Question, newItem: Question): Boolean {
                return oldItem == newItem && oldItem.questionId == newItem.questionId
            }
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


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionAdapter.QuestionVH {
        return QuestionVH(
            RowQuestionBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), { data, selected ->
                clickListener(data)
                if(!viewMode &&selectedIds.isNotEmpty() && selected != null){
                    if(selected)
                        addSelectedId(data.questionId)
                    else
                        deleteSelectedId(data.questionId)
                }
            },
            {
                if(!viewMode &&selectedIds.isEmpty()) {
                    startSelectedMode(listOf(it.questionId))
                    selectedModeListener(true)
                }
            }, likeListener
        )
    }

    override fun onBindViewHolder(holder: QuestionAdapter.QuestionVH, position: Int) {
        holder.bind(getItem(position), isSelected(getItem(position)?.questionId))
    }

    inner class QuestionVH(
        private val binding: RowQuestionBinding,
        private val clickListener:(data:Question, isSelect:Boolean?)->Unit,
        private val longClickListener:(data:Question)->Unit,
        private val likeListener:(data:Question)->Unit,
        ) : RecyclerView.ViewHolder(binding.root) {
        private var isOpen = false
        private var isSelect:Boolean? = null

        fun bind(data: Question?, select:Boolean?) {
            data?.let {
                binding.data = it

                setLikeLayout(it.like)

                isSelect = select
                isSelect?.let { s ->
                    setSelectedLayout(s)
                }

                // listener
                binding.root.setOnClickListener{ _ ->
                    if(isSelect != null){
                        isSelect = !isSelect !!
                        setSelectedLayout(isSelect!!)
                    }
                    clickListener(it, isSelect)
                }
                binding.root.setOnLongClickListener{ _ ->
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
                binding.open.setOnClickListener{
                    isOpen = !isOpen
                    if(isOpen) {
                        binding.answer.maxLines = Integer.MAX_VALUE
                        binding.open.scaleY = -1f
                    }
                    else {
                        binding.answer.maxLines = 3
                        binding.open.scaleY = 1f
                    }
                }
            }
        }

        private fun setLikeLayout(isLike:Boolean){
            if(isLike){
                binding.like.setImageResource(R.drawable.full_star)
                binding.root.setBackgroundResource(R.color.point6)
            }else{
                binding.like.setImageResource(R.drawable.empty_star)
                binding.root.setBackgroundColor(Color.TRANSPARENT)
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