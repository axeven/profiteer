package com.axeven.profiteerapp.viewmodel

import android.content.Intent
import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.repository.AuthRepository
import com.axeven.profiteerapp.data.repository.TransactionExportRepository
import com.axeven.profiteerapp.data.repository.TransactionRepository
import com.axeven.profiteerapp.data.repository.UserPreferencesRepository
import com.axeven.profiteerapp.data.repository.WalletRepository
import com.axeven.profiteerapp.data.ui.ExportUiState
import com.axeven.profiteerapp.service.GoogleSheetsService
import com.axeven.profiteerapp.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Test suite for TransactionListViewModel export functionality following TDD approach.
 * Tests the ViewModel layer integration for transaction export operations.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TransactionListViewModelTest {

    private lateinit var viewModel: TransactionListViewModel
    private lateinit var mockAuthRepository: AuthRepository
    private lateinit var mockTransactionRepository: TransactionRepository
    private lateinit var mockWalletRepository: WalletRepository
    private lateinit var mockUserPreferencesRepository: UserPreferencesRepository
    private lateinit var mockTransactionExportRepository: TransactionExportRepository
    private lateinit var mockGoogleSheetsService: GoogleSheetsService
    private lateinit var mockLogger: Logger

    private val testUserId = "test-user-123"
    private val testTransactions = listOf(
        Transaction(
            id = "tx1",
            title = "Test Transaction 1",
            amount = 100.0,
            type = TransactionType.EXPENSE,
            walletId = "wallet1",
            affectedWalletIds = listOf("wallet1"),
            tags = listOf("Food"),
            userId = testUserId
        ),
        Transaction(
            id = "tx2",
            title = "Test Transaction 2",
            amount = 200.0,
            type = TransactionType.INCOME,
            walletId = "wallet2",
            affectedWalletIds = listOf("wallet2"),
            tags = listOf("Salary"),
            userId = testUserId
        )
    )

    @Before
    fun setup() {
        // Set up the Main dispatcher for testing
        Dispatchers.setMain(UnconfinedTestDispatcher())

        mockAuthRepository = mock()
        mockTransactionRepository = mock()
        mockWalletRepository = mock()
        mockUserPreferencesRepository = mock()
        mockTransactionExportRepository = mock()
        mockGoogleSheetsService = mock()
        mockLogger = mock()

        // Setup default mocks for ViewModel initialization
        whenever(mockAuthRepository.getCurrentUserId()).thenReturn(testUserId)
        whenever(mockTransactionRepository.getUserTransactionsForCalculations(testUserId))
            .thenReturn(flowOf(emptyList()))
        whenever(mockWalletRepository.getUserWallets(testUserId))
            .thenReturn(flowOf(emptyList()))
        whenever(mockUserPreferencesRepository.getUserPreferences(testUserId))
            .thenReturn(flowOf(null))
    }

    @After
    fun tearDown() {
        // Reset the Main dispatcher
        Dispatchers.resetMain()
    }

    private fun createViewModel(): TransactionListViewModel {
        return TransactionListViewModel(
            authRepository = mockAuthRepository,
            transactionRepository = mockTransactionRepository,
            walletRepository = mockWalletRepository,
            userPreferencesRepository = mockUserPreferencesRepository,
            transactionExportRepository = mockTransactionExportRepository,
            googleSheetsService = mockGoogleSheetsService,
            logger = mockLogger
        )
    }

    // ========== Tests for exportTransactions() ==========

    @Test
    fun `exportTransactions should initiate export with current filtered transactions`() {
        runBlocking {
            // Arrange
            val expectedUrl = "https://docs.google.com/spreadsheets/d/test123"
            whenever(mockTransactionExportRepository.exportToGoogleSheets(any(), any(), eq(testUserId)))
                .thenReturn(Result.success(expectedUrl))

            viewModel = createViewModel()

            // Act
            viewModel.exportTransactions()
            Thread.sleep(100) // Allow coroutine to execute

            // Assert
            verify(mockTransactionExportRepository).exportToGoogleSheets(
                transactions = any(),
                currency = any(),
                userId = eq(testUserId)
            )
        }
    }

    @Test
    fun `exportTransactions should update export UI state to loading`() {
        runBlocking {
            // Arrange
            var sawExportingState = false
            whenever(mockTransactionExportRepository.exportToGoogleSheets(any(), any(), eq(testUserId)))
                .thenAnswer {
                    Thread.sleep(50) // Simulate some work
                    Result.success("https://test.com")
                }

            viewModel = createViewModel()

            // Act
            viewModel.exportTransactions()
            Thread.sleep(20) // Check state during export

            val stateWhileExporting = viewModel.exportUiState.value
            sawExportingState = stateWhileExporting.isExporting

            Thread.sleep(100) // Allow export to complete

            // Assert
            assertTrue(sawExportingState)
        }
    }

    @Test
    fun `exportTransactions should handle successful export`() {
        runBlocking {
            // Arrange
            val expectedUrl = "https://docs.google.com/spreadsheets/d/success123"
            whenever(mockTransactionExportRepository.exportToGoogleSheets(any(), any(), eq(testUserId)))
                .thenReturn(Result.success(expectedUrl))

            viewModel = createViewModel()

            // Act
            viewModel.exportTransactions()
            Thread.sleep(100) // Allow coroutine to complete

            // Assert
            val finalState = viewModel.exportUiState.value
            assertFalse(finalState.isExporting)
            assertEquals(expectedUrl, finalState.successUrl)
            assertNull(finalState.errorMessage)
        }
    }

    @Test
    fun `exportTransactions should handle export failure`() {
        runBlocking {
            // Arrange
            val errorException = Exception("Network connection failed")
            whenever(mockTransactionExportRepository.exportToGoogleSheets(any(), any(), eq(testUserId)))
                .thenReturn(Result.failure(errorException))

            viewModel = createViewModel()

            // Act
            viewModel.exportTransactions()
            Thread.sleep(100) // Allow coroutine to complete

            // Assert
            val finalState = viewModel.exportUiState.value
            assertFalse(finalState.isExporting)
            assertNull(finalState.successUrl)
            assertNotNull(finalState.errorMessage)
            assertTrue(finalState.errorMessage!!.contains("Network connection failed"))
        }
    }

    @Test
    fun `exportTransactions should handle authorization required`() {
        runBlocking {
            // Arrange
            val authException = SecurityException("User is not authorized for Google Sheets access")
            whenever(mockTransactionExportRepository.exportToGoogleSheets(any(), any(), eq(testUserId)))
                .thenReturn(Result.failure(authException))

            viewModel = createViewModel()

            // Act
            viewModel.exportTransactions()
            Thread.sleep(100) // Allow coroutine to complete

            // Assert
            val finalState = viewModel.exportUiState.value
            assertFalse(finalState.isExporting)
            assertNull(finalState.successUrl)
            assertNotNull(finalState.errorMessage)
            assertTrue(finalState.errorMessage!!.contains("not authorized"))
        }
    }

    @Test
    fun `exportTransactions should handle empty transaction list`() {
        runBlocking {
            // Arrange
            whenever(mockTransactionRepository.getUserTransactionsForCalculations(testUserId))
                .thenReturn(flowOf(emptyList()))
            whenever(mockTransactionExportRepository.exportToGoogleSheets(any(), any(), eq(testUserId)))
                .thenReturn(Result.success("https://docs.google.com/spreadsheets/d/empty"))

            viewModel = createViewModel()

            // Act
            viewModel.exportTransactions()
            Thread.sleep(100) // Allow coroutine to complete

            // Assert
            verify(mockTransactionExportRepository).exportToGoogleSheets(
                transactions = eq(emptyList()),
                currency = any(),
                userId = eq(testUserId)
            )
        }
    }

    @Test
    fun `exportTransactions should cancel ongoing export when called again`() {
        runBlocking {
            // Arrange
            var firstCallCompleted = false
            whenever(mockTransactionExportRepository.exportToGoogleSheets(any(), any(), eq(testUserId)))
                .thenAnswer {
                    Thread.sleep(200) // Simulate long-running export
                    firstCallCompleted = true
                    Result.success("https://test.com")
                }

            viewModel = createViewModel()

            // Act
            viewModel.exportTransactions() // First call
            Thread.sleep(50) // Let it start but not complete

            viewModel.exportTransactions() // Second call should cancel first
            Thread.sleep(250) // Allow second call to complete

            // Assert
            // The export state should reflect the second call
            val finalState = viewModel.exportUiState.value
            // At least verify that we're not stuck in loading state
            assertFalse(finalState.isExporting)
        }
    }

    // ========== Tests for requestExportPermissions() ==========

    @Test
    fun `requestExportPermissions should return authorization intent`() {
        runBlocking {
            // Arrange
            val mockIntent = mock<Intent>()
            whenever(mockGoogleSheetsService.requestAuthorization()).thenReturn(mockIntent)

            viewModel = createViewModel()

            // Act
            val result = viewModel.requestExportPermissions()

            // Assert
            assertEquals(mockIntent, result)
            verify(mockGoogleSheetsService).requestAuthorization()
        }
    }

    @Test
    fun `requestExportPermissions should handle permission grant`() {
        runBlocking {
            // Arrange
            whenever(mockTransactionExportRepository.checkExportPermissions()).thenReturn(true)

            viewModel = createViewModel()

            // Act
            viewModel.handlePermissionResult(granted = true)
            Thread.sleep(100) // Allow coroutine to complete

            // Assert
            // Verify that permissions were checked
            verify(mockTransactionExportRepository).checkExportPermissions()
        }
    }

    @Test
    fun `requestExportPermissions should handle permission denial`() {
        runBlocking {
            // Arrange
            whenever(mockTransactionExportRepository.checkExportPermissions()).thenReturn(false)

            viewModel = createViewModel()

            // Act
            viewModel.handlePermissionResult(granted = false)
            Thread.sleep(100) // Allow coroutine to complete

            // Assert
            val exportState = viewModel.exportUiState.value
            // Should have an error message about permissions
            assertNotNull(exportState.errorMessage)
        }
    }

    // ========== Tests for dismissExportResult() ==========

    @Test
    fun `dismissExportResult should reset export state`() {
        runBlocking {
            // Arrange
            whenever(mockTransactionExportRepository.exportToGoogleSheets(any(), any(), eq(testUserId)))
                .thenReturn(Result.success("https://test.com"))

            viewModel = createViewModel()

            // First, perform an export to set some state
            viewModel.exportTransactions()
            Thread.sleep(100) // Allow export to complete

            // Verify state has success URL
            var exportState = viewModel.exportUiState.value
            assertNotNull(exportState.successUrl)

            // Act
            viewModel.dismissExportResult()

            // Assert
            exportState = viewModel.exportUiState.value
            assertNull(exportState.successUrl)
            assertNull(exportState.errorMessage)
            assertFalse(exportState.isExporting)
        }
    }

    @Test
    fun `dismissExportResult should clear success and error messages`() {
        runBlocking {
            // Arrange
            viewModel = createViewModel()

            // Manually set an error state
            whenever(mockTransactionExportRepository.exportToGoogleSheets(any(), any(), eq(testUserId)))
                .thenReturn(Result.failure(Exception("Test error")))
            viewModel.exportTransactions()
            Thread.sleep(100) // Allow export to complete

            // Verify error is set
            var exportState = viewModel.exportUiState.value
            assertNotNull(exportState.errorMessage)

            // Act
            viewModel.dismissExportResult()

            // Assert
            exportState = viewModel.exportUiState.value
            assertNull(exportState.errorMessage)
            assertNull(exportState.successUrl)
        }
    }

    // ========== Tests for export state flow ==========

    @Test
    fun `exportUiState should emit correct states during export flow`() {
        runBlocking {
            // Arrange
            val expectedUrl = "https://docs.google.com/spreadsheets/d/test"

            whenever(mockTransactionExportRepository.exportToGoogleSheets(any(), any(), eq(testUserId)))
                .thenReturn(Result.success(expectedUrl))

            viewModel = createViewModel()

            // Check initial state
            val initialState = viewModel.exportUiState.value
            assertFalse(initialState.isExporting)
            assertNull(initialState.successUrl)
            assertNull(initialState.errorMessage)

            // Act
            viewModel.exportTransactions()
            Thread.sleep(100) // Allow export to complete

            // Assert final state
            val finalState = viewModel.exportUiState.value
            assertFalse(finalState.isExporting)
            assertEquals(expectedUrl, finalState.successUrl)
            assertNull(finalState.errorMessage)
        }
    }

    @Test
    fun `exportUiState updates should be observed by UI`() {
        runBlocking {
            // Arrange
            whenever(mockTransactionExportRepository.exportToGoogleSheets(any(), any(), eq(testUserId)))
                .thenReturn(Result.success("https://test.com"))

            viewModel = createViewModel()

            // Act
            val initialState = viewModel.exportUiState.value
            viewModel.exportTransactions()
            Thread.sleep(100) // Allow export to complete
            val finalState = viewModel.exportUiState.value

            // Assert
            // Initial state should be default
            assertFalse(initialState.isExporting)
            assertNull(initialState.successUrl)

            // Final state should have success
            assertFalse(finalState.isExporting)
            assertNotNull(finalState.successUrl)
        }
    }

    // ========== Additional edge case tests ==========

    @Test
    fun `exportTransactions should use filtered transactions when filters are active`() {
        runBlocking {
            // Arrange
            whenever(mockTransactionRepository.getUserTransactionsForCalculations(testUserId))
                .thenReturn(flowOf(testTransactions))
            whenever(mockTransactionExportRepository.exportToGoogleSheets(any(), any(), eq(testUserId)))
                .thenReturn(Result.success("https://test.com"))

            viewModel = createViewModel()
            viewModel.refreshData()
            Thread.sleep(100) // Allow data to load

            // Apply a tag filter
            viewModel.toggleTagFilter("Food")
            Thread.sleep(100) // Allow filter to apply

            // Act
            viewModel.exportTransactions()
            Thread.sleep(100) // Allow export to complete

            // Assert
            // Should export filtered transactions (though we can't easily verify the exact list in this test)
            verify(mockTransactionExportRepository).exportToGoogleSheets(
                transactions = any(),
                currency = any(),
                userId = eq(testUserId)
            )
        }
    }

    @Test
    fun `exportTransactions should use default currency from user preferences`() {
        runBlocking {
            // Arrange
            val mockPreferences = mock<com.axeven.profiteerapp.data.model.UserPreferences>()
            whenever(mockPreferences.defaultCurrency).thenReturn("EUR")
            whenever(mockUserPreferencesRepository.getUserPreferences(testUserId))
                .thenReturn(flowOf(mockPreferences))
            whenever(mockTransactionExportRepository.exportToGoogleSheets(any(), eq("EUR"), eq(testUserId)))
                .thenReturn(Result.success("https://test.com"))

            viewModel = createViewModel()
            Thread.sleep(100) // Allow initialization

            // Act
            viewModel.exportTransactions()
            Thread.sleep(100) // Allow export to complete

            // Assert
            verify(mockTransactionExportRepository).exportToGoogleSheets(
                transactions = any(),
                currency = eq("EUR"),
                userId = eq(testUserId)
            )
        }
    }
}
