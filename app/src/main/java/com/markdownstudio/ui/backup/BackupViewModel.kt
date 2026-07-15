package com.markdownstudio.ui.backup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.markdownstudio.domain.model.BackupReport
import com.markdownstudio.domain.repository.BackupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class BackupOperation { IDLE, EXPORTING, IMPORTING, ZIPPING, RESTORING }

data class BackupUiState(
    val operation: BackupOperation = BackupOperation.IDLE,
    val completed: Int = 0,
    val total: Int = 0,
    val report: BackupReport? = null,
    val error: String? = null
)

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupRepository: BackupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    fun exportAll(outputTreeUri: Uri) {
        _uiState.value = BackupUiState(operation = BackupOperation.EXPORTING)
        viewModelScope.launch {
            backupRepository.exportAll(
                outputTreeUri = outputTreeUri,
                onProgress = { c, t -> _uiState.value = _uiState.value.copy(completed = c, total = t) }
            ).fold(
                onSuccess = { report ->
                    _uiState.value = _uiState.value.copy(
                        operation = BackupOperation.IDLE,
                        completed = 0, total = 0,
                        report = report
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        operation = BackupOperation.IDLE,
                        completed = 0, total = 0,
                        error = e.message
                    )
                }
            )
        }
    }

    fun importFrom(inputTreeUri: Uri) {
        _uiState.value = BackupUiState(operation = BackupOperation.IMPORTING)
        viewModelScope.launch {
            backupRepository.importFrom(
                inputTreeUri = inputTreeUri,
                onProgress = { c, t -> _uiState.value = _uiState.value.copy(completed = c, total = t) }
            ).fold(
                onSuccess = { report ->
                    _uiState.value = _uiState.value.copy(
                        operation = BackupOperation.IDLE,
                        completed = 0, total = 0,
                        report = report
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        operation = BackupOperation.IDLE,
                        completed = 0, total = 0,
                        error = e.message
                    )
                }
            )
        }
    }

    fun createBackupZip(outputZipUri: Uri) {
        _uiState.value = BackupUiState(operation = BackupOperation.ZIPPING)
        viewModelScope.launch {
            backupRepository.createBackupZip(
                outputZipUri = outputZipUri,
                onProgress = { c, t -> _uiState.value = _uiState.value.copy(completed = c, total = t) }
            ).fold(
                onSuccess = { report ->
                    _uiState.value = _uiState.value.copy(
                        operation = BackupOperation.IDLE,
                        completed = 0, total = 0,
                        report = report
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        operation = BackupOperation.IDLE,
                        completed = 0, total = 0,
                        error = e.message
                    )
                }
            )
        }
    }

    fun restoreFromZip(inputZipUri: Uri) {
        _uiState.value = BackupUiState(operation = BackupOperation.RESTORING)
        viewModelScope.launch {
            backupRepository.restoreFromZip(
                inputZipUri = inputZipUri,
                onProgress = { c, t -> _uiState.value = _uiState.value.copy(completed = c, total = t) }
            ).fold(
                onSuccess = { report ->
                    _uiState.value = _uiState.value.copy(
                        operation = BackupOperation.IDLE,
                        completed = 0, total = 0,
                        report = report
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        operation = BackupOperation.IDLE,
                        completed = 0, total = 0,
                        error = e.message
                    )
                }
            )
        }
    }

    fun clearReport() {
        _uiState.value = _uiState.value.copy(report = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
