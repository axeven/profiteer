# Profiteer Database Schema Documentation

This document provides a comprehensive overview of the Firestore database schema for the Profiteer personal finance application. The application uses Firebase Firestore in Native Mode for optimal real-time synchronization and performance.

## Database Architecture Overview

Profiteer implements a user-centric data isolation pattern where all user-specific data is stored as subcollections under individual user documents. This ensures complete data separation and security while enabling efficient querying and real-time updates.

### Core Collections Structure
The database uses the following main collections and subcollections:
- **`users/{userId}`** - User profiles and authentication data
- **`users/{userId}/wallets`** - Physical and logical wallet management
- **`users/{userId}/transactions`** - All financial transaction records
- **`users/{userId}/currency_rates`** - User-specific currency conversion rates
- **`users/{userId}/user_preferences`** - Application configuration and user settings

## Collection Schemas

### users/{userId} (Root User Document)
**Purpose**: Stores basic user profile information and authentication data
**Path**: `users/{userId}` where `{userId}` is Firebase Auth UID

```json
{
  "userId": "string",           // Firebase Auth UID (auto-generated document ID)
  "email": "string",            // User's email from Google Sign-in
  "displayName": "string",      // User's display name from Google account
  "photoURL": "string",         // Profile photo URL from Google account
  "defaultCurrency": "string",  // User's preferred currency code (e.g., "USD", "IDR")
  "createdAt": "timestamp",      // Account creation timestamp
  "updatedAt": "timestamp",      // Last profile update timestamp
  "lastLoginAt": "timestamp"     // Last successful login timestamp
}
```

**Business Rules**:
- Document ID must match Firebase Auth UID for security isolation
- Email field is automatically populated from Google Sign-in
- Default currency affects home screen balance aggregation display
- Profile updates trigger `updatedAt` timestamp refresh

**Security Considerations**:
- Only the authenticated user can read/write their own user document
- Email verification status inherited from Google Sign-in
- Photo URL and display name automatically synced from Google account

### currency_rates (User Subcollection)
**Purpose**: Manages user-specific currency conversion rates for multi-currency support
**Path**: `users/{userId}/currency_rates/{rateId}`

```json
{
  "rateId": "string",           // Auto-generated Firestore document ID
  "fromCurrency": "string",     // Source currency code (USD, EUR, BTC, GOLD, etc.)
  "toCurrency": "string",       // Target currency code
  "rate": "number",             // Conversion multiplier (from → to)
  "rateType": "string",         // "default" or "monthly" for rate precedence
  "monthYear": "string",        // For monthly rates: "YYYY-MM" format (null for default rates)
  "userId": "string",           // Reference to parent user document (for validation)
  "createdAt": "timestamp",     // Rate creation timestamp
  "updatedAt": "timestamp",     // Last rate modification timestamp
  "isActive": "boolean"         // Whether rate is currently active
}
```

**Supported Currency Types**:
- **Fiat Currencies**: USD, EUR, GBP, JPY, CAD, AUD, IDR
- **Precious Metals**: GOLD (gram-based pricing)
- **Cryptocurrency**: BTC (satoshi-level precision)

**Rate Resolution Logic**:
1. **Monthly rates** take precedence over default rates when available
2. **Bi-directional lookup**: System calculates inverse rates when direct rates unavailable
3. **Fallback system**: Default → Monthly → Warning when no rates exist

**Business Rules**:
- Rate must be positive number (> 0)
- Monthly rates require valid YYYY-MM format in `monthYear` field
- Default rates have `monthYear` set to null
- System prevents duplicate rates for same currency pair and time period
- Inactive rates (`isActive: false`) are excluded from conversion calculations

### wallets (User Subcollection)
**Purpose**: Manages both Physical and Logical wallets for comprehensive financial tracking
**Path**: `users/{userId}/wallets/{walletId}`

```json
{
  "walletId": "string",         // Auto-generated Firestore document ID
  "name": "string",             // Wallet display name (must be unique per user)
  "walletType": "string",       // "Physical" or "Logical" for wallet classification
  "physicalForm": "string",     // Physical form: "FIAT_CURRENCY", "PRECIOUS_METAL", "CRYPTOCURRENCY"
  "currency": "string",         // Native currency code (USD, EUR, BTC, GOLD, etc.)
  "balance": "number",          // Current total balance (initial + transaction changes)
  "initialBalance": "number",   // Starting balance (excluded from analytics calculations)
  "userId": "string",           // Reference to parent user document
  "createdAt": "timestamp",     // Wallet creation timestamp
  "updatedAt": "timestamp"      // Last balance/details modification timestamp
}
```

