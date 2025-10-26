package com.axeven.profiteerapp.navigation

import com.axeven.profiteerapp.AppScreen
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for NavigationStack class.
 *
 * Tests all navigation stack operations following TDD principles.
 * These tests are written BEFORE implementing NavigationStack to ensure
 * correct behavior specification.
 */
class NavigationStackTest {

    private lateinit var navigationStack: NavigationStack

    @Before
    fun setUp() {
        navigationStack = NavigationStack()
    }

    // ════════════════════════════════════════════════════════════════════════
    // Initial State Tests
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `initial stack should have HOME as current screen`() {
        // Given: A newly created navigation stack
        // When: Checking the current screen
        // Then: It should be HOME
        assertEquals(AppScreen.HOME, navigationStack.current)
    }

    @Test
    fun `initial stack should have size 1`() {
        // Given: A newly created navigation stack
        // When: Checking the stack size
        // Then: It should be 1 (containing only HOME)
        assertEquals(1, navigationStack.size)
    }

    @Test
    fun `initial stack should not allow back navigation`() {
        // Given: A newly created navigation stack
        // When: Checking if back navigation is possible
        // Then: It should return false (cannot go back from HOME)
        assertFalse(navigationStack.canGoBack())
    }

    @Test
    fun `initial stack should have no previous screen`() {
        // Given: A newly created navigation stack
        // When: Peeking at the previous screen
        // Then: It should return null (no previous screen)
        assertNull(navigationStack.peekPrevious())
    }

    @Test
    fun `initial stack with custom screen should use that screen`() {
        // Given: Creating stack with custom initial screen
        val customStack = NavigationStack(AppScreen.SETTINGS)

        // When: Checking the current screen
        // Then: It should be the custom screen
        assertEquals(AppScreen.SETTINGS, customStack.current)
    }

    // ════════════════════════════════════════════════════════════════════════
    // Push Operation Tests
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `push should add screen to stack`() {
        // Given: Initial stack with HOME
        // When: Pushing SETTINGS
        navigationStack.push(AppScreen.SETTINGS)

        // Then: Current screen should be SETTINGS
        assertEquals(AppScreen.SETTINGS, navigationStack.current)
    }

    @Test
    fun `push should increase stack size`() {
        // Given: Initial stack with size 1
        // When: Pushing SETTINGS
        navigationStack.push(AppScreen.SETTINGS)

        // Then: Stack size should be 2
        assertEquals(2, navigationStack.size)
    }

    @Test
    fun `push should enable back navigation`() {
        // Given: Initial stack where back is disabled
        // When: Pushing SETTINGS
        navigationStack.push(AppScreen.SETTINGS)

        // Then: Back navigation should be enabled
        assertTrue(navigationStack.canGoBack())
    }

    @Test
    fun `push should update previous screen`() {
        // Given: Initial stack with HOME
        // When: Pushing SETTINGS
        navigationStack.push(AppScreen.SETTINGS)

        // Then: Previous screen should be HOME
        assertEquals(AppScreen.HOME, navigationStack.peekPrevious())
    }

    @Test
    fun `multiple pushes should stack correctly`() {
        // Given: Initial stack with HOME
        // When: Pushing multiple screens
        navigationStack.push(AppScreen.SETTINGS)
        navigationStack.push(AppScreen.WALLET_LIST)
        navigationStack.push(AppScreen.WALLET_DETAIL)

        // Then: Current should be last pushed screen
        assertEquals(AppScreen.WALLET_DETAIL, navigationStack.current)
        // And: Stack size should be 4
        assertEquals(4, navigationStack.size)
        // And: Previous should be second-to-last screen
        assertEquals(AppScreen.WALLET_LIST, navigationStack.peekPrevious())
    }

    @Test
    fun `push should allow duplicate screens`() {
        // Given: Stack with HOME → SETTINGS
        navigationStack.push(AppScreen.SETTINGS)

        // When: Pushing SETTINGS again
        navigationStack.push(AppScreen.SETTINGS)

        // Then: Stack should contain duplicate
        assertEquals(AppScreen.SETTINGS, navigationStack.current)
        assertEquals(3, navigationStack.size)
    }

    // ════════════════════════════════════════════════════════════════════════
    // Pop Operation Tests
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `pop should return previous screen`() {
        // Given: Stack with HOME → SETTINGS
        navigationStack.push(AppScreen.SETTINGS)

        // When: Popping the stack
        val previous = navigationStack.pop()

        // Then: Should return HOME
        assertEquals(AppScreen.HOME, previous)
    }

    @Test
    fun `pop should update current screen`() {
        // Given: Stack with HOME → SETTINGS
        navigationStack.push(AppScreen.SETTINGS)

        // When: Popping the stack
        navigationStack.pop()

        // Then: Current should be HOME
        assertEquals(AppScreen.HOME, navigationStack.current)
    }

