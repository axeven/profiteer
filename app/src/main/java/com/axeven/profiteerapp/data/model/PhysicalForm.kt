package com.axeven.profiteerapp.data.model

/**
 * Represents the physical form/asset type of a Physical wallet.
 * This enum categorizes wallets by their underlying asset class to enable
 * better portfolio management, filtering, and financial analysis.
 */
enum class PhysicalForm(
    val displayName: String,
    val description: String,
    val icon: String, // Unicode emoji for visual representation
    val allowedCurrencies: Set<String>? = null // null means all currencies allowed
) {
    FIAT_CURRENCY(
        displayName = "Fiat Currency",
        description = "Traditional government-issued currencies",
        icon = "💰",
        allowedCurrencies = setOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD", "IDR")
    ),
    
    CRYPTOCURRENCY(
        displayName = "Cryptocurrency",
        description = "Digital currencies like Bitcoin, Ethereum",
        icon = "₿",
        allowedCurrencies = setOf("BTC", "ETH", "ADA", "DOT", "SOL", "MATIC")
    ),
    
    PRECIOUS_METALS(
        displayName = "Precious Metals",
        description = "Gold, Silver, Platinum investments",
        icon = "🥇",
        allowedCurrencies = setOf("GOLD", "SILVER", "PLATINUM", "PALLADIUM")
    ),
    
    STOCKS(
        displayName = "Stocks",
        description = "Individual company shares",
        icon = "📈",
        allowedCurrencies = setOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD") // Stock market currencies
    ),
    
    ETFS(
        displayName = "ETFs",
        description = "Exchange-Traded Funds",
        icon = "📊",
        allowedCurrencies = setOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD")
    ),
    
    BONDS(
        displayName = "Bonds",
        description = "Government and corporate bonds",
        icon = "🏛️",
        allowedCurrencies = setOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD")
    ),
    
    MUTUAL_FUNDS(
        displayName = "Mutual Funds",
        description = "Pooled investment vehicles",
        icon = "🏦",
        allowedCurrencies = setOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD")
    ),
    
    REAL_ESTATE(
        displayName = "Real Estate",
        description = "Property investments and REITs",
        icon = "🏠",
        allowedCurrencies = setOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD", "IDR")
    ),
    
    COMMODITIES(
        displayName = "Commodities",
        description = "Oil, agricultural products, raw materials",
        icon = "🛢️",
        allowedCurrencies = setOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD")
    ),
    
    CASH_EQUIVALENT(
        displayName = "Cash Equivalent",
        description = "Money market accounts, savings accounts",
        icon = "💳",
        allowedCurrencies = setOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD", "IDR")
    ),
    
    OTHER(
        displayName = "Other",
        description = "Custom or miscellaneous asset types",
        icon = "📁",
        allowedCurrencies = null // Allow all currencies
    );
    
    /**
     * Checks if the given currency is compatible with this physical form.
     * @param currency The currency code to validate
     * @return true if the currency is allowed for this physical form
     */
    fun isCurrencyAllowed(currency: String): Boolean {
        return allowedCurrencies?.contains(currency) ?: true
    }
    
    /**
     * Gets the appropriate default physical form for a given currency.
     * This is used for migration and smart defaults.
     */
    companion object {
        fun getDefaultForCurrency(currency: String): PhysicalForm {
            return when (currency.uppercase()) {
                "BTC", "ETH", "ADA", "DOT", "SOL", "MATIC" -> CRYPTOCURRENCY
                "GOLD", "SILVER", "PLATINUM", "PALLADIUM" -> PRECIOUS_METALS
                else -> FIAT_CURRENCY
            }
        }
        
        /**
         * Gets all physical forms that support the given currency.
         */
        fun getCompatibleForms(currency: String): List<PhysicalForm> {
            return values().filter { it.isCurrencyAllowed(currency) }
        }
        
        /**
         * Groups for UI organization
         */
        val INVESTMENT_FORMS = setOf(STOCKS, ETFS, BONDS, MUTUAL_FUNDS)
        val ALTERNATIVE_INVESTMENTS = setOf(PRECIOUS_METALS, REAL_ESTATE, COMMODITIES)
        val CASH_FORMS = setOf(FIAT_CURRENCY, CASH_EQUIVALENT)
        val DIGITAL_ASSETS = setOf(CRYPTOCURRENCY)
    }
}