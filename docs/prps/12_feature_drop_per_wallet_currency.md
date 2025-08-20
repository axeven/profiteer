# PRP 12: Drop Per-Wallet Currency Support

## Overview

This PRP documents the plan to remove per-wallet currency support from the Profiteer application. Currently, each wallet stores its own currency, leading to currency mismatches between Physical and Logical wallets and complex currency conversion logic. The new approach will use a single global default currency for all wallets, with currency rates only used for display conversion when users want to view balances in non-default currencies.

## Problem Statement

### Current Issues
1. **Currency Mismatch Complexity**: Physical and Logical wallets can have different currencies, causing validation issues and complex balance calculations
2. **Transfer Restrictions**: Current transfer validation requires same currency between source and destination wallets, limiting flexibility
3. **Complex Conversion Logic**: Multiple currency conversion paths in balance aggregation and transaction processing
4. **User Confusion**: Managing multiple currencies per wallet adds unnecessary complexity for most users
5. **Physical Form Restrictions**: `PhysicalForm` enum enforces `allowedCurrencies` restrictions that become obsolete

### Current Architecture Problems
- `Wallet.currency` field requires validation across wallet types
- `PhysicalForm.allowedCurrencies` creates artificial restrictions
- Currency conversion logic scattered across ViewModels
- Transfer validation blocks same-type, different-currency transfers

## Proposed Solution

### Single Global Currency Approach
- All wallets will use the system's default currency (from `UserPreferences.defaultCurrency`)
- Currency rates will only be used for display conversion when users want to view their portfolio in different currencies
- Remove all currency-based restrictions on wallet creation and transfers
- Simplify balance calculations by eliminating currency conversion in core logic

## Implementation Plan

### Phase 1: Data Model Changes

#### 1.1 Update Wallet Model
**File**: `app/src/main/java/com/axeven/profiteerapp/data/model/Wallet.kt`

```kotlin
data class Wallet(
    @DocumentId
    val id: String = "",
    val name: String = "",
    // REMOVED: val currency: String = "",
    val balance: Double = 0.0,
    val initialBalance: Double = 0.0,
    val walletType: String = "Physical",
    val physicalForm: PhysicalForm = PhysicalForm.FIAT_CURRENCY,
    val userId: String = "",
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
) {
    // Updated constructor without currency
    constructor() : this("", "", 0.0, 0.0, "Physical", PhysicalForm.FIAT_CURRENCY, "", null, null)
    
    @get:Exclude
    val transactionBalance: Double
        get() = balance - initialBalance
}
```

#### 1.2 Update PhysicalForm Enum
**File**: `app/src/main/java/com/axeven/profiteerapp/data/model/PhysicalForm.kt`

```kotlin
enum class PhysicalForm(
    val displayName: String,
    val description: String,
    val icon: String
    // REMOVED: val allowedCurrencies: Set<String>? = null
) {
    FIAT_CURRENCY(
        displayName = "Fiat Currency",
        description = "Traditional government-issued currencies",
        icon = "💰"
        // REMOVED: allowedCurrencies = setOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD", "IDR")
    ),
    // ... other forms updated similarly
    
    companion object {
        // REMOVED: isCurrencyAllowed(), getDefaultForCurrency(), getCompatibleForms()
        
        // Keep grouping logic for UI organization
        val INVESTMENT_FORMS = setOf(STOCKS, ETFS, BONDS, MUTUAL_FUNDS)
        val ALTERNATIVE_INVESTMENTS = setOf(PRECIOUS_METALS, REAL_ESTATE, COMMODITIES)
        val CASH_FORMS = setOf(FIAT_CURRENCY, CASH_EQUIVALENT)
        val DIGITAL_ASSETS = setOf(CRYPTOCURRENCY)
    }
}
```

#### 1.3 Extend UserPreferences for Display Currency
**File**: `app/src/main/java/com/axeven/profiteerapp/data/model/UserPreferences.kt`

```kotlin
data class UserPreferences(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val defaultCurrency: String = "USD", // Used for all wallet operations
    val displayCurrency: String = "USD", // Used for balance display conversion
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
) {
    constructor() : this("", "", "USD", "USD", null, null)
}
```

