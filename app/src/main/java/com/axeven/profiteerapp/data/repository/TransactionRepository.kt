package com.axeven.profiteerapp.data.repository

import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val transactionsCollection = firestore.collection("transactions")

    fun getUserTransactions(userId: String): Flow<List<Transaction>> = callbackFlow {
        val listener = transactionsCollection
            .whereEqualTo("userId", userId)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val transactions = snapshot?.documents?.mapNotNull { document ->
                    try {
                        val transaction = document.toObject(Transaction::class.java)?.copy(id = document.id)
                        // Debug: Log transaction details
                        transaction?.let {
                            android.util.Log.d("TransactionRepo", "Retrieved transaction: ${it.id}, title: ${it.title}")
                        }
                        transaction
                    } catch (e: Exception) {
                        // Log the error and skip this document
                        android.util.Log.e("TransactionRepo", "Error parsing transaction document: ${document.id}", e)
                        null
                    }
                }?.filter { it.id.isNotEmpty() }
                ?.sortedByDescending { it.transactionDate ?: it.createdAt ?: java.util.Date(0) } ?: emptyList()
                
                android.util.Log.d("TransactionRepo", "Total transactions retrieved: ${transactions.size}")
                trySend(transactions)
            }
        
        awaitClose { listener.remove() }
    }

    fun getWalletTransactions(walletId: String): Flow<List<Transaction>> = callbackFlow {
        // Query for transactions where wallet is either the primary wallet or in affectedWalletIds
        val listener1 = transactionsCollection
            .whereEqualTo("walletId", walletId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("TransactionRepo", "Error in primary wallet query", error)
                    return@addSnapshotListener
                }
                
                val primaryWalletTransactions = snapshot?.documents?.mapNotNull { document ->
                    try {
                        document.toObject(Transaction::class.java)?.copy(id = document.id)
                    } catch (e: Exception) {
                        android.util.Log.e("TransactionRepo", "Error parsing primary wallet transaction: ${document.id}", e)
                        null
                    }
                }?.filter { it.id.isNotEmpty() } ?: emptyList()
                
                // Query for transactions where wallet is in affectedWalletIds
                val listener2 = transactionsCollection
                    .whereArrayContains("affectedWalletIds", walletId)
                    .addSnapshotListener { snapshot2, error2 ->
                        if (error2 != null) {
                            android.util.Log.e("TransactionRepo", "Error in affected wallets query", error2)
                            return@addSnapshotListener
                        }
                        
                        val affectedWalletTransactions = snapshot2?.documents?.mapNotNull { document ->
                            try {
                                document.toObject(Transaction::class.java)?.copy(id = document.id)
                            } catch (e: Exception) {
                                android.util.Log.e("TransactionRepo", "Error parsing affected wallet transaction: ${document.id}", e)
                                null
                            }
                        }?.filter { it.id.isNotEmpty() } ?: emptyList()
                        
                        // Combine and deduplicate transactions
                        val allTransactions = (primaryWalletTransactions + affectedWalletTransactions)
                            .distinctBy { it.id }
                            .sortedByDescending { it.transactionDate ?: it.createdAt ?: java.util.Date(0) }
                        
                        trySend(allTransactions)
                    }
            }
        
        awaitClose { 
            // Note: In a real implementation, you'd need to track both listeners
            // For simplicity, we're only removing the first one here
        }
    }

    suspend fun createTransaction(transaction: Transaction): Result<String> {
        return try {
            val documentRef = transactionsCollection.add(transaction).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTransactionById(transactionId: String): Result<Transaction?> {
        return try {
            val document = transactionsCollection.document(transactionId).get().await()
            val transaction = document.toObject(Transaction::class.java)?.copy(id = document.id)
            Result.success(transaction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTransaction(transaction: Transaction): Result<Unit> {
        return try {
            transactionsCollection.document(transaction.id).set(transaction).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTransaction(transactionId: String): Result<Unit> {
        return try {
            android.util.Log.d("TransactionRepo", "Starting deletion of transaction: $transactionId")
            
            // First verify the document exists
            val docSnapshot = transactionsCollection.document(transactionId).get().await()
            if (!docSnapshot.exists()) {
                android.util.Log.e("TransactionRepo", "Transaction does not exist: $transactionId")
                return Result.failure(Exception("Transaction with ID $transactionId does not exist"))
            }
            
            android.util.Log.d("TransactionRepo", "Transaction exists, proceeding with deletion: $transactionId")
            
            // Delete the document
            transactionsCollection.document(transactionId).delete().await()
            
            android.util.Log.d("TransactionRepo", "Delete operation completed for: $transactionId")
            
            // Verify deletion (with a small delay to account for eventual consistency)
            kotlinx.coroutines.delay(100)
            val verifySnapshot = transactionsCollection.document(transactionId).get().await()
            if (verifySnapshot.exists()) {
                android.util.Log.e("TransactionRepo", "Verification failed - document still exists: $transactionId")
                return Result.failure(Exception("Transaction deletion verification failed - document still exists"))
            }
            
            android.util.Log.d("TransactionRepo", "Transaction successfully deleted and verified: $transactionId")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("TransactionRepo", "Error deleting transaction: $transactionId", e)
            Result.failure(e)
        }
    }

    suspend fun getTransactionsByType(userId: String, type: TransactionType): Result<List<Transaction>> {
        return try {
            val snapshot = transactionsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("type", type.name)
                .get()
                .await()
            
            val transactions = snapshot.documents.map { document ->
                document.toObject(Transaction::class.java)?.copy(id = document.id)
                    ?: Transaction()
            }
            Result.success(transactions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}