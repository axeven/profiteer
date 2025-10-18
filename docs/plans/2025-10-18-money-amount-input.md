# Money Amount Input Enhancement Plan

**Status**: Planning
**Priority**: High
**Created**: 2025-10-18
**Category**: UX/UI Enhancement
**Approach**: Test-Driven Development (TDD)

## Executive Summary

**PRIMARY PAIN POINT**: All money amount input fields show the full QWERTY keyboard instead of the numeric keyboard, making it cumbersome for users to enter numbers on mobile devices.

This plan focuses on **adding `keyboardOptions` configuration** to all amount input fields to show the numeric keyboard by default, with optional input filtering as a bonus enhancement.

**TDD Workflow**: Write failing tests → Implement minimum code to pass tests → Refactor → Repeat

## Problem Statement

### Primary Issue: Wrong Keyboard Type

**Core Problem**: Mobile users see the QWERTY keyboard when tapping amount fields, requiring them to manually switch to the numeric keyboard.

**Root Cause**: All `OutlinedTextField` components for amount input lack `keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)` configuration.

### Current Issues (Priority Order)

1. ❗**CRITICAL - No Keyboard Type Configuration**: Amount TextFields show QWERTY keyboard instead of numeric keyboard
2. **Secondary - No Input Filtering**: Users can type letters/symbols, which are caught by validation later
3. **Nice-to-Have - Currency Decimal Enforcement**: Decimal limits not enforced during input

### Impact

- 🔴 **Critical UX Issue**: Users must manually switch keyboard on every amount field (adds friction)
- 🟡 **Moderate**: Post-validation catches invalid input but doesn't prevent it
- 🟢 **Minor**: Decimal precision validated at submit time

## Current Implementation Analysis

### Affected Files

| File | Lines | Component | Current Behavior |
|------|-------|-----------|------------------|
| `ui/transaction/CreateTransactionScreen.kt` | 149-172 | Amount TextField | Free text, no keyboard config |
| `ui/transaction/EditTransactionScreen.kt` | 189-212 | Amount TextField | Free text, no keyboard config |
| `ui/wallet/WalletListScreen.kt` | 594-604 | Physical Wallet Balance | Free text, parseDouble() validation |
| `ui/wallet/WalletListScreen.kt` | 732-742 | Logical Wallet Balance | Free text, parseDouble() validation |
| `ui/wallet/WalletListScreen.kt` | 866-876 | Edit Physical Balance | Free text, parseDouble() validation |
| `ui/wallet/WalletListScreen.kt` | 999-1009 | Edit Logical Balance | Free text, parseDouble() validation |
| `ui/settings/SettingsScreen.kt` | 519-529 | Currency Rate Input | Free text, no validation shown |

### Current Validation Logic

**Location**: `ui/transaction/TransactionFormValidator.kt`

```kotlin
fun validateAmount(amount: String): String? {
    return when {
        amount.isBlank() -> "Amount is required"
        amount.toDoubleOrNull() == null -> "Amount must be a valid number"
        amount.toDoubleOrNull()?.let { it <= 0 } == true -> "Amount must be greater than 0"
        else -> null
    }
}
```

**Issues**:
- Uses basic `toDoubleOrNull()` instead of `NumberFormatter.parseDouble()`
- No currency-specific decimal place enforcement
- No thousands separator handling

### Currency Decimal Requirements

From `NumberFormatter.kt`:

| Currency | Decimal Places | Use Case |
|----------|----------------|----------|
| BTC | 8 | Bitcoin amounts (satoshi precision) |
| GOLD | 3 | Gold weight in grams |
| JPY | 0 | Japanese Yen (no fractional currency) |
| Standard | 2 | USD, EUR, GBP, CAD, AUD, IDR |

## Solution Design

### Goals (Priority Order)

1. ✅ **PRIMARY**: Add `KeyboardType.Decimal` to all amount input fields (solves the main pain point)
2. ✅ **SECONDARY**: Add optional input filtering to prevent invalid characters (nice-to-have)
3. ✅ **BONUS**: Enforce currency-specific decimal places during input (enhancement)
4. ✅ Maintain existing validation error messaging
5. ✅ Preserve consolidated state management pattern

### Simplified Architecture

Since the main pain point is just the keyboard type, we have **two approach options**:

#### Option A: Minimal Solution (Keyboard Type Only)
**Fastest path to solve the pain point**:
- Add `keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)` to all 7 amount TextFields
- No new components needed
- No input filtering
- Users still see numeric keyboard ✅
- Users can still type letters (but validation catches it)

**Pros**:
- Solves the primary UX issue immediately
- Minimal code changes
- No new testing needed (keyboard type is UI behavior)
- Can be done in ~30 minutes

**Cons**:
- Doesn't prevent invalid input (users can still type letters if keyboard allows)
- No currency-specific decimal enforcement

#### Option B: Enhanced Solution (Keyboard Type + Input Filtering)
**Complete solution with bonus features**:
- Add `KeyboardType.Decimal` to all amount fields ✅
- Create `AmountInputFilter` for real-time filtering (DONE in Phase 1) ✅
- Create reusable `AmountTextField` component with both features
- Currency-specific decimal limits enforced

**Pros**:
- Solves keyboard issue ✅
- Prevents invalid input entirely
- Better UX with real-time feedback
- Reusable component for future fields

**Cons**:
- More implementation time
- Requires comprehensive testing
- Phase 1 already completed (AmountInputFilter exists)

### Recommended Approach: **Option B** (Keep Current Plan)

**Rationale**: Since we've already completed Phase 1 (AmountInputFilter), we should continue with the enhanced solution. The keyboard type fix is simple to add alongside the filtering.

### Proposed Architecture

#### 1. Create Reusable Amount Input Component

**New File**: `ui/components/AmountTextField.kt`

```kotlin
/**
 * Reusable TextField component for money amount input with automatic
 * number-only filtering and currency-specific decimal place enforcement.
 *
 * Features:
 * - Numeric keyboard (KeyboardType.Decimal)
 * - Real-time input filtering (digits and single decimal point only)
 * - Currency-specific decimal place limits
 * - Automatic currency symbol prefix
 * - Validation error display
 *
 * @param value Current amount value (String representation)
 * @param onValueChange Callback when value changes (returns filtered String)
 * @param currency Currency code for decimal place rules and symbol
 * @param label TextField label
 * @param modifier Composable modifier
 * @param isError Whether validation error exists
 * @param errorMessage Optional error message to display
 * @param placeholder Optional placeholder text
 */
@Composable
fun AmountTextField(
    value: String,
    onValueChange: (String) -> Unit,
    currency: String,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    placeholder: String? = null
)
```

#### 2. Create Input Filter Utility

**New File**: `utils/AmountInputFilter.kt`

