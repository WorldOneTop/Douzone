package com.example.portfolio.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.portfolio.R
import com.example.portfolio.base.BaseFragment
import com.example.portfolio.databinding.FragmentPortfolioBinding
import com.example.portfolio.databinding.RowBinding


class PortfolioFragment : BaseFragment<FragmentPortfolioBinding>(R.layout.fragment_portfolio) {

    override fun initView() {
        val adapter = TabOneAdapter(arrayOf("","","","","","",))
        binding.rv.adapter = adapter

    }

    override fun initListener() {
    }
}


class TabOneAdapter(private val data:Array<String>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val category:ArrayList<String> = ArrayList()
    init{
        category.addAll(data)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return TabOneVH(
            RowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            ))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as TabOneVH).bind()
    }
    override fun getItemCount(): Int {
        return category.size
    }
}
class TabOneVH(private val binding: RowBinding) : RecyclerView.ViewHolder(binding.root){
    fun bind(){
    }
}