### Phase 2: Repository Layer Updates

#### 2.1 Update WalletRepository
**File**: `app/src/main/java/com/axeven/profiteerapp/data/repository/WalletRepository.kt`

**Changes**:
- Remove currency parameters from wallet creation methods
- Update wallet validation to remove currency checks
- Simplify balance calculation methods by removing currency conversion
- Update transfer validation to remove currency matching requirements

#### 2.2 Update CurrencyRateRepository
**File**: `app/src/main/java/com/axeven/profiteerapp/data/repository/CurrencyRateRepository.kt`

**Changes**:
- Focus currency rate methods on display conversion only
- Add method to get display rate from default currency to display currency
- Simplify rate lookup logic since it's only needed for display

#### 2.3 Update UserPreferencesRepository
**File**: `app/src/main/java/com/axeven/profiteerapp/data/repository/UserPreferencesRepository.kt`

**Changes**:
- Add methods to manage `displayCurrency` setting
- Ensure default currency is properly propagated to new wallets

### Phase 3: ViewModel Updates

#### 3.1 Update WalletListViewModel
**File**: `app/src/main/java/com/axeven/profiteerapp/viewmodel/WalletListViewModel.kt`

**Changes**:
- Remove currency selection from wallet creation UI state
- Update wallet creation to use default currency automatically
- Simplify balance aggregation by removing per-wallet currency conversion
- Update physical form validation to remove currency restrictions

#### 3.2 Update WalletDetailViewModel
**File**: `app/src/main/java/com/axeven/profiteerapp/viewmodel/WalletDetailViewModel.kt`

**Changes**:
- Remove `useWalletCurrency` flag from UI state
- Update balance display to use user's display currency preference
- Simplify transaction calculations without currency conversion

#### 3.3 Update HomeViewModel
**File**: `app/src/main/java/com/axeven/profiteerapp/viewmodel/HomeViewModel.kt`

**Changes**:
- Simplify balance aggregation logic
- Use display currency for balance presentation
- Remove currency-specific warnings and validations

#### 3.4 Update TransactionViewModel
**File**: `app/src/main/java/com/axeven/profiteerapp/viewmodel/TransactionViewModel.kt`

**Changes**:
- Remove currency validation from transfer transactions
- Simplify wallet selection logic by removing currency matching
- Update transaction processing to work with single default currency

### Phase 4: UI Layer Updates

#### 4.1 Update WalletListScreen
**File**: `app/src/main/java/com/axeven/profiteerapp/ui/wallet/WalletListScreen.kt`

**Changes**:
- Remove currency selection dropdowns from wallet creation forms
- Update wallet creation dialogs to focus on name and physical form only
- Remove currency-related validation messages
- Simplify physical form selector to remove currency restrictions

#### 4.2 Update TransactionScreens
**Files**: 
- `app/src/main/java/com/axeven/profiteerapp/ui/transaction/CreateTransactionScreen.kt`
- `app/src/main/java/com/axeven/profiteerapp/ui/transaction/EditTransactionScreen.kt`

**Changes**:
- Remove currency display from wallet selection
- Update transfer validation UI to remove currency mismatch warnings
- Simplify wallet selection dropdowns

#### 4.3 Update WalletDetailScreen
**File**: `app/src/main/java/com/axeven/profiteerapp/ui/wallet/WalletDetailScreen.kt`

**Changes**:
- Remove currency toggle functionality
- Display balances in user's preferred display currency
- Update balance formatting to use display currency

#### 4.4 Update SettingsScreen
**File**: `app/src/main/java/com/axeven/profiteerapp/ui/settings/SettingsScreen.kt`

**Changes**:
- Add display currency selection option
- Keep default currency setting for system operations
- Update currency rate management UI to focus on display conversion

### Phase 5: Utility Updates

#### 5.1 Update NumberFormatter
**File**: `app/src/main/java/com/axeven/profiteerapp/utils/NumberFormatter.kt`

