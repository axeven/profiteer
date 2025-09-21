package com.axeven.profiteerapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axeven.profiteerapp.utils.CredentialDiagnostics
import com.axeven.profiteerapp.utils.logging.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiagnosticsUiState(
    val isRunning: Boolean = false,
    val report: CredentialDiagnostics.DiagnosticReport? = null,
    val error: String? = null
)

@HiltViewModel
class DiagnosticsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logger: Logger
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiagnosticsUiState())
    val uiState: StateFlow<DiagnosticsUiState> = _uiState.asStateFlow()

    fun runDiagnostics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRunning = true, error = null)

            try {
                val report = CredentialDiagnostics.runFullDiagnostics(context)
                CredentialDiagnostics.logDiagnosticReport(report, logger)

                _uiState.value = _uiState.value.copy(
                    isRunning = false,
                    report = report,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRunning = false,
                    error = "Failed to run diagnostics: ${e.message}"
                )
            }
        }
    }

    fun clearReport() {
        _uiState.value = _uiState.value.copy(report = null, error = null)
    }
}