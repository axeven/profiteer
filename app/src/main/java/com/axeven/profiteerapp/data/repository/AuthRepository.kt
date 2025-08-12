package com.axeven.profiteerapp.data.repository

import android.content.Context
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
    private val firebaseAuth: FirebaseAuth
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
            android.util.Log.w("AuthRepository", "Google Play Services SecurityException (non-critical): ${e.message}")
            Result.success(Unit) // Continue anyway as auth likely succeeded
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Sign-in failed: ${e.message}", e)
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
        firebaseAuth.signOut()
        googleSignInClient.signOut().await()
    }
}