package com.davidperez.tfgwifirtt.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxColors
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Slider
import androidx.compose.material.SliderColors
import androidx.compose.material.SliderDefaults
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
import com.davidperez.tfgwifirtt.ui.components.SettingsSectionTitle
import com.davidperez.tfgwifirtt.ui.viewmodels.UserSettingsViewModel


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsScreen(
    userSettingsViewModel: UserSettingsViewModel = hiltViewModel()
) {
    val userSettingsUiState by userSettingsViewModel.uiState.collectAsState()

    LazyColumn(verticalArrangement = Arrangement.Center) {
        item {
            SettingsSectionTitle("General Settings")
        }
        item {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Only Show RTT-Compatible APs")
                Spacer(Modifier.weight(1f))
                Checkbox(
                    checked = userSettingsUiState.userSettings.showOnlyRttCompatibleAps,
                    onCheckedChange = { userSettingsViewModel.setShowRTTCompatibleOnly(it) },
                    colors = myCheckBoxColors()
                )
            }
        }

        item {
            SettingsSectionTitle("RTT Ranging Settings")
        }
        item {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Perform Continuous RTT Ranging")
                Spacer(Modifier.weight(1f))
                Checkbox(
                    checked = userSettingsUiState.userSettings.performContinuousRttRanging,
                    onCheckedChange = { userSettingsViewModel.setPerformContinuousRttRanging(it) },
                    colors = myCheckBoxColors()
                )
            }
        }
        item {
            Column {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("RTT Ranging Period (s): ")
                    Text(text = userSettingsUiState.userSettings.rttPeriod.toString())
                }
                Slider(
                    value = userSettingsUiState.userSettings.rttPeriod.toFloat(),
                    onValueChange = {
                        val snappedValue = (it/5).toLong() * 5
                        userSettingsViewModel.setRttPeriod(snappedValue)
                    },
                    valueRange = 5f..120f,
                    steps = 22,
                    enabled = userSettingsUiState.userSettings.performContinuousRttRanging,
                    colors = mySliderColors()
                )
            }
        }
        item {
            Column {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Interval between RTT Requests (ms): ")
                    Text(text = userSettingsUiState.userSettings.rttInterval.toString())
                }
                Slider(
                    value = userSettingsUiState.userSettings.rttInterval.toFloat(),
                    onValueChange = {
                        val snappedValue = (it/20).toLong() * 20
                        userSettingsViewModel.setRttInterval(snappedValue)
                    },
                    valueRange = 20f..500f,
                    steps = 23,
                    enabled = userSettingsUiState.userSettings.performContinuousRttRanging,
                    colors = mySliderColors()
                )
            }
        }

        item {
            SettingsSectionTitle("RTT Results Settings")
        }
        item {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Save RTT Results For Export")
                Spacer(Modifier.weight(1f))
                Checkbox(
                    checked = userSettingsUiState.userSettings.saveRttResults,
                    onCheckedChange = { userSettingsViewModel.setSaveRttResults(it) },
                    colors = myCheckBoxColors()
                )
            }
        }
        item {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Only Save Results of Last RTT Operation")
                Spacer(Modifier.weight(1f))
                Checkbox(
                    checked = userSettingsUiState.userSettings.saveOnlyLastRttOperation,
                    onCheckedChange = { userSettingsViewModel.setSaveLastRttOperationOnly(it) },
                    colors = myCheckBoxColors(),
                    enabled = userSettingsUiState.userSettings.saveRttResults,
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

@Composable
fun mySliderColors(): SliderColors {
    return SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.primary,
        disabledThumbColor = Color.Gray,
        activeTrackColor = MaterialTheme.colorScheme.primary
    )
}
