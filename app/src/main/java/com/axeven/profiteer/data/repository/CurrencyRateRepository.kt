package com.axeven.profiteer.data.repository

import com.axeven.profiteer.data.model.CurrencyRate
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
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val rates = snapshot?.toObjects(CurrencyRate::class.java) ?: emptyList()
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
}