**Wallet Type Classifications**:

**Physical Wallets**:
- Represent real-world financial accounts (bank accounts, cash, crypto wallets)
- Each operates independently with its own currency and balance
- Form the foundation for all financial operations
- Support three physical forms via `PhysicalForm` enum

**Logical Wallets**:
- Virtual categorizations for budgeting and allocation
- Examples: "Monthly Expenses", "Emergency Fund", "Vacation Savings"
- Reference physical wallets through transaction relationships
- Enable sophisticated budgeting without affecting physical account balances

**Physical Form Categories**:
- **`FIAT_CURRENCY`**: Traditional government currencies (USD, EUR, IDR)
- **`PRECIOUS_METAL`**: Gold with gram-based pricing (3-decimal precision)
- **`CRYPTOCURRENCY`**: Bitcoin with satoshi-level precision (8 decimals)

**Balance Calculations**:
- **`balance`**: Total current balance including initial setup amount
- **`transactionBalance`** (computed): `balance - initialBalance` for analytics
- **Critical Business Rule**: Sum of all logical wallet balances must equal sum of all physical wallet balances

**Validation Rules**:
- Wallet names must be unique within a user's account
- Physical form must match supported currency type
- Initial balance can be positive, negative, or zero
- Currency code must be supported by the application
- Balance updates trigger automatic `updatedAt` timestamp refresh

### Transaction Tag System
**Purpose**: The application uses a unified tagging approach that replaces traditional transaction categories

**Tag Implementation**: Instead of a separate `transaction_tags` collection, Profiteer implements tags directly within transaction documents using a `tags` array field. This approach provides:

- **Simplified Data Model**: Eliminates need for separate tag management collection
- **Flexible Tagging**: Multiple tags per transaction without complex relationships
- **Historical Auto-completion**: System tracks all previously used tags across transactions
- **Backward Compatibility**: Legacy `category` field preserved while enabling new tag functionality

**Tag Features**:
- **Multiple Tags**: Each transaction can have an array of descriptive tags
- **Auto-completion**: Tag suggestions appear after typing 3+ characters
- **Default Behavior**: Transactions without tags receive "Untagged" designation
- **Tag Persistence**: All historical tags available for future use
- **Case Sensitivity**: Tags are case-sensitive for precise categorization

**Tag Usage Examples**:
```json
"tags": ["Food", "Restaurant", "Business Lunch"]
"tags": ["Transportation", "Fuel", "Monthly"]
"tags": ["Healthcare", "Insurance", "Quarterly Premium"]
"tags": [] // Results in "Untagged" display
```

### transactions (User Subcollection)
**Purpose**: Records all financial transactions with comprehensive validation and multi-wallet support
**Path**: `users/{userId}/transactions/{transactionId}`

```json
{
  "transactionId": "string",     // Auto-generated Firestore document ID
  "title": "string",            // Transaction description/memo
  "amount": "number",           // Transaction amount (always positive, type determines impact)
  "category": "string",         // Legacy category field ("Untagged" default for backward compatibility)
  "type": "string",             // Transaction type: "INCOME", "EXPENSE", "TRANSFER"
  "walletId": "string",         // Primary wallet ID (backward compatibility field)
  "affectedWalletIds": [        // Modern multi-wallet system (Physical + Logical for Income/Expense)
    "string"                     // Array of wallet IDs affected by this transaction
  ],
  "sourceWalletId": "string",   // For TRANSFER transactions: source wallet
  "destinationWalletId": "string", // For TRANSFER transactions: destination wallet
  "tags": [                      // Modern tagging system (replaces categories)
    "string"                     // Array of descriptive tags for transaction
  ],
  "transactionDate": "timestamp", // User-specified transaction date (can differ from createdAt)
  "userId": "string",           // Reference to parent user document
  "createdAt": "timestamp",     // System timestamp when transaction was created
  "updatedAt": "timestamp"      // Last modification timestamp
}
```

**Transaction Type Classifications**:

