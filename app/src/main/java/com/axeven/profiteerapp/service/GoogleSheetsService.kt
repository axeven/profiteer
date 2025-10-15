package com.axeven.profiteerapp.service

import android.content.Context
import android.content.Intent
import com.axeven.profiteerapp.data.repository.AuthRepository
import com.axeven.profiteerapp.utils.logging.Logger
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for interacting with Google Sheets API.
 *
 * Handles authorization, credential management, and Sheets API service creation.
 * Follows TDD approach with comprehensive test coverage.
 */
@Singleton
class GoogleSheetsService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val logger: Logger
) {

    companion object {
        private const val APP_NAME = "Profiteer"
    }

    /**
     * Check if the user is authorized to access Google Sheets.
     *
     * @return true if user has granted Sheets scope, false otherwise
     */
    fun isAuthorized(): Boolean {
        logger.d("GoogleSheetsService", "Checking Google Sheets authorization")
        return authRepository.hasSheetsScope()
    }

    /**
     * Request authorization for Google Sheets access.
     *
     * Returns an Intent that should be launched to request the necessary permissions.
     * The calling Activity should handle the result in onActivityResult().
     *
     * @return Intent to launch for authorization
     */
    fun requestAuthorization(): Intent {
        logger.d("GoogleSheetsService", "Requesting Google Sheets authorization")
        val client = authRepository.getGoogleSignInClientWithSheets()
        return client.signInIntent
    }

    /**
     * Create a Google Sheets service instance with the current user's credentials.
     *
     * @return Result containing Sheets service instance or exception if not authorized
     */
    fun createSheetsService(): Result<Sheets> {
        return try {
            logger.d("GoogleSheetsService", "Creating Google Sheets service")

            if (!isAuthorized()) {
                val exception = SecurityException("User is not authorized for Google Sheets")
                logger.e("GoogleSheetsService", "Failed to create Google Sheets service: not authorized", exception)
                return Result.failure(exception)
            }

            val userEmail = authRepository.getCurrentUserEmail()
            if (userEmail == null) {
                val exception = SecurityException("User email not available")
                logger.e("GoogleSheetsService", "Failed to create Google Sheets service: no email", exception)
                return Result.failure(exception)
            }

            // Create credentials
            val credential = GoogleAccountCredential.usingOAuth2(
                context,
                listOf(SheetsScopes.SPREADSHEETS)
            ).apply {
                selectedAccount = com.google.android.gms.auth.GoogleAuthUtil.getAccountId(context, userEmail)
                    ?.let { android.accounts.Account(userEmail, "com.google") }
            }

            // Create Sheets service
            val service = Sheets.Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            )
                .setApplicationName(APP_NAME)
                .build()

            logger.i("GoogleSheetsService", "Google Sheets service created successfully for user: $userEmail")
            Result.success(service)

        } catch (e: Exception) {
            logger.e("GoogleSheetsService", "Failed to create Google Sheets service", e)
            Result.failure(e)
        }
    }
}
