package com.axeven.profiteerapp

import com.axeven.profiteerapp.AppScreen
import com.axeven.profiteerapp.navigation.NavigationStack
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests for top-left back button functionality in TopAppBar.
 * Verifies that clicking the back arrow icon properly navigates back.
 *
 * This test suite ensures that all screens with TopAppBar back buttons
 * properly call navigationStack.pop() when the UI back button is clicked.
 *
 * Related: docs/plans/2025-10-28-fix-back-button.md
 */
class TopLeftBackButtonTest {

    private lateinit var navigationStack: NavigationStack

    @Before
    fun setup() {
        // Initialize navigation stack with HOME as starting screen
        navigationStack = NavigationStack(AppScreen.HOME)
    }

    @Test
    fun `setup should initialize navigation stack correctly`() {
        // Verify that setup initializes the navigation stack
        assertEquals(AppScreen.HOME, navigationStack.current)
        assertEquals(1, navigationStack.size)
    }

    // ════════════════════════════════════════════════════════════════════════
    // Phase 2: Failing Tests (RED) - These tests verify back button behavior
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `settings screen back button should pop navigation stack`() {
        // Arrange: Navigate to Settings
        navigationStack.push(AppScreen.SETTINGS)
        val initialStackSize = navigationStack.size

        // Create onNavigateBack callback that will be passed to screen
        var onNavigateBackCalled = false
        val onNavigateBack = {
            if (navigationStack.canGoBack()) {
                navigationStack.pop()
                onNavigateBackCalled = true
            }
        }

        // Act: Simulate back button click
        onNavigateBack()

        // Assert
        assertTrue("onNavigateBack should have been called", onNavigateBackCalled)
        assertEquals("Stack should have one less item", initialStackSize - 1, navigationStack.size)
        assertEquals("Should return to HOME", AppScreen.HOME, navigationStack.current)
    }

    @Test
    fun `create transaction screen back button should pop navigation stack`() {
        // Arrange
        navigationStack.push(AppScreen.CREATE_TRANSACTION)
        val initialStackSize = navigationStack.size

        var onNavigateBackCalled = false
        val onNavigateBack = {
            if (navigationStack.canGoBack()) {
                navigationStack.pop()
                onNavigateBackCalled = true
            }
        }

        // Act
        onNavigateBack()

        // Assert
        assertTrue(onNavigateBackCalled)
        assertEquals(initialStackSize - 1, navigationStack.size)
    }

    @Test
    fun `edit transaction screen back button should pop navigation stack`() {
        // Arrange
        navigationStack.push(AppScreen.EDIT_TRANSACTION)
        val initialStackSize = navigationStack.size

        var onNavigateBackCalled = false
        val onNavigateBack = {
            if (navigationStack.canGoBack()) {
                navigationStack.pop()
                onNavigateBackCalled = true
            }
        }

        // Act
        onNavigateBack()

        // Assert
        assertTrue(onNavigateBackCalled)
        assertEquals(initialStackSize - 1, navigationStack.size)
    }

    @Test
    fun `wallet detail screen back button should pop navigation stack`() {
        // Arrange
        navigationStack.push(AppScreen.WALLET_DETAIL)
        val initialStackSize = navigationStack.size

        var onNavigateBackCalled = false
        val onNavigateBack = {
            if (navigationStack.canGoBack()) {
                navigationStack.pop()
                onNavigateBackCalled = true
            }
        }

        // Act
        onNavigateBack()

        // Assert
        assertTrue(onNavigateBackCalled)
        assertEquals(initialStackSize - 1, navigationStack.size)
    }

    @Test
    fun `wallet list screen back button should pop navigation stack`() {
        // Arrange
        navigationStack.push(AppScreen.WALLET_LIST)
        val initialStackSize = navigationStack.size

        var onNavigateBackCalled = false
        val onNavigateBack = {
            if (navigationStack.canGoBack()) {
                navigationStack.pop()
                onNavigateBackCalled = true
            }
        }

        // Act
        onNavigateBack()

        // Assert
        assertTrue(onNavigateBackCalled)
        assertEquals(initialStackSize - 1, navigationStack.size)
    }

    @Test
    fun `transaction list screen back button should pop navigation stack`() {
        // Arrange
        navigationStack.push(AppScreen.TRANSACTION_LIST)
        val initialStackSize = navigationStack.size

        var onNavigateBackCalled = false
        val onNavigateBack = {
            if (navigationStack.canGoBack()) {
                navigationStack.pop()
                onNavigateBackCalled = true
            }
        }

        // Act
        onNavigateBack()

        // Assert
        assertTrue(onNavigateBackCalled)
        assertEquals(initialStackSize - 1, navigationStack.size)
    }

    @Test
    fun `report screen back button should pop navigation stack`() {
        // Arrange
        navigationStack.push(AppScreen.REPORTS)
        val initialStackSize = navigationStack.size

        var onNavigateBackCalled = false
        val onNavigateBack = {
            if (navigationStack.canGoBack()) {
                navigationStack.pop()
                onNavigateBackCalled = true
            }
        }

        // Act
        onNavigateBack()

        // Assert
        assertTrue(onNavigateBackCalled)
        assertEquals(initialStackSize - 1, navigationStack.size)
    }

    @Test
    fun `discrepancy debug screen back button should pop navigation stack`() {
        // Arrange
        navigationStack.push(AppScreen.DISCREPANCY_DEBUG)
        val initialStackSize = navigationStack.size

        var onNavigateBackCalled = false
        val onNavigateBack = {
            if (navigationStack.canGoBack()) {
                navigationStack.pop()
                onNavigateBackCalled = true
            }
        }

        // Act
        onNavigateBack()

        // Assert
        assertTrue(onNavigateBackCalled)
        assertEquals(initialStackSize - 1, navigationStack.size)
    }

    @Test
    fun `back button should not pop when only home screen in stack`() {
        // Arrange: Only HOME in stack
        val initialStackSize = navigationStack.size
        assertEquals(1, initialStackSize)

        var onNavigateBackCalled = false
        val onNavigateBack = {
            if (navigationStack.canGoBack()) {
                navigationStack.pop()
                onNavigateBackCalled = true
            }
        }

        // Act: Try to navigate back from HOME
        onNavigateBack()

        // Assert: Should NOT pop (can't go back from HOME)
        assertFalse("onNavigateBack should not pop when at HOME", onNavigateBackCalled)
        assertEquals("Stack size should remain 1", 1, navigationStack.size)
        assertEquals("Should still be on HOME", AppScreen.HOME, navigationStack.current)
    }
}
