package com.axeven.profiteerapp.utils.logging

/**
 * Performance-optimized logger for production use.
 * This implementation minimizes overhead for high-frequency logging scenarios.
 */
class PerformanceOptimizedLogger(private val isDebugBuild: Boolean = false) : Logger {

    override fun d(tag: String, message: String) {
        if (isDebugBuild) {
            // Only log debug in debug builds
            android.util.Log.d(tag, message)
        }
        // No-op in release builds for maximum performance
    }

    override fun i(tag: String, message: String) {
        if (isDebugBuild) {
            android.util.Log.i(tag, message)
        }
        // No-op in release builds for maximum performance
    }

    override fun w(tag: String, message: String) {
        // Always log warnings
        android.util.Log.w(tag, message)
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        // Always log errors
        if (throwable != null) {
            android.util.Log.e(tag, message, throwable)
        } else {
            android.util.Log.e(tag, message)
        }
    }
}

/**
 * Lazy-initialized performance-optimized logger for testing scenarios
 */
object TestPerformanceLogger : Logger {
    private val isTestEnvironment = try {
        Class.forName("org.junit.Test")
        true
    } catch (e: ClassNotFoundException) {
        false
    }

    override fun d(tag: String, message: String) {
        if (isTestEnvironment) {
            // In tests, we can log everything for debugging
            println("DEBUG $tag: $message")
        }
    }

    override fun i(tag: String, message: String) {
        if (isTestEnvironment) {
            println("INFO $tag: $message")
        }
    }

    override fun w(tag: String, message: String) {
        if (isTestEnvironment) {
            println("WARN $tag: $message")
        }
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        if (isTestEnvironment) {
            if (throwable != null) {
                println("ERROR $tag: $message - ${throwable.message}")
            } else {
                println("ERROR $tag: $message")
            }
        }
    }
}