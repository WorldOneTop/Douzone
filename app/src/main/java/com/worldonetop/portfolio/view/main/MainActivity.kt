package com.worldonetop.portfolio.view.main

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.worldonetop.portfolio.R
import com.worldonetop.portfolio.databinding.ActivityMainBinding
import com.worldonetop.portfolio.view.detail.AddActivity
import com.worldonetop.portfolio.view.intro.IntroActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var fragments:List<Fragment>
    private lateinit var pagerAdapter: FragmentStateAdapter
    private val viewModel: MainViewModel by viewModels()

    lateinit var activityResultLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        initData()
        initView()
        initListener()


//        "/download/SmartPortfolio/Portfolios/자료조사 보고서.docx"
//        FileUtil().openFileIntent(applicationContext, "자료조사 보고서.docx", null)?.let {
//            startActivity(it)
//        }
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.GetContent()){
//            it?.let {
//                FileUtil().downloadFile(applicationContext, it,null)
//            }

            it?.let {
//                Log.d("asd path",it.toFile().toString()) // 저장소 쓸수있는지

            }

        }
    }
    private fun initData(){
        // fragment data setting
        fragments = listOf(
            ProjectFragment.newInstance(),
            PortfolioFragment.newInstance(),
            QuestionFragment.newInstance(),
        )

        // fragment pager setting
        pagerAdapter = object: FragmentStateAdapter(this){
            override fun getItemCount(): Int = fragments.size
            override fun createFragment(position: Int): Fragment = fragments[position]
        }
    }
    private fun initView(){
        // appbar setting
        setSupportActionBar(binding.mainToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_home_24)

        // pager adapter setting
        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.registerOnPageChangeCallback(
            object: ViewPager2.OnPageChangeCallback(){
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    // appbar title change
                    supportActionBar?.title = getString(
                        when(position){
                            0 -> R.string.tab_activity
                            1 -> R.string.tab_portfolio
                            2 -> R.string.tab_qna
                            else -> R.string.tab_portfolio
                        }
                    )

                    // bottom FAB 컬러 설정
                    if(position==1)
                        binding.fabPortfolio.setColorFilter(ContextCompat.getColor(this@MainActivity, R.color.primaryDarkColor))
                    else
                        binding.fabPortfolio.setColorFilter(Color.WHITE) // 가운데 FAB unselected color

                    // bottom appbar select
                    binding.bottomNavigation.menu.getItem(position).isChecked = true
                }
            }
        )
        // fragment init page
        binding.viewPager.currentItem = 1
        binding.bottomNavigation.selectedItemId = R.id.page_portfolio
    }
    private fun initListener(){
        // 바텀 탭 이동
        binding.bottomNavigation.setOnItemSelectedListener {
            binding.fabPortfolio.setColorFilter(Color.WHITE) // 가운데 FAB unselected color
            when(it.itemId){
                R.id.page_activity -> binding.viewPager.currentItem = 0
                R.id.page_qna -> binding.viewPager.currentItem = 2
            }
            true
        }
        binding.fabPortfolio.setOnClickListener{
            binding.bottomNavigation.selectedItemId = R.id.page_portfolio
            binding.fabPortfolio.setColorFilter(ContextCompat.getColor(this, R.color.primaryDarkColor))
            binding.viewPager.currentItem = 1
        }

        binding.searchViewClose.setOnClickListener{
            binding.searchView.setText("")
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.appbar_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { // home or back button
                if(supportFragmentManager.backStackEntryCount > 1){ // back button
                    supportFragmentManager.popBackStack()
                    if(supportFragmentManager.backStackEntryCount == 1) { // back button 을 home 버튼으로
                        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_home_24)
                    }
                }else{ // home button
                    startActivity(
                        Intent(this@MainActivity, IntroActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    )
                }
                true
            }
            R.id.menu_add -> {
                startActivity(Intent(this@MainActivity, AddActivity::class.java))
                true
            }
            R.id.menu_remove -> {

                true
            }
            R.id.menu_search -> {
                if(binding.searchLayout.isVisible){
                    binding.searchLayout.startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.fadeout_up))
                    binding.searchLayout.visibility = View.GONE
                }else{
                    binding.searchLayout.visibility = View.VISIBLE
                    binding.searchLayout.startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.fadein_down))
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onBackPressed() {
//        if (!binding.searchView.isIconified) // 검색창 열려 있을 경우
//            binding.searchView.onActionViewCollapsed()
//        else
        if(supportFragmentManager.backStackEntryCount > 1) // 이전 프래그먼트가 있을 경우
            supportFragmentManager.popBackStack()
        else
            super.onBackPressed()
    }
}
