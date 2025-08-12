package com.axeven.profiteerapp.utils

import java.text.NumberFormat
import java.util.Locale

object NumberFormatter {
    
    /**
     * Formats a double value as currency with thousands separators
     * @param amount The amount to format
     * @param currency The currency symbol (e.g., "USD", "EUR", "GOLD")
     * @param showSymbol Whether to show the currency symbol
     * @return Formatted string with thousands separators (e.g., "1,234.56")
     */
    fun formatCurrency(
        amount: Double, 
        currency: String = "", 
        showSymbol: Boolean = false
    ): String {
        val formatter = when (currency) {
            "GOLD" -> {
                // Gold uses more precision for weight (grams)
                NumberFormat.getNumberInstance(Locale.US).apply {
                    minimumFractionDigits = 1
                    maximumFractionDigits = 3
                }
            }
            "BTC" -> {
                // Bitcoin uses high precision (up to 8 decimal places)
                NumberFormat.getNumberInstance(Locale.US).apply {
                    minimumFractionDigits = 4
                    maximumFractionDigits = 8
                }
            }
            else -> {
                NumberFormat.getNumberInstance(Locale.US).apply {
                    minimumFractionDigits = 2
                    maximumFractionDigits = 2
                }
            }
        }
        
        val formattedAmount = formatter.format(amount)
        
        return if (showSymbol && currency.isNotEmpty()) {
            "$currency $formattedAmount"
        } else {
            formattedAmount
        }
    }
    
    /**
     * Formats a double value with thousands separators (no decimal places for whole numbers)
     * @param amount The amount to format
     * @return Formatted string (e.g., "1,234" or "1,234.56")
     */
    fun formatAmount(amount: Double): String {
        return if (amount % 1.0 == 0.0) {
            // Whole number, no decimal places
            NumberFormat.getNumberInstance(Locale.US).apply {
                minimumFractionDigits = 0
                maximumFractionDigits = 0
            }.format(amount)
        } else {
            // Has decimal places
            NumberFormat.getNumberInstance(Locale.US).apply {
                minimumFractionDigits = 2
                maximumFractionDigits = 2
            }.format(amount)
        }
    }
    
    /**
     * Parses a formatted string back to Double, handling thousands separators
     * @param formattedNumber The formatted number string (e.g., "1,234.56")
     * @return Double value or null if parsing fails
     */
    fun parseDouble(formattedNumber: String): Double? {
        return try {
            // Remove commas and parse
            formattedNumber.replace(",", "").toDoubleOrNull()
        } catch (e: Exception) {
            null
        }
    }
}