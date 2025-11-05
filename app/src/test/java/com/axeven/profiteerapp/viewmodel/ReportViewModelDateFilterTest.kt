package com.axeven.profiteerapp.viewmodel

import com.axeven.profiteerapp.data.constants.WalletType
import com.axeven.profiteerapp.data.model.DateFilterPeriod
import com.axeven.profiteerapp.data.model.PhysicalForm
import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.model.Wallet
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

@OptIn(ExperimentalCoroutinesApi::class)
class ReportViewModelDateFilterTest {

    private lateinit var viewModel: ReportViewModel
    private lateinit var authRepository: AuthRepository
    private lateinit var walletRepository: WalletRepository
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var logger: Logger

    private val testDispatcher = StandardTestDispatcher()

    // Helper function to create a date
    private fun createDate(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0): Date {
        return Calendar.getInstance().apply {
            set(year, month - 1, day, hour, minute, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    // Helper function to create a physical wallet
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

    // Helper function to create a logical wallet
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

    // Helper function to create a transaction
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

    // ========== State Management Tests ==========

    @Test
    fun `uiState - initial selectedDateFilter is AllTime`() = runTest {
        whenever(walletRepository.getUserWallets("user1")).thenReturn(flowOf(emptyList()))
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(emptyList()))
        whenever(userPreferencesRepository.getUserPreferences("user1")).thenReturn(flowOf(null))

        viewModel = ReportViewModel(
            authRepository, walletRepository, transactionRepository,
            userPreferencesRepository, logger
        )

        assertEquals(DateFilterPeriod.AllTime, viewModel.uiState.value.selectedDateFilter)
    }

    @Test
    fun `selectDateFilter - updates selectedDateFilter in uiState`() = runTest {
        whenever(walletRepository.getUserWallets("user1")).thenReturn(flowOf(emptyList()))
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(emptyList()))
        whenever(userPreferencesRepository.getUserPreferences("user1")).thenReturn(flowOf(null))

        viewModel = ReportViewModel(
            authRepository, walletRepository, transactionRepository,
            userPreferencesRepository, logger
        )
        viewModel.loadPortfolioData()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectDateFilter(DateFilterPeriod.Month(2025, 10))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(DateFilterPeriod.Month(2025, 10), viewModel.uiState.value.selectedDateFilter)
    }

    @Test
    fun `selectDateFilter - triggers data recalculation`() = runTest {
        val wallets = listOf(createPhysicalWallet("w1", "Wallet 1", 1000.0))
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
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

        // Initial state should have data
        assertTrue(viewModel.uiState.value.portfolioComposition.isNotEmpty())

        // Change filter
        viewModel.selectDateFilter(DateFilterPeriod.Month(2025, 9)) // Different month
        testDispatcher.scheduler.advanceUntilIdle()

        // Data should be recalculated (empty since no transactions in Sept)
        assertTrue(viewModel.uiState.value.portfolioComposition.isEmpty())
    }

    @Test
    fun `getAvailableMonths - returns months with transactions`() = runTest {
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.INCOME, 50.0,
                createDate(2025, 11, 20), affectedWalletIds = listOf("w1")),
            createTransaction("t3", TransactionType.INCOME, 30.0,
                createDate(2025, 10, 25), affectedWalletIds = listOf("w1"))
        )

        whenever(walletRepository.getUserWallets("user1")).thenReturn(flowOf(emptyList()))
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(userPreferencesRepository.getUserPreferences("user1")).thenReturn(flowOf(null))

        viewModel = ReportViewModel(
            authRepository, walletRepository, transactionRepository,
            userPreferencesRepository, logger
        )
        viewModel.loadPortfolioData()
        testDispatcher.scheduler.advanceUntilIdle()

        val availableMonths = viewModel.uiState.value.availableMonths

