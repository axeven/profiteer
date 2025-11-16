# Architecture Overview

**Status**: ✅ Implemented
**Last Updated**: 2025-11-16
**Type**: Architecture Specification

---

This document provides a comprehensive overview of the software architecture for Profiteer, a sophisticated personal finance management Android application built with modern development practices.

## System Overview

Profiteer is designed as a comprehensive personal finance management solution that implements a sophisticated dual-wallet system with full multi-currency support. The application follows modern Android development principles with a focus on maintainability, testability, and user experience.

### Technology Stack & Core Technologies
- **Frontend**: Android (Kotlin) with Jetpack Compose for modern declarative UI
- **Backend**: Firebase Authentication + Firestore Database (Native Mode)
- **Architecture**: MVVM (Model-View-ViewModel) with Repository Pattern
- **UI Framework**: Material 3 Design System
- **Dependency Injection**: Hilt for clean architecture
- **Reactive Programming**: Kotlin Coroutines & Flow for asynchronous operations
- **State Management**: StateFlow and Compose State for reactive UI updates
- **Build System**: Android Gradle with Version Catalogs
- **Target SDK**: 36, Min SDK: 24
- **Java Compatibility**: Java 11

## Core Domain Model

### Advanced Wallet System Architecture
Profiteer implements a sophisticated dual-wallet system that separates real-world financial accounts from virtual budget allocations:

**Physical Wallets**
- Represent tangible financial accounts (bank accounts, cash, cryptocurrency wallets)
- Each operates in its native currency with independent balance tracking
- Support multiple physical forms via `PhysicalForm` enum:
  - `FIAT_CURRENCY`: Traditional currencies (USD, EUR, IDR)
  - `PRECIOUS_METAL`: Gold with gram-based pricing
  - `CRYPTOCURRENCY`: Bitcoin with 8-decimal precision
- Direct transaction impacts with real-time balance updates
- Serve as the foundation for all financial operations

**Logical Wallets**
- Virtual categorizations for budgeting and allocation purposes
- Examples: "Monthly Expenses", "Emergency Fund", "Vacation Savings"
- Reference physical wallets through the `affectedWalletIds` system
- **Business Rule**: Total logical wallet balances must equal total physical wallet balances
- Enable sophisticated budgeting without affecting underlying physical accounts
- Support unallocated balance tracking for better financial oversight

### Transaction System Architecture
Profiteer supports three distinct transaction types with comprehensive validation and business logic:

1. **Income Transactions (`TransactionType.INCOME`)**
   - Add money to wallets (salary, deposits, gifts)
   - Affects both one Physical and one Logical wallet simultaneously
   - Increases wallet balances and updates transaction-based analytics
   - Required fields: target wallets, positive amount, optional tags

2. **Expense Transactions (`TransactionType.EXPENSE`)**
   - Remove money from wallets (purchases, bills, withdrawals)
   - Affects both one Physical and one Logical wallet simultaneously
   - Decreases wallet balances and tracks spending patterns
   - Required fields: source wallets, positive amount, optional tags

3. **Transfer Transactions (`TransactionType.TRANSFER`)**
   - Move money between wallets of the same type and currency
   - Implements zero-sum accounting (source decreases, destination increases)
   - **Critical Validation**: Source and destination must have same currency AND wallet type
   - Required fields: source wallet, destination wallet, positive amount
   - Optional: transfer description and categorization tags

### Multi-Currency Architecture
Profiteer implements a sophisticated multi-currency system supporting diverse financial instruments:

**Supported Currency Types**
- **Standard Currencies**: USD, EUR, GBP, JPY, CAD, AUD, IDR
- **Precious Metals**: GOLD (gram-based pricing with 3-decimal precision)
- **Cryptocurrency**: BTC (8-decimal precision for satoshi-level accuracy)

**Currency Conversion System**
- **Default Currency**: User-configurable base currency for aggregated displays
- **Native Currency Operations**: Each wallet maintains its original currency
- **Dual Rate System**:
  - **Default Rates**: Permanent conversion rates for consistent long-term calculations
  - **Monthly Rates**: Time-specific rates for accurate historical conversions
