package com.axeven.profiteerapp.utils.logging

object LogSanitizer {

    // Regex patterns for sensitive data
    private val emailPattern = Regex("""[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}""")
    private val userIdPattern = Regex("""[a-zA-Z0-9-]{20,}""") // Long alphanumeric strings with dashes (likely UUIDs)
    private val walletIdPattern = Regex("""wallet_[a-zA-Z0-9]+""")
    private val transactionIdPattern = Regex("""txn_[a-zA-Z0-9]+""")

    // Financial data patterns
    private val amountPattern = Regex("""\b\d+\.?\d*\b""") // Numbers that could be amounts
    private val currencyAmountPattern = Regex("""\b\d+\.?\d*\s*(USD|EUR|IDR|GBP|JPY|CNY|SGD)\b""")

    // Authentication patterns
    private val jwtTokenPattern = Regex("""eyJ[a-zA-Z0-9+/=]+""") // JWT tokens start with eyJ
    private val apiKeyPattern = Regex("""AIza[a-zA-Z0-9_-]{35}""") // Google API keys
    private val sessionTokenPattern = Regex("""sess_[a-zA-Z0-9]+""")
    private val tokenWithContextPattern = Regex("""with\s+token\s+([a-zA-Z0-9]+)""", RegexOption.IGNORE_CASE)
    private val tokenColonPattern = Regex(""":\s+([a-zA-Z0-9+/=]{10,})""") // Pattern like ": tokenvalue"

    /**
     * Sanitizes user-related sensitive data from log messages
     */
    fun sanitizeUserData(message: String): String {
        if (message.isEmpty()) return message

        var sanitized = message

        // Replace email addresses
        sanitized = emailPattern.replace(sanitized, "[EMAIL_REDACTED]")

        // Replace user IDs (but preserve wallet/transaction IDs for other methods)
        sanitized = userIdPattern.replace(sanitized) { matchResult ->
            val match = matchResult.value
            when {
                match.startsWith("wallet_") -> match // Let wallet sanitizer handle this
                match.startsWith("txn_") -> match // Let financial sanitizer handle this
                else -> "[USER_ID_REDACTED]"
            }
        }

        // Replace wallet IDs
        sanitized = walletIdPattern.replace(sanitized, "[WALLET_ID_REDACTED]")

        return sanitized
    }

    /**
     * Sanitizes financial data from log messages
     */
    fun sanitizeFinancialData(message: String): String {
        if (message.isEmpty()) return message

        var sanitized = message

        // Replace transaction IDs
        sanitized = transactionIdPattern.replace(sanitized, "[TRANSACTION_ID_REDACTED]")

        // Replace amounts with currency codes (preserve the currency)
        sanitized = currencyAmountPattern.replace(sanitized) { matchResult ->
            val currencyCode = matchResult.groupValues.getOrNull(1) ?: ""
            if (currencyCode.isNotEmpty()) {
                "[AMOUNT_REDACTED] $currencyCode"
            } else {
                "[AMOUNT_REDACTED]"
            }
        }

        // Replace standalone amounts (be careful not to replace IDs or other numbers)
        sanitized = amountPattern.replace(sanitized) { matchResult ->
            val match = matchResult.value
            val beforeMatch = sanitized.substring(0, matchResult.range.first)
            val afterMatch = sanitized.substring(matchResult.range.last + 1)

            // Only replace if it looks like a financial amount (has context clues)
            val context = "${beforeMatch.takeLast(20)} ${afterMatch.take(20)}".lowercase()
            val isFinancialContext = context.contains("amount") ||
                                   context.contains("balance") ||
                                   context.contains("transfer") ||
                                   context.contains("payment") ||
                                   context.contains("transaction") ||
                                   context.contains("wallet") ||
                                   match.contains(".")

            if (isFinancialContext) "[AMOUNT_REDACTED]" else match
        }

        return sanitized
    }

    /**
     * Sanitizes authentication and security-related data from log messages
     */
    fun sanitizeAuthData(message: String): String {
        if (message.isEmpty()) return message

        var sanitized = message

        // Replace JWT tokens
        sanitized = jwtTokenPattern.replace(sanitized, "[TOKEN_REDACTED]")

        // Replace API keys
        sanitized = apiKeyPattern.replace(sanitized, "[API_KEY_REDACTED]")

        // Replace session tokens
        sanitized = sessionTokenPattern.replace(sanitized, "[SESSION_TOKEN_REDACTED]")

        // Replace tokens with explicit context (like "with token xyz789")
        sanitized = tokenWithContextPattern.replace(sanitized, "with token [TOKEN_REDACTED]")

        // Replace tokens after colons (like ": eyJhbGci...")
        sanitized = tokenColonPattern.replace(sanitized, ": [TOKEN_REDACTED]")

        return sanitized
    }

    /**
     * Convenience method to sanitize all types of sensitive data
     */
    fun sanitizeAll(message: String): String {
        if (message.isEmpty()) return message

        var sanitized = message
        sanitized = sanitizeUserData(sanitized)
        sanitized = sanitizeFinancialData(sanitized)
        sanitized = sanitizeAuthData(sanitized)
        return sanitized
    }
}