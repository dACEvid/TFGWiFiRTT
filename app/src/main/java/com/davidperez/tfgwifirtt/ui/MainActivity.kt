package com.davidperez.tfgwifirtt.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.davidperez.tfgwifirtt.ui.screens.AccessPointsListScreen
import com.davidperez.tfgwifirtt.ui.screens.CompatibleDevicesListScreen
import com.davidperez.tfgwifirtt.ui.screens.SettingsScreen
import com.davidperez.tfgwifirtt.ui.theme.TFGWiFiRTTTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNeededPermissions()

        setContent {
            TFGWiFiRTTTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyAppNav()
                }
            }
        }
    }

    private fun requestNeededPermissions() {
        // Permissions needed for a successful call to startScan()
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        }
        if (checkSelfPermission(Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CHANGE_WIFI_STATE), 1)
        }

        // Permission needed for a successful call to getScanResults()
        if (checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_WIFI_STATE), 1)
        }

        // Permission needed for performing a RTT ranging request (only for Android 13 or higher)
        // TODO: request this at runtime when the user wants to perform the RTT ranging request
        if (checkSelfPermission(Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.NEARBY_WIFI_DEVICES), 1)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppNav() {
    val navController = rememberNavController() // Create NavController

    val topLevelRoutes = listOf(
        TopLevelRoute("Access Points", "accessPointsList", Icons.Default.Home),
        TopLevelRoute("RTT-capable Devices", "compatibleDevicesList", Icons.Default.Info),
        TopLevelRoute("User Preferences", "settings", Icons.Default.Settings)
    )

    Scaffold(
        bottomBar = {
            BottomNavigation(modifier = Modifier.height(80.dp), backgroundColor = MaterialTheme.colorScheme.primary) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                topLevelRoutes.forEach { topLevelRoute ->
                    val isSelected = currentDestination?.route == topLevelRoute.route
                    BottomNavigationItem(
                        icon = { Icon(topLevelRoute.icon, contentDescription = topLevelRoute.name, tint = if (isSelected) Color.White else Color.Gray) },
                        selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                        unselectedContentColor = Color.Gray,
                        label = { Text(topLevelRoute.name, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.Gray, textAlign = TextAlign.Center, lineHeight = 16.sp) },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(topLevelRoute.route) {
                                // Pop up to the start destination of the graph to avoid building up a large stack of destinations on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true // Avoid multiple copies of the same destination when re-selecting the same item
                                restoreState = true // Restore state when re-selecting a previously selected item
                            }
                        },
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = "accessPointsList", Modifier.padding(innerPadding)) {
            composable("accessPointsList") { AccessPointsListScreen() }
            composable("compatibleDevicesList") { CompatibleDevicesListScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}

data class TopLevelRoute(val name: String, val route: String, val icon: ImageVector)

