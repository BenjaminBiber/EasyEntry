package com.easyentry.app.ui.groupdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easyentry.app.data.remote.api.EspApi
import com.easyentry.app.data.remote.dto.EspControlDto
import com.easyentry.app.data.repository.DeviceGroupRepository
import com.easyentry.app.data.repository.SettingRepository
import com.easyentry.app.domain.model.Device
import com.easyentry.app.domain.model.DeviceGroup
import com.easyentry.app.domain.model.DeviceStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    private val deviceGroupRepository: DeviceGroupRepository,
    private val settingRepository: SettingRepository,
    private val espApi: EspApi,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val groupId: Int = checkNotNull(savedStateHandle["groupId"])

    data class UiState(
        val group: DeviceGroup? = null,
        val allGroups: List<DeviceGroup> = emptyList(),
        val deviceOnlineStatus: Map<Int, Boolean> = emptyMap(),
        val loadingButtons: Map<Int, DeviceStatus?> = emptyMap(),
        val showSnackBar: Boolean = true,
        val snackbarMessage: String? = null,
        val showMoveSheet: Boolean = false,
        val moveDeviceId: Int? = null,
        val showDeleteDeviceDialog: Boolean = false,
        val deleteDeviceId: Int? = null,
        val isRefreshing: Boolean = false,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        observeGroup()
        observeSettings()
    }

    private fun observeGroup() {
        viewModelScope.launch {
            deviceGroupRepository.getGroupsWithDevices().collect { groups ->
                val group = groups.firstOrNull { it.id == groupId }
                _uiState.update { it.copy(group = group, allGroups = groups) }
                group?.let { probeAllDevices(it.devices) }
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingRepository.showSnackBar.collect { show ->
                _uiState.update { it.copy(showSnackBar = show) }
            }
        }
    }

    private fun probeAllDevices(devices: List<Device>) {
        viewModelScope.launch {
            val results = devices.map { device ->
                async {
                    val isOnline = testConnection(device.deviceUrl)
                    device.id to isOnline
                }
            }.awaitAll()
            _uiState.update { state ->
                state.copy(deviceOnlineStatus = results.toMap())
            }
        }
    }

    fun onControlButton(device: Device, status: DeviceStatus) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(loadingButtons = state.loadingButtons + (device.id to status))
            }

            val url = "http://${device.deviceUrl}/"
            val success = try {
                espApi.controlDoor(url, EspControlDto(status.value))
                true
            } catch (e: Exception) {
                false
            }

            if (_uiState.value.showSnackBar) {
                val message = when {
                    success && status == DeviceStatus.OPENED  -> "Tor wurde erfolgreich geöffnet"
                    !success && status == DeviceStatus.OPENED -> "Fehler beim öffnen des Tors"
                    success && status == DeviceStatus.CLOSED  -> "Tor wurde erfolgreich geschlossen"
                    !success && status == DeviceStatus.CLOSED -> "Fehler beim schließen des Tors"
                    success && status == DeviceStatus.NEUTRAL -> "Tor wurde erfolgreich gestoppt"
                    else                                      -> "Fehler beim stoppen des Tors"
                }
                _uiState.update { it.copy(snackbarMessage = message) }
            }

            delay(4_000)

            val isOnline = testConnection(device.deviceUrl)
            _uiState.update { state ->
                state.copy(
                    loadingButtons = state.loadingButtons - device.id,
                    deviceOnlineStatus = state.deviceOnlineStatus + (device.id to isOnline)
                )
            }
        }
    }

    fun reload() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            val groups = deviceGroupRepository.getGroupsWithDevices().first()
            val group = groups.firstOrNull { it.id == groupId }
            if (group != null) {
                val results = group.devices.map { device ->
                    async {
                        val isOnline = testConnection(device.deviceUrl)
                        device.id to isOnline
                    }
                }.awaitAll()
                _uiState.update { it.copy(deviceOnlineStatus = results.toMap(), isRefreshing = false) }
            } else {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    fun snackbarShown() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun showMoveDeviceSheet(deviceId: Int) {
        _uiState.update { it.copy(showMoveSheet = true, moveDeviceId = deviceId) }
    }

    fun hideMoveDeviceSheet() {
        _uiState.update { it.copy(showMoveSheet = false, moveDeviceId = null) }
    }

    fun moveDevice(deviceId: Int, newGroupId: Int) {
        viewModelScope.launch {
            deviceGroupRepository.moveDevice(deviceId, newGroupId)
            hideMoveDeviceSheet()
        }
    }

    fun showDeleteDeviceDialog(deviceId: Int) {
        _uiState.update { it.copy(showDeleteDeviceDialog = true, deleteDeviceId = deviceId) }
    }

    fun hideDeleteDeviceDialog() {
        _uiState.update { it.copy(showDeleteDeviceDialog = false, deleteDeviceId = null) }
    }

    fun deleteDevice(deviceId: Int) {
        viewModelScope.launch {
            deviceGroupRepository.deleteDevice(deviceId)
            hideDeleteDeviceDialog()
        }
    }

    fun reorderDevices(orderedDeviceIds: List<Int>) {
        viewModelScope.launch {
            deviceGroupRepository.reorderDevices(orderedDeviceIds)
        }
    }

    private suspend fun testConnection(deviceUrl: String): Boolean = try {
        espApi.getStatus("http://$deviceUrl/")
        true
    } catch (e: Exception) {
        false
    }
}
