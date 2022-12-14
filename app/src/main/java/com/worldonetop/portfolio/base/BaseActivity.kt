package com.worldonetop.portfolio.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.worldonetop.portfolio.util.DoubleClick

abstract class BaseActivity<T: ViewDataBinding>(@LayoutRes private val layoutFile: Int): AppCompatActivity() {

    lateinit var binding: T

    protected val doubleClick by lazy { // 연속 클릭으로 인한 중복 실행 방지
        DoubleClick(lifecycle)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, layoutFile)
        binding.lifecycleOwner = this

        initData()
        initView()
        initListener()
    }
    protected abstract fun initData()
    protected abstract fun initView()
    protected abstract fun initListener()

}