package com.axeven.profiteerapp.utils.logging

import android.annotation.SuppressLint
import timber.log.Timber

/**
 * Release build logger that only logs warnings and errors.
 * This implementation is used in release builds where we want minimal logging
 * to reduce performance impact and avoid information leakage.
 */
class ReleaseLogger : Logger {

    init {
        // Plant a release tree that only logs warnings and errors
        try {
            if (Timber.treeCount == 0) {
                Timber.plant(ReleaseTree())
            }
        } catch (e: Exception) {
            // Silently handle initialization issues in tests
        }
    }

    override fun d(tag: String, message: String) {
        // Debug logs are ignored in release builds
    }

    override fun i(tag: String, message: String) {
        // Info logs are ignored in release builds
    }

    @SuppressLint("LogNotTimber") // Intentional fallback to android.util.Log when Timber fails
    override fun w(tag: String, message: String) {
        try {
            Timber.tag(tag).w(message)
        } catch (e: Exception) {
            android.util.Log.w(tag, message)
        }
    }

    @SuppressLint("LogNotTimber") // Intentional fallback to android.util.Log when Timber fails
    override fun e(tag: String, message: String, throwable: Throwable?) {
        try {
            if (throwable != null) {
                Timber.tag(tag).e(throwable, message)
            } else {
                Timber.tag(tag).e(message)
            }
        } catch (e: Exception) {
            if (throwable != null) {
                android.util.Log.e(tag, message, throwable)
            } else {
                android.util.Log.e(tag, message)
            }
        }
    }

    /**
     * Custom Timber tree for release builds that only logs warnings and errors.
     */
    private class ReleaseTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            // Only log warnings and errors in release builds
            if (priority == android.util.Log.WARN || priority == android.util.Log.ERROR) {
                super.log(priority, tag, message, t)
            }
        }
    }
}