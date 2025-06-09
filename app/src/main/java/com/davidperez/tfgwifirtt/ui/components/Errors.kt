package com.davidperez.tfgwifirtt.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ErrorDialog(
    onComplete: () -> Unit,
    errorMsg: String,
) {
    if (errorMsg != "") {
        AlertDialog(
            icon = {
                Icon(Icons.Default.Warning, contentDescription = "Warning Icon")
            },
            title = {
                Text("Error")
            },
            text = {
                Text(errorMsg)
            },
            onDismissRequest = {
                onComplete()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onComplete()
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onComplete()
                    }
                ) {
                    Text("Dismiss")
                }
            }
        )
    }
}
