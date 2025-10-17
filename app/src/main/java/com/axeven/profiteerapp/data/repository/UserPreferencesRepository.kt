package com.axeven.profiteerapp.data.repository

import com.axeven.profiteerapp.data.constants.RepositoryConstants
import com.axeven.profiteerapp.data.model.RepositoryError
import com.axeven.profiteerapp.data.model.UserPreferences
import com.axeven.profiteerapp.data.model.toRepositoryError
import com.axeven.profiteerapp.service.AuthTokenManager
import com.axeven.profiteerapp.utils.FirestoreErrorHandler
import com.axeven.profiteerapp.utils.logging.Logger
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authTokenManager: AuthTokenManager,
    private val logger: Logger
) {
    private val preferencesCollection = firestore.collection("user_preferences")

    fun getUserPreferences(userId: String): Flow<UserPreferences?> = callbackFlow {
        val listener = preferencesCollection
            .whereEqualTo("userId", userId)
            .limit(RepositoryConstants.SINGLE_RESULT_LIMIT.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    logger.e("UserPreferencesRepo", "User preferences listener error", error)
                    val errorInfo = FirestoreErrorHandler.handleError(error, logger)

                    // Handle authentication errors gracefully
                    if (errorInfo.requiresReauth) {
                        handleAuthenticationError("getUserPreferences", error)
                    }

                    // Close Flow with RepositoryError instead of calling UI layer
                    val isOffline = FirestoreErrorHandler.shouldShowOfflineMessage(error)
                    val repositoryError = errorInfo.toRepositoryError(
                        operation = "getUserPreferences",
                        isOffline = isOffline,
                        cause = error
                    )
                    close(repositoryError)
                    return@addSnapshotListener
                }
                
                val preferences = snapshot?.documents?.firstOrNull()?.toObject(UserPreferences::class.java)
                trySend(preferences)
            }
        
        awaitClose { listener.remove() }
    }

    suspend fun createOrUpdatePreferences(preferences: UserPreferences): Result<String> {
        return try {
            // Check if preferences already exist
            val existingSnapshot = preferencesCollection
                .whereEqualTo("userId", preferences.userId)
                .limit(RepositoryConstants.SINGLE_RESULT_LIMIT.toLong())
                .get()
                .await()

            if (existingSnapshot.documents.isNotEmpty()) {
                // Update existing preferences
                val docId = existingSnapshot.documents.first().id
                preferencesCollection.document(docId).set(preferences).await()
                Result.success(docId)
            } else {
                // Create new preferences
                val documentRef = preferencesCollection.add(preferences).await()
                Result.success(documentRef.id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDefaultCurrency(userId: String, currency: String): Result<Unit> {
        return try {
            val snapshot = preferencesCollection
                .whereEqualTo("userId", userId)
                .limit(RepositoryConstants.SINGLE_RESULT_LIMIT.toLong())
                .get()
                .await()

            if (snapshot.documents.isNotEmpty()) {
                val docId = snapshot.documents.first().id
                val existingPrefs = snapshot.documents.first().toObject(UserPreferences::class.java)

                // If displayCurrency is still the default "USD", update it to match the new defaultCurrency
                val updates = if (existingPrefs?.displayCurrency == RepositoryConstants.DEFAULT_CURRENCY) {
                    mapOf(
                        "defaultCurrency" to currency,
                        "displayCurrency" to currency  // Auto-update displayCurrency to match
                    )
                } else {
                    mapOf("defaultCurrency" to currency)  // Keep existing displayCurrency if it was explicitly set
                }
                
                preferencesCollection.document(docId)
                    .update(updates)
                    .await()
            } else {
                // Create new preferences if none exist - set displayCurrency to match defaultCurrency
                val newPreferences = UserPreferences(
                    userId = userId,
                    defaultCurrency = currency,
                    displayCurrency = currency  // Match displayCurrency to defaultCurrency
                )
                preferencesCollection.add(newPreferences).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDisplayCurrency(userId: String, currency: String): Result<Unit> {
        return try {
            val snapshot = preferencesCollection
                .whereEqualTo("userId", userId)
                .limit(RepositoryConstants.SINGLE_RESULT_LIMIT.toLong())
                .get()
                .await()

            if (snapshot.documents.isNotEmpty()) {
                val docId = snapshot.documents.first().id
                preferencesCollection.document(docId)
                    .update("displayCurrency", currency)
                    .await()
            } else {
                // Create new preferences if none exist
                val newPreferences = UserPreferences(
                    userId = userId,
                    displayCurrency = currency
                )
                preferencesCollection.add(newPreferences).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBothCurrencies(userId: String, defaultCurrency: String, displayCurrency: String): Result<Unit> {
        return try {
            val snapshot = preferencesCollection
                .whereEqualTo("userId", userId)
                .limit(RepositoryConstants.SINGLE_RESULT_LIMIT.toLong())
                .get()
                .await()

            if (snapshot.documents.isNotEmpty()) {
                val docId = snapshot.documents.first().id
                val updates = mapOf(
                    "defaultCurrency" to defaultCurrency,
                    "displayCurrency" to displayCurrency
                )
                preferencesCollection.document(docId)
                    .update(updates)
                    .await()
            } else {
                // Create new preferences if none exist
                val newPreferences = UserPreferences(
                    userId = userId,
                    defaultCurrency = defaultCurrency,
                    displayCurrency = displayCurrency
                )
                preferencesCollection.add(newPreferences).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sync displayCurrency to match defaultCurrency if displayCurrency is still "USD"
     * This fixes the issue where users have IDR as defaultCurrency but displayCurrency is still "USD"
     */
    suspend fun syncDisplayCurrencyWithDefault(userId: String): Result<Unit> {
        return try {
            val snapshot = preferencesCollection
                .whereEqualTo("userId", userId)
                .limit(RepositoryConstants.SINGLE_RESULT_LIMIT.toLong())
                .get()
                .await()

            if (snapshot.documents.isNotEmpty()) {
                val docId = snapshot.documents.first().id
                val existingPrefs = snapshot.documents.first().toObject(UserPreferences::class.java)

                // If displayCurrency is USD but defaultCurrency is something else, sync them
                if (existingPrefs?.displayCurrency == RepositoryConstants.DEFAULT_CURRENCY &&
                    existingPrefs.defaultCurrency != RepositoryConstants.DEFAULT_CURRENCY) {
                    preferencesCollection.document(docId)
                        .update("displayCurrency", existingPrefs.defaultCurrency)
                        .await()
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun handleAuthenticationError(operation: String, error: Throwable) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                logger.w("UserPreferencesRepo", "Authentication error in $operation - attempting graceful recovery...")

                // First try to refresh the token
                val refreshSuccess = authTokenManager.attemptTokenRefresh()

                if (refreshSuccess) {
                    logger.i("UserPreferencesRepo", "Token refresh successful - operation may retry automatically")
                } else {
                    logger.w("UserPreferencesRepo", "Token refresh failed - triggering re-authentication flow")
                }
            } catch (e: Exception) {
                logger.e("UserPreferencesRepo", "Failed to handle authentication error gracefully", e)
            }
        }
    }
}