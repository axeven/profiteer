package com.axeven.profiteerapp.viewmodel

import com.axeven.profiteerapp.data.constants.WalletType
import com.axeven.profiteerapp.data.model.DateFilterPeriod
import com.axeven.profiteerapp.data.model.PhysicalForm
import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.data.model.WalletFilter
import com.axeven.profiteerapp.data.repository.AuthRepository
import com.axeven.profiteerapp.data.repository.TransactionRepository
import com.axeven.profiteerapp.data.repository.UserPreferencesRepository
import com.axeven.profiteerapp.data.repository.WalletRepository
import com.axeven.profiteerapp.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.util.*

/**
 * Integration tests for wallet filter functionality in ReportViewModel.
 *
 * Tests the integration of wallet filtering with:
 * - All chart types (Portfolio, Physical Wallets, Logical Wallets, Expense Tags, Income Tags)
 * - Date filtering (combined filters)
 * - Filter state management and persistence
 * - Empty state handling
 * - Data updates when switching wallets
 *
 * These tests verify that the wallet filter works correctly across the entire
 * Reports feature, not just individual components.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WalletFilterIntegrationTest {

    private lateinit var viewModel: ReportViewModel
    private lateinit var authRepository: AuthRepository
    private lateinit var walletRepository: WalletRepository
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var logger: Logger

    private val testDispatcher = StandardTestDispatcher()

    // ========== Test Data Helper Functions ==========

    private fun createDate(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0): Date {
        return Calendar.getInstance().apply {
            set(year, month - 1, day, hour, minute, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    private fun createPhysicalWallet(
        id: String,
        name: String,
        balance: Double,
        physicalForm: PhysicalForm = PhysicalForm.FIAT_CURRENCY
    ): Wallet {
        return Wallet(
            id = id,
            name = name,
            balance = balance,
            walletType = WalletType.PHYSICAL.displayName,
            physicalForm = physicalForm,
            userId = "user1"
        )
    }

    private fun createLogicalWallet(
        id: String,
        name: String,
        balance: Double
    ): Wallet {
        return Wallet(
            id = id,
            name = name,
            balance = balance,
            walletType = WalletType.LOGICAL.displayName,
            physicalForm = PhysicalForm.FIAT_CURRENCY,
            userId = "user1"
        )
    }

    private fun createTransaction(
        id: String,
        type: TransactionType,
        amount: Double,
        transactionDate: Date?,
        tags: List<String> = emptyList(),
        affectedWalletIds: List<String> = emptyList(),
        sourceWalletId: String = "",
        destinationWalletId: String = ""
    ): Transaction {
        return Transaction(
            id = id,
            title = "Test Transaction",
            amount = amount,
            type = type,
            tags = tags,
            transactionDate = transactionDate,
            affectedWalletIds = affectedWalletIds,
            sourceWalletId = sourceWalletId,
            destinationWalletId = destinationWalletId,
            userId = "user1"
        )
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        authRepository = mock()
        walletRepository = mock()
        transactionRepository = mock()
        userPreferencesRepository = mock()
        logger = mock()

        whenever(authRepository.getCurrentUserId()).thenReturn("user1")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========== Test 1: Wallet Filter Affects Physical Wallet Balance Chart ==========

    @Test
    fun `selectWalletFilter - affects physical wallet balance chart`() = runTest {
        val wallet1 = createPhysicalWallet("w1", "Cash Wallet", 1000.0)
        val wallet2 = createPhysicalWallet("w2", "Bank Account", 2000.0)
        val wallets = listOf(wallet1, wallet2)

        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 500.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.INCOME, 1500.0,
                createDate(2025, 10, 16), affectedWalletIds = listOf("w2"))
        )

        whenever(walletRepository.getUserWallets("user1")).thenReturn(flowOf(wallets))
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(userPreferencesRepository.getUserPreferences("user1")).thenReturn(flowOf(null))

        viewModel = ReportViewModel(
            authRepository, walletRepository, transactionRepository,
            userPreferencesRepository, logger
        )
        viewModel.loadPortfolioData()
        testDispatcher.scheduler.advanceUntilIdle()

        // Initially shows all physical wallets
        assertEquals(2, viewModel.uiState.value.physicalWalletBalances.size)
        assertTrue(viewModel.uiState.value.physicalWalletBalances.containsKey("Cash Wallet"))
        assertTrue(viewModel.uiState.value.physicalWalletBalances.containsKey("Bank Account"))

        // Filter by wallet2
        viewModel.selectWalletFilter(WalletFilter.SpecificWallet("w2", "Bank Account"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Should only show wallet2
        assertEquals(1, viewModel.uiState.value.physicalWalletBalances.size)
        assertTrue(viewModel.uiState.value.physicalWalletBalances.containsKey("Bank Account"))
        assertFalse(viewModel.uiState.value.physicalWalletBalances.containsKey("Cash Wallet"))
    }

    @Test
    fun `selectWalletFilter - affects logical wallet balance chart`() = runTest {
        val logical1 = createLogicalWallet("l1", "Savings Goal", 500.0)
        val logical2 = createLogicalWallet("l2", "Emergency Fund", 1500.0)
        val wallets = listOf(logical1, logical2)

        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 500.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("l1")),
            createTransaction("t2", TransactionType.INCOME, 1500.0,
                createDate(2025, 10, 16), affectedWalletIds = listOf("l2"))
        )

        whenever(walletRepository.getUserWallets("user1")).thenReturn(flowOf(wallets))
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(userPreferencesRepository.getUserPreferences("user1")).thenReturn(flowOf(null))

        viewModel = ReportViewModel(
            authRepository, walletRepository, transactionRepository,
            userPreferencesRepository, logger
        )
        viewModel.loadPortfolioData()
        testDispatcher.scheduler.advanceUntilIdle()

        // Initially shows all logical wallets
        assertEquals(2, viewModel.uiState.value.logicalWalletBalances.size)

        // Filter by logical1
        viewModel.selectWalletFilter(WalletFilter.SpecificWallet("l1", "Savings Goal"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Should only show logical1
        assertEquals(1, viewModel.uiState.value.logicalWalletBalances.size)
        assertTrue(viewModel.uiState.value.logicalWalletBalances.containsKey("Savings Goal"))
        assertFalse(viewModel.uiState.value.logicalWalletBalances.containsKey("Emergency Fund"))
    }

    // ========== Test 2: Wallet Filter Affects Transaction Tag Charts ==========

    @Test
    fun `selectWalletFilter - affects expense transactions by tag chart`() = runTest {
        val wallet1 = createPhysicalWallet("w1", "Cash Wallet", 1000.0)
        val wallet2 = createPhysicalWallet("w2", "Bank Account", 2000.0)
        val wallets = listOf(wallet1, wallet2)

        val transactions = listOf(
            createTransaction("t1", TransactionType.EXPENSE, 50.0,
                createDate(2025, 10, 15), tags = listOf("food"),
                affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.EXPENSE, 100.0,
                createDate(2025, 10, 16), tags = listOf("food"),
                affectedWalletIds = listOf("w2")),
            createTransaction("t3", TransactionType.EXPENSE, 75.0,
                createDate(2025, 10, 17), tags = listOf("transport"),
                affectedWalletIds = listOf("w1"))
        )

        whenever(walletRepository.getUserWallets("user1")).thenReturn(flowOf(wallets))
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(userPreferencesRepository.getUserPreferences("user1")).thenReturn(flowOf(null))

        viewModel = ReportViewModel(
            authRepository, walletRepository, transactionRepository,
            userPreferencesRepository, logger
        )
        viewModel.loadPortfolioData()
        testDispatcher.scheduler.advanceUntilIdle()

        // Initially shows all expense transactions (food: 150, transport: 75)
        assertEquals(2, viewModel.uiState.value.expenseTransactionsByTag.size)
        assertEquals(150.0, viewModel.uiState.value.expenseTransactionsByTag["food"] ?: 0.0, 0.01)
        assertEquals(75.0, viewModel.uiState.value.expenseTransactionsByTag["transport"] ?: 0.0, 0.01)

        // Filter by wallet1 (should show food: 50, transport: 75)
        viewModel.selectWalletFilter(WalletFilter.SpecificWallet("w1", "Cash Wallet"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.expenseTransactionsByTag.size)
        assertEquals(50.0, viewModel.uiState.value.expenseTransactionsByTag["food"] ?: 0.0, 0.01)
        assertEquals(75.0, viewModel.uiState.value.expenseTransactionsByTag["transport"] ?: 0.0, 0.01)
    }

    @Test
    fun `selectWalletFilter - affects income transactions by tag chart`() = runTest {
        val wallet1 = createPhysicalWallet("w1", "Cash Wallet", 1000.0)
        val wallet2 = createPhysicalWallet("w2", "Bank Account", 2000.0)
        val wallets = listOf(wallet1, wallet2)

        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 500.0,
                createDate(2025, 10, 15), tags = listOf("salary"),
                affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.INCOME, 1500.0,
                createDate(2025, 10, 16), tags = listOf("salary"),
                affectedWalletIds = listOf("w2")),
            createTransaction("t3", TransactionType.INCOME, 200.0,
                createDate(2025, 10, 17), tags = listOf("bonus"),
                affectedWalletIds = listOf("w1"))
        )

        whenever(walletRepository.getUserWallets("user1")).thenReturn(flowOf(wallets))
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(userPreferencesRepository.getUserPreferences("user1")).thenReturn(flowOf(null))

        viewModel = ReportViewModel(
            authRepository, walletRepository, transactionRepository,
            userPreferencesRepository, logger
        )
        viewModel.loadPortfolioData()
        testDispatcher.scheduler.advanceUntilIdle()

        // Initially shows all income transactions (salary: 2000, bonus: 200)
        assertEquals(2, viewModel.uiState.value.incomeTransactionsByTag.size)
        assertEquals(2000.0, viewModel.uiState.value.incomeTransactionsByTag["salary"] ?: 0.0, 0.01)
        assertEquals(200.0, viewModel.uiState.value.incomeTransactionsByTag["bonus"] ?: 0.0, 0.01)

        // Filter by wallet2 (should show only salary: 1500)
        viewModel.selectWalletFilter(WalletFilter.SpecificWallet("w2", "Bank Account"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.incomeTransactionsByTag.size)
        assertEquals(1500.0, viewModel.uiState.value.incomeTransactionsByTag["salary"] ?: 0.0, 0.01)
        assertFalse(viewModel.uiState.value.incomeTransactionsByTag.containsKey("bonus"))
    }

    // ========== Test 3: Combining Date Filter + Wallet Filter ==========

    @Test
    fun `combined filters - date and wallet filter work together for physical wallets`() = runTest {
        val wallet1 = createPhysicalWallet("w1", "Cash Wallet", 1000.0)
        val wallet2 = createPhysicalWallet("w2", "Bank Account", 2000.0)
        val wallets = listOf(wallet1, wallet2)

        val transactions = listOf(
            // October wallet1
            createTransaction("t1", TransactionType.INCOME, 500.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1")),
            // October wallet2
            createTransaction("t2", TransactionType.INCOME, 1000.0,
                createDate(2025, 10, 16), affectedWalletIds = listOf("w2")),
            // November wallet1
            createTransaction("t3", TransactionType.INCOME, 300.0,
                createDate(2025, 11, 5), affectedWalletIds = listOf("w1")),
            // November wallet2
            createTransaction("t4", TransactionType.INCOME, 700.0,
                createDate(2025, 11, 6), affectedWalletIds = listOf("w2"))
        )

        whenever(walletRepository.getUserWallets("user1")).thenReturn(flowOf(wallets))
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(userPreferencesRepository.getUserPreferences("user1")).thenReturn(flowOf(null))

        viewModel = ReportViewModel(
            authRepository, walletRepository, transactionRepository,
            userPreferencesRepository, logger
        )
        viewModel.loadPortfolioData()
        testDispatcher.scheduler.advanceUntilIdle()

        // Apply date filter first (October 2025)
        viewModel.selectDateFilter(DateFilterPeriod.Month(2025, 10))
        testDispatcher.scheduler.advanceUntilIdle()

        // Should show both wallets
        assertEquals(2, viewModel.uiState.value.physicalWalletBalances.size)

        // Now apply wallet filter (wallet1)
        viewModel.selectWalletFilter(WalletFilter.SpecificWallet("w1", "Cash Wallet"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Should show only wallet1
        assertEquals(1, viewModel.uiState.value.physicalWalletBalances.size)
        assertTrue(viewModel.uiState.value.physicalWalletBalances.containsKey("Cash Wallet"))
    }

    @Test
    fun `combined filters - date and wallet filter work together for expense tags`() = runTest {
        val wallet1 = createPhysicalWallet("w1", "Cash Wallet", 1000.0)
        val wallet2 = createPhysicalWallet("w2", "Bank Account", 2000.0)
        val wallets = listOf(wallet1, wallet2)

        val transactions = listOf(
            // October wallet1 - food
            createTransaction("t1", TransactionType.EXPENSE, 50.0,
                createDate(2025, 10, 15), tags = listOf("food"),
                affectedWalletIds = listOf("w1")),
            // October wallet2 - food
            createTransaction("t2", TransactionType.EXPENSE, 100.0,
                createDate(2025, 10, 16), tags = listOf("food"),
                affectedWalletIds = listOf("w2")),
            // November wallet1 - food
            createTransaction("t3", TransactionType.EXPENSE, 30.0,
                createDate(2025, 11, 5), tags = listOf("food"),
                affectedWalletIds = listOf("w1")),
            // October wallet1 - transport
            createTransaction("t4", TransactionType.EXPENSE, 75.0,
                createDate(2025, 10, 17), tags = listOf("transport"),
                affectedWalletIds = listOf("w1"))
        )

        whenever(walletRepository.getUserWallets("user1")).thenReturn(flowOf(wallets))
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(userPreferencesRepository.getUserPreferences("user1")).thenReturn(flowOf(null))

        viewModel = ReportViewModel(
            authRepository, walletRepository, transactionRepository,
            userPreferencesRepository, logger
        )
        viewModel.loadPortfolioData()
        testDispatcher.scheduler.advanceUntilIdle()

        // Apply both filters: October + wallet1
        viewModel.selectDateFilter(DateFilterPeriod.Month(2025, 10))
        viewModel.selectWalletFilter(WalletFilter.SpecificWallet("w1", "Cash Wallet"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Should show only October wallet1 expenses (food: 50, transport: 75)
        assertEquals(2, viewModel.uiState.value.expenseTransactionsByTag.size)
        assertEquals(50.0, viewModel.uiState.value.expenseTransactionsByTag["food"] ?: 0.0, 0.01)
        assertEquals(75.0, viewModel.uiState.value.expenseTransactionsByTag["transport"] ?: 0.0, 0.01)
    }

    // ========== Test 4: Switching Between Wallets Updates Charts Immediately ==========

    @Test
    fun `switching wallets - updates physical wallet data immediately`() = runTest {
        val wallet1 = createPhysicalWallet("w1", "Cash Wallet", 1000.0)
        val wallet2 = createPhysicalWallet("w2", "Bank Account", 2000.0)
        val wallets = listOf(wallet1, wallet2)

        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 500.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.INCOME, 1500.0,
                createDate(2025, 10, 16), affectedWalletIds = listOf("w2"))
        )

        whenever(walletRepository.getUserWallets("user1")).thenReturn(flowOf(wallets))
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(userPreferencesRepository.getUserPreferences("user1")).thenReturn(flowOf(null))

        viewModel = ReportViewModel(
            authRepository, walletRepository, transactionRepository,
            userPreferencesRepository, logger
        )
        viewModel.loadPortfolioData()
        testDispatcher.scheduler.advanceUntilIdle()

        // Filter by wallet1
        viewModel.selectWalletFilter(WalletFilter.SpecificWallet("w1", "Cash Wallet"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.physicalWalletBalances.size)
        assertTrue(viewModel.uiState.value.physicalWalletBalances.containsKey("Cash Wallet"))

        // Switch to wallet2
        viewModel.selectWalletFilter(WalletFilter.SpecificWallet("w2", "Bank Account"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Should immediately update to show wallet2
        assertEquals(1, viewModel.uiState.value.physicalWalletBalances.size)
        assertTrue(viewModel.uiState.value.physicalWalletBalances.containsKey("Bank Account"))
        assertFalse(viewModel.uiState.value.physicalWalletBalances.containsKey("Cash Wallet"))

        // Switch back to wallet1
        viewModel.selectWalletFilter(WalletFilter.SpecificWallet("w1", "Cash Wallet"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Should immediately update to show wallet1 again
        assertEquals(1, viewModel.uiState.value.physicalWalletBalances.size)
        assertTrue(viewModel.uiState.value.physicalWalletBalances.containsKey("Cash Wallet"))
        assertFalse(viewModel.uiState.value.physicalWalletBalances.containsKey("Bank Account"))
    }

    // ========== Test 5: Resetting to "All Wallets" Restores Full Data ==========

    @Test
    fun `resetting to AllWallets - restores full physical wallet data`() = runTest {
        val wallet1 = createPhysicalWallet("w1", "Cash Wallet", 1000.0)
        val wallet2 = createPhysicalWallet("w2", "Bank Account", 2000.0)
        val wallets = listOf(wallet1, wallet2)

        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 500.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.INCOME, 1500.0,
                createDate(2025, 10, 16), affectedWalletIds = listOf("w2"))
        )

        whenever(walletRepository.getUserWallets("user1")).thenReturn(flowOf(wallets))
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(userPreferencesRepository.getUserPreferences("user1")).thenReturn(flowOf(null))

        viewModel = ReportViewModel(
            authRepository, walletRepository, transactionRepository,
            userPreferencesRepository, logger
        )
        viewModel.loadPortfolioData()
        testDispatcher.scheduler.advanceUntilIdle()

        // Initially all wallets
        assertEquals(2, viewModel.uiState.value.physicalWalletBalances.size)

        // Filter by wallet1
        viewModel.selectWalletFilter(WalletFilter.SpecificWallet("w1", "Cash Wallet"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.physicalWalletBalances.size)

        // Reset to AllWallets
        viewModel.selectWalletFilter(WalletFilter.AllWallets)
        testDispatcher.scheduler.advanceUntilIdle()

        // Should restore full data
        assertEquals(2, viewModel.uiState.value.physicalWalletBalances.size)
        assertTrue(viewModel.uiState.value.physicalWalletBalances.containsKey("Cash Wallet"))
        assertTrue(viewModel.uiState.value.physicalWalletBalances.containsKey("Bank Account"))
    }

    @Test
    fun `resetting to AllWallets - restores full expense tag data`() = runTest {
        val wallet1 = createPhysicalWallet("w1", "Cash Wallet", 1000.0)
        val wallet2 = createPhysicalWallet("w2", "Bank Account", 2000.0)
        val wallets = listOf(wallet1, wallet2)

        val transactions = listOf(
            createTransaction("t1", TransactionType.EXPENSE, 50.0,
                createDate(2025, 10, 15), tags = listOf("food"),
                affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.EXPENSE, 100.0,
                createDate(2025, 10, 16), tags = listOf("food"),
                affectedWalletIds = listOf("w2"))
        )

        whenever(walletRepository.getUserWallets("user1")).thenReturn(flowOf(wallets))
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(userPreferencesRepository.getUserPreferences("user1")).thenReturn(flowOf(null))

        viewModel = ReportViewModel(
            authRepository, walletRepository, transactionRepository,
            userPreferencesRepository, logger
        )
        viewModel.loadPortfolioData()
        testDispatcher.scheduler.advanceUntilIdle()

        // Initially all transactions (food: 150)
        assertEquals(150.0, viewModel.uiState.value.expenseTransactionsByTag["food"] ?: 0.0, 0.01)

        // Filter by wallet1
        viewModel.selectWalletFilter(WalletFilter.SpecificWallet("w1", "Cash Wallet"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(50.0, viewModel.uiState.value.expenseTransactionsByTag["food"] ?: 0.0, 0.01)

        // Reset to AllWallets
        viewModel.selectWalletFilter(WalletFilter.AllWallets)
        testDispatcher.scheduler.advanceUntilIdle()

        // Should restore full data
        assertEquals(150.0, viewModel.uiState.value.expenseTransactionsByTag["food"] ?: 0.0, 0.01)
    }

    // ========== Test 6: Wallet Filter with No Matching Transactions ==========

    @Test
    fun `wallet filter with no matching transactions - shows empty expense tags`() = runTest {
        val wallet1 = createPhysicalWallet("w1", "Cash Wallet", 1000.0)
        val wallet2 = createPhysicalWallet("w2", "Unused Wallet", 0.0)
        val wallets = listOf(wallet1, wallet2)

        val transactions = listOf(
            createTransaction("t1", TransactionType.EXPENSE, 50.0,
                createDate(2025, 10, 15), tags = listOf("food"),
                affectedWalletIds = listOf("w1"))
        )

        whenever(walletRepository.getUserWallets("user1")).thenReturn(flowOf(wallets))
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(userPreferencesRepository.getUserPreferences("user1")).thenReturn(flowOf(null))

        viewModel = ReportViewModel(
            authRepository, walletRepository, transactionRepository,
            userPreferencesRepository, logger
        )
        viewModel.loadPortfolioData()
        testDispatcher.scheduler.advanceUntilIdle()

        // Filter by wallet2 (no transactions)
        viewModel.selectWalletFilter(WalletFilter.SpecificWallet("w2", "Unused Wallet"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Should show empty expense tags
        assertTrue(viewModel.uiState.value.expenseTransactionsByTag.isEmpty())
    }

    // ========== Test 7: Wallet Filter State Persistence ==========

    @Test
    fun `wallet filter - persists when switching chart types`() = runTest {
        val wallet1 = createPhysicalWallet("w1", "Cash Wallet", 1000.0)
        val wallet2 = createPhysicalWallet("w2", "Bank Account", 2000.0)
        val wallets = listOf(wallet1, wallet2)

        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 500.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.EXPENSE, 100.0,
                createDate(2025, 10, 16), tags = listOf("food"),
                affectedWalletIds = listOf("w2"))
        )

        whenever(walletRepository.getUserWallets("user1")).thenReturn(flowOf(wallets))
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(userPreferencesRepository.getUserPreferences("user1")).thenReturn(flowOf(null))

        viewModel = ReportViewModel(
            authRepository, walletRepository, transactionRepository,
            userPreferencesRepository, logger
        )
        viewModel.loadPortfolioData()
        testDispatcher.scheduler.advanceUntilIdle()

        // Apply wallet filter
        viewModel.selectWalletFilter(WalletFilter.SpecificWallet("w1", "Cash Wallet"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify filter is applied
        assertEquals(WalletFilter.SpecificWallet("w1", "Cash Wallet"), viewModel.uiState.value.selectedWalletFilter)

        // Switch chart type (simulated by user switching dropdown)
        viewModel.selectChartDataType(ChartDataType.EXPENSE_TRANSACTION_BY_TAG)
        testDispatcher.scheduler.advanceUntilIdle()

        // Filter should still be applied
        assertEquals(WalletFilter.SpecificWallet("w1", "Cash Wallet"), viewModel.uiState.value.selectedWalletFilter)

        // Switch chart type again
        viewModel.selectChartDataType(ChartDataType.PORTFOLIO_ASSET_COMPOSITION)
        testDispatcher.scheduler.advanceUntilIdle()

        // Filter should still persist
        assertEquals(WalletFilter.SpecificWallet("w1", "Cash Wallet"), viewModel.uiState.value.selectedWalletFilter)
    }

    @Test
    fun `wallet filter - persists across multiple operations`() = runTest {
        val wallet1 = createPhysicalWallet("w1", "Cash Wallet", 1000.0)
        val wallet2 = createPhysicalWallet("w2", "Bank Account", 2000.0)
        val wallets = listOf(wallet1, wallet2)

        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 500.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.INCOME, 1500.0,
                createDate(2025, 11, 5), affectedWalletIds = listOf("w2"))
        )

        whenever(walletRepository.getUserWallets("user1")).thenReturn(flowOf(wallets))
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(userPreferencesRepository.getUserPreferences("user1")).thenReturn(flowOf(null))

        viewModel = ReportViewModel(
            authRepository, walletRepository, transactionRepository,
            userPreferencesRepository, logger
        )
        viewModel.loadPortfolioData()
        testDispatcher.scheduler.advanceUntilIdle()

        // Apply wallet filter
        viewModel.selectWalletFilter(WalletFilter.SpecificWallet("w1", "Cash Wallet"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Change date filter
        viewModel.selectDateFilter(DateFilterPeriod.Month(2025, 10))
        testDispatcher.scheduler.advanceUntilIdle()

        // Wallet filter should still be active
        assertEquals(WalletFilter.SpecificWallet("w1", "Cash Wallet"), viewModel.uiState.value.selectedWalletFilter)

        // Change chart type
        viewModel.selectChartDataType(ChartDataType.PHYSICAL_WALLET_BALANCE)
        testDispatcher.scheduler.advanceUntilIdle()

        // Wallet filter should still be active
        assertEquals(WalletFilter.SpecificWallet("w1", "Cash Wallet"), viewModel.uiState.value.selectedWalletFilter)

        // Change date filter again
        viewModel.selectDateFilter(DateFilterPeriod.AllTime)
        testDispatcher.scheduler.advanceUntilIdle()

        // Wallet filter should STILL be active
        assertEquals(WalletFilter.SpecificWallet("w1", "Cash Wallet"), viewModel.uiState.value.selectedWalletFilter)
    }
}
