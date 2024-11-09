package com.davidperez.tfgwifirtt.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.wifi.ScanResult
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.davidperez.tfgwifirtt.model.AccessPoint
import com.davidperez.tfgwifirtt.ui.theme.TFGWiFiRTTTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize  LocationManager
        locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        requestNeededPermissions()

        setContent {
            TFGWiFiRTTTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AccessPointsListScreen()
                }
            }
        }
    }

    private fun requestNeededPermissions() {
        // check if location is enabled
        if (!locationManager.isLocationEnabled) {
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show()
        }

        // Permissions needed for a successful call to startScan()
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        }
        if (checkSelfPermission(Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CHANGE_WIFI_STATE), 1)
        }

        // Permission needed for a successful call to getScanResults()
        if (checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_WIFI_STATE), 1)
        }

        // Permission needed for performing a RTT ranging request (only for Android 13 or higher)
        // TODO: request this at runtime when the user wants to perform the RTT ranging request
        if (checkSelfPermission(Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.NEARBY_WIFI_DEVICES), 1)
        }
    }

}

@Composable
fun AccessPointsListScreen(
    accessPointsViewModel: AccessPointsViewModel = viewModel()
) {
    // State to hold the scan results
    val accessPointsUiState by accessPointsViewModel.uiState.collectAsState()

    AccessPoints(
        accessPointsList = accessPointsUiState.accessPointsList,
        selectedForRTT = accessPointsUiState.selectedForRTT,
        onStartScan = { accessPointsViewModel.refreshAccessPoints() },
        onToggleSelectionForRTT = { accessPointsViewModel.toggleSelectionForRTT(it) },
        onCreateRTTRangingRequest = { accessPointsViewModel.createRTTRangingRequest(it) },
    )
}

@Composable
private fun AccessPoints(
    accessPointsList: List<AccessPoint>,
    selectedForRTT: Set<ScanResult>,
    onStartScan: () -> Unit,
    onToggleSelectionForRTT: (ScanResult) -> Unit,
    onCreateRTTRangingRequest: (Set<ScanResult>) -> Unit,
    modifier: Modifier = Modifier,
) {
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
        }
    }

    // TODO: show error message if any
}

@Composable
fun AccessPointItem(ap: AccessPoint, onToggleSelectionForRTT: (ScanResult) -> Unit,) {
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


//@Preview(showBackground = true)
//@Composable
//fun AccessPointListPreview() {
//    TFGWiFiRTTTheme {
//        AccessPointList(aps = null)
//    }
//}
