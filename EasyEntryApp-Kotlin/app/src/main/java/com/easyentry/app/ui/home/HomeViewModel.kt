package com.easyentry.app.ui.home

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
class HomeViewModel @Inject constructor(
    private val deviceGroupRepository: DeviceGroupRepository,
    private val reachabilityRepository: DeviceReachabilityRepository,
    private val settingRepository: SettingRepository,
    private val espApi: EspApi
) : ViewModel() {

    data class UiState(
        val groups: List<DeviceGroup> = emptyList(),
        val expandedGroups: Set<Int> = emptySet(),
        val deviceReachability: Map<Int, ReachabilityEntry> = emptyMap(),
        val loadingDeviceActions: Set<Pair<Int, DeviceStatus>> = emptySet(),
        val snackbarMessage: String? = null,
        val showMoveSheet: Boolean = false,
        val moveDeviceId: Int? = null,
    )

    /**
     * Reiner UI-Zustand, getrennt von den Repository-Flows.
     *
     * [collapsedGroups] haelt bewusst die zugeklappten Gruppen statt der aufgeklappten: so
     * bleibt die Auswahl des Nutzers ueber jede Datenbank-Emission hinweg erhalten, und neue
     * Gruppen sind automatisch aufgeklappt. Vorher wurde die Auswahl bei jeder Emission
     * ueberschrieben, wodurch ein Reorder alle zugeklappten Gruppen wieder aufriss.
     */
    private data class LocalState(
        val collapsedGroups: Set<Int> = emptySet(),
        val loadingDeviceActions: Set<Pair<Int, DeviceStatus>> = emptySet(),
        val snackbarMessage: String? = null,
        val showMoveSheet: Boolean = false,
        val moveDeviceId: Int? = null,
    )

    private val localState = MutableStateFlow(LocalState())

    val uiState: StateFlow<UiState> = combine(
        deviceGroupRepository.getGroupsWithDevices(),
        reachabilityRepository.status,
        localState,
    ) { groups, reachability, local ->
        UiState(
            groups = groups,
            expandedGroups = groups.map { it.id }.toSet() - local.collapsedGroups,
            deviceReachability = reachability,
            loadingDeviceActions = local.loadingDeviceActions,
            snackbarMessage = local.snackbarMessage,
            showMoveSheet = local.showMoveSheet,
            moveDeviceId = local.moveDeviceId,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    fun onControlButton(device: Device, status: DeviceStatus) {
        viewModelScope.launch {
            val key = device.id to status
            localState.update { it.copy(loadingDeviceActions = it.loadingDeviceActions + key) }

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
            localState.update { it.copy(loadingDeviceActions = it.loadingDeviceActions - key) }
        }
    }

    fun toggleGroup(groupId: Int) {
        localState.update { state ->
            val collapsed = state.collapsedGroups.toMutableSet()
            if (groupId in collapsed) collapsed.remove(groupId) else collapsed.add(groupId)
            state.copy(collapsedGroups = collapsed)
        }
    }

    fun reload() {
        viewModelScope.launch { reachabilityRepository.refreshNow() }
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

    fun reorderDevices(orderedDeviceIds: List<Int>) {
        viewModelScope.launch {
            deviceGroupRepository.reorderDevices(orderedDeviceIds)
        }
    }
}
