package com.aaron.bluehandle.utils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import com.aaron.baselibs.base.BaseApplication
import com.aaron.baselibs.utils.toast

class BlueUtils{

    private var context: Context

    private constructor(context: Context){
        this.context = context
    }

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        blueToothManager.adapter

    }

    private val blueToothManager: BluetoothManager by lazy {
        BaseApplication.app.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    fun isSupportBlue(): Boolean{

        return null != bluetoothAdapter

    }

    fun isBlueOpen(): Boolean{

        return bluetoothAdapter.isEnabled
    }

    @SuppressLint("MissingPermission")
    fun findPairedBlue(callback: (pairedBlue: Set<BluetoothDevice>?)->Unit){
        val pairedBlue: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
        callback(pairedBlue)
    }

    @SuppressLint("MissingPermission")
    fun stopFindDevice(scanCallback: ScanCallback?){
        scanCallback?.let {
            bluetoothAdapter.bluetoothLeScanner.stopScan(it)
        }

        //"蓝牙设备搜索已经停止".toast(context)
    }

    @SuppressLint("MissingPermission")
    fun connectBlue(blueDev: BluetoothDevice, result: (message: String) -> Unit){

        blueDev?.let {

            it.connectGatt(context, false, object : BluetoothGattCallback(){

                override fun onConnectionStateChange(
                    gatt: BluetoothGatt?,
                    status: Int,
                    newState: Int
                ) {
                    super.onConnectionStateChange(gatt, status, newState)

                    result("onConnectionStateChange--status=$status-newState=$newState")
                }

                override fun onServiceChanged(gatt: BluetoothGatt) {
                    super.onServiceChanged(gatt)
                    result("onServiceChanged--")
                }

                override fun onCharacteristicChanged(
                    gatt: BluetoothGatt,
                    characteristic: BluetoothGattCharacteristic,
                    value: ByteArray
                ) {
                    super.onCharacteristicChanged(gatt, characteristic, value)
                    result("onCharacteristicChanged--")
                }

                override fun onCharacteristicRead(
                    gatt: BluetoothGatt,
                    characteristic: BluetoothGattCharacteristic,
                    value: ByteArray,
                    status: Int
                ) {
                    super.onCharacteristicRead(gatt, characteristic, value, status)
                    result("onCharacteristicRead--")
                }

                override fun onCharacteristicWrite(
                    gatt: BluetoothGatt?,
                    characteristic: BluetoothGattCharacteristic?,
                    status: Int
                ) {
                    super.onCharacteristicWrite(gatt, characteristic, status)

                    result("onCharacteristicWrite--")
                }

                override fun onDescriptorRead(
                    gatt: BluetoothGatt,
                    descriptor: BluetoothGattDescriptor,
                    status: Int,
                    value: ByteArray
                ) {
                    super.onDescriptorRead(gatt, descriptor, status, value)
                    result("onDescriptorRead--")
                }

                override fun onDescriptorWrite(
                    gatt: BluetoothGatt?,
                    descriptor: BluetoothGattDescriptor?,
                    status: Int
                ) {
                    super.onDescriptorWrite(gatt, descriptor, status)
                    result("onDescriptorWrite--")
                }

                override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
                    super.onMtuChanged(gatt, mtu, status)
                    result("onMtuChanged--")
                }

                override fun onPhyRead(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
                    super.onPhyRead(gatt, txPhy, rxPhy, status)
                    result("onPhyRead--")
                }

                override fun onPhyUpdate(
                    gatt: BluetoothGatt?,
                    txPhy: Int,
                    rxPhy: Int,
                    status: Int
                ) {
                    super.onPhyUpdate(gatt, txPhy, rxPhy, status)
                    result("onPhyUpdate--")
                }

                override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
                    super.onReadRemoteRssi(gatt, rssi, status)
                    result("onReadRemoteRssi--")
                }

                override fun onReliableWriteCompleted(gatt: BluetoothGatt?, status: Int) {
                    super.onReliableWriteCompleted(gatt, status)
                    result("onReliableWriteCompleted--")
                }

                override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                    super.onServicesDiscovered(gatt, status)
                    result("onServicesDiscovered--")
                }

            })
        }

    }
    @SuppressLint("MissingPermission")
    fun findDevices(findSuccess: (results: MutableList<ScanResult>?) -> Unit,
                    findFailed: (errorCode: Int) -> Unit,
                    getScanCallback: (scanCallback: ScanCallback) -> Unit){

        var scanCallback: ScanCallback = object : ScanCallback(){

            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                //super.onBatchScanResults(results)
                findSuccess(results)
            }

            override fun onScanFailed(errorCode: Int) {
                //super.onScanFailed(errorCode)
                findFailed(errorCode)
            }

            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                //super.onScanResult(callbackType, result)

                if(null!=result){

                    val results = mutableListOf<ScanResult>()
                    results.add(result)
                    findSuccess(results)

                } else{

                    findFailed(0x911110)
                }


            }
        }

        bluetoothAdapter.bluetoothLeScanner.startScan(scanCallback)

        getScanCallback(scanCallback)
    }


    companion object{

        private var instance: BlueUtils? = null

        @JvmStatic
        fun getInstance(context: Context): BlueUtils{
            return instance?: synchronized(this){
                instance?:BlueUtils(context).also { instance = it }
            }

        }

    }

}