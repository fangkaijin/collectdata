package com.aaron.pichandle.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import androidx.core.content.FileProvider
import com.aaron.baselibs.base.BaseActivity
import com.aaron.baselibs.base.BaseApplication
import com.aaron.baselibs.utils.doScale
import com.aaron.baselibs.utils.showLog
import com.aaron.baselibs.utils.toast
import com.aaron.pichandle.databinding.ActivityPichandleBinding
import com.aaron.pichandle.utils.OpenCVUtils
import com.permissionx.guolindev.PermissionX
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import java.io.File

class PicHandleActivity: BaseActivity<ActivityPichandleBinding>() {

    init {

        System.loadLibrary("opencv_java4")

        val opencvInited = OpenCVLoader.initLocal()

        if(opencvInited){

            "OpenCV初始化成功".toast(BaseApplication.app)
        }

    }

    override fun getBinding(): ActivityPichandleBinding =
        ActivityPichandleBinding.inflate(LayoutInflater.from(this@PicHandleActivity))

    override fun parseView() {

        xBing?.picCap?.text = "请选择一张图片进行检测"
        xBing?.choicePIc?.setOnClickListener {

            //选择图片
            PermissionX.init(this@PicHandleActivity).permissions(
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

                        //全部通过授权
                        //去拍照
                        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
                        startActivityForResult(intent, 1)

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

    @SuppressLint("Range")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK && null != data) {

            try{


                val selectedImage: Uri? = data.getData();

                selectedImage?.let {


                    val picBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(it))

                    //图片信息
//                    val cursor = contentResolver.query(it, null, null, null, null, null)
//
//                    cursor?.let {
//
//                        if (it.moveToFirst() == true) {
//
//                            //图片大小
//                            val picSize = it.getLong(it.getColumnIndex(MediaStore.Images.ImageColumns.SIZE))
//                            "图片大小=$picSize".showLog()
//                            val path = it.getString(it.getColumnIndex(MediaStore.Images.ImageColumns.DATA))
//                            "图片路径=$path".showLog()
//                        }
//
//                    }


                    Observable.create<Double> {

                        val value = OpenCVUtils.getInstance().calcPicOfClarity(picBitmap)

                        it.onNext(value)
                    }.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {

                            xBing?.picCap?.text = "图片清晰度数值：${it.doScale()}";

                        }


                }

            }catch (e: Exception){
                e.printStackTrace()
            }

        }
    }
}