package com.worldonetop.portfolio.view.main

import android.content.Intent
import android.graphics.Color
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.worldonetop.portfolio.R
import com.worldonetop.portfolio.base.BaseFragment
import com.worldonetop.portfolio.data.model.Activitys
import com.worldonetop.portfolio.data.model.Portfolio
import com.worldonetop.portfolio.databinding.FragmentProjectBinding
import com.worldonetop.portfolio.databinding.RowPortfolioBinding
import com.worldonetop.portfolio.databinding.RowProjectBinding

class ProjectFragment : BaseFragment<FragmentProjectBinding>(R.layout.fragment_project) {
    companion object {
        @JvmStatic
        fun newInstance() = ProjectFragment()
    }

    override fun initData() {

    }
    override fun initView() {
        binding.rvProject.setOnClickListener{
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/pdf"

                // Optionally, specify a URI for the file that should appear in the
                // system file picker when it loads.
//                putExtra(DocumentsContract.EXTRA_INITIAL_URI, activity.geturi)
            }

            startActivityForResult(intent, 2)
        }
    }

    override fun initListener() {

    }
}
class ProjectVH(private val binding: RowProjectBinding) : RecyclerView.ViewHolder(binding.root){
    fun bind(data: Activitys?){
        data?.let {
            binding.startDate.text = Activitys.dateFormat.format(it.startDate)
            binding.endDate.text = if(it.endDate == null)
                ""
            else
                Activitys.dateFormat.format(it.endDate!!)

            binding.category.text =""
            binding.title.text =it.title
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
//class ProjectAdapter(private val data:Array<String>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//    private val category:ArrayList<String> = ArrayList()
//    init{
//        category.addAll(data)
//    }
//    inner class projectVH(private val binding:RowProjectBinding): RecyclerView.ViewHolder(binding.root){
//        fun bind(){
//        }
//    }
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        return TabOneVH(
//            RowBinding.inflate(
//                LayoutInflater.from(parent.context),
//                parent, false
//            ))
//    }
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        (holder as TabOneVH).bind()
//    }
//    override fun getItemCount(): Int {
//        return category.size
//    }
//}