package com.davidperez.tfgwifirtt.model

data class AccessPoint(
    val ssid: String,
    val bssid: String,
    val isWifiRTTCompatible: Boolean,
    val selectedForRTT: Boolean = false
)
