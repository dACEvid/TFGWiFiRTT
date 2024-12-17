package com.davidperez.tfgwifirtt.data

import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.net.wifi.rtt.RangingRequest
import android.net.wifi.rtt.RangingResult
import android.net.wifi.rtt.RangingResultCallback
import android.net.wifi.rtt.WifiRttManager
import android.os.Build
import android.util.Log
import com.davidperez.tfgwifirtt.model.AccessPoint
import com.davidperez.tfgwifirtt.model.RTTCompatibleDevice
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * Interface to the Access Points data layer.
 */
interface AccessPointsRepository {

    /**
     * Scan access points.
     */
    suspend fun scanAccessPoints()

    /**
     * Observe the access points.
     */
    fun observeAccessPointsList(): Flow<List<AccessPoint>>

    /**
     * Observe the access points that have been selected for RTT
     */
    fun observeSelectedForRTT(): Flow<Set<ScanResult>>

    /**
     * Observe the results of the RTT Ranging Requests
     */
    fun observeRTTRangingResults(): Flow<List<RangingResult>>

    /**
     * Observe the message to show in the dialog that shows RTT results
     */
    fun observeRTTResultDialogText(): Flow<String>

    /**
     * Toggle an access point to be selected for RTT or not.
     */
    suspend fun toggleSelectionForRTT(accessPointScanResult: ScanResult)

    /**
     * Create RTT ranging request for the selected APs
     */
    suspend fun createRTTRangingRequest(selectedForRTT: Set<ScanResult>)

    suspend fun startRTTRanging(selectedForRTT: Set<ScanResult>, performContinuousRttRanging: Boolean, rttPeriod: Long, rttInterval: Long)

    suspend fun removeRTTResultDialog()
}

class AccessPointsRepositoryImpl @Inject constructor(private val application: Application) : AccessPointsRepository {
    private val selectedForRTT = MutableStateFlow<Set<ScanResult>>(setOf())
    private val accessPointsList = MutableStateFlow<List<AccessPoint>>(emptyList())
    private val rttRangingResults = MutableStateFlow<List<RangingResult>>(emptyList())
    private val rttResultDialogText = MutableStateFlow("")

    var wifiManager: WifiManager = this.application.getSystemService(Context.WIFI_SERVICE) as WifiManager // Initialize WifiManager
    //var wifiRTTManager: WifiRttManager = this.application.getSystemService(Context.WIFI_RTT_RANGING_SERVICE) as WifiRttManager // Initialize WifiRttManager
    lateinit var wifiRTTManager: WifiRttManager
    var scanResultList = mutableListOf<ScanResult>()

