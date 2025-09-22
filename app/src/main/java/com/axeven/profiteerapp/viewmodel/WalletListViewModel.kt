package com.axeven.profiteerapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.data.model.PhysicalForm
import com.axeven.profiteerapp.data.repository.AuthRepository
import com.axeven.profiteerapp.data.repository.CurrencyRateRepository
import com.axeven.profiteerapp.data.repository.UserPreferencesRepository
import com.axeven.profiteerapp.data.repository.WalletRepository
import com.axeven.profiteerapp.utils.logging.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WalletListUiState(
    val wallets: List<Wallet> = emptyList(),
    val defaultCurrency: String = "USD",
    val displayCurrency: String = "USD",
    val displayRate: Double = 1.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showPhysicalWallets: Boolean = true,
    val existingWalletNames: Set<String> = emptySet(),
    val unallocatedBalance: Double = 0.0,
    val selectedPhysicalForms: Set<PhysicalForm> = emptySet(),
    val availablePhysicalForms: Set<PhysicalForm> = emptySet(),
    val isGroupedByForm: Boolean = false,
    val groupedWallets: Map<PhysicalForm, List<Wallet>> = emptyMap()
)

@HiltViewModel
class WalletListViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val walletRepository: WalletRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val currencyRateRepository: CurrencyRateRepository,
    private val logger: Logger
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletListUiState())
    val uiState: StateFlow<WalletListUiState> = _uiState.asStateFlow()

    private val userId = authRepository.getCurrentUserId() ?: ""

    init {
        if (userId.isNotEmpty()) {
            // Fix display currency sync issue
            viewModelScope.launch {
                userPreferencesRepository.syncDisplayCurrencyWithDefault(userId)
            }
            loadWallets()
        }
    }

    private fun loadWallets() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                combine(
                    walletRepository.getUserWallets(userId),
                    userPreferencesRepository.getUserPreferences(userId)
                ) { wallets, preferences ->
                    Pair(wallets, preferences)
                }.collect { (wallets, preferences) ->
                    val defaultCurrency = preferences?.defaultCurrency ?: "USD"
                    val displayCurrency = preferences?.displayCurrency ?: defaultCurrency
                    
                    // Get display rate for UI conversion
                    val displayRate = if (defaultCurrency != displayCurrency) {
                        currencyRateRepository.getDisplayRate(userId, defaultCurrency, displayCurrency).getOrElse { 1.0 }
                    } else {
                        1.0
                    }
                    
                    // Filter and sort wallets based on current view type
                    val baseFilteredWallets = wallets.filter { wallet ->
                        if (_uiState.value.showPhysicalWallets) {
                            wallet.walletType == "Physical"
                        } else {
                            wallet.walletType != "Physical"
                        }
                    }
                    
                    // Apply physical form filtering if any forms are selected
                    val filteredWallets = if (_uiState.value.selectedPhysicalForms.isNotEmpty() && _uiState.value.showPhysicalWallets) {
                        baseFilteredWallets.filter { wallet ->
                            _uiState.value.selectedPhysicalForms.contains(wallet.physicalForm)
                        }
                    } else {
                        baseFilteredWallets
                    }.sortedByDescending { wallet ->
                        // Sort by balance in descending order (all wallets use default currency now)
                        wallet.balance
                    }
                    
                    // Get available physical forms from current wallets
                    val availablePhysicalForms = baseFilteredWallets
                        .filter { it.walletType == "Physical" }
                        .map { it.physicalForm }
                        .toSet()
                    
                    // Group wallets by physical form if grouping is enabled
                    val groupedWallets = if (_uiState.value.isGroupedByForm && _uiState.value.showPhysicalWallets) {
                        filteredWallets.groupBy { it.physicalForm }
                            .mapValues { (_, wallets) ->
                                wallets.sortedByDescending { wallet ->
                                    wallet.balance
                                }
                            }
                    } else {
                        emptyMap()
                    }

                    // Get existing wallet names for validation
                    val existingNames = wallets.map { it.name.lowercase() }.toSet()

                    // Calculate unallocated balance (physical - logical)
                    val unallocatedBalance = calculateUnallocatedBalance(wallets)

                    _uiState.update {
                        it.copy(
                            wallets = filteredWallets,
                            defaultCurrency = defaultCurrency,
                            displayCurrency = displayCurrency,
                            displayRate = displayRate,
                            isLoading = false,
                            error = null,
                            existingWalletNames = existingNames,
                            unallocatedBalance = unallocatedBalance,
                            availablePhysicalForms = availablePhysicalForms,
                            groupedWallets = groupedWallets
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
        initialBalance: Double,
        physicalForm: PhysicalForm = PhysicalForm.FIAT_CURRENCY
    ) {
        if (userId.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val wallet = Wallet(
                name = name,
                balance = initialBalance,
                initialBalance = initialBalance,
                walletType = walletType,
                physicalForm = physicalForm,
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


    
    
    private fun calculateUnallocatedBalance(
        wallets: List<Wallet>
    ): Double {
        val physicalWallets = wallets.filter { it.walletType == "Physical" }
        val logicalWallets = wallets.filter { it.walletType == "Logical" }
        
        val totalPhysicalBalance = physicalWallets.sumOf { it.balance }
        val totalLogicalBalance = logicalWallets.sumOf { it.balance }
        
        return totalPhysicalBalance - totalLogicalBalance
    }
    
    // Physical Form Filtering Methods
    
    fun togglePhysicalFormFilter(physicalForm: PhysicalForm) {
        _uiState.update { currentState ->
            val currentSelected = currentState.selectedPhysicalForms
            val newSelected = if (currentSelected.contains(physicalForm)) {
                currentSelected - physicalForm
            } else {
                currentSelected + physicalForm
            }
            currentState.copy(selectedPhysicalForms = newSelected)
        }
        loadWallets() // Refresh with new filter
    }
    
    fun clearPhysicalFormFilters() {
        _uiState.update { it.copy(selectedPhysicalForms = emptySet()) }
        loadWallets()
    }
    
    fun setPhysicalFormFilters(physicalForms: Set<PhysicalForm>) {
        _uiState.update { it.copy(selectedPhysicalForms = physicalForms) }
        loadWallets()
    }
    
    fun toggleGroupByForm() {
        _uiState.update { currentState ->
            currentState.copy(isGroupedByForm = !currentState.isGroupedByForm)
        }
        loadWallets() // Refresh with new grouping
    }
    
    fun setGroupByForm(enabled: Boolean) {
        _uiState.update { it.copy(isGroupedByForm = enabled) }
        loadWallets()
    }
}