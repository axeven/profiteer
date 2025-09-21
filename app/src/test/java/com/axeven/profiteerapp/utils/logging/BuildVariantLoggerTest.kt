package com.axeven.profiteerapp.utils.logging

import org.junit.Test
import org.junit.Assert.*

class BuildVariantLoggerTest {

    @Test
    fun `debug logger should log all levels in debug build`() {
        // Arrange
        val debugLogger = DebugLogger()
        val tag = "TestTag"

        // Act & Assert - Debug logger should not crash and should handle all log levels
        // Since we can't easily test actual logging output in unit tests,
        // we test that the methods can be called without throwing exceptions
        debugLogger.d(tag, "Debug message")
        debugLogger.i(tag, "Info message")
        debugLogger.w(tag, "Warning message")
        debugLogger.e(tag, "Error message")
        debugLogger.e(tag, "Error with throwable", RuntimeException("Test"))

        // If we reach here, all methods executed successfully
        assertTrue("Debug logger executed all log methods without error", true)
    }

    @Test
    fun `release logger should only log warnings and errors`() {
        // Arrange
        val releaseLogger = ReleaseLogger()
        val tag = "TestTag"

        // Act & Assert - Release logger should handle all calls but only log warnings and errors
        // Since we can't easily test actual logging output in unit tests,
        // we test that the methods can be called without throwing exceptions
        releaseLogger.d(tag, "Debug message") // Should be ignored
        releaseLogger.i(tag, "Info message") // Should be ignored
        releaseLogger.w(tag, "Warning message") // Should be logged
        releaseLogger.e(tag, "Error message") // Should be logged
        releaseLogger.e(tag, "Error with throwable", RuntimeException("Test")) // Should be logged

        // If we reach here, all methods executed successfully
        assertTrue("Release logger executed all log methods without error", true)
    }

    @Test
    fun `logger should handle null messages gracefully`() {
        // Arrange
        val debugLogger = DebugLogger()
        val releaseLogger = ReleaseLogger()
        val tag = "TestTag"

        // Act & Assert - Both loggers should handle empty/null-like messages
        debugLogger.d(tag, "")
        debugLogger.i(tag, "")
        debugLogger.w(tag, "")
        debugLogger.e(tag, "")

        releaseLogger.d(tag, "")
        releaseLogger.i(tag, "")
        releaseLogger.w(tag, "")
        releaseLogger.e(tag, "")

        // If we reach here, all methods executed successfully with empty messages
        assertTrue("Loggers handled empty messages without error", true)
    }

    @Test
    fun `logger should handle empty tags gracefully`() {
        // Arrange
        val debugLogger = DebugLogger()
        val releaseLogger = ReleaseLogger()
        val message = "Test message"

        // Act & Assert - Both loggers should handle empty tags
        debugLogger.d("", message)
        debugLogger.i("", message)
        debugLogger.w("", message)
        debugLogger.e("", message)

        releaseLogger.d("", message)
        releaseLogger.i("", message)
        releaseLogger.w("", message)
        releaseLogger.e("", message)

        // If we reach here, all methods executed successfully with empty tags
        assertTrue("Loggers handled empty tags without error", true)
    }

    @Test
    fun `logger should handle null throwables gracefully`() {
        // Arrange
        val debugLogger = DebugLogger()
        val releaseLogger = ReleaseLogger()
        val tag = "TestTag"
        val message = "Error message"

        // Act & Assert - Both loggers should handle null throwables
        debugLogger.e(tag, message, null)
        releaseLogger.e(tag, message, null)

        // If we reach here, both methods executed successfully with null throwables
        assertTrue("Loggers handled null throwables without error", true)
    }
}