package com.axeven.profiteerapp.ui.transaction

import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.utils.WalletSortingUtils
import org.junit.Assert.*
import org.junit.Test
import java.util.*

/**
 * TDD Test Suite: TransactionListScreen Wallet Filter Ordering
 *
 * Purpose: Verify that wallet filters in TransactionListScreen display alphabetically
 *
 * Test Coverage:
 * - Physical wallet filter ordering
 * - Logical wallet filter ordering
 * - Multi-select maintains alphabetical order
 * - Checkbox state preserved with sorting
 * - Edge cases (empty lists, single wallet, duplicate names)
 *
 * Note: These tests verify the sorting logic that will be applied in TransactionListScreen.
 * They test WalletSortingUtils functions directly (which the screen will use).
 */
class TransactionListScreenWalletOrderingTest {

    // Helper function to create test wallets
    private fun createWallet(
        name: String,
        walletType: String = "Physical",
        id: String = UUID.randomUUID().toString(),
        balance: Double = 0.0
    ): Wallet {
        return Wallet(
            id = id,
            name = name,
            walletType = walletType,
            balance = balance,
            initialBalance = 0.0,
            userId = "test-user-id",
            createdAt = Date(),
            updatedAt = Date()
        )
    }

    // ============================================================================
    // Physical Wallet Filter Tests
    // ============================================================================

    @Test
    fun `physical wallet filter displays alphabetically`() {
        // Given: Physical wallets in random order
        val wallets = listOf(
            createWallet("Zebra Bank", walletType = "Physical", id = "1"),
            createWallet("Apple Pay", walletType = "Physical", id = "2"),
            createWallet("Mango Wallet", walletType = "Physical", id = "3")
        )

        // When: Apply alphabetical sorting
        val physicalWallets = WalletSortingUtils.sortAlphabetically(
            wallets.filter { it.walletType == "Physical" }
        )

        // Then: Wallets should be sorted A-Z
        assertEquals(3, physicalWallets.size)
        assertEquals("Apple Pay", physicalWallets[0].name)
        assertEquals("Mango Wallet", physicalWallets[1].name)
        assertEquals("Zebra Bank", physicalWallets[2].name)
    }

    @Test
    fun `physical wallet filter is case insensitive`() {
        // Given: Physical wallets with mixed case
        val wallets = listOf(
            createWallet("zebra", walletType = "Physical", id = "1"),
            createWallet("Apple", walletType = "Physical", id = "2"),
            createWallet("MANGO", walletType = "Physical", id = "3"),
            createWallet("banana", walletType = "Physical", id = "4")
        )

        // When: Apply alphabetical sorting
        val physicalWallets = WalletSortingUtils.sortAlphabetically(
            wallets.filter { it.walletType == "Physical" }
        )

        // Then: Wallets should be sorted case-insensitively
        assertEquals("Apple", physicalWallets[0].name)
        assertEquals("banana", physicalWallets[1].name)
        assertEquals("MANGO", physicalWallets[2].name)
        assertEquals("zebra", physicalWallets[3].name)
    }

    @Test
    fun `physical wallet filter with special characters`() {
        // Given: Physical wallets with special characters
        val wallets = listOf(
            createWallet("Zoo Bank", walletType = "Physical", id = "1"),
            createWallet("!Special Account", walletType = "Physical", id = "2"),
            createWallet("@Cash", walletType = "Physical", id = "3"),
            createWallet("Alpha Wallet", walletType = "Physical", id = "4")
        )

        // When: Apply alphabetical sorting
        val physicalWallets = WalletSortingUtils.sortAlphabetically(
            wallets.filter { it.walletType == "Physical" }
        )

        // Then: Special characters should sort before letters
        assertEquals(4, physicalWallets.size)
        assertTrue(physicalWallets[0].name.startsWith("!") || physicalWallets[0].name.startsWith("@"))
        assertEquals("Zoo Bank", physicalWallets[3].name)
    }

    @Test
    fun `physical wallet filter with empty list`() {
        // Given: No physical wallets
        val wallets = emptyList<Wallet>()

        // When: Apply alphabetical sorting
        val physicalWallets = WalletSortingUtils.sortAlphabetically(
            wallets.filter { it.walletType == "Physical" }
        )

        // Then: Result should be empty
        assertTrue(physicalWallets.isEmpty())
    }

    @Test
    fun `physical wallet filter with single wallet`() {
        // Given: Single physical wallet
        val wallets = listOf(
            createWallet("Cash", walletType = "Physical", id = "1")
        )

        // When: Apply alphabetical sorting
        val physicalWallets = WalletSortingUtils.sortAlphabetically(
            wallets.filter { it.walletType == "Physical" }
        )

        // Then: Should return the same wallet
        assertEquals(1, physicalWallets.size)
        assertEquals("Cash", physicalWallets[0].name)
    }

