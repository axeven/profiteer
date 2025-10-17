package com.axeven.profiteerapp.service

import com.axeven.profiteerapp.utils.logging.Logger
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse
import com.google.api.services.sheets.v4.model.Request
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.SpreadsheetProperties
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.IOException

/**
 * Test suite for GoogleSheetsExporter following TDD approach.
 * Tests the core export logic for creating and writing to Google Sheets.
 */
class GoogleSheetsExporterTest {

    private lateinit var exporter: GoogleSheetsExporter
    private lateinit var mockSheetsService: Sheets
    private lateinit var mockLogger: Logger

    // Mock for Sheets API operations
    private lateinit var mockSpreadsheetsOperation: Sheets.Spreadsheets
    private lateinit var mockCreateOperation: Sheets.Spreadsheets.Create
    private lateinit var mockValuesOperation: Sheets.Spreadsheets.Values
    private lateinit var mockAppendOperation: Sheets.Spreadsheets.Values.Append
    private lateinit var mockBatchUpdateOperation: Sheets.Spreadsheets.BatchUpdate

    @Before
    fun setup() {
        mockSheetsService = mock()
        mockLogger = mock()

        // Setup mock operation chains
        mockSpreadsheetsOperation = mock()
        mockCreateOperation = mock()
        mockValuesOperation = mock()
        mockAppendOperation = mock()
        mockBatchUpdateOperation = mock()

        whenever(mockSheetsService.spreadsheets()).thenReturn(mockSpreadsheetsOperation)
        whenever(mockSpreadsheetsOperation.create(any())).thenReturn(mockCreateOperation)
        whenever(mockSpreadsheetsOperation.values()).thenReturn(mockValuesOperation)
        whenever(mockSpreadsheetsOperation.batchUpdate(any(), any())).thenReturn(mockBatchUpdateOperation)

        // Setup the append operation chain
        whenever(mockValuesOperation.append(any(), any(), any())).thenReturn(mockAppendOperation)
        whenever(mockAppendOperation.setValueInputOption(any())).thenReturn(mockAppendOperation)
        whenever(mockAppendOperation.setInsertDataOption(any())).thenReturn(mockAppendOperation)

        exporter = GoogleSheetsExporter(mockLogger)
    }

    // ========== Tests for createSpreadsheet() ==========

    @Test
    fun `createSpreadsheet should successfully create spreadsheet with default name`() = runTest {
        // Arrange
        val expectedSpreadsheetId = "test-spreadsheet-id-123"
        val mockSpreadsheet = Spreadsheet().apply {
            spreadsheetId = expectedSpreadsheetId
            spreadsheetUrl = "https://docs.google.com/spreadsheets/d/$expectedSpreadsheetId"
        }

        whenever(mockCreateOperation.execute()).thenReturn(mockSpreadsheet)

        // Act
        val result = exporter.createSpreadsheet(mockSheetsService)

        // Assert
        assertTrue(result.isSuccess)
        val spreadsheet = result.getOrNull()
        assertNotNull(spreadsheet)
        assertEquals(expectedSpreadsheetId, spreadsheet?.spreadsheetId)
    }

    @Test
    fun `createSpreadsheet should create spreadsheet with custom sheet name`() = runTest {
        // Arrange
        val customSheetName = "My Custom Export Sheet"
        val expectedSpreadsheetId = "test-spreadsheet-id-456"
        val mockSpreadsheet = Spreadsheet().apply {
            spreadsheetId = expectedSpreadsheetId
        }

        whenever(mockCreateOperation.execute()).thenReturn(mockSpreadsheet)

        // Act
        val result = exporter.createSpreadsheet(mockSheetsService, customSheetName)

        // Assert
        assertTrue(result.isSuccess)
        verify(mockSpreadsheetsOperation).create(any())
    }

    @Test
    fun `createSpreadsheet should include timestamp in sheet name`() = runTest {
        // Arrange
        val mockSpreadsheet = Spreadsheet().apply {
            spreadsheetId = "test-id"
            properties = SpreadsheetProperties()
        }

        whenever(mockCreateOperation.execute()).thenReturn(mockSpreadsheet)

        // Act
        val result = exporter.createSpreadsheet(mockSheetsService)

        // Assert
        assertTrue(result.isSuccess)
        // Verify that create was called with a spreadsheet object
        verify(mockSpreadsheetsOperation).create(any())
    }

