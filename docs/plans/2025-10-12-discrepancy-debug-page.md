# Debug Discrepancy Page Implementation Plan

**Date:** 2025-10-12
**Status:** Phase 2 Complete ✅
**Approach:** Test-Driven Development (TDD)

## Overview

Implement a debug page to identify and display balance discrepancies between Physical and Logical wallets. The page will be accessible from the Logical Wallet List page only when a discrepancy exists, and will display transactions in descending order starting from the first transaction where the discrepancy occurred.

## Business Requirements

### Discrepancy Definition
- **Discrepancy exists when**: `Sum(Physical Wallet Balances) ≠ Sum(Logical Wallet Balances)`
- Must check all wallets for the current user
- Must consider multi-currency conversions to base currency for accurate comparison

### Access Conditions
- Only visible from Logical Wallet List page
- Only accessible when discrepancy is detected
- Should show visual indicator (button/badge) on Logical Wallet List page when discrepancy exists

### Display Requirements
- Show transactions in **descending order** (newest first)
- Start from the **first transaction where discrepancy occurred**
- Display running balance totals (Physical vs Logical) for each transaction
- Highlight the specific transaction where discrepancy started

## Implementation Plan

### Phase 1: Core Business Logic (TDD) ✅ COMPLETED

#### 1.1 Balance Calculation Utility ✅
- [x] **Test**: Write test for `BalanceDiscrepancyDetector.calculateTotalPhysicalBalance()`
  - [x] Test with single wallet
  - [x] Test with multiple wallets
  - [x] Test with empty wallet list
  - [x] Test with zero balance wallets
  - [x] Test ignores logical wallets
  - [x] Test with negative balances
- [x] **Code**: Implement `calculateTotalPhysicalBalance()`
- [x] **Test**: Write test for `BalanceDiscrepancyDetector.calculateTotalLogicalBalance()`
  - [x] Test with single wallet
  - [x] Test with multiple wallets
  - [x] Test with empty wallet list
  - [x] Test with zero balance wallets
  - [x] Test ignores physical wallets
  - [x] Test with negative balances
- [x] **Code**: Implement `calculateTotalLogicalBalance()`

**Note**: Multi-currency conversion not needed - all wallets use same currency (defaultCurrency from UserPreferences).

#### 1.2 Discrepancy Detection Logic ✅
- [x] **Test**: Write test for `BalanceDiscrepancyDetector.hasDiscrepancy()`
  - [x] Test when balances match exactly
  - [x] Test when balances differ
  - [x] Test with floating-point precision tolerance (0.01)
  - [x] Test with custom tolerance
  - [x] Test with zero balances
  - [x] Test with negative balances
- [x] **Code**: Implement `hasDiscrepancy()`
- [x] **Test**: Write test for `BalanceDiscrepancyDetector.getDiscrepancyAmount()`
  - [x] Test positive discrepancy (Physical > Logical)
  - [x] Test negative discrepancy (Logical > Physical)
  - [x] Test zero discrepancy
  - [x] Test with zero balances
  - [x] Test with negative balances
  - [x] Test with large values
- [x] **Code**: Implement `getDiscrepancyAmount()`

#### 1.3 Transaction Analysis Logic ✅
- [x] **Test**: Write test for `DiscrepancyAnalyzer.findFirstDiscrepancyTransaction()`
  - [x] Test with transactions causing immediate discrepancy
  - [x] Test with balanced transactions (no discrepancy)
  - [x] Test with empty transaction list
  - [x] Test with gradual discrepancy accumulation
  - [x] Test chronological ordering (oldest to newest analysis)
- [x] **Code**: Implement `findFirstDiscrepancyTransaction()`
- [x] **Test**: Write test for `DiscrepancyAnalyzer.calculateRunningBalances()`
  - [x] Test with single transaction
  - [x] Test with multiple transactions tracking cumulative balance
  - [x] Test marks first discrepancy correctly
  - [x] Test with empty list
  - [x] Test handles expense transactions
  - [x] Test handles transfer transactions
  - [x] Test with multiple wallets per type
- [x] **Code**: Implement `calculateRunningBalances()`
- [x] **Code**: Create `TransactionWithBalances` data class

**Files Created:**
- `app/src/main/java/com/axeven/profiteerapp/utils/BalanceDiscrepancyDetector.kt`
- `app/src/main/java/com/axeven/profiteerapp/utils/DiscrepancyAnalyzer.kt`
- `app/src/test/java/com/axeven/profiteerapp/utils/BalanceDiscrepancyDetectorTest.kt`
- `app/src/test/java/com/axeven/profiteerapp/utils/DiscrepancyAnalyzerTest.kt`

