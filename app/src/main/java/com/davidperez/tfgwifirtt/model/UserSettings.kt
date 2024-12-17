package com.davidperez.tfgwifirtt.model

data class UserSettings(
    val showOnlyRttCompatibleAps: Boolean = false,
    val performContinuousRttRanging: Boolean = false,
    val rttPeriod: Int = 10,
    val rttInterval: Int = 500,
    val saveRttResults: Boolean = true
)
