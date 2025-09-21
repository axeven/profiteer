package com.axeven.profiteerapp.utils

import com.axeven.profiteerapp.utils.logging.Logger
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreException.Code

object FirestoreErrorHandler {

    data class ErrorInfo(
        val userMessage: String,
        val shouldRetry: Boolean = false,
        val requiresReauth: Boolean = false,
        val debugInfo: String = ""
    )

    fun handleError(exception: Throwable, logger: Logger? = null): ErrorInfo {
        val debugInfo = generateDebugInfo(exception)
        logger?.e("FirestoreErrorHandler", "Firestore error occurred: $debugInfo", exception)
            ?: android.util.Log.e("FirestoreErrorHandler", "Firestore error occurred: $debugInfo", exception)

        return when (exception) {
            is FirebaseFirestoreException -> {
                when (exception.code) {
                    Code.PERMISSION_DENIED -> ErrorInfo(
                        userMessage = "Your session has expired. Please sign in again to continue.",
                        shouldRetry = false,
                        requiresReauth = true,
                        debugInfo = debugInfo
                    )
                    Code.UNAUTHENTICATED -> ErrorInfo(
                        userMessage = "Authentication required. Please sign in to continue.",
                        shouldRetry = false,
                        requiresReauth = true,
                        debugInfo = debugInfo
                    )
                    Code.UNAVAILABLE -> ErrorInfo(
                        userMessage = "Unable to connect to the server. Please check your internet connection and try again.",
                        shouldRetry = true,
                        requiresReauth = false,
                        debugInfo = debugInfo
                    )
                    Code.DEADLINE_EXCEEDED -> ErrorInfo(
                        userMessage = "The request timed out. Please try again.",
                        shouldRetry = true,
                        requiresReauth = false,
                        debugInfo = debugInfo
                    )
                    Code.RESOURCE_EXHAUSTED -> ErrorInfo(
                        userMessage = "Too many requests. Please wait a moment and try again.",
                        shouldRetry = true,
                        requiresReauth = false,
                        debugInfo = debugInfo
                    )
                    Code.CANCELLED -> ErrorInfo(
                        userMessage = "The operation was cancelled. Please try again.",
                        shouldRetry = true,
                        requiresReauth = false,
                        debugInfo = debugInfo
                    )
                    Code.DATA_LOSS -> ErrorInfo(
                        userMessage = "Data corruption detected. Please contact support.",
                        shouldRetry = false,
                        requiresReauth = false,
                        debugInfo = debugInfo
                    )
                    Code.INVALID_ARGUMENT -> ErrorInfo(
                        userMessage = "Invalid data provided. Please check your input and try again.",
                        shouldRetry = false,
                        requiresReauth = false,
                        debugInfo = debugInfo
                    )
                    Code.NOT_FOUND -> ErrorInfo(
                        userMessage = "The requested data was not found.",
                        shouldRetry = false,
                        requiresReauth = false,
                        debugInfo = debugInfo
                    )
                    Code.ALREADY_EXISTS -> ErrorInfo(
                        userMessage = "This item already exists.",
                        shouldRetry = false,
                        requiresReauth = false,
                        debugInfo = debugInfo
                    )
                    Code.FAILED_PRECONDITION -> ErrorInfo(
                        userMessage = "The operation failed due to a conflict. Please refresh and try again.",
                        shouldRetry = true,
                        requiresReauth = false,
                        debugInfo = debugInfo
                    )
                    Code.OUT_OF_RANGE -> ErrorInfo(
                        userMessage = "The requested data is out of range.",
                        shouldRetry = false,
                        requiresReauth = false,
                        debugInfo = debugInfo
                    )
                    Code.INTERNAL -> ErrorInfo(
                        userMessage = "An internal server error occurred. Please try again later.",
                        shouldRetry = true,
                        requiresReauth = false,
                        debugInfo = debugInfo
                    )
                    else -> ErrorInfo(
                        userMessage = "An unexpected error occurred. Please try again.",
                        shouldRetry = true,
                        requiresReauth = false,
                        debugInfo = debugInfo
                    )
                }
            }
            else -> ErrorInfo(
                userMessage = exception.message ?: "An unexpected error occurred. Please try again.",
                shouldRetry = true,
                requiresReauth = false,
                debugInfo = debugInfo
            )
        }
    }

    fun isAuthenticationError(exception: Throwable): Boolean {
        return when (exception) {
            is FirebaseFirestoreException -> {
                exception.code == Code.PERMISSION_DENIED || exception.code == Code.UNAUTHENTICATED
            }
            else -> false
        }
    }

    fun shouldShowOfflineMessage(exception: Throwable): Boolean {
        return when (exception) {
            is FirebaseFirestoreException -> {
                exception.code == Code.UNAVAILABLE
            }
            else -> false
        }
    }

    private fun generateDebugInfo(exception: Throwable): String {
        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        val debugInfo = buildString {
            append("=== CREDENTIAL DEBUG INFO ===\n")
            append("Timestamp: ${java.util.Date()}\n")
            append("Error Type: ${exception::class.simpleName}\n")

            if (exception is FirebaseFirestoreException) {
                append("Firestore Error Code: ${exception.code}\n")
                append("Firestore Error Message: ${exception.message}\n")
            }

            append("\n--- Firebase Auth State ---\n")
            if (currentUser != null) {
                append("User ID: ${currentUser.uid}\n")
                append("Email: ${currentUser.email}\n")
                append("Display Name: ${currentUser.displayName}\n")
                append("Is Anonymous: ${currentUser.isAnonymous}\n")
                append("Email Verified: ${currentUser.isEmailVerified}\n")
                append("Provider Data: ${currentUser.providerData.map { "${it.providerId}: ${it.email}" }}\n")

                // Check token freshness
                try {
                    currentUser.getIdToken(false).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            append("ID Token Retrieved: SUCCESS (cached)\n")
                        } else {
                            append("ID Token Retrieved: FAILED - ${task.exception?.message}\n")
                        }
                    }

                    currentUser.getIdToken(true).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            append("ID Token Refresh: SUCCESS\n")
                        } else {
                            append("ID Token Refresh: FAILED - ${task.exception?.message}\n")
                        }
                    }
                } catch (e: Exception) {
                    append("Token Check Error: ${e.message}\n")
                }

                append("Last Sign In: ${java.util.Date(currentUser.metadata?.lastSignInTimestamp ?: 0)}\n")
                append("Creation Time: ${java.util.Date(currentUser.metadata?.creationTimestamp ?: 0)}\n")
            } else {
                append("User: NULL (not authenticated)\n")
            }

            append("\n--- System Info ---\n")
            append("App Package: ${try { android.app.Application().packageName } catch (e: Exception) { "unknown" }}\n")
            append("Thread: ${Thread.currentThread().name}\n")

            append("\n--- Error Details ---\n")
            append("Exception: ${exception.message}\n")
            append("Stack Trace: ${exception.stackTrace.take(5).joinToString("\n") { "  at $it" }}\n")

            append("=== END DEBUG INFO ===")
        }

        return debugInfo
    }
}