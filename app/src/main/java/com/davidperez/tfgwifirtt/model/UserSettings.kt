package com.davidperez.tfgwifirtt.model

data class UserSettings(
    val showOnlyRttCompatibleAps: Boolean = false,
    val performContinuousRttRanging: Boolean = false,
    val rttPeriod: Long = 10,
    val rttInterval: Long = 500,
    val saveRttResults: Boolean = true
)
