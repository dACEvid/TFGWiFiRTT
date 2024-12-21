package com.davidperez.tfgwifirtt.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.davidperez.tfgwifirtt.ui.viewmodels.UserSettingsViewModel


@Composable
fun SettingsScreen(
    userSettingsViewModel: UserSettingsViewModel = hiltViewModel()
) {
    val userSettingsUiState by userSettingsViewModel.uiState.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Settings",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Only Show RTT-Compatible Access Points")
            Checkbox(
                checked = userSettingsUiState.userSettings.showOnlyRttCompatibleAps,
                onCheckedChange = { userSettingsViewModel.setShowRTTCompatibleOnly(it) }
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Perform Continuous RTT Ranging")
            Checkbox(
                checked = userSettingsUiState.userSettings.performContinuousRttRanging,
                onCheckedChange = { userSettingsViewModel.setPerformContinuousRttRanging(it) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("RTT Ranging Period (s): ")
            Text(text = userSettingsUiState.userSettings.rttPeriod.toString())
        }
        Slider(
            value = userSettingsUiState.userSettings.rttPeriod.toFloat(),
            onValueChange = { userSettingsViewModel.setRttPeriod(it.toLong()) },
            valueRange = 5f..30f,
            steps = 24,
            enabled = userSettingsUiState.userSettings.performContinuousRttRanging
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Interval between RTT Requests (ms): ")
            Text(text = userSettingsUiState.userSettings.rttInterval.toString())
        }
        Slider(
            value = userSettingsUiState.userSettings.rttInterval.toFloat(),
            onValueChange = {
                val snappedValue = (it/50).toLong() * 50
                userSettingsViewModel.setRttInterval(snappedValue)
            },
            valueRange = 50f..1000f,
            steps = 18,
            enabled = userSettingsUiState.userSettings.performContinuousRttRanging
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Save RTT Results For Export")
            Checkbox(
                checked = userSettingsUiState.userSettings.saveRttResults,
                onCheckedChange = { userSettingsViewModel.setSaveRttResults(it) }
            )
        }
    }
}
