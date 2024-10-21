package com.davidperez.tfgwifirtt.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
    val selectedForRTT: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val errorMessage: String = ""
)

/**
 * UI state for the Access Points
 */
//class MutableAccessPointsUiState: AccessPointsUiState {
//    override var accessPointsList: List<AccessPoint> by mutableStateOf(emptyList())
//    override var selectedForRTT: Set<String> by mutableStateOf(emptySet())
//    override var isLoading: Boolean by mutableStateOf(false)
//    override var errorMessage: String by mutableStateOf("")
//}

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
    fun toggleSelectionForRTT(accessPointSsid: String) {
        viewModelScope.launch {
            accessPointsRepository.toggleSelectionForRTT(accessPointSsid)
        }
    }

//    /**
//     * Factory for AccessPointsViewModel that takes AccessPointsRepository as a dependency
//     */
//    companion object {
//        fun provideFactory(
//            accessPointsRepository: AccessPointsRepository
//        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
//            @Suppress("UNCHECKED_CAST")
//            override fun <T : ViewModel> create(modelClass: Class<T>): T {
//                return AccessPointsViewModel(accessPointsRepository) as T
//            }
//        }
//    }
}