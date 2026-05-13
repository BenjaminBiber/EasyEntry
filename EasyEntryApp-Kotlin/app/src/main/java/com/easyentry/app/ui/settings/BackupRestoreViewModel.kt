package com.easyentry.app.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easyentry.app.R
import com.easyentry.app.data.repository.BackupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class BackupRestoreUiState(
    val isLoading: Boolean = false,
    val snackbarMessage: String? = null,
    val showImportConfirmDialog: Boolean = false,
    val pendingImportUri: Uri? = null
)

@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backupRepository: BackupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupRestoreUiState())
    val uiState: StateFlow<BackupRestoreUiState> = _uiState.asStateFlow()

    fun suggestedFilename(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "easyentry_backup_$timestamp.json"
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            backupRepository.createBackupJson()
                .onSuccess { json ->
                    try {
                        context.contentResolver.openOutputStream(uri)?.use { stream ->
                            stream.write(json.toByteArray(Charsets.UTF_8))
                        }
                        _uiState.update {
                            it.copy(isLoading = false, snackbarMessage = context.getString(R.string.backup_export_success))
                        }
                    } catch (e: Exception) {
                        _uiState.update {
                            it.copy(isLoading = false, snackbarMessage = context.getString(R.string.backup_export_error))
                        }
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(isLoading = false, snackbarMessage = context.getString(R.string.backup_export_error))
                    }
                }
        }
    }

    fun onImportUriSelected(uri: Uri) {
        _uiState.update { it.copy(pendingImportUri = uri, showImportConfirmDialog = true) }
    }

    fun confirmImport() {
        val uri = _uiState.value.pendingImportUri ?: return
        _uiState.update { it.copy(showImportConfirmDialog = false, pendingImportUri = null, isLoading = true) }
        viewModelScope.launch {
            try {
                val json = context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.readBytes().toString(Charsets.UTF_8)
                } ?: throw Exception("Datei konnte nicht gelesen werden")
                backupRepository.restoreFromJson(json)
                    .onSuccess {
                        _uiState.update {
                            it.copy(isLoading = false, snackbarMessage = context.getString(R.string.backup_import_success))
                        }
                    }
                    .onFailure {
                        _uiState.update {
                            it.copy(isLoading = false, snackbarMessage = context.getString(R.string.backup_import_error))
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, snackbarMessage = context.getString(R.string.backup_import_error))
                }
            }
        }
    }

    fun dismissImportDialog() {
        _uiState.update { it.copy(showImportConfirmDialog = false, pendingImportUri = null) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
