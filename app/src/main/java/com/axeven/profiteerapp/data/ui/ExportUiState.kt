package com.axeven.profiteerapp.data.ui

/**
 * Consolidated UI state for transaction export operations.
 *
 * This state class follows the consolidated state management pattern,
 * providing immutable state transitions for export operations.
 *
 * State Transitions:
 * - Default (idle) -> Exporting (user initiates export)
 * - Exporting -> Success (export completes successfully)
 * - Exporting -> Error (export fails)
 * - Success/Error -> Exporting (user retries)
 *
 * @property isExporting True when an export operation is in progress
 * @property successUrl URL of the successfully created spreadsheet (null if not successful)
 * @property errorMessage Error message if export failed (null if no error)
 */
data class ExportUiState(
    val isExporting: Boolean = false,
    val successUrl: String? = null,
    val errorMessage: String? = null
) {
    /**
     * Transitions to exporting state, clearing any previous success or error states.
     *
     * @return New state with isExporting=true and cleared success/error states
     */
    fun withExporting(): ExportUiState {
        return copy(
            isExporting = true,
            successUrl = null,
            errorMessage = null
        )
    }

    /**
     * Transitions to success state with the spreadsheet URL.
     *
     * @param url URL of the successfully created Google Spreadsheet
     * @return New state with isExporting=false, the URL, and cleared error state
     */
    fun withSuccess(url: String): ExportUiState {
        return copy(
            isExporting = false,
            successUrl = url,
            errorMessage = null
        )
    }

    /**
     * Transitions to error state with an error message.
     *
     * @param message Description of the error that occurred
     * @return New state with isExporting=false, the error message, and cleared success URL
     */
    fun withError(message: String): ExportUiState {
        return copy(
            isExporting = false,
            successUrl = null,
            errorMessage = message
        )
    }
}
