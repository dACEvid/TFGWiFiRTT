package com.davidperez.tfgwifirtt.model

data class RTTCompatibleDevicesFilters(
    val deviceQuery: String = "",
    val androidVersion: String? = null,
    val showMc: Boolean = true,
    val showAz: Boolean = true
)