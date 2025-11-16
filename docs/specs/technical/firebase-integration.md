# Firebase Security Guidelines

This document outlines the critical security requirements for Firestore queries in the Profiteer app.

## Security Rule Context

Our Firestore security rules enforce strict user data isolation. Every document contains a `userId` field, and users can only access documents where `userId` matches their authenticated user ID.

### Current Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // All collections follow this pattern
    match /{collection}/{documentId} {
      allow read, write: if request.auth != null && request.auth.uid == resource.data.userId;
      allow create: if request.auth != null && request.auth.uid == request.resource.data.userId;
    }
  }
}
```

## Mandatory Query Requirements

### 🚨 Rule #1: Every Query Must Filter by userId

**NO EXCEPTIONS**: All Firestore queries must include `.whereEqualTo("userId", userId)` as the first filter condition.

### ✅ Correct Implementations

```kotlin
// Single field query
fun getUserTransactions(userId: String): Flow<List<Transaction>> = callbackFlow {
  transactionsCollection
    .whereEqualTo("userId", userId)
    .orderBy("transactionDate", Query.Direction.DESCENDING)
    .addSnapshotListener { ... }
}

// Multi-field query
fun getWalletTransactions(walletId: String, userId: String): Flow<List<Transaction>> = callbackFlow {
  transactionsCollection
    .whereEqualTo("userId", userId)        // ALWAYS FIRST
    .whereEqualTo("walletId", walletId)
    .addSnapshotListener { ... }
}

// Array contains query
fun getAffectedWalletTransactions(walletId: String, userId: String): Flow<List<Transaction>> = callbackFlow {
  transactionsCollection
    .whereEqualTo("userId", userId)        // ALWAYS FIRST
    .whereArrayContains("affectedWalletIds", walletId)
    .addSnapshotListener { ... }
}

// Transfer queries
fun getTransferTransactions(walletId: String, userId: String): Flow<List<Transaction>> = callbackFlow {
  transactionsCollection
    .whereEqualTo("userId", userId)        // ALWAYS FIRST
    .whereEqualTo("sourceWalletId", walletId)
    .addSnapshotListener { ... }
}
```

### ❌ Forbidden Patterns

```kotlin
// Missing userId entirely
transactionsCollection
  .whereEqualTo("walletId", walletId)  // WILL FAIL WITH PERMISSION_DENIED
  .get()

// Missing userId from function signature
fun getTransactions(walletId: String): Flow<List<Transaction>>  // NO userId PARAMETER

// Wrong order (userId should be first)
transactionsCollection
  .whereEqualTo("walletId", walletId)
  .whereEqualTo("userId", userId)  // Should be first for index efficiency
```

## Repository Pattern Requirements

### Function Signatures

All repository query methods must:
1. Accept `userId: String` as a parameter
2. Use `userId` in the first `.whereEqualTo()` condition
3. Pass `userId` from ViewModels/callers

```kotlin
// Required pattern for all query methods
fun getSomeData(otherParams: String, userId: String): Flow<List<SomeModel>> = callbackFlow {
  collection
    .whereEqualTo("userId", userId)  // Required first filter
    .whereEqualTo("otherField", otherParams)
    .addSnapshotListener { ... }
}
```

### Caller Responsibility

ViewModels and other callers must:
1. Obtain `userId` from `AuthRepository.getCurrentUserId()`
2. Pass `userId` to all repository query methods
3. Never assume `userId` is available within repositories

```kotlin
// ✅ Correct ViewModel pattern
class SomeViewModel @Inject constructor(
  private val authRepository: AuthRepository,
  private val dataRepository: DataRepository
) : ViewModel() {

  private val userId = authRepository.getCurrentUserId() ?: ""

  fun loadData(itemId: String) {
    dataRepository.getSomeData(itemId, userId)  // Pass userId
      .collect { ... }
  }
}
```

## Security Validation Process

### Before Adding New Queries

Use this checklist for every new Firestore query:

- [ ] Does the function accept `userId: String` parameter?
- [ ] Is `.whereEqualTo("userId", userId)` the FIRST condition?
- [ ] Do all callers pass the correct `userId`?
- [ ] Is the query tested with actual Firebase security rules?
- [ ] Does the query follow existing repository patterns?

### Code Review Checklist

When reviewing Firestore-related code:

- [ ] All `.whereEqualTo()`, `.whereArrayContains()`, etc. start with `userId`
- [ ] Repository methods have `userId` parameter
- [ ] ViewModels pass `userId` from `AuthRepository`
- [ ] No queries bypass the security pattern

## Common Security Violations & Fixes

### 1. Missing userId Filter

**Problem:**
```kotlin
// ❌ Missing userId
transactionsCollection.whereEqualTo("walletId", walletId)
```

**Fix:**
```kotlin
// ✅ Add userId first
transactionsCollection
  .whereEqualTo("userId", userId)
  .whereEqualTo("walletId", walletId)
```

### 2. Missing userId Parameter

**Problem:**
```kotlin
// ❌ No userId parameter
fun getWalletData(walletId: String): Flow<List<Data>>
```

**Fix:**
```kotlin
// ✅ Add userId parameter
fun getWalletData(walletId: String, userId: String): Flow<List<Data>>
```

### 3. Repository Without AuthRepository

**Problem:**
```kotlin
// ❌ Repository trying to get userId internally
class Repository @Inject constructor(
  private val firestore: FirebaseFirestore
) {
  fun getData() {
    // Can't get userId here!
  }
}
```

**Fix:**
```kotlin
// ✅ Get userId from caller (ViewModel)
class Repository @Inject constructor(
  private val firestore: FirebaseFirestore
) {
  fun getData(userId: String) {  // Accept as parameter
    firestore.collection("data")
      .whereEqualTo("userId", userId)
      .get()
  }
}
```

## Error Diagnostics

### Permission Denied Errors

If you see these errors, check for missing `userId` filters:

```
PERMISSION_DENIED: Missing or insufficient permissions
Error in [query type] query
FirebaseFirestoreException: PERMISSION_DENIED
```

### Debugging Steps

1. **Check Query Pattern**: Ensure `userId` is the first filter
2. **Verify Function Signature**: Confirm `userId` parameter exists
3. **Trace Caller**: Verify `userId` is passed correctly
4. **Test Locally**: Use Firebase emulator with security rules
5. **Review Logs**: Look for specific query error messages

## Security Benefits

Following these patterns ensures:

1. **Data Isolation**: Users can only access their own data
2. **Performance**: Queries use optimal indexes (userId first)
3. **Compliance**: Meets security rule requirements
4. **Maintainability**: Consistent patterns across codebase
5. **Future-Proof**: Works with any security rule updates

## Examples from Codebase

### Fixed Security Issues (September 2025)

Previously insecure queries that were fixed:

```kotlin
// ❌ Before (caused PERMISSION_DENIED)
transactionsCollection.whereEqualTo("destinationWalletId", walletId)

// ✅ After (secure)
transactionsCollection
  .whereEqualTo("userId", userId)
  .whereEqualTo("destinationWalletId", walletId)
```

### Secure Repository Examples

See these files for correct implementations:
- `TransactionRepository.kt` - All queries secured with userId filters
- `WalletRepository.kt` - Proper userId filtering patterns
- `UserPreferencesRepository.kt` - Single-user data access pattern
- `CurrencyRateRepository.kt` - Multi-condition queries with userId first

---

**Remember**: Every Firestore query is a potential security vulnerability if it doesn't include proper userId filtering. When in doubt, always include the userId filter first.