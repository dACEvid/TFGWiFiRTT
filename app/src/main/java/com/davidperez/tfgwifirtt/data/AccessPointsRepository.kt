package com.davidperez.tfgwifirtt.data

import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
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
     * Toggle an access point to be selected for RTT or not.
     */
    suspend fun createRTTRangingRequest(selectedForRTT: Set<ScanResult>)
}

class AccessPointsRepositoryImpl @Inject constructor(private val application: Application) : AccessPointsRepository {
    private val selectedForRTT = MutableStateFlow<Set<ScanResult>>(setOf())
    private val accessPointsList = MutableStateFlow<List<AccessPoint>>(emptyList())
    var wifiManager: WifiManager = this.application.getSystemService(Context.WIFI_SERVICE) as WifiManager // Initialize WiFiManager
    var scanResultList = mutableListOf<ScanResult>()

    private val wifiScanReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission") // Permissions are checked before starting to scan
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("Test", "onReceive Called")
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            if (!success) {
                Log.e("TestDavid", "Scanning of Access Points failed")
            }

            scanResultList = wifiManager.scanResults
            Log.d("TestDavid", scanResultList.joinToString())
            accessPointsList.value = scanResultList.map { sr -> AccessPoint(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) sr.wifiSsid.toString() else sr.SSID, sr.BSSID, sr.is80211mcResponder, sr) }
        }
    }

    init {
        // Register a broadcast listener for SCAN_RESULTS_AVAILABLE_ACTION, which is called when scan requests are completed
        this.application.registerReceiver(wifiScanReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        //ContextCompat.registerReceiver(wifiScanReceiver., wifiScanReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION), ContextCompat.RECEIVER_NOT_EXPORTED)
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

    override suspend fun createRTTRangingRequest(selectedForRTT: Set<ScanResult>) {
        // TODO: implement logic for RTT ranging request
        Log.d("TestDavid", "RTT Ranging Request function")
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

