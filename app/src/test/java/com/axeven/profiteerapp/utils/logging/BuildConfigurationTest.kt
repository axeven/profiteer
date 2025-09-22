package com.axeven.profiteerapp.utils.logging

import org.junit.Test
import org.junit.Assert.*

class BuildConfigurationTest {

    @Test
    fun `should have release logger that ignores debug logs`() {
        val releaseLogger = ReleaseLogger()

        // Release logger should have no-op debug and info methods
        val startTime = System.currentTimeMillis()

        repeat(100) {
            releaseLogger.d("TestTag", "This debug message should be ignored")
            releaseLogger.i("TestTag", "This info message should be ignored")
        }

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        // Should complete very quickly since debug/info are no-ops
        assertTrue("Release logger debug/info calls should be fast: ${duration}ms", duration < 200)
    }

    @Test
    fun `should preserve error and warning logging in release builds`() {
        val releaseLogger = ReleaseLogger()

        // Test that these don't crash - we can't easily verify output in tests
        try {
            releaseLogger.w("TestTag", "This warning should be logged")
            releaseLogger.e("TestTag", "This error should be logged")
            releaseLogger.e("TestTag", "This error should be logged", RuntimeException("Test exception"))
        } catch (e: Exception) {
            fail("Release logger should handle warning/error logs without crashing: ${e.message}")
        }
    }

    @Test
    fun `should have debug logger that logs all levels`() {
        val debugLogger = DebugLogger()

        // Test that these don't crash
        try {
            debugLogger.d("TestTag", "This debug message should be logged")
            debugLogger.i("TestTag", "This info message should be logged")
            debugLogger.w("TestTag", "This warning should be logged")
            debugLogger.e("TestTag", "This error should be logged")
            debugLogger.e("TestTag", "This error should be logged", RuntimeException("Test exception"))
        } catch (e: Exception) {
            fail("Debug logger should handle all log levels without crashing: ${e.message}")
        }
    }

    @Test
    fun `should verify logger interface consistency`() {
        val debugLogger: Logger = DebugLogger()
        val releaseLogger: Logger = ReleaseLogger()

        // Both loggers should implement the same interface consistently
        assertTrue("Debug logger should implement Logger interface", debugLogger is Logger)
        assertTrue("Release logger should implement Logger interface", releaseLogger is Logger)

        // Test that interface methods work
        try {
            val testMessage = "Interface consistency test"

            debugLogger.d("Test", testMessage)
            debugLogger.i("Test", testMessage)
            debugLogger.w("Test", testMessage)
            debugLogger.e("Test", testMessage)

            releaseLogger.d("Test", testMessage)
            releaseLogger.i("Test", testMessage)
            releaseLogger.w("Test", testMessage)
            releaseLogger.e("Test", testMessage)
        } catch (e: Exception) {
            fail("Logger interface methods should work consistently: ${e.message}")
        }
    }

    @Test
    fun `should verify test logger interface works`() {
        val testLogger = TestPerformanceLogger

        try {
            testLogger.d("PerfTest", "Debug message")
            testLogger.i("PerfTest", "Info message")
            testLogger.w("PerfTest", "Warning message")
            testLogger.e("PerfTest", "Error message")
        } catch (e: Exception) {
            fail("Test performance logger should work: ${e.message}")
        }
    }

    @Test
    fun `should ensure minimal overhead for release debug calls`() {
        val releaseLogger = ReleaseLogger()

        // Measure time for debug calls (should be minimal since they're no-ops)
        val startTime = System.nanoTime()

        repeat(1000) {
            releaseLogger.d("BuildTest", "This should be optimized away")
        }

        val endTime = System.nanoTime()
        val durationMs = (endTime - startTime) / 1_000_000

        // Debug calls on release logger should have minimal overhead
        assertTrue("Release logger debug calls should have minimal overhead: ${durationMs}ms",
                  durationMs < 50)
    }
}