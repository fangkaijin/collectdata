package com.aaron.baselibs.base

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.viewbinding.ViewBinding

abstract class BaseCustomView<T: ViewBinding>: FrameLayout {

    private var _binding: T? = null

    constructor(context: Context):
            super(context){
                initView(context)
    }

    constructor(context: Context, attrs: AttributeSet):
            super(context, attrs){
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int):
            super(context, attrs, defStyleAttr){
        initView(context)

    }

    private fun initView(context: Context){

        _binding = getBinding(LayoutInflater.from(context))

        parseView(_binding)
    }

    override fun onDetachedFromWindow() {

        _binding = null;
        super.onDetachedFromWindow()
    }

    abstract fun getBinding(inflater: LayoutInflater): T?
    abstract fun parseView(binding: T?)
}