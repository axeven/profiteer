package com.axeven.profiteerapp.utils

import com.axeven.profiteerapp.data.model.PhysicalForm
import com.axeven.profiteerapp.data.model.Wallet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for BalanceDiscrepancyDetector.
 * Tests the core balance calculation and discrepancy detection logic.
 */
class BalanceDiscrepancyDetectorTest {

    private lateinit var detector: BalanceDiscrepancyDetector

    @Before
    fun setup() {
        detector = BalanceDiscrepancyDetector()
    }

    // Tests for calculateTotalPhysicalBalance()

    @Test
    fun `calculateTotalPhysicalBalance with single wallet returns correct balance`() {
        val wallets = listOf(
            createWallet(id = "1", balance = 1000.0, walletType = "Physical")
        )

        val total = detector.calculateTotalPhysicalBalance(wallets)

        assertEquals(1000.0, total, 0.001)
    }

    @Test
    fun `calculateTotalPhysicalBalance with multiple wallets returns sum`() {
        val wallets = listOf(
            createWallet(id = "1", balance = 1000.0, walletType = "Physical"),
            createWallet(id = "2", balance = 2500.0, walletType = "Physical"),
            createWallet(id = "3", balance = 500.0, walletType = "Physical")
        )

        val total = detector.calculateTotalPhysicalBalance(wallets)

        assertEquals(4000.0, total, 0.001)
    }

    @Test
    fun `calculateTotalPhysicalBalance with empty list returns zero`() {
        val wallets = emptyList<Wallet>()

        val total = detector.calculateTotalPhysicalBalance(wallets)

        assertEquals(0.0, total, 0.001)
    }

    @Test
    fun `calculateTotalPhysicalBalance with zero balance wallets returns zero`() {
        val wallets = listOf(
            createWallet(id = "1", balance = 0.0, walletType = "Physical"),
            createWallet(id = "2", balance = 0.0, walletType = "Physical")
        )

        val total = detector.calculateTotalPhysicalBalance(wallets)

        assertEquals(0.0, total, 0.001)
    }

    @Test
    fun `calculateTotalPhysicalBalance ignores logical wallets`() {
        val wallets = listOf(
            createWallet(id = "1", balance = 1000.0, walletType = "Physical"),
            createWallet(id = "2", balance = 2000.0, walletType = "Logical"),
            createWallet(id = "3", balance = 500.0, walletType = "Physical")
        )

        val total = detector.calculateTotalPhysicalBalance(wallets)

        assertEquals(1500.0, total, 0.001)
    }

    @Test
    fun `calculateTotalPhysicalBalance with negative balances`() {
        val wallets = listOf(
            createWallet(id = "1", balance = 1000.0, walletType = "Physical"),
            createWallet(id = "2", balance = -200.0, walletType = "Physical")
        )

        val total = detector.calculateTotalPhysicalBalance(wallets)

        assertEquals(800.0, total, 0.001)
    }

    // Tests for calculateTotalLogicalBalance()

    @Test
    fun `calculateTotalLogicalBalance with single wallet returns correct balance`() {
        val wallets = listOf(
            createWallet(id = "1", balance = 1500.0, walletType = "Logical")
        )

        val total = detector.calculateTotalLogicalBalance(wallets)

        assertEquals(1500.0, total, 0.001)
    }

    @Test
    fun `calculateTotalLogicalBalance with multiple wallets returns sum`() {
        val wallets = listOf(
            createWallet(id = "1", balance = 1000.0, walletType = "Logical"),
            createWallet(id = "2", balance = 2500.0, walletType = "Logical"),
            createWallet(id = "3", balance = 500.0, walletType = "Logical")
        )

        val total = detector.calculateTotalLogicalBalance(wallets)

        assertEquals(4000.0, total, 0.001)
    }

    @Test
    fun `calculateTotalLogicalBalance with empty list returns zero`() {
        val wallets = emptyList<Wallet>()

        val total = detector.calculateTotalLogicalBalance(wallets)

        assertEquals(0.0, total, 0.001)
    }

    @Test
    fun `calculateTotalLogicalBalance with zero balance wallets returns zero`() {
        val wallets = listOf(
            createWallet(id = "1", balance = 0.0, walletType = "Logical"),
            createWallet(id = "2", balance = 0.0, walletType = "Logical")
        )

        val total = detector.calculateTotalLogicalBalance(wallets)

        assertEquals(0.0, total, 0.001)
    }

    @Test
    fun `calculateTotalLogicalBalance ignores physical wallets`() {
        val wallets = listOf(
            createWallet(id = "1", balance = 1000.0, walletType = "Physical"),
            createWallet(id = "2", balance = 2000.0, walletType = "Logical"),
            createWallet(id = "3", balance = 500.0, walletType = "Logical")
        )

        val total = detector.calculateTotalLogicalBalance(wallets)

        assertEquals(2500.0, total, 0.001)
    }