- **Smart Rate Resolution**: Monthly rates take precedence over default rates when available
- **Bi-directional Conversion**: Automatic inverse rate calculation when direct rates unavailable
- **Missing Rate Warnings**: User alerts when conversion rates needed for accurate balance aggregation

**Display Flexibility**
- Home screen aggregation converts all balances to default currency
- Individual wallet views support native currency or converted currency display
- Transaction displays preserve original currency while showing converted amounts
- Currency-specific formatting with appropriate decimal precision

### Advanced Transaction Categorization
Profiteer implements a unified tagging system that replaced the traditional category approach:

**Tag-Based System**
- **Multiple Tags per Transaction**: Each transaction can have multiple descriptive tags
- **Unified Category/Tag Model**: Merged traditional categories and tags into a single flexible system
- **Smart Auto-completion**: Historical tag suggestions appear after typing 3+ characters
- **Default Behavior**: Transactions without explicit tags receive "Untagged" designation
- **Tag Persistence**: System remembers all previously used tags for future auto-completion

**Analytics Integration**
- Tags enable sophisticated filtering and grouping for financial analytics
- Support complex queries combining time range, wallet, and tag filters
- Monthly expense categorization based on tag frequency and amounts
- Transaction search and filtering across all tag dimensions

**Backward Compatibility**
- Legacy transactions with `category` field maintain compatibility
- Gradual migration from categories to tags without data loss
- Existing category data preserved while enabling new tag functionality

## Application Architecture

### MVVM Pattern Implementation
```
View (Jetpack Compose UI)
    ↓
 ViewModel (Business Logic + State Management)
    ↓
Repository (Data Abstraction Layer)
    ↓
Data Sources (Firebase Firestore)
```

### Detailed Package Architecture
```
com.axeven.profiteerapp/
├── data/
│   ├── di/              # Hilt dependency injection modules
│   │   └── AppModule.kt # Application-wide dependencies
│   ├── model/           # Core data models with Firestore annotations
│   │   ├── Wallet.kt    # Physical/Logical wallet model with PhysicalForm support
│   │   ├── Transaction.kt # Enhanced transaction model with tag support
│   │   ├── CurrencyRate.kt # Currency conversion rates (default/monthly)
│   │   ├── UserPreferences.kt # User configuration and settings
│   │   └── PhysicalForm.kt # Enum for wallet physical forms
│   └── repository/      # Repository pattern implementations
│       ├── AuthRepository.kt # Firebase Authentication integration
│       ├── WalletRepository.kt # Wallet CRUD with real-time updates
│       ├── TransactionRepository.kt # Transaction management with validation
│       ├── CurrencyRateRepository.kt # Rate management and conversion logic
│       └── UserPreferencesRepository.kt # User settings persistence
├── ui/                  # Jetpack Compose UI components
│   ├── home/           # Home screen with balance aggregation
│   │   └── HomeScreen.kt # Dashboard with multi-currency balance display
│   ├── wallet/         # Wallet management interfaces
│   │   ├── WalletListScreen.kt # Physical/Logical wallet separation
│   │   ├── WalletDetailScreen.kt # Individual wallet analysis
│   │   ├── WalletGrouping.kt # Wallet organization logic
│   │   ├── MonthSelector.kt # Time range selection component
│   │   └── PhysicalFormSelector.kt # Wallet type selection UI
│   ├── transaction/    # Transaction creation and editing
│   │   ├── CreateTransactionScreen.kt # New transaction interface
│   │   └── EditTransactionScreen.kt # Transaction modification
│   ├── report/         # Comprehensive reports and analytics
│   │   └── ReportScreenSimple.kt # Unified analytics dashboard
│   ├── settings/       # Configuration and preferences
│   │   └── SettingsScreen.kt # Currency rates and wallet management
│   ├── login/          # Authentication screens
│   │   └── LoginScreen.kt # Google Sign-in integration
│   └── theme/          # Material 3 theming system
│       ├── Color.kt    # Application color palette
│       ├── Theme.kt    # Theme configuration and dark mode
│       └── Type.kt     # Typography definitions
├── utils/              # Utility classes and helper functions
│   ├── NumberFormatter.kt # Multi-currency formatting with precision control
│   └── WalletValidator.kt # Business rule validation
├── viewmodel/          # MVVM ViewModels with reactive state management
│   ├── AuthViewModel.kt # Authentication state management
│   ├── HomeViewModel.kt # Dashboard data aggregation
│   ├── WalletListViewModel.kt # Wallet list state and operations
│   ├── WalletDetailViewModel.kt # Individual wallet analysis
│   ├── TransactionViewModel.kt # Transaction CRUD operations
│   ├── ReportViewModel.kt # Reports and analytics data management
│   └── SettingsViewModel.kt # Configuration management
├── MainActivity.kt     # Single activity hosting all Compose screens
└── ProfiteerApplication.kt # Hilt application class
```

