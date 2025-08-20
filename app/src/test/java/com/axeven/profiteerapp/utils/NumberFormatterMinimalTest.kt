package com.axeven.profiteerapp.utils

import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals

/**
 * Minimal test focused on the core bug fix: currency symbols display correctly
 * This validates that PRP-13 wallet balance currency display bug is fixed
 */
class NumberFormatterMinimalTest {

    @Test
    fun testCurrencySymbolMapping() {
        // Test that getCurrencySymbol returns correct symbols
        assertEquals("$", NumberFormatter.getCurrencySymbol("USD"))
        assertEquals("€", NumberFormatter.getCurrencySymbol("EUR"))
        assertEquals("£", NumberFormatter.getCurrencySymbol("GBP"))
        assertEquals("¥", NumberFormatter.getCurrencySymbol("JPY"))
        assertEquals("g", NumberFormatter.getCurrencySymbol("GOLD"))
        assertEquals("₿", NumberFormatter.getCurrencySymbol("BTC"))
    }

    @Test
    fun testMainBugFix_CurrencySymbolsShowCorrectly() {
        val amount = 100.0
        
        // The main bug was: all currencies showed "$" regardless of parameter
        // Now each currency should show its correct symbol
        
        val usdResult = NumberFormatter.formatCurrency(amount, "USD", showSymbol = true)
        assertTrue("USD should contain $", usdResult.contains("$"))
        
        val eurResult = NumberFormatter.formatCurrency(amount, "EUR", showSymbol = true)
        assertTrue("EUR should contain €", eurResult.contains("€"))
        assertTrue("EUR should not contain $", !eurResult.contains("$"))
        
        val gbpResult = NumberFormatter.formatCurrency(amount, "GBP", showSymbol = true)
        assertTrue("GBP should contain £", gbpResult.contains("£"))
        assertTrue("GBP should not contain $", !gbpResult.contains("$"))
        
        val jpyResult = NumberFormatter.formatCurrency(amount, "JPY", showSymbol = true)
        assertTrue("JPY should contain ¥", jpyResult.contains("¥"))
        assertTrue("JPY should not contain $", !jpyResult.contains("$"))
        
        val btcResult = NumberFormatter.formatCurrency(amount, "BTC", showSymbol = true)
        assertTrue("BTC should contain ₿", btcResult.contains("₿"))
        assertTrue("BTC should not contain $", !btcResult.contains("$"))
        
        val goldResult = NumberFormatter.formatCurrency(amount, "GOLD", showSymbol = true)
        assertTrue("GOLD should contain g", goldResult.contains("g"))
        assertTrue("GOLD should not contain $", !goldResult.contains("$"))
    }

    @Test
    fun testWithoutSymbol_NoSymbolsShown() {
        val amount = 100.0
        
        // When showSymbol = false, no currency symbols should appear
        val usdResult = NumberFormatter.formatCurrency(amount, "USD", showSymbol = false)
        assertTrue("USD without symbol should not contain $", !usdResult.contains("$"))
        
        val eurResult = NumberFormatter.formatCurrency(amount, "EUR", showSymbol = false)
        assertTrue("EUR without symbol should not contain €", !eurResult.contains("€"))
        assertTrue("EUR without symbol should not contain $", !eurResult.contains("$"))
    }

    @Test
    fun testCompactCurrency_CorrectSymbols() {
        val amount = 1500000.0 // 1.5M
        
        val usdResult = NumberFormatter.formatCompactCurrency(amount, "USD")
        assertTrue("Compact USD should contain $", usdResult.contains("$"))
        
        val eurResult = NumberFormatter.formatCompactCurrency(amount, "EUR")
        assertTrue("Compact EUR should contain €", eurResult.contains("€"))
        assertTrue("Compact EUR should not contain $", !eurResult.contains("$"))
    }
    
    @Test
    fun testParseDouble_BasicFunctionality() {
        // Just verify parseDouble works for basic cases
        assertEquals(1234.56, NumberFormatter.parseDouble("1,234.56")!!, 0.001)
        assertEquals(null, NumberFormatter.parseDouble("invalid"))
    }
}