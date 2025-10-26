package com.axeven.profiteerapp.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.axeven.profiteerapp.AppScreen

/**
 * Navigation stack for managing screen history and back navigation.
 *
 * Maintains a stack of screens with HOME as the permanent bottom entry.
 * Supports push/pop operations for standard navigation patterns.
 *
 * ## Usage Example
 * ```kotlin
 * val navigationStack = remember { NavigationStack(AppScreen.HOME) }
 * val currentScreen = navigationStack.current
 *
 * // Forward navigation
 * Button(onClick = { navigationStack.push(AppScreen.SETTINGS) }) {
 *     Text("Go to Settings")
 * }
 *
 * // Back navigation with BackHandler
 * BackHandler(enabled = navigationStack.canGoBack()) {
 *     navigationStack.pop()
 * }
 * ```
 *
 * ## Implementation Details
 * - Uses [SnapshotStateList] for automatic Compose recomposition
 * - Minimum stack size is always 1 (cannot be empty)
 * - Duplicate screens are allowed (e.g., HOME → A → B → A is valid)
 * - Operations are O(1) except getStackTrace() which is O(n)
 * - API 24+ compatible (uses removeAt instead of removeLast)
 *
 * ## Thread Safety
 * Safe for use in Compose UI thread. All operations use Compose snapshot state,
 * ensuring proper recomposition and state management.
 *
 * @property initialScreen The screen to initialize the stack with (default: HOME)
 * @see AppScreen for available screen types
 * @since Phase 3 - Back Button Navigation Implementation (2025-10-25)
 */
class NavigationStack(
    initialScreen: AppScreen = AppScreen.HOME
) {
    // Use SnapshotStateList for automatic Compose recomposition
    private val stack: SnapshotStateList<AppScreen> = mutableStateListOf(initialScreen)

    /**
     * Current screen at the top of the stack.
     */
    val current: AppScreen
        get() = stack.last()

    /**
     * Number of screens in the stack.
     */
    val size: Int
        get() = stack.size

    /**
     * Push a new screen onto the stack.
     *
     * Adds a screen to the top of the navigation stack, making it the current screen.
     * Duplicate screens are allowed - the same screen can appear multiple times in the stack.
     *
     * Example:
     * ```kotlin
     * navigationStack.push(AppScreen.SETTINGS)  // Stack: [HOME, SETTINGS]
     * navigationStack.push(AppScreen.WALLET_LIST)  // Stack: [HOME, SETTINGS, WALLET_LIST]
     * ```
     *
     * @param screen The screen to navigate to
     * @see pop for back navigation
     * @see current for accessing the top screen
     */
    fun push(screen: AppScreen) {
        stack.add(screen)
    }

    /**
     * Pop the current screen and return to the previous screen.
     *
     * Removes the current screen from the stack and returns the previous screen.
     * If the stack has only one item (at bottom), returns null and does not modify the stack.
     *
     * Example:
     * ```kotlin
     * // Stack: [HOME, SETTINGS, WALLET_LIST]
     * val prev = navigationStack.pop()  // Returns SETTINGS, Stack: [HOME, SETTINGS]
     * val prev2 = navigationStack.pop()  // Returns HOME, Stack: [HOME]
     * val prev3 = navigationStack.pop()  // Returns null, Stack: [HOME] (unchanged)
     * ```
     *
     * Note: Uses removeAt(lastIndex) instead of removeLast() for API 24+ compatibility.
     *
     * @return The previous screen, or null if already at bottom of stack (cannot go back)
     * @see canGoBack for checking if pop is possible
     * @see push for forward navigation
     */
    fun pop(): AppScreen? {
        return if (canGoBack()) {
            stack.removeAt(stack.lastIndex)  // API 24+ compatible
            stack.last()
        } else {
            null // Cannot go back from HOME (or single item)
        }
    }

    /**
     * Check if back navigation is possible.
     *
     * @return true if stack has more than one screen (not at bottom)
     */
    fun canGoBack(): Boolean = stack.size > 1

    /**
     * Peek at the previous screen without popping.
     *
     * @return The previous screen, or null if at bottom of stack
     */
    fun peekPrevious(): AppScreen? {
        return if (stack.size > 1) {
            stack[stack.size - 2]
        } else {
            null
        }
    }

    /**
     * Clear the entire stack and reset to initial screen.
     *
     * Useful for:
     * - Sign out flow (reset to LOGIN)
     * - Re-authentication completion (reset to HOME)
     *
     * @param resetTo The screen to reset the stack to (default: HOME)
     */
    fun clear(resetTo: AppScreen = AppScreen.HOME) {
        stack.clear()
        stack.add(resetTo)
    }

    /**
     * Get a copy of the current stack for debugging.
     *
     * @return Immutable list of all screens in stack (bottom to top)
     */
    fun getStackTrace(): List<AppScreen> = stack.toList()
}
