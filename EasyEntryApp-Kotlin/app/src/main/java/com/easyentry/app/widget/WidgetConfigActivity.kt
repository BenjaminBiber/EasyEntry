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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
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
import com.easyentry.app.domain.model.DeviceGroup
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
                    groups = uiState.groups,
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun WidgetConfigScreen(
    groups: List<DeviceGroup>,
    isLoading: Boolean,
    onDeviceSelected: (Device) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Widget konfigurieren") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
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
            groups.isEmpty() || groups.all { it.devices.isEmpty() } -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.no_devices))
                }
            }
            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                    groups.forEach { group ->
                        if (group.devices.isNotEmpty()) {
                            stickyHeader(key = "header_${group.id}") {
                                ListItem(
                                    headlineContent = {
                                        Text(
                                            text = group.groupName,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    leadingContent = {
                                        Icon(
                                            imageVector = group.icon.toImageVector(),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    colors = ListItemDefaults.colors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                            }
                            items(group.devices, key = { it.id }) { device ->
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
    }
}