**Test Results:** All 37 tests passing ✅

### Phase 2: Repository Layer (TDD) ✅ COMPLETED

#### 2.1 Transaction Repository Enhancement ✅
- [x] **Test**: Write test for `TransactionRepository.getAllTransactionsChronological(userId)`
  - [x] Test returns transactions in chronological order (oldest first)
  - [x] Test filters by userId correctly
  - [x] Test handles empty results
  - [x] Test includes all transaction types (INCOME, EXPENSE, TRANSFER)
  - [x] Test includes affectedWalletIds
  - [x] Test handles null transactionDate with createdAt fallback
- [x] **Code**: Implement `getAllTransactionsChronological(userId)`
  - [x] Add `.whereEqualTo("userId", userId)` as first filter (security compliant)
  - [x] Add `.orderBy("transactionDate", Query.Direction.ASCENDING)`
  - [x] Return Flow for real-time updates
  - [x] Add comprehensive error handling with FirestoreErrorHandler
  - [x] Add authentication error recovery
  - [x] Add logging for debugging

#### 2.2 Wallet Repository Enhancement ✅
- [x] **Verification**: Confirmed `WalletRepository.getUserWallets(userId)` already exists
  - [x] Returns all Physical wallets
  - [x] Returns all Logical wallets
  - [x] Filters by userId correctly (security compliant)
  - [x] Returns Flow for real-time updates
  - [x] userId filter is first condition
  - [x] Includes balance and initialBalance
  - [x] Handles zero and negative balances
- [x] **Test**: Write comprehensive tests for wallet repository behavior
  - [x] Test filters by userId
  - [x] Test includes both Physical and Logical wallets
  - [x] Test includes balance information
  - [x] Test handles empty wallet list
  - [x] Test handles zero balance wallets
  - [x] Test handles negative balance wallets

**Files Created/Modified:**
- Created: `app/src/test/java/com/axeven/profiteerapp/data/repository/DiscrepancyRepositoryTest.kt` (18 comprehensive tests)
- Modified: `app/src/main/java/com/axeven/profiteerapp/data/repository/TransactionRepository.kt` (added `getAllTransactionsChronological()`)
- Verified: `app/src/main/java/com/axeven/profiteerapp/data/repository/WalletRepository.kt` (`getUserWallets()` already exists)

**Test Results:** All 18 repository tests passing ✅
**Security Compliance:** All queries follow Firebase security rules (userId filter first) ✅

### Phase 3: ViewModel Layer (TDD)

#### 3.1 Logical Wallet List ViewModel Enhancement
- [ ] **Test**: Write test for `LogicalWalletListViewModel.discrepancyExists`
  - [ ] Test StateFlow emits true when discrepancy detected
  - [ ] Test StateFlow emits false when balances match
  - [ ] Test updates on wallet changes
  - [ ] Test updates on transaction changes
- [ ] **Code**: Add `discrepancyExists: StateFlow<Boolean>`
- [ ] **Test**: Write test for `LogicalWalletListViewModel.navigateToDiscrepancyDebug()`
  - [ ] Test navigation event emitted
  - [ ] Test only callable when discrepancy exists
- [ ] **Code**: Implement `navigateToDiscrepancyDebug()`

#### 3.2 Discrepancy Debug ViewModel
- [ ] **Test**: Write test for `DiscrepancyDebugViewModel.uiState` initialization
  - [ ] Test loads all transactions on init
  - [ ] Test loads all wallets on init
  - [ ] Test calculates discrepancy amount
  - [ ] Test identifies first discrepancy transaction
- [ ] **Code**: Create `DiscrepancyDebugViewModel` with DI
  - [ ] Inject `TransactionRepository`
  - [ ] Inject `WalletRepository`
  - [ ] Inject `BalanceDiscrepancyDetector`
  - [ ] Inject `DiscrepancyAnalyzer`
  - [ ] Inject `Logger`
- [ ] **Test**: Write test for transaction list with running balances
  - [ ] Test transactions sorted descending (newest first)
  - [ ] Test running balances calculated for each transaction
  - [ ] Test first discrepancy transaction highlighted
  - [ ] Test physical vs logical balance shown per transaction
- [ ] **Code**: Implement `uiState: StateFlow<DiscrepancyDebugUiState>`
- [ ] **Test**: Write test for `DiscrepancyDebugViewModel.refresh()`
  - [ ] Test reloads data
  - [ ] Test recalculates discrepancy
  - [ ] Test updates UI state
- [ ] **Code**: Implement `refresh()`

#### 3.3 UI State Model
- [ ] **Test**: Write test for `DiscrepancyDebugUiState` data class
  - [ ] Test immutable state updates
  - [ ] Test derived properties (isLoading, hasError)
  - [ ] Test transaction item state (highlighted, balances)
