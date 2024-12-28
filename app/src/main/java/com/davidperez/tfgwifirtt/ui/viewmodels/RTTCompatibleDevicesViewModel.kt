package com.davidperez.tfgwifirtt.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidperez.tfgwifirtt.data.RTTCompatibleDevicesRepository
import com.davidperez.tfgwifirtt.model.RTTCompatibleDevice
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
data class RTTCompatibleDevicesUiState(
    val rttCompatibleDevicesList: List<RTTCompatibleDevice> = emptyList()
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

}