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
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
     * Observe the access points that have been selected for RTT
     */
    fun observeSelectedForRTT(): Flow<Set<ScanResult>>

    /**
     * Observe the access points.
     */
    fun observeAccessPointsList(): Flow<List<AccessPoint>>

    /**
     * Toggle an access point to be selected for RTT or not.
     */
    suspend fun toggleSelectionForRTT(accessPointScanResult: ScanResult)

    /**
     * Create RTT ranging request for the selected APs
     */
    suspend fun createRTTRangingRequest(selectedForRTT: Set<ScanResult>)
}

class AccessPointsRepositoryImpl @Inject constructor(private val application: Application) : AccessPointsRepository {
    private val selectedForRTT = MutableStateFlow<Set<ScanResult>>(setOf())
    private val accessPointsList = MutableStateFlow<List<AccessPoint>>(emptyList())
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
        //ContextCompat.registerReceiver(wifiScanReceiver., wifiScanReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION), ContextCompat.RECEIVER_NOT_EXPORTED)

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
            Log.e("TestDavid", "Device does not support WiFi RTT")
            return
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
                Log.d("TestDavid", "RTT Ranging Results: $results")
            }

            // Callback that triggers when the whole ranging operation fails
            override fun onRangingFailure(code: Int) {
                Log.e("TestDavid", "RTT Ranging Request failed")
            }
        })
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

