package com.axeven.profiteerapp.data.model

import org.junit.Assert.*
import org.junit.Test

class WalletFilterTest {

    @Test
    fun `AllWallets getDisplayText returns All Wallets`() {
        val filter = WalletFilter.AllWallets

        assertEquals("All Wallets", filter.getDisplayText())
    }

    @Test
    fun `AllWallets equals itself`() {
        val filter1 = WalletFilter.AllWallets
        val filter2 = WalletFilter.AllWallets

        assertEquals(filter1, filter2)
    }

    @Test
    fun `AllWallets toString returns AllWallets`() {
        val filter = WalletFilter.AllWallets

        assertEquals("AllWallets", filter.toString())
    }

    @Test
    fun `SpecificWallet getDisplayText returns wallet name`() {
        val filter = WalletFilter.SpecificWallet(
            walletId = "wallet123",
            walletName = "Cash Wallet"
        )

        assertEquals("Cash Wallet", filter.getDisplayText())
    }

    @Test
    fun `SpecificWallet equals same walletId and walletName`() {
        val filter1 = WalletFilter.SpecificWallet(
            walletId = "wallet123",
            walletName = "Cash Wallet"
        )
        val filter2 = WalletFilter.SpecificWallet(
            walletId = "wallet123",
            walletName = "Cash Wallet"
        )

        assertEquals(filter1, filter2)
    }

    @Test
    fun `SpecificWallet does not equal different walletId`() {
        val filter1 = WalletFilter.SpecificWallet(
            walletId = "wallet123",
            walletName = "Cash Wallet"
        )
        val filter2 = WalletFilter.SpecificWallet(
            walletId = "wallet456",
            walletName = "Cash Wallet"
        )

        assertNotEquals(filter1, filter2)
    }

    @Test
    fun `SpecificWallet does not equal different walletName`() {
        val filter1 = WalletFilter.SpecificWallet(
            walletId = "wallet123",
            walletName = "Cash Wallet"
        )
        val filter2 = WalletFilter.SpecificWallet(
            walletId = "wallet123",
            walletName = "Bank Account"
        )

        assertNotEquals(filter1, filter2)
    }

    @Test
    fun `SpecificWallet toString contains walletId and walletName`() {
        val filter = WalletFilter.SpecificWallet(
            walletId = "wallet123",
            walletName = "Cash Wallet"
        )

        val toString = filter.toString()
        assertTrue(toString.contains("wallet123"))
        assertTrue(toString.contains("Cash Wallet"))
    }

    @Test
    fun `AllWallets does not equal SpecificWallet`() {
        val filter1 = WalletFilter.AllWallets
        val filter2 = WalletFilter.SpecificWallet(
            walletId = "wallet123",
            walletName = "Cash Wallet"
        )

        assertNotEquals(filter1, filter2)
    }

    @Test
    fun `SpecificWallet copy creates new instance with same values`() {
        val original = WalletFilter.SpecificWallet(
            walletId = "wallet123",
            walletName = "Cash Wallet"
        )
        val copied = original.copy()

        assertEquals(original, copied)
        assertNotSame(original, copied)
    }

    @Test
    fun `SpecificWallet copy with new walletId creates different instance`() {
        val original = WalletFilter.SpecificWallet(
            walletId = "wallet123",
            walletName = "Cash Wallet"
        )
        val modified = original.copy(walletId = "wallet456")

        assertEquals("wallet456", modified.walletId)
        assertEquals("Cash Wallet", modified.walletName)
        assertNotEquals(original, modified)
    }

    @Test
    fun `SpecificWallet copy with new walletName creates different instance`() {
        val original = WalletFilter.SpecificWallet(
            walletId = "wallet123",
            walletName = "Cash Wallet"
        )
        val modified = original.copy(walletName = "Bank Account")

        assertEquals("wallet123", modified.walletId)
        assertEquals("Bank Account", modified.walletName)
        assertNotEquals(original, modified)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `SpecificWallet throws exception when walletId is empty`() {
        WalletFilter.SpecificWallet(
            walletId = "",
            walletName = "Cash Wallet"
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `SpecificWallet throws exception when walletId is blank`() {
        WalletFilter.SpecificWallet(
            walletId = "   ",
            walletName = "Cash Wallet"
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `SpecificWallet throws exception when walletName is empty`() {
        WalletFilter.SpecificWallet(
            walletId = "wallet123",
            walletName = ""
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `SpecificWallet throws exception when walletName is blank`() {
        WalletFilter.SpecificWallet(
            walletId = "wallet123",
            walletName = "   "
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `SpecificWallet throws exception when both walletId and walletName are blank`() {
        WalletFilter.SpecificWallet(
            walletId = "",
            walletName = ""
        )
    }

    @Test
    fun `SpecificWallet accepts valid walletId and walletName`() {
        val filter = WalletFilter.SpecificWallet(
            walletId = "wallet123",
            walletName = "Cash Wallet"
        )

        assertEquals("wallet123", filter.walletId)
        assertEquals("Cash Wallet", filter.walletName)
    }

    @Test
    fun `SpecificWallet accepts walletName with special characters`() {
        val filter = WalletFilter.SpecificWallet(
            walletId = "wallet123",
            walletName = "Cash & Savings (USD)"
        )

        assertEquals("Cash & Savings (USD)", filter.walletName)
    }

    @Test
    fun `SpecificWallet hashCode same for equal objects`() {
        val filter1 = WalletFilter.SpecificWallet(
            walletId = "wallet123",
            walletName = "Cash Wallet"
        )
        val filter2 = WalletFilter.SpecificWallet(
            walletId = "wallet123",
            walletName = "Cash Wallet"
        )

        assertEquals(filter1.hashCode(), filter2.hashCode())
    }

    @Test
    fun `AllWallets hashCode is consistent`() {
        val hashCode1 = WalletFilter.AllWallets.hashCode()
        val hashCode2 = WalletFilter.AllWallets.hashCode()

        assertEquals(hashCode1, hashCode2)
    }
}
