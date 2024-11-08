package com.davidperez.tfgwifirtt.ui

import android.net.wifi.ScanResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidperez.tfgwifirtt.data.AccessPointsRepository
import com.davidperez.tfgwifirtt.model.AccessPoint
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
    val selectedForRTT: Set<ScanResult> = emptySet(),
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
}