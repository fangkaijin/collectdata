package com.aaron.collectdata.bean

data class GPSCollectData(val status: String = "GPS opened",
                          val satelliteCount: Int = 0,
                          val svid: Int = 0,
                          val lationType: Int = 0,
                          val latitude: Double = 0.0,
                          val longitude: Double = 0.0,
                          val accuracy: Float = 0.0F)
