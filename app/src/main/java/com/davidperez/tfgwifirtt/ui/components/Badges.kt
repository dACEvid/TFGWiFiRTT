package com.davidperez.tfgwifirtt.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Chip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

@Composable
fun CompatibilityBadge(label: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Green.copy(alpha = 0.2f),
        modifier = Modifier.padding(4.dp)
    ) {
        Text(label, color = Color.Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

//@Composable
//fun TooltipChip(label: String, color: Color, icon: ImageVector) {
//    var showTooltip by remember { mutableStateOf(false) }
//
//    Box {
//        Chip(label, color, icon)
//        if (showTooltip) {
//            TooltipBox { Text("802.11az support can only be checked on Android 15 or later.") }
//        }
//    }
//}
