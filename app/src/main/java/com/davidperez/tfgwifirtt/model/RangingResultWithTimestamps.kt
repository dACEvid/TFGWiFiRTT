package com.davidperez.tfgwifirtt.model

import android.net.wifi.rtt.RangingResult

data class RangingResultWithTimestamps(
    val startTime: Long,
    val endTime: Long,
    val results: List<RangingResult>
)