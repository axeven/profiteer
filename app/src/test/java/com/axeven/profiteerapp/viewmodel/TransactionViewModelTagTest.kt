package com.axeven.profiteerapp.viewmodel

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

/**
 * Test-Driven Development tests for TransactionViewModel tag normalization (Phase 3).
 *
 * Following TDD methodology - these tests define the expected behavior for tag collection
 * and autocomplete functionality with normalized tags.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModelTagTest {

    private lateinit var viewModel: TransactionViewModel
    private lateinit var authRepository: AuthRepository
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var walletRepository: WalletRepository
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var logger: Logger
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock dependencies
        authRepository = mock()
        transactionRepository = mock()
        walletRepository = mock()
        userPreferencesRepository = mock()
        logger = mock()

        // Setup default behavior
        whenever(authRepository.getCurrentUserId()).thenReturn("test-user-id")
        whenever(walletRepository.getUserWallets(any())).thenReturn(flowOf(emptyList()))
        whenever(userPreferencesRepository.getUserPreferences(any())).thenReturn(flowOf(null))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========================================
    // Tag Collection Tests
    // ========================================

    @Test
    fun `availableTags should deduplicate tags case-insensitively`() = runTest {
        // Arrange: Transactions with mixed-case duplicate tags
        val transactions = listOf(
            Transaction(id = "1", title = "T1", tags = listOf("food", "Food", "FOOD")),
            Transaction(id = "2", title = "T2", tags = listOf("travel", "TRAVEL"))
        )
        whenever(transactionRepository.getUserTransactions(any())).thenReturn(flowOf(transactions))

        // Act
        viewModel = TransactionViewModel(
            authRepository,
            transactionRepository,
            walletRepository,
            userPreferencesRepository,
            logger
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Should have only unique normalized tags
        val availableTags = viewModel.uiState.value.availableTags
        assertEquals(listOf("food", "travel"), availableTags)
    }

    @Test
    fun `availableTags should trim whitespace from tags`() = runTest {
        // Arrange: Transactions with whitespace in tags
        val transactions = listOf(
            Transaction(id = "1", title = "T1", tags = listOf("  food  ", " travel ")),
            Transaction(id = "2", title = "T2", tags = listOf("shopping"))
        )
        whenever(transactionRepository.getUserTransactions(any())).thenReturn(flowOf(transactions))

        // Act
        viewModel = TransactionViewModel(
            authRepository,
            transactionRepository,
            walletRepository,
            userPreferencesRepository,
            logger
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Tags should be trimmed
        val availableTags = viewModel.uiState.value.availableTags
        assertEquals(listOf("food", "shopping", "travel"), availableTags)
    }

    @Test
    fun `availableTags should filter out Untagged keyword case-insensitively`() = runTest {
        // Arrange: Transactions with various forms of "Untagged"
        val transactions = listOf(
            Transaction(id = "1", title = "T1", tags = listOf("food", "Untagged", "travel")),
            Transaction(id = "2", title = "T2", tags = listOf("shopping", "untagged", "UNTAGGED"))
        )
        whenever(transactionRepository.getUserTransactions(any())).thenReturn(flowOf(transactions))

        // Act
        viewModel = TransactionViewModel(
            authRepository,
            transactionRepository,
            walletRepository,
            userPreferencesRepository,
            logger
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: "Untagged" should be filtered out
        val availableTags = viewModel.uiState.value.availableTags
        assertEquals(listOf("food", "shopping", "travel"), availableTags)
    }

    @Test
    fun `availableTags should filter out blank tags`() = runTest {
        // Arrange: Transactions with blank tags
        val transactions = listOf(
            Transaction(id = "1", title = "T1", tags = listOf("food", "", "   ", "travel")),
            Transaction(id = "2", title = "T2", tags = listOf("shopping", ""))
        )
        whenever(transactionRepository.getUserTransactions(any())).thenReturn(flowOf(transactions))

        // Act
        viewModel = TransactionViewModel(
            authRepository,
            transactionRepository,
            walletRepository,
            userPreferencesRepository,
            logger
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Blank tags should be filtered out
        val availableTags = viewModel.uiState.value.availableTags
        assertEquals(listOf("food", "shopping", "travel"), availableTags)
    }

    @Test
    fun `availableTags should be sorted alphabetically`() = runTest {
        // Arrange: Transactions with unsorted tags
        val transactions = listOf(
            Transaction(id = "1", title = "T1", tags = listOf("zebra", "apple", "mango")),
            Transaction(id = "2", title = "T2", tags = listOf("banana"))
        )
        whenever(transactionRepository.getUserTransactions(any())).thenReturn(flowOf(transactions))

        // Act
        viewModel = TransactionViewModel(
            authRepository,
            transactionRepository,
            walletRepository,
            userPreferencesRepository,
            logger
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Tags should be sorted
        val availableTags = viewModel.uiState.value.availableTags
        assertEquals(listOf("apple", "banana", "mango", "zebra"), availableTags)
    }

    @Test
    fun `availableTags complex real-world scenario`() = runTest {
        // Arrange: Mix of all edge cases
        val transactions = listOf(
            Transaction(id = "1", title = "T1", tags = listOf("Food", "food", "FOOD", "  travel  ")),
            Transaction(id = "2", title = "T2", tags = listOf("Shopping", "Untagged", "", "travel")),
            Transaction(id = "3", title = "T3", tags = listOf("entertainment", "SHOPPING"))
        )
        whenever(transactionRepository.getUserTransactions(any())).thenReturn(flowOf(transactions))

        // Act
        viewModel = TransactionViewModel(
            authRepository,
            transactionRepository,
            walletRepository,
            userPreferencesRepository,
            logger
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Should have normalized, deduplicated, sorted tags
        val availableTags = viewModel.uiState.value.availableTags
        assertEquals(listOf("entertainment", "food", "shopping", "travel"), availableTags)
    }

    // ========================================
    // Tag Autocomplete Tests
    // ========================================

    @Test
    fun `getTagSuggestions should match case-insensitively`() = runTest {
        // Arrange
        val transactions = listOf(
            Transaction(id = "1", title = "T1", tags = listOf("food", "travel", "shopping"))
        )
        whenever(transactionRepository.getUserTransactions(any())).thenReturn(flowOf(transactions))

        viewModel = TransactionViewModel(
            authRepository,
            transactionRepository,
            walletRepository,
            userPreferencesRepository,
            logger
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Act: Search with different cases
        val suggestionsLower = viewModel.getTagSuggestions("foo")
        val suggestionsUpper = viewModel.getTagSuggestions("FOO")
        val suggestionsMixed = viewModel.getTagSuggestions("FoO")

        // Assert: All should match "food"
        assertEquals(listOf("food"), suggestionsLower)
        assertEquals(listOf("food"), suggestionsUpper)
        assertEquals(listOf("food"), suggestionsMixed)
    }

    @Test
    fun `getTagSuggestions should suggest normalized tags`() = runTest {
        // Arrange: Tags stored with mixed case (simulating legacy data)
        val transactions = listOf(
            Transaction(id = "1", title = "T1", tags = listOf("Food", "TRAVEL", "Shopping"))
        )
        whenever(transactionRepository.getUserTransactions(any())).thenReturn(flowOf(transactions))

        viewModel = TransactionViewModel(
            authRepository,
            transactionRepository,
            walletRepository,
            userPreferencesRepository,
            logger
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        val suggestions = viewModel.getTagSuggestions("sho")

        // Assert: Should suggest normalized version
        assertEquals(listOf("shopping"), suggestions)
    }

    @Test
    fun `getTagSuggestions should not suggest Untagged`() = runTest {
        // Arrange
        val transactions = listOf(
            Transaction(id = "1", title = "T1", tags = listOf("food", "Untagged", "travel"))
        )
        whenever(transactionRepository.getUserTransactions(any())).thenReturn(flowOf(transactions))

        viewModel = TransactionViewModel(
            authRepository,
            transactionRepository,
            walletRepository,
            userPreferencesRepository,
            logger
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        val suggestions = viewModel.getTagSuggestions("unt")

        // Assert: Should not suggest "Untagged"
        assertTrue(suggestions.isEmpty())
    }

    @Test
    fun `getTagSuggestions should respect minimum character limit`() = runTest {
        // Arrange
        val transactions = listOf(
            Transaction(id = "1", title = "T1", tags = listOf("food", "travel"))
        )
        whenever(transactionRepository.getUserTransactions(any())).thenReturn(flowOf(transactions))

        viewModel = TransactionViewModel(
            authRepository,
            transactionRepository,
            walletRepository,
            userPreferencesRepository,
            logger
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Act: Input less than minimum characters (typically 3)
        val suggestionsEmpty = viewModel.getTagSuggestions("")
        val suggestionsTwoChars = viewModel.getTagSuggestions("fo")

        // Assert: Should return empty for inputs below threshold
        assertTrue(suggestionsEmpty.isEmpty())
        assertTrue(suggestionsTwoChars.isEmpty())
    }

    @Test
    fun `getTagSuggestions should limit number of suggestions`() = runTest {
        // Arrange: Many tags starting with 'f'
        val transactions = listOf(
            Transaction(id = "1", title = "T1", tags = listOf(
                "food1", "food2", "food3", "food4", "food5",
                "food6", "food7", "food8", "food9", "food10"
            ))
        )
        whenever(transactionRepository.getUserTransactions(any())).thenReturn(flowOf(transactions))

        viewModel = TransactionViewModel(
            authRepository,
            transactionRepository,
            walletRepository,
            userPreferencesRepository,
            logger
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        val suggestions = viewModel.getTagSuggestions("foo")

        // Assert: Should limit to reasonable number (typically 5)
        assertTrue(suggestions.size <= 5)
    }

    @Test
    fun `getTagSuggestions should handle partial matches`() = runTest {
        // Arrange
        val transactions = listOf(
            Transaction(id = "1", title = "T1", tags = listOf("food", "football", "foot massage"))
        )
        whenever(transactionRepository.getUserTransactions(any())).thenReturn(flowOf(transactions))

        viewModel = TransactionViewModel(
            authRepository,
            transactionRepository,
            walletRepository,
            userPreferencesRepository,
            logger
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        val suggestions = viewModel.getTagSuggestions("foo")

        // Assert: Should return all matching tags
        assertEquals(3, suggestions.size)
        assertTrue(suggestions.containsAll(listOf("food", "football", "foot massage")))
    }
}