**1. Income Transactions (`TransactionType.INCOME`)**
- Add money to both Physical and Logical wallets simultaneously
- Increases wallet balances and updates analytics
- Required: `affectedWalletIds` with exactly one Physical and one Logical wallet
- Amount is always positive, system handles balance increase

**2. Expense Transactions (`TransactionType.EXPENSE`)**
- Remove money from both Physical and Logical wallets simultaneously
- Decreases wallet balances and tracks spending
- Required: `affectedWalletIds` with exactly one Physical and one Logical wallet
- Amount is always positive, system handles balance decrease

**3. Transfer Transactions (`TransactionType.TRANSFER`)**
- Move money between wallets of the same type and currency
- Zero-sum operation: source decreases, destination increases by same amount
- Required: `sourceWalletId` and `destinationWalletId`
- Validation: Both wallets must have same currency AND same type (Physical or Logical)

**Field Evolution & Compatibility**:
- **`category`**: Legacy field maintained for backward compatibility (default: "Untagged")
- **`tags`**: Modern replacement for categories, supports multiple tags per transaction
- **`walletId`**: Legacy single wallet reference, still used for backward compatibility
- **`affectedWalletIds`**: Modern multi-wallet system for Income/Expense transactions
- **`transactionDate`**: User-specified date (can be different from `createdAt` for backdated entries)

**Validation Rules**:
- Amount must be positive number (> 0)
- Transaction date cannot be in the future
- Income/Expense: `affectedWalletIds` must contain exactly 2 wallets (1 Physical, 1 Logical)
- Transfer: Source and destination must have identical currency and wallet type
- Transfer: Cannot transfer from wallet to itself
- Tags array can be empty (results in "Untagged" display)
- Legacy category field defaults to "Untagged" for new transactions

### user_preferences (User Subcollection)
**Purpose**: Stores user-specific application configuration and display preferences
**Path**: `users/{userId}/user_preferences/{prefId}`

```json
{
  "preferencesId": "string",    // Auto-generated Firestore document ID
  "defaultCurrency": "string",  // User's preferred currency for aggregated displays
  "currencyDisplayMode": "string", // "native" or "converted" for wallet displays
  "dateFormat": "string",       // User's preferred date format ("MM/DD/YYYY", "DD/MM/YYYY")
  "numberFormat": "string",     // Number formatting preference ("1,234.56", "1.234,56")
  "theme": "string",            // UI theme preference ("light", "dark", "system")
  "transactionPageSize": "number", // Default number of transactions per page (25, 50, 100)
  "homeScreenLayout": "string", // Home screen layout preference ("compact", "detailed")
  "balanceVisibility": "boolean", // Whether to show/hide balances by default
  "notificationSettings": {
    "balanceAlerts": "boolean",   // Enable low balance notifications
    "monthlyReports": "boolean", // Enable monthly summary emails
    "spendingAlerts": "boolean", // Enable spending threshold notifications
    "emailFrequency": "string"   // "daily", "weekly", "monthly", "never"
  },
  "userId": "string",           // Reference to parent user document
  "createdAt": "timestamp",     // Preferences creation timestamp
  "updatedAt": "timestamp"      // Last preferences modification timestamp
}
```

**Configuration Categories**:

**Display Preferences**:
- **Default Currency**: Affects home screen balance aggregation and conversion displays
- **Currency Display Mode**: Whether to show native currency or converted amounts by default
- **Date/Number Formatting**: Localization support for different regional preferences
- **Theme Settings**: UI appearance customization with system theme detection

**Functional Preferences**:
- **Transaction Page Size**: Controls pagination for transaction lists (performance optimization)
- **Home Screen Layout**: Compact view for quick overview or detailed view for comprehensive data
- **Balance Visibility**: Privacy setting for hiding/showing balance information

**Notification Preferences**:
- **Balance Alerts**: Low balance warnings and threshold notifications
- **Monthly Reports**: Automated financial summary generation and delivery
- **Spending Alerts**: Budget deviation and spending limit notifications
- **Email Frequency**: Configurable notification delivery schedule

**Business Rules**:
- Only one preferences document per user (singleton pattern)
- Default currency must be supported by the application
- Page size limits enforced for performance (minimum 10, maximum 200)
- Theme changes apply immediately without restart
- Notification settings integrate with Firebase Cloud Messaging when implemented

