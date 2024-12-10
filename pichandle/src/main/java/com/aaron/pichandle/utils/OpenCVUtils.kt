package com.aaron.pichandle.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc

class OpenCVUtils private constructor(){


    //计算图片清晰度
    fun calcPicOfClarity(picBitmap: Bitmap): Double{

        try{

            val origin: Mat = Mat()
            Utils.bitmapToMat(picBitmap, origin)

            val sobelX = Mat()
            val sobelY = Mat()

            Imgproc.Sobel(origin, sobelX, CvType.CV_32F, 1, 0)
            Imgproc.Sobel(origin, sobelY, CvType.CV_32F, 0, 1)

            return Math.sqrt(Math.pow(Core.norm(sobelX), 2.0) + Math.pow(Core.norm(sobelY), 2.0))

        }catch (e: Exception){
            e.printStackTrace()

            return 0.0
        }

    }


    companion object{

        private var instance: OpenCVUtils? = null

        @JvmStatic
        fun getInstance(): OpenCVUtils{
            return instance?: synchronized(this){
                instance?:OpenCVUtils().also { instance = it }
            }

        }

    }
}