package com.axeven.profiteerapp.data.repository

import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.service.GoogleSheetsExporter
import com.axeven.profiteerapp.service.GoogleSheetsService
import com.axeven.profiteerapp.utils.ExportFormatter
import com.axeven.profiteerapp.utils.logging.Logger
import com.google.api.services.sheets.v4.Sheets
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
 * Test suite for TransactionExportRepository following TDD approach.
 * Tests the repository layer for transaction export operations.
 */
class TransactionExportRepositoryTest {

    private lateinit var repository: TransactionExportRepository
    private lateinit var mockGoogleSheetsService: GoogleSheetsService
    private lateinit var mockGoogleSheetsExporter: GoogleSheetsExporter
    private lateinit var mockLogger: Logger
    private lateinit var mockWalletRepository: WalletRepository

    private lateinit var mockSheetsServiceInstance: Sheets

    @Before
    fun setup() {
        mockGoogleSheetsService = mock()
        mockGoogleSheetsExporter = mock()
        mockLogger = mock()
        mockWalletRepository = mock()
        mockSheetsServiceInstance = mock()

        repository = TransactionExportRepository(
            googleSheetsService = mockGoogleSheetsService,
            googleSheetsExporter = mockGoogleSheetsExporter,
            walletRepository = mockWalletRepository,
            logger = mockLogger
        )
    }

    // ========== Tests for exportToGoogleSheets() ==========

    @Test
    fun `exportToGoogleSheets should return success with URL on successful export`() {
        runBlocking {
        // Arrange
        val userId = "user123"
        val transactions = listOf(
            Transaction(
                id = "tx1",
                title = "Test Transaction",
                amount = 100.0,
                type = TransactionType.EXPENSE,
                walletId = "wallet1",
                affectedWalletIds = listOf("wallet1"),
                tags = listOf("Test"),
                userId = userId
            )
        )
        val currency = "USD"
        val expectedUrl = "https://docs.google.com/spreadsheets/d/abc123"

        val mockWallets = listOf(
            Wallet(
                id = "wallet1",
                name = "Test Wallet",
                walletType = "Physical",
                balance = 1000.0
            )
        )

        // Mock the service chain
        whenever(mockGoogleSheetsService.isAuthorized()).thenReturn(true)
        whenever(mockGoogleSheetsService.createSheetsService()).thenReturn(Result.success(mockSheetsServiceInstance))
        whenever(mockWalletRepository.getUserWallets(userId)).thenReturn(flowOf(mockWallets))
        whenever(mockGoogleSheetsExporter.exportTransactions(any<Sheets>(), any<List<List<String>>>(), any<String>())).thenReturn(Result.success(expectedUrl))

        // Act
        val result = repository.exportToGoogleSheets(transactions, currency, userId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedUrl, result.getOrNull())
        verify(mockGoogleSheetsService).isAuthorized()
        verify(mockGoogleSheetsService).createSheetsService()
        verify(mockGoogleSheetsExporter).exportTransactions(any<Sheets>(), any<List<List<String>>>(), any<String>())
        }
    }

