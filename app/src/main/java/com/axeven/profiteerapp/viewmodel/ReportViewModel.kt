package com.axeven.profiteerapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axeven.profiteerapp.data.model.PhysicalForm
import com.axeven.profiteerapp.data.model.Wallet
import com.axeven.profiteerapp.data.repository.AuthRepository
import com.axeven.profiteerapp.data.repository.UserPreferencesRepository
import com.axeven.profiteerapp.data.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportUiState(
    val portfolioComposition: Map<PhysicalForm, Double> = emptyMap(),
    val totalPortfolioValue: Double = 0.0,
    val wallets: List<Wallet> = emptyList(),
    val defaultCurrency: String = "USD",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val walletRepository: WalletRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    private val userId = authRepository.getCurrentUserId() ?: ""

    fun loadPortfolioData() {
        if (userId.isEmpty()) {
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    error = "User not authenticated"
                ) 
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            android.util.Log.d("ReportViewModel", "Loading portfolio data for user: $userId")

            try {
                combine(
                    walletRepository.getUserWallets(userId),
                    userPreferencesRepository.getUserPreferences(userId)
                ) { wallets, preferences ->
                    Pair(wallets, preferences)
                }.collect { (wallets, preferences) ->
                    android.util.Log.d("ReportViewModel", "Data received - wallets: ${wallets.size}")
                    
                    val defaultCurrency = preferences?.defaultCurrency ?: "USD"
                    
                    // Calculate portfolio composition by PhysicalForm
                    val portfolioComposition = calculatePortfolioComposition(wallets)
                    val totalValue = portfolioComposition.values.sum()
                    
                    android.util.Log.d("ReportViewModel", "Portfolio composition: $portfolioComposition, total: $totalValue")
                    
                    _uiState.update {
                        it.copy(
                            portfolioComposition = portfolioComposition,
                            totalPortfolioValue = totalValue,
                            wallets = wallets,
                            defaultCurrency = defaultCurrency,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ReportViewModel", "Error loading portfolio data", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load portfolio data"
                    )
                }
            }
        }
    }
    
    private fun calculatePortfolioComposition(wallets: List<Wallet>): Map<PhysicalForm, Double> {
        // Group wallets by their physical form and sum their balances
        val composition = mutableMapOf<PhysicalForm, Double>()
        
        wallets.forEach { wallet ->
            // Only include wallets with positive balances
            if (wallet.balance > 0) {
                val currentAmount = composition[wallet.physicalForm] ?: 0.0
                composition[wallet.physicalForm] = currentAmount + wallet.balance
                
                android.util.Log.d("ReportViewModel", 
                    "Adding wallet '${wallet.name}': ${wallet.physicalForm} = ${wallet.balance}")
            }
        }
        
        return composition.toMap()
    }
    
    fun refreshData() {
        android.util.Log.d("ReportViewModel", "Refreshing portfolio data")
        loadPortfolioData()
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    // Helper function to get wallets by physical form
    fun getWalletsByPhysicalForm(physicalForm: PhysicalForm): List<Wallet> {
        return _uiState.value.wallets.filter { 
            it.physicalForm == physicalForm && it.balance > 0 
        }
    }
    
    // Helper function to calculate percentage for a physical form
    fun getPhysicalFormPercentage(physicalForm: PhysicalForm): Double {
        val total = _uiState.value.totalPortfolioValue
        val amount = _uiState.value.portfolioComposition[physicalForm] ?: 0.0
        return if (total > 0) (amount / total) * 100 else 0.0
    }
}