    @Test
    fun `pop should decrease stack size`() {
        // Given: Stack with HOME → SETTINGS (size 2)
        navigationStack.push(AppScreen.SETTINGS)

        // When: Popping the stack
        navigationStack.pop()

        // Then: Stack size should be 1
        assertEquals(1, navigationStack.size)
    }

    @Test
    fun `pop on single item stack should return null`() {
        // Given: Initial stack with only HOME
        // When: Attempting to pop
        val result = navigationStack.pop()

        // Then: Should return null (cannot go back from HOME)
        assertNull(result)
    }

    @Test
    fun `pop on single item stack should not change current`() {
        // Given: Initial stack with only HOME
        // When: Attempting to pop
        navigationStack.pop()

        // Then: Current should still be HOME
        assertEquals(AppScreen.HOME, navigationStack.current)
    }

    @Test
    fun `pop on single item stack should not change size`() {
        // Given: Initial stack with size 1
        // When: Attempting to pop
        navigationStack.pop()

        // Then: Size should still be 1
        assertEquals(1, navigationStack.size)
    }

    @Test
    fun `multiple pops should navigate correctly`() {
        // Given: Stack with HOME → WALLET_LIST → WALLET_DETAIL → CREATE_TRANSACTION
        navigationStack.push(AppScreen.WALLET_LIST)
        navigationStack.push(AppScreen.WALLET_DETAIL)
        navigationStack.push(AppScreen.CREATE_TRANSACTION)

        // When: Popping once
        val first = navigationStack.pop()
        // Then: Should return to WALLET_DETAIL
        assertEquals(AppScreen.WALLET_DETAIL, first)
        assertEquals(AppScreen.WALLET_DETAIL, navigationStack.current)

        // When: Popping again
        val second = navigationStack.pop()
        // Then: Should return to WALLET_LIST
        assertEquals(AppScreen.WALLET_LIST, second)
        assertEquals(AppScreen.WALLET_LIST, navigationStack.current)

        // When: Popping again
        val third = navigationStack.pop()
        // Then: Should return to HOME
        assertEquals(AppScreen.HOME, third)
        assertEquals(AppScreen.HOME, navigationStack.current)

        // When: Popping again (at HOME)
        val fourth = navigationStack.pop()
        // Then: Should return null
        assertNull(fourth)
        assertEquals(AppScreen.HOME, navigationStack.current)
    }

    // ════════════════════════════════════════════════════════════════════════
    // canGoBack() Tests
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `canGoBack should return false for HOME only`() {
        // Given: Initial stack with only HOME
        // When: Checking canGoBack
        // Then: Should return false
        assertFalse(navigationStack.canGoBack())
    }

    @Test
    fun `canGoBack should return true with multiple screens`() {
        // Given: Stack with HOME → SETTINGS
        navigationStack.push(AppScreen.SETTINGS)

        // When: Checking canGoBack
        // Then: Should return true
        assertTrue(navigationStack.canGoBack())
    }

    @Test
    fun `canGoBack should return false after popping to HOME`() {
        // Given: Stack with HOME → SETTINGS
        navigationStack.push(AppScreen.SETTINGS)

        // When: Popping back to HOME
        navigationStack.pop()

        // Then: canGoBack should return false
        assertFalse(navigationStack.canGoBack())
    }

    @Test
    fun `canGoBack should handle deep stacks`() {
        // Given: Deep stack (5 levels)
        navigationStack.push(AppScreen.WALLET_LIST)
        navigationStack.push(AppScreen.WALLET_DETAIL)
        navigationStack.push(AppScreen.CREATE_TRANSACTION)
        navigationStack.push(AppScreen.EDIT_TRANSACTION)

        // When: Checking canGoBack at each level
        // Then: Should always return true until at HOME
        assertTrue(navigationStack.canGoBack())

        navigationStack.pop() // Back to CREATE_TRANSACTION
        assertTrue(navigationStack.canGoBack())

        navigationStack.pop() // Back to WALLET_DETAIL
        assertTrue(navigationStack.canGoBack())

        navigationStack.pop() // Back to WALLET_LIST
        assertTrue(navigationStack.canGoBack())

        navigationStack.pop() // Back to HOME
        assertFalse(navigationStack.canGoBack())
    }

    // ════════════════════════════════════════════════════════════════════════
    // peekPrevious() Tests
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `peekPrevious should return null for single screen`() {
        // Given: Initial stack with only HOME
        // When: Peeking at previous
        // Then: Should return null
        assertNull(navigationStack.peekPrevious())
    }

