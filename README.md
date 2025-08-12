# Profiteer

A comprehensive personal finance management Android application built with modern Android development practices.

## Features

### 🏦 Wallet Management
- **Physical Wallets**: Real-world accounts (bank accounts, cash, crypto wallets)
- **Investment Wallets**: Separate category for investment tracking
- **Multi-currency Support**: Comprehensive currency support including:
  - Standard currencies (USD, EUR, GBP, JPY, CAD, AUD, IDR)
  - Precious metals (GOLD with gram-based pricing)
  - Cryptocurrency (BTC with 8-decimal precision)
- **Automatic Balance Conversion**: Home screen aggregates all wallet balances in default currency
- **Smart Currency Conversion**: Uses conversion rates with fallback from default to monthly rates

### 💰 Transaction Tracking
- **Real-time Balance Updates**: Transactions automatically update wallet balances
- **Transaction Analytics**: Income/expense tracking excluding initial balances
- **Thousands Separator Formatting**: All amounts display with proper formatting (1,234.56)
- **Multi-currency Display**: View amounts in wallet's native currency or default currency

### 🔧 Settings & Configuration
- **Wallet Management**: Create, edit, delete wallets with comprehensive validation
- **Currency Rate Management**: Set default and monthly conversion rates
- **Multi-currency Rate Setup**: Support for all currencies including GOLD and BTC pricing
- **Smart Rate Warnings**: Alerts when conversion rates are missing for accurate balance calculations
- **Wallet Type Validation**: Proper dropdown handling for wallet types vs currencies

### 🔐 Security & Data
- **Firebase Authentication**: Secure Google Sign-In integration with proper Web Client ID configuration
- **Firestore Native Mode**: Optimized for real-time data synchronization
- **User Data Isolation**: Each user's data is completely isolated with proper document mapping
- **Real-time Sync**: All data syncs across devices in real-time with Firestore listeners

## Tech Stack
- **Frontend**: Android (Kotlin) with Jetpack Compose
- **Backend**: Firebase Authentication + Firestore Database
- **Architecture**: MVVM with Repository Pattern
- **UI Design**: Material 3 Design System
- **Dependency Injection**: Hilt
- **State Management**: StateFlow and Compose State

## Project Structure
```
app/src/main/java/com/axeven/profiteerapp/
├── data/
│   ├── model/           # Data models (Wallet, Transaction, CurrencyRate, UserPreferences)
│   └── repository/      # Repository pattern (Auth, Wallet, Transaction, CurrencyRate)
├── ui/
│   ├── home/           # Home screen with multi-currency balance aggregation
│   ├── settings/       # Settings with wallet & currency rate management
│   └── theme/          # Material 3 theming system
├── utils/              # NumberFormatter with multi-currency support
└── viewmodel/          # ViewModels with reactive state management
```

## Key Architectural Decisions

### Multi-Currency System Design
- **Currency-specific formatting**: Each currency has tailored precision (BTC: 8 decimals, GOLD: 3 decimals)
- **Smart conversion logic**: Prioritizes default rates, falls back to monthly rates, supports bi-directional conversion
- **Balance aggregation**: Home screen converts all wallets to default currency with missing rate warnings

### Firestore Integration
- **Document ID mapping**: Manual mapping ensures proper model instantiation with real document IDs
- **Real-time listeners**: Live data synchronization without composite index requirements
- **Error handling**: Comprehensive error handling for authentication and data operations

### State Management
- **Reactive UI**: StateFlow and Compose State for live updates
- **MVVM pattern**: Clear separation between UI, business logic, and data layers
- **Hilt DI**: Proper dependency injection for testable, maintainable code

## Getting Started

### Prerequisites
- Android Studio with Kotlin support
- Firebase account for backend services

### Setup Steps
1. Clone the repository
2. Create Firebase project in **Native Mode** (not Datastore Mode)
3. Enable Authentication with Google Sign-in provider
4. Create Firestore database in Native Mode
5. Add `google-services.json` to `app/` directory
6. Configure SHA-1 fingerprint in Firebase console
7. Update Web Client ID in `AuthRepository.kt` if needed
8. Build and run the project

### Firebase Configuration Requirements
- **Firestore**: Must be in Native Mode for real-time listeners
- **Authentication**: Google Sign-in with proper OAuth 2.0 setup
- **Security Rules**: Configure appropriate read/write rules for user data isolation

## Build Commands
- `./gradlew build` - Build the project
- `./gradlew assembleDebug` - Build debug APK
- `./gradlew test` - Run unit tests
- `./gradlew lint` - Run Android lint checks