    // ============================================================================
    // Logical Wallet Filter Tests
    // ============================================================================

    @Test
    fun `logical wallet filter displays alphabetically`() {
        // Given: Logical wallets in random order
        val wallets = listOf(
            createWallet("Yellow Fund", walletType = "Logical", id = "1"),
            createWallet("Blue Savings", walletType = "Logical", id = "2"),
            createWallet("Red Budget", walletType = "Logical", id = "3")
        )

        // When: Apply alphabetical sorting
        val logicalWallets = WalletSortingUtils.sortAlphabetically(
            wallets.filter { it.walletType == "Logical" }
        )

        // Then: Wallets should be sorted A-Z
        assertEquals(3, logicalWallets.size)
        assertEquals("Blue Savings", logicalWallets[0].name)
        assertEquals("Red Budget", logicalWallets[1].name)
        assertEquals("Yellow Fund", logicalWallets[2].name)
    }

    @Test
    fun `logical wallet filter is case insensitive`() {
        // Given: Logical wallets with mixed case
        val wallets = listOf(
            createWallet("yellow", walletType = "Logical", id = "1"),
            createWallet("Blue", walletType = "Logical", id = "2"),
            createWallet("RED", walletType = "Logical", id = "3"),
            createWallet("alpha", walletType = "Logical", id = "4")
        )

        // When: Apply alphabetical sorting
        val logicalWallets = WalletSortingUtils.sortAlphabetically(
            wallets.filter { it.walletType == "Logical" }
        )

        // Then: Wallets should be sorted case-insensitively
        assertEquals("alpha", logicalWallets[0].name)
        assertEquals("Blue", logicalWallets[1].name)
        assertEquals("RED", logicalWallets[2].name)
        assertEquals("yellow", logicalWallets[3].name)
    }

    @Test
    fun `logical wallet filter with numbers in names`() {
        // Given: Logical wallets with numbers
        val wallets = listOf(
            createWallet("Fund 10", walletType = "Logical", id = "1"),
            createWallet("Fund 2", walletType = "Logical", id = "2"),
            createWallet("Fund 1", walletType = "Logical", id = "3")
        )

        // When: Apply alphabetical sorting
        val logicalWallets = WalletSortingUtils.sortAlphabetically(
            wallets.filter { it.walletType == "Logical" }
        )

        // Then: Should sort lexicographically (1, 10, 2)
        assertEquals("Fund 1", logicalWallets[0].name)
        assertEquals("Fund 10", logicalWallets[1].name)
        assertEquals("Fund 2", logicalWallets[2].name)
    }

    @Test
    fun `logical wallet filter with empty list`() {
        // Given: No logical wallets
        val wallets = emptyList<Wallet>()

        // When: Apply alphabetical sorting
        val logicalWallets = WalletSortingUtils.sortAlphabetically(
            wallets.filter { it.walletType == "Logical" }
        )

        // Then: Result should be empty
        assertTrue(logicalWallets.isEmpty())
    }

    // ============================================================================
    // Multi-Select Behavior Tests
    // ============================================================================

    @Test
    fun `multi-select preserves alphabetical order`() {
        // Given: Wallets in alphabetical order
        val wallets = listOf(
            createWallet("Alpha", walletType = "Physical", id = "1"),
            createWallet("Bravo", walletType = "Physical", id = "2"),
            createWallet("Charlie", walletType = "Physical", id = "3"),
            createWallet("Delta", walletType = "Physical", id = "4")
        )

        // When: Apply alphabetical sorting
        val physicalWallets = WalletSortingUtils.sortAlphabetically(
            wallets.filter { it.walletType == "Physical" }
        )

        // Simulate multi-select by extracting IDs in order
        val selectedWalletIds = listOf(
            physicalWallets[1].id, // "Bravo"
            physicalWallets[3].id  // "Delta"
        )

        // Then: Selected wallets should maintain alphabetical positions
        val selectedWallets = physicalWallets.filter { it.id in selectedWalletIds }
        assertEquals(2, selectedWallets.size)
        assertEquals("Bravo", selectedWallets[0].name)
        assertEquals("Delta", selectedWallets[1].name)
    }

    @Test
    fun `multi-select with all wallets selected maintains order`() {
        // Given: Multiple wallets
        val wallets = listOf(
            createWallet("Zebra", walletType = "Physical", id = "1"),
            createWallet("Apple", walletType = "Physical", id = "2"),
            createWallet("Mango", walletType = "Physical", id = "3")
        )

        // When: Apply alphabetical sorting
        val physicalWallets = WalletSortingUtils.sortAlphabetically(
            wallets.filter { it.walletType == "Physical" }
        )

        // Simulate selecting all wallets
        val allSelected = physicalWallets.map { it.id }

        // Then: All wallets should be in alphabetical order
        assertEquals(3, allSelected.size)
        assertEquals("Apple", physicalWallets[0].name)
        assertEquals("Mango", physicalWallets[1].name)
        assertEquals("Zebra", physicalWallets[2].name)
    }

