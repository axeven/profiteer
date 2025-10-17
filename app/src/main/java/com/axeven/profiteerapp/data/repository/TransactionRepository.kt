package com.axeven.profiteerapp.data.repository

import android.content.Context
import com.axeven.profiteerapp.data.constants.RepositoryConstants
import com.axeven.profiteerapp.data.model.RepositoryError
import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.data.model.TransactionType
import com.axeven.profiteerapp.data.model.toRepositoryError
import com.axeven.profiteerapp.service.AuthTokenManager
import com.axeven.profiteerapp.utils.CredentialDiagnostics
import com.axeven.profiteerapp.utils.FirestoreErrorHandler
import com.axeven.profiteerapp.utils.logging.Logger
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authTokenManager: AuthTokenManager,
    private val logger: Logger,
    @ApplicationContext private val context: Context
) {
    private val transactionsCollection = firestore.collection("transactions")

    fun getUserTransactions(userId: String): Flow<List<Transaction>> = callbackFlow {
        val listener = transactionsCollection
            .whereEqualTo("userId", userId)
            .orderBy("transactionDate", Query.Direction.DESCENDING)
            .limit(RepositoryConstants.TRANSACTION_PAGE_SIZE.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    val errorInfo = FirestoreErrorHandler.handleError(error, logger)

                    // Handle authentication errors gracefully
                    if (errorInfo.requiresReauth) {
                        handleAuthenticationError("getUserTransactions", error)
                    }

                    // Close Flow with RepositoryError instead of calling UI layer
                    val isOffline = FirestoreErrorHandler.shouldShowOfflineMessage(error)
                    val repositoryError = errorInfo.toRepositoryError(
                        operation = "getUserTransactions",
                        isOffline = isOffline,
                        cause = error
                    )
                    close(repositoryError)
                    return@addSnapshotListener
                }
                
                val transactions = snapshot?.documents?.mapNotNull { document ->
                    try {
                        val transaction = document.toObject(Transaction::class.java)?.copy(id = document.id)
                        // Debug: Log transaction details
                        transaction?.let {
                            logger.d("TransactionRepo", "Retrieved transaction: ${it.id}, title: ${it.title}")
                        }
                        transaction
                    } catch (e: Exception) {
                        // Log the error and skip this document
                        logger.e("TransactionRepo", "Error parsing transaction document: ${document.id}", e)
                        null
                    }
                }?.filter { it.id.isNotEmpty() } ?: emptyList()
                
                logger.d("TransactionRepo", "Total transactions retrieved: ${transactions.size}")
                trySend(transactions)
            }
        
        awaitClose { listener.remove() }
    }

    fun getUserTransactionsForCalculations(userId: String, startDate: Date? = null, endDate: Date? = null): Flow<List<Transaction>> = callbackFlow {
        logger.d("TransactionRepo", "Starting getUserTransactionsForCalculations for user: $userId")
        
        var query = transactionsCollection
            .whereEqualTo("userId", userId)
        
        // Add date filtering if provided
        if (startDate != null) {
            query = query.whereGreaterThanOrEqualTo("transactionDate", startDate)
        }
        if (endDate != null) {
            query = query.whereLessThanOrEqualTo("transactionDate", endDate)
        }
        
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                logger.e("TransactionRepo", "Error in calculation query", error)
                val errorInfo = FirestoreErrorHandler.handleError(error, logger)

                // Handle authentication errors gracefully
                if (errorInfo.requiresReauth) {
                    handleAuthenticationError("getUserTransactionsForCalculations", error)
                }

                // Close Flow with RepositoryError instead of calling UI layer
                val isOffline = FirestoreErrorHandler.shouldShowOfflineMessage(error)
                val repositoryError = errorInfo.toRepositoryError(
                    operation = "getUserTransactionsForCalculations",
                    isOffline = isOffline,
                    cause = error
                )
                close(repositoryError)
                return@addSnapshotListener
            }
            
            val transactions = snapshot?.documents?.mapNotNull { document ->
                try {
                    val transaction = document.toObject(Transaction::class.java)?.copy(id = document.id)
                    // For calculation purposes, we need all transactions regardless of date
                    // If no transactionDate, use createdAt, if neither exists, still include but note in logs
                    transaction?.let {
                        if (it.transactionDate == null && it.createdAt == null) {
                            logger.w("TransactionRepo", "Transaction ${it.id} has no date information")
                        }
                        it
                    }
                } catch (e: Exception) {
                    logger.e("TransactionRepo", "Error parsing calculation transaction: ${document.id}", e)
                    null
                }
            }?.filter { it.id.isNotEmpty() } ?: emptyList()
            
            logger.d("TransactionRepo", "Calculation transactions retrieved: ${transactions.size} for user $userId")
            trySend(transactions)
        }
        
        awaitClose { listener.remove() }
    }

    fun getWalletTransactions(walletId: String, userId: String): Flow<List<Transaction>> = callbackFlow {
        var primaryWalletTransactions = emptyList<Transaction>()
        var affectedWalletTransactions = emptyList<Transaction>()
        var sourceWalletTransactions = emptyList<Transaction>()
        var destinationWalletTransactions = emptyList<Transaction>()
        
        fun combineAndEmitTransactions() {
            val allTransactions = (primaryWalletTransactions + affectedWalletTransactions + 
                                  sourceWalletTransactions + destinationWalletTransactions)
                .distinctBy { it.id }
                .sortedByDescending { it.transactionDate ?: it.createdAt ?: java.util.Date(0) }
            
            logger.d("TransactionRepo", "Combined transactions for wallet $walletId: ${allTransactions.size} " +
                "(primary: ${primaryWalletTransactions.size}, affected: ${affectedWalletTransactions.size}, " +
                "source: ${sourceWalletTransactions.size}, destination: ${destinationWalletTransactions.size})")
            
            trySend(allTransactions)
        }
        
        // Query 1: Transactions where wallet is the primary wallet
        val listener1 = transactionsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("walletId", walletId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    logger.e("TransactionRepo", "Error in primary wallet query", error)
                    val errorInfo = FirestoreErrorHandler.handleError(error, logger)

                    // Handle authentication errors gracefully
                    if (errorInfo.requiresReauth) {
                        handleAuthenticationError("getWalletTransactions:primaryWallet", error)
                    }

                    // Log error but continue - partial results from other queries may still be useful
                    logger.w("TransactionRepo", "Primary wallet query failed: ${errorInfo.userMessage}")
                    return@addSnapshotListener
                }
                
                primaryWalletTransactions = snapshot?.documents?.mapNotNull { document ->
                    try {
                        document.toObject(Transaction::class.java)?.copy(id = document.id)
                    } catch (e: Exception) {
                        logger.e("TransactionRepo", "Error parsing primary wallet transaction: ${document.id}", e)
                        null
                    }
                }?.filter { it.id.isNotEmpty() } ?: emptyList()
                
                combineAndEmitTransactions()
            }
        
        // Query 2: Transactions where wallet is in affectedWalletIds
        val listener2 = transactionsCollection
            .whereEqualTo("userId", userId)
            .whereArrayContains("affectedWalletIds", walletId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    logger.e("TransactionRepo", "Error in affected wallets query", error)
                    val errorInfo = FirestoreErrorHandler.handleError(error, logger)

                    // Handle authentication errors gracefully
                    if (errorInfo.requiresReauth) {
                        handleAuthenticationError("getWalletTransactions:affectedWallets", error)
                    }

                    // Log error but continue - partial results from other queries may still be useful
                    logger.w("TransactionRepo", "Affected wallets query failed: ${errorInfo.userMessage}")
                    return@addSnapshotListener
                }
                
                affectedWalletTransactions = snapshot?.documents?.mapNotNull { document ->
                    try {
                        document.toObject(Transaction::class.java)?.copy(id = document.id)
                    } catch (e: Exception) {
                        logger.e("TransactionRepo", "Error parsing affected wallet transaction: ${document.id}", e)
                        null
                    }
                }?.filter { it.id.isNotEmpty() } ?: emptyList()
                
                combineAndEmitTransactions()
            }
        
        // Query 3: Transfer transactions where wallet is the source
        val listener3 = transactionsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("sourceWalletId", walletId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    logger.e("TransactionRepo", "Error in source wallet query", error)
                    val errorInfo = FirestoreErrorHandler.handleError(error, logger)

                    // Handle authentication errors gracefully
                    if (errorInfo.requiresReauth) {
                        handleAuthenticationError("getWalletTransactions:sourceWallet", error)
                    }

                    // Log error but continue - partial results from other queries may still be useful
                    logger.w("TransactionRepo", "Source wallet query failed: ${errorInfo.userMessage}")
                    return@addSnapshotListener
                }
                
                sourceWalletTransactions = snapshot?.documents?.mapNotNull { document ->
                    try {
                        document.toObject(Transaction::class.java)?.copy(id = document.id)
                    } catch (e: Exception) {
                        logger.e("TransactionRepo", "Error parsing source wallet transaction: ${document.id}", e)
                        null
                    }
                }?.filter { it.id.isNotEmpty() } ?: emptyList()
                
                logger.d("TransactionRepo", "Source wallet transactions for $walletId: ${sourceWalletTransactions.size}")
                combineAndEmitTransactions()
            }
        
        // Query 4: Transfer transactions where wallet is the destination
        val listener4 = transactionsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("destinationWalletId", walletId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    logger.e("TransactionRepo", "Error in destination wallet query", error)
                    val errorInfo = FirestoreErrorHandler.handleError(error, logger)

                    // Handle authentication errors gracefully
                    if (errorInfo.requiresReauth) {
                        handleAuthenticationError("getWalletTransactions:destinationWallet", error)
                    }

                    // Log error but continue - partial results from other queries may still be useful
                    logger.w("TransactionRepo", "Destination wallet query failed: ${errorInfo.userMessage}")
                    return@addSnapshotListener
                }
                
                destinationWalletTransactions = snapshot?.documents?.mapNotNull { document ->
                    try {
                        document.toObject(Transaction::class.java)?.copy(id = document.id)
                    } catch (e: Exception) {
                        logger.e("TransactionRepo", "Error parsing destination wallet transaction: ${document.id}", e)
                        null
                    }
                }?.filter { it.id.isNotEmpty() } ?: emptyList()
                
                logger.d("TransactionRepo", "Destination wallet transactions for $walletId: ${destinationWalletTransactions.size}")
                combineAndEmitTransactions()
            }
        
        awaitClose { 
            listener1.remove()
            listener2.remove()
            listener3.remove()
            listener4.remove()
        }
    }

    suspend fun createTransaction(transaction: Transaction): Result<String> {
        return try {
            val documentRef = transactionsCollection.add(transaction).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            logger.e("TransactionRepo", "Error creating transaction: ${transaction.title}", e)

            // Handle authentication errors gracefully
            val errorInfo = FirestoreErrorHandler.handleError(e, logger)
            if (errorInfo.requiresReauth) {
                handleAuthenticationError("createTransaction", e)
            }

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
            logger.e("TransactionRepo", "Error updating transaction: ${transaction.id}", e)

            // Handle authentication errors gracefully
            val errorInfo = FirestoreErrorHandler.handleError(e, logger)
            if (errorInfo.requiresReauth) {
                handleAuthenticationError("updateTransaction", e)
            }

            Result.failure(e)
        }
    }

    suspend fun deleteTransaction(transactionId: String): Result<Unit> {
        return try {
            logger.d("TransactionRepo", "Starting deletion of transaction: $transactionId")
            
            // First verify the document exists
            val docSnapshot = transactionsCollection.document(transactionId).get().await()
            if (!docSnapshot.exists()) {
                logger.e("TransactionRepo", "Transaction does not exist: $transactionId")
                return Result.failure(Exception("Transaction with ID $transactionId does not exist"))
            }
            
            logger.d("TransactionRepo", "Transaction exists, proceeding with deletion: $transactionId")
            
            // Delete the document
            transactionsCollection.document(transactionId).delete().await()
            
            logger.d("TransactionRepo", "Delete operation completed for: $transactionId")
            
            // Verify deletion (with a small delay to account for eventual consistency)
            kotlinx.coroutines.delay(100)
            val verifySnapshot = transactionsCollection.document(transactionId).get().await()
            if (verifySnapshot.exists()) {
                logger.e("TransactionRepo", "Verification failed - document still exists: $transactionId")
                return Result.failure(Exception("Transaction deletion verification failed - document still exists"))
            }
            
            logger.d("TransactionRepo", "Transaction successfully deleted and verified: $transactionId")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.e("TransactionRepo", "Error deleting transaction: $transactionId", e)
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

    /**
     * Gets all transactions for a user in chronological order (oldest first).
     * This method is designed for discrepancy analysis and running balance calculations.
     *
     * Security: userId filter is applied first as per Firebase security rules.
     * Ordering: Transactions are ordered by transactionDate ascending (oldest to newest).
     * Real-time: Returns Flow for reactive updates.
     *
     * @param userId User ID to filter transactions
     * @return Flow emitting list of transactions in chronological order
     */
    fun getAllTransactionsChronological(userId: String): Flow<List<Transaction>> = callbackFlow {
        logger.d("TransactionRepo", "Setting up chronological transaction listener for user: $userId")

        val listener = transactionsCollection
            .whereEqualTo("userId", userId) // Security: userId filter MUST be first
            .orderBy("transactionDate", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    logger.e("TransactionRepo", "Error in chronological transaction query", error)
                    val errorInfo = FirestoreErrorHandler.handleError(error, logger)

                    // Handle authentication errors gracefully
                    if (errorInfo.requiresReauth) {
                        handleAuthenticationError("getAllTransactionsChronological", error)
                    }

                    // Close Flow with RepositoryError instead of calling UI layer
                    val isOffline = FirestoreErrorHandler.shouldShowOfflineMessage(error)
                    val repositoryError = errorInfo.toRepositoryError(
                        operation = "getAllTransactionsChronological",
                        isOffline = isOffline,
                        cause = error
                    )
                    close(repositoryError)
                    return@addSnapshotListener
                }

                val transactions = snapshot?.documents?.mapNotNull { document ->
                    try {
                        document.toObject(Transaction::class.java)?.copy(id = document.id)
                    } catch (e: Exception) {
                        logger.e("TransactionRepo", "Error parsing chronological transaction: ${document.id}", e)
                        null
                    }
                }?.filter { it.id.isNotEmpty() } ?: emptyList()

                logger.d("TransactionRepo", "Chronological transactions retrieved: ${transactions.size} for user $userId")
                trySend(transactions)
            }

        awaitClose {
            logger.d("TransactionRepo", "Removing chronological transaction listener for user: $userId")
            listener.remove()
        }
    }

    private fun handleAuthenticationError(operation: String, error: Throwable) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                logger.w("TransactionRepo", "Authentication error in $operation - attempting graceful recovery...")

                // First try to refresh the token
                val refreshSuccess = authTokenManager.attemptTokenRefresh()

                if (refreshSuccess) {
                    logger.i("TransactionRepo", "Token refresh successful - operation may retry automatically")
                } else {
                    logger.w("TransactionRepo", "Token refresh failed - triggering re-authentication flow")

                    // Run diagnostics for detailed troubleshooting
                    val report = CredentialDiagnostics.runFullDiagnostics(context)
                    CredentialDiagnostics.logDiagnosticReport(report, logger)
                }
            } catch (e: Exception) {
                logger.e("TransactionRepo", "Failed to handle authentication error gracefully", e)
            }
        }
    }

    private fun triggerCredentialDiagnostics(operation: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                logger.w("TransactionRepo", "Authentication error in $operation - running diagnostics...")
                val report = CredentialDiagnostics.runFullDiagnostics(context)
                CredentialDiagnostics.logDiagnosticReport(report, logger)
            } catch (e: Exception) {
                logger.e("TransactionRepo", "Failed to run credential diagnostics", e)
            }
        }
    }
}