    private val wifiScanReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission") // Permissions are checked before starting to scan
        override fun onReceive(context: Context, intent: Intent) {
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            if (!success) {
                Log.e("TestDavid", "Scanning of Access Points failed")
            }

            scanResultList = wifiManager.scanResults
            Log.d("TestDavid", scanResultList.joinToString())
            accessPointsList.value = scanResultList.map { sr -> AccessPoint(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) sr.wifiSsid.toString() else sr.SSID, sr.BSSID, sr.is80211mcResponder, sr) }
        }
    }

    private val wifiRTTStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (!wifiRTTManager.isAvailable) {
                Log.e("TestDavid", "WiFi RTT is not available")
            }
        }
    }

    init {
        // Register a broadcast listener for SCAN_RESULTS_AVAILABLE_ACTION, which is called when scan requests are completed
        this.application.registerReceiver(wifiScanReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))

        // Register a broadcast listener for ACTION_WIFI_RTT_STATE_CHANGED, which is called when the availability of RTT changes
        this.application.registerReceiver(wifiRTTStateReceiver, IntentFilter(WifiRttManager.ACTION_WIFI_RTT_STATE_CHANGED))
    }

    override suspend fun scanAccessPoints() {
        val success = wifiManager.startScan()
        if (!success) {
            Log.e("TestDavid", "Starting Scanning of Access Points failed")
        }
    }

    override fun observeAccessPointsList(): Flow<List<AccessPoint>> = accessPointsList.asStateFlow()

    override fun observeSelectedForRTT(): Flow<Set<ScanResult>> = selectedForRTT.asStateFlow()

    override fun observeRTTRangingResults(): Flow<List<RangingResult>> = rttRangingResults.asStateFlow()

    override fun observeRTTResultDialogText(): Flow<String> = rttResultDialogText.asStateFlow()

    override suspend fun toggleSelectionForRTT(accessPointScanResult: ScanResult) {
        selectedForRTT.update {
            it.addOrRemove(accessPointScanResult)
        }
    }

    private fun <E> Set<E>.addOrRemove(element: E): Set<E> {
        return this.toMutableSet().apply {
            if (!add(element)) {
                remove(element)
            }
        }.toSet()
    }

    @SuppressLint("MissingPermission")
        override suspend fun createRTTRangingRequest(selectedForRTT: Set<ScanResult>) {
        // Check whether the device supports WiFi RTT
        if (!this.application.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_RTT)) {
            rttResultDialogText.value = "Device does not support WiFi RTT"
            return
        } else {
            saveCompatibleDevice()
        }

        wifiRTTManager = this.application.getSystemService(Context.WIFI_RTT_RANGING_SERVICE) as WifiRttManager // Initialize WifiRttManager
        // Create a ranging request
        val req: RangingRequest = RangingRequest.Builder().run {
            addAccessPoints(selectedForRTT.toList())
            build()
        }
        // Request ranging
        wifiRTTManager.startRanging(req, this.application.mainExecutor, object : RangingResultCallback() {
            // Callback that triggers when the ranging operation completes
            override fun onRangingResults(results: List<RangingResult>) {
                rttRangingResults.update { it + results }
                val resultsStr = buildString {
                    for (result in results) {
                        appendLine()
                        if (result.status == RangingResult.STATUS_SUCCESS) {
                            append("Status: " + result.status.toString() + " MAC: " + result.macAddress.toString() + " Distance (mm): " + result.distanceMm.toString() + " Std Dev (mm): " + result.distanceStdDevMm.toString())
                        } else {
                            append("RTT failed for MAC " + result.macAddress.toString())
                        }
                    }
                }
                rttResultDialogText.value = resultsStr
            }

            // Callback that triggers when the whole ranging operation fails
            override fun onRangingFailure(code: Int) {
                rttResultDialogText.value = "RTT Ranging Request failed"
            }
        })
    }

    override suspend fun startRTTRanging(selectedForRTT: Set<ScanResult>, performContinuousRttRanging: Boolean, rttPeriod: Long, rttInterval: Long) {
        // We could add a setting to only keep the last results for export

        if (performContinuousRttRanging) {
            val endTime = System.currentTimeMillis() + rttPeriod
            while (System.currentTimeMillis() < endTime) {
                createRTTRangingRequest(selectedForRTT)
                delay(rttInterval) // Delay between requests
            }
            rttResultDialogText.value = "Continuous RTT Ranging finished"
        } else {
            createRTTRangingRequest(selectedForRTT)
        }
    }

    override suspend fun removeRTTResultDialog() {
        rttResultDialogText.value = ""
    }

    private fun saveCompatibleDevice() {
        // The device information will be stored in Firestore
        val db = Firebase.firestore
        val device = RTTCompatibleDevice(
            lastChecked = System.currentTimeMillis().toInt(),
            model = Build.MODEL,
            manufacturer = Build.MANUFACTURER,
            androidVersion = Build.VERSION.RELEASE
        )

        val compositeId = "${device.manufacturer}-${device.model}-${device.androidVersion}"
        db.collection("compatible-devices")
            .document(compositeId)
            .set(device)
            .addOnSuccessListener {
                Log.d("TestDavid", "Device added with composite ID: $compositeId")
            }
            .addOnFailureListener { e ->
                Log.e("TestDavid", "Error adding device to Firestore", e)
            }

//        db.collection("compatible-devices")
//            .add(device)
//            .addOnSuccessListener { documentReference ->
//                Log.d("TestDavid", "Device successfully added with ID: ${documentReference.id}")
//            }
//            .addOnFailureListener { e ->
//                Log.e("TestDavid", "Error adding device to Firestore", e)
//            }
    }
}

/**
 * In Hilt, a module is a class that provides the dependencies that your app needs
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
//    @Provides
//    @Singleton
//    fun provideAccessPointsRepository(application: Application): AccessPointsRepository {
//        return AccessPointsRepositoryImpl(application)
//    }
    @Binds
    abstract fun bindAccessPointsRepository(
        accessPointsRepositoryImpl: AccessPointsRepositoryImpl
    ): AccessPointsRepository
}



