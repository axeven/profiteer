package com.axeven.profiteerapp.ui.transaction

import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.utils.WalletSortingUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for wallet dropdown ordering in EditTransactionScreen.
 *
 * Following TDD approach - tests written BEFORE implementation (RED phase).
 * These tests verify that wallet dropdowns display wallets in alphabetical order
 * when editing existing transactions.
 *
 * EditTransactionScreen has 4 wallet selection scenarios (same as Create):
 * 1. Physical wallet dropdown (for Income/Expense)
 * 2. Logical wallet dropdown (for Income/Expense)
 * 3. Transfer source wallet dropdown (all wallets, grouped by type)
 * 4. Transfer destination wallet dropdown (all except source, grouped by type)
 *
 * Additional consideration: Pre-selected wallets should appear in their correct
 * alphabetical position within the sorted list.
 */
class EditTransactionScreenWalletOrderingTest {

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
        // Given: Existing transaction with pre-selected Physical wallet
        val allWallets = listOf(
            createWallet("Zebra Physical", walletType = "Physical", id = "1"),
            createWallet("Apple Physical", walletType = "Physical", id = "2"),
            createWallet("Mango Physical", walletType = "Physical", id = "3"),
            createWallet("Blue Logical", walletType = "Logical", id = "4") // Should be filtered out
        )

