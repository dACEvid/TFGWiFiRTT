package com.davidperez.tfgwifirtt.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.davidperez.tfgwifirtt.model.AccessPoint
import com.davidperez.tfgwifirtt.model.RangingResultWithTimestamps
import com.davidperez.tfgwifirtt.model.UserSettings
import com.davidperez.tfgwifirtt.ui.viewmodels.AccessPointsViewModel


@Composable
fun RTTRangingScreen(
    accessPointsViewModel: AccessPointsViewModel = hiltViewModel()
) {
    val accessPointsUiState by accessPointsViewModel.uiState.collectAsState()

    RTTResults(
        selectedForRTT = accessPointsUiState.selectedForRTT,
        rttRangingResults = accessPointsUiState.rttRangingResults,
        onStartRTTRanging = { selectedForRTT, performContinuousRttRanging, rttPeriod, rttInterval, saveRttResults, saveOnlyLastRttOperation -> accessPointsViewModel.startRTTRanging(selectedForRTT, performContinuousRttRanging, rttPeriod, rttInterval, saveRttResults, saveOnlyLastRttOperation) },
        onExportRTTRangingResultsToCsv = { accessPointsViewModel.exportRTTRangingResultsToCsv(it) },
        userSettings = accessPointsUiState.userSettings
    )

    RTTResultDialog(
        onComplete = { accessPointsViewModel.removeDialogs() },
        dialogText = accessPointsUiState.rttResultDialogText,
        icon = Icons.Default.Info
    )
}

@Composable
fun RTTResults(
    selectedForRTT: List<AccessPoint>,
    rttRangingResults: List<RangingResultWithTimestamps>,
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
                    val csvContent = onExportRTTRangingResultsToCsv(rttRangingResults)
                    outputStream.write(csvContent.toByteArray())
                }
            }
        }
    )

    LazyColumn {
        items(selectedForRTT.toList()) { ap ->
            RTTResultItem(ap)
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
        }
        if (rttRangingResults.isNotEmpty()) {
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
    ap: AccessPoint
) {
    // TODO: show the actual RTT results here, not in a dialog
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp)
            ) {
                Column {
                    Text("SSID: ")
                    Text(
                        text = ap.ssid,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text("BSSID: ")
                    Text(
                        text = ap.bssid,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
