package com.axeven.profiteerapp.ui.transaction

import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.data.ui.*
import org.junit.Test
import org.junit.Assert.*
import java.util.*

/**
 * Test-Driven Development tests for TransactionFormValidator.
 *
 * Following TDD methodology - these tests define the expected behavior
 * for form validation logic. They will initially fail (RED phase)
 * and will be made to pass during implementation (GREEN phase).
 */
class TransactionFormValidatorTest {

    // Mock data for testing
    private val mockPhysicalWallet = Wallet(
        id = "physical-1",
        name = "Main Account",
        walletType = "Physical",
        balance = 1000.0
    )

    private val mockLogicalWallet = Wallet(
        id = "logical-1",
        name = "Savings Goal",
        walletType = "Logical",
        balance = 500.0
    )

    private val mockPhysicalWallet2 = Wallet(
        id = "physical-2",
        name = "Backup Account",
        walletType = "Physical",
        balance = 200.0
    )

    private val mockLogicalWallet2 = Wallet(
        id = "logical-2",
        name = "Another Goal",
        walletType = "Logical",
        balance = 300.0
    )

    // Title validation tests
    @Test
    fun `validateTitle should accept valid title`() {
        val result = validateTitle("Valid Transaction Title")

        assertNull(result)
    }

    @Test
    fun `validateTitle should reject empty title`() {
        val result = validateTitle("")

        assertEquals("Title is required", result)
    }

    @Test
    fun `validateTitle should reject blank title`() {
        val result = validateTitle("   ")

        assertEquals("Title is required", result)
    }

    @Test
    fun `validateTitle should reject title over 100 characters`() {
        val longTitle = "a".repeat(101)

        val result = validateTitle(longTitle)

        assertEquals("Title must be less than 100 characters", result)
    }

    @Test
    fun `validateTitle should accept title with exactly 100 characters`() {
        val exactTitle = "a".repeat(100)

        val result = validateTitle(exactTitle)

        assertNull(result)
    }

    // Amount validation tests
    @Test
    fun `validateAmount should accept valid positive amount`() {
        val result = validateAmount("100.50")

        assertNull(result)
    }

    @Test
    fun `validateAmount should accept valid integer amount`() {
        val result = validateAmount("100")

        assertNull(result)
    }

    @Test
    fun `validateAmount should reject empty amount`() {
        val result = validateAmount("")

        assertEquals("Amount is required", result)
    }

    @Test
    fun `validateAmount should reject blank amount`() {
        val result = validateAmount("   ")

        assertEquals("Amount is required", result)
    }

    @Test
    fun `validateAmount should reject non-numeric amount`() {
        val result = validateAmount("invalid")

        assertEquals("Amount must be a valid number", result)
    }

    @Test
    fun `validateAmount should reject negative amount`() {
        val result = validateAmount("-50.00")

        assertEquals("Amount must be greater than 0", result)
    }

    @Test
    fun `validateAmount should reject zero amount`() {
        val result = validateAmount("0")

        assertEquals("Amount must be greater than 0", result)
    }

    @Test
    fun `validateAmount should reject amount with invalid decimal format`() {
        val result = validateAmount("100.5.0")

        assertEquals("Amount must be a valid number", result)
    }

    // Wallet validation tests for regular transactions
    @Test
    fun `validateWalletSelection should accept valid wallet selection for expense`() {
        val wallets = SelectedWallets(physical = mockPhysicalWallet)

        val result = validateWalletSelection(wallets, TransactionType.EXPENSE)

        assertNull(result)
    }

    @Test
    fun `validateWalletSelection should accept both wallets for expense`() {
        val wallets = SelectedWallets(
            physical = mockPhysicalWallet,
            logical = mockLogicalWallet
        )

        val result = validateWalletSelection(wallets, TransactionType.EXPENSE)

        assertNull(result)
    }

    @Test
    fun `validateWalletSelection should reject no wallets for expense`() {
        val wallets = SelectedWallets()

        val result = validateWalletSelection(wallets, TransactionType.EXPENSE)

        assertEquals("At least one wallet must be selected", result)
    }

    @Test
    fun `validateWalletSelection should accept valid wallet selection for income`() {
        val wallets = SelectedWallets(logical = mockLogicalWallet)

        val result = validateWalletSelection(wallets, TransactionType.INCOME)

        assertNull(result)
    }

    @Test
    fun `validateWalletSelection should reject no wallets for income`() {
        val wallets = SelectedWallets()

        val result = validateWalletSelection(wallets, TransactionType.INCOME)

        assertEquals("At least one wallet must be selected", result)
    }

