package com.davidperez.tfgwifirtt.ui.viewmodels

import android.net.wifi.ScanResult
import android.net.wifi.rtt.RangingResult
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidperez.tfgwifirtt.data.AccessPointsRepository
import com.davidperez.tfgwifirtt.data.UserSettingsRepository
import com.davidperez.tfgwifirtt.model.AccessPoint
import com.davidperez.tfgwifirtt.model.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val userSettings: UserSettings = UserSettings(),
    val selectedForRTT: Set<ScanResult> = emptySet(),
    val rttRangingResults: List<RangingResult> = emptyList(),
    val rttResultDialogText: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String = "",
)

/**
 * ViewModel that handles the logic of the access points screen
 */
@HiltViewModel
class AccessPointsViewModel @Inject constructor(
    private val accessPointsRepository: AccessPointsRepository,
    private val userSettingsRepository: UserSettingsRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(AccessPointsUiState())
    val uiState: StateFlow<AccessPointsUiState> = _uiState.asStateFlow()

    init {
        observeAccessPointsList() // Observe for changes in the scanned access points
        observeSelectedForRTT() // Observe for changes in the selected APs for RTT
        observeRTTRangingResults()
        observeRTTResultDialogText()
        observeIsLoading()
        observeUserSettings()
    }

    private fun observeUserSettings() {
        viewModelScope.launch {
            userSettingsRepository.getUserSettings().collect { userSettingsObserved ->
                _uiState.update { currentState ->
                    currentState.copy(
                        userSettings = userSettingsObserved
                    )
                }
            }
        }
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

    private fun observeIsLoading() {
        viewModelScope.launch {
            accessPointsRepository.observeIsLoading().collect { isLoadingObserved ->
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = isLoadingObserved
                    )
                }
            }
        }
    }

    /**
     * Refresh access points and update the UI state
     */
    fun refreshAccessPoints() {
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
     * Start RTT ranging to the selected APs
     */
    fun startRTTRanging(selectedForRTT: Set<ScanResult>, performContinuousRttRanging: Boolean, rttPeriod: Long, rttInterval: Long, saveRttResults: Boolean, saveOnlyLastRttOperation: Boolean) {
        viewModelScope.launch {
            accessPointsRepository.startRTTRanging(selectedForRTT, performContinuousRttRanging, rttPeriod, rttInterval, saveRttResults, saveOnlyLastRttOperation)
        }
    }

    /**
     * Export RTT results to CSV
     */
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