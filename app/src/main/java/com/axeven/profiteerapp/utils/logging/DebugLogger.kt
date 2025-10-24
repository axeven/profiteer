package com.axeven.profiteerapp.utils.logging

import android.annotation.SuppressLint
import timber.log.Timber

/**
 * Debug build logger that logs all levels.
 * This implementation is used in debug builds where we want full logging.
 */
class DebugLogger : Logger {

    init {
        // Plant debug tree if not already planted
        try {
            if (Timber.treeCount == 0) {
                Timber.plant(Timber.DebugTree())
            }
        } catch (e: Exception) {
            // Silently handle initialization issues in tests
        }
    }

    @SuppressLint("LogNotTimber") // Intentional fallback to android.util.Log when Timber fails
    override fun d(tag: String, message: String) {
        try {
            Timber.tag(tag).d(message)
        } catch (e: Exception) {
            // Fallback to system logging if Timber fails
            android.util.Log.d(tag, message)
        }
    }

    @SuppressLint("LogNotTimber") // Intentional fallback to android.util.Log when Timber fails
    override fun i(tag: String, message: String) {
        try {
            Timber.tag(tag).i(message)
        } catch (e: Exception) {
            android.util.Log.i(tag, message)
        }
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
}