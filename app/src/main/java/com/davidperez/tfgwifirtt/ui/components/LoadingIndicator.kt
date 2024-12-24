package com.davidperez.tfgwifirtt.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun LoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize().zIndex(1f)) {
        CircularProgressIndicator(
            modifier = Modifier.width(64.dp).align(Alignment.Center),
            color = MaterialTheme.colorScheme.secondary
        )
    }
}
