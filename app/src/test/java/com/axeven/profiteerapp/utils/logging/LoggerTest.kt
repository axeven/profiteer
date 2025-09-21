package com.axeven.profiteerapp.utils.logging

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

class LoggerTest {

    private lateinit var testLogger: TestLogger

    // Test implementation to capture logs
    class TestLogger : Logger {
        val logs = mutableListOf<LogEntry>()

        override fun d(tag: String, message: String) {
            logs.add(LogEntry(LogLevel.DEBUG, tag, message, null))
        }

        override fun i(tag: String, message: String) {
            logs.add(LogEntry(LogLevel.INFO, tag, message, null))
        }

        override fun w(tag: String, message: String) {
            logs.add(LogEntry(LogLevel.WARNING, tag, message, null))
        }

        override fun e(tag: String, message: String, throwable: Throwable?) {
            logs.add(LogEntry(LogLevel.ERROR, tag, message, throwable))
        }
    }

    data class LogEntry(
        val level: LogLevel,
        val tag: String,
        val message: String,
        val throwable: Throwable?
    )

    enum class LogLevel { DEBUG, INFO, WARNING, ERROR }

    @Before
    fun setup() {
        testLogger = TestLogger()
    }

    @Test
    fun `should log debug message with correct tag and message`() {
        // Arrange
        val tag = "TestTag"
        val message = "Debug message"

        // Act
        testLogger.d(tag, message)

        // Assert
        assertEquals(1, testLogger.logs.size)
        val logEntry = testLogger.logs.first()
        assertEquals(LogLevel.DEBUG, logEntry.level)
        assertEquals(tag, logEntry.tag)
        assertEquals(message, logEntry.message)
        assertNull(logEntry.throwable)
    }

    @Test
    fun `should log info message with correct parameters`() {
        // Arrange
        val tag = "InfoTag"
        val message = "Info message"

        // Act
        testLogger.i(tag, message)

        // Assert
        assertEquals(1, testLogger.logs.size)
        val logEntry = testLogger.logs.first()
        assertEquals(LogLevel.INFO, logEntry.level)
        assertEquals(tag, logEntry.tag)
        assertEquals(message, logEntry.message)
    }

    @Test
    fun `should log warning message with correct parameters`() {
        // Arrange
        val tag = "WarnTag"
        val message = "Warning message"

        // Act
        testLogger.w(tag, message)

        // Assert
        assertEquals(1, testLogger.logs.size)
        val logEntry = testLogger.logs.first()
        assertEquals(LogLevel.WARNING, logEntry.level)
        assertEquals(tag, logEntry.tag)
        assertEquals(message, logEntry.message)
    }

    @Test
    fun `should log error message with throwable`() {
        // Arrange
        val tag = "ErrorTag"
        val message = "Error message"
        val throwable = RuntimeException("Test exception")

        // Act
        testLogger.e(tag, message, throwable)

        // Assert
        assertEquals(1, testLogger.logs.size)
        val logEntry = testLogger.logs.first()
        assertEquals(LogLevel.ERROR, logEntry.level)
        assertEquals(tag, logEntry.tag)
        assertEquals(message, logEntry.message)
        assertEquals(throwable, logEntry.throwable)
    }

    @Test
    fun `should log error message without throwable`() {
        // Arrange
        val tag = "ErrorTag"
        val message = "Error message"

        // Act
        testLogger.e(tag, message)

        // Assert
        assertEquals(1, testLogger.logs.size)
        val logEntry = testLogger.logs.first()
        assertEquals(LogLevel.ERROR, logEntry.level)
        assertEquals(tag, logEntry.tag)
        assertEquals(message, logEntry.message)
        assertNull(logEntry.throwable)
    }

    @Test
    fun `should handle empty tag gracefully`() {
        // Arrange
        val tag = ""
        val message = "Message with empty tag"

        // Act
        testLogger.d(tag, message)

        // Assert
        assertEquals(1, testLogger.logs.size)
        val logEntry = testLogger.logs.first()
        assertEquals("", logEntry.tag)
        assertEquals(message, logEntry.message)
    }

    @Test
    fun `should handle empty message gracefully`() {
        // Arrange
        val tag = "TestTag"
        val message = ""

        // Act
        testLogger.d(tag, message)

        // Assert
        assertEquals(1, testLogger.logs.size)
        val logEntry = testLogger.logs.first()
        assertEquals(tag, logEntry.tag)
        assertEquals("", logEntry.message)
    }

    @Test
    fun `should handle multiple log calls correctly`() {
        // Arrange & Act
        testLogger.d("Debug", "Debug message")
        testLogger.i("Info", "Info message")
        testLogger.w("Warn", "Warning message")
        testLogger.e("Error", "Error message")

        // Assert
        assertEquals(4, testLogger.logs.size)
        assertEquals(LogLevel.DEBUG, testLogger.logs[0].level)
        assertEquals(LogLevel.INFO, testLogger.logs[1].level)
        assertEquals(LogLevel.WARNING, testLogger.logs[2].level)
        assertEquals(LogLevel.ERROR, testLogger.logs[3].level)
    }
}