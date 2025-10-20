package com.axeven.profiteerapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axeven.profiteerapp.data.migration.TagMigration
import com.axeven.profiteerapp.data.model.CurrencyRate
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.data.repository.AuthRepository
import com.axeven.profiteerapp.data.repository.CurrencyRateRepository
import com.axeven.profiteerapp.data.repository.UserPreferencesRepository
import com.axeven.profiteerapp.data.repository.WalletRepository
import com.axeven.profiteerapp.utils.logging.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val wallets: List<Wallet> = emptyList(),
    val currencyRates: List<CurrencyRate> = emptyList(),
    val defaultCurrency: String = "USD",
    val isLoading: Boolean = false,
    val error: String? = null,
    val migrationStatus: MigrationStatus = MigrationStatus.NotStarted
)

sealed class MigrationStatus {
    object NotStarted : MigrationStatus()
    object InProgress : MigrationStatus()
    data class Success(val transactionsUpdated: Int) : MigrationStatus()
    data class Failed(val errorMessage: String) : MigrationStatus()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val walletRepository: WalletRepository,
    private val currencyRateRepository: CurrencyRateRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val tagMigration: TagMigration,
    private val logger: Logger
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val userId = authRepository.getCurrentUserId() ?: ""

    init {
        if (userId.isNotEmpty()) {
            loadUserData()
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                combine(
                    walletRepository.getUserWallets(userId),
                    currencyRateRepository.getUserCurrencyRates(userId),
                    userPreferencesRepository.getUserPreferences(userId)
                ) { wallets, rates, preferences ->
                    Triple(wallets, rates, preferences)
                }.collect { (wallets, rates, preferences) ->
                    logger.d("SettingsViewModel", "Data update received - wallets: ${wallets.size}, rates: ${rates.size}")
                    wallets.forEach { wallet ->
                        logger.d("SettingsViewModel", "Wallet in UI: ${wallet.name} (${wallet.id})")
                    }
                    
                    _uiState.update {
                        it.copy(
                            wallets = wallets,
                            currencyRates = rates,
                            defaultCurrency = preferences?.defaultCurrency ?: "USD",
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    fun createWallet(name: String, currency: String, walletType: String, initialBalance: Double) {
        if (userId.isEmpty()) {
            logger.e("SettingsViewModel", "Cannot create wallet - userId is empty")
            return
        }

        logger.d("SettingsViewModel", "Creating wallet: $name, type: $walletType, currency: $currency, balance: $initialBalance")

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val wallet = Wallet(
                name = name,
                balance = initialBalance, // Set current balance to initial balance
                initialBalance = initialBalance,
                walletType = walletType,
                userId = userId
            )

            walletRepository.createWallet(wallet)
                .onSuccess { walletId ->
                    logger.d("SettingsViewModel", "Wallet created successfully with ID: $walletId")
                    _uiState.update {
                        it.copy(isLoading = false, error = null)
                    }
                }
                .onFailure { error ->
                    logger.e("SettingsViewModel", "Failed to create wallet: ${error.message}", error)
                    _uiState.update {
                        it.copy(isLoading = false, error = error.message)
                    }
                }
        }
    }

    fun updateWallet(walletId: String, name: String, currency: String, walletType: String, newInitialBalance: Double) {
        if (userId.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // First get the current wallet to preserve other fields
            walletRepository.getWalletById(walletId)
                .onSuccess { currentWallet ->
                    currentWallet?.let { wallet ->
                        // Calculate the difference in initial balance
                        val initialBalanceDifference = newInitialBalance - wallet.initialBalance
                        val newCurrentBalance = wallet.balance + initialBalanceDifference
                        
                        val updatedWallet = wallet.copy(
                            name = name,
                            walletType = walletType,
                            initialBalance = newInitialBalance,
                            balance = newCurrentBalance // Adjust current balance by the difference
                        )
                        
                        walletRepository.updateWallet(updatedWallet)
                            .onSuccess {
                                _uiState.update {
                                    it.copy(isLoading = false, error = null)
                                }
                            }
                            .onFailure { error ->
                                _uiState.update {
                                    it.copy(isLoading = false, error = error.message)
                                }
                            }
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, error = error.message)
                    }
                }
        }
    }

    fun deleteWallet(walletId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            walletRepository.deleteWallet(walletId)
                .onSuccess {
                    _uiState.update {
                        it.copy(isLoading = false, error = null)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, error = error.message)
                    }
                }
        }
    }

    fun updateDefaultCurrency(currency: String) {
        if (userId.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            userPreferencesRepository.updateDefaultCurrency(userId, currency)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            defaultCurrency = currency,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, error = error.message)
                    }
                }
        }
    }

    fun createCurrencyRate(fromCurrency: String, toCurrency: String, rate: Double, month: String?) {
        if (userId.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val currencyRate = CurrencyRate(
                fromCurrency = fromCurrency,
                toCurrency = toCurrency,
                rate = rate,
                month = month.takeIf { it?.isNotBlank() == true },
                userId = userId
            )

            currencyRateRepository.createCurrencyRate(currencyRate)
                .onSuccess {
                    _uiState.update {
                        it.copy(isLoading = false, error = null)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, error = error.message)
                    }
                }
        }
    }

    fun updateCurrencyRate(rateId: String, fromCurrency: String, toCurrency: String, rate: Double, month: String?) {
        if (userId.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // First get the current rate to preserve other fields
            currencyRateRepository.getCurrencyRateById(rateId)
                .onSuccess { currentRate ->
                    currentRate?.let { existingRate ->
                        val updatedRate = existingRate.copy(
                            fromCurrency = fromCurrency,
                            toCurrency = toCurrency,
                            rate = rate,
                            month = month.takeIf { it?.isNotBlank() == true }
                        )
                        
                        currencyRateRepository.updateCurrencyRate(updatedRate)
                            .onSuccess {
                                _uiState.update {
                                    it.copy(isLoading = false, error = null)
                                }
                            }
                            .onFailure { error ->
                                _uiState.update {
                                    it.copy(isLoading = false, error = error.message)
                                }
                            }
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, error = error.message)
                    }
                }
        }
    }

    fun deleteCurrencyRate(rateId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            currencyRateRepository.deleteCurrencyRate(rateId)
                .onSuccess {
                    _uiState.update {
                        it.copy(isLoading = false, error = null)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, error = error.message)
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Manually trigger tag migration for the current user.
     *
     * This will normalize all existing transaction tags:
     * - Convert to lowercase
     * - Trim whitespace
     * - Remove duplicates
     * - Filter out "Untagged" keyword
     *
     * Safe to run multiple times (idempotent).
     */
    fun migrateTagsManually() {
        if (userId.isEmpty()) {
            logger.e("SettingsViewModel", "Cannot migrate tags - userId is empty")
            _uiState.update {
                it.copy(migrationStatus = MigrationStatus.Failed("User not authenticated"))
            }
            return
        }

        viewModelScope.launch {
            logger.i("SettingsViewModel", "Manual tag migration requested for user: $userId")
            _uiState.update { it.copy(migrationStatus = MigrationStatus.InProgress) }

            try {
                val result = tagMigration.migrateTransactionTags(userId)

                result.onSuccess { count ->
                    logger.i("SettingsViewModel", "Tag migration successful: $count transactions updated")

                    // Update migration completed flag in user preferences
                    userPreferencesRepository.updateTagsMigrationFlag(userId, true)
                        .onSuccess {
                            logger.i("SettingsViewModel", "Migration flag updated successfully")
                        }
                        .onFailure { error ->
                            logger.w("SettingsViewModel", "Failed to update migration flag: ${error.message}")
                        }

                    _uiState.update {
                        it.copy(migrationStatus = MigrationStatus.Success(count))
                    }
                }.onFailure { error ->
                    logger.e("SettingsViewModel", "Tag migration failed: ${error.message}", error)
                    _uiState.update {
                        it.copy(migrationStatus = MigrationStatus.Failed(error.message ?: "Unknown error"))
                    }
                }
            } catch (e: Exception) {
                logger.e("SettingsViewModel", "Unexpected error during tag migration", e)
                _uiState.update {
                    it.copy(migrationStatus = MigrationStatus.Failed(e.message ?: "Unknown error"))
                }
            }
        }
    }

    /**
     * Reset migration status to allow another migration attempt or clear the success/error message.
     */
    fun resetMigrationStatus() {
        _uiState.update { it.copy(migrationStatus = MigrationStatus.NotStarted) }
    }
}