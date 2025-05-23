package com.davidperez.tfgwifirtt.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.davidperez.tfgwifirtt.ui.screens.AccessPointsListScreen
import com.davidperez.tfgwifirtt.ui.screens.CompatibleDevicesListScreen
import com.davidperez.tfgwifirtt.ui.screens.RTTRangingScreen
import com.davidperez.tfgwifirtt.ui.screens.SettingsScreen
import com.davidperez.tfgwifirtt.ui.theme.TFGWiFiRTTTheme
import com.davidperez.tfgwifirtt.ui.viewmodels.AccessPointsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
}

@SuppressLint("UnrememberedGetBackStackEntry")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppNav(navController: NavHostController = rememberNavController()) {

    val topLevelRoutes = listOf(
        WifiRTTAppScreen("Visible Access Points", "accessPointsList", Icons.Default.Home),
        WifiRTTAppScreen("RTT-capable Devices", "compatibleDevicesList", Icons.Default.Info),
        WifiRTTAppScreen("User Preferences", "settings", Icons.Default.Settings)
    )
    val childRoutes = listOf(
        WifiRTTAppScreen("RTT Results", "rttResults")
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = (topLevelRoutes + childRoutes).find { it.route == navBackStackEntry?.destination?.route } ?: topLevelRoutes[0]

    Scaffold(
        topBar = {
            WifiRTTAppBar(
                currentScreen = currentScreen,
                canNavigateBack = !topLevelRoutes.contains(currentScreen),
                navigateUp = { navController.navigateUp() }
            )
        },
        bottomBar = {
            BottomNavigation(modifier = Modifier.height(80.dp), backgroundColor = MaterialTheme.colorScheme.primary) {

                val currentDestination = navBackStackEntry?.destination
                topLevelRoutes.forEach { topLevelRoute ->
                    val isSelected = currentDestination?.route == topLevelRoute.route
                    BottomNavigationItem(
                        icon = { Icon(topLevelRoute.icon, contentDescription = topLevelRoute.name, tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.Gray) },
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
            composable("accessPointsList") { AccessPointsListScreen(onGoToRTTRanging = { navController.navigate("rttResults") } ) }
            composable("compatibleDevicesList") { CompatibleDevicesListScreen() }
            composable("settings") { SettingsScreen() }
            composable("rttResults") {
                // Share the same AccessPointsViewModel instance
                val backStackEntry = remember {
                    navController.getBackStackEntry("accessPointsList")
                }
                RTTRangingScreen(hiltViewModel(backStackEntry))
            }
        }
    }
}

/**
 * Composable that displays the topBar and displays back button if back navigation is possible.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiRTTAppBar(
    currentScreen: WifiRTTAppScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        title = { Text(currentScreen.name, fontSize = 30.sp, fontWeight = FontWeight.Bold) },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    )
}

data class WifiRTTAppScreen(val name: String, val route: String, val icon: ImageVector = Icons.Default.Home)

