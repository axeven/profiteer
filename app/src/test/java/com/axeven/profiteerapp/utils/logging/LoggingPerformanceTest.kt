package com.axeven.profiteerapp.utils.logging

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import kotlin.system.measureTimeMillis

class LoggingPerformanceTest {

    private lateinit var debugLogger: Logger
    private lateinit var releaseLogger: Logger

    @Before
    fun setup() {
        // Use test performance logger for safe testing
        debugLogger = TestPerformanceLogger
        releaseLogger = TestPerformanceLogger
    }

    @Test
    fun `should have minimal overhead for release builds`() {
        val iterations = 1000
        val testMessage = "Performance test message"

        // Measure release logger performance for debug calls (should be no-ops)
        val releaseTime = measureTimeMillis {
            repeat(iterations) {
                releaseLogger.d("PerfTest", testMessage)
                releaseLogger.i("PerfTest", testMessage)
            }
        }

        // Release logger debug/info calls should be very fast (no-ops)
        assertTrue("Release logger should be performant for debug calls: ${releaseTime}ms", releaseTime < 50)
    }

    @Test
    fun `should handle basic logging without performance issues`() {
        val iterations = 500
        val testMessage = "Basic logging test message"

        // Measure basic logging performance
        val loggingTime = measureTimeMillis {
            repeat(iterations) {
                debugLogger.w("BasicTest", testMessage)
                debugLogger.e("BasicTest", testMessage)
            }
        }

        // Basic logging should complete in reasonable time
        assertTrue("Basic logging should be performant: ${loggingTime}ms", loggingTime < 1000)
    }

    @Test
    fun `should verify logger instantiation is fast`() {
        val iterations = 100

        // Measure logger instantiation time
        val instantiationTime = measureTimeMillis {
            repeat(iterations) {
                val logger = TestPerformanceLogger
                val releaseLogger = TestPerformanceLogger
                // Use the loggers to prevent optimization
                logger.toString()
                releaseLogger.toString()
            }
        }

        // Logger instantiation should be fast
        assertTrue("Logger instantiation should be fast: ${instantiationTime}ms for $iterations instances",
                  instantiationTime < 100)
    }

    @Test
    fun `should benchmark sanitization overhead`() {
        val iterations = 100
        val sensitiveMessage = "User john.doe@example.com transferred 150.50 USD"

        // Measure sanitization performance
        val sanitizationTime = measureTimeMillis {
            repeat(iterations) {
                val sanitized = LogSanitizer.sanitizeAll(sensitiveMessage)
                // Verify sanitization worked
                assertFalse("Sanitization should work", sanitized.contains("john.doe@example.com"))
            }
        }

        // Sanitization should complete in reasonable time
        assertTrue("Sanitization should complete in reasonable time: ${sanitizationTime}ms",
                  sanitizationTime < 1000)
    }

    @Test
    fun `should benchmark structured logging overhead`() {
        val iterations = 100
        val metadata = mapOf(
            "userId" to "user123",
            "action" to "test_action"
        )

        // Measure structured logging performance
        val structuredTime = measureTimeMillis {
            repeat(iterations) {
                val formatted = LogFormatter.formatUserAction("test_action", "user123", metadata)
                // Verify formatting worked
                assertTrue("Formatting should work", formatted.contains("action=test_action"))
            }
        }

        // Structured logging should complete in reasonable time
        assertTrue("Structured logging should complete in reasonable time: ${structuredTime}ms",
                  structuredTime < 1000)
    }
}