```kotlin
/**
 * Filters and validates numeric input for money amounts.
 * Enforces currency-specific decimal place limits and numeric-only input.
 */
object AmountInputFilter {

    /**
     * Filters input to allow only valid numeric characters.
     * Enforces decimal place limits based on currency type.
     *
     * Rules:
     * - Only digits (0-9) and one decimal point allowed
     * - Decimal places limited by currency (BTC=8, GOLD=3, JPY=0, others=2)
     * - Multiple decimal points prevented
     * - Leading zeros handled gracefully
     *
     * @param input Raw user input
     * @param currentValue Current field value
     * @param currency Currency code for decimal rules
     * @return Filtered string with only valid numeric characters
     */
    fun filterAmountInput(
        input: String,
        currentValue: String,
        currency: String
    ): String

    /**
     * Gets maximum decimal places allowed for currency.
     * Should match NumberFormatter.getDecimalPlaces() logic.
     */
    private fun getMaxDecimalPlaces(currency: String): Int

    /**
     * Validates if decimal point is allowed at this position.
     */
    private fun canAddDecimalPoint(value: String): Boolean

    /**
     * Handles leading zeros (e.g., "00.5" -> "0.5").
     */
    private fun normalizeLeadingZeros(value: String): String
}
```

#### 3. Enhanced Validation with NumberFormatter

**Update**: `ui/transaction/TransactionFormValidator.kt`

```kotlin
/**
 * Validates amount using NumberFormatter.parseDouble() instead of toDoubleOrNull().
 * Handles thousands separators and currency-specific formatting.
 */
fun validateAmount(amount: String, currency: String = "USD"): String? {
    return when {
        amount.isBlank() -> "Amount is required"
        NumberFormatter.parseDouble(amount) == null -> "Amount must be a valid number"
        NumberFormatter.parseDouble(amount)?.let { it <= 0 } == true -> "Amount must be greater than 0"
        !isValidDecimalPlaces(amount, currency) -> "Invalid decimal places for $currency"
        else -> null
    }
}

/**
 * Validates decimal places match currency requirements.
 */
private fun isValidDecimalPlaces(amount: String, currency: String): Boolean {
    val parsed = NumberFormatter.parseDouble(amount) ?: return false
    val decimalPlaces = amount.substringAfter('.', "").length
    val maxDecimals = when (currency) {
        "BTC" -> 8
        "GOLD" -> 3
        "JPY" -> 0
        else -> 2
    }
    return decimalPlaces <= maxDecimals
}
```

### Implementation Approach

#### Phase 1: Foundation (Core Utilities)

**Tasks**:
1. Create `AmountInputFilter.kt` with input filtering logic
2. Add comprehensive unit tests for filter edge cases
3. Update `TransactionFormValidator.kt` to use `NumberFormatter.parseDouble()`
4. Add decimal place validation tests

**Edge Cases to Test**:
- Multiple decimal points ("1.2.3" -> "1.2")
- Leading zeros ("00.5" -> "0.5")
- Just decimal point (".")
- Empty string after backspace
- Pasting invalid text
- Currency-specific limits (BTC 8 decimals, JPY 0 decimals)

#### Phase 2: Reusable Component

**Tasks**:
1. Create `AmountTextField.kt` composable component
2. Integrate `AmountInputFilter` with real-time filtering
3. Add `KeyboardType.Decimal` configuration
4. Add currency symbol prefix using `NumberFormatter.getCurrencySymbol()`
5. Add UI tests for component behavior
6. Create component documentation with usage examples

**Component Features**:
- Automatic numeric keyboard
- Real-time character filtering
- Currency symbol as prefix
- Error state styling
- Supporting text for errors
- Placeholder text support

#### Phase 3: Transaction Screens Migration

**Files to Update**:
1. `ui/transaction/CreateTransactionScreen.kt` (Lines 149-172)
2. `ui/transaction/EditTransactionScreen.kt` (Lines 189-212)

**Changes**:
- Replace `OutlinedTextField` with `AmountTextField`
- Remove manual `prefix` configuration (handled by component)
- Remove manual `isError` logic (handled by component)
- Pass currency from transaction state
- Update state update logic to work with filtered input

**Before**:
```kotlin
OutlinedTextField(
    value = transactionState.amount,
    onValueChange = { newAmount ->
        transactionState = updateAmount(transactionState, newAmount)
    },
    label = { Text("Amount") },
    modifier = Modifier.fillMaxWidth(),
    prefix = { Text(currencySymbol) },
    isError = transactionState.validationErrors.amountError != null,
    supportingText = transactionState.validationErrors.amountError?.let {
        { Text(it, color = MaterialTheme.colorScheme.error) }
    }
)
```

**After**:
```kotlin
AmountTextField(
    value = transactionState.amount,
    onValueChange = { filtered ->
        transactionState = updateAmount(transactionState, filtered)
    },
    currency = transactionState.selectedCurrency, // From state
    label = if (transactionState.selectedType == TransactionType.TRANSFER)
        "Transfer Amount" else "Amount",
    modifier = Modifier.fillMaxWidth(),
    isError = transactionState.validationErrors.amountError != null,
    errorMessage = transactionState.validationErrors.amountError
)
```

#### Phase 4: Wallet Screens Migration

**Files to Update**:
1. `ui/wallet/WalletListScreen.kt` (4 locations: Lines 594-604, 732-742, 866-876, 999-1009)

**Changes**:
- Replace `OutlinedTextField` with `AmountTextField`
- Remove inline `NumberFormatter.parseDouble()` validation
- Use component's built-in error handling
- Pass wallet currency for decimal rules

**Note**: WalletListScreen has 4 separate TextField instances for:
- Create Physical Wallet balance
- Create Logical Wallet balance
- Edit Physical Wallet balance
- Edit Logical Wallet balance

All four should be migrated to use the same `AmountTextField` component.

#### Phase 5: Settings Screen Migration

**Files to Update**:
1. `ui/settings/SettingsScreen.kt` (Lines 519-529)

**Special Considerations**:
- Currency rate input has different validation rules
- May need `allowLargeNumbers` parameter for rates like BTC exchange rates
- May need separate component or parameter: `RateInputField`?

**Decision Point**: Should currency rates use the same component with a flag, or a separate specialized component?

Recommendation: Add `inputMode` parameter to `AmountTextField`:
```kotlin
enum class AmountInputMode {
    CURRENCY_AMOUNT,  // Standard amount with currency decimals
    EXCHANGE_RATE     // Allows more decimals for rates
}
```

#### Phase 6: Testing & Validation

**Test Coverage Required**:

1. **Unit Tests** (`AmountInputFilterTest.kt`):
   - Filter behavior for each currency type
   - Edge cases (multiple decimals, leading zeros, etc.)
   - Paste handling with invalid characters
   - Empty string and backspace scenarios

2. **Unit Tests** (`TransactionFormValidatorTest.kt`):
   - Amount validation with NumberFormatter
   - Decimal place validation per currency
   - Negative and zero amount handling

3. **UI Tests** (`AmountTextFieldTest.kt`):
   - Keyboard type is numeric
   - Input filtering prevents invalid characters
   - Error states display correctly
   - Currency symbol prefix shows
   - State updates propagate correctly

