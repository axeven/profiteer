package com.axeven.profiteerapp.utils

import com.axeven.profiteerapp.data.constants.WalletType
import com.axeven.profiteerapp.data.model.PhysicalForm
import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.data.model.WalletFilter
import org.junit.Assert.*
import org.junit.Test
import java.util.Date

class WalletFilterUtilsTest {

    // ============ Transaction Filtering Tests ============

    @Test
    fun `filterTransactionsByWallet with AllWallets returns all transactions`() {
        val transactions = listOf(
            createTransaction(id = "t1", affectedWalletIds = listOf("w1")),
            createTransaction(id = "t2", affectedWalletIds = listOf("w2")),
            createTransaction(id = "t3", affectedWalletIds = listOf("w1", "w2"))
        )

        val result = WalletFilterUtils.filterTransactionsByWallet(
            transactions = transactions,
            filter = WalletFilter.AllWallets
        )

        assertEquals(3, result.size)
        assertEquals(transactions, result)
    }

    @Test
    fun `filterTransactionsByWallet with SpecificWallet filters by affectedWalletIds`() {
        val transactions = listOf(
            createTransaction(id = "t1", affectedWalletIds = listOf("w1")),
            createTransaction(id = "t2", affectedWalletIds = listOf("w2")),
            createTransaction(id = "t3", affectedWalletIds = listOf("w1", "w2"))
        )

        val result = WalletFilterUtils.filterTransactionsByWallet(
            transactions = transactions,
            filter = WalletFilter.SpecificWallet(walletId = "w1", walletName = "Wallet 1")
        )

        assertEquals(2, result.size)
        assertTrue(result.any { it.id == "t1" })
        assertTrue(result.any { it.id == "t3" })
        assertFalse(result.any { it.id == "t2" })
    }

    @Test
    fun `filterTransactionsByWallet with SpecificWallet returns only matching transactions`() {
        val transactions = listOf(
            createTransaction(id = "t1", affectedWalletIds = listOf("w1")),
            createTransaction(id = "t2", affectedWalletIds = listOf("w2")),
            createTransaction(id = "t3", affectedWalletIds = listOf("w3"))
        )

        val result = WalletFilterUtils.filterTransactionsByWallet(
            transactions = transactions,
            filter = WalletFilter.SpecificWallet(walletId = "w2", walletName = "Wallet 2")
        )

        assertEquals(1, result.size)
        assertEquals("t2", result[0].id)
    }

