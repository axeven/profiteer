package com.axeven.profiteer.data.repository

import com.axeven.profiteer.data.model.Wallet
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val walletsCollection = firestore.collection("wallets")

    fun getUserWallets(userId: String): Flow<List<Wallet>> = callbackFlow {
        val listener = walletsCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val wallets = snapshot?.toObjects(Wallet::class.java) ?: emptyList()
                trySend(wallets)
            }
        
        awaitClose { listener.remove() }
    }

    suspend fun createWallet(wallet: Wallet): Result<String> {
        return try {
            val documentRef = walletsCollection.add(wallet).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateWallet(wallet: Wallet): Result<Unit> {
        return try {
            walletsCollection.document(wallet.id).set(wallet).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteWallet(walletId: String): Result<Unit> {
        return try {
            walletsCollection.document(walletId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWalletById(walletId: String): Result<Wallet?> {
        return try {
            val document = walletsCollection.document(walletId).get().await()
            val wallet = document.toObject(Wallet::class.java)
            Result.success(wallet)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}