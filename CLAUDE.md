# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Profiteer is a personal finance Android application built with Kotlin and Jetpack Compose. The app uses MVVM architecture with Firebase for authentication and Firestore for data persistence. The app supports multi-currency wallet management with automatic currency conversion for balance aggregation.

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

- **`com.axeven.profiteerapp.data`** - Data layer including repositories and data sources
  - `model/` - Data models (Wallet, Transaction, CurrencyRate, UserPreferences)  
  - `repository/` - Repository pattern implementations for data access
- **`com.axeven.profiteerapp.viewmodel`** - ViewModels for business logic and state management
- **`com.axeven.profiteerapp.ui`** - UI components built with Jetpack Compose
  - `home/` - Home screen with wallet balance aggregation
  - `settings/` - Settings screen with currency rate management
  - `theme/` - Material 3 theming system
- **`com.axeven.profiteerapp.utils`** - Utility classes and helper functions
  - `NumberFormatter` - Currency formatting with multi-currency support

### Key Technologies
- **Jetpack Compose** for modern declarative UI
- **Firebase Authentication** with Google Sign-in integration
- **Firestore Database** for cloud data storage (Native Mode)
- **Material 3** for design system and theming
- **Hilt** for dependency injection
- **Kotlin Coroutines & Flow** for reactive programming

### Application Structure
- **MainActivity.kt** - Single activity hosting all Compose screens
- **Theme system** - Located in `ui/theme/` with Color.kt, Theme.kt, and Type.kt
- **Navigation** - Compose Navigation (when implemented)

## Core Features

### Multi-Currency Wallet Management
- Support for multiple wallet types (Physical, Investment, etc.)
- Comprehensive currency support including:
  - Standard currencies: USD, EUR, GBP, JPY, CAD, AUD, IDR
  - Precious metals: GOLD (gram-based pricing)
  - Cryptocurrency: BTC (8-decimal precision)
- Automatic currency conversion for balance aggregation
- Warning system for missing conversion rates

### Currency Rate Management
- Default rates for consistent conversion across time periods
- Monthly rates for time-specific conversions
- Bi-directional rate lookup (direct and inverse conversion)
- Smart rate fallback: Default → Monthly when rates missing

### Transaction Management
- Income, Expense, and Transfer transaction types
- Real-time wallet balance updates
- Firestore real-time synchronization with proper document ID mapping

## Development Notes

- Target SDK: 36, Min SDK: 24
- Java/Kotlin compatibility: Java 11
- The project uses Gradle Version Catalogs (`gradle/libs.versions.toml`) for dependency management
- Firebase configuration is handled via `google-services.json`
- **Firebase Project**: Must be in Native Mode (not Datastore Mode) for Firestore operations
- **Authentication**: Requires proper Web Client ID configuration in `AuthRepository.kt`

## Firebase Setup Requirements

### Firestore Configuration
- **Database Mode**: Native Mode (not Datastore Mode)
- **Real-time Listeners**: Used for live data synchronization
- **Document ID Mapping**: Manual mapping required for proper model instantiation
- **Index Management**: Avoided complex queries to prevent composite index requirements

### Authentication Setup
- Google Sign-in integration with proper SHA-1 fingerprints
- Web Client ID must match the Firebase project configuration
- ProGuard rules configured for Google Play Services compatibility

## Known Issues & Solutions

### Google Play Services Warnings
Non-critical SecurityException warnings may appear in logcat. These are handled with:
- ProGuard rules in `app/proguard-rules.pro`
- Exception handling in `AuthRepository.kt`

### Currency Conversion Logic
- Prioritizes default rates over monthly rates
- Falls back to inverse rate calculation when direct rates unavailable
- Warns users when rates are missing for proper balance calculation

## Testing Strategy

- Unit tests in `src/test/` using JUnit
- Instrumented tests in `src/androidTest/` using Espresso and Compose testing
- UI tests should use Compose testing utilities from `androidx.compose.ui.test`