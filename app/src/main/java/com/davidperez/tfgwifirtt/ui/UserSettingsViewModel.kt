package com.davidperez.tfgwifirtt.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidperez.tfgwifirtt.data.RTTCompatibleDevicesRepository
import com.davidperez.tfgwifirtt.data.UserSettingsRepository
import com.davidperez.tfgwifirtt.model.RTTCompatibleDevice
import com.davidperez.tfgwifirtt.model.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the User Settings
 */
data class UserSettingsUiState(
    val userSettings: UserSettings = UserSettings(),
    val isLoading: Boolean = false,
    val errorMessage: String = ""
)

@HiltViewModel
class UserSettingsViewModel @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(UserSettingsUiState())
    val uiState: StateFlow<UserSettingsUiState> = _uiState.asStateFlow()

    init {
        getUserSettings()
        //observeUserSettings()
    }

    private fun getUserSettings() {
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

//    fun getUserSettings() {
//        viewModelScope.launch {
//            userSettingsRepository.getUserSettings()
//        }
//    }

    fun setUserSettings(userSettings: UserSettings) {
        viewModelScope.launch {
            userSettingsRepository.setUserSettings(
                userSettings
//                UserSettings(
//                    showOnlyRttCompatibleAps = uiState.value.userSettings.showOnlyRttCompatibleAps,
//                    performSingleRttRequest = uiState.value.userSettings.performSingleRttRequest,
//                    rttPeriod = uiState.value.userSettings.rttPeriod,
//                    rttInterval = uiState.value.userSettings.rttInterval,
//                    saveRttResults = uiState.value.userSettings.saveRttResults
//                )
            )
        }
    }

    fun setShowRTTCompatibleOnly(value: Boolean) {
        viewModelScope.launch {
            userSettingsRepository.setShowRTTCompatibleOnly(value)
        }
    }

    fun setPerformContinuousRttRanging(value: Boolean) {
        viewModelScope.launch {
            userSettingsRepository.setPerformContinuousRttRanging(value)
        }
    }

    fun setSaveRttResults(value: Boolean) {
        viewModelScope.launch {
            userSettingsRepository.setSaveRttResults(value)
        }
    }

    fun setRttPeriod(value: Int) {
        viewModelScope.launch {
            userSettingsRepository.setRttPeriod(value)
        }
    }

    fun setRttInterval(value: Int) {
        viewModelScope.launch {
            userSettingsRepository.setRttInterval(value)
        }
    }

}