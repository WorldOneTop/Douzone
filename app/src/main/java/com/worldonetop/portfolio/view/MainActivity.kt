package com.worldonetop.portfolio.view

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import com.worldonetop.portfolio.R
import com.worldonetop.portfolio.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        initView()
        initListener()

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val fragment = PortfolioFragment()
        fragmentTransaction.add(R.id.fragmentContainer, fragment)
        fragmentTransaction.commit()
    }
    private fun initView(){
        setSupportActionBar(binding.mainToolbar)

        // bottom appbar setting
        binding.bottomNavigation.selectedItemId = R.id.page_portfolio
        // initFragment setting
    }
    private fun initListener(){
        binding.bottomNavigation.setOnItemSelectedListener {
            when(it.itemId){
                R.id.page_project ->{
                    binding.fabPortfolio.setColorFilter(Color.WHITE)

                }R.id.page_portfolio ->{
                    binding.fabPortfolio.setColorFilter(resources.getColor(R.color.primaryDarkColor,null))

                }R.id.page_qna ->{
                    binding.fabPortfolio.setColorFilter(Color.WHITE)

                }
            }
            true
        }
        binding.fabPortfolio.setOnClickListener{
            binding.bottomNavigation.selectedItemId = R.id.page_portfolio
        }

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { // 서랍 버튼
                if(binding.rootLayout.isDrawerOpen(GravityCompat.START))
                    binding.rootLayout.closeDrawer(GravityCompat.START)
                else
                    binding.rootLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onBackPressed() {
        if (!binding.searchView.isIconified)
            binding.searchView.isIconified = true
        else
            super.onBackPressed()
    }
}