    @Test
    fun `filterTransactionsByWallet with empty transaction list returns empty list`() {
        val result = WalletFilterUtils.filterTransactionsByWallet(
            transactions = emptyList(),
            filter = WalletFilter.AllWallets
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `filterTransactionsByWallet with SpecificWallet and empty list returns empty`() {
        val result = WalletFilterUtils.filterTransactionsByWallet(
            transactions = emptyList(),
            filter = WalletFilter.SpecificWallet(walletId = "w1", walletName = "Wallet 1")
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `filterTransactionsByWallet excludes transactions with empty affectedWalletIds`() {
        val transactions = listOf(
            createTransaction(id = "t1", affectedWalletIds = listOf("w1")),
            createTransaction(id = "t2", affectedWalletIds = emptyList()),
            createTransaction(id = "t3", affectedWalletIds = listOf("w1"))
        )

        val result = WalletFilterUtils.filterTransactionsByWallet(
            transactions = transactions,
            filter = WalletFilter.SpecificWallet(walletId = "w1", walletName = "Wallet 1")
        )

        assertEquals(2, result.size)
        assertTrue(result.any { it.id == "t1" })
        assertTrue(result.any { it.id == "t3" })
        assertFalse(result.any { it.id == "t2" })
    }

    @Test
    fun `filterTransactionsByWallet with wallet not in affectedWalletIds returns empty`() {
        val transactions = listOf(
            createTransaction(id = "t1", affectedWalletIds = listOf("w1")),
            createTransaction(id = "t2", affectedWalletIds = listOf("w2")),
            createTransaction(id = "t3", affectedWalletIds = listOf("w3"))
        )

        val result = WalletFilterUtils.filterTransactionsByWallet(
            transactions = transactions,
            filter = WalletFilter.SpecificWallet(walletId = "w999", walletName = "Non-existent Wallet")
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `filterTransactionsByWallet with multiple wallets in affectedWalletIds matches correctly`() {
        val transactions = listOf(
            createTransaction(id = "t1", affectedWalletIds = listOf("w1", "w2", "w3")),
            createTransaction(id = "t2", affectedWalletIds = listOf("w4", "w5")),
            createTransaction(id = "t3", affectedWalletIds = listOf("w2", "w4"))
        )

        val result = WalletFilterUtils.filterTransactionsByWallet(
            transactions = transactions,
            filter = WalletFilter.SpecificWallet(walletId = "w2", walletName = "Wallet 2")
        )

        assertEquals(2, result.size)
        assertTrue(result.any { it.id == "t1" })
        assertTrue(result.any { it.id == "t3" })
        assertFalse(result.any { it.id == "t2" })
    }

    // ============ Wallet Filtering Tests ============

    @Test
    fun `filterWalletsByWalletFilter with AllWallets returns all wallets`() {
        val wallets = listOf(
            createWallet(id = "w1", name = "Wallet 1"),
            createWallet(id = "w2", name = "Wallet 2"),
            createWallet(id = "w3", name = "Wallet 3")
        )

        val result = WalletFilterUtils.filterWalletsByWalletFilter(
            wallets = wallets,
            filter = WalletFilter.AllWallets
        )

        assertEquals(3, result.size)
        assertEquals(wallets, result)
    }

    @Test
    fun `filterWalletsByWalletFilter with SpecificWallet returns only matching wallet`() {
        val wallets = listOf(
            createWallet(id = "w1", name = "Wallet 1"),
            createWallet(id = "w2", name = "Wallet 2"),
            createWallet(id = "w3", name = "Wallet 3")
        )

        val result = WalletFilterUtils.filterWalletsByWalletFilter(
            wallets = wallets,
            filter = WalletFilter.SpecificWallet(walletId = "w2", walletName = "Wallet 2")
        )

        assertEquals(1, result.size)
        assertEquals("w2", result[0].id)
        assertEquals("Wallet 2", result[0].name)
    }

    @Test
    fun `filterWalletsByWalletFilter with empty wallet list returns empty list`() {
        val result = WalletFilterUtils.filterWalletsByWalletFilter(
            wallets = emptyList(),
            filter = WalletFilter.AllWallets
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `filterWalletsByWalletFilter with SpecificWallet and empty list returns empty`() {
        val result = WalletFilterUtils.filterWalletsByWalletFilter(
            wallets = emptyList(),
            filter = WalletFilter.SpecificWallet(walletId = "w1", walletName = "Wallet 1")
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `filterWalletsByWalletFilter with wallet not in list returns empty`() {
        val wallets = listOf(
            createWallet(id = "w1", name = "Wallet 1"),
            createWallet(id = "w2", name = "Wallet 2")
        )

        val result = WalletFilterUtils.filterWalletsByWalletFilter(
            wallets = wallets,
            filter = WalletFilter.SpecificWallet(walletId = "w999", walletName = "Non-existent Wallet")
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `filterWalletsByWalletFilter with SpecificWallet returns single wallet from many`() {
        val wallets = listOf(
            createWallet(id = "w1", name = "Cash"),
            createWallet(id = "w2", name = "Bank Account"),
            createWallet(id = "w3", name = "Savings"),
            createWallet(id = "w4", name = "Investment")
        )

        val result = WalletFilterUtils.filterWalletsByWalletFilter(
            wallets = wallets,
            filter = WalletFilter.SpecificWallet(walletId = "w3", walletName = "Savings")
        )

        assertEquals(1, result.size)
        assertEquals("w3", result[0].id)
        assertEquals("Savings", result[0].name)
    }

    @Test
    fun `filterWalletsByWalletFilter preserves wallet properties`() {
        val wallets = listOf(
            createWallet(
                id = "w1",
                name = "Test Wallet",
                balance = 1000.0,
                walletType = WalletType.PHYSICAL.displayName
            )
        )

        val result = WalletFilterUtils.filterWalletsByWalletFilter(
            wallets = wallets,
            filter = WalletFilter.SpecificWallet(walletId = "w1", walletName = "Test Wallet")
        )

        assertEquals(1, result.size)
        assertEquals(1000.0, result[0].balance, 0.001)
        assertEquals(WalletType.PHYSICAL.displayName, result[0].walletType)
    }

    // ============ Helper Functions ============

    private fun createTransaction(
        id: String = "transaction1",
        title: String = "Test Transaction",
        amount: Double = 100.0,
        type: TransactionType = TransactionType.EXPENSE,
        affectedWalletIds: List<String> = emptyList()
    ): Transaction {
        return Transaction(
            id = id,
            title = title,
            amount = amount,
            type = type,
            affectedWalletIds = affectedWalletIds,
            transactionDate = Date(),
            userId = "user123"
        )
    }

    private fun createWallet(
        id: String = "wallet1",
        name: String = "Test Wallet",
        balance: Double = 0.0,
        walletType: String = WalletType.PHYSICAL.displayName
    ): Wallet {
        return Wallet(
            id = id,
            name = name,
            balance = balance,
            walletType = walletType,
            physicalForm = PhysicalForm.FIAT_CURRENCY,
            userId = "user123"
        )
    }
}
