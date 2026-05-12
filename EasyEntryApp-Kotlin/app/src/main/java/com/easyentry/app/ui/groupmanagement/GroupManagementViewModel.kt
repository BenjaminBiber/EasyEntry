package com.easyentry.app.ui.groupmanagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easyentry.app.data.repository.DeviceGroupRepository
import com.easyentry.app.domain.model.DeviceGroup
import com.easyentry.app.domain.model.GroupIcon
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupManagementViewModel @Inject constructor(
    private val repository: DeviceGroupRepository
) : ViewModel() {

    data class UiState(
        val groups: List<DeviceGroup> = emptyList(),
        val showAddGroupSheet: Boolean = false,
        val showAssignDeviceSheet: Boolean = false,
        val errorMessage: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getGroupsWithDevices().collect { groups ->
                _uiState.update { it.copy(groups = groups) }
            }
        }
    }

    fun showAddGroupSheet() = _uiState.update { it.copy(showAddGroupSheet = true) }
    fun hideAddGroupSheet() = _uiState.update { it.copy(showAddGroupSheet = false) }

    fun showAssignDeviceSheet() = _uiState.update { it.copy(showAssignDeviceSheet = true) }
    fun hideAssignDeviceSheet() = _uiState.update { it.copy(showAssignDeviceSheet = false) }

    fun addGroup(name: String, color: Long, icon: GroupIcon) {
        viewModelScope.launch {
            if (name.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Gruppenname darf nicht leer sein!") }
                return@launch
            }
            if (repository.groupNameExists(name)) {
                _uiState.update { it.copy(errorMessage = "Gruppenname bereits vorhanden!") }
                return@launch
            }
            repository.addGroup(name, color, icon)
            _uiState.update { it.copy(showAddGroupSheet = false, errorMessage = null) }
        }
    }

    fun renameGroup(groupId: Int, newName: String) {
        viewModelScope.launch {
            if (newName.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Gruppenname darf nicht leer sein!") }
                return@launch
            }
            repository.renameGroup(groupId, newName)
        }
    }

    fun deleteGroup(groupId: Int) {
        viewModelScope.launch {
            repository.deleteGroup(groupId)
        }
    }

    fun deleteDevice(deviceId: Int) {
        viewModelScope.launch {
            repository.deleteDevice(deviceId)
        }
    }

    fun moveDevice(deviceId: Int, newGroupId: Int) {
        viewModelScope.launch {
            repository.moveDevice(deviceId, newGroupId)
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}
