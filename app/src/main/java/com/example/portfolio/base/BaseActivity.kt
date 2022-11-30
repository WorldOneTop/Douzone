package com.example.portfolio.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.annotation.LayoutRes

abstract class BaseActivity<T: ViewDataBinding>(@LayoutRes private val layoutFile: Int): AppCompatActivity() {

    lateinit var binding: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, layoutFile)
        binding.lifecycleOwner = this

    }

}