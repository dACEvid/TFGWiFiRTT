package com.davidperez.tfgwifirtt.model

import android.net.wifi.ScanResult

data class AccessPoint(
    val ssid: String,
    val bssid: String,
    val isWifiRTTCompatible: Boolean,
    val scanResultObject: ScanResult,
    var selectedForRTT: Boolean = false
)
