package com.axeven.profiteerapp.data.constants

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for WalletType enum.
 * These tests verify that the WalletType enum correctly handles
 * wallet type classification and string conversion.
 */
class WalletTypeTest {

    @Test
    fun `enum should have PHYSICAL variant`() {
        val physical = WalletType.PHYSICAL
        assertNotNull(physical)
        assertEquals("PHYSICAL", physical.name)
    }

    @Test
    fun `enum should have LOGICAL variant`() {
        val logical = WalletType.LOGICAL
        assertNotNull(logical)
        assertEquals("LOGICAL", logical.name)
    }

    @Test
    fun `PHYSICAL displayName should equal Physical`() {
        assertEquals("Physical", WalletType.PHYSICAL.displayName)
    }

    @Test
    fun `LOGICAL displayName should equal Logical`() {
        assertEquals("Logical", WalletType.LOGICAL.displayName)
    }

    @Test
    fun `fromString should return PHYSICAL for Physical`() {
        val result = WalletType.fromString("Physical")
        assertEquals(WalletType.PHYSICAL, result)
    }

    @Test
    fun `fromString should return LOGICAL for Logical`() {
        val result = WalletType.fromString("Logical")
        assertEquals(WalletType.LOGICAL, result)
    }

    @Test
    fun `fromString should be case insensitive`() {
        assertEquals(WalletType.PHYSICAL, WalletType.fromString("physical"))
        assertEquals(WalletType.PHYSICAL, WalletType.fromString("PHYSICAL"))
        assertEquals(WalletType.LOGICAL, WalletType.fromString("logical"))
        assertEquals(WalletType.LOGICAL, WalletType.fromString("LOGICAL"))
    }

    @Test
    fun `fromString should return null for invalid string`() {
        assertNull(WalletType.fromString("Invalid"))
        assertNull(WalletType.fromString(""))
        assertNull(WalletType.fromString("Cash"))
    }

    @Test
    fun `fromString should return null for blank string`() {
        assertNull(WalletType.fromString("   "))
    }

    @Test
    fun `all enum values should have non-empty displayName`() {
        WalletType.values().forEach { walletType ->
            assert(walletType.displayName.isNotBlank()) {
                "${walletType.name} must have a non-blank displayName"
            }
        }
    }

    @Test
    fun `enum should have exactly two variants`() {
        assertEquals(2, WalletType.values().size)
    }

    @Test
    fun `displayNames should be unique`() {
        val displayNames = WalletType.values().map { it.displayName }
        val uniqueDisplayNames = displayNames.distinct()
        assertEquals(displayNames.size, uniqueDisplayNames.size)
    }
}
