package com.easyentry.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.easyentry.app.R

sealed class BottomNavDestination(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector
) {
    object Home : BottomNavDestination("home", R.string.nav_dashboard, Icons.Default.Home)
    object Groups : BottomNavDestination("groups", R.string.nav_groups, Icons.Default.Workspaces)
    object Settings : BottomNavDestination("settings", R.string.nav_settings, Icons.Default.Settings)
}

val bottomNavDestinations = listOf(
    BottomNavDestination.Home,
    BottomNavDestination.Settings
)

@Composable
fun BottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        bottomNavDestinations.forEach { destination ->
            val isSelected = currentRoute == destination.route ||
                (destination is BottomNavDestination.Home && currentRoute?.startsWith("group_detail/") == true)
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(destination.icon, contentDescription = stringResource(destination.labelRes))
                },
                label = { Text(stringResource(destination.labelRes)) }
            )
        }
    }
}
