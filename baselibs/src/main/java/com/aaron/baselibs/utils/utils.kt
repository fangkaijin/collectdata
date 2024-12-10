package com.aaron.baselibs.utils

import android.content.Context
import android.graphics.Point
import android.util.Log
import android.util.Size
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.StringRes
import com.aaron.baselibs.view.LoadingDialog
//import io.reactivex.Observable
//import io.reactivex.android.schedulers.AndroidSchedulers
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date

fun View.toast(msg: String){
    this?.let {


        Toast.makeText(it.context, msg, Toast.LENGTH_SHORT).show()

    }
}

fun String.toast(context: Context){
    context?.let {

//        Observable.create<Unit> {
//
//
//        }.subscribeOn(AndroidSchedulers.mainThread()).subscribe()

        Toast.makeText(context, this, Toast.LENGTH_SHORT).show()

    }

}

fun View.showLoading(dialog: LoadingDialog, isShow: Boolean){

    this?.let {

        if(isShow) {
            dialog.showLoding()
        } else{
            dialog.hiddenLoading()
        }

    }
}

fun String.showLog(){
    this?.let {
        Log.d("fangkaijin", it?:"无log")
    }
}

fun Any.showLog(log: String){

    this?.let {
        Log.d("fangkaijin", log?:"无log")
    }

}

fun Double.doScale(scale: Int = 3): Double{
    val bigDecimal: BigDecimal = BigDecimal(this)
    return bigDecimal.setScale(scale, BigDecimal.ROUND_HALF_UP).toDouble()
}

fun Context.getScreenSize(): Point{

    this?.let {

        val point = Point()
        val wm = this.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay

        display.getSize(point)

        return point

    }

    return Point()

}

fun Date.fromat(format: String = "yyyy-MM-dd HH:mm:ss"): String{

    val format: SimpleDateFormat = SimpleDateFormat(format)
    return format.format(this)

}

fun Context.loadString(@StringRes id: Int): String{

    return this.resources.getString(id)
}