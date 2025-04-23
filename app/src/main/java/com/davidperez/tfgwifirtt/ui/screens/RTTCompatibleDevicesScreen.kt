package com.davidperez.tfgwifirtt.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.davidperez.tfgwifirtt.model.RTTCompatibleDevice
import com.davidperez.tfgwifirtt.model.RTTCompatibleDevicesFilters
import com.davidperez.tfgwifirtt.ui.components.ScreenTitle
import com.davidperez.tfgwifirtt.ui.viewmodels.RTTCompatibleDevicesViewModel


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompatibleDevicesListScreen(
    rttCompatibleDevicesViewModel: RTTCompatibleDevicesViewModel = hiltViewModel()
) {
    val rttCompatibleDevicesUiState by rttCompatibleDevicesViewModel.uiState.collectAsState()

    // TODO: add filters by standard, manufacturer, etc. Check Chip Compose component


    LazyColumn {
        stickyHeader {
            ScreenTitle("RTT-capable Devices")
        }
        item {
            FilterSection(
                filters = rttCompatibleDevicesUiState.rttCompatibleDevicesFilters,
                rttCompatibleDevicesList = rttCompatibleDevicesUiState.rttCompatibleDevicesList,
                onModelQueryChanged = { rttCompatibleDevicesViewModel.updateModelQuery(it) }
            )
        }
        items(rttCompatibleDevicesUiState.rttCompatibleDevicesListFiltered) { cd ->
            CompatibleDeviceItem(cd)
        }
    }
}

@Composable
fun CompatibleDeviceItem(cd: RTTCompatibleDevice) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Row {
                Text("Manufacturer: ")
                Text(
                    text = cd.manufacturer,
                    fontWeight = FontWeight.Bold
                )
            }
            Row {
                Text("Model: ")
                Text(
                    text = cd.model,
                    fontWeight = FontWeight.Bold
                )
            }
            Row {
                Text("Android Version: ")
                Text(
                    text = cd.androidVersion,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSection(
    filters: RTTCompatibleDevicesFilters,
    rttCompatibleDevicesList: List<RTTCompatibleDevice>,
    onModelQueryChanged: (String) -> Unit
) {
    val androidVersions = rttCompatibleDevicesList.map { it.androidVersion }.distinct().sortedDescending()
    Row {
        OutlinedTextField(
            value = filters.modelQuery,
            onValueChange = { onModelQueryChanged(it) },
            label = { Text("Search by Model") },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )
    }
}
