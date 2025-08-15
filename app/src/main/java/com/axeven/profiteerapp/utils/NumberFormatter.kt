package com.axeven.profiteerapp.utils

import java.text.NumberFormat
import java.util.Locale

object NumberFormatter {
    
    /**
     * Map of currency codes to their symbols
     */
    private val currencySymbols = mapOf(
        "USD" to "$",
        "EUR" to "€",
        "GBP" to "£",
        "JPY" to "¥",
        "CAD" to "C$",
        "AUD" to "A$",
        "IDR" to "Rp",
        "GOLD" to "Au",
        "BTC" to "₿"
    )
    
    /**
     * Get currency symbol for a given currency code
     * @param currency The currency code (e.g., "USD", "EUR", "GOLD")
     * @return Currency symbol or the currency code if no symbol exists
     */
    fun getCurrencySymbol(currency: String): String {
        return currencySymbols[currency] ?: currency
    }
    
    /**
     * Format large amounts with K/M suffixes to save space
     * @param amount The amount to format
     * @param currency The currency code
     * @return Compact formatted string (e.g., "1.2M", "150K")
     */
    fun formatCompactCurrency(amount: Double, currency: String = ""): String {
        val absAmount = kotlin.math.abs(amount)
        val symbol = getCurrencySymbol(currency)
        
        val (compactAmount, suffix) = when {
            absAmount >= 1_000_000 -> {
                val millions = absAmount / 1_000_000
                if (millions % 1.0 == 0.0) {
                    Pair("${millions.toInt()}", "M")
                } else {
                    Pair(String.format("%.1f", millions), "M")
                }
            }
            absAmount >= 1_000 -> {
                val thousands = absAmount / 1_000
                if (thousands % 1.0 == 0.0) {
                    Pair("${thousands.toInt()}", "K")
                } else {
                    Pair(String.format("%.1f", thousands), "K")
                }
            }
            else -> {
                val formatter = when (currency) {
                    "GOLD" -> {
                        NumberFormat.getNumberInstance(Locale.US).apply {
                            minimumFractionDigits = 1
                            maximumFractionDigits = 3
                        }
                    }
                    "BTC" -> {
                        NumberFormat.getNumberInstance(Locale.US).apply {
                            minimumFractionDigits = 4
                            maximumFractionDigits = 8
                        }
                    }
                    else -> {
                        NumberFormat.getNumberInstance(Locale.US).apply {
                            minimumFractionDigits = if (absAmount % 1.0 == 0.0) 0 else 2
                            maximumFractionDigits = 2
                        }
                    }
                }
                Pair(formatter.format(absAmount), "")
            }
        }
        
        return if (currency.isNotEmpty()) {
            "$symbol$compactAmount$suffix"
        } else {
            "$compactAmount$suffix"
        }
    }
    
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
            "${getCurrencySymbol(currency)} $formattedAmount"
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