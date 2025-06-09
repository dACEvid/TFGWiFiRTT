package com.davidperez.tfgwifirtt.ui.screens

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.hilt.navigation.compose.hiltViewModel
import com.davidperez.tfgwifirtt.model.AccessPoint
import com.davidperez.tfgwifirtt.model.UserSettings
import com.davidperez.tfgwifirtt.ui.components.CompatibilityBadge
import com.davidperez.tfgwifirtt.ui.components.LoadingIndicator
import com.davidperez.tfgwifirtt.ui.components.ScreenTitle
import com.davidperez.tfgwifirtt.ui.viewmodels.AccessPointsViewModel

@Composable
fun AccessPointsListScreen(
    accessPointsViewModel: AccessPointsViewModel = hiltViewModel(),
    onGoToRTTRanging: () -> Unit
) {
    // State to hold the scan results
    val accessPointsUiState by accessPointsViewModel.uiState.collectAsState()

    val context = LocalContext.current

    AccessPoints(
        accessPointsList = accessPointsUiState.accessPointsList,
        selectedForRTT = accessPointsUiState.selectedForRTT,
        isLoading = accessPointsUiState.isLoading,
        onStartScan = {
            if (checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)  {
                accessPointsViewModel.showPermissionsDialog()
            } else {
                accessPointsViewModel.refreshAccessPoints()
            }
        },
        onToggleSelectionForRTT = { accessPointsViewModel.toggleSelectionForRTT(it) },
        onGoToRTTRanging = { onGoToRTTRanging() },
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
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AccessPoints(
    accessPointsList: List<AccessPoint>,
    selectedForRTT: List<AccessPoint>,
    isLoading: Boolean,
    onStartScan: () -> Unit,
    onToggleSelectionForRTT: (AccessPoint) -> Unit,
    onGoToRTTRanging: () -> Unit,
    userSettings: UserSettings,
    modifier: Modifier = Modifier,
) {

    var accessPointsToShow: List<AccessPoint> = accessPointsList
    val rttCompatibleAccessPoints = accessPointsList.filter { it.isWifiRTTCompatibleMc || it.isWifiRTTCompatibleAz == true }

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
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "802.11az support cannot be determined on this device as it requires Android 15 or later. Checking only for 802.11mc support...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                ScreenTitle("${accessPointsList.size} APs discovered (${rttCompatibleAccessPoints.size} of them are RTT-capable)", true)
            }
        }
        items(accessPointsToShow) { ap ->
            AccessPointItem(ap, onToggleSelectionForRTT)
        }
        if (selectedForRTT.isNotEmpty()) {
            item {
                OutlinedButton(
                    onClick = { onGoToRTTRanging() },
                    modifier
                        .padding(5.dp)
                        .fillMaxSize()
                ) {
                    Text(
                        "Go to RTT Ranging",
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
    onToggleSelectionForRTT: (AccessPoint) -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
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
                if (ap.isWifiRTTCompatibleMc || ap.isWifiRTTCompatibleAz == true || true) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Select for RTT")
                    Switch(
                        checked = ap.selectedForRTT,
                        onCheckedChange = {
                            onToggleSelectionForRTT(ap)
                        },
                        enabled = true
                    )
                }
            }

            if (ap.isWifiRTTCompatibleMc || ap.isWifiRTTCompatibleAz == true) {
                // Compatibility Badges
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (ap.isWifiRTTCompatibleMc) {
                        CompatibilityBadge("mc")
                    }
                    if (ap.isWifiRTTCompatibleAz == true) {
                        CompatibilityBadge("az")
                    }
                }
            }
        }
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
                    modifier = Modifier.fillMaxWidth(0.48f)
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
                    modifier = Modifier.fillMaxWidth(0.48f)
                ) {
                    Text("No, Thanks")
                }
            }
        )
    }
}
