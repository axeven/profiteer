package com.axeven.profiteerapp.utils

import java.text.NumberFormat
import java.util.Locale

object NumberFormatter {
    
    /**
     * Comprehensive currency symbol mapping with support for special currencies
     */
    private val currencySymbols = mapOf(
        "USD" to "$",
        "EUR" to "€",
        "GBP" to "£",
        "JPY" to "¥",
        "CAD" to "C$",
        "AUD" to "A$",
        "IDR" to "Rp",
        "GOLD" to "g",  // grams for gold weight
        "BTC" to "₿"
    )
    
    /**
     * Get the appropriate decimal places for each currency type
     */
    private fun getDecimalPlaces(currencyCode: String): Int {
        return when (currencyCode) {
            "BTC" -> 8        // Bitcoin uses up to 8 decimal places
            "GOLD" -> 3       // Gold weight in grams uses 3 decimal places
            "JPY" -> 0        // Japanese Yen doesn't use decimal places
            else -> 2         // Standard currencies use 2 decimal places
        }
    }
    
    /**
     * Format currency with proper symbol placement based on currency type
     */
    private fun formatWithSymbol(amount: Double, currencyCode: String): String {
        val symbol = getCurrencySymbol(currencyCode)
        val decimalPlaces = getDecimalPlaces(currencyCode)
        
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = when (currencyCode) {
                "BTC" -> 4      // Show minimum 4 decimal places for Bitcoin
                "GOLD" -> 1     // Show minimum 1 decimal place for gold
                "JPY" -> 0      // No decimals for Japanese Yen
                else -> 2       // Standard 2 decimal places
            }
            maximumFractionDigits = decimalPlaces
        }
        
        val formattedAmount = formatter.format(amount)
        
        return when (currencyCode) {
            "GOLD" -> "$formattedAmount $symbol"  // "100.500 g"
            else -> "$symbol$formattedAmount"      // "$100.50", "€100.50", "₿0.00123456"
        }
    }
    
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
     * @return Compact formatted string (e.g., "$1.2M", "€150K", "100.5K g")
     */
    fun formatCompactCurrency(amount: Double, currency: String = "USD"): String {
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
                val decimalPlaces = getDecimalPlaces(currency)
                val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
                    minimumFractionDigits = when (currency) {
                        "BTC" -> 4
                        "GOLD" -> 1
                        "JPY" -> 0
                        else -> if (absAmount % 1.0 == 0.0) 0 else 2
                    }
                    maximumFractionDigits = decimalPlaces
                }
                Pair(formatter.format(absAmount), "")
            }
        }
        
        return if (currency.isNotEmpty()) {
            when (currency) {
                "GOLD" -> "$compactAmount$suffix $symbol"  // "100K g"
                else -> "$symbol$compactAmount$suffix"      // "$1M", "€150K"
            }
        } else {
            "$compactAmount$suffix"
        }
    }
    
    /**
     * Formats a double value as currency with thousands separators and proper symbol placement
     * @param amount The amount to format
     * @param currency The currency code (e.g., "USD", "EUR", "GOLD", "BTC")
     * @param showSymbol Whether to show the currency symbol
     * @param locale The locale for number formatting (defaults to US)
     * @return Formatted string with thousands separators and proper currency symbol
     */
    fun formatCurrency(
        amount: Double, 
        currency: String = "USD", 
        showSymbol: Boolean = false,
        locale: Locale = Locale.US
    ): String {
        return if (showSymbol && currency.isNotEmpty()) {
            formatWithSymbol(amount, currency)
        } else {
            // Format without symbol using appropriate decimal places
            val decimalPlaces = getDecimalPlaces(currency)
            val formatter = NumberFormat.getNumberInstance(locale).apply {
                minimumFractionDigits = when (currency) {
                    "BTC" -> 4      // Bitcoin minimum 4 decimal places
                    "GOLD" -> 1     // Gold minimum 1 decimal place  
                    "JPY" -> 0      // Japanese Yen no decimals
                    else -> 2       // Standard minimum 2 decimal places
                }
                maximumFractionDigits = decimalPlaces
            }
            formatter.format(amount)
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