package com.axeven.profiteerapp.utils

import android.content.Context
import com.axeven.profiteerapp.utils.logging.Logger
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date

object CredentialDiagnostics {

    data class CredentialStatus(
        val name: String,
        val status: Status,
        val details: String,
        val lastChecked: Date = Date()
    ) {
        enum class Status {
            VALID, EXPIRED, ERROR, UNKNOWN
        }
    }

    data class DiagnosticReport(
        val overallStatus: String,
        val recommendations: List<String>,
        val credentialStatuses: List<CredentialStatus>,
        val debugLog: String
    )

    suspend fun runFullDiagnostics(context: Context): DiagnosticReport {
        val log = StringBuilder()
        val statuses = mutableListOf<CredentialStatus>()
        val recommendations = mutableListOf<String>()

        log.appendLine("=== CREDENTIAL DIAGNOSTICS REPORT ===")
        log.appendLine("Generated: ${Date()}")
        log.appendLine()

        // Check Firebase Auth
        val authStatus = checkFirebaseAuth(log)
        statuses.add(authStatus)

        // Check Google Sign-In
        val googleStatus = checkGoogleSignIn(context, log)
        statuses.add(googleStatus)

        // Check Firestore Connection
        val firestoreStatus = checkFirestoreConnection(log)
        statuses.add(firestoreStatus)

        // Check App Configuration
        val configStatus = checkAppConfiguration(context, log)
        statuses.add(configStatus)

        // Generate recommendations based on findings
        generateRecommendations(statuses, recommendations, log)

        val overallStatus = determineOverallStatus(statuses)

        log.appendLine("\n=== SUMMARY ===")
        log.appendLine("Overall Status: $overallStatus")
        log.appendLine("Credential Issues Found: ${statuses.count { it.status == CredentialStatus.Status.ERROR || it.status == CredentialStatus.Status.EXPIRED }}")
        log.appendLine("Recommendations: ${recommendations.size}")

        return DiagnosticReport(
            overallStatus = overallStatus,
            recommendations = recommendations,
            credentialStatuses = statuses,
            debugLog = log.toString()
        )
    }

    private suspend fun checkFirebaseAuth(log: StringBuilder): CredentialStatus {
        log.appendLine("--- Checking Firebase Authentication ---")

        return try {
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser

            if (currentUser == null) {
                log.appendLine("❌ No authenticated user found")
                CredentialStatus(
                    name = "Firebase Auth",
                    status = CredentialStatus.Status.ERROR,
                    details = "No authenticated user"
                )
            } else {
                log.appendLine("✓ User authenticated: ${currentUser.email}")
                log.appendLine("  User ID: ${currentUser.uid}")
                log.appendLine("  Last Sign In: ${Date(currentUser.metadata?.lastSignInTimestamp ?: 0)}")

                // Try to refresh the token
                try {
                    currentUser.getIdToken(true).await()
                    log.appendLine("✓ ID Token refresh successful")
                    CredentialStatus(
                        name = "Firebase Auth",
                        status = CredentialStatus.Status.VALID,
                        details = "User authenticated, token valid"
                    )
                } catch (e: Exception) {
                    log.appendLine("❌ ID Token refresh failed: ${e.message}")
                    CredentialStatus(
                        name = "Firebase Auth",
                        status = CredentialStatus.Status.EXPIRED,
                        details = "Token refresh failed: ${e.message}"
                    )
                }
            }
        } catch (e: Exception) {
            log.appendLine("❌ Firebase Auth check failed: ${e.message}")
            CredentialStatus(
                name = "Firebase Auth",
                status = CredentialStatus.Status.ERROR,
                details = "Auth check failed: ${e.message}"
            )
        }
    }

    private fun checkGoogleSignIn(context: Context, log: StringBuilder): CredentialStatus {
        log.appendLine("\n--- Checking Google Sign-In Configuration ---")

        return try {
            // Check if google-services.json exists and is configured
            val packageName = context.packageName
            log.appendLine("✓ Package name: $packageName")

            // This is a basic check - in a real scenario you'd want to check:
            // 1. google-services.json validity
            // 2. OAuth client configuration
            // 3. SHA-1 fingerprints

            CredentialStatus(
                name = "Google Sign-In",
                status = CredentialStatus.Status.UNKNOWN,
                details = "Configuration check requires manual verification"
            )
        } catch (e: Exception) {
            log.appendLine("❌ Google Sign-In check failed: ${e.message}")
            CredentialStatus(
                name = "Google Sign-In",
                status = CredentialStatus.Status.ERROR,
                details = "Configuration check failed: ${e.message}"
            )
        }
    }

    private suspend fun checkFirestoreConnection(log: StringBuilder): CredentialStatus {
        log.appendLine("\n--- Checking Firestore Connection ---")

        return try {
            val firestore = FirebaseFirestore.getInstance()

            // Try a simple read operation
            val testDoc = firestore.collection("test").document("connectivity").get().await()
            log.appendLine("✓ Firestore connection successful")

            CredentialStatus(
                name = "Firestore",
                status = CredentialStatus.Status.VALID,
                details = "Connection successful"
            )
        } catch (e: Exception) {
            log.appendLine("❌ Firestore connection failed: ${e.message}")

            val status = when {
                e.message?.contains("PERMISSION_DENIED") == true -> CredentialStatus.Status.EXPIRED
                e.message?.contains("UNAUTHENTICATED") == true -> CredentialStatus.Status.EXPIRED
                else -> CredentialStatus.Status.ERROR
            }

            CredentialStatus(
                name = "Firestore",
                status = status,
                details = "Connection failed: ${e.message}"
            )
        }
    }

