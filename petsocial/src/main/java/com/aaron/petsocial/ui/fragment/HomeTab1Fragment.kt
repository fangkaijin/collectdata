package com.aaron.petsocial.ui.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import com.aaron.baselibs.base.BaseFragment
import com.aaron.petsocial.databinding.LayoutFragmentTab1Binding

/**
 * 星球
 */
class HomeTab1Fragment: BaseFragment<LayoutFragmentTab1Binding>() {
    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): LayoutFragmentTab1Binding? = LayoutFragmentTab1Binding.inflate(inflater, container, false)

    override fun parseView(binding: LayoutFragmentTab1Binding?) {

    }
}