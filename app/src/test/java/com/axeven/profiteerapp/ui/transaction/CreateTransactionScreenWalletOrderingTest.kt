package com.axeven.profiteerapp.ui.transaction

import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.utils.WalletSortingUtils
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for wallet dropdown ordering in CreateTransactionScreen.
 *
 * Following TDD approach - tests written BEFORE implementation (RED phase).
 * These tests verify that wallet dropdowns display wallets in alphabetical order.
 *
 * CreateTransactionScreen has 4 wallet selection scenarios:
 * 1. Physical wallet dropdown (for Income/Expense)
 * 2. Logical wallet dropdown (for Income/Expense)
 * 3. Transfer source wallet dropdown (all wallets, grouped by type)
 * 4. Transfer destination wallet dropdown (all except source, grouped by type)
 *
 * All scenarios should display wallets alphabetically (or grouped then alphabetically).
 */
class CreateTransactionScreenWalletOrderingTest {

    // ========================================
    // Helper Functions
    // ========================================

    private fun createWallet(
        name: String,
        walletType: String = "Physical",
        id: String = "test-id-${name.hashCode()}"
    ): Wallet {
        return Wallet(
            id = id,
            name = name,
            walletType = walletType,
            balance = 100.0,
            userId = "test-user"
        )
    }

    // ========================================
    // Physical Wallet Dropdown Tests
    // ========================================

    @Test
    fun `physical wallet dropdown displays alphabetically`() {
        // Given: ViewModel with unsorted Physical wallets
        val allWallets = listOf(
            createWallet("Zebra Physical", walletType = "Physical", id = "1"),
            createWallet("Apple Physical", walletType = "Physical", id = "2"),
            createWallet("Mango Physical", walletType = "Physical", id = "3"),
            createWallet("Blue Logical", walletType = "Logical", id = "4") // Should be filtered out
        )

        // When: Filter Physical wallets and sort (simulates CreateTransactionScreen logic)
        val physicalWallets = WalletSortingUtils.sortAlphabetically(
            allWallets.filter { it.walletType == "Physical" }
        )

        // Then: Physical wallets are displayed alphabetically
        assertEquals(3, physicalWallets.size)
        assertEquals("Apple Physical", physicalWallets[0].name)
        assertEquals("Mango Physical", physicalWallets[1].name)
        assertEquals("Zebra Physical", physicalWallets[2].name)
    }

    @Test
    fun `physical wallet dropdown is case insensitive`() {
        // Given: Physical wallets with mixed case names
        val allWallets = listOf(
            createWallet("zebra bank", walletType = "Physical", id = "1"),
            createWallet("Apple Pay", walletType = "Physical", id = "2"),
            createWallet("CASH", walletType = "Physical", id = "3")
        )

        // When: Filter and sort Physical wallets
        val physicalWallets = WalletSortingUtils.sortAlphabetically(
            allWallets.filter { it.walletType == "Physical" }
        )

        // Then: Sorted case-insensitively
        assertEquals("Apple Pay", physicalWallets[0].name)
        assertEquals("CASH", physicalWallets[1].name)
        assertEquals("zebra bank", physicalWallets[2].name)
    }

    @Test
    fun `physical wallet dropdown with single wallet`() {
        // Given: Only one Physical wallet
        val allWallets = listOf(
            createWallet("Cash", walletType = "Physical", id = "1")
        )

        // When: Filter and sort Physical wallets
        val physicalWallets = WalletSortingUtils.sortAlphabetically(
            allWallets.filter { it.walletType == "Physical" }
        )

        // Then: Single wallet is displayed
        assertEquals(1, physicalWallets.size)
        assertEquals("Cash", physicalWallets[0].name)
    }

    @Test
    fun `physical wallet dropdown with empty list`() {
        // Given: No Physical wallets (all Logical)
        val allWallets = listOf(
            createWallet("Blue Fund", walletType = "Logical", id = "1"),
            createWallet("Red Budget", walletType = "Logical", id = "2")
        )

        // When: Filter and sort Physical wallets
        val physicalWallets = WalletSortingUtils.sortAlphabetically(
            allWallets.filter { it.walletType == "Physical" }
        )

        // Then: Empty list is returned
        assertEquals(0, physicalWallets.size)
    }