    @Test
    fun `peekPrevious should return previous without modifying stack`() {
        // Given: Stack with HOME → SETTINGS
        navigationStack.push(AppScreen.SETTINGS)

        // When: Peeking at previous
        val previous = navigationStack.peekPrevious()

        // Then: Should return HOME
        assertEquals(AppScreen.HOME, previous)
        // And: Current should still be SETTINGS
        assertEquals(AppScreen.SETTINGS, navigationStack.current)
        // And: Size should still be 2
        assertEquals(2, navigationStack.size)
    }

    @Test
    fun `peekPrevious should handle deep stacks`() {
        // Given: Stack with HOME → A → B → C
        navigationStack.push(AppScreen.WALLET_LIST)
        navigationStack.push(AppScreen.WALLET_DETAIL)
        navigationStack.push(AppScreen.CREATE_TRANSACTION)

        // When: Peeking at previous
        // Then: Should return second-to-last screen
        assertEquals(AppScreen.WALLET_DETAIL, navigationStack.peekPrevious())
    }

    @Test
    fun `peekPrevious should update after pop`() {
        // Given: Stack with HOME → A → B → C
        navigationStack.push(AppScreen.WALLET_LIST)
        navigationStack.push(AppScreen.WALLET_DETAIL)
        navigationStack.push(AppScreen.CREATE_TRANSACTION)

        // When: Popping and peeking
        navigationStack.pop()

        // Then: Previous should now be WALLET_LIST
        assertEquals(AppScreen.WALLET_LIST, navigationStack.peekPrevious())
    }

    // ════════════════════════════════════════════════════════════════════════
    // clear() Tests
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `clear should reset to default HOME`() {
        // Given: Stack with multiple screens
        navigationStack.push(AppScreen.SETTINGS)
        navigationStack.push(AppScreen.WALLET_LIST)
        navigationStack.push(AppScreen.WALLET_DETAIL)

        // When: Clearing the stack
        navigationStack.clear()

        // Then: Current should be HOME
        assertEquals(AppScreen.HOME, navigationStack.current)
        // And: Size should be 1
        assertEquals(1, navigationStack.size)
        // And: Cannot go back
        assertFalse(navigationStack.canGoBack())
    }

    @Test
    fun `clear with custom screen should reset to that screen`() {
        // Given: Stack with multiple screens
        navigationStack.push(AppScreen.SETTINGS)
        navigationStack.push(AppScreen.WALLET_LIST)

        // When: Clearing with custom screen
        navigationStack.clear(AppScreen.REAUTH)

        // Then: Current should be REAUTH
        assertEquals(AppScreen.REAUTH, navigationStack.current)
        // And: Size should be 1
        assertEquals(1, navigationStack.size)
    }

    @Test
    fun `clear on empty stack should work`() {
        // Given: Initial stack with HOME
        // When: Clearing
        navigationStack.clear()

        // Then: Should still have HOME
        assertEquals(AppScreen.HOME, navigationStack.current)
        assertEquals(1, navigationStack.size)
    }

    // ════════════════════════════════════════════════════════════════════════
    // getStackTrace() Tests
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `getStackTrace should return single item for initial stack`() {
        // Given: Initial stack with HOME
        // When: Getting stack trace
        val trace = navigationStack.getStackTrace()

        // Then: Should contain only HOME
        assertEquals(listOf(AppScreen.HOME), trace)
    }

    @Test
    fun `getStackTrace should return all screens in order`() {
        // Given: Stack with HOME → WALLET_LIST → WALLET_DETAIL
        navigationStack.push(AppScreen.WALLET_LIST)
        navigationStack.push(AppScreen.WALLET_DETAIL)

        // When: Getting stack trace
        val trace = navigationStack.getStackTrace()

        // Then: Should return bottom-to-top order
        assertEquals(
            listOf(AppScreen.HOME, AppScreen.WALLET_LIST, AppScreen.WALLET_DETAIL),
            trace
        )
    }

    @Test
    fun `getStackTrace should return immutable copy`() {
        // Given: Stack with HOME → SETTINGS
        navigationStack.push(AppScreen.SETTINGS)

        // When: Getting stack trace and modifying it
        val trace = navigationStack.getStackTrace().toMutableList()
        trace.add(AppScreen.WALLET_LIST)

        // Then: Original stack should be unchanged
        assertEquals(2, navigationStack.size)
        assertEquals(AppScreen.SETTINGS, navigationStack.current)
    }

