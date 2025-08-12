package com.axeven.profiteerapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.data.repository.AuthRepository
import com.axeven.profiteerapp.data.repository.CurrencyRateRepository
import com.axeven.profiteerapp.data.repository.UserPreferencesRepository
import com.axeven.profiteerapp.data.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WalletListUiState(
    val wallets: List<Wallet> = emptyList(),
    val defaultCurrency: String = "USD",
    val isLoading: Boolean = false,
    val error: String? = null,
    val showPhysicalWallets: Boolean = true,
    val conversionRates: Map<String, Double> = emptyMap(),
    val existingWalletNames: Set<String> = emptySet()
)

@HiltViewModel
class WalletListViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val walletRepository: WalletRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val currencyRateRepository: CurrencyRateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletListUiState())
    val uiState: StateFlow<WalletListUiState> = _uiState.asStateFlow()

    private val userId = authRepository.getCurrentUserId() ?: ""

    init {
        if (userId.isNotEmpty()) {
            loadWallets()
        }
    }

    private fun loadWallets() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                combine(
                    walletRepository.getUserWallets(userId),
                    userPreferencesRepository.getUserPreferences(userId),
                    currencyRateRepository.getUserCurrencyRates(userId)
                ) { wallets, preferences, currencyRates ->
                    Triple(wallets, preferences, currencyRates)
                }.collect { (wallets, preferences, currencyRates) ->
                    val defaultCurrency = preferences?.defaultCurrency ?: "USD"
                    
                    // Build conversion rates map based on actual wallet currencies
                    val conversionRatesMap = buildConversionRatesMap(currencyRates, defaultCurrency, wallets)
                    
                    // Filter and sort wallets based on current view type
                    val filteredWallets = wallets.filter { wallet ->
                        if (_uiState.value.showPhysicalWallets) {
                            wallet.walletType == "Physical"
                        } else {
                            wallet.walletType != "Physical"
                        }
                    }.sortedByDescending { wallet ->
                        // Sort by converted balance in descending order
                        convertToDefaultCurrency(wallet.balance, wallet.currency, defaultCurrency, conversionRatesMap)
                    }

                    // Get existing wallet names for validation
                    val existingNames = wallets.map { it.name.lowercase() }.toSet()

                    _uiState.update {
                        it.copy(
                            wallets = filteredWallets,
                            defaultCurrency = defaultCurrency,
                            isLoading = false,
                            error = null,
                            conversionRates = conversionRatesMap,
                            existingWalletNames = existingNames
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

    fun toggleWalletType() {
        _uiState.update { currentState ->
            val newShowPhysical = !currentState.showPhysicalWallets
            currentState.copy(showPhysicalWallets = newShowPhysical)
        }
        loadWallets() // Reload with new filter
    }

    fun createWallet(
        name: String,
        walletType: String,
        currency: String,
        initialBalance: Double
    ) {
        if (userId.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val wallet = Wallet(
                name = name,
                currency = currency,
                balance = initialBalance,
                initialBalance = initialBalance,
                walletType = walletType,
                userId = userId
            )

            walletRepository.createWallet(wallet)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, error = null) }
                    loadWallets() // Refresh the list
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, error = error.message)
                    }
                }
        }
    }

    fun updateWallet(wallet: Wallet) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            walletRepository.updateWallet(wallet)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, error = null) }
                    loadWallets() // Refresh the list
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
                    _uiState.update { it.copy(isLoading = false, error = null) }
                    loadWallets() // Refresh the list
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

    fun refreshWallets() {
        if (userId.isNotEmpty()) {
            loadWallets()
        }
    }

    fun isWalletNameUnique(name: String, excludeId: String? = null): Boolean {
        val currentWallets = _uiState.value.wallets
        return currentWallets.none { 
            it.name.equals(name, ignoreCase = true) && it.id != excludeId 
        }
    }

    private fun buildConversionRatesMap(
        currencyRates: List<com.axeven.profiteerapp.data.model.CurrencyRate>,
        defaultCurrency: String,
        wallets: List<Wallet>
    ): Map<String, Double> {
        val ratesMap = mutableMapOf<String, Double>()
        
        // Get all unique currencies from wallets that need conversion rates
        val walletCurrencies = wallets.map { it.currency }.distinct()
        
        for (currency in walletCurrencies) {
            if (currency != defaultCurrency && currency.isNotBlank()) {
                val rate = findConversionRate(currency, defaultCurrency, currencyRates)
                if (rate != null) {
                    ratesMap[currency] = rate
                }
            }
        }
        
        return ratesMap
    }

    private fun convertToDefaultCurrency(
        amount: Double,
        fromCurrency: String,
        defaultCurrency: String,
        conversionRates: Map<String, Double>
    ): Double {
        if (fromCurrency == defaultCurrency || fromCurrency.isBlank()) {
            return amount
        }
        
        val rate = conversionRates[fromCurrency]
        return if (rate != null) {
            amount * rate
        } else {
            amount // Return original amount if no conversion rate available
        }
    }
    
    private fun findConversionRate(
        fromCurrency: String,
        toCurrency: String,
        currencyRates: List<com.axeven.profiteerapp.data.model.CurrencyRate>
    ): Double? {
        // 1. Try to find direct conversion rate with default month (from -> to)
        val directDefaultRate = currencyRates.find { 
            it.fromCurrency == fromCurrency && it.toCurrency == toCurrency && it.month == null 
        }?.rate
        
        if (directDefaultRate != null) {
            return directDefaultRate
        }
        
        // 2. Try to find reverse conversion rate with default month (to -> from) and invert it
        val reverseDefaultRate = currencyRates.find { 
            it.fromCurrency == toCurrency && it.toCurrency == fromCurrency && it.month == null 
        }?.rate
        
        if (reverseDefaultRate != null && reverseDefaultRate != 0.0) {
            return 1.0 / reverseDefaultRate
        }
        
        // 3. Fallback to monthly rates: try direct conversion (from -> to)
        val directMonthlyRate = currencyRates.find { 
            it.fromCurrency == fromCurrency && it.toCurrency == toCurrency && it.month != null 
        }?.rate
        
        if (directMonthlyRate != null) {
            return directMonthlyRate
        }
        
        // 4. Fallback to monthly rates: try reverse conversion (to -> from) and invert it
        val reverseMonthlyRate = currencyRates.find { 
            it.fromCurrency == toCurrency && it.toCurrency == fromCurrency && it.month != null 
        }?.rate
        
        if (reverseMonthlyRate != null && reverseMonthlyRate != 0.0) {
            return 1.0 / reverseMonthlyRate
        }
        
        return null
    }
}