    // ========================================
    // Logical Wallet Dropdown Tests
    // ========================================

    @Test
    fun `logical wallet dropdown displays alphabetically`() {
        // Given: ViewModel with unsorted Logical wallets
        val allWallets = listOf(
            createWallet("Zebra Physical", walletType = "Physical", id = "1"), // Should be filtered out
            createWallet("Yellow Logical", walletType = "Logical", id = "2"),
            createWallet("Blue Logical", walletType = "Logical", id = "3"),
            createWallet("Red Logical", walletType = "Logical", id = "4")
        )

        // When: Filter Logical wallets and sort (simulates CreateTransactionScreen logic)
        val logicalWallets = WalletSortingUtils.sortAlphabetically(
            allWallets.filter { it.walletType == "Logical" }
        )

        // Then: Logical wallets are displayed alphabetically
        assertEquals(3, logicalWallets.size)
        assertEquals("Blue Logical", logicalWallets[0].name)
        assertEquals("Red Logical", logicalWallets[1].name)
        assertEquals("Yellow Logical", logicalWallets[2].name)
    }

    @Test
    fun `logical wallet dropdown is case insensitive`() {
        // Given: Logical wallets with mixed case names
        val allWallets = listOf(
            createWallet("yellow fund", walletType = "Logical", id = "1"),
            createWallet("Blue Savings", walletType = "Logical", id = "2"),
            createWallet("RED BUDGET", walletType = "Logical", id = "3")
        )

        // When: Filter and sort Logical wallets
        val logicalWallets = WalletSortingUtils.sortAlphabetically(
            allWallets.filter { it.walletType == "Logical" }
        )

        // Then: Sorted case-insensitively
        assertEquals("Blue Savings", logicalWallets[0].name)
        assertEquals("RED BUDGET", logicalWallets[1].name)
        assertEquals("yellow fund", logicalWallets[2].name)
    }

    @Test
    fun `logical wallet dropdown with special characters`() {
        // Given: Logical wallets with special characters
        val allWallets = listOf(
            createWallet("Zoo Fund", walletType = "Logical", id = "1"),
            createWallet("!Priority Budget", walletType = "Logical", id = "2"),
            createWallet("Alpha Savings", walletType = "Logical", id = "3")
        )

        // When: Filter and sort Logical wallets
        val logicalWallets = WalletSortingUtils.sortAlphabetically(
            allWallets.filter { it.walletType == "Logical" }
        )

        // Then: Special characters sort first
        assertEquals("!Priority Budget", logicalWallets[0].name)
        assertEquals("Alpha Savings", logicalWallets[1].name)
        assertEquals("Zoo Fund", logicalWallets[2].name)
    }

    // ========================================
    // Transfer Source Dropdown Tests
    // ========================================

    @Test
    fun `transfer source dropdown groups by type then alphabetically`() {
        // Given: Mixed Physical and Logical wallets in random order
        val allWallets = listOf(
            createWallet("Yellow Fund", walletType = "Logical", id = "1"),
            createWallet("Apple Pay", walletType = "Physical", id = "2"),
            createWallet("Blue Savings", walletType = "Logical", id = "3"),
            createWallet("Zebra Bank", walletType = "Physical", id = "4")
        )

        // When: Sort by type and name (simulates transfer source dropdown)
        val sourceWallets = WalletSortingUtils.sortByTypeAndName(allWallets)

        // Then: Physical wallets first, each group alphabetically sorted
        assertEquals(4, sourceWallets.size)
        // Physical group first
        assertEquals("Physical", sourceWallets[0].walletType)
        assertEquals("Apple Pay", sourceWallets[0].name)
        assertEquals("Physical", sourceWallets[1].walletType)
        assertEquals("Zebra Bank", sourceWallets[1].name)
        // Logical group second
        assertEquals("Logical", sourceWallets[2].walletType)
        assertEquals("Blue Savings", sourceWallets[2].name)
        assertEquals("Logical", sourceWallets[3].walletType)
        assertEquals("Yellow Fund", sourceWallets[3].name)
    }