**Default Values** (Applied on user account creation):
- `defaultCurrency`: "USD" (configurable during onboarding)
- `currencyDisplayMode`: "native"
- `dateFormat`: Based on device locale
- `theme`: "system"
- `transactionPageSize`: 50
- `balanceVisibility`: true
- All notification settings: false (opt-in model)

## Data Relationships & Business Logic

### User Data Isolation Architecture
- **Complete Separation**: All user data stored as subcollections under `users/{userId}/`
- **Security Enforcement**: Firestore security rules prevent cross-user data access
- **Authentication Integration**: Firebase Auth UID serves as primary isolation key
- **Scalable Design**: Supports unlimited users without data mixing concerns

### Wallet System Relationships

**Physical Wallet Independence**:
- Each Physical wallet operates as an independent financial account
- Balance tracking includes both initial setup amount and transaction impacts
- Currency operations maintain native currency precision and formatting
- Real-time balance updates through Firestore listeners

**Logical Wallet Architecture**:
- Virtual categorizations for budgeting without affecting physical balances
- Reference Physical wallets through transaction `affectedWalletIds` relationships
- **Critical Business Rule**: Total Logical wallet balances must equal total Physical wallet balances
- Unallocated balance tracking when Physical and Logical totals don't match

**Multi-Wallet Transaction System**:
- Income/Expense transactions affect both Physical and Logical wallets simultaneously
- Transfer transactions operate within single wallet type (Physical OR Logical)
- Real-time balance synchronization across all affected wallets
- Atomic transaction processing ensures data consistency

### Transaction Relationship Matrix

**Income Transactions**:
- **Source**: External (salary, gifts, deposits)
- **Destination**: One Physical + One Logical wallet
- **Balance Impact**: Increases both selected wallets by transaction amount
- **Validation**: Both wallets must belong to authenticated user

**Expense Transactions**:
- **Source**: One Physical + One Logical wallet
- **Destination**: External (purchases, bills, withdrawals)
- **Balance Impact**: Decreases both selected wallets by transaction amount
- **Validation**: Sufficient balance in both wallets (optional overdraft protection)

**Transfer Transactions**:
- **Source**: Single wallet (Physical or Logical)
- **Destination**: Single wallet (same type and currency as source)
- **Balance Impact**: Zero-sum operation (source -amount, destination +amount)
- **Validation**: Same currency, same type, cannot transfer to self

### Currency Conversion Relationships

**Rate Hierarchy System**:
1. **Monthly Rates**: Time-specific rates take highest precedence
2. **Default Rates**: Permanent rates used when monthly rates unavailable
3. **Inverse Calculation**: System computes reverse rates when direct rates missing
4. **Warning System**: User alerts when no conversion rates available

**Multi-Currency Display Logic**:
- **Home Screen Aggregation**: Converts all balances to user's default currency
- **Individual Wallets**: Can display in native currency or converted currency
- **Transaction Records**: Always preserve original currency while showing conversions
- **Historical Accuracy**: Monthly rates ensure accurate historical conversions

**Currency Support Matrix**:
- **Fiat Currencies**: 2-decimal precision (USD: $1,234.56)
- **Precious Metals**: 3-decimal precision (GOLD: 123.456g)
- **Cryptocurrency**: 8-decimal precision (BTC: 1.23456789 BTC)

## Database Indexes & Query Optimization

### Required Composite Indexes
Firestore requires specific composite indexes for efficient querying. The following indexes are essential for Profiteer's performance:

**Transaction Query Indexes**:
```javascript
// Primary transaction listing (by date)
Collection: users/{userId}/transactions
Fields: transactionDate (Descending), createdAt (Descending)
Scope: Collection

// Transaction type filtering
Collection: users/{userId}/transactions  
Fields: type (Ascending), transactionDate (Descending)
Scope: Collection

// Wallet-specific transactions
Collection: users/{userId}/transactions
Fields: walletId (Ascending), transactionDate (Descending)
Scope: Collection

// Multi-wallet transaction queries (affected wallets)
Collection: users/{userId}/transactions
Fields: affectedWalletIds (Array-contains), transactionDate (Descending)
Scope: Collection

// Transfer transaction queries (source wallet)
Collection: users/{userId}/transactions
Fields: sourceWalletId (Ascending), transactionDate (Descending)
Scope: Collection

// Transfer transaction queries (destination wallet)
Collection: users/{userId}/transactions
Fields: destinationWalletId (Ascending), transactionDate (Descending)
Scope: Collection

// Tag-based filtering (array-contains for tags)
Collection: users/{userId}/transactions
Fields: tags (Array-contains), transactionDate (Descending)
Scope: Collection

// Complex filtering: wallet + type + date
Collection: users/{userId}/transactions
Fields: walletId (Ascending), type (Ascending), transactionDate (Descending)
Scope: Collection

// Complex filtering: affected wallets + type + date
Collection: users/{userId}/transactions
Fields: affectedWalletIds (Array-contains), type (Ascending), transactionDate (Descending)
Scope: Collection
```

