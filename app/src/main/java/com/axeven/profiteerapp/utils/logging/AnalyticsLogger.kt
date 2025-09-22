package com.axeven.profiteerapp.utils.logging

// Data classes for analytics events
data class AnalyticsEvent(
    val type: String,
    val properties: Map<String, Any>
)

// Interface for analytics integration
interface AnalyticsLogger {
    fun trackError(tag: String, message: String, throwable: Throwable?)
    fun trackUserAction(action: String, properties: Map<String, Any>)
    fun trackPerformance(operation: String, durationMs: Long, metadata: Map<String, Any>)
    fun batchTrackEvents(events: List<AnalyticsEvent>)
    fun isAnalyticsEnabled(): Boolean
    fun reportCrash(tag: String, message: String, throwable: Throwable?)
}