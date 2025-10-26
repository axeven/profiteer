package com.axeven.profiteerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import android.util.Log
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.axeven.profiteerapp.navigation.NavigationStack
import com.axeven.profiteerapp.ui.auth.ReauthScreen
import com.axeven.profiteerapp.ui.discrepancy.DiscrepancyDebugScreen
import com.axeven.profiteerapp.ui.home.HomeScreen
import com.axeven.profiteerapp.ui.login.AuthState
import com.axeven.profiteerapp.ui.login.LoginScreen
import com.axeven.profiteerapp.ui.settings.SettingsScreen
import com.axeven.profiteerapp.ui.transaction.CreateTransactionScreen
import com.axeven.profiteerapp.ui.transaction.EditTransactionScreen
import com.axeven.profiteerapp.ui.transaction.TransactionListScreen
import com.axeven.profiteerapp.ui.wallet.WalletListScreen
import com.axeven.profiteerapp.ui.wallet.WalletDetailScreen
import com.axeven.profiteerapp.ui.report.ReportScreenSimple
import com.axeven.profiteerapp.ui.theme.ProfiteerTheme
import com.axeven.profiteerapp.viewmodel.AuthViewModel
import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProfiteerTheme {
                ProfiteerApp()
            }
        }
    }
}

enum class AppScreen {
    HOME, SETTINGS, CREATE_TRANSACTION, EDIT_TRANSACTION, WALLET_LIST, WALLET_DETAIL, REPORTS, TRANSACTION_LIST, REAUTH, DISCREPANCY_DEBUG
}

