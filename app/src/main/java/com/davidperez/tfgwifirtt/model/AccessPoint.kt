package com.davidperez.tfgwifirtt.model

import android.net.wifi.ScanResult

data class AccessPoint(
    val ssid: String,
    val bssid: String,
    val isWifiRTTCompatibleMc: Boolean,
    val isWifiRTTCompatibleAz: Boolean?,
    val scanResultObject: ScanResult,
    var selectedForRTT: Boolean = false
)
