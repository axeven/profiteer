package com.axeven.profiteerapp.utils.logging

import java.time.Instant
import java.time.format.DateTimeFormatter

object LogFormatter {

    /**
     * Formats user action logs with consistent structure
     */
    fun formatUserAction(action: String, userId: String, metadata: Map<String, Any?>): String {
        val timestamp = getCurrentTimestamp()
        val baseMessage = "action=$action user=${sanitizeUserId(userId)} timestamp=$timestamp"

        if (metadata.isEmpty()) {
            return baseMessage
        }

        val metadataString = formatMetadata(metadata)
        return "$baseMessage $metadataString"
    }

    /**
     * Formats error logs with context information
     */
    fun formatError(error: Throwable, context: String): String {
        val timestamp = getCurrentTimestamp()
        val errorType = error.javaClass.simpleName
        val errorMessage = sanitizeMessage(error.message ?: "Unknown error")

        return "error=$errorType context=$context message=\"$errorMessage\" timestamp=$timestamp"
    }

    /**
     * Formats transaction logs with proper structure
     */
    fun formatTransaction(transactionType: String, metadata: Map<String, Any>): String {
        val timestamp = getCurrentTimestamp()
        val baseMessage = "transaction=true type=$transactionType timestamp=$timestamp"

        val metadataString = formatMetadata(metadata)
        return "$baseMessage $metadataString"
    }

    /**
     * Formats performance metrics consistently
     */
    fun formatPerformance(operation: String, duration: Long, metadata: Map<String, Any>): String {
        val timestamp = getCurrentTimestamp()
        val baseMessage = "performance=true operation=$operation duration=$duration timestamp=$timestamp"

        val metadataString = formatMetadata(metadata)
        return "$baseMessage $metadataString"
    }

    /**
     * Formats metadata map into key=value pairs
     */
    private fun formatMetadata(metadata: Map<String, Any?>): String {
        return metadata.entries.joinToString(" ") { (key, value) ->
            val sanitizedValue = when (value) {
                null -> "null"
                is String -> {
                    // Only sanitize strings that might contain sensitive data
                    if (key.contains("email", ignoreCase = true) ||
                        key.contains("token", ignoreCase = true) ||
                        key.contains("password", ignoreCase = true)) {
                        sanitizeMessage(value)
                    } else {
                        value
                    }
                }
                else -> value.toString()
            }
            "$key=$sanitizedValue"
        }
    }

    /**
     * Sanitizes a message to remove sensitive data
     */
    private fun sanitizeMessage(message: String): String {
        return LogSanitizer.sanitizeAll(message)
    }

    /**
     * Sanitizes user ID to remove sensitive data
     */
    private fun sanitizeUserId(userId: String): String {
        // For anonymous or short IDs, keep as-is
        if (userId == "anonymous" || userId.length < 10) {
            return userId
        }
        // For longer IDs that might be sensitive, sanitize
        return LogSanitizer.sanitizeUserData(userId)
    }

    /**
     * Gets current timestamp in ISO format
     */
    private fun getCurrentTimestamp(): String {
        return DateTimeFormatter.ISO_INSTANT.format(Instant.now())
    }

    /**
     * Escapes special characters in log values
     */
    private fun escapeValue(value: String): String {
        return value
            .replace("\"", "\\\"")
            .replace("=", "\\=")
            .replace(" ", "\\ ")
    }
}