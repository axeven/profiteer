package com.axeven.profiteerapp.utils.logging

/**
 * Logging interface that abstracts the underlying logging implementation.
 * This allows for consistent logging across the application and enables
 * build-variant specific behavior (e.g., disabling debug logs in release builds).
 */
interface Logger {

    /**
     * Log a debug message.
     * @param tag Used to identify the source of the log message
     * @param message The message to be logged
     */
    fun d(tag: String, message: String)

    /**
     * Log an informational message.
     * @param tag Used to identify the source of the log message
     * @param message The message to be logged
     */
    fun i(tag: String, message: String)

    /**
     * Log a warning message.
     * @param tag Used to identify the source of the log message
     * @param message The message to be logged
     */
    fun w(tag: String, message: String)

    /**
     * Log an error message.
     * @param tag Used to identify the source of the log message
     * @param message The message to be logged
     * @param throwable Optional throwable to be logged with the message
     */
    fun e(tag: String, message: String, throwable: Throwable? = null)
}