    private fun checkAppConfiguration(context: Context, log: StringBuilder): CredentialStatus {
        log.appendLine("\n--- Checking App Configuration ---")

        return try {
            // Check for google-services.json
            val googleServicesExists = try {
                context.assets.open("google-services.json")
                true
            } catch (e: Exception) {
                false
            }

            if (googleServicesExists) {
                log.appendLine("✓ google-services.json found")
            } else {
                log.appendLine("❌ google-services.json not found in assets")
            }

            CredentialStatus(
                name = "App Configuration",
                status = if (googleServicesExists) CredentialStatus.Status.VALID else CredentialStatus.Status.ERROR,
                details = if (googleServicesExists) "Configuration files present" else "Missing google-services.json"
            )
        } catch (e: Exception) {
            log.appendLine("❌ App configuration check failed: ${e.message}")
            CredentialStatus(
                name = "App Configuration",
                status = CredentialStatus.Status.ERROR,
                details = "Configuration check failed: ${e.message}"
            )
        }
    }

    private fun generateRecommendations(
        statuses: List<CredentialStatus>,
        recommendations: MutableList<String>,
        log: StringBuilder
    ) {
        log.appendLine("\n--- Generating Recommendations ---")

        statuses.forEach { status ->
            when (status.name) {
                "Firebase Auth" -> {
                    when (status.status) {
                        CredentialStatus.Status.ERROR -> {
                            recommendations.add("Re-authenticate the user - call signOut() then show login screen")
                            log.appendLine("📋 Recommend: Re-authentication required")
                        }
                        CredentialStatus.Status.EXPIRED -> {
                            recommendations.add("Firebase Auth token expired - sign out and re-authenticate")
                            log.appendLine("📋 Recommend: Token refresh needed")
                        }
                        else -> {}
                    }
                }
                "Firestore" -> {
                    when (status.status) {
                        CredentialStatus.Status.EXPIRED -> {
                            recommendations.add("Firestore permissions denied - check Auth rules and re-authenticate")
                            log.appendLine("📋 Recommend: Check Firestore security rules")
                        }
                        CredentialStatus.Status.ERROR -> {
                            recommendations.add("Firestore connection issues - check network and configuration")
                            log.appendLine("📋 Recommend: Verify Firestore setup")
                        }
                        else -> {}
                    }
                }
                "Google Sign-In" -> {
                    when (status.status) {
                        CredentialStatus.Status.ERROR -> {
                            recommendations.add("Check Google Sign-In configuration: SHA-1 fingerprints, OAuth client setup")
                            log.appendLine("📋 Recommend: Verify Google Sign-In configuration")
                        }
                        else -> {}
                    }
                }
                "App Configuration" -> {
                    when (status.status) {
                        CredentialStatus.Status.ERROR -> {
                            recommendations.add("Ensure google-services.json is properly configured and placed in app/ directory")
                            log.appendLine("📋 Recommend: Fix app configuration files")
                        }
                        else -> {}
                    }
                }
            }
        }

        if (recommendations.isEmpty()) {
            recommendations.add("All credentials appear to be configured correctly")
            log.appendLine("✓ No issues detected")
        }
    }

    private fun determineOverallStatus(statuses: List<CredentialStatus>): String {
        val hasErrors = statuses.any { it.status == CredentialStatus.Status.ERROR }
        val hasExpired = statuses.any { it.status == CredentialStatus.Status.EXPIRED }

        return when {
            hasErrors -> "CRITICAL - Configuration errors detected"
            hasExpired -> "WARNING - Credentials expired"
            else -> "HEALTHY - All systems operational"
        }
    }

    fun logDiagnosticReport(report: DiagnosticReport, logger: Logger? = null) {
        val log = logger ?: object : Logger {
            override fun d(tag: String, message: String) { android.util.Log.d(tag, message) }
            override fun i(tag: String, message: String) { android.util.Log.i(tag, message) }
            override fun w(tag: String, message: String) { android.util.Log.w(tag, message) }
            override fun e(tag: String, message: String, throwable: Throwable?) { android.util.Log.e(tag, message, throwable) }
        }

        log.i("CredentialDiagnostics", "=== DIAGNOSTIC SUMMARY ===")
        log.i("CredentialDiagnostics", "Overall Status: ${report.overallStatus}")

        report.credentialStatuses.forEach { status ->
            val emoji = when (status.status) {
                CredentialStatus.Status.VALID -> "✓"
                CredentialStatus.Status.EXPIRED -> "⚠️"
                CredentialStatus.Status.ERROR -> "❌"
                CredentialStatus.Status.UNKNOWN -> "❓"
            }
            log.i("CredentialDiagnostics", "$emoji ${status.name}: ${status.details}")
        }

        log.i("CredentialDiagnostics", "Recommendations:")
        report.recommendations.forEach { recommendation ->
            log.i("CredentialDiagnostics", "  • $recommendation")
        }

        log.d("CredentialDiagnostics", "Full Debug Log:\n${report.debugLog}")
    }
}