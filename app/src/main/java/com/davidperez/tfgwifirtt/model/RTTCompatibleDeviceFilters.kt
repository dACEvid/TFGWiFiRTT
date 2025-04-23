package com.davidperez.tfgwifirtt.model

data class RTTCompatibleDevicesFilters(
    val manufacturer: String? = null,
    val modelQuery: String = "",
    val androidVersion: String? = null,
    val showMc: Boolean = true,
    val showAz: Boolean = true
)