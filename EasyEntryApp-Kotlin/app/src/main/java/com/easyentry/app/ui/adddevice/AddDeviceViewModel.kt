package com.easyentry.app.ui.adddevice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easyentry.app.data.remote.api.EspApi
import com.easyentry.app.data.repository.DeviceGroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddDeviceViewModel @Inject constructor(
    private val deviceGroupRepository: DeviceGroupRepository,
    private val espApi: EspApi
) : ViewModel() {

    companion object {
        private val IPV4_REGEX = Regex(
            """^(?:(?:25[0-5]|2[0-4]\d|1?\d{1,2})(?:\.(?!${'$'})|${'$'})){4}${'$'}"""
        )
    }

    data class UiState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val savedSuccessfully: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun saveDevice(url: String) {
        viewModelScope.launch {
            if (url.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Alle Felder müssen ausgefüllt werden!") }
                return@launch
            }

            if (!IPV4_REGEX.matches(url.trim())) {
                _uiState.update { it.copy(errorMessage = "Ungültige IP-Adresse!") }
                return@launch
            }

            if (deviceGroupRepository.deviceUrlExists(url.trim())) {
                _uiState.update { it.copy(errorMessage = "Gerät ist bereits vorhanden!") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val name = try {
                val status = espApi.getStatus("http://${url.trim()}/")
                status.name.ifBlank { url.trim() }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "URL nicht erreichbar!") }
                return@launch
            }

            deviceGroupRepository.addDevice(url.trim(), name)
            _uiState.update { it.copy(isLoading = false, savedSuccessfully = true) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
