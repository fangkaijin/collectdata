package com.aaron.baselibs.camera

import android.app.Activity
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraProvider
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture

class CameraXHelper private constructor(){

    private lateinit var cameraProvider: ProcessCameraProvider

    //授权
    fun requestPermission(permission: String){



    }

    fun openCamera(context: Context, prewView: PreviewView){

        if(!(context is ComponentActivity)){

            return
        }

        if(null == cameraProvider){

            var cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({

                cameraProvider = cameraProviderFuture.get()
                startPrew(context, prewView)

            }, ContextCompat.getMainExecutor(context))
        } else {

            startPrew(context, prewView)
        }

    }

    private var cameraSelector: CameraSelector = CameraSelector.Builder()
        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
        .build();//当前相机

    private var imageCapture: ImageCapture = ImageCapture.Builder().build();

    private var prew: Preview = Preview.Builder()
        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
        .build()

    private fun startPrew(activity: ComponentActivity,
                          prewView: PreviewView){

        try{

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(activity,
                cameraSelector,
                prew,
                imageCapture
                )

            //设置用于预览的view
            prew.setSurfaceProvider(prewView.surfaceProvider)

        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    companion object{

        private var cameraX: CameraXHelper? = null
        fun getInstance(): CameraXHelper{
            return CameraXHelper.cameraX ?: synchronized(this){
                CameraXHelper.cameraX ?:CameraXHelper().also { CameraXHelper.cameraX = it }
            }

        }

    }
}