    @Test
    fun `deselecting wallet maintains alphabetical order for remaining`() {
        // Given: Sorted wallets
        val wallets = listOf(
            createWallet("Alpha", walletType = "Physical", id = "1"),
            createWallet("Bravo", walletType = "Physical", id = "2"),
            createWallet("Charlie", walletType = "Physical", id = "3")
        )

        // When: Apply alphabetical sorting
        val physicalWallets = WalletSortingUtils.sortAlphabetically(
            wallets.filter { it.walletType == "Physical" }
        )

        // Simulate deselecting "Bravo" (middle item)
        val remainingSelected = physicalWallets.filter { it.name != "Bravo" }

        // Then: Remaining wallets should maintain alphabetical order
        assertEquals(2, remainingSelected.size)
        assertEquals("Alpha", remainingSelected[0].name)
        assertEquals("Charlie", remainingSelected[1].name)
    }

    // ============================================================================
    // Checkbox State Preservation Tests
    // ============================================================================

    @Test
    fun `checkbox state preserved with sorting`() {
        // Given: Wallets with one "selected"
        val wallets = listOf(
            createWallet("Zebra", walletType = "Physical", id = "1"),
            createWallet("Mango", walletType = "Physical", id = "2"),
            createWallet("Apple", walletType = "Physical", id = "3")
        )

        val selectedWalletId = "2" // "Mango" selected

        // When: Apply alphabetical sorting
        val physicalWallets = WalletSortingUtils.sortAlphabetically(
            wallets.filter { it.walletType == "Physical" }
        )

        // Then: "Mango" should appear in correct alphabetical position
        val mangoIndex = physicalWallets.indexOfFirst { it.id == selectedWalletId }
        assertEquals(1, mangoIndex) // "Mango" should be at index 1 (Apple, Mango, Zebra)
        assertEquals("Mango", physicalWallets[mangoIndex].name)
    }

    @Test
    fun `multiple checkboxes preserved in sorted list`() {
        // Given: Wallets with multiple selected
        val wallets = listOf(
            createWallet("Zebra", walletType = "Physical", id = "1"),
            createWallet("Apple", walletType = "Physical", id = "2"),
            createWallet("Mango", walletType = "Physical", id = "3"),
            createWallet("Banana", walletType = "Physical", id = "4")
        )

        val selectedWalletIds = setOf("1", "3") // "Zebra" and "Mango" selected

        // When: Apply alphabetical sorting
        val physicalWallets = WalletSortingUtils.sortAlphabetically(
            wallets.filter { it.walletType == "Physical" }
        )

        // Then: Selected wallets should appear in correct alphabetical positions
        val selectedWallets = physicalWallets.filter { it.id in selectedWalletIds }
        assertEquals(2, selectedWallets.size)
        assertEquals("Mango", selectedWallets[0].name) // "Mango" comes before "Zebra"
        assertEquals("Zebra", selectedWallets[1].name)
    }

    @Test
    fun `checkbox state preserved after reopen`() {
        // Given: Sorted wallets (simulating filter dropdown reopening)
        val wallets = listOf(
            createWallet("Charlie", walletType = "Physical", id = "1"),
            createWallet("Alpha", walletType = "Physical", id = "2"),
            createWallet("Bravo", walletType = "Physical", id = "3")
        )

        val selectedWalletIds = setOf("2", "3") // "Alpha" and "Bravo" selected

        // When: Apply alphabetical sorting (first open)
        val firstSort = WalletSortingUtils.sortAlphabetically(
            wallets.filter { it.walletType == "Physical" }
        )

        // When: Apply alphabetical sorting again (reopen)
        val secondSort = WalletSortingUtils.sortAlphabetically(
            wallets.filter { it.walletType == "Physical" }
        )

        // Then: Order should be identical both times
        assertEquals(firstSort.map { it.name }, secondSort.map { it.name })

        // Then: Selected wallets should be in same positions
        val firstSelected = firstSort.filter { it.id in selectedWalletIds }
        val secondSelected = secondSort.filter { it.id in selectedWalletIds }
        assertEquals(firstSelected.map { it.name }, secondSelected.map { it.name })
    }

    // ============================================================================
    // Mixed Wallet Type Tests
    // ============================================================================