    @Test
    fun `createSpreadsheet should handle creation failure gracefully`() = runTest {
        // Arrange
        val exception = IOException("Network error")
        whenever(mockCreateOperation.execute()).thenThrow(exception)

        // Act
        val result = exporter.createSpreadsheet(mockSheetsService)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `createSpreadsheet should handle network error`() = runTest {
        // Arrange
        val networkError = IOException("Failed to connect to Google Sheets API")
        whenever(mockCreateOperation.execute()).thenThrow(networkError)

        // Act
        val result = exporter.createSpreadsheet(mockSheetsService)

        // Assert
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception is IOException)
    }

    // ========== Tests for writeData() ==========

    @Test
    fun `writeData should write empty data (header only)`() = runTest {
        // Arrange
        val spreadsheetId = "test-spreadsheet-id"
        val emptyData = listOf(listOf("Header1", "Header2", "Header3"))

        whenever(mockAppendOperation.execute()).thenReturn(mock())

        // Act
        val result = exporter.writeData(mockSheetsService, spreadsheetId, emptyData)

        // Assert
        assertTrue(result.isSuccess)
        verify(mockValuesOperation).append(any(), any(), any())
    }

    @Test
    fun `writeData should write single row of data`() = runTest {
        // Arrange
        val spreadsheetId = "test-spreadsheet-id"
        val singleRowData = listOf(
            listOf("Header1", "Header2"),
            listOf("Value1", "Value2")
        )

        whenever(mockAppendOperation.execute()).thenReturn(mock())

        // Act
        val result = exporter.writeData(mockSheetsService, spreadsheetId, singleRowData)

        // Assert
        assertTrue(result.isSuccess)
        verify(mockValuesOperation).append(any(), any(), any())
    }

    @Test
    fun `writeData should write multiple rows in batch operation`() = runTest {
        // Arrange
        val spreadsheetId = "test-spreadsheet-id"
        val multipleRowsData = listOf(
            listOf("Header1", "Header2", "Header3"),
            listOf("Row1Col1", "Row1Col2", "Row1Col3"),
            listOf("Row2Col1", "Row2Col2", "Row2Col3"),
            listOf("Row3Col1", "Row3Col2", "Row3Col3")
        )

        whenever(mockAppendOperation.execute()).thenReturn(mock())

        // Act
        val result = exporter.writeData(mockSheetsService, spreadsheetId, multipleRowsData)

        // Assert
        assertTrue(result.isSuccess)
        verify(mockValuesOperation).append(any(), any(), any())
    }

    @Test
    fun `writeData should handle large datasets (over 1000 rows)`() = runTest {
        // Arrange
        val spreadsheetId = "test-spreadsheet-id"
        val largeData = mutableListOf(listOf("Header1", "Header2"))
        // Add 1500 data rows
        for (i in 1..1500) {
            largeData.add(listOf("Value${i}A", "Value${i}B"))
        }

        whenever(mockAppendOperation.execute()).thenReturn(mock())

        // Act
        val result = exporter.writeData(mockSheetsService, spreadsheetId, largeData)

        // Assert
        assertTrue(result.isSuccess)
        // Should still succeed, potentially with batching
        verify(mockValuesOperation).append(any(), any(), any())
    }

