package com.axeven.profiteerapp.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axeven.profiteerapp.data.repository.AuthRepository
import com.axeven.profiteerapp.ui.login.AuthState
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _googleSignInIntent = MutableStateFlow<Intent?>(null)
    val googleSignInIntent: StateFlow<Intent?> = _googleSignInIntent.asStateFlow()
    
    init {
        checkAuthState()
    }
    
    private fun checkAuthState() {
        viewModelScope.launch {
            try {
                val isAuthenticated = authRepository.isUserAuthenticated()
                _authState.value = if (isAuthenticated) {
                    AuthState.Authenticated
                } else {
                    AuthState.Initial
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Failed to check authentication status")
            }
        }
    }
    
    fun initiateGoogleSignIn() {
        try {
            _authState.value = AuthState.Loading
            val signInIntent = authRepository.googleSignInClient.signInIntent
            _googleSignInIntent.value = signInIntent
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Failed to initiate Google Sign-In")
        }
    }
    
    fun handleGoogleSignInResult(data: Intent?) {
        viewModelScope.launch {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                
                if (idToken != null) {
                    val result = authRepository.signInWithGoogle(idToken)
                    _authState.value = if (result.isSuccess) {
                        AuthState.Authenticated
                    } else {
                        AuthState.Error(result.exceptionOrNull()?.message ?: "Sign in failed")
                    }
                } else {
                    _authState.value = AuthState.Error("Failed to get ID token")
                }
            } catch (e: ApiException) {
                val errorMessage = when (e.statusCode) {
                    10 -> "Developer Error: Please check Firebase configuration, SHA-1 fingerprints, and Web Client ID"
                    12501 -> "Sign-in was cancelled by user"
                    12502 -> "Network error occurred"
                    else -> "Google Sign-In failed with code ${e.statusCode}: ${e.message}"
                }
                _authState.value = AuthState.Error(errorMessage)
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Sign in failed: ${e.message}")
            }
        }
    }
    
    fun clearGoogleSignInIntent() {
        _googleSignInIntent.value = null
    }
    
    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                _authState.value = AuthState.Initial
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Sign out failed")
            }
        }
    }
}