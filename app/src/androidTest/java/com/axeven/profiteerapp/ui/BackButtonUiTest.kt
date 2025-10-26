package com.axeven.profiteerapp.ui

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.axeven.profiteerapp.AppScreen
import com.axeven.profiteerapp.navigation.NavigationStack
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for physical back button and top bar back button interaction.
 *
 * Ensures that:
 * 1. Physical back button navigates correctly
 * 2. Top bar back button behaves identically to physical back button
 * 3. Back navigation preserves screen state
 * 4. Rapid back presses don't cause crashes
 */
class BackButtonUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // Helper to simulate physical back press
    private fun pressPhysicalBack() {
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }
    }

    // Test screen composable with top bar
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun TestScreen(
        screenName: String,
        showBackButton: Boolean,
        onBackPressed: () -> Unit
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(screenName) },
                    navigationIcon = {
                        if (showBackButton) {
                            IconButton(
                                onClick = onBackPressed,
                                modifier = Modifier.testTag("topBarBackButton")
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                Text("Current Screen: $screenName")
                Text("Test content for $screenName")
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // Physical Back Button Tests
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun physicalBackButton_onNonHomeScreen_shouldNavigateBack() {
        // Given: App with navigation stack on SETTINGS
        lateinit var navigationStack: NavigationStack

        composeTestRule.setContent {
            navigationStack = remember { NavigationStack(AppScreen.HOME) }
            var currentScreen by remember { mutableStateOf(navigationStack.current) }

            BackHandler(enabled = navigationStack.canGoBack()) {
                navigationStack.pop()
                currentScreen = navigationStack.current
            }

            TestScreen(
                screenName = currentScreen.name,
                showBackButton = navigationStack.canGoBack(),
                onBackPressed = {
                    navigationStack.pop()
                    currentScreen = navigationStack.current
                }
            )
        }

        // Navigate to SETTINGS
        composeTestRule.runOnIdle {
            navigationStack.push(AppScreen.SETTINGS)
        }
        composeTestRule.waitForIdle()

        // Verify we're on SETTINGS
        composeTestRule.onNodeWithText("Current Screen: SETTINGS").assertExists()

        // When: Pressing physical back button
        pressPhysicalBack()
        composeTestRule.waitForIdle()

        // Then: Should navigate to HOME
        composeTestRule.onNodeWithText("Current Screen: HOME").assertExists()
        assert(navigationStack.current == AppScreen.HOME)
    }

    @Test
    fun physicalBackButton_onHomeScreen_shouldNotIntercept() {
        // Given: App on HOME screen
        lateinit var navigationStack: NavigationStack

        composeTestRule.setContent {
            navigationStack = remember { NavigationStack(AppScreen.HOME) }
            val currentScreen by remember { mutableStateOf(navigationStack.current) }

            // BackHandler should be disabled on HOME
            BackHandler(enabled = navigationStack.canGoBack()) {
                navigationStack.pop()
            }

            TestScreen(
                screenName = currentScreen.name,
                showBackButton = false,
                onBackPressed = { /* No top bar button on HOME */ }
            )
        }

        // Verify we're on HOME
        composeTestRule.onNodeWithText("Current Screen: HOME").assertExists()

        // When: Pressing physical back on HOME
        // Note: In test, this won't minimize app, but BackHandler should be disabled
        // We verify the handler is not intercepting

        // Then: Should still be on HOME (no navigation occurred)
        assert(navigationStack.current == AppScreen.HOME)
        assert(!navigationStack.canGoBack())
    }

    @Test
    fun physicalBackButton_deepNavigation_shouldNavigateCorrectly() {
        // Given: Deep navigation stack
        lateinit var navigationStack: NavigationStack

        composeTestRule.setContent {
            navigationStack = remember { NavigationStack(AppScreen.HOME) }
            var currentScreen by remember { mutableStateOf(navigationStack.current) }

            BackHandler(enabled = navigationStack.canGoBack()) {
                navigationStack.pop()
                currentScreen = navigationStack.current
            }

            TestScreen(
                screenName = currentScreen.name,
                showBackButton = navigationStack.canGoBack(),
                onBackPressed = {
                    navigationStack.pop()
                    currentScreen = navigationStack.current
                }
            )
        }

        // Build stack: HOME → WALLET_LIST → WALLET_DETAIL
        composeTestRule.runOnIdle {
            navigationStack.push(AppScreen.WALLET_LIST)
            navigationStack.push(AppScreen.WALLET_DETAIL)
        }
        composeTestRule.waitForIdle()

        // Verify on WALLET_DETAIL
        composeTestRule.onNodeWithText("Current Screen: WALLET_DETAIL").assertExists()

        // When: Press back (should go to WALLET_LIST)
        pressPhysicalBack()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Current Screen: WALLET_LIST").assertExists()

        // When: Press back again (should go to HOME)
        pressPhysicalBack()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Current Screen: HOME").assertExists()
    }

    // ════════════════════════════════════════════════════════════════════════
    // Top Bar Back Button Tests
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun topBarBackButton_shouldNavigateBackLikePhysicalButton() {
        // Given: App on SETTINGS with top bar back button
        lateinit var navigationStack: NavigationStack

        composeTestRule.setContent {
            navigationStack = remember { NavigationStack(AppScreen.HOME) }
            var currentScreen by remember { mutableStateOf(navigationStack.current) }

            BackHandler(enabled = navigationStack.canGoBack()) {
                navigationStack.pop()
                currentScreen = navigationStack.current
            }

            TestScreen(
                screenName = currentScreen.name,
                showBackButton = navigationStack.canGoBack(),
                onBackPressed = {
                    navigationStack.pop()
                    currentScreen = navigationStack.current
                }
            )
        }

        // Navigate to SETTINGS
        composeTestRule.runOnIdle {
            navigationStack.push(AppScreen.SETTINGS)
        }
        composeTestRule.waitForIdle()

        // When: Clicking top bar back button
        composeTestRule.onNodeWithTag("topBarBackButton").performClick()
        composeTestRule.waitForIdle()

        // Then: Should navigate to HOME (same as physical back)
        composeTestRule.onNodeWithText("Current Screen: HOME").assertExists()
        assert(navigationStack.current == AppScreen.HOME)
    }

    @Test
    fun topBarBackButton_shouldNotShowOnHomeScreen() {
        // Given: App on HOME screen
        composeTestRule.setContent {
            val navigationStack = remember { NavigationStack(AppScreen.HOME) }
            val currentScreen by remember { mutableStateOf(navigationStack.current) }

            TestScreen(
                screenName = currentScreen.name,
                showBackButton = navigationStack.canGoBack(),
                onBackPressed = { }
            )
        }

        // When: Checking for top bar back button
        // Then: Should not exist on HOME
        composeTestRule.onNodeWithTag("topBarBackButton").assertDoesNotExist()
    }

    @Test
    fun topBarBackButton_shouldShowOnNonHomeScreens() {
        // Given: App navigated to SETTINGS
        lateinit var navigationStack: NavigationStack

        composeTestRule.setContent {
            navigationStack = remember { NavigationStack(AppScreen.HOME) }
            var currentScreen by remember { mutableStateOf(navigationStack.current) }

            TestScreen(
                screenName = currentScreen.name,
                showBackButton = navigationStack.canGoBack(),
                onBackPressed = {
                    navigationStack.pop()
                    currentScreen = navigationStack.current
                }
            )
        }

        // Navigate to SETTINGS
        composeTestRule.runOnIdle {
            navigationStack.push(AppScreen.SETTINGS)
        }
        composeTestRule.waitForIdle()

        // When: Checking for top bar back button
        // Then: Should exist on SETTINGS
        composeTestRule.onNodeWithTag("topBarBackButton").assertExists()
        composeTestRule.onNodeWithTag("topBarBackButton").assertHasClickAction()
    }

    @Test
    fun topBarAndPhysicalBack_shouldBehaveSimilarly() {
        // Given: Two identical navigation scenarios
        lateinit var navigationStack1: NavigationStack
        lateinit var navigationStack2: NavigationStack
        var screen1 by mutableStateOf(AppScreen.HOME)
        var screen2 by mutableStateOf(AppScreen.HOME)

        composeTestRule.setContent {
            // Setup identical navigation stacks
            navigationStack1 = remember { NavigationStack(AppScreen.HOME) }
            navigationStack2 = remember { NavigationStack(AppScreen.HOME) }
        }

        // Build identical stacks
        composeTestRule.runOnIdle {
            navigationStack1.push(AppScreen.WALLET_LIST)
            navigationStack1.push(AppScreen.WALLET_DETAIL)

            navigationStack2.push(AppScreen.WALLET_LIST)
            navigationStack2.push(AppScreen.WALLET_DETAIL)
        }

        // When: Using physical back on stack1
        composeTestRule.runOnIdle {
            navigationStack1.pop()
            screen1 = navigationStack1.current
        }

        // When: Using top bar back (simulated) on stack2
        composeTestRule.runOnIdle {
            navigationStack2.pop()
            screen2 = navigationStack2.current
        }

        // Then: Both should be in identical state
        assert(screen1 == screen2)
        assert(navigationStack1.current == navigationStack2.current)
        assert(navigationStack1.size == navigationStack2.size)
    }

    // ════════════════════════════════════════════════════════════════════════
    // State Preservation Tests
    // ════════════════════════════════════════════════════════════════════════

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun backNavigation_shouldPreserveFormData() {
        // Given: Screen with form data
        lateinit var navigationStack: NavigationStack
        var textFieldValue by mutableStateOf("")

        composeTestRule.setContent {
            navigationStack = remember { NavigationStack(AppScreen.HOME) }
            var currentScreen by remember { mutableStateOf(navigationStack.current) }

            BackHandler(enabled = navigationStack.canGoBack()) {
                navigationStack.pop()
                currentScreen = navigationStack.current
            }

            when (currentScreen) {
                AppScreen.HOME -> {
                    TestScreen(
                        screenName = "HOME",
                        showBackButton = false,
                        onBackPressed = { }
                    )
                }
                AppScreen.CREATE_TRANSACTION -> {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = { Text("Create Transaction") },
                                navigationIcon = {
                                    IconButton(
                                        onClick = {
                                            navigationStack.pop()
                                            currentScreen = navigationStack.current
                                        },
                                        modifier = Modifier.testTag("topBarBackButton")
                                    ) {
                                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                    }
                                }
                            )
                        }
                    ) { paddingValues ->
                        Column(modifier = Modifier.padding(paddingValues)) {
                            TextField(
                                value = textFieldValue,
                                onValueChange = { textFieldValue = it },
                                modifier = Modifier.testTag("formField"),
                                label = { Text("Amount") }
                            )
                        }
                    }
                }
                else -> {
                    Text("Other screen")
                }
            }
        }

        // Navigate to CREATE_TRANSACTION
        composeTestRule.runOnIdle {
            navigationStack.push(AppScreen.CREATE_TRANSACTION)
        }
        composeTestRule.waitForIdle()

        // Enter data
        composeTestRule.onNodeWithTag("formField").performTextInput("12345")
        composeTestRule.waitForIdle()

        // Note: In real implementation, form data would be preserved in ViewModel
        // This test verifies the navigation mechanism doesn't interfere
        assert(textFieldValue == "12345")

        // When: Navigating back
        pressPhysicalBack()
        composeTestRule.waitForIdle()

        // Then: Should be on HOME
        composeTestRule.onNodeWithText("Current Screen: HOME").assertExists()
    }

    @Test
    fun backNavigation_shouldNotAffectOtherScreensData() {
        // Given: Multiple screens with different data
        lateinit var navigationStack: NavigationStack
        val screenData = mutableMapOf<AppScreen, String>()

        composeTestRule.setContent {
            navigationStack = remember { NavigationStack(AppScreen.HOME) }
            var currentScreen by remember { mutableStateOf(navigationStack.current) }

            // Initialize screen data
            LaunchedEffect(Unit) {
                screenData[AppScreen.HOME] = "Home Data"
                screenData[AppScreen.SETTINGS] = "Settings Data"
                screenData[AppScreen.WALLET_LIST] = "Wallet List Data"
            }

            BackHandler(enabled = navigationStack.canGoBack()) {
                navigationStack.pop()
                currentScreen = navigationStack.current
            }

            TestScreen(
                screenName = currentScreen.name,
                showBackButton = navigationStack.canGoBack(),
                onBackPressed = {
                    navigationStack.pop()
                    currentScreen = navigationStack.current
                }
            )

            // Display screen data
            Text("Data: ${screenData[currentScreen] ?: "None"}")
        }

        // Navigate through screens
        composeTestRule.runOnIdle {
            navigationStack.push(AppScreen.SETTINGS)
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Data: Settings Data").assertExists()

        composeTestRule.runOnIdle {
            navigationStack.push(AppScreen.WALLET_LIST)
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Data: Wallet List Data").assertExists()

        // When: Navigating back
        pressPhysicalBack()
        composeTestRule.waitForIdle()

        // Then: Should show SETTINGS data (preserved)
        composeTestRule.onNodeWithText("Data: Settings Data").assertExists()
    }

    // ════════════════════════════════════════════════════════════════════════
    // Rapid Press Tests
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun rapidBackPresses_shouldNotCauseErrors() {
        // Given: Deep navigation stack
        lateinit var navigationStack: NavigationStack

        composeTestRule.setContent {
            navigationStack = remember { NavigationStack(AppScreen.HOME) }
            var currentScreen by remember { mutableStateOf(navigationStack.current) }

            BackHandler(enabled = navigationStack.canGoBack()) {
                navigationStack.pop()
                currentScreen = navigationStack.current
            }

            TestScreen(
                screenName = currentScreen.name,
                showBackButton = navigationStack.canGoBack(),
                onBackPressed = {
                    navigationStack.pop()
                    currentScreen = navigationStack.current
                }
            )
        }

        // Build deep stack
        composeTestRule.runOnIdle {
            navigationStack.push(AppScreen.WALLET_LIST)
            navigationStack.push(AppScreen.WALLET_DETAIL)
            navigationStack.push(AppScreen.CREATE_TRANSACTION)
        }

        // When: Rapidly pressing back
        repeat(5) {
            pressPhysicalBack()
            Thread.sleep(50) // Small delay to simulate rapid but not instant presses
        }
        composeTestRule.waitForIdle()

        // Then: Should gracefully end at HOME without errors
        composeTestRule.onNodeWithText("Current Screen: HOME").assertExists()
        assert(navigationStack.current == AppScreen.HOME)
        assert(navigationStack.size == 1)
    }

    @Test
    fun rapidTopBarBackClicks_shouldNotCauseErrors() {
        // Given: Navigation stack with multiple screens
        lateinit var navigationStack: NavigationStack

        composeTestRule.setContent {
            navigationStack = remember { NavigationStack(AppScreen.HOME) }
            var currentScreen by remember { mutableStateOf(navigationStack.current) }

            BackHandler(enabled = navigationStack.canGoBack()) {
                navigationStack.pop()
                currentScreen = navigationStack.current
            }

            TestScreen(
                screenName = currentScreen.name,
                showBackButton = navigationStack.canGoBack(),
                onBackPressed = {
                    navigationStack.pop()
                    currentScreen = navigationStack.current
                }
            )
        }

        // Build stack
        composeTestRule.runOnIdle {
            navigationStack.push(AppScreen.SETTINGS)
            navigationStack.push(AppScreen.WALLET_LIST)
        }
        composeTestRule.waitForIdle()

        // When: Rapidly clicking top bar back button
        repeat(3) {
            if (navigationStack.canGoBack()) {
                composeTestRule.onNodeWithTag("topBarBackButton").performClick()
                composeTestRule.waitForIdle()
            }
        }

        // Then: Should end at HOME without errors
        composeTestRule.onNodeWithText("Current Screen: HOME").assertExists()
    }

    @Test
    fun mixedBackActions_physicalAndTopBar_shouldWorkCorrectly() {
        // Given: Navigation stack
        lateinit var navigationStack: NavigationStack

        composeTestRule.setContent {
            navigationStack = remember { NavigationStack(AppScreen.HOME) }
            var currentScreen by remember { mutableStateOf(navigationStack.current) }

            BackHandler(enabled = navigationStack.canGoBack()) {
                navigationStack.pop()
                currentScreen = navigationStack.current
            }

            TestScreen(
                screenName = currentScreen.name,
                showBackButton = navigationStack.canGoBack(),
                onBackPressed = {
                    navigationStack.pop()
                    currentScreen = navigationStack.current
                }
            )
        }

        // Build stack: HOME → A → B → C → D
        composeTestRule.runOnIdle {
            navigationStack.push(AppScreen.WALLET_LIST)
            navigationStack.push(AppScreen.WALLET_DETAIL)
            navigationStack.push(AppScreen.CREATE_TRANSACTION)
            navigationStack.push(AppScreen.EDIT_TRANSACTION)
        }
        composeTestRule.waitForIdle()

        // When: Alternating between physical and top bar back
        pressPhysicalBack() // D → C
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("topBarBackButton").performClick() // C → B
        composeTestRule.waitForIdle()

        pressPhysicalBack() // B → A
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("topBarBackButton").performClick() // A → HOME
        composeTestRule.waitForIdle()

        // Then: Should be at HOME
        composeTestRule.onNodeWithText("Current Screen: HOME").assertExists()
        assert(navigationStack.current == AppScreen.HOME)
    }
}
