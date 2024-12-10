package com.aaron.baselibs.base

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class BaseFragmentPagerAdapter(fragmentManager: FragmentManager,
                               val fragments: MutableList<Fragment>):
    FragmentPagerAdapter(fragmentManager){
    override fun getCount(): Int {

        return fragments?.size?:0
    }

    override fun getItem(position: Int): Fragment{

        return fragments.get(position)
    }


}