package com.example.portfolio.view

import android.os.Bundle
import com.example.portfolio.R
import com.example.portfolio.base.BaseActivity
import com.example.portfolio.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
}