    @Test
    fun `getStackTrace should update after operations`() {
        // Given: Stack operations
        navigationStack.push(AppScreen.SETTINGS)
        navigationStack.push(AppScreen.WALLET_LIST)

        // When: Getting trace
        val traceBefore = navigationStack.getStackTrace()

        // Then: Should show 3 screens
        assertEquals(3, traceBefore.size)

        // When: Popping
        navigationStack.pop()
        val traceAfter = navigationStack.getStackTrace()

        // Then: Should show 2 screens
        assertEquals(2, traceAfter.size)
        assertEquals(listOf(AppScreen.HOME, AppScreen.SETTINGS), traceAfter)
    }

    // ════════════════════════════════════════════════════════════════════════
    // Edge Cases & Complex Scenarios
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `stack should handle same screen pushed multiple times`() {
        // Given: Pushing same screen multiple times
        navigationStack.push(AppScreen.SETTINGS)
        navigationStack.push(AppScreen.SETTINGS)
        navigationStack.push(AppScreen.SETTINGS)

        // When: Checking stack state
        // Then: Should have 4 items (HOME + 3x SETTINGS)
        assertEquals(4, navigationStack.size)
        assertEquals(AppScreen.SETTINGS, navigationStack.current)

        // When: Popping once
        navigationStack.pop()

        // Then: Should still be on SETTINGS
        assertEquals(AppScreen.SETTINGS, navigationStack.current)
        assertEquals(3, navigationStack.size)
    }

    @Test
    fun `stack should handle alternating screens`() {
        // Given: Alternating between two screens
        navigationStack.push(AppScreen.SETTINGS)
        navigationStack.push(AppScreen.WALLET_LIST)
        navigationStack.push(AppScreen.SETTINGS)
        navigationStack.push(AppScreen.WALLET_LIST)

        // When: Checking trace
        val trace = navigationStack.getStackTrace()

        // Then: Should maintain exact order
        assertEquals(
            listOf(
                AppScreen.HOME,
                AppScreen.SETTINGS,
                AppScreen.WALLET_LIST,
                AppScreen.SETTINGS,
                AppScreen.WALLET_LIST
            ),
            trace
        )
    }

    @Test
    fun `stack should handle complex navigation flow`() {
        // Given: Simulating real user flow
        // HOME → WALLET_LIST → WALLET_DETAIL → CREATE_TRANSACTION
        navigationStack.push(AppScreen.WALLET_LIST)
        assertEquals(AppScreen.WALLET_LIST, navigationStack.current)

        navigationStack.push(AppScreen.WALLET_DETAIL)
        assertEquals(AppScreen.WALLET_DETAIL, navigationStack.current)

        navigationStack.push(AppScreen.CREATE_TRANSACTION)
        assertEquals(AppScreen.CREATE_TRANSACTION, navigationStack.current)

        // When: User presses back twice
        navigationStack.pop() // Back to WALLET_DETAIL
        assertEquals(AppScreen.WALLET_DETAIL, navigationStack.current)

        navigationStack.pop() // Back to WALLET_LIST
        assertEquals(AppScreen.WALLET_LIST, navigationStack.current)

        // And: User navigates to different screen
        navigationStack.push(AppScreen.SETTINGS)
        assertEquals(AppScreen.SETTINGS, navigationStack.current)

        // Then: Stack should be HOME → WALLET_LIST → SETTINGS
        assertEquals(
            listOf(AppScreen.HOME, AppScreen.WALLET_LIST, AppScreen.SETTINGS),
            navigationStack.getStackTrace()
        )
    }

    @Test
    fun `stack should handle clear and rebuild`() {
        // Given: Stack with screens
        navigationStack.push(AppScreen.SETTINGS)
        navigationStack.push(AppScreen.WALLET_LIST)

        // When: Clearing and rebuilding
        navigationStack.clear()
        navigationStack.push(AppScreen.TRANSACTION_LIST)
        navigationStack.push(AppScreen.EDIT_TRANSACTION)

        // Then: Should have fresh stack
        assertEquals(
            listOf(AppScreen.HOME, AppScreen.TRANSACTION_LIST, AppScreen.EDIT_TRANSACTION),
            navigationStack.getStackTrace()
        )
    }

    @Test
    fun `stack size should always be at least 1`() {
        // Given: Various operations
        navigationStack.push(AppScreen.SETTINGS)
        assertTrue(navigationStack.size >= 1)

        navigationStack.pop()
        assertTrue(navigationStack.size >= 1)

        navigationStack.pop() // Try to pop HOME
        assertTrue(navigationStack.size >= 1)

        navigationStack.clear()
        assertTrue(navigationStack.size >= 1)
    }

    @Test
    fun `current should never be null`() {
        // Given: Various states
        assertNotNull(navigationStack.current)

        navigationStack.push(AppScreen.SETTINGS)
        assertNotNull(navigationStack.current)

        navigationStack.pop()
        assertNotNull(navigationStack.current)

        navigationStack.clear()
        assertNotNull(navigationStack.current)
    }
}
