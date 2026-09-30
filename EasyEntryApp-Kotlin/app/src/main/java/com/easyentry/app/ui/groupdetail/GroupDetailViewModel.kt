package com.easyentry.app.ui.groupdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easyentry.app.data.remote.ProbeTarget
import com.easyentry.app.data.remote.api.EspApi
import com.easyentry.app.data.remote.dto.EspControlDto
import com.easyentry.app.data.repository.DeviceGroupRepository
import com.easyentry.app.data.repository.DeviceReachabilityRepository
import com.easyentry.app.data.repository.SettingRepository
import com.easyentry.app.domain.model.Device
import com.easyentry.app.domain.model.DeviceGroup
import com.easyentry.app.domain.model.DeviceStatus
import com.easyentry.app.domain.model.ReachabilityEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    private val deviceGroupRepository: DeviceGroupRepository,
    private val reachabilityRepository: DeviceReachabilityRepository,
    private val settingRepository: SettingRepository,
    private val espApi: EspApi,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val groupId: Int = checkNotNull(savedStateHandle["groupId"])

    data class UiState(
        val group: DeviceGroup? = null,
        val allGroups: List<DeviceGroup> = emptyList(),
        val deviceReachability: Map<Int, ReachabilityEntry> = emptyMap(),
        val loadingButtons: Map<Int, DeviceStatus?> = emptyMap(),
        val batchLoadingAction: DeviceStatus? = null,
        val snackbarMessage: String? = null,
        val showMoveSheet: Boolean = false,
        val moveDeviceId: Int? = null,
        val showDeleteDeviceDialog: Boolean = false,
        val deleteDeviceId: Int? = null,
        val isRefreshing: Boolean = false,
    )

    /** Reiner UI-Zustand, getrennt von den Repository-Flows. */
    private data class LocalState(
        val loadingButtons: Map<Int, DeviceStatus?> = emptyMap(),
        val batchLoadingAction: DeviceStatus? = null,
        val snackbarMessage: String? = null,
        val showMoveSheet: Boolean = false,
        val moveDeviceId: Int? = null,
        val showDeleteDeviceDialog: Boolean = false,
        val deleteDeviceId: Int? = null,
        val isRefreshing: Boolean = false,
    )

    private val localState = MutableStateFlow(LocalState())

    val uiState: StateFlow<UiState> = combine(
        deviceGroupRepository.getGroupsWithDevices(),
        reachabilityRepository.status,
        localState,
    ) { groups, reachability, local ->
        UiState(
            group = groups.firstOrNull { it.id == groupId },
            allGroups = groups,
            deviceReachability = reachability,
            loadingButtons = local.loadingButtons,
            batchLoadingAction = local.batchLoadingAction,
            snackbarMessage = local.snackbarMessage,
            showMoveSheet = local.showMoveSheet,
            moveDeviceId = local.moveDeviceId,
            showDeleteDeviceDialog = local.showDeleteDeviceDialog,
            deleteDeviceId = local.deleteDeviceId,
            isRefreshing = local.isRefreshing,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    fun onControlButton(device: Device, status: DeviceStatus) {
        viewModelScope.launch {
            localState.update { it.copy(loadingButtons = it.loadingButtons + (device.id to status)) }

            val url = "http://${device.deviceUrl}/"
            val success = try {
                espApi.controlDoor(url, EspControlDto(status.value))
                true
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: Exception) {
                false
            }

            if (settingRepository.showSnackBar.first()) {
                val message = when {
                    success && status == DeviceStatus.OPENED  -> "Tor wurde erfolgreich geöffnet"
                    !success && status == DeviceStatus.OPENED -> "Fehler beim öffnen des Tors"
                    success && status == DeviceStatus.CLOSED  -> "Tor wurde erfolgreich geschlossen"
                    !success && status == DeviceStatus.CLOSED -> "Fehler beim schließen des Tors"
                    success && status == DeviceStatus.NEUTRAL -> "Tor wurde erfolgreich gestoppt"
                    else                                      -> "Fehler beim stoppen des Tors"
                }
                localState.update { it.copy(snackbarMessage = message) }
            }

            delay(4_000)

            reachabilityRepository.refreshOne(ProbeTarget(device.id, device.deviceUrl))
            localState.update { it.copy(loadingButtons = it.loadingButtons - device.id) }
        }
    }

    fun onBatchControl(status: DeviceStatus) {
        val devices = uiState.value.group?.devices ?: return
        if (devices.isEmpty()) return

        viewModelScope.launch {
            localState.update { it.copy(batchLoadingAction = status) }

            val results: List<Boolean> = coroutineScope {
                devices.map { device ->
                    async {
                        try {
                            espApi.controlDoor("http://${device.deviceUrl}/", EspControlDto(status.value))
                            true
                        } catch (ce: CancellationException) {
                            throw ce
                        } catch (e: Exception) {
                            false
                        }
                    }
                }.awaitAll()
            }

            if (settingRepository.showSnackBar.first()) {
                val successCount = results.count { it }
                val total = devices.size
                val actionWord = when (status) {
                    DeviceStatus.OPENED  -> "geöffnet"
                    DeviceStatus.CLOSED  -> "geschlossen"
                    DeviceStatus.NEUTRAL -> "gestoppt"
                }
                val message = if (successCount == total) {
                    "Alle Geräte erfolgreich $actionWord"
                } else {
                    "$successCount von $total Geräten erfolgreich $actionWord"
                }
                localState.update { it.copy(snackbarMessage = message) }
            }

            delay(4_000)

            coroutineScope {
                devices.forEach { device ->
                    launch { reachabilityRepository.refreshOne(ProbeTarget(device.id, device.deviceUrl)) }
                }
            }

            localState.update { it.copy(batchLoadingAction = null) }
        }
    }

    fun reload() {
        viewModelScope.launch {
            localState.update { it.copy(isRefreshing = true) }
            try {
                reachabilityRepository.refreshNow()
            } finally {
                localState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    fun snackbarShown() {
        localState.update { it.copy(snackbarMessage = null) }
    }

    fun showMoveDeviceSheet(deviceId: Int) {
        localState.update { it.copy(showMoveSheet = true, moveDeviceId = deviceId) }
    }

    fun hideMoveDeviceSheet() {
        localState.update { it.copy(showMoveSheet = false, moveDeviceId = null) }
    }

    fun moveDevice(deviceId: Int, newGroupId: Int) {
        viewModelScope.launch {
            deviceGroupRepository.moveDevice(deviceId, newGroupId)
            hideMoveDeviceSheet()
        }
    }

    fun showDeleteDeviceDialog(deviceId: Int) {
        localState.update { it.copy(showDeleteDeviceDialog = true, deleteDeviceId = deviceId) }
    }

    fun hideDeleteDeviceDialog() {
        localState.update { it.copy(showDeleteDeviceDialog = false, deleteDeviceId = null) }
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
}
