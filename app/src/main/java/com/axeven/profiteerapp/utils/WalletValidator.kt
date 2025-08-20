package com.axeven.profiteerapp.utils

import com.axeven.profiteerapp.data.model.PhysicalForm

/**
 * Utility object for validating wallet-related data including physical form and currency combinations.
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
     * Validates if a physical form is compatible with the given currency.
     * 
     * @param physicalForm The physical form to validate
     * @param currency The currency code to check compatibility with
     * @return ValidationResult indicating if the combination is valid
     */
    fun validatePhysicalFormCurrency(physicalForm: PhysicalForm, currency: String): ValidationResult {
        if (!physicalForm.isCurrencyAllowed(currency)) {
            val allowedCurrencies = physicalForm.allowedCurrencies
            return if (allowedCurrencies != null) {
                ValidationResult.error(
                    "${physicalForm.displayName} supports only: ${allowedCurrencies.joinToString(", ")}. " +
                    "Currency '$currency' is not compatible."
                )
            } else {
                // This shouldn't happen as OTHER form allows all currencies
                ValidationResult.error("Currency '$currency' is not compatible with ${physicalForm.displayName}.")
            }
        }
        return ValidationResult.success()
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
     * Validates currency code format and supported currencies.
     * 
     * @param currency The currency code to validate
     * @return ValidationResult indicating if the currency is valid
     */
    fun validateCurrency(currency: String): ValidationResult {
        val supportedCurrencies = setOf(
            "USD", "EUR", "GBP", "JPY", "CAD", "AUD", "IDR", // Fiat currencies
            "BTC", "ETH", "ADA", "DOT", "SOL", "MATIC", // Cryptocurrencies
            "GOLD", "SILVER", "PLATINUM", "PALLADIUM" // Precious metals
        )
        
        return when {
            currency.isBlank() -> ValidationResult.error("Currency cannot be empty")
            currency.length !in 2..10 -> ValidationResult.error("Currency code must be 2-10 characters long")
            !currency.matches(Regex("^[A-Z]+$")) -> ValidationResult.error("Currency code must contain only uppercase letters")
            !supportedCurrencies.contains(currency) -> ValidationResult.error(
                "Currency '$currency' is not supported. Supported currencies: ${supportedCurrencies.joinToString(", ")}"
            )
            else -> ValidationResult.success()
        }
    }
    
    /**
     * Validates complete wallet data including all fields and their relationships.
     * 
     * @param name Wallet name
     * @param currency Currency code
     * @param physicalForm Physical form of the wallet
     * @param initialBalance Initial balance
     * @return ValidationResult indicating if all wallet data is valid
     */
    fun validateWalletData(
        name: String,
        currency: String,
        physicalForm: PhysicalForm,
        initialBalance: Double
    ): ValidationResult {
        // Validate individual fields
        validateWalletName(name).let { if (!it.isValid) return it }
        validateCurrency(currency).let { if (!it.isValid) return it }
        validateWalletBalance(initialBalance).let { if (!it.isValid) return it }
        
        // Validate relationships
        validatePhysicalFormCurrency(physicalForm, currency).let { if (!it.isValid) return it }
        
        return ValidationResult.success()
    }
    
    /**
     * Gets suggested physical forms for a given currency.
     * This is useful for UI to show appropriate options.
     * 
     * @param currency The currency code
     * @return List of compatible physical forms
     */
    fun getSuggestedPhysicalForms(currency: String): List<PhysicalForm> {
        return PhysicalForm.getCompatibleForms(currency)
    }
    
    /**
     * Gets the recommended physical form for a currency based on smart defaults.
     * 
     * @param currency The currency code
     * @return Recommended PhysicalForm
     */
    fun getRecommendedPhysicalForm(currency: String): PhysicalForm {
        return PhysicalForm.getDefaultForCurrency(currency)
    }
    
    /**
     * Validates if a physical form change is allowed for an existing wallet.
     * Some form changes might be restricted based on business rules.
     * 
     * @param currentForm Current physical form
     * @param newForm New physical form to change to
     * @param currency Wallet currency
     * @return ValidationResult indicating if the form change is valid
     */
    fun validatePhysicalFormChange(
        currentForm: PhysicalForm,
        newForm: PhysicalForm,
        currency: String
    ): ValidationResult {
        // First validate that the new form is compatible with the currency
        validatePhysicalFormCurrency(newForm, currency).let { if (!it.isValid) return it }
        
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