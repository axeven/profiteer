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
import com.axeven.profiteerapp.ui.home.HomeScreen
import com.axeven.profiteerapp.ui.login.AuthState
import com.axeven.profiteerapp.ui.login.LoginScreen
import com.axeven.profiteerapp.ui.settings.SettingsScreen
import com.axeven.profiteerapp.ui.transaction.CreateTransactionScreen
import com.axeven.profiteerapp.ui.transaction.EditTransactionScreen
import com.axeven.profiteerapp.ui.wallet.WalletListScreen
import com.axeven.profiteerapp.ui.wallet.WalletDetailScreen
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
    HOME, SETTINGS, CREATE_TRANSACTION, EDIT_TRANSACTION, WALLET_LIST, WALLET_DETAIL
}

@Composable
fun ProfiteerApp(authViewModel: AuthViewModel = viewModel()) {
    val authState by authViewModel.authState.collectAsState()
    val googleSignInIntent by authViewModel.googleSignInIntent.collectAsState()
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
    var selectedWalletId by remember { mutableStateOf<String?>(null) }
    var initialTransactionType by remember { mutableStateOf<TransactionType?>(null) }
    var homeRefreshTrigger by remember { mutableStateOf(0) }
    
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
                                // Return to wallet detail if we came from there, otherwise home
                                currentScreen = if (selectedWalletId != null) AppScreen.WALLET_DETAIL else AppScreen.HOME
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