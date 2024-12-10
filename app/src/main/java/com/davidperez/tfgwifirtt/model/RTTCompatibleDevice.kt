package com.davidperez.tfgwifirtt.model

import com.google.firebase.firestore.PropertyName

data class RTTCompatibleDevice(
    val lastChecked: Int = 0,
    val model: String = "TestModel",
    val manufacturer: String = "TestManufacturer",
    val androidVersion: String = "TestVersion"
)