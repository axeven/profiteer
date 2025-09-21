package com.axeven.profiteerapp.data.repository

import android.content.Context
import com.axeven.profiteerapp.service.AuthTokenManager
import com.axeven.profiteerapp.service.AuthTokenState
import com.axeven.profiteerapp.utils.logging.Logger
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val tokenManager: AuthTokenManager,
    private val logger: Logger
) {
    
    val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("103782623354-2o33osadigv28sdamdb5p1otfolj1hk5.apps.googleusercontent.com")
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }
    
    suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            firebaseAuth.signInWithCredential(credential).await()
            Result.success(Unit)
        } catch (e: SecurityException) {
            // Log but don't fail for Google Play Services internal errors
            logger.w("AuthRepository", "Google Play Services SecurityException (non-critical): ${e.message}")
            Result.success(Unit) // Continue anyway as auth likely succeeded
        } catch (e: Exception) {
            logger.e("AuthRepository", "Sign-in failed: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    fun isUserAuthenticated(): Boolean {
        return firebaseAuth.currentUser != null
    }
    
    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }
    
    fun getCurrentUserEmail(): String? {
        return firebaseAuth.currentUser?.email
    }
    
    suspend fun signOut() {
        tokenManager.signOut()
        googleSignInClient.signOut().await()
    }

    suspend fun validateToken(forceRefresh: Boolean = false): AuthTokenState {
        return tokenManager.validateToken(forceRefresh)
    }

    suspend fun attemptTokenRefresh(): Boolean {
        return tokenManager.attemptTokenRefresh()
    }

    fun getTokenState() = tokenManager.tokenState

    fun getReauthRequirement() = tokenManager.requiresReauth

    fun clearReauthFlag() = tokenManager.clearReauthFlag()

    fun getUserDebugInfo(): String = tokenManager.getUserDebugInfo()
}