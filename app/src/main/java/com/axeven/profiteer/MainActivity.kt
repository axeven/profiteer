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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.axeven.profiteer.ui.login.AuthState
import com.axeven.profiteer.ui.login.LoginScreen
import com.axeven.profiteer.ui.theme.ProfiteerTheme
import com.axeven.profiteer.viewmodel.AuthViewModel
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

@Composable
fun ProfiteerApp(authViewModel: AuthViewModel = viewModel()) {
    val authState by authViewModel.authState.collectAsState()
    val googleSignInIntent by authViewModel.googleSignInIntent.collectAsState()
    
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
            // Show main app content
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                HomeContent(modifier = Modifier.padding(innerPadding))
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

@Composable
fun HomeContent(modifier: Modifier = Modifier) {
    Text(
        text = "Welcome to Profiteer!\nYou are now logged in.",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun ProfiteerAppPreview() {
    ProfiteerTheme {
        ProfiteerApp()
    }
}