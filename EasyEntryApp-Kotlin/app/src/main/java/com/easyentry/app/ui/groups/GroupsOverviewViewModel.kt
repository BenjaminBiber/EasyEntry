package com.easyentry.app.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easyentry.app.data.repository.DeviceGroupRepository
import com.easyentry.app.domain.model.DeviceGroup
import com.easyentry.app.domain.model.GroupIcon
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupsOverviewViewModel @Inject constructor(
    private val repository: DeviceGroupRepository
) : ViewModel() {

    data class UiState(
        val groups: List<DeviceGroup> = emptyList(),
        val editingGroup: DeviceGroup? = null,
        val errorMessage: String? = null,
        val isRefreshing: Boolean = false,
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

    fun startEditing(group: DeviceGroup) {
        _uiState.update { it.copy(editingGroup = group, errorMessage = null) }
    }

    fun stopEditing() {
        _uiState.update { it.copy(editingGroup = null, errorMessage = null) }
    }

    fun updateGroup(groupId: Int, name: String, color: Long, icon: GroupIcon) {
        viewModelScope.launch {
            if (name.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Gruppenname darf nicht leer sein!") }
                return@launch
            }
            val nameChanged = _uiState.value.editingGroup?.groupName != name
            if (nameChanged && repository.groupNameExists(name)) {
                _uiState.update { it.copy(errorMessage = "Gruppenname bereits vorhanden!") }
                return@launch
            }
            repository.updateGroup(groupId, name, color, icon)
            stopEditing()
        }
    }

    fun deleteGroup(groupId: Int) {
        viewModelScope.launch {
            repository.deleteGroup(groupId)
            stopEditing()
        }
    }

    fun reload() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            val groups = repository.getGroupsWithDevices().first()
            delay(300)
            _uiState.update { it.copy(groups = groups, isRefreshing = false) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
