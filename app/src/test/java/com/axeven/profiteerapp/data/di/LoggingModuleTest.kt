package com.axeven.profiteerapp.data.di

import com.axeven.profiteerapp.utils.logging.DebugLogger
import com.axeven.profiteerapp.utils.logging.Logger
import com.axeven.profiteerapp.utils.logging.ReleaseLogger
import org.junit.Test
import org.junit.Assert.*

class LoggingModuleTest {

    @Test
    fun `should provide logger without crashing`() {
        // Arrange
        val module = LoggingModule

        // Act
        val logger = module.provideLogger()

        // Assert
        assertNotNull("Should provide a logger instance", logger)
        assertTrue("Should provide a Logger implementation", logger is Logger)
    }

    @Test
    fun `should provide consistent logger type`() {
        // Arrange
        val module = LoggingModule

        // Act
        val logger1 = module.provideLogger()
        val logger2 = module.provideLogger()

        // Assert
        assertEquals("Should return same logger type for consistent calls",
                    logger1::class, logger2::class)
    }

    @Test
    fun `test helper method should provide debug logger for debug config`() {
        // Arrange
        val module = LoggingModule

        // Act
        val logger = module.provideLogger(isDebugBuild = true)

        // Assert
        assertTrue("Should provide DebugLogger for debug builds", logger is DebugLogger)
    }

    @Test
    fun `test helper method should provide release logger for release config`() {
        // Arrange
        val module = LoggingModule

        // Act
        val logger = module.provideLogger(isDebugBuild = false)

        // Assert
        assertTrue("Should provide ReleaseLogger for release builds", logger is ReleaseLogger)
    }
}