4. **Integration Tests**:
   - Transaction creation with filtered amounts
   - Wallet creation with filtered balances
   - Amount editing preserves decimal rules
   - Currency switching updates decimal limits

### Optional Enhancements (Future Consideration)

#### Thousands Separator Input (Low Priority)

Currently, `NumberFormatter.formatCurrency()` adds thousands separators for display only. We could add real-time separator insertion during input:

- Input: "1000000" → Display: "1,000,000"
- Requires more complex filtering logic
- Must handle backspace correctly (remove digit or separator?)
- Locale-specific separators (US uses comma, EU uses period)

**Recommendation**: Defer to future enhancement. Current plan focuses on number-only input without separators.

#### Visual Feedback Improvements

- Shake animation on invalid input attempt
- Color change when maximum decimals reached
- Tooltip showing decimal limits per currency
- Auto-truncate decimals on currency switch

## TDD Implementation Checklist

**TDD Principle**: ❌ Write Test → ❌ Run & Fail → ✅ Implement → ✅ Pass → ♻️ Refactor

### Phase 1: AmountInputFilter (Core Utility with TDD) ✅

**Progress**: 31/31 tasks completed (100%)
**Status**: ✅ COMPLETED (2025-10-18)

#### Step 1.1: Write Tests for Basic Input Filtering ✅
- [x] ❌ **TEST**: Create `app/src/test/java/com/axeven/profiteerapp/utils/AmountInputFilterTest.kt`
- [x] ❌ **TEST**: Write `filterAmountInput returns empty string when input is empty`
- [x] ❌ **TEST**: Write `filterAmountInput allows digits only - input "123" returns "123"`
- [x] ❌ **TEST**: Write `filterAmountInput filters out letters - input "1a2b3c" returns "123"`
- [x] ❌ **TEST**: Write `filterAmountInput filters out symbols - input "1$2@3#" returns "123"`
- [x] ⚫ **RUN**: Execute tests → FAILED ✅ (as expected - TDD RED phase)
- [x] ⚫ **IMPL**: Create `app/src/main/java/com/axeven/profiteerapp/utils/AmountInputFilter.kt`
- [x] ⚫ **IMPL**: Implement basic `filterAmountInput()` to filter digits only
- [x] ✅ **RUN**: Execute tests → PASSED ✅ (TDD GREEN phase)
- [x] ♻️ **REFACTOR**: Clean up code - optimized multiple decimal handling

#### Step 1.2: Write Tests for Decimal Point Handling ✅
- [x] ❌ **TEST**: Write `filterAmountInput allows single decimal point - input "1.5" returns "1.5"`
- [x] ❌ **TEST**: Write `filterAmountInput prevents multiple decimals - input "1.2.3" returns "1.2"`
- [x] ❌ **TEST**: Write `filterAmountInput allows just decimal point - input "." returns "."`
- [x] ❌ **TEST**: Write `filterAmountInput allows decimal at start - input ".5" returns ".5"`
- [x] ⚫ **RUN**: Execute tests → FAILED ✅ (TDD RED phase)
- [x] ⚫ **IMPL**: Add decimal point logic to `filterAmountInput()`
- [x] ⚫ **IMPL**: Implement `canAddDecimalPoint()` helper function
- [x] ✅ **RUN**: Execute tests → PASSED ✅ (TDD GREEN phase)
- [x] ♻️ **REFACTOR**: Improved decimal rejection logic to return currentValue

#### Step 1.3: Write Tests for Leading Zero Normalization ✅
- [x] ❌ **TEST**: Write `normalizeLeadingZeros converts "00.5" to "0.5"`
- [x] ❌ **TEST**: Write `normalizeLeadingZeros converts "007" to "7"`
- [x] ❌ **TEST**: Write `normalizeLeadingZeros keeps "0" as "0"`
- [x] ❌ **TEST**: Write `normalizeLeadingZeros keeps "0.5" as "0.5"`
- [x] ⚫ **RUN**: Execute tests → FAILED ✅ (TDD RED phase)
- [x] ⚫ **IMPL**: Implement `normalizeLeadingZeros()` function
- [x] ⚫ **IMPL**: Integrate into `filterAmountInput()`
- [x] ✅ **RUN**: Execute tests → PASSED ✅ (TDD GREEN phase)
- [x] ♻️ **REFACTOR**: Optimized logic with proper edge case handling

#### Step 1.4: Write Tests for Currency-Specific Decimal Places ✅
- [x] ❌ **TEST**: Write `USD allows up to 2 decimal places - "1.12" allowed, "1.123" blocked`
- [x] ❌ **TEST**: Write `BTC allows up to 8 decimal places - "1.12345678" allowed, "1.123456789" blocked`
- [x] ❌ **TEST**: Write `GOLD allows up to 3 decimal places - "1.123" allowed, "1.1234" blocked`
- [x] ❌ **TEST**: Write `JPY prevents any decimal point - "100" allowed, "100." blocked`
- [x] ❌ **TEST**: Write `EUR allows up to 2 decimal places (standard currency test)`
- [x] ⚫ **RUN**: Execute tests → FAILED ✅ (TDD RED phase)
- [x] ⚫ **IMPL**: Implement `getMaxDecimalPlaces()` function with currency rules
- [x] ⚫ **IMPL**: Add decimal place enforcement to `filterAmountInput()`
- [x] ✅ **RUN**: Execute tests → PASSED ✅ (TDD GREEN phase)
- [x] ♻️ **REFACTOR**: No refactoring needed - code is clean

**Phase 1 Checkpoint**: ✅ PASSED - All 25 tests passing (`./gradlew testDebugUnitTest --tests AmountInputFilterTest`)

**Implementation Notes**:
- Created 25 unit tests covering all edge cases (4 bonus tests for comprehensive coverage)
- Implemented complete AmountInputFilter with all required functionality
- All tests pass on first checkpoint run
- Code includes comprehensive KDoc documentation
- Filter handles: digits, decimals, leading zeros, currency-specific decimal limits, negative numbers, spaces
- Follows TDD Red-Green-Refactor cycle strictly

---

### Phase 2: Add Keyboard Type to Existing Fields (SOLVES PRIMARY PAIN POINT) ✅

**Progress**: 14/14 tasks completed (100%)
**Status**: ✅ COMPLETED (2025-10-18)
**Goal**: Add `KeyboardType.Decimal` to all 7 amount input fields
**Impact**: Users see numeric keyboard instead of QWERTY keyboard

#### Step 2.1: Update Transaction Screens ✅
- [x] 📝 **IMPL**: Update `CreateTransactionScreen.kt` amount TextField (line 154)
  - [x] Add import: `androidx.compose.foundation.text.KeyboardOptions`
  - [x] Add import: `androidx.compose.ui.text.input.KeyboardType`
  - [x] Add import: `androidx.compose.ui.text.input.ImeAction`
  - [x] Add parameter: `keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done)`
