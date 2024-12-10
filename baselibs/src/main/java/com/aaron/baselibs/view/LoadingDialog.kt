package com.aaron.baselibs.view

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import com.aaron.baselibs.base.BaseDialog
import com.aaron.baselibs.databinding.LayoutDialogBinding

class LoadingDialog private constructor(context: Context) : BaseDialog<LayoutDialogBinding>(context) {

    override fun getBinding(): LayoutDialogBinding = LayoutDialogBinding.inflate(LayoutInflater.from(context))

    override fun alpha(): Float = 0.3F

    override fun dimAmount(): Float = 0.8F

    override fun parseView() {
        bind?.loadingTips?.setText("数据加载中...")
    }

    companion object{

        private var dialogInstance: LoadingDialog? = null

        fun getInstance(context: Context): LoadingDialog{
            return dialogInstance?: synchronized(this){
                dialogInstance?:LoadingDialog(context).also { dialogInstance = it }
            }

        }

    }


}