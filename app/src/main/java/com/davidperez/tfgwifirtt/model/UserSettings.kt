package com.davidperez.tfgwifirtt.model

data class UserSettings(
    val showOnlyRttCompatibleAps: Boolean = false,
    val performContinuousRttRanging: Boolean = false,
    var rttPeriod: Int = 10,
    var rttInterval: Int = 500,
    val saveRttResults: Boolean = true
)
