package com.davidperez.tfgwifirtt.data

import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.net.wifi.rtt.RangingRequest
import android.net.wifi.rtt.RangingResult
import android.net.wifi.rtt.RangingResultCallback
import android.net.wifi.rtt.WifiRttManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.davidperez.tfgwifirtt.model.AccessPoint
import com.davidperez.tfgwifirtt.model.RTTCompatibleDevice
import com.davidperez.tfgwifirtt.model.RangingResultWithTimestamps
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
    fun observeSelectedForRTT(): Flow<List<AccessPoint>>

    /**
     * Observe the results of the RTT Ranging Requests
     */
    fun observeRTTRangingResults(): Flow<List<RangingResult>>

    /**
     * Observe the RTT results to include in export
     */
    fun observeRTTRangingResultsForExport(): Flow<List<RangingResultWithTimestamps>>

    /**
     * Observe the message to show in the dialog that shows RTT results
     */
    fun observeRTTResultDialogText(): Flow<String>

    /**
     * Observe the loading status
     */
    fun observeShowPermissionsDialog(): Flow<Boolean>

    /**
     * Observe the loading status
     */
    fun observeIsLoading(): Flow<Boolean>

    /**
     * Create RTT ranging request for the selected APs
     */
    suspend fun createRTTRangingRequest(selectedForRTT: List<AccessPoint>, saveRttResults: Boolean, startTime: Long)

    suspend fun startRTTRanging(selectedForRTT: List<AccessPoint>, performContinuousRttRanging: Boolean, rttPeriod: Long, rttInterval: Long, saveRttResults: Boolean, saveOnlyLastRttOperation: Boolean)

    suspend fun removeDialogs()

    suspend fun showPermissionsDialog()
}

class AccessPointsRepositoryImpl @Inject constructor(private val application: Application) : AccessPointsRepository {
    private val selectedForRTT = MutableStateFlow<List<AccessPoint>>(emptyList())
    private val accessPointsList = MutableStateFlow<List<AccessPoint>>(emptyList())
    private val rttRangingResults = MutableStateFlow<List<RangingResult>>(emptyList())
    private val rttRangingResultsForExport = MutableStateFlow<List<RangingResultWithTimestamps>>(emptyList())
    private val rttResultDialogText = MutableStateFlow("")
    private val showPermissionsDialog = MutableStateFlow(false)
    private val isLoading = MutableStateFlow(false)

    var locationManager: LocationManager = this.application.getSystemService(Context.LOCATION_SERVICE) as LocationManager // Initialize LocationManager
    var wifiManager: WifiManager = this.application.getSystemService(Context.WIFI_SERVICE) as WifiManager // Initialize WifiManager
    lateinit var wifiRTTManager: WifiRttManager
    var scanResultList = mutableListOf<ScanResult>()