    @Test
    fun `filtering by type maintains alphabetical order within each type`() {
        // Given: Mixed wallet types
        val wallets = listOf(
            createWallet("Zebra Physical", walletType = "Physical", id = "1"),
            createWallet("Yellow Logical", walletType = "Logical", id = "2"),
            createWallet("Apple Physical", walletType = "Physical", id = "3"),
            createWallet("Blue Logical", walletType = "Logical", id = "4")
        )

        // When: Apply sorting to each type separately
        val physicalWallets = WalletSortingUtils.sortAlphabetically(
            wallets.filter { it.walletType == "Physical" }
        )
        val logicalWallets = WalletSortingUtils.sortAlphabetically(
            wallets.filter { it.walletType == "Logical" }
        )

        // Then: Each type should be sorted alphabetically
        assertEquals("Apple Physical", physicalWallets[0].name)
        assertEquals("Zebra Physical", physicalWallets[1].name)
        assertEquals("Blue Logical", logicalWallets[0].name)
        assertEquals("Yellow Logical", logicalWallets[1].name)
    }

    @Test
    fun `filter dropdown shows only wallets of correct type in alphabetical order`() {
        // Given: Mixed wallet types
        val wallets = listOf(
            createWallet("Zebra Physical", walletType = "Physical", id = "1"),
            createWallet("Blue Logical", walletType = "Logical", id = "2"),
            createWallet("Apple Physical", walletType = "Physical", id = "3"),
            createWallet("Yellow Logical", walletType = "Logical", id = "4"),
            createWallet("Mango Physical", walletType = "Physical", id = "5")
        )

        // When: Filter for Physical wallets only
        val physicalWallets = WalletSortingUtils.sortAlphabetically(
            wallets.filter { it.walletType == "Physical" }
        )

        // Then: Should contain only Physical wallets in alphabetical order
        assertEquals(3, physicalWallets.size)
        assertTrue(physicalWallets.all { it.walletType == "Physical" })
        assertEquals("Apple Physical", physicalWallets[0].name)
        assertEquals("Mango Physical", physicalWallets[1].name)
        assertEquals("Zebra Physical", physicalWallets[2].name)
    }

    // ============================================================================
    // Edge Cases
    // ============================================================================

    @Test
    fun `wallet filter with duplicate names sorts stably`() {
        // Given: Wallets with duplicate names
        val wallets = listOf(
            createWallet("Cash", walletType = "Physical", id = "1"),
            createWallet("Bank", walletType = "Physical", id = "2"),
            createWallet("Cash", walletType = "Physical", id = "3")
        )

        // When: Apply alphabetical sorting
        val physicalWallets = WalletSortingUtils.sortAlphabetically(
            wallets.filter { it.walletType == "Physical" }
        )

        // Then: Should handle duplicates without errors
        assertEquals(3, physicalWallets.size)
        assertEquals("Bank", physicalWallets[0].name)
        assertEquals("Cash", physicalWallets[1].name)
        assertEquals("Cash", physicalWallets[2].name)
    }

    @Test
    fun `wallet filter ordering is deterministic`() {
        // Given: Same wallets in different initial orders
        val wallets1 = listOf(
            createWallet("Zebra", walletType = "Physical", id = "1"),
            createWallet("Apple", walletType = "Physical", id = "2"),
            createWallet("Mango", walletType = "Physical", id = "3")
        )
        val wallets2 = listOf(
            createWallet("Apple", walletType = "Physical", id = "2"),
            createWallet("Mango", walletType = "Physical", id = "3"),
            createWallet("Zebra", walletType = "Physical", id = "1")
        )

        // When: Apply alphabetical sorting to both
        val sorted1 = WalletSortingUtils.sortAlphabetically(
            wallets1.filter { it.walletType == "Physical" }
        )
        val sorted2 = WalletSortingUtils.sortAlphabetically(
            wallets2.filter { it.walletType == "Physical" }
        )

        // Then: Both should produce identical order
        assertEquals(sorted1.map { it.name }, sorted2.map { it.name })
        assertEquals(sorted1.map { it.id }, sorted2.map { it.id })
    }

    @Test
    fun `wallet filter with very long names sorts correctly`() {
        // Given: Wallets with very long names
        val wallets = listOf(
            createWallet("Zebra Bank Account with Very Long Name for Testing", walletType = "Physical", id = "1"),
            createWallet("Apple Pay Mobile Payment Wallet", walletType = "Physical", id = "2"),
            createWallet("Banana Finance Primary Checking Account", walletType = "Physical", id = "3")
        )

        // When: Apply alphabetical sorting
        val physicalWallets = WalletSortingUtils.sortAlphabetically(
            wallets.filter { it.walletType == "Physical" }
        )

        // Then: Should sort by first character correctly
        assertTrue(physicalWallets[0].name.startsWith("Apple"))
        assertTrue(physicalWallets[1].name.startsWith("Banana"))
        assertTrue(physicalWallets[2].name.startsWith("Zebra"))
    }
}
