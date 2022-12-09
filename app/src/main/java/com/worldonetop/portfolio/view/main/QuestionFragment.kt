package com.worldonetop.portfolio.view.main

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.worldonetop.portfolio.R
import com.worldonetop.portfolio.base.BaseFragment
import com.worldonetop.portfolio.data.model.Portfolio
import com.worldonetop.portfolio.data.model.Question
import com.worldonetop.portfolio.databinding.FragmentQuestionBinding
import com.worldonetop.portfolio.databinding.RowPortfolioBinding
import com.worldonetop.portfolio.databinding.RowQuestionBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class QuestionFragment : BaseFragment<FragmentQuestionBinding>(R.layout.fragment_question) {
    companion object {
        @JvmStatic
        fun newInstance() = QuestionFragment()
    }

    private val viewModel: MainViewModel by activityViewModels()

    lateinit var rvAdapter: QuestionAdapter

    override fun initData() {
        rvAdapter = QuestionAdapter(
            object : DiffUtil.ItemCallback<Question>() {
                override fun areItemsTheSame(oldItem: Question, newItem: Question): Boolean =
                    oldItem.questionId == newItem.questionId

                override fun areContentsTheSame(oldItem: Question, newItem: Question): Boolean =
                    oldItem == newItem
            }
        )

        viewLifecycleOwner.lifecycleScope.launch{
            viewModel.questionData.collectLatest {
                rvAdapter.submitData(it)
            }
        }
    }

    override fun initView() {

    }

    override fun initListener() {

    }
}


class QuestionAdapter(diffCallback: DiffUtil.ItemCallback<Question>) :
    PagingDataAdapter<Question, QuestionAdapter.QuestionVH>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionAdapter.QuestionVH {
        return QuestionVH(
            RowQuestionBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: QuestionAdapter.QuestionVH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class QuestionVH(private val binding: RowQuestionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Question?) {
            data?.let {
                binding.question.text = it.question
                binding.answer.text = it.answer
                binding.answerCount.text = "${it.answer.length} 자"

                if (it.like) {
                    binding.like.setImageResource(R.drawable.full_star)
                    binding.root.setBackgroundResource(R.color.primaryLightColor)
                } else {
                    binding.like.setImageResource(R.drawable.empty_star)
                    binding.root.setBackgroundColor(Color.TRANSPARENT)
                }
            }
        }
    }
}