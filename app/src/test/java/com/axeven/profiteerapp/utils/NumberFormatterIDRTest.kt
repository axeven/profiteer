package com.axeven.profiteerapp.utils

import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals

/**
 * Specific test for IDR currency formatting to verify user's reported issue
 * Tests that IDR currency shows "Rp" symbol instead of "$"
 */
class NumberFormatterIDRTest {

    @Test
    fun testIDRCurrencySymbol() {
        // Test that getCurrencySymbol returns "Rp" for IDR
        assertEquals("Rp", NumberFormatter.getCurrencySymbol("IDR"))
    }

    @Test
    fun testIDRCurrencyFormatting() {
        val amount = 100000.0 // 100,000 IDR (typical amount)
        
        // Test with symbol
        val resultWithSymbol = NumberFormatter.formatCurrency(amount, "IDR", showSymbol = true)
        assertTrue("IDR currency should contain Rp symbol, got: $resultWithSymbol", resultWithSymbol.contains("Rp"))
        assertTrue("IDR currency should not contain $ symbol, got: $resultWithSymbol", !resultWithSymbol.contains("$"))
        
        // Test without symbol  
        val resultWithoutSymbol = NumberFormatter.formatCurrency(amount, "IDR", showSymbol = false)
        assertTrue("IDR without symbol should not contain Rp, got: $resultWithoutSymbol", !resultWithoutSymbol.contains("Rp"))
        assertTrue("IDR without symbol should not contain $, got: $resultWithoutSymbol", !resultWithoutSymbol.contains("$"))
        
        // Should show formatted number with commas
        assertTrue("IDR should format with thousands separator, got: $resultWithoutSymbol", resultWithoutSymbol.contains(","))
    }

    @Test
    fun testIDRCompactCurrencyFormatting() {
        val amount = 1500000.0 // 1.5M IDR
        
        val result = NumberFormatter.formatCompactCurrency(amount, "IDR")
        assertTrue("Compact IDR should contain Rp symbol, got: $result", result.contains("Rp"))
        assertTrue("Compact IDR should not contain $ symbol, got: $result", !result.contains("$"))
        assertTrue("Compact IDR should show 1.5M format, got: $result", result.contains("1.5M"))
    }

    @Test
    fun testIDRVersusUSDFormatting() {
        val amount = 50000.0
        
        val idrResult = NumberFormatter.formatCurrency(amount, "IDR", showSymbol = true)
        val usdResult = NumberFormatter.formatCurrency(amount, "USD", showSymbol = true)
        
        // IDR should have Rp, USD should have $, they should be different
        assertTrue("IDR should contain Rp", idrResult.contains("Rp"))
        assertTrue("USD should contain $", usdResult.contains("$"))
        assertTrue("IDR and USD formatting should be different", idrResult != usdResult)
        
        // Neither should contain the other's symbol
        assertTrue("IDR should not contain $", !idrResult.contains("$"))
        assertTrue("USD should not contain Rp", !usdResult.contains("Rp"))
    }
}