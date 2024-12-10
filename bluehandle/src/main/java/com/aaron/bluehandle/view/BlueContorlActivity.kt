package com.aaron.bluehandle.view

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.aaron.baselibs.base.BaseActivity
import com.aaron.baselibs.utils.toast
import com.aaron.bluehandle.databinding.ActivityBlueBinding
import com.aaron.bluehandle.utils.BlueUtils
import com.permissionx.guolindev.PermissionX
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class BlueContorlActivity: BaseActivity<ActivityBlueBinding>() {

    private var scanCallback: ScanCallback? = null
    private val findBlues: MutableSet<BluetoothDevice> = mutableSetOf()
    private var disposable: Disposable? = null

    override fun getBinding(): ActivityBlueBinding =
        ActivityBlueBinding.inflate(LayoutInflater.from(this@BlueContorlActivity))

    @SuppressLint("MissingPermission")
    override fun parseView() {

        xBing?.isSupport?.setOnClickListener {

            val isSupport = BlueUtils.getInstance(this@BlueContorlActivity).isSupportBlue()

            if(isSupport){
                "设备支持蓝牙".toast(this@BlueContorlActivity)
            } else {
                "设备不支持蓝牙".toast(this@BlueContorlActivity)
            }
        }

        xBing?.isOpen?.setOnClickListener {

            val isOpen = BlueUtils.getInstance(this@BlueContorlActivity).isBlueOpen()

            if(isOpen){
                "设备蓝牙已打开".toast(this@BlueContorlActivity)
            } else {
                "设备持蓝牙未打开".toast(this@BlueContorlActivity)
            }

        }

        xBing?.pairedBlue?.setOnClickListener {


            BlueUtils.getInstance(this@BlueContorlActivity).findPairedBlue {

                if(null!=it && !it.isEmpty()){

                    xBing?.pairedBlueL?.visibility = View.VISIBLE
                    xBing?.pairedBlueL?.removeAllViews()

                    for(device: BluetoothDevice in it){

                        val subView = TextView(this@BlueContorlActivity)
                        subView.append("已配对设备：${device.name}--${device.address}")
                        subView.textSize = 14F
                        subView.setTextColor(Color.parseColor("#333333"))
                        xBing?.pairedBlueL?.addView(subView, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
                    }

                }else{
                    xBing?.pairedBlueL?.visibility = View.GONE
                }

            }

        }

        xBing?.findDevice?.setOnClickListener {

            BlueUtils.getInstance(this@BlueContorlActivity).findDevices({

                if(null!=it && !it.isEmpty()){

                    //"扫描蓝牙成功-${it.size}个设备".toast(this@BlueContorlActivity)
                    xBing?.findBlueL?.visibility = View.VISIBLE

                    for(result: ScanResult in it){

                        result?.let {

                            findBlues.add(it.device)
                        }
                    }

                    if(null!=findBlues && !findBlues.isEmpty()){

                        xBing?.findBlueL?.removeAllViews()

                        for(device: BluetoothDevice in findBlues){

                            val subView = TextView(this@BlueContorlActivity)
                            subView.append("发现设备：${device.name}--${device.address}")
                            subView.textSize = 14F
                            subView.setTextColor(Color.parseColor("#333333"))
                            xBing?.findBlueL?.addView(subView, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
                        }
                    }


                } else{

                    xBing?.findBlueL?.visibility = View.GONE

                }
            }, {

                "扫描蓝牙失败-$it".toast(this@BlueContorlActivity)
                xBing?.findBlueL?.visibility = View.GONE
            }, {

                scanCallback = it

                //12秒后停止扫描
                disposable = Observable.timer(8, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                               BlueUtils.getInstance(this@BlueContorlActivity).stopFindDevice(scanCallback)
                        scanCallback = null
                    }, {
                               BlueUtils.getInstance(this@BlueContorlActivity).stopFindDevice(scanCallback)
                        scanCallback = null
                    })

            })

        }

        xBing?.connectDevice?.setOnClickListener {

            //设备连接
            if(null!=findBlues&&!findBlues.isEmpty()){

                val blueDev: BluetoothDevice = findBlues.take(1).get(0)
                BlueUtils.getInstance(this@BlueContorlActivity).connectBlue(blueDev,
                    {

                        it.toast(this@BlueContorlActivity)

                    })

            }

        }

    }

    override fun onDestroy() {
        disposable?.dispose()
        disposable = null

        if(null!=scanCallback){

            BlueUtils.getInstance(this@BlueContorlActivity).stopFindDevice(scanCallback)

        }
        super.onDestroy()
    }


}