package com.aaron.petsocial.ui

import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import com.aaron.baselibs.base.BaseActivity
import com.aaron.baselibs.base.BaseFragmentPagerAdapter
import com.aaron.petsocial.R
import com.aaron.petsocial.databinding.ActivityHomeBinding
import com.aaron.petsocial.ui.fragment.HomeTab1Fragment
import com.aaron.petsocial.ui.fragment.HomeTab2Fragment
import com.aaron.petsocial.ui.fragment.HomeTab3Fragment
import com.aaron.petsocial.ui.fragment.HomeTab4Fragment

class HomeActivity : BaseActivity<ActivityHomeBinding>(){

    private val homeFragments = mutableListOf<Fragment>()
    override fun getBinding(): ActivityHomeBinding = ActivityHomeBinding.inflate(LayoutInflater.from(this@HomeActivity))

    override fun parseView() {

        initViewPage()

        changeTab(0)
        xBing?.tab1L?.setOnClickListener {

            changeTab(0)

        }

        xBing?.tab2L?.setOnClickListener {

            changeTab(1)

        }

        xBing?.tab3L?.setOnClickListener {

            //发布


        }

        xBing?.tab4L?.setOnClickListener {

            changeTab(3)

        }

        xBing?.tab5L?.setOnClickListener {

            changeTab(4)

        }


    }

    private fun initViewPage(){
        homeFragments.add(0, HomeTab1Fragment())
        homeFragments.add(1, HomeTab2Fragment())
        homeFragments.add(2, HomeTab3Fragment())
        homeFragments.add(3, HomeTab4Fragment())

        val pageAdapter = BaseFragmentPagerAdapter(supportFragmentManager, homeFragments)
        xBing?.homeVP?.adapter = pageAdapter
        xBing?.homeVP?.setCurrentItem(0, true)
        //禁止滑动
    }

    private fun changeTab(position: Int){

        when(position){

            0 -> {
                xBing?.tab1Iv?.setImageDrawable(resources.getDrawable(R.drawable.ic_tab_xq_choice))
                xBing?.tab1Tx?.setTextColor(resources.getColor(R.color.teal_200))

                xBing?.tab2Iv?.setImageDrawable(resources.getDrawable(R.drawable.ic_tab_gc_normal))
                xBing?.tab2Tx?.setTextColor(resources.getColor(R.color.color_33333))

                xBing?.tab4Iv?.setImageDrawable(resources.getDrawable(R.drawable.ic_tab_lt_normal))
                xBing?.tab4Tx?.setTextColor(resources.getColor(R.color.color_33333))

                xBing?.tab5Iv?.setImageDrawable(resources.getDrawable(R.drawable.ic_tab_zj_normal))
                xBing?.tab5Tx?.setTextColor(resources.getColor(R.color.color_33333))

                xBing?.homeVP?.setCurrentItem(0, true)
            }

            1 -> {
                xBing?.tab1Iv?.setImageDrawable(resources.getDrawable(R.drawable.ic_tab_xq_normal))
                xBing?.tab1Tx?.setTextColor(resources.getColor(R.color.color_33333))

                xBing?.tab2Iv?.setImageDrawable(resources.getDrawable(R.drawable.ic_tab_gc_choice))
                xBing?.tab2Tx?.setTextColor(resources.getColor(R.color.teal_200))

                xBing?.tab4Iv?.setImageDrawable(resources.getDrawable(R.drawable.ic_tab_lt_normal))
                xBing?.tab4Tx?.setTextColor(resources.getColor(R.color.color_33333))

                xBing?.tab5Iv?.setImageDrawable(resources.getDrawable(R.drawable.ic_tab_zj_normal))
                xBing?.tab5Tx?.setTextColor(resources.getColor(R.color.color_33333))
                xBing?.homeVP?.setCurrentItem(1, true)

            }

            3 -> {
                xBing?.tab1Iv?.setImageDrawable(resources.getDrawable(R.drawable.ic_tab_xq_normal))
                xBing?.tab1Tx?.setTextColor(resources.getColor(R.color.color_33333))

                xBing?.tab2Iv?.setImageDrawable(resources.getDrawable(R.drawable.ic_tab_gc_normal))
                xBing?.tab2Tx?.setTextColor(resources.getColor(R.color.color_33333))

                xBing?.tab4Iv?.setImageDrawable(resources.getDrawable(R.drawable.ic_tab_lt_choice))
                xBing?.tab4Tx?.setTextColor(resources.getColor(R.color.teal_200))

                xBing?.tab5Iv?.setImageDrawable(resources.getDrawable(R.drawable.ic_tab_zj_normal))
                xBing?.tab5Tx?.setTextColor(resources.getColor(R.color.color_33333))

                xBing?.homeVP?.setCurrentItem(2, true)
            }

            4 -> {
                xBing?.tab1Iv?.setImageDrawable(resources.getDrawable(R.drawable.ic_tab_xq_normal))
                xBing?.tab1Tx?.setTextColor(resources.getColor(R.color.color_33333))

                xBing?.tab2Iv?.setImageDrawable(resources.getDrawable(R.drawable.ic_tab_gc_normal))
                xBing?.tab2Tx?.setTextColor(resources.getColor(R.color.color_33333))

                xBing?.tab4Iv?.setImageDrawable(resources.getDrawable(R.drawable.ic_tab_lt_normal))
                xBing?.tab4Tx?.setTextColor(resources.getColor(R.color.color_33333))

                xBing?.tab5Iv?.setImageDrawable(resources.getDrawable(R.drawable.ic_tab_zj_choice))
                xBing?.tab5Tx?.setTextColor(resources.getColor(R.color.teal_200))

                xBing?.homeVP?.setCurrentItem(3, true)

            }

        }
    }


}