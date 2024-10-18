package com.davidperez.tfgwifirtt

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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.davidperez.tfgwifirtt.ui.theme.TFGWiFiRTTTheme

class MainActivity : ComponentActivity() {

    lateinit var wifiManager: WifiManager
    lateinit var locationManager: LocationManager
    var scanResultList = mutableListOf<ScanResult>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize  WiFiManager and LocationManager
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        requestNeededPermissions()

        startScanning()

        setContent {
            TFGWiFiRTTTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Text("TEST")
                    //AccessPointList(wifiAPs)
                }
            }
        }
    }

    private fun startScanning() {
        val wifiScanReceiver = object : BroadcastReceiver() {
            @SuppressLint("MissingPermission") // Permissions are checked before starting to scan
            override fun onReceive(context: Context, intent: Intent) {
                Log.d("Test", "onReceive Called")
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (!success) {
                    Log.e("TestDavid", "Scanning of Access Points failed1")
                }

                scanResultList = wifiManager.scanResults
                Log.d("TestDavid", scanResultList.joinToString())
                val wifiAPs = scanResultList.map { sr -> AccessPoint(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) sr.wifiSsid.toString() else sr.SSID, sr.is80211mcResponder) }
            }
        }

        // Register a broadcast listener for SCAN_RESULTS_AVAILABLE_ACTION, which is called when scan requests are completed
        registerReceiver(wifiScanReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))

        val success = wifiManager.startScan()
        if (!success) {
            Log.e("TestDavid", "Scanning of Access Points failed2")
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
    }

}

@Composable
fun AccessPointList(aps: List<AccessPoint>, modifier: Modifier = Modifier) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = modifier
    ) {
        item {
            Text("Visible Access Points")
        }
        items(aps) { ap ->
            AccessPointItem(ap)
        }
    }
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
