package com.axeven.profiteerapp.data.di

import android.content.Context
import android.content.pm.ApplicationInfo
import com.axeven.profiteerapp.utils.logging.DebugLogger
import com.axeven.profiteerapp.utils.logging.Logger
import com.axeven.profiteerapp.utils.logging.ReleaseLogger
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class LoggingModuleTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockApplicationInfo: ApplicationInfo

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        `when`(mockContext.applicationInfo).thenReturn(mockApplicationInfo)
    }

    @Test
    fun `should provide debug logger for debug build`() {
        // Arrange
        val module = LoggingModule
        mockApplicationInfo.flags = ApplicationInfo.FLAG_DEBUGGABLE

        // Act
        val logger = module.provideLogger(mockContext)

        // Assert
        assertNotNull("Should provide a logger instance", logger)
        assertTrue("Should provide DebugLogger for debug build", logger is DebugLogger)
    }

    @Test
    fun `should provide release logger for release build`() {
        // Arrange
        val module = LoggingModule
        mockApplicationInfo.flags = 0 // Not debuggable

        // Act
        val logger = module.provideLogger(mockContext)

        // Assert
        assertNotNull("Should provide a logger instance", logger)
        assertTrue("Should provide ReleaseLogger for release build", logger is ReleaseLogger)
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