package com.easyentry.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easyentry.app.data.repository.DeviceGroupRepository
import com.easyentry.app.data.repository.SettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingRepository: SettingRepository,
    private val deviceGroupRepository: DeviceGroupRepository
) : ViewModel() {

    val showSnackBar = settingRepository.showSnackBar
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun setShowSnackBar(value: Boolean) {
        viewModelScope.launch {
            settingRepository.setShowSnackBar(value)
        }
    }

    private val _showResetDialog = MutableStateFlow(false)
    val showResetDialog: StateFlow<Boolean> = _showResetDialog.asStateFlow()

    fun showResetDialog() { _showResetDialog.value = true }
    fun hideResetDialog() { _showResetDialog.value = false }

    fun resetAllData() {
        viewModelScope.launch {
            deviceGroupRepository.deleteAllGroupsAndDevices()
            _showResetDialog.value = false
        }
    }
}
