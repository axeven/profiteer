# Profiteer

A comprehensive personal finance management Android application built with modern Android development practices.

## Features

### 🏦 Wallet Management
- **Physical Wallets**: Real-world accounts (bank accounts, cash, crypto wallets)
- **Logical Wallets**: Virtual categorizations for budgeting (monthly expenses, emergency fund, savings)
- **Multi-currency Support**: Each wallet can operate in different currencies
- **Initial Balance Setup**: Set initial balances that won't affect transaction analytics

### 💰 Transaction Tracking
- **Real-time Balance Updates**: Transactions automatically update wallet balances
- **Transaction Analytics**: Income/expense tracking excluding initial balances
- **Thousands Separator Formatting**: All amounts display with proper formatting (1,234.56)
- **Multi-currency Display**: View amounts in wallet's native currency or default currency

### 🔧 Settings & Configuration
- **Wallet Management**: Create, edit, delete wallets with validation
- **Currency Configuration**: Set default currency and conversion rates
- **Name Validation**: Prevents duplicate wallet names
- **Form Validation**: Comprehensive input validation with error messages

### 🔐 Security & Data
- **Firebase Authentication**: Secure Google Sign-In integration
- **User Data Isolation**: Each user's data is completely isolated
- **Real-time Sync**: All data syncs across devices in real-time

## Tech Stack
- **Frontend**: Android (Kotlin) with Jetpack Compose
- **Backend**: Firebase Authentication + Firestore Database
- **Architecture**: MVVM with Repository Pattern
- **UI Design**: Material 3 Design System
- **Dependency Injection**: Hilt
- **State Management**: StateFlow and Compose State

## Project Structure
```
app/src/main/java/com/axeven/profiteer/
├── data/
│   ├── model/           # Data models (Wallet, Transaction, etc.)
│   └── repository/      # Repository layer for data access
├── ui/
│   ├── home/           # Home screen with dashboard
│   └── settings/       # Settings screen with wallet management
├── utils/              # Utility classes (NumberFormatter)
└── viewmodel/          # ViewModels for business logic
```

## Key Architectural Decisions

### Wallet System Design
- **Physical wallets** represent real-world accounts with actual balances
- **Logical wallets** are virtual allocations built on top of physical wallets
- Initial balance tracking separate from transaction-based balance changes

### Number Formatting
- All financial amounts display with thousands separators (1,234.56)
- Smart input parsing handles both formatted and plain number input
- Consistent formatting across all UI components

### Data Validation
- Wallet name uniqueness validation (case-insensitive)
- Numeric input validation with clear error messaging
- Form state management with real-time validation

## Getting Started

1. Clone the repository
2. Set up Firebase project and add `google-services.json`
3. Configure SHA-1 fingerprint in Firebase console
4. Build and run the project

## Build Commands
- `./gradlew build` - Build the project
- `./gradlew assembleDebug` - Build debug APK
- `./gradlew test` - Run unit tests
- `./gradlew lint` - Run Android lint checks