    @Test
    fun `calculateTotalLogicalBalance with negative balances`() {
        val wallets = listOf(
            createWallet(id = "1", balance = 1000.0, walletType = "Logical"),
            createWallet(id = "2", balance = -200.0, walletType = "Logical")
        )

        val total = detector.calculateTotalLogicalBalance(wallets)

        assertEquals(800.0, total, 0.001)
    }

    // Tests for hasDiscrepancy()

    @Test
    fun `hasDiscrepancy returns false when balances match exactly`() {
        val hasDiscrepancy = detector.hasDiscrepancy(
            physicalTotal = 1000.0,
            logicalTotal = 1000.0
        )

        assertFalse(hasDiscrepancy)
    }

    @Test
    fun `hasDiscrepancy returns true when balances differ`() {
        val hasDiscrepancy = detector.hasDiscrepancy(
            physicalTotal = 1000.0,
            logicalTotal = 950.0
        )

        assertTrue(hasDiscrepancy)
    }

    @Test
    fun `hasDiscrepancy returns false within tolerance`() {
        val hasDiscrepancy = detector.hasDiscrepancy(
            physicalTotal = 1000.0,
            logicalTotal = 1000.005, // Within 0.01 tolerance
            tolerance = 0.01
        )

        assertFalse(hasDiscrepancy)
    }

    @Test
    fun `hasDiscrepancy returns true outside tolerance`() {
        val hasDiscrepancy = detector.hasDiscrepancy(
            physicalTotal = 1000.0,
            logicalTotal = 1000.02, // Outside 0.01 tolerance
            tolerance = 0.01
        )

        assertTrue(hasDiscrepancy)
    }

    @Test
    fun `hasDiscrepancy with custom tolerance`() {
        val hasDiscrepancy = detector.hasDiscrepancy(
            physicalTotal = 1000.0,
            logicalTotal = 1001.0, // Within 2.0 tolerance
            tolerance = 2.0
        )

        assertFalse(hasDiscrepancy)
    }

    @Test
    fun `hasDiscrepancy with zero balances`() {
        val hasDiscrepancy = detector.hasDiscrepancy(
            physicalTotal = 0.0,
            logicalTotal = 0.0
        )

        assertFalse(hasDiscrepancy)
    }

    @Test
    fun `hasDiscrepancy with negative balances`() {
        val hasDiscrepancy = detector.hasDiscrepancy(
            physicalTotal = -100.0,
            logicalTotal = -100.0
        )

        assertFalse(hasDiscrepancy)
    }

    // Tests for getDiscrepancyAmount()

    @Test
    fun `getDiscrepancyAmount returns positive when physical exceeds logical`() {
        val discrepancy = detector.getDiscrepancyAmount(
            physicalTotal = 1000.0,
            logicalTotal = 900.0
        )

        assertEquals(100.0, discrepancy, 0.001)
    }

    @Test
    fun `getDiscrepancyAmount returns negative when logical exceeds physical`() {
        val discrepancy = detector.getDiscrepancyAmount(
            physicalTotal = 900.0,
            logicalTotal = 1000.0
        )

        assertEquals(-100.0, discrepancy, 0.001)
    }

    @Test
    fun `getDiscrepancyAmount returns zero when balances match`() {
        val discrepancy = detector.getDiscrepancyAmount(
            physicalTotal = 1000.0,
            logicalTotal = 1000.0
        )

        assertEquals(0.0, discrepancy, 0.001)
    }

    @Test
    fun `getDiscrepancyAmount with zero balances`() {
        val discrepancy = detector.getDiscrepancyAmount(
            physicalTotal = 0.0,
            logicalTotal = 0.0
        )

        assertEquals(0.0, discrepancy, 0.001)
    }

    @Test
    fun `getDiscrepancyAmount with negative balances`() {
        val discrepancy = detector.getDiscrepancyAmount(
            physicalTotal = -100.0,
            logicalTotal = -200.0
        )

        assertEquals(100.0, discrepancy, 0.001)
    }

    @Test
    fun `getDiscrepancyAmount with large values`() {
        val discrepancy = detector.getDiscrepancyAmount(
            physicalTotal = 1000000.0,
            logicalTotal = 999500.0
        )

        assertEquals(500.0, discrepancy, 0.001)
    }

    // Helper functions

    private fun createWallet(
        id: String,
        name: String = "Test Wallet",
        balance: Double = 0.0,
        initialBalance: Double = 0.0,
        walletType: String = "Physical",
        physicalForm: PhysicalForm = PhysicalForm.FIAT_CURRENCY,
        userId: String = "user123"
    ): Wallet {
        return Wallet(
            id = id,
            name = name,
            balance = balance,
            initialBalance = initialBalance,
            walletType = walletType,
            physicalForm = physicalForm,
            userId = userId
        )
    }
}