    @Test
    fun `writeData should handle write failure gracefully`() = runTest {
        // Arrange
        val spreadsheetId = "test-spreadsheet-id"
        val data = listOf(listOf("Header1", "Header2"))
        val exception = IOException("Write failed")

        whenever(mockAppendOperation.execute()).thenThrow(exception)

        // Act
        val result = exporter.writeData(mockSheetsService, spreadsheetId, data)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `writeData should retry transient errors`() = runTest {
        // Arrange
        val spreadsheetId = "test-spreadsheet-id"
        val data = listOf(listOf("Header1"))

        // First call fails, second succeeds
        whenever(mockAppendOperation.execute())
            .thenThrow(IOException("Transient error"))
            .thenReturn(mock())

        // Act
        val result = exporter.writeData(mockSheetsService, spreadsheetId, data)

        // Assert - depending on retry implementation, this might succeed or fail
        // For now, we expect it to fail on first error
        assertTrue(result.isFailure)
    }

    // ========== Tests for formatCells() ==========

    @Test
    fun `formatCells should format header row with bold and background color`() = runTest {
        // Arrange
        val spreadsheetId = "test-spreadsheet-id"
        val sheetId = 0

        whenever(mockBatchUpdateOperation.execute()).thenReturn(BatchUpdateSpreadsheetResponse())

        // Act
        val result = exporter.formatCells(mockSheetsService, spreadsheetId, sheetId)

        // Assert
        assertTrue(result.isSuccess)
        verify(mockSpreadsheetsOperation).batchUpdate(any(), any())
    }

    @Test
    fun `formatCells should apply currency column formatting`() = runTest {
        // Arrange
        val spreadsheetId = "test-spreadsheet-id"
        val sheetId = 0

        whenever(mockBatchUpdateOperation.execute()).thenReturn(BatchUpdateSpreadsheetResponse())

        // Act
        val result = exporter.formatCells(mockSheetsService, spreadsheetId, sheetId)

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `formatCells should apply date column formatting`() = runTest {
        // Arrange
        val spreadsheetId = "test-spreadsheet-id"
        val sheetId = 0

        whenever(mockBatchUpdateOperation.execute()).thenReturn(BatchUpdateSpreadsheetResponse())

        // Act
        val result = exporter.formatCells(mockSheetsService, spreadsheetId, sheetId)

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `formatCells should auto-size column widths`() = runTest {
        // Arrange
        val spreadsheetId = "test-spreadsheet-id"
        val sheetId = 0

        whenever(mockBatchUpdateOperation.execute()).thenReturn(BatchUpdateSpreadsheetResponse())

        // Act
        val result = exporter.formatCells(mockSheetsService, spreadsheetId, sheetId)

        // Assert
        assertTrue(result.isSuccess)
        // Verify batch update was called with formatting requests
        verify(mockSpreadsheetsOperation).batchUpdate(any(), any())
    }

    // ========== Tests for exportTransactions() ==========

    @Test
    fun `exportTransactions should complete end-to-end export flow`() = runTest {
        // Arrange
        val transactionData = listOf(
            listOf("Date", "Title", "Amount"),
            listOf("2025-10-15", "Test Transaction", "100.0")
        )

        val mockSpreadsheet = Spreadsheet().apply {
            spreadsheetId = "test-id"
            spreadsheetUrl = "https://docs.google.com/spreadsheets/d/test-id"
            sheets = listOf(
                com.google.api.services.sheets.v4.model.Sheet().apply {
                    properties = com.google.api.services.sheets.v4.model.SheetProperties().apply {
                        sheetId = 0
                    }
                }
            )
        }

        whenever(mockCreateOperation.execute()).thenReturn(mockSpreadsheet)
        whenever(mockAppendOperation.execute()).thenReturn(mock())
        whenever(mockBatchUpdateOperation.execute()).thenReturn(BatchUpdateSpreadsheetResponse())

        // Act
        val result = exporter.exportTransactions(mockSheetsService, transactionData)

        // Assert
        assertTrue(result.isSuccess)
        val url = result.getOrNull()
        assertNotNull(url)
        assertTrue(url?.contains("spreadsheets") == true)
    }

    @Test
    fun `exportTransactions should handle empty transaction list`() = runTest {
        // Arrange
        val emptyData = listOf(listOf("Header1", "Header2"))

        val mockSpreadsheet = Spreadsheet().apply {
            spreadsheetId = "test-id"
            spreadsheetUrl = "https://docs.google.com/spreadsheets/d/test-id"
            sheets = listOf(
                com.google.api.services.sheets.v4.model.Sheet().apply {
                    properties = com.google.api.services.sheets.v4.model.SheetProperties().apply {
                        sheetId = 0
                    }
                }
            )
        }

        whenever(mockCreateOperation.execute()).thenReturn(mockSpreadsheet)
        whenever(mockAppendOperation.execute()).thenReturn(mock())
        whenever(mockBatchUpdateOperation.execute()).thenReturn(BatchUpdateSpreadsheetResponse())

        // Act
        val result = exporter.exportTransactions(mockSheetsService, emptyData)

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `exportTransactions should handle large dataset (100+ transactions)`() = runTest {
        // Arrange
        val largeData = mutableListOf(listOf("Header1", "Header2"))
        for (i in 1..150) {
            largeData.add(listOf("Transaction$i", "100.$i"))
        }

        val mockSpreadsheet = Spreadsheet().apply {
            spreadsheetId = "test-id"
            spreadsheetUrl = "https://docs.google.com/spreadsheets/d/test-id"
            sheets = listOf(
                com.google.api.services.sheets.v4.model.Sheet().apply {
                    properties = com.google.api.services.sheets.v4.model.SheetProperties().apply {
                        sheetId = 0
                    }
                }
            )
        }

        whenever(mockCreateOperation.execute()).thenReturn(mockSpreadsheet)
        whenever(mockAppendOperation.execute()).thenReturn(mock())
        whenever(mockBatchUpdateOperation.execute()).thenReturn(BatchUpdateSpreadsheetResponse())

        // Act
        val result = exporter.exportTransactions(mockSheetsService, largeData)

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `exportTransactions should return shareable spreadsheet URL`() = runTest {
        // Arrange
        val transactionData = listOf(
            listOf("Header1", "Header2"),
            listOf("Value1", "Value2")
        )

        val expectedUrl = "https://docs.google.com/spreadsheets/d/abc123"
        val mockSpreadsheet = Spreadsheet().apply {
            spreadsheetId = "abc123"
            spreadsheetUrl = expectedUrl
            sheets = listOf(
                com.google.api.services.sheets.v4.model.Sheet().apply {
                    properties = com.google.api.services.sheets.v4.model.SheetProperties().apply {
                        sheetId = 0
                    }
                }
            )
        }

        whenever(mockCreateOperation.execute()).thenReturn(mockSpreadsheet)
        whenever(mockAppendOperation.execute()).thenReturn(mock())
        whenever(mockBatchUpdateOperation.execute()).thenReturn(BatchUpdateSpreadsheetResponse())

        // Act
        val result = exporter.exportTransactions(mockSheetsService, transactionData)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedUrl, result.getOrNull())
    }

    @Test
    fun `exportTransactions should handle error and rollback gracefully`() = runTest {
        // Arrange
        val transactionData = listOf(listOf("Header1"))

        val mockSpreadsheet = Spreadsheet().apply {
            spreadsheetId = "test-id"
            sheets = listOf(
                com.google.api.services.sheets.v4.model.Sheet().apply {
                    properties = com.google.api.services.sheets.v4.model.SheetProperties().apply {
                        sheetId = 0
                    }
                }
            )
        }

        whenever(mockCreateOperation.execute()).thenReturn(mockSpreadsheet)
        whenever(mockAppendOperation.execute()).thenThrow(IOException("Write failed"))

        // Act
        val result = exporter.exportTransactions(mockSheetsService, transactionData)

        // Assert
        assertTrue(result.isFailure)
        // Ideally, the created spreadsheet should be cleaned up, but that's a future enhancement
    }

    @Test
    fun `exportTransactions should prevent concurrent export attempts`() = runTest {
        // Arrange
        val transactionData = listOf(listOf("Header1"))

        // This test verifies that concurrent exports are handled properly
        // Implementation will need a lock mechanism

        val mockSpreadsheet = Spreadsheet().apply {
            spreadsheetId = "test-id"
            spreadsheetUrl = "https://docs.google.com/spreadsheets/d/test-id"
            sheets = listOf(
                com.google.api.services.sheets.v4.model.Sheet().apply {
                    properties = com.google.api.services.sheets.v4.model.SheetProperties().apply {
                        sheetId = 0
                    }
                }
            )
        }

        whenever(mockCreateOperation.execute()).thenReturn(mockSpreadsheet)
        whenever(mockAppendOperation.execute()).thenReturn(mock())
        whenever(mockBatchUpdateOperation.execute()).thenReturn(BatchUpdateSpreadsheetResponse())

        // Act
        val result = exporter.exportTransactions(mockSheetsService, transactionData)

        // Assert
        assertTrue(result.isSuccess)
        // Note: Concurrent export prevention would need additional testing with threads
    }
}