    @Test
    fun `transfer source dropdown with only physical wallets`() {
        // Given: Only Physical wallets
        val allWallets = listOf(
            createWallet("Zebra Bank", walletType = "Physical", id = "1"),
            createWallet("Apple Pay", walletType = "Physical", id = "2"),
            createWallet("Cash", walletType = "Physical", id = "3")
        )

        // When: Sort by type and name
        val sourceWallets = WalletSortingUtils.sortByTypeAndName(allWallets)

        // Then: Alphabetically sorted Physical wallets
        assertEquals(3, sourceWallets.size)
        assertEquals("Apple Pay", sourceWallets[0].name)
        assertEquals("Cash", sourceWallets[1].name)
        assertEquals("Zebra Bank", sourceWallets[2].name)
    }

    @Test
    fun `transfer source dropdown with only logical wallets`() {
        // Given: Only Logical wallets
        val allWallets = listOf(
            createWallet("Yellow Fund", walletType = "Logical", id = "1"),
            createWallet("Blue Savings", walletType = "Logical", id = "2"),
            createWallet("Alpha Budget", walletType = "Logical", id = "3")
        )

        // When: Sort by type and name
        val sourceWallets = WalletSortingUtils.sortByTypeAndName(allWallets)

        // Then: Alphabetically sorted Logical wallets
        assertEquals(3, sourceWallets.size)
        assertEquals("Alpha Budget", sourceWallets[0].name)
        assertEquals("Blue Savings", sourceWallets[1].name)
        assertEquals("Yellow Fund", sourceWallets[2].name)
    }

    // ========================================
    // Transfer Destination Dropdown Tests
    // ========================================

    @Test
    fun `transfer destination dropdown excludes source and sorts alphabetically`() {
        // Given: Source wallet selected as "Apple Pay"
        val sourceWallet = createWallet("Apple Pay", walletType = "Physical", id = "source-1")
        val allWallets = listOf(
            sourceWallet,
            createWallet("Zebra Bank", walletType = "Physical", id = "2"),
            createWallet("Banana Account", walletType = "Physical", id = "3"),
            createWallet("Blue Savings", walletType = "Logical", id = "4")
        )

        // When: Filter out source wallet and sort (simulates destination dropdown)
        val destinationWallets = WalletSortingUtils.sortByTypeAndName(
            allWallets.filter { it.id != sourceWallet.id }
        )

        // Then: Source wallet excluded, remaining wallets sorted by type then name
        assertEquals(3, destinationWallets.size)
        // Physical wallets first
        assertEquals("Banana Account", destinationWallets[0].name)
        assertEquals("Zebra Bank", destinationWallets[1].name)
        // Logical wallets second
        assertEquals("Blue Savings", destinationWallets[2].name)
        // Apple Pay should not be in the list
        assertEquals(false, destinationWallets.any { it.id == sourceWallet.id })
    }

    @Test
    fun `transfer destination dropdown with logical source excludes correctly`() {
        // Given: Source wallet is Logical
        val sourceWallet = createWallet("Yellow Fund", walletType = "Logical", id = "source-1")
        val allWallets = listOf(
            createWallet("Apple Pay", walletType = "Physical", id = "1"),
            createWallet("Zebra Bank", walletType = "Physical", id = "2"),
            sourceWallet,
            createWallet("Blue Savings", walletType = "Logical", id = "3")
        )

        // When: Filter out source and sort
        val destinationWallets = WalletSortingUtils.sortByTypeAndName(
            allWallets.filter { it.id != sourceWallet.id }
        )

        // Then: Source excluded, others sorted by type then name
        assertEquals(3, destinationWallets.size)
        assertEquals("Apple Pay", destinationWallets[0].name)
        assertEquals("Zebra Bank", destinationWallets[1].name)
        assertEquals("Blue Savings", destinationWallets[2].name)
        // Yellow Fund should not be in the list
        assertEquals(false, destinationWallets.any { it.id == sourceWallet.id })
    }

