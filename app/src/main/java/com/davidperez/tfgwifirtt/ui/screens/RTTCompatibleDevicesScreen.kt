package com.davidperez.tfgwifirtt.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.davidperez.tfgwifirtt.model.RTTCompatibleDevice
import com.davidperez.tfgwifirtt.ui.components.TitleWithDivider
import com.davidperez.tfgwifirtt.ui.viewmodels.RTTCompatibleDevicesViewModel


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompatibleDevicesListScreen(
    rttCompatibleDevicesViewModel: RTTCompatibleDevicesViewModel = hiltViewModel()
) {
    val rttCompatibleDevicesUiState by rttCompatibleDevicesViewModel.uiState.collectAsState()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        stickyHeader {
            TitleWithDivider("RTT-capable Devices")
        }
        items(rttCompatibleDevicesUiState.rttCompatibleDevicesList) { cd ->
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
            Text("Manufacturer: " + cd.manufacturer)
            Text("Model: " + cd.model)
            Text("Android Version: " + cd.androidVersion)
        }
    }
}