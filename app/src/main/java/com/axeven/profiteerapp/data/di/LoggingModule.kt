package com.axeven.profiteerapp.data.di

import com.axeven.profiteerapp.utils.logging.DebugLogger
import com.axeven.profiteerapp.utils.logging.Logger
import com.axeven.profiteerapp.utils.logging.ReleaseLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
    fun provideLogger(): Logger {
        // For now, default to DebugLogger for development
        // This will be properly configured with BuildConfig later
        return DebugLogger()
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