- [x] 📝 **IMPL**: Update `EditTransactionScreen.kt` amount TextField (line 197)
  - [x] Add same imports as above
  - [x] Add parameter: `keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done)`

#### Step 2.2: Update Wallet Screens (4 TextFields) ✅
- [x] 📝 **IMPL**: Update `WalletListScreen.kt` - Create Physical Wallet balance
  - [x] Add keyboard imports
  - [x] Add parameter: `keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done)`
- [x] 📝 **IMPL**: Update `WalletListScreen.kt` - Create Logical Wallet balance
  - [x] Add parameter: `keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done)`
- [x] 📝 **IMPL**: Update `WalletListScreen.kt` - Edit Physical Wallet balance
  - [x] Add parameter: `keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done)`
- [x] 📝 **IMPL**: Update `WalletListScreen.kt` - Edit Logical Wallet balance
  - [x] Add parameter: `keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done)`

#### Step 2.3: Update Settings Screen ✅
- [x] 📝 **IMPL**: Update `SettingsScreen.kt` - Currency rate input (2 fields)
  - [x] Add keyboard imports
  - [x] Add parameter: `keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done)`

#### Step 2.4: Build Verification ✅
- [x] ✅ **BUILD**: Run `./gradlew assembleDebug` → BUILD SUCCESSFUL

#### Step 2.5: Manual Testing & Verification (Ready for testing)
- [ ] ✅ **MANUAL TEST**: Build and run app on device/emulator
- [ ] ✅ **MANUAL TEST**: Tap amount field in CreateTransactionScreen → Verify numeric keyboard appears
- [ ] ✅ **MANUAL TEST**: Tap amount field in EditTransactionScreen → Verify numeric keyboard appears
- [ ] ✅ **MANUAL TEST**: Tap balance field in Create Physical Wallet dialog → Verify numeric keyboard
- [ ] ✅ **MANUAL TEST**: Tap balance field in Create Logical Wallet dialog → Verify numeric keyboard
- [ ] ✅ **MANUAL TEST**: Tap balance field in Edit Physical Wallet dialog → Verify numeric keyboard
- [ ] ✅ **MANUAL TEST**: Tap balance field in Edit Logical Wallet dialog → Verify numeric keyboard
- [ ] ✅ **MANUAL TEST**: Tap rate field in Settings screen → Verify numeric keyboard
- [ ] ✅ **MANUAL TEST**: Verify decimal point key is available on numeric keyboard
- [ ] ✅ **MANUAL TEST**: Verify "Done" action button works correctly

**Phase 2 Checkpoint**: ✅ IMPLEMENTATION COMPLETE - All 7 amount fields updated with keyboard configuration

**PRIMARY PAIN POINT SOLVED**: Users will see numeric keyboard instead of QWERTY for amount inputs! 🎉

**Implementation Summary**:
- Modified 3 files: CreateTransactionScreen.kt, EditTransactionScreen.kt, WalletListScreen.kt, SettingsScreen.kt
- Added keyboard imports to all files
- Updated 7 TextField instances with `keyboardOptions` parameter
- Build verified successfully
- Ready for manual testing on device/emulator

---

### Phase 3: AmountTextField Component (TDD)

**Progress**: 0/28 tasks completed

