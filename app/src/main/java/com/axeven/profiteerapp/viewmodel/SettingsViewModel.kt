package com.axeven.profiteerapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axeven.profiteerapp.data.model.CurrencyRate
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.data.repository.AuthRepository
import com.axeven.profiteerapp.data.repository.CurrencyRateRepository
import com.axeven.profiteerapp.data.repository.UserPreferencesRepository
import com.axeven.profiteerapp.data.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val wallets: List<Wallet> = emptyList(),
    val currencyRates: List<CurrencyRate> = emptyList(),
    val defaultCurrency: String = "USD",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val walletRepository: WalletRepository,
    private val currencyRateRepository: CurrencyRateRepository,
    private val userPreferencesRepository: UserPreferencesRepository
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
                    android.util.Log.d("SettingsViewModel", "Data update received - wallets: ${wallets.size}, rates: ${rates.size}")
                    wallets.forEach { wallet ->
                        android.util.Log.d("SettingsViewModel", "Wallet in UI: ${wallet.name} (${wallet.id})")
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
            android.util.Log.e("SettingsViewModel", "Cannot create wallet - userId is empty")
            return
        }

        android.util.Log.d("SettingsViewModel", "Creating wallet: $name, type: $walletType, currency: $currency, balance: $initialBalance")

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
                    android.util.Log.d("SettingsViewModel", "Wallet created successfully with ID: $walletId")
                    _uiState.update {
                        it.copy(isLoading = false, error = null)
                    }
                }
                .onFailure { error ->
                    android.util.Log.e("SettingsViewModel", "Failed to create wallet: ${error.message}", error)
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
}