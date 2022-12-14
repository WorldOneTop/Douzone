package com.worldonetop.portfolio.view.main

import android.content.Intent
import android.graphics.Color
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.jakewharton.rxbinding4.widget.textChanges
import com.worldonetop.portfolio.R
import com.worldonetop.portfolio.base.BaseActivity
import com.worldonetop.portfolio.databinding.ActivityMainBinding
import com.worldonetop.portfolio.view.detail.DetailPortfolioActivity
import com.worldonetop.portfolio.view.detail.DetailProjectActivity
import com.worldonetop.portfolio.view.detail.DetailQuestionActivity
import com.worldonetop.portfolio.view.intro.IntroActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main){
    private val viewModel: MainViewModel by viewModels()
    private lateinit var fragments:List<Fragment>
    private lateinit var pagerAdapter: FragmentStateAdapter
    private lateinit var imm:InputMethodManager // soft keyboard manager
    private lateinit var fabVisibleAnimation: Animation
    private lateinit var fabInvisibleAnimation: Animation


    override fun initData(){
        // 키보드 매니저 세팅
        imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

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
        fabVisibleAnimation = AnimationUtils.loadAnimation(applicationContext,R.anim.floating_up)
        fabInvisibleAnimation = AnimationUtils.loadAnimation(applicationContext,R.anim.floating_down)
    }
    override fun initView(){
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

                    // select mode 종료
                    if(viewModel.selectMode.value == true)
                        viewModel.selectMode.value = false
                }
            }
        )
        // fragment init page
        binding.viewPager.currentItem = 1
        binding.bottomNavigation.selectedItemId = R.id.page_portfolio
    }
    override fun initListener(){
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

        // 검색의 X 버튼
        binding.searchViewClose.setOnClickListener{
            binding.searchLayout.startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.fadeout_up))
            binding.searchLayout.visibility = View.GONE
            binding.searchView.setText("")
            imm.hideSoftInputFromWindow(binding.searchView.windowToken,0)
        }

        // 검색 시작과 끝 사이의 표시
        binding.searchView.addTextChangedListener{
            if(binding.viewPager.alpha != 0.5f)
                setViewPagerAlpha(0.5f)
        }
        // 검색 text rx binding
        binding.searchView.textChanges()
            .debounce(700, TimeUnit.MILLISECONDS)// 마지막 글자 입력 0.7초 후에 onNext 이벤트로 데이터 스트리밍
            .subscribe{
                CoroutineScope(Dispatchers.Main).launch {// view 의 값을 부르기에 main thread 로
                    binding.viewPager.alpha = 1f
                    viewModel.setQuery(it.toString())
                }
            }

        // floating button
        binding.fabShare.setOnClickListener{
            viewModel.eventFloatingBtn.value = MainViewModel.Companion.Type.SHARE
        }
        binding.fabDelete.setOnClickListener{
            viewModel.eventFloatingBtn.value = MainViewModel.Companion.Type.DELETE
        }
        viewModel.selectMode.observe(this){
            if(it)
                visibleFloatingButtons()
            else
                invisibleFloatingButtons()
        }

    }

    // 검색 끝(paging load success)일 때 필요
    fun setViewPagerAlpha(alpha: Float){
        binding.viewPager.alpha = alpha
    }
    private fun visibleFloatingButtons(){
        binding.floatingButtonLayout.visibility = View.VISIBLE
        for(floating in binding.floatingButtonLayout.children){
            floating.startAnimation(fabVisibleAnimation)
        }
    }
    private fun invisibleFloatingButtons(){
        binding.floatingButtonLayout.startAnimation(fabInvisibleAnimation)
        binding.floatingButtonLayout.visibility = View.GONE
    }

    private fun convertDetailActivityIntent():Intent{
        return when(binding.viewPager.currentItem){
            0 -> Intent(this@MainActivity, DetailProjectActivity::class.java)
            1 -> Intent(this@MainActivity, DetailPortfolioActivity::class.java)
            else -> Intent(this@MainActivity, DetailQuestionActivity::class.java)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.appbar_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { // home or back button
                 startActivity(
                     Intent(this@MainActivity, IntroActivity::class.java)
                         .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                 )
                true
            }
            R.id.menu_add -> {
                doubleClick.run {
                    startActivity(convertDetailActivityIntent())
                }
                true
            }
            R.id.menu_search -> {
                if(binding.searchLayout.isVisible){
                    binding.searchLayout.startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.fadeout_up))
                    binding.searchLayout.visibility = View.GONE
                    binding.searchView.setText("")
                    imm.hideSoftInputFromWindow(binding.searchView.windowToken,0)
                }else{
                    binding.searchLayout.visibility = View.VISIBLE
                    binding.searchLayout.startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.fadein_down))
                    binding.searchView.requestFocus()
                    imm.showSoftInput(binding.searchView, 0)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onBackPressed() {
        if(viewModel.selectMode.value == true){
            viewModel.selectMode.value = false
        }
        else if(binding.searchLayout.isVisible){
            binding.searchLayout.startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.fadeout_up))
            binding.searchLayout.visibility = View.GONE
            binding.searchView.setText("")
            imm.hideSoftInputFromWindow(binding.searchView.windowToken,0)
        }
        else
            super.onBackPressed()
    }

}
