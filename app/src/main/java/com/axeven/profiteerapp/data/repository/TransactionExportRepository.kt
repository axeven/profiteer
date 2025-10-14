package com.axeven.profiteerapp.data.repository

import com.axeven.profiteerapp.data.model.Transaction
import com.axeven.profiteerapp.service.GoogleSheetsExporter
import com.axeven.profiteerapp.service.GoogleSheetsService
import com.axeven.profiteerapp.utils.ExportFormatter
import com.axeven.profiteerapp.utils.logging.Logger
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for handling transaction export operations to Google Sheets.
 *
 * This repository layer coordinates between the domain layer (transactions, wallets)
 * and the Google Sheets integration layer (service, exporter).
 *
 * @property googleSheetsService Service for Google Sheets authentication and setup
 * @property googleSheetsExporter Service for performing the actual export operations
 * @property walletRepository Repository for fetching wallet information
 * @property logger Logger for tracking export operations
 */
@Singleton
class TransactionExportRepository @Inject constructor(
    private val googleSheetsService: GoogleSheetsService,
    private val googleSheetsExporter: GoogleSheetsExporter,
    private val walletRepository: WalletRepository,
    private val logger: Logger
) {

    /**
     * Exports a list of transactions to Google Sheets.
     *
     * This method:
     * 1. Checks if the user is authorized for Google Sheets access
     * 2. Creates a Sheets API service instance
     * 3. Fetches wallet information for name resolution
     * 4. Formats the transaction data using ExportFormatter
     * 5. Exports the formatted data to a new spreadsheet
     *
     * @param transactions List of transactions to export
     * @param currency Currency code for the export (e.g., "USD")
     * @param userId User ID for fetching associated wallet data
     * @return Result containing the spreadsheet URL on success, or exception on failure
     */
    suspend fun exportToGoogleSheets(
        transactions: List<Transaction>,
        currency: String,
        userId: String
    ): Result<String> {
        return try {
            logger.i(
                "TransactionExportRepo",
                "Starting export for ${transactions.size} transactions, currency: $currency"
            )

            // Step 1: Check authorization
            if (!googleSheetsService.isAuthorized()) {
                val exception = SecurityException("User is not authorized for Google Sheets access")
                logger.e("TransactionExportRepo", "Export failed: not authorized", exception)
                return Result.failure(exception)
            }

            // Step 2: Create Sheets service
            val sheetsServiceResult = googleSheetsService.createSheetsService()
            if (sheetsServiceResult.isFailure) {
                val exception = sheetsServiceResult.exceptionOrNull()
                    ?: Exception("Failed to create Sheets service")
                logger.e("TransactionExportRepo", "Failed to create Sheets service", exception)
                return Result.failure(exception)
            }

            val sheetsService = sheetsServiceResult.getOrNull()
                ?: return Result.failure(Exception("Sheets service is null"))

            // Step 3: Fetch wallets for name resolution
            logger.d("TransactionExportRepo", "Fetching wallets for user: $userId")
            val wallets = try {
                walletRepository.getUserWallets(userId).first()
            } catch (e: Exception) {
                logger.e("TransactionExportRepo", "Failed to fetch wallets", e)
                throw e
            }

            val walletsMap = wallets.associateBy { it.id }
            logger.d("TransactionExportRepo", "Fetched ${walletsMap.size} wallets")

            // Step 4: Format transaction data
            val formatter = ExportFormatter(walletsMap, currency)
            val formattedData = formatter.formatTransactionList(transactions)
            logger.d(
                "TransactionExportRepo",
                "Formatted ${formattedData.size} rows (including header)"
            )

            // Step 5: Export to Google Sheets
            val exportResult = googleSheetsExporter.exportTransactions(
                sheetsService = sheetsService,
                transactionData = formattedData
            )

            if (exportResult.isSuccess) {
                val url = exportResult.getOrNull() ?: ""
                logger.i(
                    "TransactionExportRepo",
                    "Export completed successfully. URL: $url"
                )
                Result.success(url)
            } else {
                val exception = exportResult.exceptionOrNull()
                    ?: Exception("Export failed with unknown error")
                logger.e("TransactionExportRepo", "Export failed", exception)
                Result.failure(exception)
            }

        } catch (e: Exception) {
            logger.e("TransactionExportRepo", "Unexpected error during export", e)
            Result.failure(e)
        }
    }

    /**
     * Checks if the user has the necessary permissions to export to Google Sheets.
     *
     * @return true if the user is authorized for Google Sheets access, false otherwise
     */
    fun checkExportPermissions(): Boolean {
        logger.d("TransactionExportRepo", "Checking export permissions")
        val isAuthorized = googleSheetsService.isAuthorized()
        logger.d("TransactionExportRepo", "Export permissions check result: $isAuthorized")
        return isAuthorized
    }
}