**Changes**:
- Simplify formatting methods to use consistent currency
- Add display currency formatting methods
- Remove currency-specific formatting logic

#### 5.2 Update WalletValidator
**File**: `app/src/main/java/com/axeven/profiteerapp/utils/WalletValidator.kt`

**Changes**:
- Remove currency validation methods
- Update physical form validation to remove currency restrictions
- Simplify wallet name and balance validation

### Phase 6: Migration Strategy

#### 6.1 Data Migration Approach
Since this is a breaking change to the data model, implement a migration strategy:

1. **Backup Strategy**: Ensure all existing data is backed up before migration
2. **Field Migration**: 
   - Remove `currency` field from existing Wallet documents
   - Convert existing wallet balances to default currency using current rates
   - Update PhysicalForm values to remove currency restrictions
3. **User Communication**: Notify users about the change and its benefits
4. **Rollback Plan**: Maintain ability to restore previous data model if needed

#### 6.2 Migration Implementation
**New File**: `app/src/main/java/com/axeven/profiteerapp/data/migration/CurrencyMigration.kt`

```kotlin
class CurrencyMigration(
    private val walletRepository: WalletRepository,
    private val currencyRateRepository: CurrencyRateRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend fun migrateToDrupalSingleCurrency(userId: String) {
        // Migration logic to convert existing multi-currency wallets
        // to single default currency system
    }
}
```

### Phase 7: Testing Strategy

#### 7.1 Unit Tests
- Test wallet creation without currency parameter
- Test balance calculations with single currency
- Test transfer validation without currency restrictions
- Test display currency conversion

#### 7.2 Integration Tests
- Test wallet-transaction flow with new currency model
- Test balance aggregation across wallet types
- Test currency rate display conversion

#### 7.3 UI Tests
- Test wallet creation forms without currency selection
- Test transaction forms with simplified wallet selection
- Test settings screen with display currency option

## Benefits of This Change

### 1. Simplified User Experience
- No need to manage currencies per wallet
- Easier wallet creation process
- No currency mismatch errors in transfers

### 2. Cleaner Architecture
- Reduced complexity in data models
- Simplified validation logic
- Cleaner separation between storage and display

### 3. Better Performance
- Fewer currency conversion calculations
- Simplified database queries
- Reduced memory usage in ViewModels

### 4. Enhanced Flexibility
- Physical forms no longer restricted by currency
- Transfers possible between any same-type wallets
- Easier to add new physical forms

### 5. Maintainability
- Fewer edge cases to handle
- Cleaner code in currency-related operations
- Easier to understand and modify

## Risk Assessment

### 1. Data Loss Risk
**Risk**: Migration could cause data corruption
**Mitigation**: Comprehensive backup and testing strategy

### 2. User Confusion
**Risk**: Users might be confused by the change
**Mitigation**: Clear communication and documentation

### 3. Currency Rate Dependencies
**Risk**: Display conversion might fail without proper rates
**Mitigation**: Robust fallback to default currency display

## Success Criteria

1. **Functional**: All wallets operate with single default currency
2. **Performance**: Balance calculations are faster and simpler
3. **User Experience**: Wallet creation and transfers are easier
4. **Data Integrity**: No data loss during migration
5. **Code Quality**: Reduced complexity in currency-related code

## Timeline

- **Phase 1-2 (Data & Repository)**: 2-3 days
- **Phase 3 (ViewModels)**: 2-3 days  
- **Phase 4 (UI)**: 3-4 days
- **Phase 5 (Utilities)**: 1 day
- **Phase 6 (Migration)**: 2-3 days
- **Phase 7 (Testing)**: 2-3 days

**Total Estimated Time**: 12-17 days

## Future Considerations

1. **Multi-Currency Support**: If needed in future, implement as display-only feature
2. **Currency Analytics**: Track portfolio performance across different currency displays
3. **Exchange Rate Automation**: Integrate with external APIs for real-time rates
4. **Advanced Display Options**: Allow per-screen currency display preferences

---

*This PRP represents a significant simplification of the Profiteer currency system, focusing on user experience and architectural clarity while maintaining the flexibility to display balances in different currencies.*