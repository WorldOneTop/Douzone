package com.worldonetop.portfolio.view.main

import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.worldonetop.portfolio.R
import com.worldonetop.portfolio.base.BaseFragment
import com.worldonetop.portfolio.data.model.Portfolio
import com.worldonetop.portfolio.databinding.FragmentPortfolioBinding
import com.worldonetop.portfolio.databinding.RowPortfolioBinding


class PortfolioFragment : BaseFragment<FragmentPortfolioBinding>(R.layout.fragment_portfolio) {
    companion object {
        @JvmStatic
        fun newInstance() = PortfolioFragment()
    }
    lateinit var rvAdapter:PortfolioAdapter

    override fun initData() {
        rvAdapter = PortfolioAdapter(
            object : DiffUtil.ItemCallback<Portfolio>() {
                override fun areItemsTheSame(oldItem: Portfolio, newItem: Portfolio): Boolean =
                    oldItem.portfolioId == newItem.portfolioId

                override fun areContentsTheSame(oldItem: Portfolio, newItem: Portfolio): Boolean =
                    oldItem == newItem
            }
        )

    }
    override fun initView() {
        binding.rvPortfolio.adapter = rvAdapter
    }

    override fun initListener() {

    }
}

class PortfolioAdapter(diffCallback: DiffUtil.ItemCallback<Portfolio>) :
    PagingDataAdapter<Portfolio, PortfolioAdapter.PortfolioVH>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): PortfolioAdapter.PortfolioVH {
        return PortfolioVH(
            RowPortfolioBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: PortfolioAdapter.PortfolioVH, position: Int) {
        holder.bind(getItem(position))
    }
    inner class PortfolioVH(private val binding: RowPortfolioBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(data:Portfolio?){
            data?.let {
                binding.fileName.text = it.title
                binding.content.text = it.content ?: ""


                if(it.like){
                    binding.like.setImageResource(R.drawable.full_star)
                    binding.root.setBackgroundResource(R.color.primaryLightColor)
                }else{
                    binding.like.setImageResource(R.drawable.empty_star)
                    binding.root.setBackgroundColor(Color.TRANSPARENT)
                }
            }
        }
    }
}