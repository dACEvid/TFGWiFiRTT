package com.davidperez.tfgwifirtt.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidperez.tfgwifirtt.data.UserSettingsRepository
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
    val userSettings: UserSettings = UserSettings()
)

@HiltViewModel
class UserSettingsViewModel @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(UserSettingsUiState())
    val uiState: StateFlow<UserSettingsUiState> = _uiState.asStateFlow()

    init {
        getUserSettings()
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

    fun setIgnoreRttPeriod(value: Boolean) {
        viewModelScope.launch {
            userSettingsRepository.setIgnoreRttPeriod(value)
        }
    }

    fun setSaveRttResults(value: Boolean) {
        viewModelScope.launch {
            userSettingsRepository.setSaveRttResults(value)
        }
    }

    fun setSaveLastRttOperationOnly(value: Boolean) {
        viewModelScope.launch {
            userSettingsRepository.setSaveLastRttOperationOnly(value)
        }
    }

    fun setRttPeriod(value: Long) {
        viewModelScope.launch {
            userSettingsRepository.setRttPeriod(value)
        }
    }

    fun setRttInterval(value: Long) {
        viewModelScope.launch {
            userSettingsRepository.setRttInterval(value)
        }
    }

}