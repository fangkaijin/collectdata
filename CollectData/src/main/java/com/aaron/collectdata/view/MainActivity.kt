package com.aaron.collectdata.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.lifecycleScope
import com.aaron.baselibs.base.BaseActivity
import com.aaron.baselibs.utils.loadString
import com.aaron.collectdata.R
import com.aaron.collectdata.databinding.ActivityHomeBinding
import com.aaron.collectdata.utils.CollectUtils
import com.permissionx.guolindev.PermissionX
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit


class MainActivity : BaseActivity<ActivityHomeBinding>() {

    private var  collecting: Boolean = false;//判断是否采集
    private var disposable: Disposable? = null
    private var  lastRotateDegree: Float = 0F;
    private lateinit var photoFile: File

    override fun getBinding() =
        ActivityHomeBinding.inflate(LayoutInflater.from(this))

    override fun parseView() {

//        requestPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            android.Manifest.permission.READ_EXTERNAL_STORAGE,
//            android.Manifest.permission.READ_CONTACTS)

        xBing?.toArBtn?.setOnClickListener {

            //跳转到 ar 界面
            PermissionX.init(this@MainActivity).permissions(android.Manifest.permission.CAMERA,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE).onExplainRequestReason{scope, deniedList ->

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

                        endingCollect()
                        startActivity(Intent(this@MainActivity, ArActivity::class.java))

                    } else {

                        //未授权
                        if(null!=deniedList
                            && !deniedList.isEmpty()){

                            //处理拒绝的权限逻辑


                        }



                    }
                }


        }

        xBing?.collectBtn?.setOnClickListener {

            if(!collecting){

                xBing?.collectBtn?.text = loadString(R.string.home_btn_end)
                startCollect();

                collecting = true;
            } else {

                xBing?.collectBtn?.text = loadString(R.string.home_btn_start)
                endingCollect();

                collecting = false;
            }

        }

