# Profiteer

A comprehensive personal finance management Android application built with modern Android development practices. Profiteer provides a sophisticated dual-wallet system for managing both physical and logical financial accounts with full multi-currency support and real-time synchronization.

## 🏗️ Architecture

Profiteer follows the **MVVM (Model-View-ViewModel)** architecture pattern with the Repository pattern for data abstraction. The application is built using:

- **Frontend**: Android (Kotlin) with Jetpack Compose
- **Backend**: Firebase Authentication + Firestore Database (Native Mode)
- **UI Framework**: Material 3 Design System
- **Dependency Injection**: Hilt
- **State Management**: StateFlow and Compose State
- **Reactive Programming**: Kotlin Coroutines & Flow

## ✨ Key Features

### 🏦 Advanced Wallet Management
- **Dual Wallet System**: 
  - **Physical Wallets**: Real-world accounts (bank accounts, cash, crypto wallets)
  - **Logical Wallets**: Virtual categorizations for budgeting and allocation
- **Multi-Currency Support**: Comprehensive currency support including:
  - Standard currencies (USD, EUR, GBP, JPY, CAD, AUD, IDR)
  - Precious metals (GOLD with gram-based pricing)
  - Cryptocurrency (BTC with 8-decimal precision)
- **Unallocated Balance Tracking**: Monitor unallocated physical wallet balance
- **Dedicated Wallet List Page**: Complete wallet management interface with Physical/Logical separation

### 💰 Sophisticated Transaction Management
- **Three Transaction Types**: Income, Expense, and Transfer with comprehensive validation
- **Tag-Based Categorization**: Unified tag system with multiple tags per transaction
- **Smart Auto-completion**: Tag suggestions based on historical data (triggers after 3+ characters)
- **Transfer Validation**: Enhanced validation requiring same wallet type AND currency
- **Real-time Balance Updates**: Automatic wallet balance synchronization
- **Single Wallet Selection**: Each transaction affects exactly one Physical and one Logical wallet

### 🌍 Multi-Currency System
- **Smart Currency Conversion**: 
  - Default rates for consistent conversion across time periods
  - Monthly rates for time-specific conversions
  - Bi-directional rate lookup with intelligent fallback
- **Currency-Specific Formatting**: Tailored precision for different currencies
- **Missing Rate Warnings**: Alerts when conversion rates are needed for accurate calculations
- **Home Screen Aggregation**: Converts all wallet balances to default currency

### 📊 Analytics & Insights
- **Transaction Analytics**: Income/expense tracking excluding initial balances
- **Monthly Financial Metrics**: Expense and net income calculations
- **Balance Integrity Monitoring**: Ensures logical wallet totals match physical wallet totals
- **Real-time Credit/Debit Summaries**: Live calculation updates for filtered results

### 🔐 Security & Data Management
- **Firebase Authentication**: Secure Google Sign-In integration
- **User Data Isolation**: Complete data separation with Firestore subcollections
- **Real-time Synchronization**: Live data updates across all devices
- **Firestore Native Mode**: Optimized for real-time listeners and performance

## 📁 Project Structure

```
app/src/main/java/com/axeven/profiteerapp/
├── data/
│   ├── di/              # Dependency injection modules
│   ├── model/           # Data models (Wallet, Transaction, CurrencyRate, UserPreferences)
│   └── repository/      # Repository pattern implementations
├── ui/
│   ├── home/           # Home screen with balance aggregation
│   ├── wallet/         # Wallet management screens
│   ├── transaction/    # Transaction creation and editing
│   ├── settings/       # Settings and configuration
│   ├── login/          # Authentication screens
│   └── theme/          # Material 3 theming system
├── utils/              # Utility classes (NumberFormatter, WalletValidator)
└── viewmodel/          # ViewModels for business logic and state management
```

## 🎯 Core Domain Model

### Wallet System Architecture
The application implements a sophisticated dual-wallet system:

**Physical Wallets**
- Represent real-world financial accounts
- Independent entities with their own balance and currency
- Direct transaction impacts and balance tracking

**Logical Wallets**  
- Virtual categorizations built on top of physical wallets
- Used for budgeting and allocation (e.g., "Monthly Expenses", "Emergency Fund")
- **Critical Constraint**: Sum of logical wallet balances must equal sum of physical wallet balances

### Transaction Types
1. **Income Transactions**: Add money to wallets (salary, deposits)
2. **Expense Transactions**: Remove money from wallets (purchases, bills)  
3. **Transfer Transactions**: Move money between wallets (same currency required)

### Multi-Currency Architecture
- Each wallet operates in its native currency
- User-configurable default currency for display purposes
- Conversion rates support both permanent and monthly-specific rates
- Smart fallback system: Default → Monthly when rates are missing

