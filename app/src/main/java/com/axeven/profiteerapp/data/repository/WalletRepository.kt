package com.axeven.profiteerapp.data.repository

import com.axeven.profiteerapp.data.model.PhysicalForm
import com.axeven.profiteerapp.data.model.RepositoryError
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.data.model.toRepositoryError
import com.axeven.profiteerapp.service.AuthTokenManager
import com.axeven.profiteerapp.utils.FirestoreErrorHandler
import com.axeven.profiteerapp.utils.logging.Logger
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
    private val firestore: FirebaseFirestore,
    private val authTokenManager: AuthTokenManager,
    private val logger: Logger
) {
    private val walletsCollection = firestore.collection("wallets")

    fun getUserWallets(userId: String): Flow<List<Wallet>> = callbackFlow {
        logger.d("WalletRepo", "Setting up wallet listener for user: $userId")
        
        val listener = walletsCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    logger.e("WalletRepo", "Wallet listener error for user $userId", error)
                    val errorInfo = FirestoreErrorHandler.handleError(error, logger)

                    // Handle authentication errors gracefully
                    if (errorInfo.requiresReauth) {
                        handleAuthenticationError("getUserWallets", error)
                    }

                    // Close Flow with RepositoryError instead of calling UI layer
                    val isOffline = FirestoreErrorHandler.shouldShowOfflineMessage(error)
                    val repositoryError = errorInfo.toRepositoryError(
                        operation = "getUserWallets",
                        isOffline = isOffline,
                        cause = error
                    )
                    close(repositoryError)
                    return@addSnapshotListener
                }
                
                logger.d("WalletRepo", "Wallet snapshot received - documents: ${snapshot?.documents?.size ?: 0}")
                
                val wallets = snapshot?.documents?.mapNotNull { document ->
                    try {
                        logger.d("WalletRepo", "Processing wallet document: ${document.id}, exists: ${document.exists()}")
                        val data = document.data
                        
                        val wallet = document.toObject(Wallet::class.java)?.copy(id = document.id)
                        
                        logger.d("WalletRepo", "Parsed wallet: ${wallet?.name}, userId: ${wallet?.userId}")
                        wallet
                    } catch (e: Exception) {
                        logger.e("WalletRepo", "Error parsing wallet document: ${document.id}", e)
                        null
                    }
                }?.filter { it.id.isNotEmpty() }
                ?.sortedByDescending { it.createdAt ?: java.util.Date(0) } ?: emptyList()
                
                logger.d("WalletRepo", "Final wallet list - count: ${wallets.size}, names: ${wallets.map { it.name }}")
                trySend(wallets)
            }
        
        awaitClose { 
            logger.d("WalletRepo", "Removing wallet listener for user: $userId")
            listener.remove() 
        }
    }

    suspend fun createWallet(wallet: Wallet): Result<String> {
        return try {
            logger.d("WalletRepo", "Creating wallet: ${wallet.name} for user: ${wallet.userId}")
            val documentRef = walletsCollection.add(wallet).await()
            logger.d("WalletRepo", "Wallet created successfully with ID: ${documentRef.id}")
            Result.success(documentRef.id)
        } catch (e: Exception) {
            logger.e("WalletRepo", "Error creating wallet: ${wallet.name}", e)

            // Handle authentication errors gracefully
            val errorInfo = FirestoreErrorHandler.handleError(e, logger)
            if (errorInfo.requiresReauth) {
                handleAuthenticationError("createWallet", e)
            }

            Result.failure(e)
        }
    }

    suspend fun updateWallet(wallet: Wallet): Result<Unit> {
        return try {
            walletsCollection.document(wallet.id).set(wallet).await()
            Result.success(Unit)
        } catch (e: Exception) {
            logger.e("WalletRepo", "Error updating wallet: ${wallet.id}", e)

            // Handle authentication errors gracefully
            val errorInfo = FirestoreErrorHandler.handleError(e, logger)
            if (errorInfo.requiresReauth) {
                handleAuthenticationError("updateWallet", e)
            }

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
            logger.d("WalletRepo", "Retrieved wallet by ID: $walletId, exists: ${document.exists()}")
            Result.success(wallet)
        } catch (e: Exception) {
            logger.e("WalletRepo", "Error getting wallet by ID: $walletId", e)
            Result.failure(e)
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
                    logger.e("WalletRepo", "Wallet listener error (by physical form)", error)
                    val errorInfo = FirestoreErrorHandler.handleError(error, logger)

                    // Handle authentication errors gracefully
                    if (errorInfo.requiresReauth) {
                        handleAuthenticationError("getUserWalletsByPhysicalForm", error)
                    }

                    // Close Flow with RepositoryError instead of calling UI layer
                    val isOffline = FirestoreErrorHandler.shouldShowOfflineMessage(error)
                    val repositoryError = errorInfo.toRepositoryError(
                        operation = "getUserWalletsByPhysicalForm",
                        isOffline = isOffline,
                        cause = error
                    )
                    close(repositoryError)
                    return@addSnapshotListener
                }
                
                val wallets = snapshot?.documents?.mapNotNull { document ->
                    try {
                        document.toObject(Wallet::class.java)?.copy(id = document.id)
                    } catch (e: Exception) {
                        logger.e("WalletRepo", "Error parsing wallet document: ${document.id}", e)
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
                    document.toObject(Wallet::class.java)?.copy(id = document.id)
                } catch (e: Exception) {
                    logger.e("WalletRepo", "Error parsing wallet document: ${document.id}", e)
                    null
                }
            }.filter { it.id.isNotEmpty() }
            
            val groupedWallets = wallets.groupBy { it.physicalForm }
            Result.success(groupedWallets)
        } catch (e: Exception) {
            logger.e("WalletRepo", "Error getting grouped wallets", e)
            Result.failure(e)
        }
    }

    private fun handleAuthenticationError(operation: String, error: Throwable) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                logger.w("WalletRepo", "Authentication error in $operation - attempting graceful recovery...")

                // First try to refresh the token
                val refreshSuccess = authTokenManager.attemptTokenRefresh()

                if (refreshSuccess) {
                    logger.i("WalletRepo", "Token refresh successful - operation may retry automatically")
                } else {
                    logger.w("WalletRepo", "Token refresh failed - triggering re-authentication flow")
                }
            } catch (e: Exception) {
                logger.e("WalletRepo", "Failed to handle authentication error gracefully", e)
            }
        }
    }
}