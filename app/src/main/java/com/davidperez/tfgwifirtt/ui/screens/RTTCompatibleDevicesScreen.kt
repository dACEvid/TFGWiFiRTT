package com.davidperez.tfgwifirtt.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.Shape
import com.davidperez.tfgwifirtt.ui.components.CompatibilityBadge


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompatibleDevicesListScreen(
    rttCompatibleDevicesViewModel: RTTCompatibleDevicesViewModel = hiltViewModel()
) {
    val rttCompatibleDevicesUiState by rttCompatibleDevicesViewModel.uiState.collectAsState()

    LazyColumn {
        item {
            FilterSection(
                filters = rttCompatibleDevicesUiState.rttCompatibleDevicesFilters,
                rttCompatibleDevicesList = rttCompatibleDevicesUiState.rttCompatibleDevicesList,
                onDeviceQueryChanged = { rttCompatibleDevicesViewModel.updateDeviceQuery(it) },
                onVersionSelected = { rttCompatibleDevicesViewModel.updateAndroidVersionFilter(it) },
                onMcFilterToggled = { rttCompatibleDevicesViewModel.toggleMcFilter() },
                onAzFilterToggled = { rttCompatibleDevicesViewModel.toggleAzFilter() }
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
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                Row {
                    Text("Device: ")
                    Text(
                        text = cd.manufacturer + " " + cd.model,
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
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CompatibilityBadge(cd.standard)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSection(
    filters: RTTCompatibleDevicesFilters,
    rttCompatibleDevicesList: List<RTTCompatibleDevice>,
    onDeviceQueryChanged: (String) -> Unit,
    onVersionSelected: (String?) -> Unit,
    onMcFilterToggled: () -> Unit,
    onAzFilterToggled: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        OutlinedTextField(
            value = filters.deviceQuery,
            onValueChange = { onDeviceQueryChanged(it) },
            label = { Text("Search by Device") },
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
    Row (
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ){
        FilterChip(
            label = { Text("802.11mc") },
            selected = filters.showMc,
            onClick = { onMcFilterToggled() },
            leadingIcon = if (filters.showMc) {
                {
                    Icon(
                        imageVector = Icons.Filled.Done,
                        contentDescription = "Done icon",
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            } else {
                null
            }
//            modifier = Modifier
//                .shadow(
//                    elevation = 15.dp,
//                    shape = FilterChipDefaults.shape,
//                    spotColor = DefaultShadowColor
//                )
        )
        FilterChip(
            label = { Text("802.11az") },
            selected = filters.showAz,
            onClick = { onAzFilterToggled() },
            leadingIcon = if (filters.showAz) {
                {
                    Icon(
                        imageVector = Icons.Filled.Done,
                        contentDescription = "Done icon",
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            } else {
                null
            }
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

