package com.axeven.profiteerapp.utils

import com.axeven.profiteerapp.data.model.PhysicalForm

/**
 * Utility object for validating wallet-related data.
 * Provides validation logic for creating and editing wallets with physical forms.
 */
object WalletValidator {
    
    /**
     * Validation result for wallet operations.
     */
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    ) {
        companion object {
            fun success() = ValidationResult(true)
            fun error(message: String) = ValidationResult(false, message)
        }
    }
    
    
    /**
     * Validates wallet name requirements.
     * 
     * @param name The wallet name to validate
     * @return ValidationResult indicating if the name is valid
     */
    fun validateWalletName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.error("Wallet name cannot be empty")
            name.length < 2 -> ValidationResult.error("Wallet name must be at least 2 characters long")
            name.length > 50 -> ValidationResult.error("Wallet name cannot exceed 50 characters")
            else -> ValidationResult.success()
        }
    }
    
    /**
     * Validates wallet balance (initial balance).
     * 
     * @param balance The balance to validate
     * @return ValidationResult indicating if the balance is valid
     */
    fun validateWalletBalance(balance: Double): ValidationResult {
        return when {
            balance.isNaN() -> ValidationResult.error("Balance must be a valid number")
            balance.isInfinite() -> ValidationResult.error("Balance cannot be infinite")
            balance < -1_000_000_000 -> ValidationResult.error("Balance cannot be less than -1 billion")
            balance > 1_000_000_000_000 -> ValidationResult.error("Balance cannot exceed 1 trillion")
            else -> ValidationResult.success()
        }
    }
    
    
    /**
     * Validates complete wallet data including all fields.
     * 
     * @param name Wallet name
     * @param physicalForm Physical form of the wallet
     * @param initialBalance Initial balance
     * @return ValidationResult indicating if all wallet data is valid
     */
    fun validateWalletData(
        name: String,
        physicalForm: PhysicalForm,
        initialBalance: Double
    ): ValidationResult {
        // Validate individual fields
        validateWalletName(name).let { if (!it.isValid) return it }
        validateWalletBalance(initialBalance).let { if (!it.isValid) return it }
        
        return ValidationResult.success()
    }
    
    
    
    /**
     * Validates if a physical form change is allowed for an existing wallet.
     * Some form changes might be restricted based on business rules.
     * 
     * @param currentForm Current physical form
     * @param newForm New physical form to change to
     * @return ValidationResult indicating if the form change is valid
     */
    fun validatePhysicalFormChange(
        currentForm: PhysicalForm,
        newForm: PhysicalForm
    ): ValidationResult {
        // Check for potentially problematic form changes
        val riskyChanges = mapOf(
            PhysicalForm.CRYPTOCURRENCY to listOf(PhysicalForm.FIAT_CURRENCY),
            PhysicalForm.PRECIOUS_METALS to listOf(PhysicalForm.FIAT_CURRENCY),
            PhysicalForm.STOCKS to listOf(PhysicalForm.FIAT_CURRENCY, PhysicalForm.CASH_EQUIVALENT)
        )
        
        riskyChanges[currentForm]?.let { riskyTargets ->
            if (riskyTargets.contains(newForm)) {
                return ValidationResult.error(
                    "Warning: Changing from ${currentForm.displayName} to ${newForm.displayName} " +
                    "may affect how this wallet is categorized in your portfolio analysis. " +
                    "Please confirm this change is intentional."
                )
            }
        }
        
        return ValidationResult.success()
    }
}