    @Test
    fun `exportToGoogleSheets should return failure when user not authorized`() {
        runBlocking {
        // Arrange
        val userId = "user123"
        val transactions = listOf(
            Transaction(
                id = "tx1",
                title = "Test",
                amount = 100.0,
                type = TransactionType.EXPENSE,
                walletId = "wallet1",
                affectedWalletIds = listOf("wallet1"),
                tags = listOf("Test"),
                userId = userId
            )
        )
        val currency = "USD"

        whenever(mockGoogleSheetsService.isAuthorized()).thenReturn(false)

        // Act
        val result = repository.exportToGoogleSheets(transactions, currency, userId)

        // Assert
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception is SecurityException)
        assertTrue(exception?.message?.contains("not authorized") == true)
        }
    }

    @Test
    fun `exportToGoogleSheets should return failure on network error`() {
        runBlocking {
        // Arrange
        val userId = "user123"
        val transactions = listOf(
            Transaction(
                id = "tx1",
                title = "Test",
                amount = 100.0,
                type = TransactionType.EXPENSE,
                walletId = "wallet1",
                affectedWalletIds = listOf("wallet1"),
                tags = listOf("Test"),
                userId = userId
            )
        )
        val currency = "USD"
        val networkError = IOException("Network connection failed")

        val mockWallets = listOf(
            Wallet(
                id = "wallet1",
                name = "Test Wallet",
                walletType = "Physical",
                balance = 1000.0
            )
        )

        whenever(mockGoogleSheetsService.isAuthorized()).thenReturn(true)
        whenever(mockGoogleSheetsService.createSheetsService()).thenReturn(Result.success(mockSheetsServiceInstance))
        whenever(mockWalletRepository.getUserWallets(userId)).thenReturn(flowOf(mockWallets))
        whenever(mockGoogleSheetsExporter.exportTransactions(any<Sheets>(), any<List<List<String>>>(), any<String>())).thenReturn(Result.failure(networkError))

        // Act
        val result = repository.exportToGoogleSheets(transactions, currency, userId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(networkError, result.exceptionOrNull())
        }
    }

    @Test
    fun `exportToGoogleSheets should handle empty transaction list`() {
        runBlocking {
        // Arrange
        val userId = "user123"
        val emptyTransactions = emptyList<Transaction>()
        val currency = "USD"
        val expectedUrl = "https://docs.google.com/spreadsheets/d/empty123"

        whenever(mockGoogleSheetsService.isAuthorized()).thenReturn(true)
        whenever(mockGoogleSheetsService.createSheetsService()).thenReturn(Result.success(mockSheetsServiceInstance))
        whenever(mockWalletRepository.getUserWallets(userId)).thenReturn(flowOf(emptyList()))
        whenever(mockGoogleSheetsExporter.exportTransactions(any<Sheets>(), any<List<List<String>>>(), any<String>())).thenReturn(Result.success(expectedUrl))

        // Act
        val result = repository.exportToGoogleSheets(emptyTransactions, currency, userId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedUrl, result.getOrNull())
        }
    }

    @Test
    fun `exportToGoogleSheets should return failure when Sheets service creation fails`() {
        runBlocking {
        // Arrange
        val userId = "user123"
        val transactions = listOf(
            Transaction(
                id = "tx1",
                title = "Test",
                amount = 100.0,
                type = TransactionType.EXPENSE,
                walletId = "wallet1",
                affectedWalletIds = listOf("wallet1"),
                tags = listOf("Test"),
                userId = userId
            )
        )
        val currency = "USD"
        val serviceError = SecurityException("Failed to create Sheets service")

        whenever(mockGoogleSheetsService.isAuthorized()).thenReturn(true)
        whenever(mockGoogleSheetsService.createSheetsService()).thenReturn(Result.failure(serviceError))

        // Act
        val result = repository.exportToGoogleSheets(transactions, currency, userId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(serviceError, result.exceptionOrNull())
        }
    }

    // ========== Tests for checkExportPermissions() ==========

    @Test
    fun `checkExportPermissions should return true when user has permissions`() {
        // Arrange
        whenever(mockGoogleSheetsService.isAuthorized()).thenReturn(true)

        // Act
        val hasPermissions = repository.checkExportPermissions()

        // Assert
        assertTrue(hasPermissions)
        verify(mockGoogleSheetsService).isAuthorized()
    }

    @Test
    fun `checkExportPermissions should return false when user lacks permissions`() {
        // Arrange
        whenever(mockGoogleSheetsService.isAuthorized()).thenReturn(false)

        // Act
        val hasPermissions = repository.checkExportPermissions()

        // Assert
        assertFalse(hasPermissions)
        verify(mockGoogleSheetsService).isAuthorized()
    }

    @Test
    fun `checkExportPermissions should return false when user not signed in`() {
        // Arrange
        whenever(mockGoogleSheetsService.isAuthorized()).thenReturn(false)

        // Act
        val hasPermissions = repository.checkExportPermissions()

        // Assert
        assertFalse(hasPermissions)
    }

    // ========== Additional edge case tests ==========

    @Test
    fun `exportToGoogleSheets should handle large transaction list`() {
        runBlocking {
        // Arrange
        val userId = "user123"
        val largeTransactionList = (1..500).map { index ->
            Transaction(
                id = "tx$index",
                title = "Transaction $index",
                amount = 100.0 * index,
                type = TransactionType.EXPENSE,
                walletId = "wallet1",
                affectedWalletIds = listOf("wallet1"),
                tags = listOf("Test"),
                userId = userId
            )
        }
        val currency = "USD"
        val expectedUrl = "https://docs.google.com/spreadsheets/d/large123"

        val mockWallets = listOf(
            Wallet(
                id = "wallet1",
                name = "Test Wallet",
                walletType = "Physical",
                balance = 1000.0
            )
        )

        whenever(mockGoogleSheetsService.isAuthorized()).thenReturn(true)
        whenever(mockGoogleSheetsService.createSheetsService()).thenReturn(Result.success(mockSheetsServiceInstance))
        whenever(mockWalletRepository.getUserWallets(userId)).thenReturn(flowOf(mockWallets))
        whenever(mockGoogleSheetsExporter.exportTransactions(any<Sheets>(), any<List<List<String>>>(), any<String>())).thenReturn(Result.success(expectedUrl))

        // Act
        val result = repository.exportToGoogleSheets(largeTransactionList, currency, userId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedUrl, result.getOrNull())
        }
    }

    @Test
    fun `exportToGoogleSheets should handle wallet repository errors gracefully`() {
        runBlocking {
        // Arrange
        val userId = "user123"
        val transactions = listOf(
            Transaction(
                id = "tx1",
                title = "Test",
                amount = 100.0,
                type = TransactionType.EXPENSE,
                walletId = "wallet1",
                affectedWalletIds = listOf("wallet1"),
                tags = listOf("Test"),
                userId = userId
            )
        )
        val currency = "USD"

        whenever(mockGoogleSheetsService.isAuthorized()).thenReturn(true)
        whenever(mockGoogleSheetsService.createSheetsService()).thenReturn(Result.success(mockSheetsServiceInstance))
        whenever(mockWalletRepository.getUserWallets(userId)).thenThrow(RuntimeException("Database error"))

        // Act
        val result = repository.exportToGoogleSheets(transactions, currency, userId)

        // Assert
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception is RuntimeException)
        }
    }
}
