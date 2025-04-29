package com.davidperez.tfgwifirtt.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.davidperez.tfgwifirtt.ui.viewmodels.AccessPointsViewModel


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RTTResultsScreen(
    accessPointsViewModel: AccessPointsViewModel = hiltViewModel()
) {
    val accessPointsUiState by accessPointsViewModel.uiState.collectAsState()
    val selectedForRTT = accessPointsUiState.selectedForRTT.toList()

    LazyColumn {
        stickyHeader {
            Text("RTT Results")
        }
        items(selectedForRTT) { ap ->
            Text("Item")
        }
    }
}
