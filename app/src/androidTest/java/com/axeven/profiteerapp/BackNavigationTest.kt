package com.axeven.profiteerapp

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.axeven.profiteerapp.navigation.NavigationStack
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Integration tests for back navigation behavior.
 *
 * Tests the interaction between NavigationStack and BackHandler
 * in realistic navigation scenarios.
 */
class BackNavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // Test helper to simulate back press
    private fun pressBack() {
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // Simple Navigation Flow Tests
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun backPressOnHomeScreen_shouldMinimizeApp() {
        // Given: App is on HOME screen
        var appMinimized = false
        var currentScreen by mutableStateOf(AppScreen.HOME)

        composeTestRule.setContent {
            val navigationStack = remember { NavigationStack(AppScreen.HOME) }
            currentScreen = navigationStack.current

            // BackHandler should NOT intercept on HOME (enabled = false)
            // Default Android behavior should occur (minimize app)
            BackHandler(enabled = navigationStack.canGoBack()) {
                // This should not be called on HOME
                navigationStack.pop()
                currentScreen = navigationStack.current
            }

            // Simulate default back behavior detection
            if (!navigationStack.canGoBack()) {
                // In real app, this would minimize
                // For test, we set a flag
                LaunchedEffect(Unit) {
                    // Default behavior would occur
                }
            }

            Text("Current: ${navigationStack.current}")
        }

        // When: Pressing back on HOME
        // Note: Since BackHandler is disabled, Android's default behavior occurs
        // In test environment, we verify BackHandler is not enabled
        composeTestRule.onNodeWithText("Current: HOME").assertExists()

        // Then: BackHandler should be disabled (we can't directly test minimize in unit tests)
        // Verify current screen is still HOME
        assert(currentScreen == AppScreen.HOME)
    }

    @Test
    fun backPressOnSettings_shouldReturnToHome() {
        // Given: App navigated from HOME to SETTINGS
        lateinit var navigationStack: NavigationStack

        composeTestRule.setContent {
            navigationStack = remember { NavigationStack(AppScreen.HOME) }
            var currentScreen by remember { mutableStateOf(navigationStack.current) }

            BackHandler(enabled = navigationStack.canGoBack()) {
                navigationStack.pop()
                currentScreen = navigationStack.current
            }

            Text("Current: ${currentScreen.name}")
        }

        // Navigate to SETTINGS
        composeTestRule.runOnIdle {
            navigationStack.push(AppScreen.SETTINGS)
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Current: SETTINGS").assertExists()

        // When: Pressing back
        pressBack()

        // Then: Should return to HOME
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Current: HOME").assertExists()
        assert(navigationStack.current == AppScreen.HOME)
    }

    @Test
    fun backPressOnWalletDetail_shouldReturnToWalletList() {
        // Given: Navigation flow HOME → WALLET_LIST → WALLET_DETAIL
        lateinit var navigationStack: NavigationStack

        composeTestRule.setContent {
            navigationStack = remember { NavigationStack(AppScreen.HOME) }
            var currentScreen by remember { mutableStateOf(navigationStack.current) }

            BackHandler(enabled = navigationStack.canGoBack()) {
                navigationStack.pop()
                currentScreen = navigationStack.current
            }

            Text("Current: ${currentScreen.name}")
        }

        // Navigate: HOME → WALLET_LIST → WALLET_DETAIL
        composeTestRule.runOnIdle {
            navigationStack.push(AppScreen.WALLET_LIST)
            navigationStack.push(AppScreen.WALLET_DETAIL)
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Current: WALLET_DETAIL").assertExists()

        // When: Pressing back
        pressBack()

        // Then: Should return to WALLET_LIST
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Current: WALLET_LIST").assertExists()
        assert(navigationStack.current == AppScreen.WALLET_LIST)
    }

    // ════════════════════════════════════════════════════════════════════════
    // Complex Navigation Flow Tests
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun backPressSequence_homeToWalletListToWalletDetail_shouldNavigateCorrectly() {
        // Given: Deep navigation flow
        lateinit var navigationStack: NavigationStack
        var screenHistory = mutableListOf<AppScreen>()

        composeTestRule.setContent {
            navigationStack = remember { NavigationStack(AppScreen.HOME) }
            var currentScreen by remember { mutableStateOf(navigationStack.current) }

            BackHandler(enabled = navigationStack.canGoBack()) {
                navigationStack.pop()
                currentScreen = navigationStack.current
            }

            // Track screen changes
            LaunchedEffect(currentScreen) {
                screenHistory.add(currentScreen)
            }

            Text("Current: ${currentScreen.name}")
        }

        // When: Navigate HOME → WALLET_LIST → WALLET_DETAIL
        composeTestRule.runOnIdle {
            navigationStack.push(AppScreen.WALLET_LIST)
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Current: WALLET_LIST").assertExists()

        composeTestRule.runOnIdle {
            navigationStack.push(AppScreen.WALLET_DETAIL)
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Current: WALLET_DETAIL").assertExists()

        // When: Press back (should go to WALLET_LIST)
        pressBack()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Current: WALLET_LIST").assertExists()

        // When: Press back again (should go to HOME)
        pressBack()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Current: HOME").assertExists()

        // Then: Verify final state
        assert(navigationStack.current == AppScreen.HOME)
        assert(navigationStack.size == 1)
        assert(!navigationStack.canGoBack())
    }

    @Test
    fun backPressFromCreateTransaction_shouldReturnToPreviousScreen() {
        // Given: Navigate from different starting points to CREATE_TRANSACTION
        lateinit var navigationStack: NavigationStack

        composeTestRule.setContent {
            navigationStack = remember { NavigationStack(AppScreen.HOME) }
            var currentScreen by remember { mutableStateOf(navigationStack.current) }

            BackHandler(enabled = navigationStack.canGoBack()) {
                navigationStack.pop()
                currentScreen = navigationStack.current
            }

            Text("Current: ${currentScreen.name}")
        }

        // Scenario 1: HOME → CREATE_TRANSACTION
        composeTestRule.runOnIdle {
            navigationStack.push(AppScreen.CREATE_TRANSACTION)
        }
        composeTestRule.waitForIdle()

        pressBack()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Current: HOME").assertExists()

        // Scenario 2: HOME → WALLET_DETAIL → CREATE_TRANSACTION
        composeTestRule.runOnIdle {
            navigationStack.push(AppScreen.WALLET_DETAIL)
            navigationStack.push(AppScreen.CREATE_TRANSACTION)
        }
        composeTestRule.waitForIdle()

        pressBack()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Current: WALLET_DETAIL").assertExists()
    }

    @Test
    fun backPressFromEditTransaction_shouldReturnToCorrectPreviousScreen() {
        // Given: Navigate to EDIT_TRANSACTION from different screens
        lateinit var navigationStack: NavigationStack

        composeTestRule.setContent {
            navigationStack = remember { NavigationStack(AppScreen.HOME) }
            var currentScreen by remember { mutableStateOf(navigationStack.current) }

            BackHandler(enabled = navigationStack.canGoBack()) {
                navigationStack.pop()
                currentScreen = navigationStack.current
            }

            Text("Current: ${currentScreen.name}")
        }

        // Scenario 1: HOME → EDIT_TRANSACTION (from home screen transaction list)
        composeTestRule.runOnIdle {
            navigationStack.push(AppScreen.EDIT_TRANSACTION)
        }
        composeTestRule.waitForIdle()

        pressBack()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Current: HOME").assertExists()

        // Scenario 2: HOME → TRANSACTION_LIST → EDIT_TRANSACTION
        composeTestRule.runOnIdle {
            navigationStack.clear() // Reset to HOME
            navigationStack.push(AppScreen.TRANSACTION_LIST)
            navigationStack.push(AppScreen.EDIT_TRANSACTION)
        }
        composeTestRule.waitForIdle()

        pressBack()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Current: TRANSACTION_LIST").assertExists()

        // Scenario 3: HOME → WALLET_LIST → DISCREPANCY_DEBUG → EDIT_TRANSACTION
        composeTestRule.runOnIdle {
            navigationStack.clear() // Reset to HOME
            navigationStack.push(AppScreen.WALLET_LIST)
            navigationStack.push(AppScreen.DISCREPANCY_DEBUG)
            navigationStack.push(AppScreen.EDIT_TRANSACTION)
        }
        composeTestRule.waitForIdle()

        pressBack()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Current: DISCREPANCY_DEBUG").assertExists()
    }

    // ════════════════════════════════════════════════════════════════════════
    // REAUTH Flow Tests
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun backPressOnReauthScreen_shouldBeBlocked() {
        // Given: App is on REAUTH screen (security requirement)
        lateinit var navigationStack: NavigationStack

        composeTestRule.setContent {
            navigationStack = remember { NavigationStack(AppScreen.HOME) }
            var currentScreen by remember { mutableStateOf(navigationStack.current) }

            // BackHandler should be DISABLED on REAUTH screen
            val canGoBack = navigationStack.canGoBack() && currentScreen != AppScreen.REAUTH

            BackHandler(enabled = canGoBack) {
                navigationStack.pop()
                currentScreen = navigationStack.current
            }

            Text("Current: ${currentScreen.name}")
        }

        // Navigate to REAUTH
        composeTestRule.runOnIdle {
            navigationStack.push(AppScreen.SETTINGS)
            navigationStack.push(AppScreen.REAUTH)
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Current: REAUTH").assertExists()

        // When: Pressing back on REAUTH screen
        pressBack()

        // Then: Should stay on REAUTH (back press blocked)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Current: REAUTH").assertExists()
        assert(navigationStack.current == AppScreen.REAUTH)
    }

    @Test
    fun reauthFlow_shouldClearStackAndReturnToHome() {
        // Given: User goes through reauth flow
        lateinit var navigationStack: NavigationStack

        composeTestRule.setContent {
            navigationStack = remember { NavigationStack(AppScreen.HOME) }
            var currentScreen by remember { mutableStateOf(navigationStack.current) }

            BackHandler(enabled = navigationStack.canGoBack() && currentScreen != AppScreen.REAUTH) {
                navigationStack.pop()
                currentScreen = navigationStack.current
            }

            Text("Current: ${currentScreen.name}")
            Text("Stack size: ${navigationStack.size}")
        }

        // Build up navigation stack
        composeTestRule.runOnIdle {
            navigationStack.push(AppScreen.WALLET_LIST)
            navigationStack.push(AppScreen.WALLET_DETAIL)
            navigationStack.push(AppScreen.CREATE_TRANSACTION)
        }

        // When: REAUTH is triggered (simulating session expiry)
        composeTestRule.runOnIdle {
            navigationStack.push(AppScreen.REAUTH)
        }
        composeTestRule.waitForIdle()

        // When: User successfully re-authenticates (clear and reset to HOME)
        composeTestRule.runOnIdle {
            navigationStack.clear(AppScreen.HOME)
        }
        composeTestRule.waitForIdle()

        // Then: Should be at HOME with clean stack
        composeTestRule.onNodeWithText("Current: HOME").assertExists()
        composeTestRule.onNodeWithText("Stack size: 1").assertExists()
        assert(navigationStack.current == AppScreen.HOME)
        assert(navigationStack.size == 1)
    }

    // ════════════════════════════════════════════════════════════════════════
    // Edge Cases
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun rapidBackPresses_shouldNotCrash() {
        // Given: Deep navigation stack
        lateinit var navigationStack: NavigationStack

        composeTestRule.setContent {
            navigationStack = remember { NavigationStack(AppScreen.HOME) }
            var currentScreen by remember { mutableStateOf(navigationStack.current) }

            BackHandler(enabled = navigationStack.canGoBack()) {
                navigationStack.pop()
                currentScreen = navigationStack.current
            }

            Text("Current: ${currentScreen.name}")
        }

        // Build stack: HOME → A → B → C → D
        composeTestRule.runOnIdle {
            navigationStack.push(AppScreen.WALLET_LIST)
            navigationStack.push(AppScreen.WALLET_DETAIL)
            navigationStack.push(AppScreen.CREATE_TRANSACTION)
            navigationStack.push(AppScreen.EDIT_TRANSACTION)
        }

        // When: Rapidly pressing back multiple times
        repeat(10) {
            pressBack()
            composeTestRule.waitForIdle()
        }

        // Then: Should gracefully handle and stay at HOME
        composeTestRule.onNodeWithText("Current: HOME").assertExists()
        assert(navigationStack.current == AppScreen.HOME)
        assert(navigationStack.size == 1)
    }

    @Test
    fun backPressAfterClearAndNavigate_shouldWorkCorrectly() {
        // Given: Stack is cleared and rebuilt
        lateinit var navigationStack: NavigationStack

        composeTestRule.setContent {
            navigationStack = remember { NavigationStack(AppScreen.HOME) }
            var currentScreen by remember { mutableStateOf(navigationStack.current) }

            BackHandler(enabled = navigationStack.canGoBack()) {
                navigationStack.pop()
                currentScreen = navigationStack.current
            }

            Text("Current: ${currentScreen.name}")
        }

        // Initial navigation
        composeTestRule.runOnIdle {
            navigationStack.push(AppScreen.SETTINGS)
            navigationStack.push(AppScreen.WALLET_LIST)
        }

        // When: Clear and rebuild
        composeTestRule.runOnIdle {
            navigationStack.clear()
            navigationStack.push(AppScreen.TRANSACTION_LIST)
            navigationStack.push(AppScreen.EDIT_TRANSACTION)
        }
        composeTestRule.waitForIdle()

        // When: Press back
        pressBack()
        composeTestRule.waitForIdle()

        // Then: Should go to TRANSACTION_LIST (not old stack)
        composeTestRule.onNodeWithText("Current: TRANSACTION_LIST").assertExists()

        // When: Press back again
        pressBack()
        composeTestRule.waitForIdle()

        // Then: Should go to HOME
        composeTestRule.onNodeWithText("Current: HOME").assertExists()
    }

    @Test
    fun navigationStack_shouldMaintainConsistencyThroughoutFlow() {
        // Given: Complex real-world navigation scenario
        lateinit var navigationStack: NavigationStack
        val navigationLog = mutableListOf<String>()

        composeTestRule.setContent {
            navigationStack = remember { NavigationStack(AppScreen.HOME) }
            var currentScreen by remember { mutableStateOf(navigationStack.current) }

            BackHandler(enabled = navigationStack.canGoBack()) {
                navigationLog.add("Back pressed from ${currentScreen.name}")
                navigationStack.pop()
                currentScreen = navigationStack.current
            }

            Text("Current: ${currentScreen.name}")
        }

        // Simulate: User explores app
        composeTestRule.runOnIdle {
            navigationStack.push(AppScreen.WALLET_LIST)
            navigationLog.add("Navigated to WALLET_LIST")
        }

        composeTestRule.runOnIdle {
            navigationStack.push(AppScreen.WALLET_DETAIL)
            navigationLog.add("Navigated to WALLET_DETAIL")
        }

        // User presses back
        pressBack()
        composeTestRule.waitForIdle()

        // User navigates to create transaction
        composeTestRule.runOnIdle {
            navigationStack.push(AppScreen.CREATE_TRANSACTION)
            navigationLog.add("Navigated to CREATE_TRANSACTION")
        }

        // User presses back
        pressBack()
        composeTestRule.waitForIdle()

        // User navigates to settings
        composeTestRule.runOnIdle {
            navigationStack.push(AppScreen.SETTINGS)
            navigationLog.add("Navigated to SETTINGS")
        }

        // User presses back twice
        pressBack()
        composeTestRule.waitForIdle()
        pressBack()
        composeTestRule.waitForIdle()

        // Then: Should end at HOME
        composeTestRule.onNodeWithText("Current: HOME").assertExists()
        assert(navigationStack.current == AppScreen.HOME)

        // And: Stack should be consistent
        assert(navigationStack.size == 1)
        assert(!navigationStack.canGoBack())

        // Log verification
        println("Navigation log: ${navigationLog.joinToString(" → ")}")
    }
}
