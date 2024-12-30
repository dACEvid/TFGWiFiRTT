package com.davidperez.tfgwifirtt.ui.screens

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.rtt.RangingResult
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.hilt.navigation.compose.hiltViewModel
import com.davidperez.tfgwifirtt.model.AccessPoint
import com.davidperez.tfgwifirtt.model.UserSettings
import com.davidperez.tfgwifirtt.ui.components.LoadingIndicator
import com.davidperez.tfgwifirtt.ui.components.ScreenTitle
import com.davidperez.tfgwifirtt.ui.viewmodels.AccessPointsViewModel

@Composable
fun AccessPointsListScreen(
    accessPointsViewModel: AccessPointsViewModel = hiltViewModel()
) {
    // State to hold the scan results
    val accessPointsUiState by accessPointsViewModel.uiState.collectAsState()

    val context = LocalContext.current

    AccessPoints(
        accessPointsList = accessPointsUiState.accessPointsList,
        selectedForRTT = accessPointsUiState.selectedForRTT,
        rttRangingResults = accessPointsUiState.rttRangingResults,
        isLoading = accessPointsUiState.isLoading,
        onStartScan = {
            if (checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)  {
                accessPointsViewModel.showPermissionsDialog()
            } else {
                accessPointsViewModel.refreshAccessPoints()
            }
        },
        onToggleSelectionForRTT = { accessPointsViewModel.toggleSelectionForRTT(it) },
        onStartRTTRanging = { selectedForRTT, performContinuousRttRanging, rttPeriod, rttInterval, saveRttResults, saveOnlyLastRttOperation -> accessPointsViewModel.startRTTRanging(selectedForRTT, performContinuousRttRanging, rttPeriod, rttInterval, saveRttResults, saveOnlyLastRttOperation) },
        onExportRTTRangingResultsToCsv = { accessPointsViewModel.exportRTTRangingResultsToCsv(it) },
        userSettings = accessPointsUiState.userSettings
    )

    RequestPermissionsDialog(
        onAccept = {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.NEARBY_WIFI_DEVICES
                ),
                1
            )
            accessPointsViewModel.removeDialogs()
        },
        onReject = { accessPointsViewModel.removeDialogs() },
        showDialog = accessPointsUiState.showPermissionsDialog
    )

    RTTResultDialog(
        onComplete = { accessPointsViewModel.removeDialogs() },
        dialogText = accessPointsUiState.rttResultDialogText,
        icon = Icons.Default.Info
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AccessPoints(
    accessPointsList: List<AccessPoint>,
    selectedForRTT: Set<ScanResult>,
    rttRangingResults: List<RangingResult>,
    isLoading: Boolean,
    onStartScan: () -> Unit,
    onToggleSelectionForRTT: (ScanResult) -> Unit,
    onStartRTTRanging: (Set<ScanResult>, Boolean, Long, Long, Boolean, Boolean) -> Unit,
    onExportRTTRangingResultsToCsv: (List<RangingResult>) -> String,
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

    var accessPointsToShow: List<AccessPoint> = accessPointsList
    val rttCompatibleAccessPoints = accessPointsList.filter { it.isWifiRTTCompatible }

    if (userSettings.showOnlyRttCompatibleAps ) {
        accessPointsToShow = rttCompatibleAccessPoints
    }

    if (isLoading) {
        LoadingIndicator()
    }

    // Show access point list
    LazyColumn {
        if (accessPointsList.isNotEmpty()) {
            stickyHeader {
                ScreenTitle("Visible Access Points")
                ScreenTitle("${accessPointsList.size} APs discovered (${rttCompatibleAccessPoints.size} of them are RTT-capable)", true)
            }
        }
        items(accessPointsToShow) { ap ->
            AccessPointItem(ap, onToggleSelectionForRTT)
        }
        if (selectedForRTT.isNotEmpty()) {
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
fun AccessPointItem(
    ap: AccessPoint,
    onToggleSelectionForRTT: (ScanResult) -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Row {
                Text("SSID: ")
                Text(
                    text = ap.ssid,
                    fontWeight = FontWeight.Bold
                )
            }
            Row {
                Text("BSSID: ")
                Text(
                    text = ap.bssid,
                    fontWeight = FontWeight.Bold
                )
            }
            Row {
                Text("Supports RTT: ")
                Text(
                    text = ap.isWifiRTTCompatible.toString(),
                    fontWeight = FontWeight.Bold
                )
            }
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

@Composable
fun RequestPermissionsDialog(
    onAccept: () -> Unit,
    onReject: () -> Unit,
    showDialog: Boolean
) {
    if (showDialog) {
        AlertDialog(
            title = {
                Text(
                    text = "Extra permissions required",
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "This app needs access to your device's location to scan nearby access points.",
                    textAlign = TextAlign.Center
                )
            },
            onDismissRequest = {
                onReject()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAccept()
                    },
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(0.45f)
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onReject()
                    },
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(0.45f)
                ) {
                    Text("No, Thanks")
                }
            }
        )
    }
}