- [ ] **Code**: Create `DiscrepancyDebugUiState` following consolidated state pattern
  ```kotlin
  data class DiscrepancyDebugUiState(
      val isLoading: Boolean = true,
      val transactions: List<TransactionWithBalances> = emptyList(),
      val firstDiscrepancyId: String? = null,
      val currentDiscrepancy: Double = 0.0,
      val totalPhysicalBalance: Double = 0.0,
      val totalLogicalBalance: Double = 0.0,
      val error: String? = null
  )

  data class TransactionWithBalances(
      val transaction: Transaction,
      val physicalBalanceAfter: Double,
      val logicalBalanceAfter: Double,
      val isFirstDiscrepancy: Boolean
  )
  ```

### Phase 4: UI Layer (Compose)

#### 4.1 Logical Wallet List Page Enhancement
- [ ] **Test**: Write Compose UI test for discrepancy indicator
  - [ ] Test indicator visible when discrepancy exists
  - [ ] Test indicator hidden when no discrepancy
  - [ ] Test indicator clickable and navigates to debug page
  - [ ] Test indicator shows discrepancy amount
- [ ] **Code**: Add discrepancy warning banner/button
  - [ ] Display at top of Logical Wallet List
  - [ ] Show discrepancy amount formatted
  - [ ] Use warning colors (Material 3 error/warning)
  - [ ] Add "View Details" or "Debug" action

#### 4.2 Discrepancy Debug Screen
- [ ] **Test**: Write Compose UI test for loading state
  - [ ] Test shows loading indicator
  - [ ] Test hides content while loading
- [ ] **Code**: Implement loading state UI
- [ ] **Test**: Write Compose UI test for error state
  - [ ] Test displays error message
  - [ ] Test shows retry button
- [ ] **Code**: Implement error state UI
- [ ] **Test**: Write Compose UI test for transaction list
  - [ ] Test displays transactions in descending order
  - [ ] Test shows transaction details (date, amount, wallets)
  - [ ] Test highlights first discrepancy transaction
  - [ ] Test shows running balances for each transaction
  - [ ] Test displays Physical vs Logical balance comparison
- [ ] **Code**: Implement transaction list with LazyColumn
  - [ ] Use Card for each transaction item
  - [ ] Show transaction date, amount, description
  - [ ] Show affected wallets
  - [ ] Display running Physical balance
  - [ ] Display running Logical balance
  - [ ] Highlight first discrepancy (border/background color)
- [ ] **Test**: Write Compose UI test for summary header
  - [ ] Test shows total Physical balance
  - [ ] Test shows total Logical balance
  - [ ] Test shows discrepancy amount
  - [ ] Test uses appropriate color (red for mismatch)
- [ ] **Code**: Implement summary header
  - [ ] Display current totals
  - [ ] Show discrepancy prominently
  - [ ] Add refresh button

#### 4.3 Navigation
- [ ] **Test**: Write navigation test for debug page
  - [ ] Test navigation from Logical Wallet List
  - [ ] Test back navigation
  - [ ] Test deep link support (optional)
- [ ] **Code**: Add route to navigation graph
  - [ ] Define route constant
  - [ ] Add composable to NavHost
  - [ ] Implement navigation from Logical Wallet List

### Phase 5: Integration & Polish

#### 5.1 Integration Testing
- [ ] **Test**: Write end-to-end test for discrepancy detection flow
  - [ ] Test create transactions causing discrepancy
  - [ ] Test indicator appears on Logical Wallet List
  - [ ] Test navigation to debug page
  - [ ] Test correct transaction identified
  - [ ] Test fix discrepancy (manually adjust transaction)
  - [ ] Test indicator disappears when fixed
- [ ] **Code**: Fix any integration issues

#### 5.2 Performance Optimization
- [ ] **Test**: Write performance test for large transaction lists
  - [ ] Test with 1000+ transactions
  - [ ] Test memory usage
  - [ ] Test UI rendering performance
- [ ] **Code**: Optimize if needed
  - [ ] Consider pagination for very large lists
  - [ ] Add caching for balance calculations
  - [ ] Use LazyColumn efficiently

#### 5.3 Error Handling & Logging
- [ ] **Test**: Write test for Firebase query errors
  - [ ] Test network errors
  - [ ] Test permission errors
  - [ ] Test empty state handling
- [ ] **Code**: Add comprehensive error handling
  - [ ] Log errors using Logger
  - [ ] Display user-friendly error messages
  - [ ] Add retry mechanisms
  - [ ] Log discrepancy detection events