## 🚀 Getting Started

### Prerequisites
- Android Studio with Kotlin support
- Firebase account for backend services
- **Target SDK**: 36, **Min SDK**: 24
- **Java Compatibility**: Java 11
- Git for version control

### Firebase Setup Requirements

**Critical Setup Steps**:
1. Create Firebase project in **Native Mode** (not Datastore Mode)
2. Enable Authentication with Google Sign-in provider
3. Create Firestore database in Native Mode with appropriate security rules
4. Configure SHA-1 fingerprints for Google Sign-in in Firebase Console
5. Download `google-services.json` and place in `app/` directory
6. Configure Web Client ID in `AuthRepository.kt` if needed

**Security Rules**: Ensure Firestore security rules enforce user data isolation:
```javascript
allow read, write: if request.auth != null && request.auth.uid == userId;
```

### Installation Steps
```bash
# Clone the repository
git clone <repository-url>
cd Profiteer

# Add your Firebase configuration
# Place google-services.json in app/ directory

# Build and run
./gradlew assembleDebug
./gradlew installDebug

# Optional: Run tests to ensure everything is working
./gradlew test
./gradlew lint
```

### Development Setup
- Uses Gradle Version Catalogs (`gradle/libs.versions.toml`) for dependency management
- ProGuard rules configured for Google Play Services compatibility
- Hilt dependency injection setup in `ProfiteerApplication.kt`

## 🛠️ Build Commands

### Development
- `./gradlew build` - Build the project
- `./gradlew assembleDebug` - Build debug APK
- `./gradlew assembleRelease` - Build release APK
- `./gradlew installDebug` - Install debug build on connected device

### Testing & Quality
- `./gradlew test` - Run unit tests
- `./gradlew connectedAndroidTest` - Run instrumented tests
- `./gradlew testDebugUnitTest` - Run debug unit tests specifically
- `./gradlew lint` - Run Android lint checks
- `./gradlew lintDebug` - Run lint on debug build variant

## 📚 Documentation

Comprehensive documentation is available in the `docs/` directory:

- **[Architecture](docs/architecture.md)** - Detailed system architecture and design decisions
- **[Database Schema](docs/database-schema.md)** - Complete Firestore schema documentation
- **[Feature Requirements](docs/feature-requirements.md)** - Detailed feature specifications
- **[Implementation Status](docs/implementation-status.md)** - Current development status

### Concept Documentation
- **[Wallets](docs/concepts/wallets.md)** - Wallet system design and implementation
- **[Transactions](docs/concepts/transactions.md)** - Transaction management and validation
- **[Currencies](docs/concepts/currencies.md)** - Multi-currency support and conversion

### Page Documentation  
- **[Homepage](docs/pages/homepage.md)** - Home screen functionality
- **[Wallet List](docs/pages/wallet_list_page.md)** - Wallet management interface
- **[Transaction Pages](docs/pages/transaction_list_page.md)** - Transaction management screens
- **[Settings](docs/pages/settings_page.md)** - Configuration and preferences

## 🔧 Recent Improvements

### Transaction System Overhaul
- **Tag Unification**: Merged category and tag concepts into unified tagging system
- **Enhanced Validation**: Real-time validation with clear error messages
- **Smart Auto-completion**: Historical tag-based suggestions
- **Transfer Validation**: Same wallet type AND currency requirements

### Wallet Management Enhancements
- **Dedicated Wallet List Page**: Complete interface with Physical/Logical separation
- **Unallocated Balance Tracking**: Shows unallocated Physical wallet balance
- **Enhanced Navigation**: Direct navigation from home page to wallet list

### UI/UX Improvements
- **Consistent Tag Display**: Tags properly displayed throughout the application
- **Separate Wallet Type Selection**: Dedicated UI sections for different wallet types
- **Material 3 Integration**: Modern design system implementation

## 🧪 Testing Strategy

- **Unit Tests**: ViewModels and business logic (`src/test/`)
- **Integration Tests**: Repository layer and Firebase data operations
- **Instrumented Tests**: Android-specific functionality (`src/androidTest/`)
- **UI Tests**: Compose testing utilities (`androidx.compose.ui.test`) for user interface
- **Continuous Integration**: Automated testing on every build to ensure stability

### Testing Guidelines
- All major features have corresponding unit tests
- Business rules and edge cases are thoroughly tested
- Firebase integration and real-time data synchronization are validated
- Currency conversion logic and balance integrity are verified

## 📄 License

This project is a personal finance management application. Please refer to the license file for usage terms.

## 🤝 Contributing

Contributions are welcome! Please read our contributing guidelines and ensure all tests pass before submitting pull requests.

---

**Built with ❤️ using modern Android development practices**