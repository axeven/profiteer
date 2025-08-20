package com.axeven.profiteerapp.data.repository

import com.axeven.profiteerapp.data.model.CurrencyRate
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyRateRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val currencyRatesCollection = firestore.collection("currency_rates")

    fun getUserCurrencyRates(userId: String): Flow<List<CurrencyRate>> = callbackFlow {
        val listener = currencyRatesCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val rates = snapshot?.documents?.mapNotNull { document ->
                    try {
                        document.toObject(CurrencyRate::class.java)?.copy(id = document.id)
                    } catch (e: Exception) {
                        android.util.Log.e("CurrencyRateRepo", "Error parsing rate document: ${document.id}", e)
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

            val snapshot = query.limit(1).get().await()
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
}