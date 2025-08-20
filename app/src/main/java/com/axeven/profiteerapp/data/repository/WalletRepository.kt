package com.axeven.profiteerapp.data.repository

import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.data.model.PhysicalForm
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
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
                        val data = document.data
                        
                        // Check if wallet needs physical form migration
                        val needsMigration = data?.get("physicalForm") == null
                        
                        val wallet = document.toObject(Wallet::class.java)?.copy(id = document.id)
                        
                        // Perform migration if needed (async, don't block)
                        if (needsMigration && wallet != null) {
                            // Launch migration in background without blocking the listener
                            CoroutineScope(Dispatchers.IO).launch {
                                migrateWalletPhysicalForm(document.id, wallet.currency)
                            }
                        }
                        
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

    /**
     * Migrates a wallet document to include the physicalForm field.
     * This is done asynchronously and doesn't block the main data flow.
     */
    private suspend fun migrateWalletPhysicalForm(walletId: String, currency: String) {
        try {
            val defaultForm = PhysicalForm.getDefaultForCurrency(currency)
            val updateData = mapOf("physicalForm" to defaultForm.name)
            
            walletsCollection.document(walletId)
                .set(updateData, SetOptions.merge())
                .await()
            
            android.util.Log.d("WalletRepo", "Migrated wallet $walletId to physical form: ${defaultForm.name}")
        } catch (e: Exception) {
            android.util.Log.e("WalletRepo", "Failed to migrate wallet $walletId physical form", e)
        }
    }

    /**
     * Filters wallets by physical form for advanced portfolio management.
     */
    fun getUserWalletsByPhysicalForm(userId: String, physicalForm: PhysicalForm): Flow<List<Wallet>> = callbackFlow {
        val listener = walletsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("physicalForm", physicalForm.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val wallets = snapshot?.documents?.mapNotNull { document ->
                    try {
                        document.toObject(Wallet::class.java)?.copy(id = document.id)
                    } catch (e: Exception) {
                        android.util.Log.e("WalletRepo", "Error parsing wallet document: ${document.id}", e)
                        null
                    }
                }?.filter { it.id.isNotEmpty() }
                ?.sortedByDescending { it.createdAt ?: java.util.Date(0) } ?: emptyList()
                
                trySend(wallets)
            }
        
        awaitClose { listener.remove() }
    }

    /**
     * Gets wallets grouped by physical form for portfolio analysis.
     */
    suspend fun getUserWalletsGroupedByForm(userId: String): Result<Map<PhysicalForm, List<Wallet>>> {
        return try {
            val snapshot = walletsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            val wallets = snapshot.documents.mapNotNull { document ->
                try {
                    val data = document.data
                    val needsMigration = data?.get("physicalForm") == null
                    
                    val wallet = document.toObject(Wallet::class.java)?.copy(id = document.id)
                    
                    // Perform migration if needed
                    if (needsMigration && wallet != null) {
                        // Launch migration in background without blocking
                        CoroutineScope(Dispatchers.IO).launch {
                            migrateWalletPhysicalForm(document.id, wallet.currency)
                        }
                        // Return wallet with migrated form for immediate use
                        wallet.copy(physicalForm = PhysicalForm.getDefaultForCurrency(wallet.currency))
                    } else {
                        wallet
                    }
                } catch (e: Exception) {
                    android.util.Log.e("WalletRepo", "Error parsing wallet document: ${document.id}", e)
                    null
                }
            }.filter { it.id.isNotEmpty() }
            
            val groupedWallets = wallets.groupBy { it.physicalForm }
            Result.success(groupedWallets)
        } catch (e: Exception) {
            android.util.Log.e("WalletRepo", "Error getting grouped wallets", e)
            Result.failure(e)
        }
    }
}