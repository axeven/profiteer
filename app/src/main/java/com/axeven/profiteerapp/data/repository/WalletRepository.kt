package com.axeven.profiteerapp.data.repository

import com.axeven.profiteerapp.data.model.Wallet
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
        android.util.Log.d("WalletRepo", "Setting up wallet listener for user: $userId")
        
        val listener = walletsCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("WalletRepo", "Wallet listener error for user $userId", error)
                    close(error)
                    return@addSnapshotListener
                }
                
                android.util.Log.d("WalletRepo", "Wallet snapshot received - documents: ${snapshot?.documents?.size ?: 0}")
                
                val wallets = snapshot?.documents?.mapNotNull { document ->
                    try {
                        android.util.Log.d("WalletRepo", "Processing wallet document: ${document.id}, exists: ${document.exists()}")
                        val wallet = document.toObject(Wallet::class.java)?.copy(id = document.id)
                        android.util.Log.d("WalletRepo", "Parsed wallet: ${wallet?.name}, userId: ${wallet?.userId}")
                        wallet
                    } catch (e: Exception) {
                        android.util.Log.e("WalletRepo", "Error parsing wallet document: ${document.id}", e)
                        null
                    }
                }?.filter { it.id.isNotEmpty() }
                ?.sortedByDescending { it.createdAt ?: java.util.Date(0) } ?: emptyList()
                
                android.util.Log.d("WalletRepo", "Final wallet list - count: ${wallets.size}, names: ${wallets.map { it.name }}")
                trySend(wallets)
            }
        
        awaitClose { 
            android.util.Log.d("WalletRepo", "Removing wallet listener for user: $userId")
            listener.remove() 
        }
    }

    suspend fun createWallet(wallet: Wallet): Result<String> {
        return try {
            android.util.Log.d("WalletRepo", "Creating wallet: ${wallet.name} for user: ${wallet.userId}")
            val documentRef = walletsCollection.add(wallet).await()
            android.util.Log.d("WalletRepo", "Wallet created successfully with ID: ${documentRef.id}")
            Result.success(documentRef.id)
        } catch (e: Exception) {
            android.util.Log.e("WalletRepo", "Error creating wallet: ${wallet.name}", e)
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
            val wallet = document.toObject(Wallet::class.java)?.copy(id = document.id)
            android.util.Log.d("WalletRepo", "Retrieved wallet by ID: $walletId, exists: ${document.exists()}")
            Result.success(wallet)
        } catch (e: Exception) {
            android.util.Log.e("WalletRepo", "Error getting wallet by ID: $walletId", e)
            Result.failure(e)
        }
    }
}