    @Test
    fun `transfer destination dropdown maintains alphabetical order after exclusion`() {
        // Given: Source wallet in the middle alphabetically
        val sourceWallet = createWallet("Mango Account", walletType = "Physical", id = "source-1")
        val allWallets = listOf(
            createWallet("Apple Pay", walletType = "Physical", id = "1"),
            sourceWallet,
            createWallet("Zebra Bank", walletType = "Physical", id = "2")
        )

        // When: Filter out source and sort
        val destinationWallets = WalletSortingUtils.sortByTypeAndName(
            allWallets.filter { it.id != sourceWallet.id }
        )

        // Then: Remaining wallets still alphabetical
        assertEquals(2, destinationWallets.size)
        assertEquals("Apple Pay", destinationWallets[0].name)
        assertEquals("Zebra Bank", destinationWallets[1].name)
    }

    // ========================================
    // Ordering Persistence Tests
    // ========================================

    @Test
    fun `wallet ordering persists across multiple sorts`() {
        // Given: Same wallet list sorted multiple times
        val allWallets = listOf(
            createWallet("Zebra", walletType = "Physical", id = "1"),
            createWallet("Apple", walletType = "Physical", id = "2"),
            createWallet("Mango", walletType = "Physical", id = "3")
        )

        // When: Sort multiple times (simulates dialog reopen)
        val firstSort = WalletSortingUtils.sortAlphabetically(
            allWallets.filter { it.walletType == "Physical" }
        )
        val secondSort = WalletSortingUtils.sortAlphabetically(
            allWallets.filter { it.walletType == "Physical" }
        )

        // Then: Ordering is consistent
        assertEquals(firstSort.map { it.name }, secondSort.map { it.name })
        assertEquals("Apple", firstSort[0].name)
        assertEquals("Mango", firstSort[1].name)
        assertEquals("Zebra", firstSort[2].name)
    }

    @Test
    fun `wallet ordering is deterministic`() {
        // Given: Wallets with same creation order
        val allWallets = listOf(
            createWallet("Charlie", walletType = "Physical", id = "1"),
            createWallet("Alpha", walletType = "Physical", id = "2"),
            createWallet("Bravo", walletType = "Physical", id = "3")
        )

        // When: Sort multiple times
        val results = (1..5).map {
            WalletSortingUtils.sortAlphabetically(
                allWallets.filter { it.walletType == "Physical" }
            ).map { it.name }
        }

        // Then: All results are identical
        val expectedOrder = listOf("Alpha", "Bravo", "Charlie")
        results.forEach { result ->
            assertEquals(expectedOrder, result)
        }
    }

    // ========================================
    // Edge Cases
    // ========================================

    @Test
    fun `wallet dropdown with numbers in names sorts correctly`() {
        // Given: Wallets with numbers in names
        val allWallets = listOf(
            createWallet("Wallet2", walletType = "Physical", id = "1"),
            createWallet("Wallet10", walletType = "Physical", id = "2"),
            createWallet("Wallet1", walletType = "Physical", id = "3")
        )

        // When: Sort Physical wallets
        val physicalWallets = WalletSortingUtils.sortAlphabetically(
            allWallets.filter { it.walletType == "Physical" }
        )

        // Then: Lexicographic sorting (not numeric)
        assertEquals("Wallet1", physicalWallets[0].name)
        assertEquals("Wallet10", physicalWallets[1].name)
        assertEquals("Wallet2", physicalWallets[2].name)
    }

    @Test
    fun `wallet dropdown with duplicate names maintains stable order`() {
        // Given: Wallets with duplicate names
        val allWallets = listOf(
            createWallet("Cash", walletType = "Physical", id = "1"),
            createWallet("Bank", walletType = "Physical", id = "2"),
            createWallet("Cash", walletType = "Physical", id = "3")
        )

        // When: Sort Physical wallets
        val physicalWallets = WalletSortingUtils.sortAlphabetically(
            allWallets.filter { it.walletType == "Physical" }
        )

        // Then: Both "Cash" wallets appear, Bank comes first
        assertEquals(3, physicalWallets.size)
        assertEquals("Bank", physicalWallets[0].name)
        assertEquals("Cash", physicalWallets[1].name)
        assertEquals("Cash", physicalWallets[2].name)
    }
}
