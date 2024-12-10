package com.aaron.baselibs.base

import android.app.Application

abstract class BaseApplication(): Application() {

    override fun onCreate() {
        super.onCreate()

        app = this

        //闪退捕获
        initSdk()
    }

    abstract fun initSdk()

    companion object{

        lateinit var app: Application

    }
}