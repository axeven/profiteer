package com.axeven.profiteerapp.data.repository

import com.axeven.profiteerapp.data.constants.RepositoryConstants
import com.axeven.profiteerapp.data.model.CurrencyRate
import com.axeven.profiteerapp.data.model.RepositoryError
import com.axeven.profiteerapp.data.model.toRepositoryError
import com.axeven.profiteerapp.service.AuthTokenManager
import com.axeven.profiteerapp.utils.FirestoreErrorHandler
import com.axeven.profiteerapp.utils.logging.Logger
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
class CurrencyRateRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authTokenManager: AuthTokenManager,
    private val logger: Logger
) {
    private val currencyRatesCollection = firestore.collection("currency_rates")

    fun getUserCurrencyRates(userId: String): Flow<List<CurrencyRate>> = callbackFlow {
        val listener = currencyRatesCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    logger.e("CurrencyRateRepo", "Currency rate listener error", error)
                    val errorInfo = FirestoreErrorHandler.handleError(error, logger)

                    // Handle authentication errors gracefully
                    if (errorInfo.requiresReauth) {
                        handleAuthenticationError("getUserCurrencyRates", error)
                    }

                    // Close Flow with RepositoryError instead of calling UI layer
                    val isOffline = FirestoreErrorHandler.shouldShowOfflineMessage(error)
                    val repositoryError = errorInfo.toRepositoryError(
                        operation = "getUserCurrencyRates",
                        isOffline = isOffline,
                        cause = error
                    )
                    close(repositoryError)
                    return@addSnapshotListener
                }
                
                val rates = snapshot?.documents?.mapNotNull { document ->
                    try {
                        document.toObject(CurrencyRate::class.java)?.copy(id = document.id)
                    } catch (e: Exception) {
                        logger.e("CurrencyRateRepo", "Error parsing rate document: ${document.id}", e)
                        null
                    }
                }?.filter { it.id.isNotEmpty() }
                ?.sortedByDescending { it.createdAt ?: java.util.Date(0) } ?: emptyList()
                
                trySend(rates)
            }
        
        awaitClose { listener.remove() }
    }

    suspend fun createCurrencyRate(rate: CurrencyRate): Result<String> {
        return try {
            val documentRef = currencyRatesCollection.add(rate).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrencyRateById(rateId: String): Result<CurrencyRate?> {
        return try {
            val document = currencyRatesCollection.document(rateId).get().await()
            val rate = document.toObject(CurrencyRate::class.java)
            Result.success(rate)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCurrencyRate(rate: CurrencyRate): Result<Unit> {
        return try {
            currencyRatesCollection.document(rate.id).set(rate).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCurrencyRate(rateId: String): Result<Unit> {
        return try {
            currencyRatesCollection.document(rateId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getExchangeRate(
        userId: String,
        fromCurrency: String,
        toCurrency: String,
        month: String? = null
    ): Result<CurrencyRate?> {
        return try {
            var query = currencyRatesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("fromCurrency", fromCurrency)
                .whereEqualTo("toCurrency", toCurrency)

            query = if (month != null) {
                query.whereEqualTo("month", month)
            } else {
                query.whereEqualTo("month", null)
            }

            val snapshot = query.limit(RepositoryConstants.SINGLE_RESULT_LIMIT.toLong()).get().await()
            val rate = snapshot.documents.firstOrNull()?.toObject(CurrencyRate::class.java)
            Result.success(rate)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets the display conversion rate from default currency to display currency.
     * This is specifically for UI display purposes only.
     */
    suspend fun getDisplayRate(
        userId: String,
        defaultCurrency: String,
        displayCurrency: String,
        month: String? = null
    ): Result<Double> {
        return try {
            // If currencies are the same, rate is 1.0
            if (defaultCurrency == displayCurrency) {
                return Result.success(1.0)
            }

            // First try direct rate
            val directRate = getExchangeRate(userId, defaultCurrency, displayCurrency, month)
            directRate.getOrNull()?.let {
                return Result.success(it.rate)
            }

            // Try inverse rate
            val inverseRate = getExchangeRate(userId, displayCurrency, defaultCurrency, month)
            inverseRate.getOrNull()?.let {
                return Result.success(1.0 / it.rate)
            }

            // If monthly rate not found, try default rate
            if (month != null) {
                return getDisplayRate(userId, defaultCurrency, displayCurrency, null)
            }

            // No rate found, return 1.0 as fallback
            Result.success(1.0)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun handleAuthenticationError(operation: String, error: Throwable) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                logger.w("CurrencyRateRepo", "Authentication error in $operation - attempting graceful recovery...")

                // First try to refresh the token
                val refreshSuccess = authTokenManager.attemptTokenRefresh()

                if (refreshSuccess) {
                    logger.i("CurrencyRateRepo", "Token refresh successful - operation may retry automatically")
                } else {
                    logger.w("CurrencyRateRepo", "Token refresh failed - triggering re-authentication flow")
                }
            } catch (e: Exception) {
                logger.e("CurrencyRateRepo", "Failed to handle authentication error gracefully", e)
            }
        }
    }
}