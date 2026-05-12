package com.easyentry.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.easyentry.app.ui.navigation.AppNavGraph
import com.easyentry.app.ui.navigation.BottomNavBar
import com.easyentry.app.ui.theme.EasyEntryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EasyEntryTheme {
                EasyEntryApp()
            }
        }
    }
}

@Composable
private fun EasyEntryApp() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            AppNavGraph(navController = navController)
        }
    }
}