        // When: Filter Physical wallets and sort (simulates EditTransactionScreen logic)
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
    fun `physical wallet dropdown with pre-selected wallet in middle`() {
        // Given: Pre-selected wallet "Mango" which should appear in middle alphabetically
        val preSelectedWallet = createWallet("Mango Physical", walletType = "Physical", id = "pre-selected")
        val allWallets = listOf(
            createWallet("Zebra Physical", walletType = "Physical", id = "1"),
            preSelectedWallet,
            createWallet("Apple Physical", walletType = "Physical", id = "2")
        )

        // When: Filter and sort Physical wallets
        val physicalWallets = WalletSortingUtils.sortAlphabetically(
            allWallets.filter { it.walletType == "Physical" }
        )

        // Then: Pre-selected wallet appears in correct alphabetical position
        assertEquals(3, physicalWallets.size)
        assertEquals("Apple Physical", physicalWallets[0].name)
        assertEquals("Mango Physical", physicalWallets[1].name) // Pre-selected appears in middle
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

    // ========================================
    // Logical Wallet Dropdown Tests
    // ========================================

    @Test
    fun `logical wallet dropdown displays alphabetically`() {
        // Given: Existing transaction with pre-selected Logical wallet
        val allWallets = listOf(
            createWallet("Zebra Physical", walletType = "Physical", id = "1"), // Should be filtered out
            createWallet("Yellow Logical", walletType = "Logical", id = "2"),
            createWallet("Blue Logical", walletType = "Logical", id = "3"),
            createWallet("Red Logical", walletType = "Logical", id = "4")
        )

        // When: Filter Logical wallets and sort (simulates EditTransactionScreen logic)
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
    fun `logical wallet dropdown with pre-selected wallet at beginning`() {
        // Given: Pre-selected wallet "Alpha" which should appear first alphabetically
        val preSelectedWallet = createWallet("Alpha Fund", walletType = "Logical", id = "pre-selected")
        val allWallets = listOf(
            createWallet("Zebra Fund", walletType = "Logical", id = "1"),
            preSelectedWallet,
            createWallet("Mango Budget", walletType = "Logical", id = "2")
        )

        // When: Filter and sort Logical wallets
        val logicalWallets = WalletSortingUtils.sortAlphabetically(
            allWallets.filter { it.walletType == "Logical" }
        )

        // Then: Pre-selected wallet appears first
        assertEquals(3, logicalWallets.size)
        assertEquals("Alpha Fund", logicalWallets[0].name) // Pre-selected appears first
        assertEquals("Mango Budget", logicalWallets[1].name)
        assertEquals("Zebra Fund", logicalWallets[2].name)
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

    // ========================================
    // Transfer Source Dropdown Tests
    // ========================================

    @Test
    fun `transfer source dropdown groups by type then alphabetically`() {
        // Given: Editing existing transfer transaction with mixed wallets
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
    fun `transfer source dropdown with pre-selected source wallet`() {
        // Given: Pre-selected source wallet should appear in correct position
        val preSelectedSource = createWallet("Cash", walletType = "Physical", id = "pre-selected")
        val allWallets = listOf(
            createWallet("Zebra Bank", walletType = "Physical", id = "1"),
            preSelectedSource,
            createWallet("Apple Pay", walletType = "Physical", id = "2"),
            createWallet("Blue Fund", walletType = "Logical", id = "3")
        )

        // When: Sort by type and name
        val sourceWallets = WalletSortingUtils.sortByTypeAndName(allWallets)

        // Then: Pre-selected source appears in correct alphabetical position
        assertEquals(4, sourceWallets.size)
        assertEquals("Apple Pay", sourceWallets[0].name)
        assertEquals("Cash", sourceWallets[1].name) // Pre-selected in middle of Physical group
        assertEquals("Zebra Bank", sourceWallets[2].name)
        assertEquals("Blue Fund", sourceWallets[3].name)
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

    // ========================================
    // Transfer Destination Dropdown Tests
    // ========================================

    @Test
    fun `transfer destination dropdown excludes source and sorts alphabetically`() {
        // Given: Editing transfer with source wallet selected as "Apple Pay"
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
        assertTrue(destinationWallets.none { it.id == sourceWallet.id })
    }

    @Test
    fun `transfer destination dropdown with pre-selected destination wallet`() {
        // Given: Editing transfer with pre-selected source and destination
        val sourceWallet = createWallet("Apple Pay", walletType = "Physical", id = "source-1")
        val preSelectedDestination = createWallet("Mango Account", walletType = "Physical", id = "dest-1")
        val allWallets = listOf(
            sourceWallet,
            createWallet("Zebra Bank", walletType = "Physical", id = "2"),
            preSelectedDestination,
            createWallet("Blue Savings", walletType = "Logical", id = "3")
        )

        // When: Filter out source and sort
        val destinationWallets = WalletSortingUtils.sortByTypeAndName(
            allWallets.filter { it.id != sourceWallet.id }
        )

        // Then: Pre-selected destination appears in correct position, source excluded
        assertEquals(3, destinationWallets.size)
        assertEquals("Mango Account", destinationWallets[0].name) // Pre-selected destination
        assertEquals("Zebra Bank", destinationWallets[1].name)
        assertEquals("Blue Savings", destinationWallets[2].name)
        assertTrue(destinationWallets.none { it.id == sourceWallet.id })
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
        assertTrue(destinationWallets.none { it.id == sourceWallet.id })
    }

    // ========================================
    // Pre-Selected Wallet Position Tests
    // ========================================

    @Test
    fun `pre-selected wallet appears in alphabetically sorted list`() {
        // Given: Transaction with wallet "Mango" selected
        val preSelectedWallet = createWallet("Mango", walletType = "Physical", id = "pre-selected")
        val allWallets = listOf(
            createWallet("Zebra", walletType = "Physical", id = "1"),
            createWallet("Apple", walletType = "Physical", id = "2"),
            preSelectedWallet
        )

        // When: Filter and sort Physical wallets
        val physicalWallets = WalletSortingUtils.sortAlphabetically(
            allWallets.filter { it.walletType == "Physical" }
        )

        // Then: "Mango" appears in correct alphabetical position
        assertEquals(3, physicalWallets.size)
        assertEquals("Apple", physicalWallets[0].name)
        assertEquals("Mango", physicalWallets[1].name) // Pre-selected wallet in correct position
        assertEquals("Zebra", physicalWallets[2].name)

        // And: Pre-selected wallet is present in the list
        assertTrue(physicalWallets.any { it.id == preSelectedWallet.id })
    }

    @Test
    fun `changing wallet selection maintains alphabetical order`() {
        // Given: Initial wallet selected, then user changes selection
        val initialWallet = createWallet("Zebra", walletType = "Physical", id = "initial")
        val newWallet = createWallet("Apple", walletType = "Physical", id = "new")
        val allWallets = listOf(
            initialWallet,
            newWallet,
            createWallet("Mango", walletType = "Physical", id = "3")
        )

        // When: Sort wallets (simulates opening dropdown after changing selection)
        val physicalWallets = WalletSortingUtils.sortAlphabetically(
            allWallets.filter { it.walletType == "Physical" }
        )

        // Then: Both old and new wallet appear in alphabetical order
        assertEquals(3, physicalWallets.size)
        assertEquals("Apple", physicalWallets[0].name) // New selection
        assertEquals("Mango", physicalWallets[1].name)
        assertEquals("Zebra", physicalWallets[2].name) // Old selection
    }

    // ========================================
    // Ordering Consistency Tests
    // ========================================

    @Test
    fun `wallet ordering persists during edit session`() {
        // Given: Same wallet list sorted multiple times during edit session
        val allWallets = listOf(
            createWallet("Zebra", walletType = "Physical", id = "1"),
            createWallet("Apple", walletType = "Physical", id = "2"),
            createWallet("Mango", walletType = "Physical", id = "3")
        )

        // When: Sort multiple times (simulates reopening dropdown during edit)
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
    fun `wallet ordering is deterministic during edit`() {
        // Given: Wallets with same creation order
        val allWallets = listOf(
            createWallet("Charlie", walletType = "Physical", id = "1"),
            createWallet("Alpha", walletType = "Physical", id = "2"),
            createWallet("Bravo", walletType = "Physical", id = "3")
        )

        // When: Sort multiple times during edit session
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
    fun `edit transaction with numbers in wallet names sorts correctly`() {
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
    fun `edit transaction maintains order when wallet list unchanged`() {
        // Given: Same wallet list used in edit mode
        val allWallets = listOf(
            createWallet("Zebra", walletType = "Physical", id = "1"),
            createWallet("Apple", walletType = "Physical", id = "2")
        )

        // When: Sort wallets twice without changes
        val firstSort = WalletSortingUtils.sortAlphabetically(
            allWallets.filter { it.walletType == "Physical" }
        )

        // Simulate time passing or screen recomposition
        val secondSort = WalletSortingUtils.sortAlphabetically(
            allWallets.filter { it.walletType == "Physical" }
        )

        // Then: Order is identical
        assertEquals(firstSort.map { it.id }, secondSort.map { it.id })
        assertEquals(firstSort.map { it.name }, secondSort.map { it.name })
    }
}