**Currency Rate Indexes**:
```javascript
// Currency conversion lookup
Collection: users/{userId}/currency_rates
Fields: fromCurrency (Ascending), toCurrency (Ascending)
Scope: Collection

// Time-specific rate lookup
Collection: users/{userId}/currency_rates
Fields: fromCurrency (Ascending), toCurrency (Ascending), monthYear (Ascending)
Scope: Collection

// Rate type filtering
Collection: users/{userId}/currency_rates
Fields: rateType (Ascending), fromCurrency (Ascending), toCurrency (Ascending)
Scope: Collection

// Active rate filtering
Collection: users/{userId}/currency_rates
Fields: isActive (Ascending), fromCurrency (Ascending), toCurrency (Ascending)
Scope: Collection
```

**Wallet Management Indexes**:
```javascript
// Wallet type filtering
Collection: users/{userId}/wallets
Fields: walletType (Ascending), createdAt (Descending)
Scope: Collection

// Physical form filtering
Collection: users/{userId}/wallets
Fields: physicalForm (Ascending), walletType (Ascending)
Scope: Collection

// Currency-based wallet filtering
Collection: users/{userId}/wallets
Fields: currency (Ascending), walletType (Ascending)
Scope: Collection
```

### Index Optimization Strategy

**Performance Considerations**:
- **Single-field Indexes**: Automatically created by Firestore for individual field queries
- **Composite Indexes**: Required for multi-field queries and complex filtering
- **Array-contains Indexes**: Essential for tag-based and multi-wallet filtering
- **Descending Date Ordering**: Optimizes recent transaction retrieval

**Index Management Best Practices**:
- **Minimal Index Set**: Only create indexes actually used by application queries
- **Query Planning**: Design queries to leverage existing indexes efficiently
- **Performance Monitoring**: Track query performance and index usage patterns
- **Maintenance**: Remove unused indexes to reduce storage costs and write overhead

## Security Rules & Data Validation

### Firestore Security Rules Implementation

**User Data Isolation**:
```javascript
// Ensure users can only access their own data
allow read, write: if request.auth != null && request.auth.uid == userId;

// Root user document security
match /users/{userId} {
  allow read, write: if request.auth != null && request.auth.uid == userId;
  
  // Subcollection security inheritance
  match /wallets/{walletId} {
    allow read, write: if request.auth != null && request.auth.uid == userId;
  }
  
  match /transactions/{transactionId} {
    allow read, write: if request.auth != null && request.auth.uid == userId;
  }
  
  match /currency_rates/{rateId} {
    allow read, write: if request.auth != null && request.auth.uid == userId;
  }
  
  match /user_preferences/{prefId} {
    allow read, write: if request.auth != null && request.auth.uid == userId;
  }
}
```

### Data Validation Rules

**Transaction Validation**:
- **Amount Validation**: All transaction amounts must be positive numbers (> 0)
- **Date Validation**: Transaction dates cannot be in the future
- **Wallet Ownership**: All referenced wallets must belong to the authenticated user
- **Transfer Constraints**: Source and destination wallets must have same currency and type
- **Balance Consistency**: System validates wallet balance integrity on transaction creation

**Wallet Validation**:
- **Unique Names**: Wallet names must be unique within user's account
- **Currency Support**: Currency codes must be supported by the application
- **Physical Form Matching**: Physical form must correspond to currency type
- **Balance Integrity**: Logical wallet sum must equal Physical wallet sum

