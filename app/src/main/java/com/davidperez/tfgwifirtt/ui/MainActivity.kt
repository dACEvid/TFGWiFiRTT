package com.davidperez.tfgwifirtt.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.wifi.ScanResult
import android.net.wifi.rtt.RangingResult
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.davidperez.tfgwifirtt.model.AccessPoint
import com.davidperez.tfgwifirtt.model.RTTCompatibleDevice
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
                    MyAppNav()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppNav() {
    val navController = rememberNavController() // Create NavController

    val topLevelRoutes = listOf(
        TopLevelRoute("Access Points", "accessPointsList", Icons.Default.Home),
        TopLevelRoute("Compatible Devices", "compatibleDevicesList", Icons.Default.Info)
    )

    Scaffold(
        bottomBar = {
            BottomNavigation {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                topLevelRoutes.forEach { topLevelRoute ->
                    BottomNavigationItem(
                        icon = { Icon(topLevelRoute.icon, contentDescription = topLevelRoute.name) },
                        selectedContentColor = Color.White,
                        unselectedContentColor = Color.Gray,
                        label = { Text(topLevelRoute.name) },
                        selected = currentDestination?.route == topLevelRoute.route,
                        onClick = {
                            navController.navigate(topLevelRoute.route) {
                                // Pop up to the start destination of the graph to avoid building up a large stack of destinations on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true // Avoid multiple copies of the same destination when re-selecting the same item
                                restoreState = true // Restore state when re-selecting a previously selected item
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = "accessPointsList", Modifier.padding(innerPadding)) {
            composable("accessPointsList") { AccessPointsListScreen() }
            composable("compatibleDevicesList") { CompatibleDevicesListScreen() }
        }
    }
}

@Composable
fun CompatibleDevicesListScreen(
    rttCompatibleDevicesViewModel: RTTCompatibleDevicesViewModel = hiltViewModel()
) {
    val rttCompatibleDevicesUiState by rttCompatibleDevicesViewModel.uiState.collectAsState()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 16.dp),
    ) {
        item {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "RTT-compatible Devices",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        items(rttCompatibleDevicesUiState.rttCompatibleDevicesList) { cd ->
            CompatibleDeviceItem(cd)
        }
    }
}

@Composable
fun CompatibleDeviceItem(cd: RTTCompatibleDevice) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Text("Manufacturer: " + cd.manufacturer)
            Text("Model: " + cd.model)
            Text("Android Version: " + cd.androidVersion)
        }
    }
}

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
    onExportRTTRangingResultsToCsv: (List<RangingResult>) -> Unit,
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
                    onClick = { onExportRTTRangingResultsToCsv(rttRangingResults) },
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


//@Preview(showBackground = true)
//@Composable
//fun AccessPointListPreview() {
//    TFGWiFiRTTTheme {
//        AccessPointList(aps = null)
//    }
//}

data class TopLevelRoute(val name: String, val route: String, val icon: ImageVector)

