package com.davidperez.tfgwifirtt.ui.viewmodels

import android.net.wifi.rtt.RangingResult
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidperez.tfgwifirtt.data.AccessPointsRepository
import com.davidperez.tfgwifirtt.data.UserSettingsRepository
import com.davidperez.tfgwifirtt.model.AccessPoint
import com.davidperez.tfgwifirtt.model.RangingResultWithTimestamps
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
    val selectedForRTT: List<AccessPoint> = emptyList(),
    val rttRangingResults: List<RangingResult> = emptyList(),
    val rttRangingResultsForExport: List<RangingResultWithTimestamps> = emptyList(),
    val errorMsg: String = "",
    val showPermissionsDialog: Boolean = false,
    val isLoading: Boolean = false
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
        observeRTTRangingResultsForExport()
        observeErrorMsg()
        observeShowPermissionsDialog()
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

    private fun observeRTTRangingResultsForExport() {
        viewModelScope.launch {
            accessPointsRepository.observeRTTRangingResultsForExport().collect { rttRangingResultsForExportObserved ->
                _uiState.update { currentState ->
                    currentState.copy(
                        rttRangingResultsForExport = rttRangingResultsForExportObserved
                    )
                }
            }
        }
    }

    private fun observeErrorMsg() {
        viewModelScope.launch {
            accessPointsRepository.observeErrorMsg().collect { errorMsgObserved ->
                _uiState.update { currentState ->
                    currentState.copy(
                        errorMsg = errorMsgObserved
                    )
                }
            }
        }
    }

    private fun observeShowPermissionsDialog() {
        viewModelScope.launch {
            accessPointsRepository.observeShowPermissionsDialog().collect { showPermissionsDialogObserved ->
                _uiState.update { currentState ->
                    currentState.copy(
                        showPermissionsDialog = showPermissionsDialogObserved
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
    fun toggleSelectionForRTT(accessPoint: AccessPoint) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                val updatedList = currentState.accessPointsList.map {
                    if (it.bssid == accessPoint.bssid) it.copy(selectedForRTT = !it.selectedForRTT) else it
                }
                currentState.copy(accessPointsList = updatedList, selectedForRTT = updatedList.filter { it.selectedForRTT })
            }
        }
    }

    /**
     * Start RTT ranging to the selected APs
     */
    fun startRTTRanging(selectedForRTT: List<AccessPoint>, performContinuousRttRanging: Boolean, rttPeriod: Long, rttInterval: Long, saveRttResults: Boolean, saveOnlyLastRttOperation: Boolean) {
        viewModelScope.launch {
            accessPointsRepository.startRTTRanging(selectedForRTT, performContinuousRttRanging, rttPeriod, rttInterval, saveRttResults, saveOnlyLastRttOperation)
        }
    }

    /**
     * Export RTT results to CSV
     */
    fun exportRTTRangingResultsToCsv(rttRangingResults: List<RangingResultWithTimestamps>): String {
        val csvContent = buildString {
            appendLine("Request Start Unix Timestamp,Request Completed Unix Timestamp,Time Since System Boot (ms),Device,Android Version,AP MAC Address,Status,Distance (mm),Std Dev (mm),Attempted Measurements,Successful Measurements,Bandwidth,Frequency (MHz),Average RSSI (dbM)")
            for (result in rttRangingResults) {
                for (r in result.results) {
                    if (r.status == RangingResult.STATUS_SUCCESS) {
                        val bandwidth =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) r.measurementBandwidth else "Requires Android >= 14"
                        val frequency =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) r.measurementChannelFrequencyMHz else "Requires Android >= 14"
                        appendLine("${result.startTime},${result.endTime},${r.rangingTimestampMillis},${Build.MANUFACTURER + Build.MODEL},${Build.VERSION.RELEASE},${r.macAddress.toString()},${r.status},${r.distanceMm},${r.distanceStdDevMm},${r.numAttemptedMeasurements},${r.numSuccessfulMeasurements},${bandwidth},${frequency},${r.rssi}")
                    } else {
                        appendLine("${result.startTime},,,${Build.MANUFACTURER + Build.MODEL},${Build.VERSION.RELEASE},${r.macAddress.toString()},${r.status},,,,,")
                    }
                }
            }
        }
        return csvContent
    }

    fun removeDialogs() {
        viewModelScope.launch {
            accessPointsRepository.removeDialogs()
        }
    }

    fun showPermissionsDialog() {
        viewModelScope.launch {
            accessPointsRepository.showPermissionsDialog()
        }
    }
}