package com.davidperez.tfgwifirtt.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.ScanResult
import android.net.wifi.rtt.RangingResult
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidperez.tfgwifirtt.data.AccessPointsRepository
import com.davidperez.tfgwifirtt.model.AccessPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the Access Points
 */
data class AccessPointsUiState(
    val accessPointsList: List<AccessPoint> = emptyList(),
    val selectedForRTT: Set<ScanResult> = emptySet(),
    val rttRangingResults: List<RangingResult> = emptyList(),
    val rttResultDialogText: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String = ""
)

/**
 * ViewModel that handles the logic of the access points screen
 */
@HiltViewModel
class AccessPointsViewModel @Inject constructor(
    private val accessPointsRepository: AccessPointsRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(AccessPointsUiState())
    val uiState: StateFlow<AccessPointsUiState> = _uiState.asStateFlow()

    init {
        observeAccessPointsList() // Observe for changes in the scanned access points
        refreshAccessPoints() // Start scan
        observeSelectedForRTT() // Observe for changes in the selected APs for RTT
        observeRTTRangingResults()
        observeRTTResultDialogText()
    }

    private fun observeAccessPointsList() {
        viewModelScope.launch {
            accessPointsRepository.observeAccessPointsList().collect { accessPointListObserved ->
                _uiState.update { currentState ->
                    currentState.copy(
                        accessPointsList = accessPointListObserved
                    )
                }
            }
        }
    }

    private fun observeSelectedForRTT() {
        viewModelScope.launch {
            accessPointsRepository.observeSelectedForRTT().collect { selectedForRTTObserved ->
                _uiState.update { currentState ->
                    currentState.copy(
                        selectedForRTT = selectedForRTTObserved
                    )
                }
            }
        }
    }

    private fun observeRTTRangingResults() {
        viewModelScope.launch {
            accessPointsRepository.observeRTTRangingResults().collect { rttRangingResultsObserved ->
                _uiState.update { currentState ->
                    currentState.copy(
                        rttRangingResults = rttRangingResultsObserved
                    )
                }
            }
        }
    }

    private fun observeRTTResultDialogText() {
        viewModelScope.launch {
            accessPointsRepository.observeRTTResultDialogText().collect { rttResultDialogTextObserved ->
                _uiState.update { currentState ->
                    currentState.copy(
                        rttResultDialogText = rttResultDialogTextObserved
                    )
                }
            }
        }
    }

    /**
     * Refresh access points and update the UI state
     */
    fun refreshAccessPoints() {
        _uiState.value = AccessPointsUiState(isLoading = true) // Show loading

        viewModelScope.launch {
            accessPointsRepository.scanAccessPoints()
        }
    }

    /**
     * Toggle selection of an access point for RTT
     */
    fun toggleSelectionForRTT(accessPointScanResult: ScanResult) {
        viewModelScope.launch {
            accessPointsRepository.toggleSelectionForRTT(accessPointScanResult)
        }
    }

    /**
     * Create RTT ranging request for the selected APs
     */
    fun createRTTRangingRequest(selectedForRTT: Set<ScanResult>) {
        viewModelScope.launch {
            accessPointsRepository.createRTTRangingRequest(selectedForRTT)
        }
    }

    fun doContinuousRTTRanging(selectedForRTT: Set<ScanResult>) {
        viewModelScope.launch {
            accessPointsRepository.doContinuousRTTRanging(selectedForRTT, 20000, 500) // Ranging time hardcoded to 20 seconds and intervals to 0.5 seconds
        }
    }

    fun exportRTTRangingResultsToCsv(rttRangingResults: List<RangingResult>): String {
        val csvContent = buildString {
            appendLine("Timestamp,Device,Android Version,AP MAC Address,Status,Distance (mm),Std Dev (mm),Attempted Measurements,Successful Measurements,Bandwidth,Frequency (MHz)")
            for (result in rttRangingResults) {
                if (result.status == RangingResult.STATUS_SUCCESS) {
                    appendLine("${result.rangingTimestampMillis},${Build.MANUFACTURER + Build.MODEL},${Build.VERSION.RELEASE},${result.macAddress.toString()},${result.status},${result.distanceMm},${result.distanceStdDevMm},${result.numAttemptedMeasurements},${result.numSuccessfulMeasurements}")
                } else {
                    appendLine(",${Build.MANUFACTURER + Build.MODEL},${Build.VERSION.RELEASE},${result.macAddress.toString()},${result.status},,,,")
                }
            }
        }
        return csvContent
    }

    fun removeRTTResultDialog() {
        viewModelScope.launch {
            accessPointsRepository.removeRTTResultDialog()
        }
    }
}