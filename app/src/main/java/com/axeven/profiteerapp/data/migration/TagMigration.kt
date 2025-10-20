package com.axeven.profiteerapp.data.migration

import com.axeven.profiteerapp.data.repository.TransactionRepository
import com.axeven.profiteerapp.utils.TagNormalizer
import com.axeven.profiteerapp.utils.logging.Logger
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for migrating existing transaction tags to normalized format.
 *
 * This migration:
 * - Converts tags to lowercase
 * - Trims whitespace
 * - Removes case-insensitive duplicates
 * - Filters out "Untagged" keyword
 * - Filters out blank tags
 *
 * The migration is idempotent - it can be run multiple times safely.
 * Transactions with already-normalized tags are skipped.
 *
 * Usage:
 * ```kotlin
 * val result = tagMigration.migrateTransactionTags(userId)
 * result.onSuccess { count ->
 *     logger.i("Migration", "Migrated $count transactions")
 * }
 * ```
 */
@Singleton
class TagMigration @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val logger: Logger
) {

    /**
     * Migrates all transaction tags for a user to normalized format.
     *
     * @param userId User ID to migrate transactions for
     * @return Result containing the count of transactions updated, or error
     */
    suspend fun migrateTransactionTags(userId: String): Result<Int> {
        return try {
            logger.i("TagMigration", "Starting tag migration for user: $userId")

            // Fetch ALL user transactions (no limit) for migration
            // Note: getUserTransactions has a 20-item limit for UI display,
            // but getUserTransactionsForCalculations fetches all transactions
            val transactions = transactionRepository.getUserTransactionsForCalculations(userId).first()

            logger.d("TagMigration", "Found ${transactions.size} transactions to process")

            var updatedCount = 0

            // Process each transaction
            for (transaction in transactions) {
                // Skip transactions with empty tags
                if (transaction.tags.isEmpty()) {
                    logger.d("TagMigration", "Skipping transaction ${transaction.id} (no tags)")
                    continue
                }

                // Normalize the tags
                val normalizedTags = TagNormalizer.normalizeTags(transaction.tags)

                // Check if tags actually changed (avoid unnecessary updates)
                if (normalizedTags == transaction.tags) {
                    logger.d("TagMigration", "Skipping transaction ${transaction.id} (already normalized)")
                    continue
                }

                // Update transaction with normalized tags
                val updatedTransaction = transaction.copy(tags = normalizedTags)
                val updateResult = transactionRepository.updateTransaction(updatedTransaction)

                if (updateResult.isSuccess) {
                    updatedCount++
                    logger.d(
                        "TagMigration",
                        "Updated transaction ${transaction.id}: ${transaction.tags} -> $normalizedTags"
                    )
                } else {
                    // Log error but don't fail entire migration
                    logger.e(
                        "TagMigration",
                        "Failed to update transaction ${transaction.id}",
                        updateResult.exceptionOrNull()
                    )
                    // Return failure on first error
                    return Result.failure(
                        Exception("Migration failed at transaction ${transaction.id}: ${updateResult.exceptionOrNull()?.message}")
                    )
                }
            }

            logger.i("TagMigration", "Tag migration completed: $updatedCount transactions updated")
            Result.success(updatedCount)

        } catch (e: Exception) {
            logger.e("TagMigration", "Tag migration failed", e)
            Result.failure(e)
        }
    }

    /**
     * Checks if migration is needed for a user (has any non-normalized tags).
     *
     * This can be used to determine if migration should be triggered.
     *
     * @param userId User ID to check
     * @return True if migration is needed, false if all tags are normalized
     */
    suspend fun isMigrationNeeded(userId: String): Boolean {
        return try {
            // Fetch ALL transactions to check (no limit)
            val transactions = transactionRepository.getUserTransactionsForCalculations(userId).first()

            // Check if any transaction has non-normalized tags
            transactions.any { transaction ->
                transaction.tags.isNotEmpty() &&
                TagNormalizer.normalizeTags(transaction.tags) != transaction.tags
            }
        } catch (e: Exception) {
            logger.e("TagMigration", "Error checking if migration needed", e)
            false
        }
    }
}