### Data Layer Architecture

**Firebase Integration**
- **Authentication**: Firebase Auth for user management
- **Database**: Firestore with user-isolated subcollections
- **Security**: User-scoped data access with Firestore security rules

### Firebase Data Architecture

**Firestore Database Structure (Native Mode)**
```
users/{userId}/                    # User document (profile data)
├── wallets/{walletId}            # Physical and logical wallets
├── transactions/{transactionId}   # All transaction records
├── currency_rates/{rateId}       # User-specific conversion rates
└── user_preferences/{prefId}     # User configuration settings
```

**Real-time Data Synchronization**
- **Firestore Listeners**: Live updates across all user devices
- **Document ID Mapping**: Manual mapping ensures proper model instantiation
- **Optimized Queries**: Composite indexes for efficient filtering and pagination
- **Error Handling**: Comprehensive exception handling for network and authentication issues

**Security Architecture**
- **User Data Isolation**: Firestore security rules prevent cross-user data access
- **Authentication Integration**: Firebase Auth UID serves as user document identifier
- **Subcollection Security**: Nested security rules ensure proper data access control

## User Interface Architecture

### Navigation Structure & Screen Flow
```
Main Navigation (Bottom Navigation)
├── Homepage (Dashboard)
│   ├── Physical Wallet Balance Summary
│   ├── Logical Wallet Balance Summary with Discrepancy Detection
│   ├── Monthly Financial Metrics (Expense, Net Income)
│   ├── Recent Transaction List (Top 10)
│   └── Quick Action Buttons
├── Wallet Management
│   ├── Wallet List Screen (Physical/Logical Separation)
│   ├── Wallet Detail Screen (Individual Analysis)
│   ├── Create/Edit Wallet Screens
│   └── Unallocated Balance Tracking
├── Transaction Management
│   ├── Create Transaction Screen (Income/Expense/Transfer)
│   ├── Edit Transaction Screen (Tag Management)
│   ├── Transaction List Screen (Advanced Filtering)
│   └── Transaction History with Pagination
├── Reports & Analytics
│   ├── Portfolio Composition Analysis (by Physical Form)
│   ├── Physical/Logical Wallet Balance Analytics
│   ├── Transaction Analytics by Tags (Income/Expense)
│   └── Visual Charts and Data Insights
└── Settings & Configuration
    ├── Currency Rate Management (Default/Monthly)
    ├── Wallet Type Configuration
    ├── User Preferences
    └── Account Management

Authentication Flow
├── Login Screen (Google Sign-in)
└── Initial Setup (Default Currency Selection)
```

### Key Screen Components & Features

**Homepage Dashboard (HomeScreen.kt)**
- **Multi-Currency Balance Aggregation**: Converts all wallet balances to default currency
- **Physical/Logical Wallet Separation**: Distinct sections for different wallet types
- **Balance Discrepancy Detection**: Alerts when logical wallet totals don't match physical totals
- **Monthly Analytics**: Net income, total expenses, and spending trends
- **Recent Transaction Feed**: Last 10 transactions with tag display
- **Quick Actions**: Direct navigation to create transaction, manage wallets, and view reports
- **Missing Rate Warnings**: Alerts when currency conversion rates are needed

