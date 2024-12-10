package com.aaron.baselibs.http

import android.content.Context
import com.aaron.baselibs.base.BaseApplication
import com.aaron.baselibs.utils.showLog
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.concurrent.TimeUnit

class OkHttpUtils private constructor(){

    suspend inline fun <reified T> requestPost(apiName: String,
                    params: JsonObject): T?{

        try {

            val request = Request.Builder()
                .url(rootUrl + apiName)
                .post(params.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = getOkClient().newCall(request)!!.execute();

            response?.let {
                it.body?.let {

                    it.string()?.let {


                        //解析
                        val isJsonObject = JsonParser().parse(it).isJsonObject();
                        val isJsonArray = JsonParser().parse(it).isJsonArray();

                        val gson = Gson()


                        if(isJsonObject){

                            return gson.fromJson(it, T::class.java)

                        } else if(isJsonArray){

                            return gson.fromJson<T>(it, object :TypeToken<T>(){}.type)

                        }

                        return null


                    }

                }


            }

        }catch (e: Exception){
            (e?.message?:"请求异常").showLog()
            e.printStackTrace()
        }

        return null
    }

    suspend inline fun <reified T> requestGet(apiName: String,
                                               params: JsonObject): T?{

        try {

            val urlRequest: HttpUrl.Builder  = (rootUrl+apiName).toHttpUrl()
                .newBuilder()

            if(null!=params && params.size() > 0){

                val map = params.asMap()

                map.forEach{

                    urlRequest.addQueryParameter(it.key, it.value.asString);

                }

            }

            val request: Request  = Request.Builder()
                .url(urlRequest.build())
                .build();

            val response = getOkClient().newCall(request)!!.execute();

            response?.let {
                it.body?.let {

                    it.string()?.let {


                        //解析
                        val isJsonObject = JsonParser().parse(it).isJsonObject();
                        val isJsonArray = JsonParser().parse(it).isJsonArray();

                        val gson = Gson()


                        if(isJsonObject){

                            return gson.fromJson(it, T::class.java)

                        } else if(isJsonArray){

                            return gson.fromJson<T>(it, object :TypeToken<T>(){}.type)

                        }

                        return null


                    }

                }


            }

        }catch (e: Exception){
            (e?.message?:"请求异常").showLog()
            e.printStackTrace()
        }

        return null
    }

    suspend inline fun <reified T> uploadFile(apiName: String,
                                              fileMap: Map<File, MediaType>): T?{

        try {

            val builder: MultipartBody.Builder = MultipartBody.Builder();

            fileMap.forEach{

                builder.addFormDataPart("reqFiles", it.key.getName(),
                    it.key.asRequestBody(it.value));
            }

            val requestBody: RequestBody  = builder
                    .setType(MultipartBody.FORM)
                .build();

            val request: Request  = Request.Builder()
                .url(rootUrl + apiName)
                .post(requestBody)
                .build();

            val response = getOkClient().newCall(request)!!.execute();

            response?.let {
                it.body?.let {

                    it.string()?.let {


                        //解析
                        val isJsonObject = JsonParser().parse(it).isJsonObject();
                        val isJsonArray = JsonParser().parse(it).isJsonArray();

                        val gson = Gson()


                        if(isJsonObject){

                            return gson.fromJson(it, T::class.java)

                        } else if(isJsonArray){

                            return gson.fromJson<T>(it, object :TypeToken<T>(){}.type)

                        }

                        return null


                    }

                }


            }

        }catch (e: Exception){
            (e?.message?:"请求异常").showLog()
            e.printStackTrace()
        }

        return null
    }

    suspend inline fun <reified T> downloadFile(fileUrl: String,
                                                file: File): T?{

        try {

            val request: Request  = Request.Builder().get().url(fileUrl).build();

            val response = getOkClient().newCall(request)!!.execute();

            response?.let {
                it.body?.let {

                    it.string()?.let {


                        //解析
                        val isJsonObject = JsonParser().parse(it).isJsonObject();
                        val isJsonArray = JsonParser().parse(it).isJsonArray();

                        val gson = Gson()


                        if(isJsonObject){

                            return gson.fromJson(it, T::class.java)

                        } else if(isJsonArray){

                            return gson.fromJson<T>(it, object :TypeToken<T>(){}.type)

                        }

                        return null


                    }

                }


            }

        }catch (e: Exception){
            (e?.message?:"请求异常").showLog()
            e.printStackTrace()
        }

        return null
    }




    companion object{

        private var okInstance: OkHttpUtils? = null

        private var okClient: OkHttpClient? = null

        fun getInstance(): OkHttpUtils{
            return okInstance?: synchronized(this){
                okInstance?:OkHttpUtils().also { okInstance = it }
            }

        }

        const val rootUrl = "https://uapis.cn/"

        fun getOkClient(): OkHttpClient{

            return okClient?: synchronized(this){
                okClient?: OkHttpClient.Builder()
                    .cache(Cache( File(BaseApplication.app.cacheDir.absolutePath+ File.separator + "cache" + File.separator), 10*1024*1024))
                    .callTimeout(60, TimeUnit.SECONDS)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .connectionPool(ConnectionPool(32, 5, TimeUnit.MINUTES))
                    .retryOnConnectionFailure(true)
                    .addNetworkInterceptor(HttpLoggingInterceptor( HttpLoggingInterceptor.Logger() {

                        ("接口请求=="+it).showLog()

                    }).setLevel(HttpLoggingInterceptor.Level.BODY))
                    .build().also { okClient = it }
            }

        }

    }

}