package com.davidperez.tfgwifirtt.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.davidperez.tfgwifirtt.model.RTTCompatibleDevice
import com.davidperez.tfgwifirtt.model.RTTCompatibleDevicesFilters
import com.davidperez.tfgwifirtt.ui.components.ScreenTitle
import com.davidperez.tfgwifirtt.ui.viewmodels.RTTCompatibleDevicesViewModel
import androidx.compose.runtime.setValue


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
                onModelQueryChanged = { rttCompatibleDevicesViewModel.updateModelQuery(it) },
                onVersionSelected = { rttCompatibleDevicesViewModel.updateAndroidVersionFilter(it) }
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
    onModelQueryChanged: (String) -> Unit,
    onVersionSelected: (String?) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        OutlinedTextField(
            value = filters.modelQuery,
            onValueChange = { onModelQueryChanged(it) },
            label = { Text("Search by Model") },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        AndroidVersionDropdown(
            androidVersions = rttCompatibleDevicesList.map { it.androidVersion }.distinct().sortedDescending(),
            selectedVersion = filters.androidVersion,
            onItemSelected = { onVersionSelected(it) },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroidVersionDropdown(
    androidVersions: List<String>,
    selectedVersion: String?,
    onItemSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = selectedVersion ?: "All"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.padding(horizontal = 5.dp)
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selectedText,
            onValueChange = {},
            label = { Text("Android version") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("All") },
                onClick = {
                    onItemSelected(null)
                    expanded = false
                }
            )
            androidVersions.forEach { version ->
                DropdownMenuItem(
                    text = { Text(version) },
                    onClick = {
                        onItemSelected(version)
                        expanded = false
                    }
                )
            }
        }
    }
}

