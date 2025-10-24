package com.axeven.profiteerapp

import android.annotation.SuppressLint
import android.util.Log
import com.axeven.profiteerapp.data.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Debug helper to test transaction operations during development
 */
@Singleton
class DebugTransactionHelper @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    
    @SuppressLint("LogNotTimber") // Debug-only helper class intentionally uses android.util.Log
    suspend fun verifyTransactionDeletion(userId: String, deletedTransactionId: String): Boolean {
        return try {
            Log.d("DebugTransactionHelper", "Verifying deletion of transaction: $deletedTransactionId")
            
            // Get current transactions
            val transactions = transactionRepository.getUserTransactions(userId).first()
            
            // Check if deleted transaction is still present
            val stillExists = transactions.any { it.id == deletedTransactionId }
            
            Log.d("DebugTransactionHelper", "Transaction $deletedTransactionId still exists: $stillExists")
            Log.d("DebugTransactionHelper", "Current transaction count: ${transactions.size}")
            
            // Log all current transaction IDs for debugging
            transactions.forEach { transaction ->
                Log.d("DebugTransactionHelper", "Current transaction: ${transaction.id} - ${transaction.title}")
            }
            
            !stillExists // Return true if transaction is NOT found (successfully deleted)
            
        } catch (e: Exception) {
            Log.e("DebugTransactionHelper", "Error verifying transaction deletion", e)
            false
        }
    }
}