**Currency Rate Validation**:
- **Rate Values**: Conversion rates must be positive numbers
- **Date Format**: Monthly rates must use valid YYYY-MM format
- **Currency Codes**: Both source and target currencies must be supported
- **Duplicate Prevention**: No duplicate rates for same currency pair and time period

**User Preferences Validation**:
- **Default Currency**: Must be supported by the application
- **Page Size Limits**: Transaction page size between 10 and 200
- **Theme Options**: Theme must be one of supported values (light/dark/system)
- **Notification Settings**: Boolean validation for notification preferences

### Authentication & Authorization

**Firebase Authentication Integration**:
- **Google Sign-in**: Primary authentication method with OAuth 2.0
- **Session Management**: Automatic token refresh and session validation
- **User Profile Sync**: Display name and email automatically updated from Google account
- **Security Headers**: Proper HTTPS enforcement for all Firebase communication

**Authorization Levels**:
- **User Level**: Full CRUD access to own subcollections only
- **Document Level**: Individual document ownership verification
- **Field Level**: Sensitive fields protected with additional validation
- **Operation Level**: Different permissions for read vs. write operations

**Security Best Practices**:
- **Client-side Validation**: Immediate user feedback with comprehensive form validation
- **Server-side Validation**: Firestore security rules as final validation layer
- **Error Handling**: Secure error messages that don't expose sensitive information
- **Audit Trail**: All operations logged with timestamps and user identification

## Optimized Query Patterns

### Transaction List Page Queries
Efficient queries designed for the transaction list page with advanced filtering and pagination:

**1. Default Monthly Transaction View**:
```javascript
// Current month transactions (default view)
users/{userId}/transactions
  .where('transactionDate', '>=', monthStart)
  .where('transactionDate', '<=', monthEnd)
  .orderBy('transactionDate', 'desc')
  .orderBy('createdAt', 'desc')  // Secondary sort for same-date transactions
  .limit(pageSize);
```

**2. Time Range Filtering**:
```javascript
// Custom date range transactions
users/{userId}/transactions
  .where('transactionDate', '>=', startDate)
  .where('transactionDate', '<=', endDate)
  .orderBy('transactionDate', 'desc')
  .limit(pageSize);
```

**3. Wallet-Specific Transaction Queries**:
```javascript
// Legacy wallet filtering (single wallet)
users/{userId}/transactions
  .where('walletId', '==', walletId)
  .where('transactionDate', '>=', startDate)
  .where('transactionDate', '<=', endDate)
  .orderBy('transactionDate', 'desc')
  .limit(pageSize);

// Modern multi-wallet filtering (affected wallets)
users/{userId}/transactions
  .where('affectedWalletIds', 'array-contains', walletId)
  .where('transactionDate', '>=', startDate)
  .where('transactionDate', '<=', endDate)
  .orderBy('transactionDate', 'desc')
  .limit(pageSize);

// Transfer transaction filtering (source or destination)
// Source wallet transfers
users/{userId}/transactions
  .where('sourceWalletId', '==', walletId)
  .where('transactionDate', '>=', startDate)
  .where('transactionDate', '<=', endDate)
  .orderBy('transactionDate', 'desc')
  .limit(pageSize);

// Destination wallet transfers
users/{userId}/transactions
  .where('destinationWalletId', '==', walletId)
  .where('transactionDate', '>=', startDate)
  .where('transactionDate', '<=', endDate)
  .orderBy('transactionDate', 'desc')
  .limit(pageSize);
```

**4. Tag-Based Filtering**:
```javascript
// Single tag filtering
users/{userId}/transactions
  .where('tags', 'array-contains', tagName)
  .where('transactionDate', '>=', startDate)
  .where('transactionDate', '<=', endDate)
  .orderBy('transactionDate', 'desc')
  .limit(pageSize);
```

**5. Transaction Type Filtering**:
```javascript
// Income transactions only
users/{userId}/transactions
  .where('type', '==', 'INCOME')
  .where('transactionDate', '>=', startDate)
  .where('transactionDate', '<=', endDate)
  .orderBy('transactionDate', 'desc')
  .limit(pageSize);

// Expense transactions only
users/{userId}/transactions
  .where('type', '==', 'EXPENSE')
  .where('transactionDate', '>=', startDate)
  .where('transactionDate', '<=', endDate)
  .orderBy('transactionDate', 'desc')
  .limit(pageSize);

// Transfer transactions only
users/{userId}/transactions
  .where('type', '==', 'TRANSFER')
  .where('transactionDate', '>=', startDate)
  .where('transactionDate', '<=', endDate)
  .orderBy('transactionDate', 'desc')
  .limit(pageSize);
```

