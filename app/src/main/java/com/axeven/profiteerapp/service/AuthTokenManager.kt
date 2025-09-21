package com.axeven.profiteerapp.service

import com.axeven.profiteerapp.utils.logging.Logger
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthTokenState {
    object Valid : AuthTokenState()
    object Expired : AuthTokenState()
    object Invalid : AuthTokenState()
    object Checking : AuthTokenState()
    data class Error(val message: String) : AuthTokenState()
}

@Singleton
class AuthTokenManager @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val logger: Logger
) {
    private val _tokenState = MutableStateFlow<AuthTokenState>(AuthTokenState.Valid)
    val tokenState: StateFlow<AuthTokenState> = _tokenState.asStateFlow()

    private val _requiresReauth = MutableStateFlow(false)
    val requiresReauth: StateFlow<Boolean> = _requiresReauth.asStateFlow()

    suspend fun validateToken(forceRefresh: Boolean = false): AuthTokenState {
        _tokenState.value = AuthTokenState.Checking

        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            logger.w("AuthTokenManager", "No authenticated user found")
            _tokenState.value = AuthTokenState.Invalid
            return AuthTokenState.Invalid
        }

        return try {
            // Try to get a fresh token
            val tokenResult = currentUser.getIdToken(forceRefresh).await()
            val token = tokenResult.token

            if (token.isNullOrEmpty()) {
                logger.e("AuthTokenManager", "Token is null or empty")
                _tokenState.value = AuthTokenState.Expired
                triggerReauth("Token is null or empty")
                return AuthTokenState.Expired
            }

            // Check if token is actually valid by examining claims
            val claims = tokenResult.claims
            val issuedAt = claims["iat"] as? Long
            val expiresAt = claims["exp"] as? Long
            val currentTime = System.currentTimeMillis() / 1000

            if (expiresAt != null && currentTime >= expiresAt) {
                logger.e("AuthTokenManager", "Token is expired. Expires: $expiresAt, Current: $currentTime")
                _tokenState.value = AuthTokenState.Expired
                triggerReauth("Token has expired")
                return AuthTokenState.Expired
            }

            logger.d("AuthTokenManager", "Token validation successful")
            _tokenState.value = AuthTokenState.Valid
            _requiresReauth.value = false
            return AuthTokenState.Valid

        } catch (e: Exception) {
            logger.e("AuthTokenManager", "Token validation failed", e)

            // Check if this is specifically an auth error
            val isAuthError = e.message?.contains("authentication", ignoreCase = true) == true ||
                            e.message?.contains("permission", ignoreCase = true) == true ||
                            e.message?.contains("token", ignoreCase = true) == true

            val state = if (isAuthError) {
                triggerReauth("Authentication error: ${e.message}")
                AuthTokenState.Expired
            } else {
                AuthTokenState.Error(e.message ?: "Unknown error")
            }

            _tokenState.value = state
            return state
        }
    }

    private fun triggerReauth(reason: String) {
        logger.w("AuthTokenManager", "Triggering re-authentication: $reason")
        _requiresReauth.value = true
    }

    fun clearReauthFlag() {
        _requiresReauth.value = false
    }

    suspend fun attemptTokenRefresh(): Boolean {
        return try {
            val user = firebaseAuth.currentUser ?: return false
            user.getIdToken(true).await()
            _tokenState.value = AuthTokenState.Valid
            _requiresReauth.value = false
            logger.d("AuthTokenManager", "Token refresh successful")
            true
        } catch (e: Exception) {
            logger.e("AuthTokenManager", "Token refresh failed", e)
            triggerReauth("Token refresh failed: ${e.message}")
            false
        }
    }

    fun signOut() {
        try {
            firebaseAuth.signOut()
            _tokenState.value = AuthTokenState.Invalid
            _requiresReauth.value = false
            logger.d("AuthTokenManager", "User signed out successfully")
        } catch (e: Exception) {
            logger.e("AuthTokenManager", "Error signing out", e)
        }
    }

    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    fun isUserAuthenticated(): Boolean = firebaseAuth.currentUser != null

    // Helper method to check if we should show re-auth UI
    fun shouldShowReauth(): Boolean = _requiresReauth.value

    // Helper method to get user info for debugging
    fun getUserDebugInfo(): String {
        val user = firebaseAuth.currentUser
        return if (user != null) {
            buildString {
                append("User ID: ${user.uid}\n")
                append("Email: ${user.email}\n")
                append("Display Name: ${user.displayName}\n")
                append("Email Verified: ${user.isEmailVerified}\n")
                append("Last Sign In: ${user.metadata?.lastSignInTimestamp?.let { java.util.Date(it) }}\n")
                append("Creation Time: ${user.metadata?.creationTimestamp?.let { java.util.Date(it) }}\n")
            }
        } else {
            "No authenticated user"
        }
    }
}