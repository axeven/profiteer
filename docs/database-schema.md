# Database Schema

This document outlines the Firestore database schema for the Profiteer personal finance application.

## Collections Overview

The database uses the following main collections:
- `users` - User profiles and settings
- `currencies` - Supported currencies and conversion rates
- `wallets` - Physical and logical wallets
- `transactions` - All financial transactions
- `transaction_tags` - Categories/tags for transactions

## Collection Schemas

### users
```json
{
  "userId": "string", // Firebase Auth UID (document ID)
  "email": "string",
  "defaultCurrency": "string", // Currency code (e.g., "USD")
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

### currencies
```json
{
  "currencyId": "string", // Currency code (document ID, e.g., "USD")
  "name": "string", // Full name (e.g., "US Dollar")
  "symbol": "string", // Currency symbol (e.g., "$")
  "isActive": "boolean"
}
```

### currency_rates (subcollection of users)
Path: `users/{userId}/currency_rates`
```json
{
  "rateId": "string", // Auto-generated document ID
  "fromCurrency": "string", // Source currency code
  "toCurrency": "string", // Target currency code
  "rate": "number", // Conversion rate
  "rateType": "string", // "flat" or "monthly"
  "monthYear": "string", // For monthly rates: "2024-01" format, null for flat rates
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

### wallets (subcollection of users)
Path: `users/{userId}/wallets`
```json
{
  "walletId": "string", // Auto-generated document ID
  "name": "string", // Wallet name (must be unique per user)
  "walletType": "string", // "Physical" or "Logical"
  "currency": "string", // Currency code (USD, EUR, IDR, etc.)
  "balance": "number", // Current balance (includes transactions + initial balance)
  "initialBalance": "number", // Initial setup balance (excluded from analytics)
  "userId": "string", // Reference to user document
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

**Note**: The `transactionBalance` (balance - initialBalance) represents the net change from transactions only and is used for analytics calculations.

### transaction_tags (subcollection of users)
Path: `users/{userId}/transaction_tags`
```json
{
  "tagId": "string", // Auto-generated document ID
  "name": "string", // Tag name (e.g., "Food", "Transportation")
  "color": "string", // Hex color code for UI
  "icon": "string", // Icon identifier
  "createdAt": "timestamp"
}
```

### transactions (subcollection of users)
Path: `users/{userId}/transactions`
```json
{
  "transactionId": "string", // Auto-generated document ID
  "title": "string", // Transaction title/description
  "amount": "number", // Transaction amount (positive for income, negative for expense)
  "category": "string", // Transaction category
  "type": "string", // "INCOME" or "EXPENSE" (enum values)
  "walletId": "string", // Associated wallet ID
  "userId": "string", // Reference to user document
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

**Current Implementation Note**: The current codebase uses a simplified transaction model with income/expense types. The original three-type system (increase/decrease/transfer) from the architecture docs is not yet implemented.

### user_preferences (subcollection of users)
Path: `users/{userId}/user_preferences`
```json
{
  "preferencesId": "string", // Auto-generated document ID
  "defaultCurrency": "string", // User's default currency (USD, EUR, IDR, etc.)
  "userId": "string", // Reference to user document
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

## Data Relationships

### User Data Isolation
- All user-specific data is stored as subcollections under the user's document
- Each user can only access their own data through security rules

### Wallet Relationships
- **Physical Wallets**: Independent entities with their own balances
- **Logical Wallets**: Reference one or more physical wallets via `parentWalletIds`
- Sum of logical wallet balances should equal sum of physical wallet balances

### Transaction Relationships
- Each transaction belongs to a user and references wallet(s)
- Transaction tags provide categorization (with "Uncategorized" as default)
- Transfer transactions must have same currency for source and destination wallets

### Currency Relationships
- User sets a default currency for display purposes
- Currency rates allow conversion between different currencies
- Rates can be flat (permanent) or monthly-specific for accuracy

## Indexes

### Composite Indexes Required
```
Collection: users/{userId}/transactions
Fields: date (Descending), createdAt (Descending)

Collection: users/{userId}/transactions  
Fields: type (Ascending), date (Descending)

Collection: users/{userId}/transactions
Fields: tagId (Ascending), date (Descending)

Collection: users/{userId}/transactions
Fields: walletId (Ascending), date (Descending)

Collection: users/{userId}/transactions
Fields: sourceWalletId (Ascending), date (Descending)

Collection: users/{userId}/transactions
Fields: destinationWalletId (Ascending), date (Descending)

Collection: users/{userId}/transactions
Fields: walletId (Ascending), tagId (Ascending), date (Descending)

Collection: users/{userId}/transactions
Fields: sourceWalletId (Ascending), tagId (Ascending), date (Descending)

Collection: users/{userId}/transactions
Fields: destinationWalletId (Ascending), tagId (Ascending), date (Descending)

Collection: users/{userId}/currency_rates
Fields: fromCurrency (Ascending), toCurrency (Ascending), monthYear (Ascending)
```

## Security Rules Considerations

- Users can only read/write their own data
- Wallet balance consistency should be validated
- Transaction amounts must be positive
- Currency codes must exist in currencies collection
- Transfer transactions require matching currencies

## Query Patterns

### Transaction List Page Queries
To support the transaction list page with filtering and pagination:

1. **Get transactions for current month** (default view):
   ```
   Query: users/{userId}/transactions
   Where: date >= {monthStart} AND date <= {monthEnd}
   OrderBy: date (descending)
   Limit: {pageSize}
   ```

2. **Filter by time range**:
   ```
   Query: users/{userId}/transactions
   Where: date >= {startDate} AND date <= {endDate}
   OrderBy: date (descending)
   Limit: {pageSize}
   ```

3. **Filter by wallet**:
   ```
   Query: users/{userId}/transactions
   Where: (walletId == {walletId} OR sourceWalletId == {walletId} OR destinationWalletId == {walletId})
         AND date >= {startDate} AND date <= {endDate}
   OrderBy: date (descending)
   Limit: {pageSize}
   ```

4. **Filter by tag**:
   ```
   Query: users/{userId}/transactions
   Where: tagId == {tagId} AND date >= {startDate} AND date <= {endDate}
   OrderBy: date (descending)
   Limit: {pageSize}
   ```

5. **Combined filters** (wallet + tag + time range):
   ```
   Query: users/{userId}/transactions
   Where: walletId == {walletId} AND tagId == {tagId} AND date >= {startDate} AND date <= {endDate}
   OrderBy: date (descending)
   Limit: {pageSize}
   ```

6. **Calculate total credit/debit for filtered results**:
   ```
   Query: users/{userId}/transactions
   Where: [same filters as above] AND type == "increase" // for credit
   Query: users/{userId}/transactions
   Where: [same filters as above] AND type == "decrease" // for debit
   ```

### Wallet Detail Page Queries
To support the wallet detail page requirements:

1. **Get wallet transactions** (descending by date):
   ```
   Query: users/{userId}/transactions
   Where: walletId == {walletId} OR sourceWalletId == {walletId} OR destinationWalletId == {walletId}
   OrderBy: date (descending)
   ```

2. **Calculate monthly debit/credit sums**:
   ```
   Query: users/{userId}/transactions  
   Where: walletId == {walletId} AND date >= {monthStart} AND date <= {monthEnd}
   Additional filters by transaction type for debit/credit calculation
   ```

3. **Currency conversion for display**:
   ```
   Query: users/{userId}/currency_rates
   Where: fromCurrency == {walletCurrency} AND toCurrency == {defaultCurrency}
   Additional filter by monthYear for monthly rates or rateType == "flat"
   ```

## Business Logic Constraints

1. **Balance Integrity**: Sum of logical wallet balances must equal sum of physical wallet balances
2. **Currency Matching**: Transfer transactions require same currency for both wallets
3. **Transaction Validation**: All amounts must be positive numbers
4. **Default Tag**: Transactions without explicit tags get "Uncategorized" tag
5. **Rate Management**: Currency rates can be either flat (permanent) or month-specific
6. **Wallet Detail Display**: Amounts can be shown in wallet's native currency or converted to user's default currency
7. **Transaction List Pagination**: Default view shows current month transactions with pagination support
8. **Filter Combinations**: Multiple filters (time range, wallet, tag) can be applied simultaneously
9. **Credit/Debit Calculation**: Summary totals must be calculated separately for filtered transaction sets