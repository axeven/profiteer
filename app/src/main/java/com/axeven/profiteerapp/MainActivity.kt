package com.axeven.profiteerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
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
fun ProfiteerApp(authViewModel: AuthViewModel = viewModel()) {
    val authState by authViewModel.authState.collectAsState()
    val googleSignInIntent by authViewModel.googleSignInIntent.collectAsState()
    val requiresReauth by authViewModel.authRepository.getReauthRequirement().collectAsState()

    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
    var selectedWalletId by remember { mutableStateOf<String?>(null) }
    var initialTransactionType by remember { mutableStateOf<TransactionType?>(null) }
    var homeRefreshTrigger by remember { mutableStateOf(0) }
    var previousScreen by remember { mutableStateOf<AppScreen?>(null) }

    // Monitor re-authentication requirement
    LaunchedEffect(requiresReauth) {
        if (requiresReauth && authState is AuthState.Authenticated) {
            currentScreen = AppScreen.REAUTH
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
    
    when (authState) {
        is AuthState.Authenticated -> {
            // Show main app content with navigation
            when (currentScreen) {
                AppScreen.HOME -> {
                    HomeScreen(
                        onNavigateToSettings = { currentScreen = AppScreen.SETTINGS },
                        onNavigateToWalletList = { currentScreen = AppScreen.WALLET_LIST },
                        onNavigateToCreateTransaction = { transactionType ->
                            initialTransactionType = transactionType
                            currentScreen = AppScreen.CREATE_TRANSACTION
                        },
                        onEditTransaction = { transaction ->
                            selectedTransaction = transaction
                            currentScreen = AppScreen.EDIT_TRANSACTION
                        },
                        onNavigateToReports = { currentScreen = AppScreen.REPORTS },
                        onNavigateToTransactionList = { currentScreen = AppScreen.TRANSACTION_LIST },
                        onNavigateToAuth = { authViewModel.signOut() },
                        refreshTrigger = homeRefreshTrigger
                    )
                }
                AppScreen.SETTINGS -> {
                    SettingsScreen(
                        onNavigateBack = { currentScreen = AppScreen.HOME }
                    )
                }
                AppScreen.CREATE_TRANSACTION -> {
                    CreateTransactionScreen(
                        initialTransactionType = initialTransactionType,
                        preSelectedWalletId = selectedWalletId,
                        onNavigateBack = { 
                            initialTransactionType = null
                            homeRefreshTrigger++ // Trigger refresh
                            // Return to wallet detail if we came from there, otherwise home
                            currentScreen = if (selectedWalletId != null) AppScreen.WALLET_DETAIL else AppScreen.HOME
                        }
                    )
                }
                AppScreen.EDIT_TRANSACTION -> {
                    selectedTransaction?.let { transaction ->
                        EditTransactionScreen(
                            transaction = transaction,
                            onNavigateBack = {
                                homeRefreshTrigger++ // Trigger refresh
                                // Return to previous screen or appropriate default
                                currentScreen = when {
                                    previousScreen == AppScreen.TRANSACTION_LIST -> AppScreen.TRANSACTION_LIST
                                    previousScreen == AppScreen.DISCREPANCY_DEBUG -> AppScreen.DISCREPANCY_DEBUG
                                    selectedWalletId != null -> AppScreen.WALLET_DETAIL
                                    else -> AppScreen.HOME
                                }
                                previousScreen = null // Reset
                            }
                        )
                    }
                }
                AppScreen.WALLET_LIST -> {
                    WalletListScreen(
                        onNavigateBack = { currentScreen = AppScreen.HOME },
                        onNavigateToWalletDetail = { walletId ->
                            selectedWalletId = walletId
                            currentScreen = AppScreen.WALLET_DETAIL
                        },
                        onNavigateToDiscrepancyDebug = {
                            currentScreen = AppScreen.DISCREPANCY_DEBUG
                        }
                    )
                }
                AppScreen.WALLET_DETAIL -> {
                    selectedWalletId?.let { walletId ->
                        WalletDetailScreen(
                            walletId = walletId,
                            onNavigateBack = { 
                                selectedWalletId = null
                                currentScreen = AppScreen.WALLET_LIST 
                            },
                            onNavigateToCreateTransaction = { transactionType, preSelectedWalletId ->
                                initialTransactionType = transactionType
                                selectedWalletId = preSelectedWalletId
                                currentScreen = AppScreen.CREATE_TRANSACTION
                            },
                            onEditTransaction = { transaction ->
                                selectedTransaction = transaction
                                currentScreen = AppScreen.EDIT_TRANSACTION
                            }
                        )
                    }
                }
                AppScreen.REPORTS -> {
                    ReportScreenSimple(
                        onNavigateBack = { currentScreen = AppScreen.HOME }
                    )
                }
                AppScreen.TRANSACTION_LIST -> {
                    TransactionListScreen(
                        onNavigateBack = { currentScreen = AppScreen.HOME },
                        onEditTransaction = { transaction ->
                            selectedTransaction = transaction
                            previousScreen = AppScreen.TRANSACTION_LIST
                            currentScreen = AppScreen.EDIT_TRANSACTION
                        }
                    )
                }
                AppScreen.REAUTH -> {
                    ReauthScreen(
                        reason = "Your session has expired for security reasons",
                        userEmail = authViewModel.getCurrentUserEmail(),
                        onReauthSuccess = {
                            authViewModel.clearReauthFlag()
                            currentScreen = AppScreen.HOME
                            homeRefreshTrigger++
                        },
                        onSignOut = {
                            // Will trigger login screen via authState change
                        },
                        authViewModel = authViewModel
                    )
                }
                AppScreen.DISCREPANCY_DEBUG -> {
                    DiscrepancyDebugScreen(
                        onNavigateBack = { currentScreen = AppScreen.WALLET_LIST },
                        onNavigateToEdit = { transaction ->
                            selectedTransaction = transaction
                            previousScreen = AppScreen.DISCREPANCY_DEBUG
                            currentScreen = AppScreen.EDIT_TRANSACTION
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