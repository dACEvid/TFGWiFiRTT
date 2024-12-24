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
fun TitleWithDivider(
    title: String,
    isSubtitle: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSubtitle) MaterialTheme.colorScheme.inversePrimary else MaterialTheme.colorScheme.primary)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = if (isSubtitle) 12.sp else 30.sp,
            fontWeight = if (isSubtitle) FontWeight.Normal else FontWeight.Bold,
            color = if (isSubtitle) Color.Gray else MaterialTheme.colorScheme.onPrimary
        )
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
            thickness = 1.dp
        )
    }
}

