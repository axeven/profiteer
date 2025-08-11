# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Profiteer is a personal finance Android application built with Kotlin and Jetpack Compose. The app uses MVVM architecture with Firebase for authentication and Firestore for data persistence.

## Build Commands

### Development
- `./gradlew build` - Build the project
- `./gradlew assembleDebug` - Build debug APK
- `./gradlew assembleRelease` - Build release APK
- `./gradlew installDebug` - Install debug build on connected device

### Testing
- `./gradlew test` - Run unit tests
- `./gradlew connectedAndroidTest` - Run instrumented tests on connected device/emulator
- `./gradlew testDebugUnitTest` - Run debug unit tests specifically

### Code Quality
- `./gradlew lint` - Run Android lint checks
- `./gradlew lintDebug` - Run lint on debug build variant

## Architecture

The codebase follows MVVM (Model-View-ViewModel) architecture with these key packages:

- **`com.axeven.profiteer.data`** - Data layer including repositories and data sources
- **`com.axeven.profiteer.viewmodel`** - ViewModels for business logic and state management
- **`com.axeven.profiteer.ui`** - UI components built with Jetpack Compose
- **`com.axeven.profiteer.utils`** - Utility classes and helper functions

### Key Technologies
- **Jetpack Compose** for modern declarative UI
- **Firebase Authentication** for user management
- **Firestore Database** for cloud data storage
- **Material 3** for design system and theming

### Application Structure
- **MainActivity.kt** - Single activity hosting all Compose screens
- **Theme system** - Located in `ui/theme/` with Color.kt, Theme.kt, and Type.kt
- **Navigation** - Compose Navigation (when implemented)

## Development Notes

- Target SDK: 36, Min SDK: 24
- Java/Kotlin compatibility: Java 11
- The project uses Gradle Version Catalogs (`gradle/libs.versions.toml`) for dependency management
- Firebase configuration is handled via `google-services.json`
- The app currently contains boilerplate code and is in early development stage

## Testing Strategy

- Unit tests in `src/test/` using JUnit
- Instrumented tests in `src/androidTest/` using Espresso and Compose testing
- UI tests should use Compose testing utilities from `androidx.compose.ui.test`