package com.davidperez.tfgwifirtt.ui

import android.net.wifi.ScanResult
import android.net.wifi.rtt.RangingResult
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.davidperez.tfgwifirtt.model.AccessPoint
import com.davidperez.tfgwifirtt.ui.viewmodels.AccessPointsViewModel

@Composable
fun AccessPointsListScreen(
    accessPointsViewModel: AccessPointsViewModel = hiltViewModel()
) {
    // State to hold the scan results
    val accessPointsUiState by accessPointsViewModel.uiState.collectAsState()

    AccessPoints(
        accessPointsList = accessPointsUiState.accessPointsList,
        selectedForRTT = accessPointsUiState.selectedForRTT,
        rttRangingResults = accessPointsUiState.rttRangingResults,
        onStartScan = { accessPointsViewModel.refreshAccessPoints() },
        onToggleSelectionForRTT = { accessPointsViewModel.toggleSelectionForRTT(it) },
        onCreateRTTRangingRequest = { accessPointsViewModel.createRTTRangingRequest(it) },
        onDoContinuousRTTRanging = { accessPointsViewModel.doContinuousRTTRanging(it) },
        onExportRTTRangingResultsToCsv = { accessPointsViewModel.exportRTTRangingResultsToCsv(it) },
    )

    RTTResultDialog(
        onComplete = { accessPointsViewModel.removeRTTResultDialog() },
        dialogText = accessPointsUiState.rttResultDialogText,
        icon = Icons.Default.Info
    )
}

@Composable
private fun AccessPoints(
    accessPointsList: List<AccessPoint>,
    selectedForRTT: Set<ScanResult>,
    rttRangingResults: List<RangingResult>,
    onStartScan: () -> Unit,
    onToggleSelectionForRTT: (ScanResult) -> Unit,
    onCreateRTTRangingRequest: (Set<ScanResult>) -> Unit,
    onDoContinuousRTTRanging: (Set<ScanResult>) -> Unit,
    onExportRTTRangingResultsToCsv: (List<RangingResult>) -> String,
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

    // Show access point list
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 16.dp),
    ) {
        if (accessPointsList.isNotEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Visible Access Points",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        items(accessPointsList) { ap ->
            AccessPointItem(ap, onToggleSelectionForRTT)
        }
        if (selectedForRTT.isNotEmpty()) {
            item {
                OutlinedButton(
                    onClick = { onCreateRTTRangingRequest(selectedForRTT) },
                    modifier
                        .padding(5.dp)
                        .fillMaxSize()
                ) {
                    Text(
                        "Create RTT Ranging Request",
                        textAlign = TextAlign.Center
                    )
                }
            }
            item {
                OutlinedButton(
                    onClick = { onDoContinuousRTTRanging(selectedForRTT) },
                    modifier
                        .padding(5.dp)
                        .fillMaxSize()
                ) {
                    Text(
                        "Continuous RTT Ranging (20 sec)",
                        textAlign = TextAlign.Center
                    )
                }
            }
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
        item {
            OutlinedButton(
                onClick = onStartScan,
                modifier
                    .padding(5.dp)
                    .fillMaxSize()
            ) {
                Text(
                    "Scan Access Points",
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // TODO: show error message if any
}

@Composable
fun AccessPointItem(ap: AccessPoint, onToggleSelectionForRTT: (ScanResult) -> Unit) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Text("SSID: " + ap.ssid)
            Text("BSSID: " + ap.bssid)
            Text("Supports RTT: " + ap.isWifiRTTCompatible)
            if (ap.isWifiRTTCompatible) {
                Spacer(modifier = Modifier.height(20.dp))
                Text("Select for RTT")
                Switch(
                    checked = ap.selectedForRTT,
                    onCheckedChange = {
                        ap.selectedForRTT = it
                        onToggleSelectionForRTT(ap.scanResultObject)
                    },
                    enabled = true
                )
            }
        }
    }
}


@Composable
fun RTTResultDialog(
    onComplete: () -> Unit,
    dialogText: String,
    icon: ImageVector,
) {
    if (dialogText != "") {
        AlertDialog(
            icon = {
                Icon(icon, contentDescription = "Example Icon")
            },
            title = {
                Text(text = "RTT Ranging Result")
            },
            text = {
                Text(text = dialogText)
            },
            onDismissRequest = {
                onComplete()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onComplete()
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onComplete()
                    }
                ) {
                    Text("Dismiss")
                }
            }
        )
    }
}
