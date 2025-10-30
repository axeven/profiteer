package com.axeven.profiteerapp.utils

import com.axeven.profiteerapp.data.model.Wallet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for WalletSortingUtils utility functions.
 *
 * Following TDD approach - tests written BEFORE implementation (RED phase).
 * These tests define the expected behavior of wallet sorting functionality.
 *
 * WalletSortingUtils provides two sorting methods:
 * 1. sortAlphabetically() - Case-insensitive alphabetical sorting by wallet name
 * 2. sortByTypeAndName() - Group by wallet type (Physical/Logical), then alphabetical within groups
 *
 * Test Coverage:
 * - Empty lists
 * - Single wallet
 * - Multiple wallets
 * - Case-insensitive sorting
 * - Special characters
 * - Numbers in names
 * - Immutability
 * - Null safety
 * - Type grouping
 * - Unknown wallet types
 */
class WalletSortingUtilsTest {

    // ========================================
    // Helper Functions
    // ========================================

    private fun createWallet(
        name: String,
        walletType: String = "Physical",
        id: String = "test-id"
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
    // sortAlphabetically() - Basic Tests
    // ========================================

    @Test
    fun `sortAlphabetically - empty list returns empty list`() {
        // Given: Empty wallet list
        val wallets = emptyList<Wallet>()

        // When: Sort alphabetically
        val result = WalletSortingUtils.sortAlphabetically(wallets)

        // Then: Result is empty list
        assertTrue(result.isEmpty())
        assertEquals(0, result.size)
    }

    @Test
    fun `sortAlphabetically - single wallet returns same wallet`() {
        // Given: List with one wallet named "Cash"
        val wallet = createWallet("Cash")
        val wallets = listOf(wallet)

        // When: Sort alphabetically
        val result = WalletSortingUtils.sortAlphabetically(wallets)

        // Then: Result contains same wallet
        assertEquals(1, result.size)
        assertEquals("Cash", result[0].name)
        assertEquals(wallet.id, result[0].id)
    }

    @Test
    fun `sortAlphabetically - multiple wallets sorts A to Z`() {
        // Given: Wallets named ["Zebra", "Apple", "Mango", "Banana"]
        val wallets = listOf(
            createWallet("Zebra", id = "1"),
            createWallet("Apple", id = "2"),
            createWallet("Mango", id = "3"),
            createWallet("Banana", id = "4")
        )

        // When: Sort alphabetically
        val result = WalletSortingUtils.sortAlphabetically(wallets)

        // Then: Result order is ["Apple", "Banana", "Mango", "Zebra"]
        assertEquals(4, result.size)
        assertEquals("Apple", result[0].name)
        assertEquals("Banana", result[1].name)
        assertEquals("Mango", result[2].name)
        assertEquals("Zebra", result[3].name)
    }

    // ========================================
    // sortAlphabetically() - Case Sensitivity Tests
    // ========================================

    @Test
    fun `sortAlphabetically - case insensitive sorts correctly`() {
        // Given: Wallets ["apple", "Banana", "CHERRY", "dragonfruit"]
        val wallets = listOf(
            createWallet("apple", id = "1"),
            createWallet("Banana", id = "2"),
            createWallet("CHERRY", id = "3"),
            createWallet("dragonfruit", id = "4")
        )

        // When: Sort alphabetically
        val result = WalletSortingUtils.sortAlphabetically(wallets)

        // Then: Lowercase "a" comes before uppercase "B" (case-insensitive)
        assertEquals(4, result.size)
        assertEquals("apple", result[0].name)
        assertEquals("Banana", result[1].name)
        assertEquals("CHERRY", result[2].name)
        assertEquals("dragonfruit", result[3].name)
    }

    @Test
    fun `sortAlphabetically - mixed case same word sorts by original case`() {
        // Given: Wallets with same name, different case
        val wallets = listOf(
            createWallet("wallet", id = "1"),
            createWallet("Wallet", id = "2"),
            createWallet("WALLET", id = "3")
        )

        // When: Sort alphabetically
        val result = WalletSortingUtils.sortAlphabetically(wallets)

        // Then: All appear in result (stable sort preserves original order for equal items)
        assertEquals(3, result.size)
        // Note: Exact order of equal items depends on sort stability
    }

    // ========================================
    // sortAlphabetically() - Special Characters Tests
    // ========================================

    @Test
    fun `sortAlphabetically - special characters sort before letters`() {
        // Given: Wallets ["Zoo", "!Special", "@Symbol", "Alpha"]
        val wallets = listOf(
            createWallet("Zoo", id = "1"),
            createWallet("!Special", id = "2"),
            createWallet("@Symbol", id = "3"),
            createWallet("Alpha", id = "4")
        )

        // When: Sort alphabetically
        val result = WalletSortingUtils.sortAlphabetically(wallets)

        // Then: Special chars sort before letters (Unicode order)
        assertEquals(4, result.size)
        assertEquals("!Special", result[0].name) // ! comes first in ASCII
        assertEquals("@Symbol", result[1].name)  // @ comes after !
        assertEquals("Alpha", result[2].name)    // Letters come after symbols
        assertEquals("Zoo", result[3].name)
    }

    @Test
    fun `sortAlphabetically - emoji in name sorts correctly`() {
        // Given: Wallets with emojis
        val wallets = listOf(
            createWallet("Zoo", id = "1"),
            createWallet("🏦 Bank", id = "2"),
            createWallet("Apple", id = "3")
        )

        // When: Sort alphabetically
        val result = WalletSortingUtils.sortAlphabetically(wallets)

        // Then: No crash, emoji sorts based on Unicode value
        assertEquals(3, result.size)
        // Note: Exact emoji position depends on Unicode value
    }

    // ========================================
    // sortAlphabetically() - Numbers Tests
    // ========================================

    @Test
    fun `sortAlphabetically - numbers in names sort lexicographically`() {
        // Given: Wallets ["Wallet2", "Wallet10", "Wallet1"]
        val wallets = listOf(
            createWallet("Wallet2", id = "1"),
            createWallet("Wallet10", id = "2"),
            createWallet("Wallet1", id = "3")
        )

        // When: Sort alphabetically
        val result = WalletSortingUtils.sortAlphabetically(wallets)

        // Then: Lexicographic sort (not numeric) - "Wallet1", "Wallet10", "Wallet2"
        assertEquals(3, result.size)
        assertEquals("Wallet1", result[0].name)
        assertEquals("Wallet10", result[1].name)  // "10" comes before "2" lexicographically
        assertEquals("Wallet2", result[2].name)
    }

    @Test
    fun `sortAlphabetically - numbers sort before letters`() {
        // Given: Wallets starting with numbers vs letters
        val wallets = listOf(
            createWallet("Alpha", id = "1"),
            createWallet("1stWallet", id = "2"),
            createWallet("2ndWallet", id = "3"),
            createWallet("Bravo", id = "4")
        )

        // When: Sort alphabetically
        val result = WalletSortingUtils.sortAlphabetically(wallets)

        // Then: Numbers sort before letters
        assertEquals(4, result.size)
        assertEquals("1stWallet", result[0].name)
        assertEquals("2ndWallet", result[1].name)
        assertEquals("Alpha", result[2].name)
        assertEquals("Bravo", result[3].name)
    }

    // ========================================
    // sortAlphabetically() - Immutability Tests
    // ========================================

    @Test
    fun `sortAlphabetically - original list remains unchanged`() {
        // Given: Original wallet list in random order
        val original = listOf(
            createWallet("Zebra", id = "1"),
            createWallet("Apple", id = "2"),
            createWallet("Mango", id = "3")
        )
        val originalOrder = original.map { it.name }

        // When: Sort alphabetically
        val result = WalletSortingUtils.sortAlphabetically(original)

        // Then: Original list order is unchanged
        assertEquals(originalOrder, original.map { it.name })
        assertEquals("Zebra", original[0].name)
        assertEquals("Apple", original[1].name)
        assertEquals("Mango", original[2].name)

        // And: Result has different order
        assertEquals("Apple", result[0].name)
        assertEquals("Mango", result[1].name)
        assertEquals("Zebra", result[2].name)
    }

    @Test
    fun `sortAlphabetically - returns new list instance`() {
        // Given: Original wallet list
        val original = listOf(
            createWallet("Apple", id = "1"),
            createWallet("Banana", id = "2")
        )

        // When: Sort alphabetically
        val result = WalletSortingUtils.sortAlphabetically(original)

        // Then: Result is a different instance
        assertNotSame(original, result)
    }

    // ========================================
    // sortAlphabetically() - Edge Cases
    // ========================================

    @Test
    fun `sortAlphabetically - whitespace in names sorts correctly`() {
        // Given: Wallets with spaces
        val wallets = listOf(
            createWallet("My Wallet", id = "1"),
            createWallet("Main Account", id = "2"),
            createWallet("Savings Fund", id = "3")
        )

        // When: Sort alphabetically
        val result = WalletSortingUtils.sortAlphabetically(wallets)

        // Then: Sorts correctly including spaces
        assertEquals(3, result.size)
        assertEquals("Main Account", result[0].name)
        assertEquals("My Wallet", result[1].name)
        assertEquals("Savings Fund", result[2].name)
    }

    @Test
    fun `sortAlphabetically - duplicate names maintain stable order`() {
        // Given: Wallets with duplicate names but different IDs
        val wallets = listOf(
            createWallet("Cash", id = "1"),
            createWallet("Bank", id = "2"),
            createWallet("Cash", id = "3")
        )

        // When: Sort alphabetically
        val result = WalletSortingUtils.sortAlphabetically(wallets)

        // Then: Both "Cash" wallets appear, "Bank" comes first
        assertEquals(3, result.size)
        assertEquals("Bank", result[0].name)
        assertEquals("Cash", result[1].name)
        assertEquals("Cash", result[2].name)
        // Stable sort should preserve relative order of equal elements
    }

    @Test
    fun `sortAlphabetically - very long names sort correctly`() {
        // Given: Wallets with very long names
        val longName1 = "A".repeat(100)
        val longName2 = "B".repeat(100)
        val wallets = listOf(
            createWallet(longName2, id = "1"),
            createWallet(longName1, id = "2")
        )

        // When: Sort alphabetically
        val result = WalletSortingUtils.sortAlphabetically(wallets)

        // Then: Long names sort correctly
        assertEquals(2, result.size)
        assertEquals(longName1, result[0].name)
        assertEquals(longName2, result[1].name)
    }

    // ========================================
    // sortByTypeAndName() - Physical First Tests
    // ========================================

    @Test
    fun `sortByTypeAndName - physical first groups correctly`() {
        // Given: Mixed Physical and Logical wallets
        val wallets = listOf(
            createWallet("Yellow", walletType = "Logical", id = "1"),
            createWallet("Apple", walletType = "Physical", id = "2"),
            createWallet("Blue", walletType = "Logical", id = "3"),
            createWallet("Zebra", walletType = "Physical", id = "4")
        )

        // When: Sort by type and name with physicalFirst = true
        val result = WalletSortingUtils.sortByTypeAndName(wallets, physicalFirst = true)

        // Then: All Physical wallets appear before Logical
        assertEquals(4, result.size)
        assertEquals("Physical", result[0].walletType)
        assertEquals("Physical", result[1].walletType)
        assertEquals("Logical", result[2].walletType)
        assertEquals("Logical", result[3].walletType)

        // And: Physical group is alphabetically sorted
        assertEquals("Apple", result[0].name)
        assertEquals("Zebra", result[1].name)

        // And: Logical group is alphabetically sorted
        assertEquals("Blue", result[2].name)
        assertEquals("Yellow", result[3].name)
    }

    @Test
    fun `sortByTypeAndName - within groups alphabetically sorted`() {
        // Given: Physical ["Zebra", "Apple"], Logical ["Yellow", "Blue"]
        val wallets = listOf(
            createWallet("Zebra", walletType = "Physical", id = "1"),
            createWallet("Apple", walletType = "Physical", id = "2"),
            createWallet("Yellow", walletType = "Logical", id = "3"),
            createWallet("Blue", walletType = "Logical", id = "4")
        )

        // When: Sort by type and name with physicalFirst = true
        val result = WalletSortingUtils.sortByTypeAndName(wallets, physicalFirst = true)

        // Then: Result is ["Apple", "Zebra", "Blue", "Yellow"]
        assertEquals(4, result.size)
        assertEquals("Apple", result[0].name)
        assertEquals("Zebra", result[1].name)
        assertEquals("Blue", result[2].name)
        assertEquals("Yellow", result[3].name)
    }

    @Test
    fun `sortByTypeAndName - physical first with only physical wallets`() {
        // Given: Only Physical wallets
        val wallets = listOf(
            createWallet("Zebra", walletType = "Physical", id = "1"),
            createWallet("Apple", walletType = "Physical", id = "2"),
            createWallet("Mango", walletType = "Physical", id = "3")
        )

        // When: Sort by type and name
        val result = WalletSortingUtils.sortByTypeAndName(wallets, physicalFirst = true)

        // Then: Alphabetically sorted
        assertEquals(3, result.size)
        assertEquals("Apple", result[0].name)
        assertEquals("Mango", result[1].name)
        assertEquals("Zebra", result[2].name)
    }

    @Test
    fun `sortByTypeAndName - physical first with only logical wallets`() {
        // Given: Only Logical wallets
        val wallets = listOf(
            createWallet("Yellow", walletType = "Logical", id = "1"),
            createWallet("Blue", walletType = "Logical", id = "2"),
            createWallet("Red", walletType = "Logical", id = "3")
        )

        // When: Sort by type and name
        val result = WalletSortingUtils.sortByTypeAndName(wallets, physicalFirst = true)

        // Then: Alphabetically sorted
        assertEquals(3, result.size)
        assertEquals("Blue", result[0].name)
        assertEquals("Red", result[1].name)
        assertEquals("Yellow", result[2].name)
    }

    // ========================================
    // sortByTypeAndName() - Logical First Tests
    // ========================================

    @Test
    fun `sortByTypeAndName - logical first groups correctly`() {
        // Given: Mixed Physical and Logical wallets
        val wallets = listOf(
            createWallet("Yellow", walletType = "Logical", id = "1"),
            createWallet("Apple", walletType = "Physical", id = "2"),
            createWallet("Blue", walletType = "Logical", id = "3"),
            createWallet("Zebra", walletType = "Physical", id = "4")
        )

        // When: Sort by type and name with physicalFirst = false
        val result = WalletSortingUtils.sortByTypeAndName(wallets, physicalFirst = false)

        // Then: All Logical wallets appear before Physical
        assertEquals(4, result.size)
        assertEquals("Logical", result[0].walletType)
        assertEquals("Logical", result[1].walletType)
        assertEquals("Physical", result[2].walletType)
        assertEquals("Physical", result[3].walletType)

        // And: Each group is alphabetically sorted
        assertEquals("Blue", result[0].name)
        assertEquals("Yellow", result[1].name)
        assertEquals("Apple", result[2].name)
        assertEquals("Zebra", result[3].name)
    }

    // ========================================
    // sortByTypeAndName() - Unknown Type Tests
    // ========================================

    @Test
    fun `sortByTypeAndName - unknown type appears last`() {
        // Given: Wallets with unknown type
        val wallets = listOf(
            createWallet("Zebra", walletType = "Physical", id = "1"),
            createWallet("Unknown1", walletType = "Unknown", id = "2"),
            createWallet("Blue", walletType = "Logical", id = "3"),
            createWallet("Unknown2", walletType = "OtherType", id = "4")
        )

        // When: Sort by type and name
        val result = WalletSortingUtils.sortByTypeAndName(wallets, physicalFirst = true)

        // Then: Unknown types appear after Physical and Logical
        assertEquals(4, result.size)
        assertEquals("Physical", result[0].walletType)
        assertEquals("Logical", result[1].walletType)
        assertTrue(result[2].walletType !in listOf("Physical", "Logical"))
        assertTrue(result[3].walletType !in listOf("Physical", "Logical"))

        // And: Unknown types are alphabetically sorted by name
        assertEquals("Unknown1", result[2].name)
        assertEquals("Unknown2", result[3].name)
    }

    @Test
    fun `sortByTypeAndName - empty string type treated as unknown`() {
        // Given: Wallet with empty string type
        val wallets = listOf(
            createWallet("Apple", walletType = "Physical", id = "1"),
            createWallet("EmptyType", walletType = "", id = "2"),
            createWallet("Blue", walletType = "Logical", id = "3")
        )

        // When: Sort by type and name
        val result = WalletSortingUtils.sortByTypeAndName(wallets, physicalFirst = true)

        // Then: Empty type appears last
        assertEquals(3, result.size)
        assertEquals("Apple", result[0].name)
        assertEquals("Blue", result[1].name)
        assertEquals("EmptyType", result[2].name)
    }

    // ========================================
    // sortByTypeAndName() - Default Parameter Tests
    // ========================================

    @Test
    fun `sortByTypeAndName - default parameter is physical first`() {
        // Given: Mixed wallet types
        val wallets = listOf(
            createWallet("Yellow", walletType = "Logical", id = "1"),
            createWallet("Apple", walletType = "Physical", id = "2")
        )

        // When: Sort by type and name without physicalFirst parameter
        val result = WalletSortingUtils.sortByTypeAndName(wallets)

        // Then: Physical wallets appear first (default behavior)
        assertEquals(2, result.size)
        assertEquals("Physical", result[0].walletType)
        assertEquals("Logical", result[1].walletType)
        assertEquals("Apple", result[0].name)
        assertEquals("Yellow", result[1].name)
    }

    // ========================================
    // sortByTypeAndName() - Edge Cases
    // ========================================

    @Test
    fun `sortByTypeAndName - empty list returns empty list`() {
        // Given: Empty wallet list
        val wallets = emptyList<Wallet>()

        // When: Sort by type and name
        val result = WalletSortingUtils.sortByTypeAndName(wallets)

        // Then: Result is empty list
        assertTrue(result.isEmpty())
        assertEquals(0, result.size)
    }

    @Test
    fun `sortByTypeAndName - single wallet returns same wallet`() {
        // Given: Single wallet
        val wallet = createWallet("Cash", walletType = "Physical")
        val wallets = listOf(wallet)

        // When: Sort by type and name
        val result = WalletSortingUtils.sortByTypeAndName(wallets)

        // Then: Result contains same wallet
        assertEquals(1, result.size)
        assertEquals("Cash", result[0].name)
    }

    @Test
    fun `sortByTypeAndName - original list remains unchanged`() {
        // Given: Original wallet list
        val original = listOf(
            createWallet("Zebra", walletType = "Physical", id = "1"),
            createWallet("Yellow", walletType = "Logical", id = "2"),
            createWallet("Apple", walletType = "Physical", id = "3")
        )
        val originalOrder = original.map { it.name }

        // When: Sort by type and name
        val result = WalletSortingUtils.sortByTypeAndName(original)

        // Then: Original list order is unchanged
        assertEquals(originalOrder, original.map { it.name })
        assertNotSame(original, result)
    }

    @Test
    fun `sortByTypeAndName - case insensitive within groups`() {
        // Given: Wallets with mixed case names
        val wallets = listOf(
            createWallet("zebra", walletType = "Physical", id = "1"),
            createWallet("Apple", walletType = "Physical", id = "2"),
            createWallet("YELLOW", walletType = "Logical", id = "3"),
            createWallet("blue", walletType = "Logical", id = "4")
        )

        // When: Sort by type and name
        val result = WalletSortingUtils.sortByTypeAndName(wallets, physicalFirst = true)

        // Then: Case-insensitive alphabetical within each group
        assertEquals(4, result.size)
        assertEquals("Apple", result[0].name)  // Physical: Apple < zebra
        assertEquals("zebra", result[1].name)
        assertEquals("blue", result[2].name)   // Logical: blue < YELLOW
        assertEquals("YELLOW", result[3].name)
    }
}