**Wallet List Screen (WalletListScreen.kt)**
- **Dual-Section Layout**: Physical wallets above, logical wallets below
- **Unallocated Balance Tracking**: Shows difference between physical and logical totals
- **Currency Display Toggle**: Native currency vs. default currency conversion
- **Wallet Creation**: Separate workflows for physical and logical wallet creation
- **Real-time Balance Updates**: Live synchronization with transaction changes

**Wallet Detail Screen (WalletDetailScreen.kt)**
- **Transaction History**: Filtered view of wallet-specific transactions
- **Monthly Analysis**: Debit/credit breakdown with trend visualization
- **Currency Conversion**: Toggle between native and default currency display
- **Balance Components**: Separation of initial balance and transaction-based changes
- **Transaction Impact**: Visual representation of how transactions affect balance

**Transaction Management (CreateTransactionScreen.kt, EditTransactionScreen.kt)**
- **Three Transaction Types**: Income, Expense, Transfer with type-specific validation
- **Dual Wallet Selection**: Both Physical and Logical wallet selection for Income/Expense
- **Smart Tag System**: Auto-completion based on historical tags (3+ character trigger)
- **Transfer Validation**: Same currency and wallet type enforcement
- **Date Selection**: Custom transaction date with DatePicker integration
- **Real-time Validation**: Immediate feedback on wallet selection and amount validation

**Reports Screen (ReportScreenSimple.kt)**
- **Portfolio Composition Analysis**: Visual breakdown of assets by physical form (Cash, Bank, Gold, Bitcoin)
- **Wallet Balance Analytics**: Separate analysis for physical and logical wallet balances
- **Transaction Analytics by Tags**: Income and expense tracking grouped by transaction tags
- **Interactive Chart Selection**: Toggle between different chart types and data views
- **Visual Data Insights**: Pie charts and legends with comprehensive data breakdown
- **Real-time Data Updates**: Live synchronization with transaction and wallet changes

**Settings Screen (SettingsScreen.kt)**
- **Currency Rate Management**: Default and monthly rate configuration
- **Wallet Type Administration**: Create, edit, delete wallets with validation
- **Default Currency Selection**: Base currency for aggregated displays
- **Rate Warning System**: Missing conversion rate detection and alerts
- **User Preferences**: Application configuration and customization options

## Business Rules & Data Consistency

### Balance Integrity System
- **Critical Constraint**: Sum of all logical wallet balances must equal sum of all physical wallet balances
- **Real-time Validation**: Balance integrity checked on every transaction operation
- **Dashboard Alerts**: Home screen displays warnings when discrepancies are detected
- **Transaction Impact**: All transactions must affect both physical and logical wallets simultaneously
- **Unallocated Balance Tracking**: System tracks and displays unallocated physical wallet balance

### Enhanced Transaction Validation
- **Amount Validation**: All transaction amounts must be positive numbers (> 0)
- **Transfer Constraints**:
  - Source and destination wallets must have identical currency
  - Source and destination must be the same wallet type (Physical or Logical)
  - Cannot transfer from a wallet to itself
- **Wallet Selection Validation**:
  - Income/Expense transactions require exactly one Physical and one Logical wallet
  - Transfer transactions require source and destination of same type
- **Tag Validation**: Multiple tags supported, "Untagged" default for empty tag lists
- **Date Validation**: Transaction dates cannot be in the future

### Multi-Currency Conversion Rules
- **Rate Hierarchy**: Monthly rates override default rates when available
- **Bi-directional Conversion**: System calculates inverse rates when direct rates unavailable
- **Non-destructive Display**: Original transaction currencies always preserved
- **Rate Fallback System**: Default → Monthly → Warning when no rates available
- **Precision Control**: Currency-specific decimal precision (BTC: 8, GOLD: 3, Fiat: 2)

