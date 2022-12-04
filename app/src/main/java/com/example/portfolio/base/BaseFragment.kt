package com.example.portfolio.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

abstract class BaseFragment<T: ViewDataBinding>(@LayoutRes private val layoutId: Int): Fragment() {
    private var _binding: T? = null
    protected val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        initView()
        initData()
        initListener()
        return binding.root
    }
    protected abstract fun initView()
    protected open fun initData() {}
    protected abstract fun initListener()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}