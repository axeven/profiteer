package com.axeven.profiteerapp.utils.logging

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseCrashlyticsLogger @Inject constructor(
    private val context: Context
) : AnalyticsLogger {

    private val crashlytics = FirebaseCrashlytics.getInstance()
    private val preferences: SharedPreferences by lazy {
        context.getSharedPreferences("logging_preferences", Context.MODE_PRIVATE)
    }

    @SuppressLint("LogNotTimber") // Fallback logging when Crashlytics fails
    override fun trackError(tag: String, message: String, throwable: Throwable?) {
        if (!isAnalyticsEnabled()) return

        try {
            // Sanitize message before sending to analytics
            val sanitizedMessage = LogSanitizer.sanitizeAll(message)

            crashlytics.setCustomKey("error_tag", tag)
            crashlytics.setCustomKey("error_message", sanitizedMessage)

            if (throwable != null) {
                crashlytics.recordException(throwable)
            } else {
                // Create a custom exception for non-exception errors
                val customException = RuntimeException("$tag: $sanitizedMessage")
                crashlytics.recordException(customException)
            }
        } catch (e: Exception) {
            // Fail silently - analytics should never crash the app
            android.util.Log.w("FirebaseCrashlyticsLogger", "Failed to track error: ${e.message}")
        }
    }

    @SuppressLint("LogNotTimber") // Fallback logging when Crashlytics fails
    override fun trackUserAction(action: String, properties: Map<String, Any>) {
        if (!isAnalyticsEnabled()) return

        try {
            crashlytics.setCustomKey("user_action", action)

            // Add sanitized properties as custom keys
            properties.forEach { (key, value) ->
                val sanitizedValue = when (value) {
                    is String -> LogSanitizer.sanitizeAll(value)
                    else -> value.toString()
                }
                crashlytics.setCustomKey("action_$key", sanitizedValue)
            }
        } catch (e: Exception) {
            android.util.Log.w("FirebaseCrashlyticsLogger", "Failed to track user action: ${e.message}")
        }
    }

    @SuppressLint("LogNotTimber") // Fallback logging when Crashlytics fails
    override fun trackPerformance(operation: String, durationMs: Long, metadata: Map<String, Any>) {
        if (!isAnalyticsEnabled()) return

        try {
            crashlytics.setCustomKey("performance_operation", operation)
            crashlytics.setCustomKey("performance_duration_ms", durationMs)

            // Add metadata as custom keys
            metadata.forEach { (key, value) ->
                val sanitizedValue = when (value) {
                    is String -> LogSanitizer.sanitizeAll(value)
                    else -> value.toString()
                }
                crashlytics.setCustomKey("perf_$key", sanitizedValue)
            }

            // Log performance issues as non-fatal exceptions if they exceed thresholds
            if (durationMs > 5000) { // 5 seconds threshold
                val performanceException = RuntimeException("Performance issue: $operation took ${durationMs}ms")
                crashlytics.recordException(performanceException)
            }
        } catch (e: Exception) {
            android.util.Log.w("FirebaseCrashlyticsLogger", "Failed to track performance: ${e.message}")
        }
    }

    @SuppressLint("LogNotTimber") // Fallback logging when Crashlytics fails
    override fun batchTrackEvents(events: List<AnalyticsEvent>) {
        if (!isAnalyticsEnabled()) return

        try {
            events.forEachIndexed { index, event ->
                crashlytics.setCustomKey("batch_event_${index}_type", event.type)

                event.properties.forEach { (key, value) ->
                    val sanitizedValue = when (value) {
                        is String -> LogSanitizer.sanitizeAll(value)
                        else -> value.toString()
                    }
                    crashlytics.setCustomKey("batch_event_${index}_$key", sanitizedValue)
                }
            }

            // Record batch processing as a custom event
            val batchException = RuntimeException("Batch analytics events processed: ${events.size} events")
            crashlytics.recordException(batchException)
        } catch (e: Exception) {
            android.util.Log.w("FirebaseCrashlyticsLogger", "Failed to batch track events: ${e.message}")
        }
    }

    override fun isAnalyticsEnabled(): Boolean {
        return preferences.getBoolean("analytics_enabled", true) &&
               crashlytics.isCrashlyticsCollectionEnabled
    }

    @SuppressLint("LogNotTimber") // Fallback logging when Crashlytics fails
    override fun reportCrash(tag: String, message: String, throwable: Throwable?) {
        try {
            // Always report crashes regardless of analytics settings (for app stability)
            val sanitizedMessage = LogSanitizer.sanitizeAll(message)

            crashlytics.setCustomKey("crash_tag", tag)
            crashlytics.setCustomKey("crash_message", sanitizedMessage)

            if (throwable != null) {
                crashlytics.recordException(throwable)
            } else {
                val crashException = RuntimeException("CRASH: $tag - $sanitizedMessage")
                crashlytics.recordException(crashException)
            }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseCrashlyticsLogger", "Failed to report crash: ${e.message}")
        }
    }

    fun setAnalyticsEnabled(enabled: Boolean) {
        preferences.edit {
            putBoolean("analytics_enabled", enabled)
        }

        crashlytics.setCrashlyticsCollectionEnabled(enabled)
    }

    @SuppressLint("LogNotTimber") // Fallback logging when Crashlytics fails
    fun setUserId(userId: String) {
        try {
            val sanitizedUserId = LogSanitizer.sanitizeUserId(userId)
            crashlytics.setUserId(sanitizedUserId)
        } catch (e: Exception) {
            android.util.Log.w("FirebaseCrashlyticsLogger", "Failed to set user ID: ${e.message}")
        }
    }
}