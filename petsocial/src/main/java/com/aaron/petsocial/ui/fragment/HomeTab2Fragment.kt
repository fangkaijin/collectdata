package com.aaron.petsocial.ui.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import com.aaron.baselibs.base.BaseFragment
import com.aaron.petsocial.databinding.LayoutFragmentTab2Binding

/**
 * 广场
 */
class HomeTab2Fragment: BaseFragment<LayoutFragmentTab2Binding>() {
    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): LayoutFragmentTab2Binding? = LayoutFragmentTab2Binding.inflate(inflater, container, false)

    override fun parseView(binding: LayoutFragmentTab2Binding?) {

    }
}