**6. Credit/Debit Summary Calculations**:
```javascript
// Calculate total income for filtered period
users/{userId}/transactions
  .where('type', '==', 'INCOME')
  .where('transactionDate', '>=', startDate)
  .where('transactionDate', '<=', endDate)
  .get()
  .then(snapshot => {
    const totalIncome = snapshot.docs.reduce((sum, doc) => sum + doc.data().amount, 0);
  });

// Calculate total expenses for filtered period
users/{userId}/transactions
  .where('type', '==', 'EXPENSE')
  .where('transactionDate', '>=', startDate)
  .where('transactionDate', '<=', endDate)
  .get()
  .then(snapshot => {
    const totalExpenses = snapshot.docs.reduce((sum, doc) => sum + doc.data().amount, 0);
  });
```

### Wallet Detail Page Queries
Optimized queries for comprehensive wallet analysis and transaction history:

**1. Wallet Transaction History**:
```javascript
// All transactions affecting specific wallet (modern approach)
users/{userId}/transactions
  .where('affectedWalletIds', 'array-contains', walletId)
  .orderBy('transactionDate', 'desc')
  .limit(100);  // Paginated loading

// Legacy wallet transaction history
users/{userId}/transactions
  .where('walletId', '==', walletId)
  .orderBy('transactionDate', 'desc')
  .limit(100);

// Transfer transactions as source
users/{userId}/transactions
  .where('sourceWalletId', '==', walletId)
  .orderBy('transactionDate', 'desc')
  .limit(100);

// Transfer transactions as destination
users/{userId}/transactions
  .where('destinationWalletId', '==', walletId)
  .orderBy('transactionDate', 'desc')
  .limit(100);
```

**2. Monthly Wallet Analysis**:
```javascript
// Monthly income for specific wallet
users/{userId}/transactions
  .where('affectedWalletIds', 'array-contains', walletId)
  .where('type', '==', 'INCOME')
  .where('transactionDate', '>=', monthStart)
  .where('transactionDate', '<=', monthEnd)
  .get();

// Monthly expenses for specific wallet
users/{userId}/transactions
  .where('affectedWalletIds', 'array-contains', walletId)
  .where('type', '==', 'EXPENSE')
  .where('transactionDate', '>=', monthStart)
  .where('transactionDate', '<=', monthEnd)
  .get();

// Monthly transfers out (as source)
users/{userId}/transactions
  .where('sourceWalletId', '==', walletId)
  .where('type', '==', 'TRANSFER')
  .where('transactionDate', '>=', monthStart)
  .where('transactionDate', '<=', monthEnd)
  .get();

// Monthly transfers in (as destination)
users/{userId}/transactions
  .where('destinationWalletId', '==', walletId)
  .where('type', '==', 'TRANSFER')
  .where('transactionDate', '>=', monthStart)
  .where('transactionDate', '<=', monthEnd)
  .get();
```

**3. Currency Conversion Rate Lookup**:
```javascript
// Primary rate lookup (monthly rates take precedence)
users/{userId}/currency_rates
  .where('fromCurrency', '==', walletCurrency)
  .where('toCurrency', '==', defaultCurrency)
  .where('monthYear', '==', currentMonth)  // "YYYY-MM" format
  .where('isActive', '==', true)
  .limit(1)
  .get();

// Fallback to default rate if monthly rate unavailable
users/{userId}/currency_rates
  .where('fromCurrency', '==', walletCurrency)
  .where('toCurrency', '==', defaultCurrency)
  .where('rateType', '==', 'default')
  .where('isActive', '==', true)
  .limit(1)
  .get();

// Inverse rate calculation if direct rate unavailable
users/{userId}/currency_rates
  .where('fromCurrency', '==', defaultCurrency)
  .where('toCurrency', '==', walletCurrency)
  .where('monthYear', '==', currentMonth)
  .where('isActive', '==', true)
  .limit(1)
  .get();
```

## Business Logic Constraints & Validation Rules

### Critical System Constraints

