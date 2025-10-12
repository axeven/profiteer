package com.axeven.profiteerapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axeven.profiteerapp.data.repository.TransactionRepository
import com.axeven.profiteerapp.data.repository.WalletRepository
import com.axeven.profiteerapp.data.ui.DiscrepancyDebugUiState
import com.axeven.profiteerapp.utils.BalanceDiscrepancyDetector
import com.axeven.profiteerapp.utils.DiscrepancyAnalyzer
import com.axeven.profiteerapp.utils.logging.Logger
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * ViewModel for the Discrepancy Debug Screen.
 *
 * This ViewModel loads all transactions and wallets for a user, analyzes them
 * for balance discrepancies, and provides running balance information for each
 * transaction to help identify where the discrepancy occurred.
 *
 * @param userId User ID to load data for
 * @param transactionRepository Repository for transaction data
 * @param walletRepository Repository for wallet data
 * @param balanceDetector Utility for balance discrepancy detection
 * @param discrepancyAnalyzer Utility for transaction analysis
 * @param logger Logger for debugging
 */
class DiscrepancyDebugViewModel @AssistedInject constructor(
    @Assisted private val userId: String,
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val balanceDetector: BalanceDiscrepancyDetector,
    private val discrepancyAnalyzer: DiscrepancyAnalyzer,
    private val logger: Logger
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscrepancyDebugUiState())
    val uiState: StateFlow<DiscrepancyDebugUiState> = _uiState.asStateFlow()

    init {
        loadDiscrepancyData()
    }

    /**
     * Load transactions and wallets, analyze for discrepancies.
     */
    private fun loadDiscrepancyData() {
        viewModelScope.launch {
            logger.d("DiscrepancyDebugVM", "Loading discrepancy data for user: $userId")

            combine(
                transactionRepository.getAllTransactionsChronological(userId),
                walletRepository.getUserWallets(userId)
            ) { transactions, wallets ->
                Pair(transactions, wallets)
            }
                .catch { error ->
                    logger.e("DiscrepancyDebugVM", "Error loading discrepancy data", error)
                    _uiState.value = _uiState.value.withError(
                        error.message ?: "Failed to load discrepancy data"
                    )
                }
                .collect { (transactions, wallets) ->
                    logger.d("DiscrepancyDebugVM", "Loaded ${transactions.size} transactions and ${wallets.size} wallets")

                    try {
                        // Calculate current totals
                        val physicalTotal = balanceDetector.calculateTotalPhysicalBalance(wallets)
                        val logicalTotal = balanceDetector.calculateTotalLogicalBalance(wallets)

                        logger.d("DiscrepancyDebugVM", "Physical total: $physicalTotal, Logical total: $logicalTotal")

                        // Create wallet map for analysis
                        val walletMap = wallets.associateBy { it.id }

                        // Find first discrepancy transaction
                        val firstDiscrepancyId = discrepancyAnalyzer.findFirstDiscrepancyTransaction(
                            transactions = transactions,
                            wallets = walletMap
                        )

                        logger.d("DiscrepancyDebugVM", "First discrepancy transaction: $firstDiscrepancyId")

                        // Calculate running balances for all transactions
                        val transactionsWithBalances = discrepancyAnalyzer.calculateRunningBalances(
                            transactions = transactions,
                            wallets = walletMap
                        )

                        logger.d("DiscrepancyDebugVM", "Calculated running balances for ${transactionsWithBalances.size} transactions")

                        // Update UI state
                        _uiState.value = _uiState.value.withTransactions(
                            transactions = transactionsWithBalances,
                            firstDiscrepancyId = firstDiscrepancyId,
                            physicalTotal = physicalTotal,
                            logicalTotal = logicalTotal
                        )

                        logger.i("DiscrepancyDebugVM", "Discrepancy analysis complete. Has discrepancy: ${_uiState.value.hasDiscrepancy}")
                    } catch (error: Exception) {
                        logger.e("DiscrepancyDebugVM", "Error analyzing discrepancy data", error)
                        _uiState.value = _uiState.value.withError(
                            "Failed to analyze discrepancy: ${error.message}"
                        )
                    }
                }
        }
    }

    /**
     * Refresh the discrepancy data.
     * This is useful if the user wants to reload the data manually.
     */
    fun refresh() {
        logger.d("DiscrepancyDebugVM", "Refreshing discrepancy data")
        _uiState.value = DiscrepancyDebugUiState() // Reset to loading state
        loadDiscrepancyData()
    }

    @AssistedFactory
    interface Factory {
        fun create(userId: String): DiscrepancyDebugViewModel
    }
}
