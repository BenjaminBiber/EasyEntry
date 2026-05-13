package com.easyentry.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.easyentry.app.ui.adddevice.AddDeviceScreen
import com.easyentry.app.ui.groupdetail.GroupDetailScreen
import com.easyentry.app.ui.groupmanagement.AddGroupSheet
import com.easyentry.app.ui.groupmanagement.GroupManagementViewModel
import com.easyentry.app.ui.groups.GroupsOverviewScreen
import com.easyentry.app.ui.schedules.SchedulesScreen
import com.easyentry.app.ui.settings.BackupRestoreScreen
import com.easyentry.app.ui.settings.SettingsScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    var showAddDevice by remember { mutableStateOf(false) }

    val groupManagementVm: GroupManagementViewModel = hiltViewModel()
    val gmState by groupManagementVm.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = BottomNavDestination.Home.route
    ) {
        composable(BottomNavDestination.Home.route) {
            GroupsOverviewScreen(
                onGroupClick = { groupId ->
                    navController.navigate("group_detail/$groupId")
                },
                onNavigateToAddDevice = { showAddDevice = true },
                onNavigateToAddGroup = { groupManagementVm.showAddGroupSheet() }
            )
        }
        composable(
            route = "group_detail/{groupId}",
            arguments = listOf(navArgument("groupId") { type = NavType.IntType })
        ) {
            GroupDetailScreen(onBack = { navController.popBackStack() })
        }
        composable(BottomNavDestination.Schedules.route) {
            SchedulesScreen()
        }
        composable(BottomNavDestination.Settings.route) {
            SettingsScreen(onNavigateToBackup = { navController.navigate("backup_restore") })
        }
        composable("backup_restore") {
            BackupRestoreScreen(onBack = { navController.popBackStack() })
        }
    }

    if (showAddDevice) {
        AddDeviceScreen(onDismiss = { showAddDevice = false })
    }

    if (gmState.showAddGroupSheet) {
        AddGroupSheet(
            errorMessage = gmState.errorMessage,
            onSave = { name, color, icon -> groupManagementVm.addGroup(name, color, icon) },
            onDismiss = { groupManagementVm.hideAddGroupSheet() }
        )
    }
}
