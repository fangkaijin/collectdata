package com.aaron.collectdata.bean

import android.bluetooth.BluetoothDevice

data class BleCollectData(val isError: Boolean, val eMsg: String = "", val name: String = "", val address: String = "", val otherDev: MutableSet<BluetoothDevice> = HashSet(16))
