package com.aaron.baselibs.base

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

abstract class BaseFragmentStateAdapter<E: Any>(val datas: MutableList<E>, fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {

        return datas?.let {
            it.size
        }?: 0
    }

    override fun createFragment(position: Int): Fragment {

        return getFragment(datas.get(position))
    }

    abstract fun getFragment(fragmentTag: E): Fragment
}