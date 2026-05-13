package com.easyentry.app.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easyentry.app.data.repository.DeviceGroupRepository
import com.easyentry.app.domain.model.Device
import com.easyentry.app.domain.model.DeviceGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetConfigViewModel @Inject constructor(
    private val deviceGroupRepository: DeviceGroupRepository,
    private val widgetPreferencesDataStore: WidgetPreferencesDataStore,
    @ApplicationContext private val context: Context
) : ViewModel() {

    data class UiState(
        val groups: List<DeviceGroup> = emptyList(),
        val isLoading: Boolean = true
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var appWidgetId: Int = -1

    init {
        viewModelScope.launch {
            val groups = deviceGroupRepository.getGroupsWithDevices().first()
            _uiState.update { it.copy(groups = groups, isLoading = false) }
        }
    }

    fun setAppWidgetId(id: Int) {
        appWidgetId = id
    }

    fun selectDevice(deviceId: Int, onComplete: () -> Unit) {
        viewModelScope.launch {
            widgetPreferencesDataStore.setDeviceId(appWidgetId, deviceId)
            val result = runCatching {
                val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
                val device = uiState.value.groups.flatMap { it.devices }.firstOrNull { it.id == deviceId }
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[EasyEntryWidget.DEVICE_ID_KEY] = deviceId
                    if (device != null) {
                        prefs[EasyEntryWidget.DEVICE_NAME_KEY] = device.name
                        prefs[EasyEntryWidget.IS_OPENED_KEY]   = device.isOpened
                    }
                }
                EasyEntryWidget().update(context, glanceId)
            }
            if (result.isFailure) {
                // Fallback für Launcher, die GlanceId erst nach der Config-Activity registrieren
                // (z.B. Nothing Launcher): provideGlance liest deviceId dann aus dem DataStore
                EasyEntryWidget().updateAll(context)
            }
            onComplete()
        }
    }
}
