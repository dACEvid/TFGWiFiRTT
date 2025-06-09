package com.davidperez.tfgwifirtt.ui.screens

import android.net.wifi.ScanResult
import android.net.wifi.rtt.RangingResult
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.davidperez.tfgwifirtt.model.AccessPoint
import com.davidperez.tfgwifirtt.model.RangingResultWithTimestamps
import com.davidperez.tfgwifirtt.model.UserSettings
import com.davidperez.tfgwifirtt.ui.components.ErrorDialog
import com.davidperez.tfgwifirtt.ui.viewmodels.AccessPointsViewModel


@Composable
fun RTTRangingScreen(
    accessPointsViewModel: AccessPointsViewModel = hiltViewModel()
) {
    val accessPointsUiState by accessPointsViewModel.uiState.collectAsState()

    RTTResults(
        selectedForRTT = accessPointsUiState.selectedForRTT,
        rttRangingResults = accessPointsUiState.rttRangingResults,
        rttRangingResultsForExport = accessPointsUiState.rttRangingResultsForExport,
        onStartRTTRanging = { selectedForRTT, performContinuousRttRanging, rttPeriod, rttInterval, saveRttResults, saveOnlyLastRttOperation -> accessPointsViewModel.startRTTRanging(selectedForRTT, performContinuousRttRanging, rttPeriod, rttInterval, saveRttResults, saveOnlyLastRttOperation) },
        onExportRTTRangingResultsToCsv = { accessPointsViewModel.exportRTTRangingResultsToCsv(it) },
        userSettings = accessPointsUiState.userSettings
    )

    ErrorDialog(
        onComplete = { accessPointsViewModel.removeDialogs() },
        errorMsg = accessPointsUiState.errorMsg
    )
}

@Composable
fun RTTResults(
    selectedForRTT: List<AccessPoint>,
    rttRangingResults: List<RangingResult>,
    rttRangingResultsForExport: List<RangingResultWithTimestamps>,
    onStartRTTRanging: (List<AccessPoint>, Boolean, Long, Long, Boolean, Boolean) -> Unit,
    onExportRTTRangingResultsToCsv: (List<RangingResultWithTimestamps>) -> String,
    userSettings: UserSettings,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    val csvContent = onExportRTTRangingResultsToCsv(rttRangingResultsForExport)
                    outputStream.write(csvContent.toByteArray())
                }
            }
        }
    )

    LazyColumn {
        items(selectedForRTT.toList()) { ap ->
            RTTResultItem(ap, rttRangingResults)
        }
        item {
            OutlinedButton(
                onClick = { onStartRTTRanging(selectedForRTT, userSettings.performContinuousRttRanging, userSettings.rttPeriod * 1000, userSettings.rttInterval, userSettings.saveRttResults, userSettings.saveOnlyLastRttOperation) },
                modifier
                    .padding(5.dp)
                    .fillMaxSize()
            ) {
                Text(
                    "Start RTT Ranging",
                    textAlign = TextAlign.Center
                )
            }
            // TODO: add start/stop RTT Ranging buttons, not just start based on userSettings
            // Add checkbox "use user settings" which is checked by default. If unchecked, "Start RTT Ranging" performs continuous ranging "for ever" and can only be stopped with the stop RTT Ranging button
        }
        if (rttRangingResultsForExport.isNotEmpty()) {
            item {
                OutlinedButton(
                    onClick = { launcher.launch("rtt_ranging_results_${System.currentTimeMillis()}.csv") },
                    modifier
                        .padding(5.dp)
                        .fillMaxSize()
                ) {
                    Text(
                        "Export RTT results to CSV",
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun RTTResultItem(
    ap: AccessPoint,
    rttRangingResults: List<RangingResult>
) {
    val rttResultsForAccessPoint = rttRangingResults.filter { it.macAddress.toString() == ap.bssid && it.status == RangingResult.STATUS_SUCCESS }
    var rttResult: RangingResult? = null
    if (rttResultsForAccessPoint.isNotEmpty()) {
        rttResult = rttResultsForAccessPoint[0]
    }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("SSID: ")
                        }
                        append(ap.ssid)
                    },)

                    Text(text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("BSSID: ")
                        }
                        append(ap.bssid)
                    },)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Distance (mm): ")
                        }
                        append(rttResult?.distanceMm?.toString() ?: "-")
                    },)

                    Text(text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Std Dev (mm): ")
                        }
                        append(rttResult?.distanceStdDevMm?.toString() ?: "-")
                    },)

                    Text(text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("RSSI (dbM): ")
                        }
                        append(rttResult?.rssi?.toString() ?: "-")
                    },)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Freq (MHz): ")
                        }
                        append(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) rttResult?.measurementChannelFrequencyMHz?.toString() ?: "-" else "Requires Android >= 14")
                    },)

                    Text(text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("BW (MHz): ")
                        }
                        append(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) getBandwidthInMHz(rttResult?.measurementBandwidth) else "Requires Android >= 14")
                    },)

                    Text(text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Successful Measurements: ")
                        }
                        append((rttResult?.numSuccessfulMeasurements?.toString() ?: "-") + "/" + (rttResult?.numAttemptedMeasurements?.toString() ?: "-"))
                    },)
                }
            }
        }
    }
}

fun getBandwidthInMHz(bandwidth: Int?): String {
    return when (bandwidth) {
        ScanResult.CHANNEL_WIDTH_20MHZ -> "20"
        ScanResult.CHANNEL_WIDTH_40MHZ -> "40"
        ScanResult.CHANNEL_WIDTH_80MHZ -> "80"
        ScanResult.CHANNEL_WIDTH_160MHZ -> "160"
        ScanResult.CHANNEL_WIDTH_80MHZ_PLUS_MHZ -> "80+80"
        ScanResult.CHANNEL_WIDTH_320MHZ -> "320"
        else -> "-"
    }
}
