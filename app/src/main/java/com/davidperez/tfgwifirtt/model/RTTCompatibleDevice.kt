package com.davidperez.tfgwifirtt.model

data class RTTCompatibleDevice(
    val lastChecked: Int = 0,
    val model: String = "TestModel",
    val manufacturer: String = "TestManufacturer",
    val androidVersion: String = "TestVersion",
    val standard: String = "mc"
)