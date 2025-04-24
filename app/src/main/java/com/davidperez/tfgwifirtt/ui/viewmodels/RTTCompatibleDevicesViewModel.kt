package com.davidperez.tfgwifirtt.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidperez.tfgwifirtt.data.RTTCompatibleDevicesRepository
import com.davidperez.tfgwifirtt.model.RTTCompatibleDevice
import com.davidperez.tfgwifirtt.model.RTTCompatibleDevicesFilters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the RTT-capable devices list screen
 */
data class RTTCompatibleDevicesUiState(
    val rttCompatibleDevicesList: List<RTTCompatibleDevice> = emptyList(),
    val rttCompatibleDevicesFilters: RTTCompatibleDevicesFilters = RTTCompatibleDevicesFilters(),
    val rttCompatibleDevicesListFiltered: List<RTTCompatibleDevice> = emptyList()
)

@HiltViewModel
class RTTCompatibleDevicesViewModel @Inject constructor(
    private val rttCompatibleDevicesRepository: RTTCompatibleDevicesRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(RTTCompatibleDevicesUiState())
    val uiState: StateFlow<RTTCompatibleDevicesUiState> = _uiState.asStateFlow()

    init {
        getRTTCompatibleDevices()
        observeRTTCompatibleDevicesList()
    }

    private fun observeRTTCompatibleDevicesList() {
        viewModelScope.launch {
            rttCompatibleDevicesRepository.observeRTTCompatibleDevicesList().collect { rttCompatibleDevicesListObserved ->
                _uiState.update { currentState ->
                    currentState.copy(
                        rttCompatibleDevicesList = rttCompatibleDevicesListObserved
                    )
                }
                applyRTTCompatibleDevicesFilters(_uiState.value.rttCompatibleDevicesFilters)
            }
        }
    }

    /**
     * Get compatible devices from db and update the UI state
     */
    private fun getRTTCompatibleDevices() {
        viewModelScope.launch {
            rttCompatibleDevicesRepository.getRTTCompatibleDevices()
        }
    }

    /**
     * Functions to update filters
     */
    fun updateDeviceQuery(deviceQuery: String) {
        applyRTTCompatibleDevicesFilters(
            _uiState.value.rttCompatibleDevicesFilters.copy(
                deviceQuery = deviceQuery
            )
        )
    }

    fun updateAndroidVersionFilter(androidVersion: String?) {
        applyRTTCompatibleDevicesFilters(
            _uiState.value.rttCompatibleDevicesFilters.copy(
                androidVersion = androidVersion
            )
        )
    }

    fun toggleMcFilter() {
        applyRTTCompatibleDevicesFilters(
            _uiState.value.rttCompatibleDevicesFilters.copy(
                showMc = !_uiState.value.rttCompatibleDevicesFilters.showMc
            )
        )
    }

    fun toggleAzFilter() {
        applyRTTCompatibleDevicesFilters(
            _uiState.value.rttCompatibleDevicesFilters.copy(
                showAz = !_uiState.value.rttCompatibleDevicesFilters.showAz
            )
        )
    }

    private fun applyRTTCompatibleDevicesFilters(filters: RTTCompatibleDevicesFilters = RTTCompatibleDevicesFilters()): List<RTTCompatibleDevice> {
        _uiState.update { currentState ->
            val filtered = currentState.rttCompatibleDevicesList.filter { item ->
                (item.model + " " + item.manufacturer).contains(filters.deviceQuery, ignoreCase = true) &&
                (filters.androidVersion == null || item.androidVersion == filters.androidVersion) &&
                ((filters.showMc && item.standard == "mc") || (filters.showAz && item.standard == "az"))
            }
            currentState.copy(
                rttCompatibleDevicesFilters = filters,
                rttCompatibleDevicesListFiltered = filtered
            )
        }
        return _uiState.value.rttCompatibleDevicesListFiltered
    }
}