        xBing?.takePhoto?.setOnClickListener {

            PermissionX.init(this@MainActivity).permissions(android.Manifest.permission.CAMERA,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE).onExplainRequestReason{scope, deniedList ->

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

                        photoFile = File(this@MainActivity.getFilesDir().absolutePath + File.separator + System.currentTimeMillis()+".jpg")
                        //全部通过授权
                        //去拍照
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        val photoUri: Uri  = FileProvider.getUriForFile(
                                this,
                        getPackageName() + ".fileprovider",
                            photoFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                        startActivityForResult(intent, 2)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == 2 && resultCode == RESULT_OK && this::photoFile.isInitialized) {


            //intent跳转携带参数返回，data转化成Bitmap，获得的是个略缩图
            var photo: Bitmap? = BitmapFactory.decodeFile(photoFile.absolutePath)

            var rgbBuiler: StringBuilder = StringBuilder()

            rgbBuiler.append(loadString(R.string.label_34))
            rgbBuiler.append("\n\r")

            xBing?.imgW?.text = loadString(R.string.label_35)+photo?.width
            rgbBuiler.append(loadString(R.string.label_35)+photo?.width)
            rgbBuiler.append("\n")
            xBing?.imgH?.text = loadString(R.string.label_36)+photo?.height
            rgbBuiler.append(loadString(R.string.label_36)+photo?.height)
            rgbBuiler.append("\n")
            xBing?.imgConfig?.text = loadString(R.string.label_37)+photo?.config.toString()
            rgbBuiler.append(loadString(R.string.label_37)+photo?.config.toString())
            rgbBuiler.append("\n\r")

            CollectUtils.getInstance(this@MainActivity).savedata2File(CollectUtils.getInstance(this@MainActivity).picRgbPath,
                rgbBuiler.toString())

            val matrix: Matrix = Matrix();

            val ei: ExifInterface = ExifInterface(photoFile.absolutePath);
            var orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

            if(orientation == ExifInterface.ORIENTATION_ROTATE_90){

                matrix.postRotate(90F)
            } else if(orientation == ExifInterface.ORIENTATION_ROTATE_180){

                matrix.postRotate(180F)
            } else if(orientation == ExifInterface.ORIENTATION_ROTATE_270){

                matrix.postRotate(270F)
            } else {
                matrix.postRotate(0F)
            }


            var scale: Float = Math.max(128 / photo?.width!!.toFloat(), 128 / photo?.height!!.toFloat())

            matrix.postScale(scale, scale)

            //将预览图放进预览框
            xBing?.showPhoto?.setImageBitmap(Bitmap.createBitmap(photo!!, 0, 0, photo.getWidth(), photo.getHeight(), matrix, false));
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    @SuppressLint("MissingPermission")
    private fun startCollect(){

        //开始采集

        PermissionX.init(this@MainActivity).permissions(android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE).onExplainRequestReason{scope, deniedList ->

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

                    //写文件测试
                    //CollectUtils.getInstance(this@MainActivity).savedata2File(this@MainActivity.getExternalFilesDir("")!!.absolutePath+"/test.txt", "12345678677")

                    //设置文件路径
                    CollectUtils.getInstance(this@MainActivity).acceFilePath = ""
                    CollectUtils.getInstance(this@MainActivity).gyroscopeFilePath = "";
                    CollectUtils.getInstance(this@MainActivity).magnetometerFilePath = "";
                    CollectUtils.getInstance(this@MainActivity).gpsFilePath = "";
                    CollectUtils.getInstance(this@MainActivity).compassFilePath = ""
                    CollectUtils.getInstance(this@MainActivity).wifiFilePath = "";
                    CollectUtils.getInstance(this@MainActivity).bleFilePath = "";

                    //全部通过授权

                    lifecycleScope.launch(Dispatchers.IO) {

                        CollectUtils.getInstance(this@MainActivity).collectAcceData()
                        CollectUtils.getInstance(this@MainActivity).collectGyroscopeData()
                        CollectUtils.getInstance(this@MainActivity).collectMagnetometerData()
                        withContext(Dispatchers.Main){

                            CollectUtils.getInstance(this@MainActivity).collectGPS()

                        }
                        CollectUtils.getInstance(this@MainActivity).collectCompass()
                        CollectUtils.getInstance(this@MainActivity).collectBle()
                        CollectUtils.getInstance(this@MainActivity).bleSearch()

                        disposable = Observable.interval(1, 1, TimeUnit.SECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe{

                                if(it > 10){

                                    xBing?.collectBtn?.text = loadString(R.string.home_btn_start)
                                    endingCollect();

                                    collecting = false;

                                }

                                //加速度计数
                                var accData = CollectUtils.getInstance(this@MainActivity).getAcceData()

                                var acceBuiler: StringBuilder = StringBuilder()

                                acceBuiler.append(loadString(R.string.label_05)+(it+1)+loadString(R.string.label_06))
                                acceBuiler.append("\n\r")

                                xBing?.AccelerL?.visibility = View.VISIBLE
                                xBing?.accuracy?.text = loadString(R.string.label_01)+accData.accuracy
                                acceBuiler.append(loadString(R.string.label_01)+accData.accuracy)
                                acceBuiler.append("\n")
                                xBing?.x?.text = loadString(R.string.label_02)+accData.x
                                acceBuiler.append(loadString(R.string.label_02)+accData.x)
                                acceBuiler.append("\n")
                                xBing?.y?.text = loadString(R.string.label_03)+accData.y
                                acceBuiler.append(loadString(R.string.label_03)+accData.y)
                                acceBuiler.append("\n")
                                xBing?.z?.text = loadString(R.string.label_04)+accData.z
                                acceBuiler.append(loadString(R.string.label_04)+accData.z)
                                acceBuiler.append("\n\r")

                                //保存加速度数据
                                CollectUtils.getInstance(this@MainActivity).savedata2File(CollectUtils.getInstance(this@MainActivity).acceFilePath,
                                    acceBuiler.toString())


                                var gyroscopeData = CollectUtils.getInstance(this@MainActivity).getGyroscopeData()

                                var gyroscopeBuiler: StringBuilder = StringBuilder()

                                gyroscopeBuiler.append(loadString(R.string.label_05)+(it+1)+loadString(R.string.label_08))
                                gyroscopeBuiler.append("\n\r")

                                xBing?.gyroscopeL?.visibility = View.VISIBLE
                                xBing?.gyroscopeAcc?.text = loadString(R.string.label_07)+gyroscopeData.accuracy
                                gyroscopeBuiler.append(loadString(R.string.label_07)+gyroscopeData.accuracy)
                                gyroscopeBuiler.append("\n")
                                xBing?.gyroscopeX?.text = loadString(R.string.label_02)+gyroscopeData.x
                                gyroscopeBuiler.append(loadString(R.string.label_02)+gyroscopeData.x)
                                gyroscopeBuiler.append("\n")
                                xBing?.gyroscopeY?.text = loadString(R.string.label_03)+gyroscopeData.y
                                gyroscopeBuiler.append(loadString(R.string.label_03)+gyroscopeData.y)
                                gyroscopeBuiler.append("\n")
                                xBing?.gyroscopeZ?.text = loadString(R.string.label_04)+gyroscopeData.z
                                gyroscopeBuiler.append(loadString(R.string.label_04)+gyroscopeData.z)
                                gyroscopeBuiler.append("\n\r")

                                //保存陀螺仪数据
                                CollectUtils.getInstance(this@MainActivity).savedata2File(CollectUtils.getInstance(this@MainActivity).gyroscopeFilePath,
                                    gyroscopeBuiler.toString())


                                var magnetometerData = CollectUtils.getInstance(this@MainActivity).getMagnetometerData()
                                var magnetometerBuiler: StringBuilder = StringBuilder()

                                magnetometerBuiler.append(loadString(R.string.label_05)+(it+1)+loadString(R.string.label_10))
                                magnetometerBuiler.append("\n\r")
                                xBing?.magnetometerL?.visibility = View.VISIBLE
                                xBing?.magnetometerAcc?.text = loadString(R.string.label_09)+magnetometerData.accuracy
                                magnetometerBuiler.append(loadString(R.string.label_09)+magnetometerData.accuracy)
                                magnetometerBuiler.append("\n")
                                xBing?.magnetometerX?.text = loadString(R.string.label_02)+magnetometerData.x
                                magnetometerBuiler.append(loadString(R.string.label_02)+magnetometerData.x)
                                magnetometerBuiler.append("\n")
                                xBing?.magnetometerY?.text = loadString(R.string.label_03)+magnetometerData.y
                                magnetometerBuiler.append(loadString(R.string.label_03)+magnetometerData.y)
                                magnetometerBuiler.append("\n")
                                xBing?.magnetometerZ?.text = loadString(R.string.label_04)+magnetometerData.z
                                magnetometerBuiler.append(loadString(R.string.label_04)+magnetometerData.z)
                                magnetometerBuiler.append("\n\r")

                                CollectUtils.getInstance(this@MainActivity).savedata2File(CollectUtils.getInstance(this@MainActivity).magnetometerFilePath,
                                    magnetometerBuiler.toString())

                                var compassData = CollectUtils.getInstance(this@MainActivity).getCompassData();

                                var compassBuiler: StringBuilder = StringBuilder()

                                compassBuiler.append(loadString(R.string.label_05)+(it+1)+loadString(R.string.label_11))
                                compassBuiler.append("\n\r")
                                xBing?.compassL?.visibility = View.VISIBLE
                                xBing?.norPoint?.text = loadString(R.string.label_12)+compassData
                                compassBuiler.append(loadString(R.string.label_12)+compassData)
                                compassBuiler.append("\n\r")
                                CollectUtils.getInstance(this@MainActivity).savedata2File(CollectUtils.getInstance(this@MainActivity).compassFilePath,
                                    compassBuiler.toString())

                                if (Math.abs(compassData - lastRotateDegree) > 1) {
                                    val animation = RotateAnimation(
                                        lastRotateDegree,
                                        compassData,
                                        Animation.RELATIVE_TO_SELF,
                                        0.5f,
                                        Animation.RELATIVE_TO_SELF,
                                        0.5f
                                    )
                                    animation.fillAfter = true
                                    xBing?.compassIcon?.startAnimation(animation)
                                    lastRotateDegree = compassData
                                }


                                //wifi
                                var wifiInfo = CollectUtils.getInstance(this@MainActivity).collectWifi()
                                var wifiBuiler: StringBuilder = StringBuilder()

                                wifiBuiler.append(loadString(R.string.label_05)+(it+1)+loadString(R.string.label_13))
                                wifiBuiler.append("\n\r")
                                xBing?.wifiL?.visibility = View.VISIBLE
                                xBing?.wifiStste?.text = loadString(R.string.label_14)+wifiInfo.wifiState
                                wifiBuiler.append(loadString(R.string.label_14)+wifiInfo.wifiState)
                                wifiBuiler.append("\n")
                                xBing?.wifiSsid?.text = loadString(R.string.label_15)+wifiInfo.wifiInfo.ssid
                                wifiBuiler.append(loadString(R.string.label_15)+wifiInfo.wifiInfo.ssid)
                                wifiBuiler.append("\n")
                                xBing?.wifiBSsid?.text = loadString(R.string.label_16)+wifiInfo.wifiInfo.bssid
                                wifiBuiler.append(loadString(R.string.label_16)+wifiInfo.wifiInfo.bssid)
                                wifiBuiler.append("\n")
                                xBing?.wifiIpA?.text = loadString(R.string.label_17)+wifiInfo.wifiInfo.ipAddress
                                wifiBuiler.append(loadString(R.string.label_17)+wifiInfo.wifiInfo.ipAddress)
                                wifiBuiler.append("\n")
                                xBing?.wifiMac?.text = loadString(R.string.label_18)+wifiInfo.wifiInfo.macAddress
                                wifiBuiler.append(loadString(R.string.label_18)+wifiInfo.wifiInfo.macAddress)
                                wifiBuiler.append("\n")
                                xBing?.wifiLinkSpeed?.text = loadString(R.string.label_19)+wifiInfo.wifiInfo.linkSpeed
                                wifiBuiler.append(loadString(R.string.label_19)+wifiInfo.wifiInfo.linkSpeed)
                                wifiBuiler.append("\n")
                                xBing?.netWorkId?.text = loadString(R.string.label_20)+wifiInfo.wifiInfo.networkId
                                wifiBuiler.append(loadString(R.string.label_20)+wifiInfo.wifiInfo.networkId)
                                wifiBuiler.append("\n")
                                xBing?.wifiRssi?.text = loadString(R.string.label_21)+wifiInfo.wifiInfo.rssi
                                wifiBuiler.append(loadString(R.string.label_21)+wifiInfo.wifiInfo.rssi)
                                wifiBuiler.append("\n")
                                xBing?.wifiHiddenSsid?.text = loadString(R.string.label_22)+wifiInfo.wifiInfo.hiddenSSID
                                wifiBuiler.append(loadString(R.string.label_22)+wifiInfo.wifiInfo.hiddenSSID)
                                wifiBuiler.append("\n")
                                xBing?.wifiFrequency?.text = loadString(R.string.label_23)+wifiInfo.wifiInfo.frequency
                                wifiBuiler.append(loadString(R.string.label_23)+wifiInfo.wifiInfo.frequency)
                                wifiBuiler.append("\n\r")
                                CollectUtils.getInstance(this@MainActivity).savedata2File(CollectUtils.getInstance(this@MainActivity).wifiFilePath,
                                    wifiBuiler.toString())


                                //蓝牙
                                var bleInfo =  CollectUtils.getInstance(this@MainActivity).getBle()

                                var bleBuiler: StringBuilder = StringBuilder()

                                bleBuiler.append(loadString(R.string.label_05)+(it+1)+loadString(R.string.label_24))
                                bleBuiler.append("\n\r")

                                if(bleInfo.isError){
                                    xBing?.blueEMsg?.visibility = View.VISIBLE
                                    xBing?.bleL?.visibility = View.GONE
                                    xBing?.blueEMsg?.text = bleInfo.eMsg
                                    bleBuiler.append(bleInfo.eMsg)
                                    bleBuiler.append("\n\r")

                                    CollectUtils.getInstance(this@MainActivity).savedata2File(CollectUtils.getInstance(this@MainActivity).bleFilePath,
                                        bleBuiler.toString())
                                } else{
                                    xBing?.blueEMsg?.visibility = View.GONE
                                    xBing?.bleL?.visibility = View.VISIBLE

                                    xBing?.bleName?.text = loadString(R.string.label_25)+bleInfo.name;
                                    bleBuiler.append(loadString(R.string.label_25)+bleInfo.name)
                                    bleBuiler.append("\n")
                                    xBing?.bleAddr?.text = loadString(R.string.label_18)+bleInfo.address;
                                    bleBuiler.append(loadString(R.string.label_18)+bleInfo.address)
                                    bleBuiler.append("\n\r")

                                    if(null!=bleInfo.otherDev && !bleInfo.otherDev!!.isEmpty()){

                                        //添加
                                        xBing?.otherDev?.visibility = View.VISIBLE
                                        xBing?.otherDev?.removeAllViews();

                                        val view: TextView = TextView(this@MainActivity)

                                        bleBuiler.append(loadString(R.string.label_26));
                                        bleBuiler.append("\n")
                                        view.text = loadString(R.string.label_26);
                                        view.textSize = 16F
                                        view.setTextColor(Color.parseColor("#333333"))

                                        xBing?.otherDev?.addView(view, LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
                                        //添加
                                        for(dev in bleInfo.otherDev!!){

                                            val view: TextView = TextView(this@MainActivity)

                                            view.text = dev.name + "--"+dev.address

                                            bleBuiler.append(dev.name + "--"+dev.address)
                                            bleBuiler.append("\n")
                                            view.textSize = 14F
                                            view.setTextColor(Color.parseColor("#333333"))

                                            xBing?.otherDev?.addView(view, LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
                                        }

                                    } else {

                                        xBing?.otherDev?.visibility = View.GONE
                                    }

                                    CollectUtils.getInstance(this@MainActivity).savedata2File(CollectUtils.getInstance(this@MainActivity).bleFilePath,
                                        bleBuiler.toString())

                                }

                                //gps
                                val gpsData = CollectUtils.getInstance(this@MainActivity).getGpsData()

                                var gpsBuiler: StringBuilder = StringBuilder()

                                gpsBuiler.append(loadString(R.string.label_05)+(it+1)+loadString(R.string.label_27))
                                gpsBuiler.append("\n\r")

                                xBing?.gpsL?.visibility = View.VISIBLE
                                xBing?.gpsStatus?.text = gpsData.status
                                xBing?.satelliteCount?.text = loadString(R.string.label_28)+gpsData.satelliteCount
                                gpsBuiler.append(loadString(R.string.label_28)+gpsData.satelliteCount)
                                gpsBuiler.append("\n")
                                xBing?.svid?.text = loadString(R.string.label_29)+gpsData.svid
                                gpsBuiler.append(loadString(R.string.label_29)+gpsData.svid)
                                gpsBuiler.append("\n")
                                xBing?.lationType?.text = loadString(R.string.label_30)+gpsData.lationType
                                gpsBuiler.append(loadString(R.string.label_30)+gpsData.lationType)
                                gpsBuiler.append("\n")
                                xBing?.latitude?.text = loadString(R.string.label_31)+gpsData.latitude
                                gpsBuiler.append(loadString(R.string.label_31)+gpsData.latitude)
                                gpsBuiler.append("\n")
                                xBing?.longitude?.text = loadString(R.string.label_32)+gpsData.longitude
                                gpsBuiler.append(loadString(R.string.label_32)+gpsData.longitude)
                                gpsBuiler.append("\n")
                                xBing?.gpsAccuracy?.text = loadString(R.string.label_33)+gpsData.accuracy
                                gpsBuiler.append(loadString(R.string.label_33)+gpsData.accuracy)
                                gpsBuiler.append("\n\r")

                                CollectUtils.getInstance(this@MainActivity).savedata2File(CollectUtils.getInstance(this@MainActivity).gpsFilePath,
                                    gpsBuiler.toString())

                            }

                    }

                } else {

                    //未授权
                    if(null!=deniedList
                        && !deniedList.isEmpty()){

                        //处理拒绝的权限逻辑


                    }



                }
            }
    }

    private fun endingCollect(){

        disposable?.dispose()
        disposable = null
        //结束采集
        CollectUtils.getInstance(this@MainActivity).stopAcceData()
        CollectUtils.getInstance(this@MainActivity).stopGyroscopeData()
        CollectUtils.getInstance(this@MainActivity).stopMagnetometerData()
        CollectUtils.getInstance(this@MainActivity).stopGPS()
        CollectUtils.getInstance(this@MainActivity).stopCompassData()
        CollectUtils.getInstance(this@MainActivity).stopBle()
    }

    override fun onDestroy() {
        endingCollect();
        collecting = false;
        super.onDestroy()
    }
}