#### 5.4 UI/UX Polish
- [ ] Add loading skeletons
- [ ] Add animations for list items
- [ ] Add pull-to-refresh
- [ ] Add empty state message (if no transactions)
- [ ] Add tooltips/help text explaining the page
- [ ] Ensure Material 3 design consistency
- [ ] Test accessibility (TalkBack, content descriptions)

### Phase 6: Documentation & Cleanup

#### 6.1 Code Documentation
- [ ] Add KDoc comments to all public classes/methods
- [ ] Document business logic reasoning
- [ ] Add inline comments for complex calculations

#### 6.2 Project Documentation
- [ ] Update README.md with new feature
- [ ] Update CLAUDE.md if new patterns introduced
- [ ] Update this plan document with ✅ completion status
- [ ] Document known limitations (if any)

#### 6.3 Final Testing
- [ ] Run all unit tests: `./gradlew testDebugUnitTest`
- [ ] Run lint checks: `./gradlew lintDebug`
- [ ] Manual testing on device/emulator
- [ ] Test with multiple currencies
- [ ] Test with edge cases (empty wallets, zero balances)

## Technical Specifications

### Data Models

```kotlin
// New utility classes
class BalanceDiscrepancyDetector @Inject constructor(
    private val currencyConverter: CurrencyConverter
) {
    fun calculateTotalPhysicalBalance(wallets: List<Wallet>, baseCurrency: String): Double
    fun calculateTotalLogicalBalance(wallets: List<Wallet>, baseCurrency: String): Double
    fun hasDiscrepancy(physicalTotal: Double, logicalTotal: Double, tolerance: Double = 0.01): Boolean
    fun getDiscrepancyAmount(physicalTotal: Double, logicalTotal: Double): Double
}

class DiscrepancyAnalyzer @Inject constructor(
    private val balanceCalculator: BalanceDiscrepancyDetector
) {
    fun findFirstDiscrepancyTransaction(
        transactions: List<Transaction>,
        wallets: List<Wallet>
    ): String? // Returns transaction ID

    fun calculateRunningBalances(
        transactions: List<Transaction>,
        wallets: List<Wallet>
    ): List<TransactionWithBalances>
}
```

### Firebase Security Compliance

All Firestore queries MUST follow security guidelines:

```kotlin
// ✅ Correct pattern - userId filter first
fun getAllTransactionsChronological(userId: String): Flow<List<Transaction>> = callbackFlow {
    transactionsCollection
        .whereEqualTo("userId", userId)  // REQUIRED FIRST!
        .orderBy("date", Query.Direction.ASCENDING)
        .addSnapshotListener { snapshot, error -> ... }
}
```

### State Management

Following consolidated state pattern:
- Single `DiscrepancyDebugUiState` data class
- Immutable state updates
- Automatic validation and derived properties
- Comprehensive unit tests for state model

### Logging Strategy

- Use injected `Logger` for all logging
- Log discrepancy detection events (info level)
- Log calculation errors (error level)
- Log navigation events (debug level)
- Never log sensitive financial data directly

## Success Criteria

- [ ] All unit tests pass (100% coverage for business logic)
- [ ] All Compose UI tests pass
- [ ] Lint checks pass with no errors
- [ ] Discrepancy correctly detected and displayed
- [ ] First discrepancy transaction accurately identified
- [ ] Running balances calculated correctly
- [ ] UI is responsive and performant
- [ ] Follows all project guidelines (CLAUDE.md)
- [ ] Firebase security rules compliant
- [ ] Documentation complete

## Estimated Effort

- **Phase 1-2**: 4-6 hours (Core logic + Repository)
- **Phase 3**: 3-4 hours (ViewModel)
- **Phase 4**: 4-5 hours (UI)
- **Phase 5**: 2-3 hours (Integration & Polish)
- **Phase 6**: 1-2 hours (Documentation)

**Total**: 14-20 hours

## Risks & Mitigations

| Risk | Mitigation |
|------|------------|
| Complex balance calculation logic | TDD approach, comprehensive test cases |
| Performance with large transaction lists | Pagination, lazy loading, performance tests |
| Multi-currency conversion accuracy | Use existing CurrencyConverter, add tolerance |
| Identifying exact discrepancy point | Incremental balance tracking, transaction-by-transaction analysis |
| Firebase query performance | Proper indexing, efficient queries with userId filter |

## Notes

- This is a **debug/diagnostic** feature, not a user-facing production feature
- Consider adding a flag to enable/disable in production builds
- May want to add export functionality (CSV) in future iterations
- Could expand to show detailed breakdown per wallet in future
- Consider adding fix suggestions or auto-correction tools

---

**Plan Status**: ⏳ Not Started
**Last Updated**: 2025-10-12
