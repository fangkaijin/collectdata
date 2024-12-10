package com.aaron.collectdata.utils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.icu.text.SimpleDateFormat
import android.location.GnssMeasurementsEvent
import android.location.GnssMeasurementsEvent.Callback
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.TextUtils
import com.aaron.baselibs.utils.showLog
import com.aaron.collectdata.bean.AccelerometerBean
import com.aaron.collectdata.bean.BleCollectData
import com.aaron.collectdata.bean.GPSCollectData
import com.aaron.collectdata.bean.WifiCollectData
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import okio.BufferedSink
import okio.Okio
import okio.Sink
import okio.buffer
import okio.sink
import java.io.File
import java.util.Date

class CollectUtils {

    private var context: Context;
    private constructor(context: Context){

        this.context = context;

    }

    //传感器
    private lateinit var sensorManager: SensorManager
    //加速度
    private lateinit var  accSensor: Sensor
    private var acceBean : AccelerometerBean = AccelerometerBean()
    private lateinit var acceListener: SensorEventListener

    //陀螺仪
    private lateinit var  gyroscopeSensor: Sensor
    private var gyroscopeBean : AccelerometerBean = AccelerometerBean()
    private lateinit var gyroscopeListener: SensorEventListener

    //磁力
    private lateinit var  magnetometerSensor: Sensor
    private var magnetometerBean : AccelerometerBean = AccelerometerBean()
    private lateinit var magnetometerListener: SensorEventListener

    //gps
    private lateinit var locationManager: LocationManager
    private lateinit var gnssMeasurementsEvent: GnssMeasurementsEvent.Callback
    private lateinit var locationListener: LocationListener
    private lateinit var mGnssStatusCallback: GnssStatus.Callback
    private var gpsCollectData: GPSCollectData = GPSCollectData()
    private var gpslatitude: Double = 0.0
    private var gpslongitude: Double = 0.0
    private var gpsaccuracy: Float = 0.0F

    //罗盘 compass
    private lateinit var compassListener: SensorEventListener
    private var norPoint: Float = -1F;
    private var accelerometerValues: FloatArray = FloatArray(3)
    private var magneticValues: FloatArray = FloatArray(3)

    //wifi
    private lateinit var wifiManager: WifiManager

    //蓝牙
    private val ble: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var bleCollectData = BleCollectData(false);
    //private lateinit var bluetoothManager: BluetoothManager
    private lateinit var scanCallback: ScanCallback

    fun getAcceData(): AccelerometerBean{

        return  acceBean

    }

    var acceFilePath: String = ""
        set(value) {
            field = context.getExternalFilesDir("")!!.absolutePath+File.separator+"Acceleration_data_acquisition"+formatDate()+".txt";
        }
        get() {
            return field
        }

    fun getCompassData(): Float{

        return  norPoint

    }

    var compassFilePath: String = ""
        set(value) {
            field = context.getExternalFilesDir("")!!.absolutePath+File.separator+"Compass_data_acquisition"+formatDate()+".txt";
        }
        get() {
            return field
        }

    @SuppressLint("MissingPermission")
    fun bleSearch(){

        ble?.let {

            if(!ble.isEnabled) return

            scanCallback = object : ScanCallback(){

                override fun onScanFailed(errorCode: Int) {
                    super.onScanFailed(errorCode)
                }

                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    super.onScanResult(callbackType, result)

                    if(null!=result && null != result.device){

                        bleCollectData.otherDev.add(result.device)
                        bleCollectData = BleCollectData(false, "", bleCollectData.name, bleCollectData.address, bleCollectData.otherDev)
                    }
                }

                override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                    super.onBatchScanResults(results)
                }
            }

