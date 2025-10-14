package com.axeven.profiteerapp.data.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test suite for ExportUiState following TDD approach.
 * Tests the consolidated state management for export operations.
 */
class ExportUiStateTest {

    // ========== Tests for default state ==========

    @Test
    fun `default state should not be exporting`() {
        // Arrange & Act
        val state = ExportUiState()

        // Assert
        assertFalse(state.isExporting)
    }

    @Test
    fun `default state should have no success URL`() {
        // Arrange & Act
        val state = ExportUiState()

        // Assert
        assertNull(state.successUrl)
    }

    @Test
    fun `default state should have no error message`() {
        // Arrange & Act
        val state = ExportUiState()

        // Assert
        assertNull(state.errorMessage)
    }

    // ========== Tests for isExporting state ==========

    @Test
    fun `state with isExporting true should be exporting`() {
        // Arrange & Act
        val state = ExportUiState(isExporting = true)

        // Assert
        assertTrue(state.isExporting)
        assertNull(state.successUrl)
        assertNull(state.errorMessage)
    }

    // ========== Tests for success state ==========

    @Test
    fun `success state should have URL and not be exporting`() {
        // Arrange
        val testUrl = "https://docs.google.com/spreadsheets/d/test123"

        // Act
        val state = ExportUiState(
            isExporting = false,
            successUrl = testUrl,
            errorMessage = null
        )

        // Assert
        assertFalse(state.isExporting)
        assertEquals(testUrl, state.successUrl)
        assertNull(state.errorMessage)
    }

    // ========== Tests for error state ==========

    @Test
    fun `error state should have message and not be exporting`() {
        // Arrange
        val testError = "Network connection failed"

        // Act
        val state = ExportUiState(
            isExporting = false,
            successUrl = null,
            errorMessage = testError
        )

        // Assert
        assertFalse(state.isExporting)
        assertNull(state.successUrl)
        assertEquals(testError, state.errorMessage)
    }

    // ========== Tests for state immutability ==========

    @Test
    fun `state transitions should be immutable`() {
        // Arrange
        val originalState = ExportUiState()

        // Act
        val newState = originalState.copy(isExporting = true)

        // Assert
        assertNotSame(originalState, newState)
        assertFalse(originalState.isExporting) // Original unchanged
        assertTrue(newState.isExporting)
    }

    // ========== Tests for withExporting() ==========

    @Test
    fun `withExporting should transition to exporting state`() {
        // Arrange
        val state = ExportUiState()

        // Act
        val exportingState = state.withExporting()

        // Assert
        assertTrue(exportingState.isExporting)
        assertNull(exportingState.successUrl)
        assertNull(exportingState.errorMessage)
    }

    @Test
    fun `withExporting should clear previous error`() {
        // Arrange
        val stateWithError = ExportUiState(
            isExporting = false,
            successUrl = null,
            errorMessage = "Previous error"
        )

        // Act
        val exportingState = stateWithError.withExporting()

        // Assert
        assertTrue(exportingState.isExporting)
        assertNull(exportingState.errorMessage)
    }

    @Test
    fun `withExporting should clear previous success URL`() {
        // Arrange
        val stateWithSuccess = ExportUiState(
            isExporting = false,
            successUrl = "https://docs.google.com/spreadsheets/d/old",
            errorMessage = null
        )

        // Act
        val exportingState = stateWithSuccess.withExporting()

        // Assert
        assertTrue(exportingState.isExporting)
        assertNull(exportingState.successUrl)
    }

    // ========== Tests for withSuccess() ==========

    @Test
    fun `withSuccess should transition to success state`() {
        // Arrange
        val exportingState = ExportUiState(isExporting = true)
        val testUrl = "https://docs.google.com/spreadsheets/d/success123"

        // Act
        val successState = exportingState.withSuccess(testUrl)

        // Assert
        assertFalse(successState.isExporting)
        assertEquals(testUrl, successState.successUrl)
        assertNull(successState.errorMessage)
    }

    @Test
    fun `withSuccess should store spreadsheet URL`() {
        // Arrange
        val state = ExportUiState(isExporting = true)
        val expectedUrl = "https://docs.google.com/spreadsheets/d/abc123"

        // Act
        val successState = state.withSuccess(expectedUrl)

        // Assert
        assertEquals(expectedUrl, successState.successUrl)
    }

    @Test
    fun `withSuccess should clear error state`() {
        // Arrange
        val stateWithError = ExportUiState(
            isExporting = false,
            successUrl = null,
            errorMessage = "Previous error"
        )
        val testUrl = "https://docs.google.com/spreadsheets/d/new123"

        // Act
        val successState = stateWithError.withSuccess(testUrl)

        // Assert
        assertNull(successState.errorMessage)
        assertEquals(testUrl, successState.successUrl)
    }

    // ========== Tests for withError() ==========

    @Test
    fun `withError should transition to error state`() {
        // Arrange
        val exportingState = ExportUiState(isExporting = true)
        val testError = "Export failed: Network error"

        // Act
        val errorState = exportingState.withError(testError)

        // Assert
        assertFalse(errorState.isExporting)
        assertNull(errorState.successUrl)
        assertEquals(testError, errorState.errorMessage)
    }

    @Test
    fun `withError should store error message`() {
        // Arrange
        val state = ExportUiState(isExporting = true)
        val expectedError = "Failed to connect to Google Sheets"

        // Act
        val errorState = state.withError(expectedError)

        // Assert
        assertEquals(expectedError, errorState.errorMessage)
    }

    @Test
    fun `withError should clear success URL`() {
        // Arrange
        val stateWithSuccess = ExportUiState(
            isExporting = false,
            successUrl = "https://docs.google.com/spreadsheets/d/old",
            errorMessage = null
        )
        val testError = "New error occurred"

        // Act
        val errorState = stateWithSuccess.withError(testError)

        // Assert
        assertNull(errorState.successUrl)
        assertEquals(testError, errorState.errorMessage)
    }

    // ========== Tests for state property combinations ==========

    @Test
    fun `state should never have both success URL and error message`() {
        // Test that the state methods enforce mutual exclusivity

        // Success state should not have error
        val successState = ExportUiState().withSuccess("https://test.com")
        assertNull(successState.errorMessage)

        // Error state should not have success URL
        val errorState = ExportUiState().withError("Test error")
        assertNull(errorState.successUrl)
    }

    @Test
    fun `transitioning from error to success should clear error`() {
        // Arrange
        val errorState = ExportUiState().withError("Previous error")

        // Act
        val successState = errorState.withSuccess("https://docs.google.com/spreadsheets/d/new")

        // Assert
        assertNull(successState.errorMessage)
        assertEquals("https://docs.google.com/spreadsheets/d/new", successState.successUrl)
    }

    @Test
    fun `transitioning from success to error should clear success URL`() {
        // Arrange
        val successState = ExportUiState().withSuccess("https://docs.google.com/spreadsheets/d/old")

        // Act
        val errorState = successState.withError("New error")

        // Assert
        assertNull(errorState.successUrl)
        assertEquals("New error", errorState.errorMessage)
    }
}
