package com.axeven.profiteerapp.utils.logging

import org.junit.Test
import org.junit.Assert.*
import java.io.File

class MigrationVerificationTest {

    @Test
    fun `should have zero android util Log imports in production code`() {
        val projectRoot = findProjectRoot()
        val sourceFiles = findKotlinSourceFiles(projectRoot)
        val filesWithAndroidLog = mutableListOf<String>()

        sourceFiles.forEach { file ->
            val content = file.readText()
            if (content.contains("import android.util.Log") && !isTestFile(file)) {
                filesWithAndroidLog.add(file.absolutePath)
            }
        }

        if (filesWithAndroidLog.isNotEmpty()) {
            val fileList = filesWithAndroidLog.joinToString("\n- ", prefix = "- ")
            fail("Found android.util.Log imports in production code:\n$fileList")
        }
    }

    @Test
    fun `should use Logger interface in all components`() {
        val projectRoot = findProjectRoot()
        val sourceFiles = findKotlinSourceFiles(projectRoot)
        val filesWithDirectAndroidLogUsage = mutableListOf<String>()

        sourceFiles.forEach { file ->
            if (!isTestFile(file) && !isLoggingUtilityFile(file)) {
                val content = file.readText()
                // Check for direct android.util.Log usage (not imports, but actual calls)
                if (content.contains("android.util.Log.") ||
                    content.contains("Log.d(") ||
                    content.contains("Log.i(") ||
                    content.contains("Log.w(") ||
                    content.contains("Log.e(")) {
                    filesWithDirectAndroidLogUsage.add(file.absolutePath)
                }
            }
        }

        if (filesWithDirectAndroidLogUsage.isNotEmpty()) {
            val fileList = filesWithDirectAndroidLogUsage.joinToString("\n- ", prefix = "- ")
            fail("Found direct android.util.Log usage in production code:\n$fileList")
        }
    }

    @Test
    fun `should have no hardcoded System out println calls in production code`() {
        val projectRoot = findProjectRoot()
        val sourceFiles = findKotlinSourceFiles(projectRoot)
        val filesWithSystemOut = mutableListOf<String>()

        sourceFiles.forEach { file ->
            if (!isTestFile(file) && !isLoggingUtilityFile(file)) {
                val content = file.readText()
                if (content.contains("System.out.println") ||
                    content.contains("println(") && !content.contains("// Test") && !content.contains("// DEBUG")) {
                    filesWithSystemOut.add(file.absolutePath)
                }
            }
        }

        if (filesWithSystemOut.isNotEmpty()) {
            val fileList = filesWithSystemOut.joinToString("\n- ", prefix = "- ")
            fail("Found System.out.println calls in production code:\n$fileList")
        }
    }

    @Test
    fun `should verify all ViewModels use Logger interface`() {
        val projectRoot = findProjectRoot()
        val viewModelFiles = findViewModelFiles(projectRoot)
        val viewModelsWithoutLogger = mutableListOf<String>()

        viewModelFiles.forEach { file ->
            val content = file.readText()
            // Check if ViewModel has Logger dependency injected
            if (!content.contains("logger: Logger") &&
                !content.contains("private val logger") &&
                !content.contains("private lateinit var logger")) {
                viewModelsWithoutLogger.add(file.absolutePath)
            }
        }

        if (viewModelsWithoutLogger.isNotEmpty()) {
            val fileList = viewModelsWithoutLogger.joinToString("\n- ", prefix = "- ")
            fail("Found ViewModels without Logger interface:\n$fileList")
        }
    }

    @Test
    fun `should verify all Repositories use Logger interface`() {
        val projectRoot = findProjectRoot()
        val repositoryFiles = findRepositoryFiles(projectRoot)
        val repositoriesWithoutLogger = mutableListOf<String>()

        repositoryFiles.forEach { file ->
            val content = file.readText()
            // Check if Repository has Logger dependency injected
            if (!content.contains("logger: Logger") &&
                !content.contains("private val logger") &&
                !content.contains("private lateinit var logger")) {
                repositoriesWithoutLogger.add(file.absolutePath)
            }
        }

        if (repositoriesWithoutLogger.isNotEmpty()) {
            val fileList = repositoriesWithoutLogger.joinToString("\n- ", prefix = "- ")
            fail("Found Repositories without Logger interface:\n$fileList")
        }
    }

    @Test
    fun `should verify logging utilities exist and are properly structured`() {
        val projectRoot = findProjectRoot()
        val loggingUtilsDir = File(projectRoot, "app/src/main/java/com/axeven/profiteerapp/utils/logging")

        assertTrue("Logging utilities directory should exist", loggingUtilsDir.exists())

        val requiredFiles = listOf(
            "Logger.kt",
            "DebugLogger.kt",
            "ReleaseLogger.kt",
            "LogSanitizer.kt",
            "LogFormatter.kt",
            "AnalyticsLogger.kt",
            "FirebaseCrashlyticsLogger.kt"
        )

        requiredFiles.forEach { fileName ->
            val file = File(loggingUtilsDir, fileName)
            assertTrue("Required logging file should exist: $fileName", file.exists())
        }
    }

    // Helper methods
    private fun findProjectRoot(): File {
        var current = File(System.getProperty("user.dir"))
        while (current.parent != null) {
            if (File(current, "app").exists() && File(current, "build.gradle.kts").exists()) {
                return current
            }
            current = current.parentFile
        }
        return File(System.getProperty("user.dir"))
    }

    private fun findKotlinSourceFiles(projectRoot: File): List<File> {
        val sourceDir = File(projectRoot, "app/src/main/java")
        return sourceDir.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .toList()
    }

    private fun findViewModelFiles(projectRoot: File): List<File> {
        val sourceDir = File(projectRoot, "app/src/main/java")
        return sourceDir.walkTopDown()
            .filter { it.isFile && it.extension == "kt" && it.name.contains("ViewModel") }
            .toList()
    }

    private fun findRepositoryFiles(projectRoot: File): List<File> {
        val sourceDir = File(projectRoot, "app/src/main/java")
        return sourceDir.walkTopDown()
            .filter { it.isFile && it.extension == "kt" && it.name.endsWith("Repository.kt") }
            .toList()
    }

    private fun isTestFile(file: File): Boolean {
        return file.absolutePath.contains("/test/") ||
               file.absolutePath.contains("\\test\\") ||
               file.name.endsWith("Test.kt")
    }

    private fun isLoggingUtilityFile(file: File): Boolean {
        return file.absolutePath.contains("utils/logging") ||
               file.name.contains("Logger") ||
               file.name.contains("LogSanitizer") ||
               file.name.contains("LogFormatter")
    }
}