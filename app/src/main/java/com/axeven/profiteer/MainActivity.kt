package com.axeven.profiteer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.axeven.profiteer.ui.home.HomeScreen
import com.axeven.profiteer.ui.login.AuthState
import com.axeven.profiteer.ui.login.LoginScreen
import com.axeven.profiteer.ui.settings.SettingsScreen
import com.axeven.profiteer.ui.transaction.CreateTransactionScreen
import com.axeven.profiteer.ui.transaction.EditTransactionScreen
import com.axeven.profiteer.ui.theme.ProfiteerTheme
import com.axeven.profiteer.viewmodel.AuthViewModel
import com.axeven.profiteer.data.model.Transaction
import com.axeven.profiteer.data.model.TransactionType
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
    HOME, SETTINGS, CREATE_TRANSACTION, EDIT_TRANSACTION
}

@Composable
fun ProfiteerApp(authViewModel: AuthViewModel = viewModel()) {
    val authState by authViewModel.authState.collectAsState()
    val googleSignInIntent by authViewModel.googleSignInIntent.collectAsState()
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
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
                        onNavigateBack = { 
                            initialTransactionType = null
                            homeRefreshTrigger++ // Trigger refresh
                            currentScreen = AppScreen.HOME 
                        }
                    )
                }
                AppScreen.EDIT_TRANSACTION -> {
                    selectedTransaction?.let { transaction ->
                        EditTransactionScreen(
                            transaction = transaction,
                            onNavigateBack = { 
                                homeRefreshTrigger++ // Trigger refresh
                                currentScreen = AppScreen.HOME 
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