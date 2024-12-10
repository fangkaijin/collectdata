package com.aaron.baselibs.base

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.aaron.baselibs.utils.showLog
import com.permissionx.guolindev.PermissionX

abstract class BaseActivity<out T: ViewBinding>: AppCompatActivity() {

    private lateinit var _binding: T

    val xBing: T?
        get() {
            try{

                if(this::_binding.isLateinit){

                    return this::_binding.get()
                }

                return null

            }catch (e: Exception){
                e.printStackTrace()
            }

            return null
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //沉浸
        transparentStatusBar()

        _binding = getBinding()
        setContentView(_binding.root)

        parseView()

    }

    //全屏
    private fun hideActionStatusBar(){

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        supportActionBar?.let {
            it.hide()
        }

        actionBar?.let {

            it.hide()
        }

    }

    private fun hideBottomStatusBar(){

        if(Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19){

            window.decorView.systemUiVisibility = View.GONE
        } else if(Build.VERSION.SDK_INT >= 19) {

            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN)

        }

    }

    //沉浸
    private fun transparentStatusBar(){

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            val option = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            val vis = window.getDecorView().getSystemUiVisibility();
            window.getDecorView().setSystemUiVisibility(option or vis);
            window.setStatusBarColor(Color.TRANSPARENT);
            setAndroidNativeLightStatusBar(true)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.setStatusBarColor(Color.TRANSPARENT)
            setAndroidNativeLightStatusBar(true)
        }

    }

    private fun setAndroidNativeLightStatusBar(isDark: Boolean){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            val decor = window.decorView
            decor?.let {

                if(isDark){
                    it.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
                } else{
                    it.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
                }

            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }


    override fun onDestroy() {
        super.onDestroy()
    }

    abstract fun getBinding(): T
    abstract fun parseView()

    fun requestPermission(vararg permissioms: String){

        PermissionX.init(this@BaseActivity)
            .permissions(
                *permissioms
            ).onExplainRequestReason{scope, deniedList ->

                scope.showRequestReasonDialog(deniedList,
                    "请授权必要权限",
                    "好的",
                    "取消")

            }.onForwardToSettings{scope, deniedList ->

                scope.showForwardToSettingsDialog(deniedList,
                    "你需要在设置中手动允许必要权限。",
                    "好的",
                    "取消")

            }
            .request{
                allGranted, grantedList, deniedList ->

                if(allGranted){

                    //全部通过授权
                    "权限全部通过".showLog()

                } else {

                    //未授权
                    if(null!=deniedList
                        && !deniedList.isEmpty()){

                        //处理拒绝的权限逻辑


                    }



                }
            }

    }

}