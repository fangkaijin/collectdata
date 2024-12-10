package com.aaron.petsocial.ui

import android.view.LayoutInflater
import com.aaron.baselibs.base.BaseActivity
import com.aaron.petsocial.databinding.ActivitySplashBinding

class SplashActivity: BaseActivity<ActivitySplashBinding>() {
    override fun getBinding(): ActivitySplashBinding = ActivitySplashBinding.inflate(LayoutInflater.from(this@SplashActivity))

    override fun parseView() {

    }
}