@Composable
fun ProfiteerApp(
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val googleSignInIntent by authViewModel.googleSignInIntent.collectAsState()
    val requiresReauth by authViewModel.authRepository.getReauthRequirement().collectAsState()

    // ════════════════════════════════════════════════════════════════
    // Navigation Stack Initialization
    // ════════════════════════════════════════════════════════════════
    // Centralized navigation state management using NavigationStack.
    // Replaces the previous manual tracking of currentScreen and previousScreen.
    //
    // Benefits:
    // - Automatic back navigation support through stack-based history
    // - Simplified state management (no manual previousScreen tracking)
    // - Integration with Android BackHandler for proper back button behavior
    // - Automatic Compose recomposition via SnapshotStateList
    val navigationStack = remember { NavigationStack(AppScreen.HOME) }
    val currentScreen = navigationStack.current  // Derived from stack.last()

    // Screen state variables
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
    var selectedWalletId by remember { mutableStateOf<String?>(null) }
    var initialTransactionType by remember { mutableStateOf<TransactionType?>(null) }
    var homeRefreshTrigger by remember { mutableIntStateOf(0) }

    // Monitor re-authentication requirement
    LaunchedEffect(requiresReauth) {
        if (requiresReauth && authState is AuthState.Authenticated) {
            navigationStack.push(AppScreen.REAUTH)
            Log.d("Navigation", "REAUTH triggered: pushed REAUTH screen (stack size: ${navigationStack.size})")
        }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        authViewModel.handleGoogleSignInResult(result.data)
        authViewModel.clearGoogleSignInIntent()
    }

    // Launch Google Sign-In when intent is available
    LaunchedEffect(googleSignInIntent) {
        googleSignInIntent?.let { intent ->
            googleSignInLauncher.launch(intent)
        }
    }

    // ════════════════════════════════════════════════════════════════
    // Back Navigation Handling (BackHandler Integration)
    // ════════════════════════════════════════════════════════════════
    // Intercepts the physical back button to navigate through the app's
    // screen stack instead of immediately minimizing the app.
    //
    // Behavior:
    // - If on HOME screen (stack size = 1): Back button minimizes app (default Android behavior)
    // - If on other screens: Back button pops stack and returns to previous screen
    // - On REAUTH screen: Back button is BLOCKED for security (cannot escape re-auth)
    //
    // The BackHandler is only enabled when ALL conditions are met:
    // 1. navigationStack.canGoBack() - Stack has more than one screen
    // 2. currentScreen != REAUTH - Not on re-authentication screen
    // 3. authState is Authenticated - User is logged in
    val canNavigateBack = navigationStack.canGoBack() &&
                          currentScreen != AppScreen.REAUTH &&
                          authState is AuthState.Authenticated

    BackHandler(enabled = canNavigateBack) {
        // Pop the navigation stack to get the previous screen
        val previousScreen = navigationStack.pop()

        if (previousScreen != null) {
            Log.d("Navigation", "Back pressed: ${currentScreen.name} → ${previousScreen.name} (stack size: ${navigationStack.size})")

            // Screen-specific cleanup: Clear state variables associated with the screen
            // being left. This prevents stale data from appearing if the user navigates
            // back to these screens later.
            when (currentScreen) {
                AppScreen.CREATE_TRANSACTION -> {
                    // Clear transaction creation state
                    initialTransactionType = null
                    selectedWalletId = null
                }
                AppScreen.EDIT_TRANSACTION -> {
                    // Clear selected transaction for editing
                    selectedTransaction = null
                }
                AppScreen.WALLET_DETAIL -> {
                    // Clear selected wallet ID
                    selectedWalletId = null
                }
                else -> {
                    // No cleanup needed for other screens (SETTINGS, WALLET_LIST, REPORTS, etc.)
                }
            }

            // Home refresh: Increment trigger when returning to HOME to reload
            // transaction list and balances (ensures fresh data is displayed)
            if (previousScreen == AppScreen.HOME) {
                homeRefreshTrigger++
            }
        }
    }
    
    when (authState) {
        is AuthState.Authenticated -> {
            // Show main app content with navigation
            when (currentScreen) {
                AppScreen.HOME -> {
                    HomeScreen(
                        onNavigateToSettings = {
                            navigationStack.push(AppScreen.SETTINGS)
                            Log.d("Navigation", "Forward: HOME → SETTINGS (stack size: ${navigationStack.size})")
                        },
                        onNavigateToWalletList = {
                            navigationStack.push(AppScreen.WALLET_LIST)
                            Log.d("Navigation", "Forward: HOME → WALLET_LIST (stack size: ${navigationStack.size})")
                        },
                        onNavigateToCreateTransaction = { transactionType ->
                            initialTransactionType = transactionType
                            navigationStack.push(AppScreen.CREATE_TRANSACTION)
                            Log.d("Navigation", "Forward: HOME → CREATE_TRANSACTION (type: $transactionType, stack size: ${navigationStack.size})")
                        },
                        onEditTransaction = { transaction ->
                            selectedTransaction = transaction
                            navigationStack.push(AppScreen.EDIT_TRANSACTION)
                            Log.d("Navigation", "Forward: HOME → EDIT_TRANSACTION (stack size: ${navigationStack.size})")
                        },
                        onNavigateToReports = {
                            navigationStack.push(AppScreen.REPORTS)
                            Log.d("Navigation", "Forward: HOME → REPORTS (stack size: ${navigationStack.size})")
                        },
                        onNavigateToTransactionList = {
                            navigationStack.push(AppScreen.TRANSACTION_LIST)
                            Log.d("Navigation", "Forward: HOME → TRANSACTION_LIST (stack size: ${navigationStack.size})")
                        },
                        onNavigateToAuth = { authViewModel.signOut() },
                        refreshTrigger = homeRefreshTrigger
                    )
                }
                AppScreen.SETTINGS -> {
                    SettingsScreen(
                        // BackHandler handles back navigation automatically
                        onNavigateBack = { }
                    )
                }
                AppScreen.CREATE_TRANSACTION -> {
                    CreateTransactionScreen(
                        initialTransactionType = initialTransactionType,
                        preSelectedWalletId = selectedWalletId,
                        // BackHandler handles back navigation automatically
                        onNavigateBack = { }
                    )
                }
                AppScreen.EDIT_TRANSACTION -> {
                    selectedTransaction?.let { transaction ->
                        EditTransactionScreen(
                            transaction = transaction,
                            // BackHandler handles back navigation automatically
                            onNavigateBack = { }
                        )
                    }
                }
                AppScreen.WALLET_LIST -> {
                    WalletListScreen(
                        // BackHandler handles back navigation automatically
                        onNavigateBack = { },
                        onNavigateToWalletDetail = { walletId ->
                            selectedWalletId = walletId
                            navigationStack.push(AppScreen.WALLET_DETAIL)
                            Log.d("Navigation", "Forward: WALLET_LIST → WALLET_DETAIL (walletId: $walletId, stack size: ${navigationStack.size})")
                        },
                        onNavigateToDiscrepancyDebug = {
                            navigationStack.push(AppScreen.DISCREPANCY_DEBUG)
                            Log.d("Navigation", "Forward: WALLET_LIST → DISCREPANCY_DEBUG (stack size: ${navigationStack.size})")
                        }
                    )
                }
                AppScreen.WALLET_DETAIL -> {
                    selectedWalletId?.let { walletId ->
                        WalletDetailScreen(
                            walletId = walletId,
                            // BackHandler handles back navigation automatically
                            onNavigateBack = { },
                            onNavigateToCreateTransaction = { transactionType, preSelectedWalletId ->
                                initialTransactionType = transactionType
                                selectedWalletId = preSelectedWalletId
                                navigationStack.push(AppScreen.CREATE_TRANSACTION)
                                Log.d("Navigation", "Forward: WALLET_DETAIL → CREATE_TRANSACTION (type: $transactionType, stack size: ${navigationStack.size})")
                            },
                            onEditTransaction = { transaction ->
                                selectedTransaction = transaction
                                navigationStack.push(AppScreen.EDIT_TRANSACTION)
                                Log.d("Navigation", "Forward: WALLET_DETAIL → EDIT_TRANSACTION (stack size: ${navigationStack.size})")
                            }
                        )
                    }
                }
                AppScreen.REPORTS -> {
                    ReportScreenSimple(
                        // BackHandler handles back navigation automatically
                        onNavigateBack = { }
                    )
                }
                AppScreen.TRANSACTION_LIST -> {
                    TransactionListScreen(
                        // BackHandler handles back navigation automatically
                        onNavigateBack = { },
                        onEditTransaction = { transaction ->
                            selectedTransaction = transaction
                            navigationStack.push(AppScreen.EDIT_TRANSACTION)
                            Log.d("Navigation", "Forward: TRANSACTION_LIST → EDIT_TRANSACTION (stack size: ${navigationStack.size})")
                        }
                    )
                }
                AppScreen.REAUTH -> {
                    ReauthScreen(
                        reason = "Your session has expired for security reasons",
                        userEmail = authViewModel.getCurrentUserEmail(),
                        onReauthSuccess = {
                            authViewModel.clearReauthFlag()
                            navigationStack.clear(AppScreen.HOME)
                            homeRefreshTrigger++
                            Log.d("Navigation", "REAUTH success: cleared stack and reset to HOME (stack size: ${navigationStack.size})")
                        },
                        onSignOut = {
                            // Will trigger login screen via authState change
                        },
                        authViewModel = authViewModel
                    )
                }
                AppScreen.DISCREPANCY_DEBUG -> {
                    DiscrepancyDebugScreen(
                        // BackHandler handles back navigation automatically
                        onNavigateBack = { },
                        onNavigateToEdit = { transaction ->
                            selectedTransaction = transaction
                            navigationStack.push(AppScreen.EDIT_TRANSACTION)
                            Log.d("Navigation", "Forward: DISCREPANCY_DEBUG → EDIT_TRANSACTION (stack size: ${navigationStack.size})")
                        }
                    )
                }
            }
        }
        else -> {
            // Show login screen
            LoginScreen(
                onNavigateToHome = {
                    // Navigation is handled by state change
                },
                authViewModel = authViewModel
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ProfiteerAppPreview() {
    ProfiteerTheme {
        ProfiteerApp()
    }
}