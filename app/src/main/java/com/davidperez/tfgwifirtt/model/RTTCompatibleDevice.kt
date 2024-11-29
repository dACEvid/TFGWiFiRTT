package com.davidperez.tfgwifirtt.model

import com.google.firebase.firestore.PropertyName

data class RTTCompatibleDevice(
    val id: Int = 0,
    val model: String = "00testmod00",
    val manufacturer: String = "00testman00",
    val androidVersion: String = "00testver00"
)