    // Transfer validation tests
    @Test
    fun `validateTransfer should accept valid physical wallet transfer`() {
        val wallets = SelectedWallets(
            source = mockPhysicalWallet,
            destination = mockPhysicalWallet2
        )

        val result = validateTransfer(wallets)

        assertNull(result)
    }

    @Test
    fun `validateTransfer should accept valid logical wallet transfer`() {
        val wallets = SelectedWallets(
            source = mockLogicalWallet,
            destination = mockLogicalWallet2
        )

        val result = validateTransfer(wallets)

        assertNull(result)
    }

    @Test
    fun `validateTransfer should reject missing source wallet`() {
        val wallets = SelectedWallets(destination = mockPhysicalWallet)

        val result = validateTransfer(wallets)

        assertEquals("Source wallet is required for transfers", result)
    }

    @Test
    fun `validateTransfer should reject missing destination wallet`() {
        val wallets = SelectedWallets(source = mockPhysicalWallet)

        val result = validateTransfer(wallets)

        assertEquals("Destination wallet is required for transfers", result)
    }

    @Test
    fun `validateTransfer should reject same source and destination wallet`() {
        val wallets = SelectedWallets(
            source = mockPhysicalWallet,
            destination = mockPhysicalWallet
        )

        val result = validateTransfer(wallets)

        assertEquals("Source and destination wallets must be different", result)
    }

    @Test
    fun `validateTransfer should reject different wallet types`() {
        val wallets = SelectedWallets(
            source = mockPhysicalWallet,
            destination = mockLogicalWallet
        )

        val result = validateTransfer(wallets)

        assertEquals("Source and destination wallets must be the same type", result)
    }

    // Comprehensive form validation tests
    @Test
    fun `validateTransactionForm should accept valid expense transaction`() {
        val state = CreateTransactionUiState(
            title = "Valid Expense",
            amount = "100.00",
            selectedType = TransactionType.EXPENSE,
            selectedWallets = SelectedWallets(physical = mockPhysicalWallet)
        )

        val validation = validateTransactionForm(state)

        assertTrue(validation.isValid)
        assertFalse(validation.errors.hasErrors)
    }

    @Test
    fun `validateTransactionForm should accept valid income transaction`() {
        val state = CreateTransactionUiState(
            title = "Valid Income",
            amount = "500.00",
            selectedType = TransactionType.INCOME,
            selectedWallets = SelectedWallets(logical = mockLogicalWallet)
        )

        val validation = validateTransactionForm(state)

        assertTrue(validation.isValid)
        assertFalse(validation.errors.hasErrors)
    }

    @Test
    fun `validateTransactionForm should accept valid transfer transaction`() {
        val state = CreateTransactionUiState(
            title = "Valid Transfer",
            amount = "200.00",
            selectedType = TransactionType.TRANSFER,
            selectedWallets = SelectedWallets(
                source = mockPhysicalWallet,
                destination = mockPhysicalWallet2
            )
        )

        val validation = validateTransactionForm(state)

        assertTrue(validation.isValid)
        assertFalse(validation.errors.hasErrors)
    }

    @Test
    fun `validateTransactionForm should reject empty title`() {
        val state = CreateTransactionUiState(
            title = "",
            amount = "100.00",
            selectedType = TransactionType.EXPENSE,
            selectedWallets = SelectedWallets(physical = mockPhysicalWallet)
        )

        val validation = validateTransactionForm(state)

        assertFalse(validation.isValid)
        assertTrue(validation.errors.hasErrors)
        assertEquals("Title is required", validation.errors.titleError)
    }

    @Test
    fun `validateTransactionForm should reject invalid amount`() {
        val state = CreateTransactionUiState(
            title = "Valid Title",
            amount = "invalid",
            selectedType = TransactionType.EXPENSE,
            selectedWallets = SelectedWallets(physical = mockPhysicalWallet)
        )

        val validation = validateTransactionForm(state)

        assertFalse(validation.isValid)
        assertTrue(validation.errors.hasErrors)
        assertEquals("Amount must be a valid number", validation.errors.amountError)
    }

    @Test
    fun `validateTransactionForm should reject missing wallets for expense`() {
        val state = CreateTransactionUiState(
            title = "Valid Title",
            amount = "100.00",
            selectedType = TransactionType.EXPENSE,
            selectedWallets = SelectedWallets()
        )

        val validation = validateTransactionForm(state)

        assertFalse(validation.isValid)
        assertTrue(validation.errors.hasErrors)
        assertEquals("At least one wallet must be selected", validation.errors.walletError)
    }

