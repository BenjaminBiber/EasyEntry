package com.easyentry.app.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.easyentry.app.R
import com.easyentry.app.domain.model.Device
import com.easyentry.app.ui.theme.EasyEntryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WidgetConfigActivity : ComponentActivity() {

    private val viewModel: WidgetConfigViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setResult(RESULT_CANCELED)

        val appWidgetId = intent
            .extras
            ?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        viewModel.setAppWidgetId(appWidgetId)

        setContent {
            EasyEntryTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                WidgetConfigScreen(
                    devices = uiState.allDevices,
                    isLoading = uiState.isLoading,
                    onDeviceSelected = { device ->
                        viewModel.selectDevice(device.id) {
                            setResult(
                                RESULT_OK,
                                Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                            )
                            finish()
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WidgetConfigScreen(
    devices: List<Device>,
    isLoading: Boolean,
    onDeviceSelected: (Device) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Widget konfigurieren") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    titleContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            devices.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.no_devices))
                }
            }
            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                    items(devices, key = { it.id }) { device ->
                        ListItem(
                            headlineContent = { Text(device.name) },
                            supportingContent = { Text(device.deviceUrl) },
                            modifier = Modifier.clickable { onDeviceSelected(device) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
