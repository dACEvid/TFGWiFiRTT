package com.davidperez.tfgwifirtt.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxColors
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Slider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.davidperez.tfgwifirtt.ui.components.ScreenTitle
import com.davidperez.tfgwifirtt.ui.viewmodels.UserSettingsViewModel


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsScreen(
    userSettingsViewModel: UserSettingsViewModel = hiltViewModel()
) {
    val userSettingsUiState by userSettingsViewModel.uiState.collectAsState()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        stickyHeader {
            ScreenTitle("Settings")
        }
        item {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Only Show RTT-Compatible APs")
                Checkbox(
                    checked = userSettingsUiState.userSettings.showOnlyRttCompatibleAps,
                    onCheckedChange = { userSettingsViewModel.setShowRTTCompatibleOnly(it) },
                    colors = myCheckBoxColors()
                )
            }
        }
        item {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Perform Continuous RTT Ranging")
                Checkbox(
                    checked = userSettingsUiState.userSettings.performContinuousRttRanging,
                    onCheckedChange = { userSettingsViewModel.setPerformContinuousRttRanging(it) },
                    colors = myCheckBoxColors()
                )
            }
        }
        item {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("RTT Ranging Period (s): ")
                Text(text = userSettingsUiState.userSettings.rttPeriod.toString())
            }
        }
        item {
            Slider(
                modifier = Modifier.padding(10.dp),
                value = userSettingsUiState.userSettings.rttPeriod.toFloat(),
                onValueChange = { userSettingsViewModel.setRttPeriod(it.toLong()) },
                valueRange = 5f..30f,
                steps = 24,
                enabled = userSettingsUiState.userSettings.performContinuousRttRanging
            )
        }
        item {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Interval between RTT Requests (ms): ")
                Text(text = userSettingsUiState.userSettings.rttInterval.toString())
            }
        }
        item {
            Slider(
                modifier = Modifier.padding(10.dp),
                value = userSettingsUiState.userSettings.rttInterval.toFloat(),
                onValueChange = {
                    val snappedValue = (it/50).toLong() * 50
                    userSettingsViewModel.setRttInterval(snappedValue)
                },
                valueRange = 50f..1000f,
                steps = 18,
                enabled = userSettingsUiState.userSettings.performContinuousRttRanging
            )
        }
        item {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Save RTT Results For Export")
                Checkbox(
                    checked = userSettingsUiState.userSettings.saveRttResults,
                    onCheckedChange = { userSettingsViewModel.setSaveRttResults(it) },
                    colors = myCheckBoxColors()
                )
            }
        }
    }
}

@Composable
fun myCheckBoxColors(): CheckboxColors {
    return CheckboxDefaults.colors(
        checkedColor = MaterialTheme.colorScheme.primary,
        uncheckedColor = Color.Gray
    )
}