### Data Validation & Integrity
- **User Data Isolation**: Strict enforcement through Firestore security rules
- **Document Consistency**: Firestore transactions ensure atomic operations
- **Real-time Synchronization**: Balance updates propagate immediately across all devices
- **Audit Trail**: All transactions maintain creation and modification timestamps

## Performance Architecture & Optimizations

### Database Performance Strategy
- **Composite Indexes**: Optimized for complex filtering scenarios
  - Transaction queries by date, wallet, and tag combinations
  - Currency rate lookups with time-based precedence
  - User-scoped data access patterns
- **Pagination Implementation**: Efficient handling of large transaction datasets
  - Default 50 transactions per page with configurable limits
  - Cursor-based pagination for consistent results
  - Optimized for monthly transaction views (current month default)
- **Query Optimization**:
  - Minimized compound queries to avoid complex composite indexes
  - Strategic use of Firestore's native filtering capabilities
  - Real-time listeners only for actively viewed data

### Key Performance Patterns
- **Dashboard Queries**:
  - Wallet balance aggregation with currency conversion
  - Monthly analytics calculation with transaction filtering
  - Recent transaction feed with limited result sets
- **Transaction Management**:
  - Time-range filtered queries with efficient ordering
  - Wallet-specific transaction retrieval with pagination
  - Real-time balance calculation updates
- **Currency Conversion**:
  - Cached rate lookups with time-based precedence
  - Bi-directional rate calculation with fallback logic
  - Optimized rate storage for frequent conversion scenarios

### Mobile Performance Considerations
- **State Management**: StateFlow for reactive UI updates without unnecessary recomposition
- **Compose Optimization**: Strategic use of remember and key parameters
- **Background Processing**: Repository pattern enables efficient data layer operations
- **Network Efficiency**: Firestore offline support with optimistic updates
- **Memory Management**: Proper lifecycle management for ViewModels and repositories

## Comprehensive Security Architecture

### Data Protection & Privacy
- **User Data Isolation**: Complete separation through Firestore subcollections
  - Each user's data stored under `users/{userId}/` path
  - Firestore security rules prevent cross-user data access
  - Authentication UID serves as primary isolation mechanism
- **Firebase Authentication Integration**:
  - Google Sign-in with proper OAuth 2.0 configuration
  - Web Client ID validation for secure authentication flow
  - SHA-1 fingerprint requirements for production deployment
  - Session management with automatic token refresh

### Input Validation & Data Integrity
- **Multi-Layer Validation**:
  - Client-side validation for immediate user feedback
  - Repository-level validation for business rule enforcement
  - Firestore security rules as final validation layer
- **Transaction Security**:
  - Positive-only transaction amounts (> 0)
  - Currency code validation against supported currency list
  - Wallet ownership verification before transaction creation
  - Transfer transaction currency and type matching

### Authentication Security
- **Google Play Services Integration**: Proper ProGuard rules for release builds
- **Exception Handling**: Comprehensive error handling for authentication failures
- **Security Configuration**:
  - google-services.json for Firebase project configuration
  - OAuth 2.0 client configuration for Google Sign-in
  - Production-ready authentication flow with error recovery

### Data Transmission Security
- **HTTPS Enforcement**: All Firebase communication over encrypted connections
- **Firestore Security Rules**: Server-side validation preventing unauthorized access
- **Client-side Error Handling**: Secure error messages without sensitive data exposure
- **Session Security**: Automatic token refresh and secure session management

## Related Specifications

- [Domain Models](../domain/) - Detailed wallet, transaction, currency specifications
- [Technical Specifications](../technical/) - Firebase, state management, logging
- [Screen Specifications](../screens/) - UI/UX specifications for each screen
- [Feature Specifications](../features/) - Individual feature requirements

---

**Implementation Guidelines**: See [docs/guides/](../../guides/) for implementation patterns
**Last Reviewed**: 2025-11-16
