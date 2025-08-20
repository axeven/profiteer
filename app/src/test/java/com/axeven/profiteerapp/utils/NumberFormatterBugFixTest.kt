package com.axeven.profiteerapp.utils

import org.junit.Test
import org.junit.Assert.assertTrue

/**
 * Focused test for the specific bug fix in PRP-13: wallet balance currency display
 * Main goal: Ensure currency symbols match the currency parameter (no hardcoded "$")
 */
class NumberFormatterBugFixTest {

    @Test
    fun testBugFix_CurrencySymbolsMatchCurrencyParameter() {
        // The main bug: formatCurrency was showing "$" regardless of currency parameter
        // Test that different currencies show their correct symbols
        
        val amount = 100.0
        
        // Test that non-USD currencies don't show "$"
        val eurResult = NumberFormatter.formatCurrency(amount, "EUR", showSymbol = true)
        assertTrue("EUR currency should contain € symbol, got: $eurResult", eurResult.contains("€"))
        assertTrue("EUR currency should not contain $ symbol, got: $eurResult", !eurResult.contains("$"))
        
        val gbpResult = NumberFormatter.formatCurrency(amount, "GBP", showSymbol = true)
        assertTrue("GBP currency should contain £ symbol, got: $gbpResult", gbpResult.contains("£"))
        assertTrue("GBP currency should not contain $ symbol, got: $gbpResult", !gbpResult.contains("$"))
        
        val jpyResult = NumberFormatter.formatCurrency(amount, "JPY", showSymbol = true)
        assertTrue("JPY currency should contain ¥ symbol, got: $jpyResult", jpyResult.contains("¥"))
        assertTrue("JPY currency should not contain $ symbol, got: $jpyResult", !jpyResult.contains("$"))
        
        // Test that USD still shows "$"
        val usdResult = NumberFormatter.formatCurrency(amount, "USD", showSymbol = true)
        assertTrue("USD currency should contain $ symbol, got: $usdResult", usdResult.contains("$"))
        
        // Test special currencies
        val btcResult = NumberFormatter.formatCurrency(amount, "BTC", showSymbol = true)
        assertTrue("BTC currency should contain ₿ symbol, got: $btcResult", btcResult.contains("₿"))
        assertTrue("BTC currency should not contain $ symbol, got: $btcResult", !btcResult.contains("$"))
        
        val goldResult = NumberFormatter.formatCurrency(amount, "GOLD", showSymbol = true)
        assertTrue("GOLD currency should contain g symbol, got: $goldResult", goldResult.contains("g"))
        assertTrue("GOLD currency should not contain $ symbol, got: $goldResult", !goldResult.contains("$"))
    }

    @Test
    fun testBugFix_CompactCurrencySymbolsMatchCurrencyParameter() {
        // Test that compact formatting also respects currency symbols
        val amount = 150000.0 // Will format as 150K
        
        val eurResult = NumberFormatter.formatCompactCurrency(amount, "EUR")
        assertTrue("Compact EUR should contain € symbol, got: $eurResult", eurResult.contains("€"))
        assertTrue("Compact EUR should not contain $ symbol, got: $eurResult", !eurResult.contains("$"))
        
        val usdResult = NumberFormatter.formatCompactCurrency(amount, "USD") 
        assertTrue("Compact USD should contain $ symbol, got: $usdResult", usdResult.contains("$"))
    }

    @Test
    fun testBugFix_WithoutSymbolDoesNotShowCurrency() {
        // When showSymbol = false, should not show any currency symbols
        val amount = 100.0
        
        val eurResult = NumberFormatter.formatCurrency(amount, "EUR", showSymbol = false)
        assertTrue("EUR without symbol should not contain €, got: $eurResult", !eurResult.contains("€"))
        assertTrue("EUR without symbol should not contain $, got: $eurResult", !eurResult.contains("$"))
        
        val usdResult = NumberFormatter.formatCurrency(amount, "USD", showSymbol = false)
        assertTrue("USD without symbol should not contain $, got: $usdResult", !usdResult.contains("$"))
    }
}