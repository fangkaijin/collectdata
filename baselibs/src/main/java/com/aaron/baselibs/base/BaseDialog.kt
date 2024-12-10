package com.aaron.baselibs.base

import android.app.Dialog
import android.content.Context
import android.view.WindowManager.LayoutParams
import androidx.viewbinding.ViewBinding

abstract class BaseDialog<T: ViewBinding>: Dialog {

    var bind: T? = null

    constructor(context: Context): super(context){
        initView(context)
    }

    constructor(context: Context, themeStyle: Int): super(context, themeStyle){
        initView(context)
    }


    private fun initView(context: Context){

        try{

            bind = getBinding()
            setContentView(bind!!.root)
            setCanceledOnTouchOutside(false)
            parseView()

            //界面大小
            val lp = this.window?.attributes
            lp!!.width = setWidth()
            lp!!.height = setHeight()

            lp!!.x = setX()
            lp!!.y = setY()

            lp!!.alpha = alpha()
            lp!!.dimAmount = dimAmount()

            lp!!.verticalMargin = 0.0F
            lp!!.horizontalMargin = 0.0F

            this.window!!.attributes = lp

        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun showLoding(){

        if(this.isShowing){
            hiddenLoading()
        }
        show()
    }

    fun hiddenLoading(){

        try{

            this.dismiss()
            this.cancel()
            bind = null;

        }catch (e: Exception){
            e.printStackTrace()
        }

    }

    abstract fun getBinding(): T
    abstract fun parseView()
    open fun alpha(): Float = 1.0F
    open fun dimAmount(): Float = 1.0F

    open fun setWidth(): Int = LayoutParams.MATCH_PARENT
    open fun setHeight(): Int = LayoutParams.MATCH_PARENT

    open fun setX(): Int = 0
    open fun setY(): Int = 0

    open fun commitSuccess() = Unit
    open fun commitFailed() = Unit

}