            ble.bluetoothLeScanner.startScan(scanCallback)

        }
    }

    fun getBle(): BleCollectData{

        return  bleCollectData

    }

    var bleFilePath: String = ""
        set(value) {
            field = context.getExternalFilesDir("")!!.absolutePath+File.separator+"Bluetooth_data_acquisition"+formatDate()+".txt";
        }
        get() {
            return field
        }

    fun getGyroscopeData(): AccelerometerBean{

        return  gyroscopeBean

    }

    var gyroscopeFilePath: String = ""
        set(value) {
            field = context.getExternalFilesDir("")!!.absolutePath+File.separator+"Gyroscope_data_acquisition."+formatDate()+".txt";
        }
        get() {
            return field
        }

    fun getMagnetometerData(): AccelerometerBean{

        return  magnetometerBean

    }

    var magnetometerFilePath: String = ""
        set(value) {
            field = context.getExternalFilesDir("")!!.absolutePath+File.separator+"Magnetometer_data_acquisition"+formatDate()+".txt";
        }
        get() {
            return field
        }

    fun getGpsData(): GPSCollectData{

        return gpsCollectData
    }

    var gpsFilePath: String = ""
        set(value) {
            field = context.getExternalFilesDir("")!!.absolutePath+File.separator+"GPS_data_acquisition"+formatDate()+".txt";
        }
        get() {
            return field
        }

    @SuppressLint("MissingPermission")
    fun collectBle(){

        if(null == ble){
            //设备不支持蓝牙

            bleCollectData = BleCollectData(true, "The device does not support Bluetooth.")
        }

        if(!ble.isEnabled){

            //蓝牙未打开
            bleCollectData = BleCollectData(true, "Bluetooth is not turned on")
            return
        }

        bleCollectData = BleCollectData(false, "", ble.name, ble.address)
    }

    var wifiFilePath: String = ""

        set(value) {
            field = context.getExternalFilesDir("")!!.absolutePath+File.separator+"Wifi_data_acquisition"+formatDate()+".txt";
        }
        get() {
            return field
        }

    val picRgbPath: String
        get() {
            return context.getExternalFilesDir("")!!.absolutePath+File.separator+"Picture_data_acquisition.txt";
        }

    fun collectWifi(): WifiCollectData {

        wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

        //wifi 状态
        var wifiState = wifiManager.wifiState
        //获取 WiFi 的详细信息
        var wifiInfo = wifiManager.connectionInfo

        var wifiStateStr = ""

        if(wifiState == WifiManager.WIFI_STATE_ENABLED){
            wifiStateStr = "wifi opened";
        } else if(wifiState == WifiManager.WIFI_STATE_DISABLED){
            wifiStateStr = "wifi closed";
        } else if(wifiState == WifiManager.WIFI_STATE_ENABLING){
            wifiStateStr = "wifi opening";
        } else if(wifiState == WifiManager.WIFI_STATE_DISABLING){
            wifiStateStr = "wifi closing";
        } else{
            wifiStateStr = "wifi unknown";
        }

        return WifiCollectData(wifiStateStr, wifiInfo)

    }

    private fun formatDate(): String{

        try{

            val format: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS")
            return format.format(Date())

        }catch (e: Exception){
            e.printStackTrace()


            return ""+System.currentTimeMillis();
        }
    }

    @SuppressLint("MissingPermission")
    fun collectGPS(){

        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        mGnssStatusCallback = object : GnssStatus.Callback(){

            override fun onSatelliteStatusChanged(status: GnssStatus) {
                super.onSatelliteStatusChanged(status)

                var satelliteCount: Int = status.getSatelliteCount();
                var sat_id = status.getSvid(1);
                var constellationType = status.getConstellationType(1);
//                v = " Scan: "+ (count)+  " ,Sat-count=" +(satelliteCount) +"id="+ sat_id +" type"+  constellationType + " ,Eacc= "+accuracy+ "\n\n";
//                v += tv1.getText();
//                tv1.setText(v);
//                count= count + 1;

                gpsCollectData = GPSCollectData(gpsCollectData.status, satelliteCount, sat_id, constellationType, gpslatitude, gpslongitude, gpsaccuracy)


            }
        }

        locationManager.registerGnssStatusCallback(mGnssStatusCallback);

        gnssMeasurementsEvent = object : Callback(){

            override fun onGnssMeasurementsReceived(eventArgs: GnssMeasurementsEvent?) {
                super.onGnssMeasurementsReceived(eventArgs)

                "GNNS 监测回调".showLog()

            }

            override fun onStatusChanged(status: Int) {
                super.onStatusChanged(status)
            }
        }

        locationManager.registerGnssMeasurementsCallback(gnssMeasurementsEvent);

        locationListener = object : LocationListener{
            override fun onLocationChanged(location: Location) {

                if(null!=location){
                    gpslatitude = location.getLatitude();
                    gpslongitude = location.getLongitude();
                    gpsaccuracy = location.getAccuracy();
                }

            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                "onStatusChanged".showLog()
            }

            override fun onProviderDisabled(provider: String) {
                "onProviderDisabled".showLog()
                gpsCollectData = GPSCollectData(provider+"disabled")
            }

            override fun onProviderEnabled(provider: String) {
                "onProviderEnabled".showLog()
                gpsCollectData = GPSCollectData(provider+"opened")
            }

            override fun onFlushComplete(requestCode: Int) {
                "onFlushComplete".showLog()
            }

            override fun onLocationChanged(locations: MutableList<Location>) {
                "onLocationChanged".showLog()
            }

        }

//        var criteria: Criteria = Criteria()
//        criteria.accuracy = Criteria.ACCURACY_COARSE
//        criteria.isAltitudeRequired = true
//        criteria.isBearingRequired = true
//        criteria.isCostAllowed = true
//        criteria.powerRequirement  = Criteria.POWER_LOW
//        val bestProvider: String? = locationManager.getBestProvider(criteria, true)

        val location: Location? = locationManager?.getLastKnownLocation((LocationManager.NETWORK_PROVIDER))

        location?.let {

            gpslatitude = it.getLatitude();
            gpslongitude = it.getLongitude();
            gpsaccuracy = it.getAccuracy();
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000L, 0F, locationListener)
    }

    //获取加速度计数据
    fun collectAcceData(){

        try{

            acceListener = object: SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {

                    ("当前加速器数据--x0="+event!!.values[0]+"x1="+event!!.values[1]+"x2="+event!!.values[2]+"accuracy-"+event.accuracy).showLog()

                    acceBean = AccelerometerBean(event.accuracy,
                        Math.round(event.values[0] * 100).toFloat(),
                        Math.round(event.values[1] * 100).toFloat(),
                        Math.round(event.values[2] * 100).toFloat())

                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

                }
            }

            //获取传感器管理器
            sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            //获取加速度传感器
            accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
            //监听
            sensorManager.registerListener(acceListener, accSensor, SensorManager.SENSOR_DELAY_NORMAL)

        }catch (e: Exception){

            e.printStackTrace();
        }


    }

    //获取罗盘数据
    fun collectCompass(){

        try{

            compassListener = object: SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {

                    // 判断当前是加速度传感器还是地磁传感器
                    // 判断当前是加速度传感器还是地磁传感器
                    if (event!!.sensor.type === Sensor.TYPE_ACCELEROMETER) {
                        // 通过clone()获取不同的values引用
                        accelerometerValues = event!!.values.clone()
                    } else if (event!!.sensor.type === Sensor.TYPE_MAGNETIC_FIELD) {
                        magneticValues = event!!.values.clone()
                    }

                    //获取地磁与加速度传感器组合的旋转矩阵

                    //获取地磁与加速度传感器组合的旋转矩阵
                    val R = FloatArray(9)
                    val values = FloatArray(3)
                    SensorManager.getRotationMatrix(
                        R, null, accelerometerValues,
                        magneticValues
                    )
                    SensorManager.getOrientation(R, values)

                    //values[0]->Z轴、values[1]->X轴、values[2]->Y轴
                    //使用前请进行转换，因为获取到的值是弧度，示例如下
                    //        Math.toDegrees(values[0]);
                    //        Math.toDegrees(values[1]);
                    //        Math.toDegrees(values[2]);


                    norPoint = -(Math.toDegrees(values[0].toDouble()).toFloat())


                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

                }
            }

            //获取传感器管理器
            sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            //获取加速度传感器
            val sensor1 : Sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
            val sensor2 : Sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)!!
            //监听
            sensorManager.registerListener(compassListener, sensor1, SensorManager.SENSOR_DELAY_GAME)
            sensorManager.registerListener(compassListener, sensor2, SensorManager.SENSOR_DELAY_GAME)

        }catch (e: Exception){

            e.printStackTrace();
        }

    }

    //获取陀螺仪数据
    fun collectGyroscopeData(){

        try{

            gyroscopeListener = object: SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {

                    ("当前陀螺仪数据--x0="+event!!.values[0]+"x1="+event!!.values[1]+"x2="+event!!.values[2]+"accuracy-"+event.accuracy).showLog()

                    gyroscopeBean = AccelerometerBean(event.accuracy,
                        Math.round(event.values[0] * 100).toFloat(),
                        Math.round(event.values[1] * 100).toFloat(),
                        Math.round(event.values[2] * 100).toFloat())

                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

                }
            }

            //获取传感器管理器
            sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            //获取加速度传感器
            gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)!!
            //监听
            sensorManager.registerListener(gyroscopeListener, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL)

        }catch (e: Exception){

            e.printStackTrace();
        }


    }

    //磁力
    fun collectMagnetometerData(){

        try{

            magnetometerListener = object: SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {

                    ("当前陀螺仪数据--x0="+event!!.values[0]+"x1="+event!!.values[1]+"x2="+event!!.values[2]+"accuracy-"+event.accuracy).showLog()

                    magnetometerBean = AccelerometerBean(event.accuracy,
                        Math.round(event.values[0] * 100).toFloat(),
                        Math.round(event.values[1] * 100).toFloat(),
                        Math.round(event.values[2] * 100).toFloat())

                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

                }
            }

            //获取传感器管理器
            sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            //获取加速度传感器
            magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)!!
            //监听
            sensorManager.registerListener(magnetometerListener, magnetometerSensor, SensorManager.SENSOR_DELAY_NORMAL)

        }catch (e: Exception){

            e.printStackTrace();
        }


    }

    fun stopGPS(){

        try{

            if(this::gnssMeasurementsEvent.isInitialized) locationManager.unregisterGnssMeasurementsCallback(gnssMeasurementsEvent);
            locationManager.removeUpdates(locationListener)
            if(this::mGnssStatusCallback.isInitialized) locationManager.unregisterGnssStatusCallback(mGnssStatusCallback);

        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun stopGyroscopeData(){

        try{

            if(this::gyroscopeListener.isInitialized) sensorManager.unregisterListener(gyroscopeListener)

        }catch (e: Exception){
            e.printStackTrace()
        }

    }

    fun stopMagnetometerData(){

        try{

            if(this::magnetometerListener.isInitialized) sensorManager.unregisterListener(magnetometerListener)

        }catch (e: Exception){
            e.printStackTrace()
        }

    }

    fun stopAcceData(){

        try{

            if(this::acceListener.isInitialized) sensorManager.unregisterListener(acceListener)

        }catch (e: Exception){
            e.printStackTrace()
        }

    }

    @SuppressLint("MissingPermission")
    fun stopBle(){
        try{

            if(this::scanCallback.isInitialized) ble.bluetoothLeScanner.stopScan(scanCallback)

        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun stopCompassData(){

        try{

            if(this::compassListener.isInitialized) sensorManager.unregisterListener(compassListener)

        }catch (e: Exception){
            e.printStackTrace()
        }

    }

    fun savedata2File(filePath: String, data: String){

        if(TextUtils.isEmpty(filePath) || TextUtils.isEmpty(data)) return

        Observable.create<Boolean> {

            var writeSink: Sink? = null;
            var bufferedSink: BufferedSink? = null;

            try{

                var file: File = File(filePath)
                if(!file.parentFile.exists()){
                    file.parentFile.mkdirs()
                }

                if(!file.exists()){
                    file.parentFile.createNewFile()
                }

                //获取 sink
                writeSink = file.sink(append = true)
                bufferedSink = writeSink.buffer()
                //追加写入数据
                bufferedSink.writeUtf8(data)



            }catch (e: Exception){
                e.printStackTrace()
            } finally {

                try{

                    bufferedSink?.close()
                    writeSink?.close()

                }catch (e: Exception){
                    e.printStackTrace()
                }
            }


        }.subscribeOn(Schedulers.io())
            .subscribe()

    }


    companion object{

        private var instance: CollectUtils? = null

        @JvmStatic
        fun getInstance(context: Context): CollectUtils{
            return instance?: synchronized(this){
                instance?:CollectUtils(context).also { instance = it }
            }

        }

    }

}