**1. Balance Integrity System**
- **Primary Rule**: Sum of all Logical wallet balances must equal sum of all Physical wallet balances
- **Validation Timing**: Checked on every transaction creation, modification, and deletion
- **Error Handling**: System displays warnings when discrepancies detected
- **Resolution**: Users must correct imbalances before certain operations proceed
- **Monitoring**: Dashboard displays unallocated balance when totals don't match

**2. Transaction Validation Framework**
- **Amount Constraints**: All transaction amounts must be positive numbers (> 0)
- **Date Validation**: Transaction dates cannot be set in the future
- **Wallet Ownership**: All referenced wallets must belong to authenticated user
- **Type-Specific Rules**:
  - Income/Expense: Must affect exactly one Physical and one Logical wallet
  - Transfer: Source and destination must have identical currency and wallet type
  - Transfer: Cannot transfer from a wallet to itself

**3. Multi-Currency System Rules**
- **Rate Hierarchy**: Monthly rates override default rates when available
- **Conversion Logic**: System calculates inverse rates when direct rates unavailable
- **Display Preservation**: Original transaction currencies always maintained
- **Warning System**: Users alerted when conversion rates missing for accurate calculations
- **Precision Control**: Currency-specific decimal precision enforced (BTC: 8, GOLD: 3, Fiat: 2)

### Data Consistency Rules

**4. Wallet Management Constraints**
- **Unique Naming**: Wallet names must be unique within user's account
- **Physical Form Matching**: Physical form must correspond to supported currency type
- **Currency Support**: Only application-supported currencies allowed
- **Balance Updates**: All balance changes trigger automatic timestamp updates
- **Type Consistency**: Wallet type cannot be changed after creation (data integrity)

**5. Tag System Rules**
- **Multiple Tags**: Transactions can have zero to unlimited tags
- **Default Behavior**: Empty tag array results in "Untagged" display
- **Case Sensitivity**: Tags are case-sensitive for precise categorization
- **Historical Persistence**: All previous tags remembered for auto-completion
- **Backward Compatibility**: Legacy category field maintained alongside new tag system

**6. Currency Rate Management**
- **Rate Validation**: All rates must be positive numbers greater than zero
- **Time Format**: Monthly rates require valid YYYY-MM format
- **Duplicate Prevention**: No duplicate rates for same currency pair and time period
- **Active Status**: Only active rates used for conversion calculations
- **Bi-directional Support**: System supports both direct and inverse rate calculations

### Operational Constraints

**7. Query Performance Rules**
- **Pagination Limits**: Transaction queries limited to configurable page sizes (10-200)
- **Date Range Restrictions**: Query date ranges should not exceed 1 year for performance
- **Filter Combinations**: Complex multi-filter queries optimized with proper indexing
- **Real-time Updates**: Live listeners limited to actively viewed data sets

**8. User Experience Constraints**
- **Default Currency**: Must be set during user onboarding, cannot be null
- **Balance Visibility**: Users can hide/show balances for privacy
- **Page Size Limits**: Transaction list pagination between 10-200 items
- **Theme Consistency**: UI theme changes apply immediately without restart

**9. Security & Privacy Rules**
- **Data Isolation**: Users can only access their own subcollection data
- **Authentication**: All operations require valid Firebase Auth token
- **Validation Layers**: Client-side validation for UX, server-side for security
- **Error Messages**: Security-conscious error messages that don't expose sensitive data
- **Audit Trail**: All significant operations logged with timestamps

### Integration Constraints

**10. Firebase Integration Rules**
- **Native Mode**: Firestore must operate in Native Mode (not Datastore Mode)
- **Document Limits**: Individual documents cannot exceed 1MB size limit
- **Query Limits**: Maximum 30 composite indexes per collection
- **Real-time Listeners**: Limited concurrent listeners for performance
- **Offline Support**: Application must handle offline scenarios gracefully

**11. Mobile Performance Constraints**
- **Memory Management**: ViewModels properly disposed when not in use
- **Network Efficiency**: Minimize unnecessary Firestore reads/writes
- **Battery Optimization**: Background processing limited to essential operations
- **Storage Limits**: Local cache size managed to prevent device storage issues
- **Responsive UI**: All operations must maintain smooth 60fps user interface

This comprehensive database schema provides the foundation for Profiteer's sophisticated personal finance management capabilities while ensuring data integrity, security, and optimal performance across all supported platforms.