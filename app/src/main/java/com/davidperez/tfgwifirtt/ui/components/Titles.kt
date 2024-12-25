package com.davidperez.tfgwifirtt.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScreenTitle(
    title: String,
    isSubtitle: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSubtitle) MaterialTheme.colorScheme.inversePrimary else MaterialTheme.colorScheme.primary)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = if (isSubtitle) 12.sp else 30.sp,
            fontWeight = if (isSubtitle) FontWeight.Normal else FontWeight.Bold,
            color = if (isSubtitle) Color.Black else MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun SettingsSectionTitle(
    title: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
    }
}

