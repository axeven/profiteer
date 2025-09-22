package com.axeven.profiteerapp.data.di

import android.content.Context
import android.content.pm.ApplicationInfo
import com.axeven.profiteerapp.utils.logging.AnalyticsLogger
import com.axeven.profiteerapp.utils.logging.DebugLogger
import com.axeven.profiteerapp.utils.logging.FirebaseCrashlyticsLogger
import com.axeven.profiteerapp.utils.logging.Logger
import com.axeven.profiteerapp.utils.logging.ReleaseLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for providing logging dependencies.
 * Provides the appropriate logger implementation based on the build variant.
 */
@Module
@InstallIn(SingletonComponent::class)
object LoggingModule {

    /**
     * Provides the logger implementation based on build configuration.
     * In debug builds, provides DebugLogger which logs all levels.
     * In release builds, provides ReleaseLogger which only logs warnings and errors.
     */
    @Provides
    @Singleton
    fun provideLogger(
        @ApplicationContext context: Context
    ): Logger {
        // Check if app is in debug mode by checking application flags
        val isDebug = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        return if (isDebug) {
            DebugLogger()
        } else {
            ReleaseLogger()
        }
    }

    /**
     * Provides the analytics logger for Firebase Crashlytics integration.
     */
    @Provides
    @Singleton
    fun provideAnalyticsLogger(
        @ApplicationContext context: Context
    ): AnalyticsLogger {
        return FirebaseCrashlyticsLogger(context)
    }

    /**
     * Test-friendly version that allows manual control of build configuration.
     * This is used by tests to verify behavior for different build variants.
     */
    internal fun provideLogger(isDebugBuild: Boolean): Logger {
        return if (isDebugBuild) {
            DebugLogger()
        } else {
            ReleaseLogger()
        }
    }
}