    @Test
    fun `validateTransactionForm should reject invalid transfer setup`() {
        val state = CreateTransactionUiState(
            title = "Valid Title",
            amount = "100.00",
            selectedType = TransactionType.TRANSFER,
            selectedWallets = SelectedWallets(source = mockPhysicalWallet)
        )

        val validation = validateTransactionForm(state)

        assertFalse(validation.isValid)
        assertTrue(validation.errors.hasErrors)
        assertEquals("Destination wallet is required for transfers", validation.errors.transferError)
    }

    @Test
    fun `validateTransactionForm should handle multiple validation errors`() {
        val state = CreateTransactionUiState(
            title = "",
            amount = "invalid",
            selectedType = TransactionType.EXPENSE,
            selectedWallets = SelectedWallets()
        )

        val validation = validateTransactionForm(state)

        assertFalse(validation.isValid)
        assertTrue(validation.errors.hasErrors)
        assertEquals("Title is required", validation.errors.titleError)
        assertEquals("Amount must be a valid number", validation.errors.amountError)
        assertEquals("At least one wallet must be selected", validation.errors.walletError)
    }

    // Date validation tests
    @Test
    fun `validateDate should accept current date`() {
        val currentDate = Date()

        val result = validateDate(currentDate)

        assertNull(result)
    }

    @Test
    fun `validateDate should accept past date`() {
        val pastDate = Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000) // Yesterday

        val result = validateDate(pastDate)

        assertNull(result)
    }

    @Test
    fun `validateDate should warn about future date`() {
        val futureDate = Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000) // Tomorrow

        val result = validateDate(futureDate)

        assertEquals("Future dates may affect accurate financial tracking", result)
    }

    @Test
    fun `validateDate should warn about dates more than 1 year in the future`() {
        val farFutureDate = Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000L) // 1 year from now

        val result = validateDate(farFutureDate)

        assertEquals("Future dates may affect accurate financial tracking", result)
    }

    // Tags validation tests
    @Test
    fun `validateTags should accept valid tags`() {
        val result = validateTags("food, grocery, household")

        assertNull(result)
    }

    @Test
    fun `validateTags should accept empty tags`() {
        val result = validateTags("")

        assertNull(result)
    }

    @Test
    fun `validateTags should accept single tag`() {
        val result = validateTags("food")

        assertNull(result)
    }

    @Test
    fun `validateTags should warn about very long tag list`() {
        val longTagList = (1..20).joinToString(", ") { "tag$it" }

        val result = validateTags(longTagList)

        assertEquals("Consider using fewer tags for better organization", result)
    }

    @Test
    fun `validateTags should warn about duplicate tags`() {
        val duplicateTags = "food, grocery, food, household"

        val result = validateTags(duplicateTags)

        assertEquals("Duplicate tags detected: food", result)
    }

    // Edge case tests
    @Test
    fun `should handle extremely large amounts`() {
        val result = validateAmount("999999999999.99")

        assertNull(result) // Should be valid unless business rules specify limits
    }

    @Test
    fun `should handle very small decimal amounts`() {
        val result = validateAmount("0.01")

        assertNull(result)
    }

    @Test
    fun `should handle international number formats`() {
        // Note: This test might need adjustment based on locale handling
        val result = validateAmount("1,000.50")

        // For now, this should be invalid since we expect clean decimal format
        assertEquals("Amount must be a valid number", result)
    }

    @Test
    fun `should validate maximum title length boundary`() {
        val maxLengthTitle = "a".repeat(100)
        val overLengthTitle = "a".repeat(101)

        val validResult = validateTitle(maxLengthTitle)
        val invalidResult = validateTitle(overLengthTitle)

        assertNull(validResult)
        assertEquals("Title must be less than 100 characters", invalidResult)
    }

    // Performance validation tests
    @Test
    fun `validateTransactionForm should complete quickly for valid state`() {
        val state = CreateTransactionUiState(
            title = "Performance Test",
            amount = "100.00",
            selectedType = TransactionType.EXPENSE,
            selectedWallets = SelectedWallets(physical = mockPhysicalWallet)
        )

        val startTime = System.currentTimeMillis()
        val validation = validateTransactionForm(state)
        val endTime = System.currentTimeMillis()

        assertTrue(validation.isValid)
        assertTrue("Validation should complete in under 100ms", (endTime - startTime) < 100)
    }

    @Test
    fun `validateTransactionForm should complete quickly for invalid state`() {
        val state = CreateTransactionUiState(
            title = "",
            amount = "invalid",
            selectedType = TransactionType.TRANSFER,
            selectedWallets = SelectedWallets()
        )

        val startTime = System.currentTimeMillis()
        val validation = validateTransactionForm(state)
        val endTime = System.currentTimeMillis()

        assertFalse(validation.isValid)
        assertTrue("Validation should complete in under 100ms", (endTime - startTime) < 100)
    }
}