#### Step 3.1: Write Tests for Component Structure
- [ ] ❌ **TEST**: Create `app/src/androidTest/java/com/axeven/profiteerapp/ui/components/AmountTextFieldTest.kt`
- [ ] ❌ **TEST**: Write `amountTextField displays label correctly`
- [ ] ❌ **TEST**: Write `amountTextField displays placeholder when empty`
- [ ] ❌ **TEST**: Write `amountTextField shows currency symbol prefix for USD`
- [ ] ❌ **TEST**: Write `amountTextField shows currency symbol prefix for EUR`
- [ ] ❌ **TEST**: Write `amountTextField shows currency symbol prefix for BTC`
- [ ] ⚫ **RUN**: Execute UI tests → Should FAIL (component doesn't exist)
- [ ] ⚫ **IMPL**: Create `app/src/main/java/com/axeven/profiteerapp/ui/components/AmountTextField.kt`
- [ ] ⚫ **IMPL**: Create basic Composable structure with OutlinedTextField
- [ ] ⚫ **IMPL**: Add label, placeholder, and prefix parameters
- [ ] ⚫ **IMPL**: Integrate `NumberFormatter.getCurrencySymbol()` for prefix
- [ ] ✅ **RUN**: Execute UI tests → Should PASS
- [ ] ♻️ **REFACTOR**: Extract symbol logic if needed

#### Step 3.2: Write Tests for Input Filtering
- [ ] ❌ **TEST**: Write `amountTextField filters out letters when typing`
- [ ] ❌ **TEST**: Write `amountTextField filters out symbols when typing`
- [ ] ❌ **TEST**: Write `amountTextField prevents multiple decimal points`
- [ ] ❌ **TEST**: Write `amountTextField enforces USD 2 decimal limit`
- [ ] ❌ **TEST**: Write `amountTextField enforces BTC 8 decimal limit`
- [ ] ❌ **TEST**: Write `amountTextField enforces JPY 0 decimal limit`
- [ ] ⚫ **RUN**: Execute UI tests → Should FAIL
- [ ] ⚫ **IMPL**: Integrate `AmountInputFilter` in `onValueChange` callback
- [ ] ⚫ **IMPL**: Pass currency parameter to filter
- [ ] ✅ **RUN**: Execute UI tests → Should PASS
- [ ] ♻️ **REFACTOR**: Optimize filter integration

#### Step 3.3: Write Tests for Keyboard Configuration
- [ ] ❌ **TEST**: Write `amountTextField uses decimal keyboard type`
- [ ] ❌ **TEST**: Write `amountTextField uses done IME action by default`
- [ ] ⚫ **RUN**: Execute tests → Should FAIL
- [ ] ⚫ **IMPL**: Add `keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)`
- [ ] ✅ **RUN**: Execute tests → Should PASS

#### Step 3.4: Write Tests for Error Handling
- [ ] ❌ **TEST**: Write `amountTextField shows error state when isError is true`
- [ ] ❌ **TEST**: Write `amountTextField displays error message in supporting text`
- [ ] ❌ **TEST**: Write `amountTextField applies error color to supporting text`
- [ ] ⚫ **RUN**: Execute tests → Should FAIL
- [ ] ⚫ **IMPL**: Add error state and supporting text logic
- [ ] ✅ **RUN**: Execute tests → Should PASS
- [ ] ♻️ **REFACTOR**: Clean up error handling

**Phase 3 Checkpoint**: Run `./gradlew connectedAndroidTest` → All AmountTextField UI tests should PASS

---

### Phase 4: Transaction Screens Migration (TDD)

**Progress**: 0/16 tasks completed

#### Step 4.1: Update CreateTransactionScreen Tests
- [ ] ❌ **TEST**: Update existing `CreateTransactionScreenTest.kt` if exists, or create tests
- [ ] ❌ **TEST**: Write `create transaction screen shows numeric keyboard for amount field`
- [ ] ❌ **TEST**: Write `create transaction screen filters invalid characters in amount`
- [ ] ❌ **TEST**: Write `create transaction screen shows USD currency symbol`
- [ ] ❌ **TEST**: Write `create transaction screen enforces decimal limits for selected currency`
- [ ] ⚫ **RUN**: Execute tests → Should FAIL (still using OutlinedTextField)
- [ ] ⚫ **IMPL**: Update `CreateTransactionScreen.kt` lines 149-172
- [ ] ⚫ **IMPL**: Replace `OutlinedTextField` with `AmountTextField`
- [ ] ⚫ **IMPL**: Remove manual prefix configuration
- [ ] ⚫ **IMPL**: Pass `transactionState.selectedCurrency` to component
- [ ] ⚫ **IMPL**: Update label logic for TRANSFER type
- [ ] ✅ **RUN**: Execute tests → Should PASS
- [ ] ♻️ **REFACTOR**: Simplify state handling if possible

#### Step 4.2: Update EditTransactionScreen Tests
- [ ] ❌ **TEST**: Update existing `EditTransactionScreenTest.kt` or create tests
- [ ] ❌ **TEST**: Write `edit transaction screen shows numeric keyboard for amount field`
- [ ] ❌ **TEST**: Write `edit transaction screen filters invalid characters in amount`
- [ ] ⚫ **RUN**: Execute tests → Should FAIL
- [ ] ⚫ **IMPL**: Update `EditTransactionScreen.kt` lines 189-212
- [ ] ⚫ **IMPL**: Replace `OutlinedTextField` with `AmountTextField`
- [ ] ⚫ **IMPL**: Pass `selectedCurrency` from state
- [ ] ✅ **RUN**: Execute tests → Should PASS

**Phase 4 Checkpoint**:
- [ ] ✅ Run `./gradlew testDebugUnitTest` → All unit tests PASS
- [ ] ✅ Manual test: Create transaction with amount input → Numeric keyboard shows
- [ ] ✅ Manual test: Type "1a2b3" → Should show "123"

---

### Phase 5: Wallet Screens Migration (TDD)

**Progress**: 0/24 tasks completed

#### Step 5.1: Create Physical Wallet Balance Tests
- [ ] ❌ **TEST**: Update/create `WalletListScreenTest.kt`
- [ ] ❌ **TEST**: Write `create physical wallet dialog shows numeric keyboard for balance`
- [ ] ❌ **TEST**: Write `create physical wallet enforces decimal limits for currency`
- [ ] ❌ **TEST**: Write `create physical wallet shows correct currency symbol`
- [ ] ⚫ **RUN**: Execute tests → Should FAIL
- [ ] ⚫ **IMPL**: Update `WalletListScreen.kt` lines 594-604
- [ ] ⚫ **IMPL**: Replace `OutlinedTextField` with `AmountTextField`
- [ ] ⚫ **IMPL**: Remove inline `NumberFormatter.parseDouble()` validation
- [ ] ⚫ **IMPL**: Pass physical wallet currency
- [ ] ✅ **RUN**: Execute tests → Should PASS

#### Step 5.2: Create Logical Wallet Balance Tests
- [ ] ❌ **TEST**: Write `create logical wallet dialog shows numeric keyboard for balance`
- [ ] ❌ **TEST**: Write `create logical wallet enforces decimal limits for currency`
- [ ] ⚫ **RUN**: Execute tests → Should FAIL
- [ ] ⚫ **IMPL**: Update `WalletListScreen.kt` lines 732-742
- [ ] ⚫ **IMPL**: Replace `OutlinedTextField` with `AmountTextField`
- [ ] ⚫ **IMPL**: Pass logical wallet currency
- [ ] ✅ **RUN**: Execute tests → Should PASS

#### Step 5.3: Edit Physical Wallet Balance Tests
- [ ] ❌ **TEST**: Write `edit physical wallet dialog shows numeric keyboard for balance`
- [ ] ❌ **TEST**: Write `edit physical wallet enforces decimal limits for currency`
- [ ] ⚫ **RUN**: Execute tests → Should FAIL
- [ ] ⚫ **IMPL**: Update `WalletListScreen.kt` lines 866-876
- [ ] ⚫ **IMPL**: Replace `OutlinedTextField` with `AmountTextField`
- [ ] ⚫ **IMPL**: Pass physical wallet currency
- [ ] ✅ **RUN**: Execute tests → Should PASS

#### Step 5.4: Edit Logical Wallet Balance Tests
- [ ] ❌ **TEST**: Write `edit logical wallet dialog shows numeric keyboard for balance`
- [ ] ❌ **TEST**: Write `edit logical wallet enforces decimal limits for currency`
- [ ] ⚫ **RUN**: Execute tests → Should FAIL
- [ ] ⚫ **IMPL**: Update `WalletListScreen.kt` lines 999-1009
- [ ] ⚫ **IMPL**: Replace `OutlinedTextField` with `AmountTextField`
- [ ] ⚫ **IMPL**: Pass logical wallet currency
- [ ] ✅ **RUN**: Execute tests → Should PASS

**Phase 5 Checkpoint**:
- [ ] ✅ Run `./gradlew testDebugUnitTest` → All tests PASS
- [ ] ✅ Manual test: Create wallet with BTC → 8 decimal limit enforced
- [ ] ✅ Manual test: Create wallet with JPY → No decimals allowed

---

### Phase 6: Settings Screen Migration (TDD)

**Progress**: 0/12 tasks completed

#### Step 6.1: Decide on Currency Rate Input Approach
- [ ] 📋 **DECISION**: Choose Option A (add `AmountInputMode` enum) or Option B (separate component)
- [ ] 📋 **DOCUMENT**: Add decision and rationale to "Decision Log" section below

#### Step 6.2: Write Tests for Currency Rate Input (Option A)
- [ ] ❌ **TEST**: Create/update `SettingsScreenTest.kt`
- [ ] ❌ **TEST**: Write `settings currency rate input shows numeric keyboard`
- [ ] ❌ **TEST**: Write `settings currency rate allows larger decimal places for BTC`
- [ ] ❌ **TEST**: Write `settings currency rate accepts large numbers like 45000.00`
- [ ] ⚫ **RUN**: Execute tests → Should FAIL
- [ ] ⚫ **IMPL**: Add `AmountInputMode` enum to `AmountTextField.kt` (if Option A)
- [ ] ⚫ **IMPL**: Update `AmountTextField` to handle EXCHANGE_RATE mode
- [ ] ⚫ **IMPL**: Update `SettingsScreen.kt` lines 519-529
- [ ] ⚫ **IMPL**: Replace `OutlinedTextField` with `AmountTextField`
- [ ] ⚫ **IMPL**: Pass appropriate `inputMode` parameter
- [ ] ✅ **RUN**: Execute tests → Should PASS
- [ ] ♻️ **REFACTOR**: Clean up rate-specific logic

**Phase 6 Checkpoint**:
- [ ] ✅ Run `./gradlew testDebugUnitTest` → All tests PASS
- [ ] ✅ Manual test: Enter BTC exchange rate → Accepts large numbers

---

### Phase 7: Integration & Regression Testing

**Progress**: 0/20 tasks completed

#### Step 7.1: Full Test Suite Execution
- [ ] ✅ **RUN**: `./gradlew testDebugUnitTest`
- [ ] ✅ **VERIFY**: All unit tests pass (expected: 100% pass rate)
- [ ] ✅ **RUN**: `./gradlew connectedAndroidTest` (if device/emulator available)
- [ ] ✅ **VERIFY**: All instrumented tests pass

#### Step 7.2: Manual Testing - Currency Decimal Enforcement
- [ ] ✅ **MANUAL**: Create transaction with USD → Type "10.999" → Should show "10.99"
- [ ] ✅ **MANUAL**: Create transaction with BTC → Type "1.123456789" → Should show "1.12345678"
- [ ] ✅ **MANUAL**: Create transaction with GOLD → Type "5.1234" → Should show "5.123"
- [ ] ✅ **MANUAL**: Create transaction with JPY → Type "1000." → Should show "1000"
- [ ] ✅ **MANUAL**: Create transaction with EUR → Type "50.999" → Should show "50.99"

#### Step 7.3: Manual Testing - Input Filtering
- [ ] ✅ **MANUAL**: Type "1a2b3c" in amount field → Should show "123"
- [ ] ✅ **MANUAL**: Type "1$2@3#" in amount field → Should show "123"
- [ ] ✅ **MANUAL**: Type "1.2.3" in amount field → Should show "1.2"
- [ ] ✅ **MANUAL**: Paste "abc123def" → Should show "123"
- [ ] ✅ **MANUAL**: Type "00.5" → Should show "0.5"

#### Step 7.4: Manual Testing - Keyboard Behavior
- [ ] ✅ **MANUAL**: Tap amount field on mobile → Numeric keyboard appears (not QWERTY)
- [ ] ✅ **MANUAL**: Verify decimal point key is available on keyboard
- [ ] ✅ **MANUAL**: Verify keyboard shows "Done" action button

#### Step 7.5: Regression Testing - Existing Functionality
- [ ] ✅ **MANUAL**: Create full transaction → Should save successfully
- [ ] ✅ **MANUAL**: Edit existing transaction → Amount should load and update correctly
- [ ] ✅ **MANUAL**: Create wallet with balance → Should calculate correctly
- [ ] ✅ **MANUAL**: Verify NumberFormatter display still shows currency symbols
- [ ] ✅ **MANUAL**: Verify balance calculations unchanged

**Phase 7 Checkpoint**: All tests pass, no regressions detected

---

### Phase 8: Documentation & Cleanup

**Progress**: 0/10 tasks completed

#### Step 8.1: Code Documentation
- [ ] 📝 **DOC**: Add KDoc to `AmountInputFilter.kt` (all public functions)
- [ ] 📝 **DOC**: Add KDoc to `AmountTextField.kt` (Composable and parameters)
- [ ] 📝 **DOC**: Add inline comments for complex logic in filter
- [ ] 📝 **DOC**: Update `CLAUDE.md` with AmountTextField usage example
- [ ] 📝 **DOC**: Update `STATE_MANAGEMENT_GUIDELINES.md` if needed

#### Step 8.2: Code Cleanup
- [ ] 🧹 **CLEAN**: Remove unused TextField imports from updated screens
- [ ] 🧹 **CLEAN**: Remove redundant validation logic from WalletListScreen
- [ ] 🧹 **CLEAN**: Check for any TODOs and resolve or document
- [ ] 🧹 **CLEAN**: Verify no hardcoded strings (extract to resources if needed)

#### Step 8.3: Plan Completion
- [ ] 📋 **UPDATE**: Mark this plan status as "Completed"
- [ ] 📋 **UPDATE**: Add completion date to header
- [ ] 📋 **UPDATE**: Document any deviations from plan in Decision Log
- [ ] 📋 **UPDATE**: Note any deferred enhancements in Future Enhancements section

---

## Implementation Summary Statistics

| Phase | Total Tasks | Type | Status |
|-------|-------------|------|--------|
| Phase 1: AmountInputFilter (Bonus) | 31 | Unit Tests + Implementation | ✅ **COMPLETED** |
| Phase 2: Add Keyboard Type (PRIMARY FIX) | 14 | Config Changes + Build | ✅ **COMPLETED** |
| Phase 3: AmountTextField Component (Bonus) | 28 | Unit/UI Tests + Implementation | ⏸️ Not Started |
| Phase 4: Transaction Screens Migration (Bonus) | 16 | Integration + Manual Tests | ⏸️ Not Started |
| Phase 5: Wallet Screens Migration (Bonus) | 24 | Integration + Manual Tests | ⏸️ Not Started |
| Phase 6: Settings Screen Migration (Bonus) | 12 | Integration + Manual Tests | ⏸️ Not Started |
| Phase 7: Integration Testing (Bonus) | 20 | Manual Testing | ⏸️ Not Started |
| Phase 8: Documentation | 10 | Documentation | ⏸️ Not Started |
| **TOTAL** | **155** | Mixed | **29% Complete (45/155)** |

### Implementation Strategy

**Two-Phase Approach**:

1. ✅ **Quick Win (Phase 2)**: Add `KeyboardType.Decimal` to all fields → **PRIMARY PAIN POINT SOLVED!**
2. ⏸️ **Enhanced Features (Phases 3-7)**: Optional improvements for better UX (input filtering, currency decimals)

**Current Status**:
- ✅ Phase 1: AmountInputFilter completed (bonus feature)
- ✅ Phase 2: Keyboard type fix **COMPLETED** - Main issue solved!
- ⏸️ Phases 3-7: Optional enhancements (can be implemented later if desired)

---

## TDD Workflow Reference

### Red-Green-Refactor Cycle

```
1. 🔴 RED (Write Failing Test)
   ├─ Write test for NEW behavior
   ├─ Test should FAIL (feature doesn't exist yet)
   └─ Verify test fails for the RIGHT reason

2. 🟢 GREEN (Make It Pass)
   ├─ Write MINIMUM code to pass test
   ├─ Don't worry about perfection
   └─ Get to green as fast as possible

3. 🔵 REFACTOR (Clean Up)
   ├─ Improve code quality
   ├─ Remove duplication
   ├─ Keep tests GREEN while refactoring
   └─ Commit when stable
```

### TDD Best Practices for This Project

1. **Test First, Always**: Never write implementation before test
2. **One Test at a Time**: Complete full red-green-refactor before next test
3. **Small Steps**: Each test should verify ONE specific behavior
4. **Descriptive Names**: Test names should explain expected behavior
5. **Run Frequently**: Execute tests after every change
6. **Keep Tests Fast**: Unit tests should run in milliseconds
7. **Mock Dependencies**: Use test doubles for external dependencies
8. **Verify Failures**: Always see test fail before making it pass

### Test Organization

```
app/src/test/java/                    # Unit Tests (JVM)
└── com/axeven/profiteerapp/
    ├── utils/
    │   └── AmountInputFilterTest.kt  # Filter logic tests
    └── ui/transaction/
        └── TransactionFormValidatorTest.kt

app/src/androidTest/java/             # Instrumented Tests (Android)
└── com/axeven/profiteerapp/
    └── ui/components/
        └── AmountTextFieldTest.kt    # UI component tests
```

### Running Tests

```bash
# Run all unit tests (fast)
./gradlew testDebugUnitTest

# Run specific test class
./gradlew testDebugUnitTest --tests AmountInputFilterTest

# Run specific test method
./gradlew testDebugUnitTest --tests AmountInputFilterTest."BTC allows up to 8 decimal places"

# Run UI tests (requires device/emulator)
./gradlew connectedAndroidTest

# Run with coverage report
./gradlew testDebugUnitTest jacocoTestReport
```

## Testing Strategy

### Unit Tests

**AmountInputFilterTest.kt**:
```kotlin
class AmountInputFilterTest {

    @Test
    fun `BTC allows up to 8 decimal places`() {
        val result = AmountInputFilter.filterAmountInput(
            input = "1.12345678",
            currentValue = "",
            currency = "BTC"
        )
        assertEquals("1.12345678", result)
    }

    @Test
    fun `BTC prevents 9th decimal place`() {
        val result = AmountInputFilter.filterAmountInput(
            input = "1.123456789",
            currentValue = "1.12345678",
            currency = "BTC"
        )
        assertEquals("1.12345678", result)
    }

    @Test
    fun `JPY prevents any decimal places`() {
        val result = AmountInputFilter.filterAmountInput(
            input = "100.",
            currentValue = "100",
            currency = "JPY"
        )
        assertEquals("100", result)
    }

    @Test
    fun `prevents multiple decimal points`() {
        val result = AmountInputFilter.filterAmountInput(
            input = "1.2.3",
            currentValue = "1.2",
            currency = "USD"
        )
        assertEquals("1.2", result)
    }

    @Test
    fun `normalizes leading zeros`() {
        val result = AmountInputFilter.filterAmountInput(
            input = "00.5",
            currentValue = "",
            currency = "USD"
        )
        assertEquals("0.5", result)
    }

    @Test
    fun `allows empty string for backspace`() {
        val result = AmountInputFilter.filterAmountInput(
            input = "",
            currentValue = "1",
            currency = "USD"
        )
        assertEquals("", result)
    }

    @Test
    fun `filters out letters and symbols`() {
        val result = AmountInputFilter.filterAmountInput(
            input = "1a2b3$",
            currentValue = "",
            currency = "USD"
        )
        assertEquals("123", result)
    }
}
```

### UI Tests

**AmountTextFieldTest.kt**:
```kotlin
@RunWith(AndroidJUnit4::class)
class AmountTextFieldTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun amountTextField_showsNumericKeyboard() {
        composeTestRule.setContent {
            AmountTextField(
                value = "",
                onValueChange = {},
                currency = "USD",
                label = "Amount"
            )
        }

        // Verify keyboard type is Decimal
        // (Requires accessibility check or keyboard configuration test)
    }

    @Test
    fun amountTextField_filtersInvalidCharacters() {
        var value by mutableStateOf("")

        composeTestRule.setContent {
            AmountTextField(
                value = value,
                onValueChange = { value = it },
                currency = "USD",
                label = "Amount"
            )
        }

        composeTestRule.onNodeWithText("Amount")
            .performTextInput("1a2b3c")

        assertEquals("123", value)
    }

    @Test
    fun amountTextField_showsCurrencySymbolPrefix() {
        composeTestRule.setContent {
            AmountTextField(
                value = "100",
                onValueChange = {},
                currency = "USD",
                label = "Amount"
            )
        }

        composeTestRule.onNodeWithText("$").assertExists()
    }

    @Test
    fun amountTextField_showsErrorState() {
        composeTestRule.setContent {
            AmountTextField(
                value = "",
                onValueChange = {},
                currency = "USD",
                label = "Amount",
                isError = true,
                errorMessage = "Amount is required"
            )
        }

        composeTestRule.onNodeWithText("Amount is required").assertExists()
    }
}
```

### Manual Testing Checklist

- [ ] Test on Android device with physical keyboard
- [ ] Test on Android device with virtual keyboard
- [ ] Test on emulator with different API levels (24, 31, 36)
- [ ] Test with different locales (US, EU, JP)
- [ ] Test copy/paste with invalid formatted text
- [ ] Test rapid typing to verify no race conditions
- [ ] Test screen rotation preserves input state
- [ ] Test accessibility features (TalkBack compatibility)

## Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| **Breaking existing validation** | Medium | High | Comprehensive test suite, gradual rollout per screen |
| **Decimal rounding issues** | Low | Medium | Use NumberFormatter consistently, test edge cases |
| **Keyboard type not showing on some devices** | Low | Low | Fallback gracefully, input filter still works |
| **User confusion with restricted input** | Low | Medium | Clear placeholder text, error messages |
| **Performance issues with real-time filtering** | Very Low | Low | AmountInputFilter is lightweight, no Regex |
| **Locale-specific decimal separators** | Medium | Medium | Document current US-only support, future enhancement |

## Dependencies

### No New External Dependencies Required

All functionality can be built using existing project dependencies:
- Jetpack Compose (already in use)
- Kotlin standard library
- Existing NumberFormatter utility

### Internal Dependencies

- `NumberFormatter.kt` - Reuse `getCurrencySymbol()` and decimal place logic
- `TransactionFormValidator.kt` - Update to use `NumberFormatter.parseDouble()`
- Consolidated state pattern - All screens already follow this

## Performance Considerations

- **Input Filtering**: O(n) string operations on each keystroke (negligible for amount strings <20 chars)
- **State Updates**: No change from current approach (still using state.copy())
- **Recomposition**: AmountTextField will recompose on value change (same as current TextField)
- **Memory**: No additional allocations beyond current implementation

**Expected Performance Impact**: Negligible to none. May slightly improve validation performance by preventing invalid input earlier.

## Backwards Compatibility

### Data Layer: No Changes Required

- Amount values stored as `Double` in Firestore (unchanged)
- Transaction and Wallet models remain the same
- No database migration needed

### UI Layer: Breaking Changes

- `CreateTransactionScreen.kt` - Replace TextField (non-breaking for users)
- `EditTransactionScreen.kt` - Replace TextField (non-breaking for users)
- `WalletListScreen.kt` - Replace TextField (non-breaking for users)
- `SettingsScreen.kt` - Replace TextField (non-breaking for users)

**User Impact**: None. Existing data works with new input method. UI changes are enhancements only.

## Rollback Plan

If critical issues discovered after implementation:

1. **Immediate Rollback**: Revert commits for Phase 3-5 (screen migrations)
2. **Keep Foundation**: Retain `AmountInputFilter` and `AmountTextField` for future use
3. **Hotfix Option**: Add `enableFiltering: Boolean = true` parameter to AmountTextField for quick disable

## Future Enhancements (Out of Scope)

These are NOT part of this plan but documented for future consideration:

1. **Thousands Separator Input**: Real-time separator insertion during typing
2. **Localization**: Support EU decimal separator (comma) and locale-specific formatting
3. **Voice Input Handling**: Filter voice-to-text amount input
4. **Accessibility**: Custom content descriptions for screen readers
5. **Visual Feedback**: Animation when max decimals reached
6. **Smart Paste**: Detect and clean formatted amounts from clipboard (e.g., "$1,234.56")
7. **Scientific Notation**: Support for very large/small numbers (e.g., BTC in satoshis)

## Success Criteria

This implementation is considered successful when:

✅ All amount input fields show numeric keyboard on mobile devices
✅ Users cannot type letters or symbols in amount fields
✅ BTC amounts accept up to 8 decimal places
✅ GOLD amounts accept up to 3 decimal places
✅ JPY amounts accept 0 decimal places (integers only)
✅ Standard currencies accept up to 2 decimal places
✅ All existing unit tests pass
✅ New unit tests achieve >90% code coverage for new components
✅ Manual testing confirms no regressions
✅ User can still paste amounts (with automatic filtering)
✅ Validation errors still show appropriately

## Timeline Estimate (TDD Approach)

| Phase | Estimated Time | Test Time | Implementation Time | Dependencies |
|-------|----------------|-----------|---------------------|--------------|
| Phase 1: AmountInputFilter | 5-7 hours | 3-4 hours | 2-3 hours | None |
| Phase 2: Validator Enhancement | 3-4 hours | 2-3 hours | 1 hour | Phase 1 |
| Phase 3: AmountTextField | 4-5 hours | 2-3 hours | 2 hours | Phase 1 |
| Phase 4: Transaction Screens | 2-3 hours | 1-2 hours | 1 hour | Phase 3 |
| Phase 5: Wallet Screens | 3-4 hours | 1-2 hours | 2 hours | Phase 3 |
| Phase 6: Settings Screen | 2-3 hours | 1 hour | 1-2 hours | Phase 3 |
| Phase 7: Integration Testing | 4-6 hours | N/A (manual) | N/A | All previous |
| Phase 8: Documentation | 1-2 hours | N/A | N/A | All previous |
| **Total** | **24-34 hours** | **10-15 hours** | **9-11 hours** | Sequential |

**Notes**:
- TDD approach adds ~20% more time compared to traditional approach (24-34 vs 19-28 hours)
- Extra time investment results in better test coverage and fewer bugs
- Tests written first prevent regression and enable confident refactoring

**Recommended Approach**:
- Implement over 4-5 development sessions
- Complete full red-green-refactor cycles for each feature
- Run tests after EVERY code change
- Commit only when all tests pass

## Questions for Stakeholders

Before implementation begins, clarify:

1. **Settings Screen**: Should currency rate input use the same component or have different decimal rules?
2. **Thousands Separators**: Should we add real-time separator insertion in Phase 1, or defer to future?
3. **Localization**: Is US decimal format (period) sufficient, or should we support EU format (comma)?
4. **Testing Coverage**: Is 90% code coverage target acceptable, or should we aim for 95%+?
5. **Rollout Strategy**: Implement all screens at once, or gradual rollout per screen?

## References

- [State Management Guidelines](../STATE_MANAGEMENT_GUIDELINES.md)
- [Testing Requirements](../../CLAUDE.md#testing-requirements)
- [NumberFormatter Implementation](../../app/src/main/java/com/axeven/profiteerapp/utils/NumberFormatter.kt)
- [Transaction Form Validator](../../app/src/main/java/com/axeven/profiteerapp/ui/transaction/TransactionFormValidator.kt)
- [Jetpack Compose TextField Documentation](https://developer.android.com/jetpack/compose/text/user-input)

---

## Implementation Log

**Status**: Planning Complete - Ready for Implementation

**Next Steps**:
1. Review plan with team/stakeholders
2. Answer stakeholder questions
3. Begin Phase 1: Foundation implementation
4. Update this document with progress as each phase completes

**Decision Log**:
- **TDD Approach Adopted** (2025-10-18): Plan restructured to follow Test-Driven Development methodology
  - Rationale: Ensures comprehensive test coverage from the start
  - Impact: Increases implementation time by ~20%, but reduces bugs and enables confident refactoring
  - All tests must pass before moving to next phase
- TBD: Settings screen approach (same component vs. separate)
- TBD: Thousands separator support (Phase 1 vs. future)
- TBD: Localization priority (US-only vs. multi-locale)

**TDD Implementation Notes**:
- Use Red-Green-Refactor cycle for all features
- Write tests FIRST, then implement minimum code to pass
- Run tests after EVERY code change
- Never commit code with failing tests
- Track progress in TDD Summary Statistics table above

---

## Appendix: Code Examples

### Example: Using AmountTextField (After Implementation)

```kotlin
@Composable
fun CreateTransactionScreen(
    transactionState: CreateTransactionUiState,
    onStateChange: (CreateTransactionUiState) -> Unit
) {
    AmountTextField(
        value = transactionState.amount,
        onValueChange = { filtered ->
            onStateChange(transactionState.updateAmount(filtered))
        },
        currency = transactionState.selectedCurrency,
        label = "Amount",
        modifier = Modifier.fillMaxWidth(),
        isError = transactionState.validationErrors.amountError != null,
        errorMessage = transactionState.validationErrors.amountError
    )
}
```

### Example: AmountInputFilter Usage

```kotlin
// In AmountTextField implementation
OutlinedTextField(
    value = value,
    onValueChange = { newValue ->
        val filtered = AmountInputFilter.filterAmountInput(
            input = newValue,
            currentValue = value,
            currency = currency
        )
        onValueChange(filtered)
    },
    keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Decimal,
        imeAction = ImeAction.Done
    )
)
```

### Example: Enhanced Validation

```kotlin
// Updated TransactionFormValidator
fun validateAmount(amount: String, currency: String): String? {
    return when {
        amount.isBlank() -> "Amount is required"
        NumberFormatter.parseDouble(amount) == null -> "Amount must be a valid number"
        NumberFormatter.parseDouble(amount)?.let { it <= 0 } == true -> "Amount must be greater than 0"
        !isValidDecimalPlaces(amount, currency) -> {
            val maxDecimals = when (currency) {
                "BTC" -> 8
                "GOLD" -> 3
                "JPY" -> 0
                else -> 2
            }
            "Maximum $maxDecimals decimal places allowed for $currency"
        }
        else -> null
    }
}
```

---

**Plan Version**: 1.0
**Last Updated**: 2025-10-18
**Author**: Claude Code
**Reviewers**: TBD
