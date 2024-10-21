package com.davidperez.tfgwifirtt.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.davidperez.tfgwifirtt.data.AccessPointsRepository
import com.davidperez.tfgwifirtt.model.AccessPoint
import com.davidperez.tfgwifirtt.ui.theme.TFGWiFiRTTTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // lateinit var wifiManager: WifiManager
    private lateinit var locationManager: LocationManager
    // var scanResultList = mutableListOf<ScanResult>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize  WiFiManager and LocationManager
        //wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        requestNeededPermissions()

        //startScanning()

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

//    private fun startScanning() {
//        val wifiScanReceiver = object : BroadcastReceiver() {
//            @SuppressLint("MissingPermission") // Permissions are checked before starting to scan
//            override fun onReceive(context: Context, intent: Intent) {
//                Log.d("Test", "onReceive Called")
//                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
//                if (!success) {
//                    Log.e("TestDavid", "Scanning of Access Points failed1")
//                }
//
//                scanResultList = wifiManager.scanResults
//                Log.d("TestDavid", scanResultList.joinToString())
//                val wifiAPs = scanResultList.map { sr -> AccessPoint(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) sr.wifiSsid.toString() else sr.SSID, sr.is80211mcResponder) }
//            }
//        }
//
//        // Register a broadcast listener for SCAN_RESULTS_AVAILABLE_ACTION, which is called when scan requests are completed
//        registerReceiver(wifiScanReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
//
//        val success = wifiManager.startScan()
//        if (!success) {
//            Log.e("TestDavid", "Scanning of Access Points failed2")
//        }
//    }


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
    }

}

@Composable
fun AccessPointsListScreen(
    accessPointsViewModel: AccessPointsViewModel = viewModel()
) {
    // State to hold the scan results
    //val uiState by accessPointsViewModel.uiState.collectAsStateWithLifecycle()
    val accessPointsUiState by accessPointsViewModel.uiState.collectAsState()

    AccessPoints(
        accessPointsList = accessPointsUiState.accessPointsList,
        onStartScan = { accessPointsViewModel.refreshAccessPoints() },
        onToggleSelectionForRTT = { accessPointsViewModel.toggleSelectionForRTT(it) }
    )
}

@Composable
private fun AccessPoints(
    accessPointsList: List<AccessPoint>,
    onStartScan: () -> Unit,
    onToggleSelectionForRTT: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (accessPointsList.isEmpty()) {
        // Show scan button
        TextButton(
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
    } else {
        // Show access point list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            item {
                Text("Visible Access Points")
            }
            items(accessPointsList) { ap ->
                AccessPointItem(ap)
            }
        }
    }

    // TODO: show error message if any
}

@Composable
fun AccessPointItem(ap: AccessPoint, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
    ) {
        Text("SSID :" + ap.ssid)
        Text("Supports WiFi RTT: " + ap.isWifiRTTCompatible)
    }
}



//@Preview(showBackground = true)
//@Composable
//fun AccessPointListPreview() {
//    TFGWiFiRTTTheme {
//        AccessPointList(aps = null)
//    }
//}