    private val wifiScanReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission") // Permissions are checked before starting to scan
        override fun onReceive(context: Context, intent: Intent) {
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            if (!success) {
                Toast.makeText(context, "Scanning of Access Points failed", Toast.LENGTH_LONG).show()
            }

            scanResultList = wifiManager.scanResults
            isLoading.value = false
            accessPointsList.value = scanResultList.map { sr -> AccessPoint(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) sr.wifiSsid.toString() else sr.SSID, sr.BSSID, sr.is80211mcResponder, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) sr.is80211azNtbResponder else null, sr) }
            selectedForRTT.value = emptyList()
        }
    }

    private val wifiRTTStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (!wifiRTTManager.isAvailable) {
                Toast.makeText(context, "WiFi RTT is not available", Toast.LENGTH_LONG).show()
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
        // check if location is enabled (needed for scanning APs)
        if (!locationManager.isLocationEnabled) {
            Toast.makeText(this.application, "Please enable location services", Toast.LENGTH_LONG).show()
            return
        }

        isLoading.value = true
        val success = wifiManager.startScan()
        if (!success) {
            Toast.makeText(this.application, "Starting Scanning of Access Points failed", Toast.LENGTH_LONG).show()
            isLoading.value = false
        }
    }

    override fun observeAccessPointsList(): Flow<List<AccessPoint>> = accessPointsList.asStateFlow()

    override fun observeSelectedForRTT(): Flow<List<AccessPoint>> = selectedForRTT.asStateFlow()

    override fun observeRTTRangingResults(): Flow<List<RangingResult>> = rttRangingResults.asStateFlow()

    override fun observeRTTRangingResultsForExport(): Flow<List<RangingResultWithTimestamps>> = rttRangingResultsForExport.asStateFlow()

    override fun observeRTTResultDialogText(): Flow<String> = rttResultDialogText.asStateFlow()

    override fun observeShowPermissionsDialog(): Flow<Boolean> = showPermissionsDialog.asStateFlow()

    override fun observeIsLoading(): Flow<Boolean> = isLoading.asStateFlow()

    @SuppressLint("MissingPermission")
    override suspend fun createRTTRangingRequest(selectedForRTT: List<AccessPoint>, saveRttResults: Boolean, startTime: Long) {
        wifiRTTManager = this.application.getSystemService(Context.WIFI_RTT_RANGING_SERVICE) as WifiRttManager // Initialize WifiRttManager
        // Create a ranging request
        val req: RangingRequest = RangingRequest.Builder().run {
            addAccessPoints(selectedForRTT.map { it.scanResultObject })
            build()
        }
        // Request ranging
        wifiRTTManager.startRanging(req, this.application.mainExecutor, object : RangingResultCallback() {
            // Callback that triggers when the ranging operation completes
            override fun onRangingResults(results: List<RangingResult>) {
                rttRangingResults.value = results
                if (saveRttResults) {
                    rttRangingResultsForExport.update { it + RangingResultWithTimestamps(startTime, System.currentTimeMillis(), results) } // Save results to export to CSV if user setting is enabled
                }
            }

            // Callback that triggers when the whole ranging operation fails
            override fun onRangingFailure(code: Int) {
                rttResultDialogText.value = "RTT Ranging Request failed"
            }
        })
    }

    override suspend fun startRTTRanging(selectedForRTT: List<AccessPoint>, performContinuousRttRanging: Boolean, rttPeriod: Long, rttInterval: Long, saveRttResults: Boolean, saveOnlyLastRttOperation: Boolean) {
        //wifiRTTManager = this.application.getSystemService(Context.WIFI_RTT_RANGING_SERVICE) as WifiRttManager // Initialize WifiRttManager

        // Check whether the device supports WiFi RTT
        val supportsRttMc = this.application.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_RTT)
        //val supportsRttAz = Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM && wifiRTTManager.rttCharacteristics.getBoolean(WifiRttManager.CHARACTERISTICS_KEY_BOOLEAN_NTB_INITIATOR)
        val supportsRttAz = false // TODO: fix wifiRTTManager not available here
        if (!supportsRttMc && !supportsRttAz) {
            rttResultDialogText.value = "Device does not support WiFi RTT"
            return
        } else {
            if (supportsRttMc) saveCompatibleDevice("mc")
            if (supportsRttAz) saveCompatibleDevice("az")
        }

        if (saveOnlyLastRttOperation) {
            rttRangingResultsForExport.value = emptyList()
        }

        if (performContinuousRttRanging) {
            val endTime = System.currentTimeMillis() + rttPeriod
            while (System.currentTimeMillis() < endTime) {
                createRTTRangingRequest(selectedForRTT, saveRttResults, System.currentTimeMillis())
                delay(rttInterval) // Delay between requests
            }
        } else {
            createRTTRangingRequest(selectedForRTT, saveRttResults, System.currentTimeMillis())
        }
    }

    override suspend fun showPermissionsDialog() {
        showPermissionsDialog.value = true
    }

    override suspend fun removeDialogs() {
        rttResultDialogText.value = ""
        showPermissionsDialog.value = false
    }

    private fun saveCompatibleDevice(standard: String) {
        // The device information will be stored in Firestore
        val db = Firebase.firestore
        val device = RTTCompatibleDevice(
            lastChecked = System.currentTimeMillis().toInt(),
            model = Build.MODEL,
            manufacturer = Build.MANUFACTURER,
            androidVersion = Build.VERSION.RELEASE,
            standard = standard
        )

        val compositeId = "${device.manufacturer}-${device.model}-${device.androidVersion}-${device.standard}"
        db.collection("compatible-devices")
            .document(compositeId)
            .set(device)
            .addOnSuccessListener {
                Log.d("TestDavid", "Device added with composite ID: $compositeId")
            }
            .addOnFailureListener { e ->
                Toast.makeText(this.application, "Error adding device to Firestore", Toast.LENGTH_LONG).show()
            }
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