        assertEquals(2, availableMonths.size)
        assertTrue(availableMonths.contains(Pair(2025, 10)))
        assertTrue(availableMonths.contains(Pair(2025, 11)))
    }

    @Test
    fun `getAvailableMonths - excludes null transactionDates`() = runTest {
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.INCOME, 50.0,
                null, affectedWalletIds = listOf("w1"))
        )

        whenever(walletRepository.getUserWallets("user1")).thenReturn(flowOf(emptyList()))
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(userPreferencesRepository.getUserPreferences("user1")).thenReturn(flowOf(null))

        viewModel = ReportViewModel(
            authRepository, walletRepository, transactionRepository,
            userPreferencesRepository, logger
        )
        viewModel.loadPortfolioData()
        testDispatcher.scheduler.advanceUntilIdle()

        val availableMonths = viewModel.uiState.value.availableMonths

        assertEquals(1, availableMonths.size)
        assertTrue(availableMonths.contains(Pair(2025, 10)))
    }

    @Test
    fun `getAvailableYears - returns years with transactions`() = runTest {
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2024, 10, 15), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.INCOME, 50.0,
                createDate(2025, 11, 20), affectedWalletIds = listOf("w1")),
            createTransaction("t3", TransactionType.INCOME, 30.0,
                createDate(2025, 10, 25), affectedWalletIds = listOf("w1"))
        )

        whenever(walletRepository.getUserWallets("user1")).thenReturn(flowOf(emptyList()))
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(userPreferencesRepository.getUserPreferences("user1")).thenReturn(flowOf(null))

        viewModel = ReportViewModel(
            authRepository, walletRepository, transactionRepository,
            userPreferencesRepository, logger
        )
        viewModel.loadPortfolioData()
        testDispatcher.scheduler.advanceUntilIdle()

        val availableYears = viewModel.uiState.value.availableYears

        assertEquals(2, availableYears.size)
        assertTrue(availableYears.contains(2024))
        assertTrue(availableYears.contains(2025))
        assertEquals(listOf(2024, 2025), availableYears.sorted())
    }

    @Test
    fun `getAvailableYears - returns sorted list`() = runTest {
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2023, 10, 15), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.INCOME, 50.0,
                createDate(2025, 11, 20), affectedWalletIds = listOf("w1")),
            createTransaction("t3", TransactionType.INCOME, 30.0,
                createDate(2024, 10, 25), affectedWalletIds = listOf("w1"))
        )

        whenever(walletRepository.getUserWallets("user1")).thenReturn(flowOf(emptyList()))
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(userPreferencesRepository.getUserPreferences("user1")).thenReturn(flowOf(null))

        viewModel = ReportViewModel(
            authRepository, walletRepository, transactionRepository,
            userPreferencesRepository, logger
        )
        viewModel.loadPortfolioData()
        testDispatcher.scheduler.advanceUntilIdle()

        val availableYears = viewModel.uiState.value.availableYears

        assertEquals(listOf(2023, 2024, 2025), availableYears)
    }

    // ========== Transaction Tag Filtering Tests (Simple Filtering) ==========

    @Test
    fun `calculateExpenseTransactionsByTag - AllTime includes all transactions`() = runTest {
        val transactions = listOf(
            createTransaction("t1", TransactionType.EXPENSE, 100.0,
                createDate(2025, 10, 15), tags = listOf("food")),
            createTransaction("t2", TransactionType.EXPENSE, 50.0,
                createDate(2024, 5, 20), tags = listOf("transport"))
        )

        whenever(walletRepository.getUserWallets("user1")).thenReturn(flowOf(emptyList()))
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(userPreferencesRepository.getUserPreferences("user1")).thenReturn(flowOf(null))

        viewModel = ReportViewModel(
            authRepository, walletRepository, transactionRepository,
            userPreferencesRepository, logger
        )
        viewModel.loadPortfolioData()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectDateFilter(DateFilterPeriod.AllTime)
        testDispatcher.scheduler.advanceUntilIdle()

        val expensesByTag = viewModel.uiState.value.expenseTransactionsByTag

        assertEquals(100.0, expensesByTag["food"]!!, 0.01)
        assertEquals(50.0, expensesByTag["transport"]!!, 0.01)
    }

    @Test
    fun `calculateExpenseTransactionsByTag - Month filters by date range`() = runTest {
        val transactions = listOf(
            createTransaction("t1", TransactionType.EXPENSE, 100.0,
                createDate(2025, 10, 15), tags = listOf("food")),
            createTransaction("t2", TransactionType.EXPENSE, 50.0,
                createDate(2025, 11, 20), tags = listOf("food")),
            createTransaction("t3", TransactionType.EXPENSE, 30.0,
                createDate(2025, 10, 25), tags = listOf("transport"))
        )

        whenever(walletRepository.getUserWallets("user1")).thenReturn(flowOf(emptyList()))
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(userPreferencesRepository.getUserPreferences("user1")).thenReturn(flowOf(null))

        viewModel = ReportViewModel(
            authRepository, walletRepository, transactionRepository,
            userPreferencesRepository, logger
        )
        viewModel.loadPortfolioData()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectDateFilter(DateFilterPeriod.Month(2025, 10))
        testDispatcher.scheduler.advanceUntilIdle()

        val expensesByTag = viewModel.uiState.value.expenseTransactionsByTag

        // Only October transactions: t1(100) + t3(30) = 130 for food/transport
        assertEquals(100.0, expensesByTag["food"]!!, 0.01)
        assertEquals(30.0, expensesByTag["transport"]!!, 0.01)
        assertNull(expensesByTag["november"])
    }

    @Test
    fun `calculateExpenseTransactionsByTag - Year filters by date range`() = runTest {
        val transactions = listOf(
            createTransaction("t1", TransactionType.EXPENSE, 100.0,
                createDate(2025, 10, 15), tags = listOf("food")),
            createTransaction("t2", TransactionType.EXPENSE, 50.0,
                createDate(2024, 11, 20), tags = listOf("food")),
            createTransaction("t3", TransactionType.EXPENSE, 30.0,
                createDate(2025, 5, 25), tags = listOf("food"))
        )

        whenever(walletRepository.getUserWallets("user1")).thenReturn(flowOf(emptyList()))
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(userPreferencesRepository.getUserPreferences("user1")).thenReturn(flowOf(null))

        viewModel = ReportViewModel(
            authRepository, walletRepository, transactionRepository,
            userPreferencesRepository, logger
        )
        viewModel.loadPortfolioData()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectDateFilter(DateFilterPeriod.Year(2025))
        testDispatcher.scheduler.advanceUntilIdle()

        val expensesByTag = viewModel.uiState.value.expenseTransactionsByTag

        // Only 2025 transactions: t1(100) + t3(30) = 130
        assertEquals(130.0, expensesByTag["food"]!!, 0.01)
    }

    @Test
    fun `calculateExpenseTransactionsByTag - excludes null transactionDate`() = runTest {
        val transactions = listOf(
            createTransaction("t1", TransactionType.EXPENSE, 100.0,
                createDate(2025, 10, 15), tags = listOf("food")),
            createTransaction("t2", TransactionType.EXPENSE, 50.0,
                null, tags = listOf("food"))
        )

        whenever(walletRepository.getUserWallets("user1")).thenReturn(flowOf(emptyList()))
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(userPreferencesRepository.getUserPreferences("user1")).thenReturn(flowOf(null))

        viewModel = ReportViewModel(
            authRepository, walletRepository, transactionRepository,
            userPreferencesRepository, logger
        )
        viewModel.loadPortfolioData()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectDateFilter(DateFilterPeriod.Month(2025, 10))
        testDispatcher.scheduler.advanceUntilIdle()

        val expensesByTag = viewModel.uiState.value.expenseTransactionsByTag

        // Only t1 should be included
        assertEquals(100.0, expensesByTag["food"]!!, 0.01)
    }

    @Test
    fun `calculateIncomeTransactionsByTag - AllTime includes all transactions`() = runTest {
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 15), tags = listOf("salary")),
            createTransaction("t2", TransactionType.INCOME, 50.0,
                createDate(2024, 5, 20), tags = listOf("bonus"))
        )

        whenever(walletRepository.getUserWallets("user1")).thenReturn(flowOf(emptyList()))
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(userPreferencesRepository.getUserPreferences("user1")).thenReturn(flowOf(null))

        viewModel = ReportViewModel(
            authRepository, walletRepository, transactionRepository,
            userPreferencesRepository, logger
        )
        viewModel.loadPortfolioData()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectDateFilter(DateFilterPeriod.AllTime)
        testDispatcher.scheduler.advanceUntilIdle()

        val incomeByTag = viewModel.uiState.value.incomeTransactionsByTag

        assertEquals(100.0, incomeByTag["salary"]!!, 0.01)
        assertEquals(50.0, incomeByTag["bonus"]!!, 0.01)
    }

    @Test
    fun `calculateIncomeTransactionsByTag - Month filters by date range`() = runTest {
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 15), tags = listOf("salary")),
            createTransaction("t2", TransactionType.INCOME, 50.0,
                createDate(2025, 11, 20), tags = listOf("salary"))
        )

        whenever(walletRepository.getUserWallets("user1")).thenReturn(flowOf(emptyList()))
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(userPreferencesRepository.getUserPreferences("user1")).thenReturn(flowOf(null))

        viewModel = ReportViewModel(
            authRepository, walletRepository, transactionRepository,
            userPreferencesRepository, logger
        )
        viewModel.loadPortfolioData()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectDateFilter(DateFilterPeriod.Month(2025, 10))
        testDispatcher.scheduler.advanceUntilIdle()

        val incomeByTag = viewModel.uiState.value.incomeTransactionsByTag

        // Only October transactions
        assertEquals(100.0, incomeByTag["salary"]!!, 0.01)
    }

    @Test
    fun `calculateIncomeTransactionsByTag - Year filters by date range`() = runTest {
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 15), tags = listOf("salary")),
            createTransaction("t2", TransactionType.INCOME, 50.0,
                createDate(2024, 11, 20), tags = listOf("salary"))
        )

        whenever(walletRepository.getUserWallets("user1")).thenReturn(flowOf(emptyList()))
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(userPreferencesRepository.getUserPreferences("user1")).thenReturn(flowOf(null))

        viewModel = ReportViewModel(
            authRepository, walletRepository, transactionRepository,
            userPreferencesRepository, logger
        )
        viewModel.loadPortfolioData()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectDateFilter(DateFilterPeriod.Year(2025))
        testDispatcher.scheduler.advanceUntilIdle()

        val incomeByTag = viewModel.uiState.value.incomeTransactionsByTag

        // Only 2025 transactions
        assertEquals(100.0, incomeByTag["salary"]!!, 0.01)
    }

    @Test
    fun `calculateIncomeTransactionsByTag - excludes null transactionDate`() = runTest {
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 15), tags = listOf("salary")),
            createTransaction("t2", TransactionType.INCOME, 50.0,
                null, tags = listOf("salary"))
        )

        whenever(walletRepository.getUserWallets("user1")).thenReturn(flowOf(emptyList()))
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(userPreferencesRepository.getUserPreferences("user1")).thenReturn(flowOf(null))

        viewModel = ReportViewModel(
            authRepository, walletRepository, transactionRepository,
            userPreferencesRepository, logger
        )
        viewModel.loadPortfolioData()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectDateFilter(DateFilterPeriod.Month(2025, 10))
        testDispatcher.scheduler.advanceUntilIdle()

        val incomeByTag = viewModel.uiState.value.incomeTransactionsByTag

        // Only t1 should be included
        assertEquals(100.0, incomeByTag["salary"]!!, 0.01)
    }

    // ========== Portfolio Reconstruction Tests (Historical Balances) ==========

    @Test
    fun `calculatePortfolioComposition - AllTime uses current wallet balances`() = runTest {
        val wallets = listOf(
            createPhysicalWallet("w1", "Wallet 1", 1000.0, PhysicalForm.FIAT_CURRENCY),
            createPhysicalWallet("w2", "Wallet 2", 500.0, PhysicalForm.CRYPTOCURRENCY)
        )

        whenever(walletRepository.getUserWallets("user1")).thenReturn(flowOf(wallets))
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(emptyList()))
        whenever(userPreferencesRepository.getUserPreferences("user1")).thenReturn(flowOf(null))

        viewModel = ReportViewModel(
            authRepository, walletRepository, transactionRepository,
            userPreferencesRepository, logger
        )
        viewModel.loadPortfolioData()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectDateFilter(DateFilterPeriod.AllTime)
        testDispatcher.scheduler.advanceUntilIdle()

        val composition = viewModel.uiState.value.portfolioComposition

        assertEquals(1000.0, composition[PhysicalForm.FIAT_CURRENCY]!!, 0.01)
        assertEquals(500.0, composition[PhysicalForm.CRYPTOCURRENCY]!!, 0.01)
    }

    @Test
    fun `calculatePortfolioComposition - Month reconstructs balances at month end`() = runTest {
        val wallets = listOf(
            createPhysicalWallet("w1", "Wallet 1", 1000.0, PhysicalForm.FIAT_CURRENCY)
        )
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.INCOME, 200.0,
                createDate(2025, 11, 5), affectedWalletIds = listOf("w1"))
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

        viewModel.selectDateFilter(DateFilterPeriod.Month(2025, 10))
        testDispatcher.scheduler.advanceUntilIdle()

        val composition = viewModel.uiState.value.portfolioComposition

        // Should only include t1, not t2
        assertEquals(100.0, composition[PhysicalForm.FIAT_CURRENCY]!!, 0.01)
    }

    @Test
    fun `calculatePortfolioComposition - Year reconstructs balances at year end`() = runTest {
        val wallets = listOf(
            createPhysicalWallet("w1", "Wallet 1", 1000.0, PhysicalForm.FIAT_CURRENCY)
        )
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.INCOME, 200.0,
                createDate(2026, 1, 5), affectedWalletIds = listOf("w1"))
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

        viewModel.selectDateFilter(DateFilterPeriod.Year(2025))
        testDispatcher.scheduler.advanceUntilIdle()

        val composition = viewModel.uiState.value.portfolioComposition

        // Should only include t1, not t2
        assertEquals(100.0, composition[PhysicalForm.FIAT_CURRENCY]!!, 0.01)
    }

    @Test
    fun `calculatePortfolioComposition - excludes wallets with no transactions before endDate`() = runTest {
        val wallets = listOf(
            createPhysicalWallet("w1", "Wallet 1", 1000.0, PhysicalForm.FIAT_CURRENCY),
            createPhysicalWallet("w2", "Wallet 2", 500.0, PhysicalForm.CRYPTOCURRENCY)
        )
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 11, 15), affectedWalletIds = listOf("w1"))
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

        viewModel.selectDateFilter(DateFilterPeriod.Month(2025, 10))
        testDispatcher.scheduler.advanceUntilIdle()

        val composition = viewModel.uiState.value.portfolioComposition

        // No transactions in October, so composition should be empty
        assertTrue(composition.isEmpty())
    }

    @Test
    fun `calculatePortfolioComposition - includes wallet with transaction before creation`() = runTest {
        val wallets = listOf(
            createPhysicalWallet("w1", "Wallet 1", 1000.0, PhysicalForm.FIAT_CURRENCY)
                .copy(createdAt = createDate(2025, 10, 20))
        )
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1"))
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

        viewModel.selectDateFilter(DateFilterPeriod.Month(2025, 10))
        testDispatcher.scheduler.advanceUntilIdle()

        val composition = viewModel.uiState.value.portfolioComposition

        // Wallet should be included because transaction exists
        assertEquals(100.0, composition[PhysicalForm.FIAT_CURRENCY]!!, 0.01)
    }

    @Test
    fun `calculatePortfolioComposition - excludes zero balance wallets`() = runTest {
        val wallets = listOf(
            createPhysicalWallet("w1", "Wallet 1", 1000.0, PhysicalForm.FIAT_CURRENCY)
        )
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 10), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.EXPENSE, 100.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1"))
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

        viewModel.selectDateFilter(DateFilterPeriod.Month(2025, 10))
        testDispatcher.scheduler.advanceUntilIdle()

        val composition = viewModel.uiState.value.portfolioComposition

        // Balance is 0, should be excluded
        assertTrue(composition.isEmpty())
    }

    @Test
    fun `calculatePortfolioComposition - groups by PhysicalForm correctly`() = runTest {
        val wallets = listOf(
            createPhysicalWallet("w1", "Wallet 1", 1000.0, PhysicalForm.FIAT_CURRENCY),
            createPhysicalWallet("w2", "Wallet 2", 500.0, PhysicalForm.FIAT_CURRENCY),
            createPhysicalWallet("w3", "Wallet 3", 300.0, PhysicalForm.CRYPTOCURRENCY)
        )
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.INCOME, 50.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w2")),
            createTransaction("t3", TransactionType.INCOME, 30.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w3"))
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

        viewModel.selectDateFilter(DateFilterPeriod.Month(2025, 10))
        testDispatcher.scheduler.advanceUntilIdle()

        val composition = viewModel.uiState.value.portfolioComposition

        // FIAT: 100 + 50 = 150
        // CRYPTO: 30
        assertEquals(150.0, composition[PhysicalForm.FIAT_CURRENCY]!!, 0.01)
        assertEquals(30.0, composition[PhysicalForm.CRYPTOCURRENCY]!!, 0.01)
    }

    // ========== Physical Wallet Reconstruction Tests ==========

    @Test
    fun `calculatePhysicalWalletBalances - AllTime uses current balances`() = runTest {
        val wallets = listOf(
            createPhysicalWallet("w1", "Cash", 1000.0),
            createPhysicalWallet("w2", "Bank", 500.0)
        )

        whenever(walletRepository.getUserWallets("user1")).thenReturn(flowOf(wallets))
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(emptyList()))
        whenever(userPreferencesRepository.getUserPreferences("user1")).thenReturn(flowOf(null))

        viewModel = ReportViewModel(
            authRepository, walletRepository, transactionRepository,
            userPreferencesRepository, logger
        )
        viewModel.loadPortfolioData()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectDateFilter(DateFilterPeriod.AllTime)
        testDispatcher.scheduler.advanceUntilIdle()

        val balances = viewModel.uiState.value.physicalWalletBalances

        assertEquals(1000.0, balances["Cash"]!!, 0.01)
        assertEquals(500.0, balances["Bank"]!!, 0.01)
    }

    @Test
    fun `calculatePhysicalWalletBalances - Month reconstructs at month end`() = runTest {
        val wallets = listOf(
            createPhysicalWallet("w1", "Cash", 1000.0)
        )
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.INCOME, 200.0,
                createDate(2025, 11, 5), affectedWalletIds = listOf("w1"))
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

        viewModel.selectDateFilter(DateFilterPeriod.Month(2025, 10))
        testDispatcher.scheduler.advanceUntilIdle()

        val balances = viewModel.uiState.value.physicalWalletBalances

        // Should only include t1, not t2
        assertEquals(100.0, balances["Cash"]!!, 0.01)
    }

    @Test
    fun `calculatePhysicalWalletBalances - excludes zero balances`() = runTest {
        val wallets = listOf(
            createPhysicalWallet("w1", "Cash", 1000.0)
        )
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 10), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.EXPENSE, 100.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1"))
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

        viewModel.selectDateFilter(DateFilterPeriod.Month(2025, 10))
        testDispatcher.scheduler.advanceUntilIdle()

        val balances = viewModel.uiState.value.physicalWalletBalances

        // Balance is 0, should be excluded
        assertTrue(balances.isEmpty())
    }

    @Test
    fun `calculatePhysicalWalletBalances - only includes physical wallets`() = runTest {
        val wallets = listOf(
            createPhysicalWallet("w1", "Cash", 1000.0),
            createLogicalWallet("w2", "Budget", 500.0)
        )
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.INCOME, 50.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w2"))
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

        viewModel.selectDateFilter(DateFilterPeriod.Month(2025, 10))
        testDispatcher.scheduler.advanceUntilIdle()

        val balances = viewModel.uiState.value.physicalWalletBalances

        // Only physical wallet
        assertEquals(1, balances.size)
        assertEquals(100.0, balances["Cash"]!!, 0.01)
    }

    // ========== Logical Wallet Reconstruction Tests ==========

    @Test
    fun `calculateLogicalWalletBalances - AllTime uses current balances`() = runTest {
        val wallets = listOf(
            createLogicalWallet("w1", "Budget 1", 500.0),
            createLogicalWallet("w2", "Budget 2", -100.0)
        )

        whenever(walletRepository.getUserWallets("user1")).thenReturn(flowOf(wallets))
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(emptyList()))
        whenever(userPreferencesRepository.getUserPreferences("user1")).thenReturn(flowOf(null))

        viewModel = ReportViewModel(
            authRepository, walletRepository, transactionRepository,
            userPreferencesRepository, logger
        )
        viewModel.loadPortfolioData()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectDateFilter(DateFilterPeriod.AllTime)
        testDispatcher.scheduler.advanceUntilIdle()

        val balances = viewModel.uiState.value.logicalWalletBalances

        assertEquals(500.0, balances["Budget 1"]!!, 0.01)
        assertEquals(-100.0, balances["Budget 2"]!!, 0.01)
    }

    @Test
    fun `calculateLogicalWalletBalances - Month reconstructs at month end`() = runTest {
        val wallets = listOf(
            createLogicalWallet("w1", "Budget", 500.0)
        )
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.INCOME, 200.0,
                createDate(2025, 11, 5), affectedWalletIds = listOf("w1"))
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

        viewModel.selectDateFilter(DateFilterPeriod.Month(2025, 10))
        testDispatcher.scheduler.advanceUntilIdle()

        val balances = viewModel.uiState.value.logicalWalletBalances

        // Should only include t1, not t2
        assertEquals(100.0, balances["Budget"]!!, 0.01)
    }

    @Test
    fun `calculateLogicalWalletBalances - includes negative balances`() = runTest {
        val wallets = listOf(
            createLogicalWallet("w1", "Budget", 500.0)
        )
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 50.0,
                createDate(2025, 10, 10), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.EXPENSE, 100.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1"))
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

        viewModel.selectDateFilter(DateFilterPeriod.Month(2025, 10))
        testDispatcher.scheduler.advanceUntilIdle()

        val balances = viewModel.uiState.value.logicalWalletBalances

        // 0 + 50 - 100 = -50 (should be included)
        assertEquals(-50.0, balances["Budget"]!!, 0.01)
    }

    @Test
    fun `calculateLogicalWalletBalances - excludes zero balances`() = runTest {
        val wallets = listOf(
            createLogicalWallet("w1", "Budget", 500.0)
        )
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 10), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.EXPENSE, 100.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1"))
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

        viewModel.selectDateFilter(DateFilterPeriod.Month(2025, 10))
        testDispatcher.scheduler.advanceUntilIdle()

        val balances = viewModel.uiState.value.logicalWalletBalances

        // Balance is 0, should be excluded
        assertTrue(balances.isEmpty())
    }

    @Test
    fun `calculateLogicalWalletBalances - only includes logical wallets`() = runTest {
        val wallets = listOf(
            createPhysicalWallet("w1", "Cash", 1000.0),
            createLogicalWallet("w2", "Budget", 500.0)
        )
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.INCOME, 50.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w2"))
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

        viewModel.selectDateFilter(DateFilterPeriod.Month(2025, 10))
        testDispatcher.scheduler.advanceUntilIdle()

        val balances = viewModel.uiState.value.logicalWalletBalances

        // Only logical wallet
        assertEquals(1, balances.size)
        assertEquals(50.0, balances["Budget"]!!, 0.01)
    }

    // ========== Edge Cases ==========

    @Test
    fun `selectDateFilter - handles month with no transactions empty state`() = runTest {
        val wallets = listOf(createPhysicalWallet("w1", "Cash", 1000.0))
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 11, 15), affectedWalletIds = listOf("w1"))
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

        viewModel.selectDateFilter(DateFilterPeriod.Month(2025, 10))
        testDispatcher.scheduler.advanceUntilIdle()

        // All data should be empty
        assertTrue(viewModel.uiState.value.portfolioComposition.isEmpty())
        assertTrue(viewModel.uiState.value.physicalWalletBalances.isEmpty())
        assertTrue(viewModel.uiState.value.expenseTransactionsByTag.isEmpty())
        assertTrue(viewModel.uiState.value.incomeTransactionsByTag.isEmpty())
    }

    @Test
    fun `selectDateFilter - handles year with no transactions empty state`() = runTest {
        val wallets = listOf(createPhysicalWallet("w1", "Cash", 1000.0))
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2024, 11, 15), affectedWalletIds = listOf("w1"))
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

        viewModel.selectDateFilter(DateFilterPeriod.Year(2025))
        testDispatcher.scheduler.advanceUntilIdle()

        // CUMULATIVE filtering: Year(2025) shows balances as of Dec 31, 2025
        // The 2024 transaction should be included (all transactions up to Dec 31, 2025)
        assertEquals(1, viewModel.uiState.value.portfolioComposition.size)
        assertEquals(100.0, viewModel.uiState.value.portfolioComposition[PhysicalForm.FIAT_CURRENCY]!!, 0.01)
        assertEquals(1, viewModel.uiState.value.physicalWalletBalances.size)
        assertEquals(100.0, viewModel.uiState.value.physicalWalletBalances["Cash"]!!, 0.01)
        // Tag-based charts use simple filtering, so they should be empty (no transactions IN 2025)
        assertTrue(viewModel.uiState.value.expenseTransactionsByTag.isEmpty())
        assertTrue(viewModel.uiState.value.incomeTransactionsByTag.isEmpty())
    }

    @Test
    fun `selectDateFilter - handles mixed null and valid transactionDates`() = runTest {
        val wallets = listOf(createPhysicalWallet("w1", "Cash", 1000.0))
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.INCOME, 50.0,
                null, affectedWalletIds = listOf("w1")),
            createTransaction("t3", TransactionType.INCOME, 30.0,
                createDate(2025, 10, 20), affectedWalletIds = listOf("w1"))
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

        viewModel.selectDateFilter(DateFilterPeriod.Month(2025, 10))
        testDispatcher.scheduler.advanceUntilIdle()

        // Should only include t1 and t3, not t2
        val composition = viewModel.uiState.value.portfolioComposition
        assertEquals(130.0, composition[PhysicalForm.FIAT_CURRENCY]!!, 0.01)
    }

    @Test
    fun `loadPortfolioData - handles all wallets created after selected period`() = runTest {
        val wallets = listOf(
            createPhysicalWallet("w1", "Cash", 1000.0)
                .copy(createdAt = createDate(2025, 11, 1))
        )
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 11, 15), affectedWalletIds = listOf("w1"))
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

        viewModel.selectDateFilter(DateFilterPeriod.Month(2025, 10))
        testDispatcher.scheduler.advanceUntilIdle()

        // Wallet has no transactions in October
        assertTrue(viewModel.uiState.value.portfolioComposition.isEmpty())
        assertTrue(viewModel.uiState.value.physicalWalletBalances.isEmpty())
    }

    // ========== Wallet Filter Tests ==========

    @Test
    fun `initial state has walletFilter AllWallets`() {
        val wallets = emptyList<Wallet>()
        val transactions = emptyList<Transaction>()

        whenever(walletRepository.getUserWallets("user1")).thenReturn(flowOf(wallets))
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(userPreferencesRepository.getUserPreferences("user1")).thenReturn(flowOf(null))

        viewModel = ReportViewModel(
            authRepository, walletRepository, transactionRepository,
            userPreferencesRepository, logger
        )

        // Initial state should have AllWallets filter
        assertEquals(com.axeven.profiteerapp.data.model.WalletFilter.AllWallets, viewModel.uiState.value.selectedWalletFilter)
    }

    @Test
    fun `updateWalletFilter with AllWallets updates state`() {
        val wallets = listOf(createPhysicalWallet("w1", "Wallet 1", 1000.0))
        val transactions = emptyList<Transaction>()

        whenever(walletRepository.getUserWallets("user1")).thenReturn(flowOf(wallets))
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(userPreferencesRepository.getUserPreferences("user1")).thenReturn(flowOf(null))

        viewModel = ReportViewModel(
            authRepository, walletRepository, transactionRepository,
            userPreferencesRepository, logger
        )
        viewModel.loadPortfolioData()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectWalletFilter(com.axeven.profiteerapp.data.model.WalletFilter.AllWallets)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(com.axeven.profiteerapp.data.model.WalletFilter.AllWallets, viewModel.uiState.value.selectedWalletFilter)
    }

    @Test
    fun `updateWalletFilter with SpecificWallet updates state`() {
        val wallets = listOf(
            createPhysicalWallet("w1", "Wallet 1", 1000.0),
            createPhysicalWallet("w2", "Wallet 2", 500.0)
        )
        val transactions = emptyList<Transaction>()

        whenever(walletRepository.getUserWallets("user1")).thenReturn(flowOf(wallets))
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(userPreferencesRepository.getUserPreferences("user1")).thenReturn(flowOf(null))

        viewModel = ReportViewModel(
            authRepository, walletRepository, transactionRepository,
            userPreferencesRepository, logger
        )
        viewModel.loadPortfolioData()
        testDispatcher.scheduler.advanceUntilIdle()

        val specificFilter = com.axeven.profiteerapp.data.model.WalletFilter.SpecificWallet("w2", "Wallet 2")
        viewModel.selectWalletFilter(specificFilter)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(specificFilter, viewModel.uiState.value.selectedWalletFilter)
    }

    @Test
    fun `wallet filter persists when switching chart types`() {
        val wallets = listOf(createPhysicalWallet("w1", "Wallet 1", 1000.0))
        val transactions = emptyList<Transaction>()

        whenever(walletRepository.getUserWallets("user1")).thenReturn(flowOf(wallets))
        whenever(transactionRepository.getUserTransactionsForCalculations("user1")).thenReturn(flowOf(transactions))
        whenever(userPreferencesRepository.getUserPreferences("user1")).thenReturn(flowOf(null))

        viewModel = ReportViewModel(
            authRepository, walletRepository, transactionRepository,
            userPreferencesRepository, logger
        )
        viewModel.loadPortfolioData()
        testDispatcher.scheduler.advanceUntilIdle()

        val specificFilter = com.axeven.profiteerapp.data.model.WalletFilter.SpecificWallet("w1", "Wallet 1")
        viewModel.selectWalletFilter(specificFilter)
        testDispatcher.scheduler.advanceUntilIdle()

        // Switch chart types
        viewModel.selectChartDataType(ChartDataType.PHYSICAL_WALLET_BALANCE)
        viewModel.selectChartDataType(ChartDataType.EXPENSE_TRANSACTION_BY_TAG)
        viewModel.selectChartDataType(ChartDataType.PORTFOLIO_ASSET_COMPOSITION)

        // Filter should persist
        assertEquals(specificFilter, viewModel.uiState.value.selectedWalletFilter)
    }

    @Test
    fun `combined date and wallet filtering in portfolio chart data`() {
        val wallets = listOf(
            createPhysicalWallet("w1", "Cash", 0.0).copy(initialBalance = 0.0, createdAt = createDate(2025, 1, 1)),
            createPhysicalWallet("w2", "Bank", 0.0).copy(initialBalance = 0.0, createdAt = createDate(2025, 1, 1))
        )
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.INCOME, 200.0,
                createDate(2025, 10, 20), affectedWalletIds = listOf("w2")),
            createTransaction("t3", TransactionType.INCOME, 300.0,
                createDate(2025, 11, 5), affectedWalletIds = listOf("w1")) // November
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

        // Apply date filter (October only)
        viewModel.selectDateFilter(DateFilterPeriod.Month(2025, 10))
        testDispatcher.scheduler.advanceUntilIdle()

        // Apply wallet filter (w1 only)
        viewModel.selectWalletFilter(com.axeven.profiteerapp.data.model.WalletFilter.SpecificWallet("w1", "Cash"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Should only show w1's balance from October (100)
        val physicalBalances = viewModel.uiState.value.physicalWalletBalances
        assertEquals(1, physicalBalances.size)
        assertEquals(100.0, physicalBalances["Cash"]!!, 0.01)
    }

    @Test
    fun `combined date and wallet filtering in tag chart data`() {
        val wallets = listOf(
            createPhysicalWallet("w1", "Cash", 0.0),
            createPhysicalWallet("w2", "Bank", 0.0)
        )
        val transactions = listOf(
            createTransaction("t1", TransactionType.EXPENSE, 50.0,
                createDate(2025, 10, 15), tags = listOf("food"), affectedWalletIds = listOf("w1")),
            createTransaction("t2", TransactionType.EXPENSE, 100.0,
                createDate(2025, 10, 20), tags = listOf("food"), affectedWalletIds = listOf("w2")),
            createTransaction("t3", TransactionType.EXPENSE, 75.0,
                createDate(2025, 11, 5), tags = listOf("food"), affectedWalletIds = listOf("w1"))
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

        // Apply date filter (October only)
        viewModel.selectDateFilter(DateFilterPeriod.Month(2025, 10))
        testDispatcher.scheduler.advanceUntilIdle()

        // Apply wallet filter (w1 only)
        viewModel.selectWalletFilter(com.axeven.profiteerapp.data.model.WalletFilter.SpecificWallet("w1", "Cash"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Should only show w1's expenses from October (50)
        val expensesByTag = viewModel.uiState.value.expenseTransactionsByTag
        assertEquals(1, expensesByTag.size)
        assertEquals(50.0, expensesByTag["food"]!!, 0.01)
    }

    @Test
    fun `wallet filter with empty wallet list returns empty data`() {
        val wallets = emptyList<Wallet>()
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1"))
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

        viewModel.selectWalletFilter(com.axeven.profiteerapp.data.model.WalletFilter.SpecificWallet("w1", "Wallet 1"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Empty wallets should result in empty data
        assertTrue(viewModel.uiState.value.portfolioComposition.isEmpty())
        assertTrue(viewModel.uiState.value.physicalWalletBalances.isEmpty())
    }

    @Test
    fun `wallet filter with wallet not in list returns empty data`() {
        val wallets = listOf(
            createPhysicalWallet("w1", "Wallet 1", 1000.0)
        )
        val transactions = listOf(
            createTransaction("t1", TransactionType.INCOME, 100.0,
                createDate(2025, 10, 15), affectedWalletIds = listOf("w1"))
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

        // Filter by wallet that doesn't exist
        viewModel.selectWalletFilter(com.axeven.profiteerapp.data.model.WalletFilter.SpecificWallet("w999", "Non-existent"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Should return empty data
        assertTrue(viewModel.uiState.value.portfolioComposition.isEmpty())
        assertTrue(viewModel.uiState.value.physicalWalletBalances.isEmpty())
    }
}
