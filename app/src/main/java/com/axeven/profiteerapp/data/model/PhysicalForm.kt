package com.axeven.profiteerapp.data.model

/**
 * Represents the physical form/asset type of a Physical wallet.
 * This enum categorizes wallets by their underlying asset class to enable
 * better portfolio management, filtering, and financial analysis.
 */
enum class PhysicalForm(
    val displayName: String,
    val description: String,
    val icon: String // Unicode emoji for visual representation
) {
    FIAT_CURRENCY(
        displayName = "Fiat Currency",
        description = "Traditional government-issued currencies",
        icon = "💰"
    ),
    
    CRYPTOCURRENCY(
        displayName = "Cryptocurrency",
        description = "Digital currencies like Bitcoin, Ethereum",
        icon = "₿"
    ),
    
    PRECIOUS_METALS(
        displayName = "Precious Metals",
        description = "Gold, Silver, Platinum investments",
        icon = "🥇"
    ),
    
    STOCKS(
        displayName = "Stocks",
        description = "Individual company shares",
        icon = "📈"
    ),
    
    ETFS(
        displayName = "ETFs",
        description = "Exchange-Traded Funds",
        icon = "📊"
    ),
    
    BONDS(
        displayName = "Bonds",
        description = "Government and corporate bonds",
        icon = "🏛️"
    ),
    
    MUTUAL_FUNDS(
        displayName = "Mutual Funds",
        description = "Pooled investment vehicles",
        icon = "🏦"
    ),
    
    REAL_ESTATE(
        displayName = "Real Estate",
        description = "Property investments and REITs",
        icon = "🏠"
    ),
    
    COMMODITIES(
        displayName = "Commodities",
        description = "Oil, agricultural products, raw materials",
        icon = "🛢️"
    ),
    
    CASH_EQUIVALENT(
        displayName = "Cash Equivalent",
        description = "Money market accounts, savings accounts",
        icon = "💳"
    ),
    
    OTHER(
        displayName = "Other",
        description = "Custom or miscellaneous asset types",
        icon = "📁"
    );
    
    companion object {
        
        /**
         * Groups for UI organization
         */
        val INVESTMENT_FORMS = setOf(STOCKS, ETFS, BONDS, MUTUAL_FUNDS)
        val ALTERNATIVE_INVESTMENTS = setOf(PRECIOUS_METALS, REAL_ESTATE, COMMODITIES)
        val CASH_FORMS = setOf(FIAT_CURRENCY, CASH_EQUIVALENT)
        val DIGITAL_ASSETS = setOf(CRYPTOCURRENCY)
    }
}