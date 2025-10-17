package com.axeven.profiteerapp.utils

import com.axeven.profiteerapp.data.model.Wallet
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Utility class for detecting and calculating balance discrepancies between
 * Physical and Logical wallets.
 *
 * The fundamental business rule: Sum of Physical wallet balances must equal
 * Sum of Logical wallet balances at all times.
 */
@Singleton
class BalanceDiscrepancyDetector @Inject constructor() {

    /**
     * Calculates the total balance across all Physical wallets.
     * All wallets use the same currency (defaultCurrency), so no conversion needed.
     *
     * @param wallets List of all wallets (Physical and Logical mixed)
     * @return Total balance of Physical wallets only
     */
    fun calculateTotalPhysicalBalance(wallets: List<Wallet>): Double {
        return wallets
            .filter { it.isPhysical }
            .sumOf { it.balance }
    }

    /**
     * Calculates the total balance across all Logical wallets.
     * All wallets use the same currency (defaultCurrency), so no conversion needed.
     *
     * @param wallets List of all wallets (Physical and Logical mixed)
     * @return Total balance of Logical wallets only
     */
    fun calculateTotalLogicalBalance(wallets: List<Wallet>): Double {
        return wallets
            .filter { it.isLogical }
            .sumOf { it.balance }
    }

    /**
     * Checks if there is a discrepancy between Physical and Logical wallet totals.
     *
     * @param physicalTotal Total balance of all Physical wallets
     * @param logicalTotal Total balance of all Logical wallets
     * @param tolerance Acceptable difference due to floating-point precision (default: 0.01)
     * @return true if discrepancy exists (difference exceeds tolerance), false otherwise
     */
    fun hasDiscrepancy(
        physicalTotal: Double,
        logicalTotal: Double,
        tolerance: Double = 0.01
    ): Boolean {
        return abs(physicalTotal - logicalTotal) > tolerance
    }

    /**
     * Calculates the amount of discrepancy between Physical and Logical totals.
     *
     * @param physicalTotal Total balance of all Physical wallets
     * @param logicalTotal Total balance of all Logical wallets
     * @return Positive if Physical > Logical, Negative if Logical > Physical, 0 if balanced
     */
    fun getDiscrepancyAmount(
        physicalTotal: Double,
        logicalTotal: Double
    ): Double {
        return physicalTotal - logicalTotal
    }
}
