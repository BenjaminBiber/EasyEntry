package com.easyentry.app.ui.schedules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easyentry.app.alarm.ScheduleAlarmManager
import com.easyentry.app.data.local.db.ScheduledActionWithDeviceInfo
import com.easyentry.app.data.repository.DeviceGroupRepository
import com.easyentry.app.data.repository.ScheduledActionRepository
import com.easyentry.app.domain.model.Device
import com.easyentry.app.worker.computeDelayMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SchedulesViewModel @Inject constructor(
    private val scheduledActionRepository: ScheduledActionRepository,
    private val deviceGroupRepository: DeviceGroupRepository,
    private val scheduleAlarmManager: ScheduleAlarmManager
) : ViewModel() {

    data class UiState(
        val actions: List<ScheduledActionWithDeviceInfo> = emptyList(),
        val availableDevices: List<Device> = emptyList(),
        val showAddSheet: Boolean = false,
        val editingAction: ScheduledActionWithDeviceInfo? = null,
        val snackbarMessage: String? = null,
        val exactAlarmPermissionMissing: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            scheduledActionRepository.getAllActions().collect { actions ->
                _uiState.update { it.copy(actions = actions) }
            }
        }
        viewModelScope.launch {
            deviceGroupRepository.getGroupsWithDevices().collect { groups ->
                _uiState.update { it.copy(availableDevices = groups.flatMap { g -> g.devices }) }
            }
        }
        _uiState.update { it.copy(exactAlarmPermissionMissing = !scheduleAlarmManager.canScheduleExact()) }
    }

    fun refreshAlarmPermission() {
        _uiState.update { it.copy(exactAlarmPermissionMissing = !scheduleAlarmManager.canScheduleExact()) }
    }

    fun showAddSheet() {
        _uiState.update { it.copy(showAddSheet = true, editingAction = null) }
    }

    fun showEditSheet(action: ScheduledActionWithDeviceInfo) {
        _uiState.update { it.copy(showAddSheet = true, editingAction = action) }
    }

    fun hideSheet() {
        _uiState.update { it.copy(showAddSheet = false, editingAction = null) }
    }

    fun saveAction(
        deviceIds: List<Int>,
        actionStatusValue: Int,
        isRecurring: Boolean,
        dayOfWeekBitmask: Int,
        hourOfDay: Int,
        minuteOfHour: Int,
        delayMinutes: Int,
        label: String
    ) {
        viewModelScope.launch {
            val editing = _uiState.value.editingAction
            val result = if (editing == null) {
                scheduledActionRepository.addAction(
                    deviceIds, actionStatusValue, isRecurring,
                    dayOfWeekBitmask, hourOfDay, minuteOfHour, delayMinutes, label
                )
            } else {
                scheduledActionRepository.updateAction(
                    editing.action.copy(
                        deviceId = deviceIds.first(),
                        deviceIds = deviceIds.joinToString(","),
                        actionStatusValue = actionStatusValue,
                        isRecurring = isRecurring,
                        dayOfWeekBitmask = dayOfWeekBitmask,
                        hourOfDay = hourOfDay,
                        minuteOfHour = minuteOfHour,
                        delayMinutes = delayMinutes,
                        label = label
                    )
                )
            }
            hideSheet()
            if (result.isSuccess) {
                val delayMillis = computeDelayMillis(
                    hourOfDay = if (isRecurring) hourOfDay else 0,
                    minuteOfHour = if (isRecurring) minuteOfHour else 0,
                    dayOfWeekBitmask = dayOfWeekBitmask,
                    isRecurring = isRecurring,
                    delayMinutes = delayMinutes
                )
                _uiState.update { it.copy(snackbarMessage = formatNextExecution(delayMillis)) }
            } else {
                _uiState.update { it.copy(snackbarMessage = "Fehler beim Speichern des Zeitplans") }
            }
        }
    }

    private fun formatNextExecution(millis: Long): String {
        val totalMinutes = millis / 60_000L
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 && minutes > 0 -> "Nächste Ausführung in ${hours}h ${minutes}min"
            hours > 0                -> "Nächste Ausführung in ${hours}h"
            else                     -> "Nächste Ausführung in ${minutes}min"
        }
    }

    fun deleteAction(id: Int) {
        viewModelScope.launch {
            scheduledActionRepository.deleteAction(id)
        }
    }

    fun toggleEnabled(id: Int, enabled: Boolean) {
        viewModelScope.launch {
            scheduledActionRepository.setEnabled(id, enabled)
        }
    }

    fun snackbarShown() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
