# Implementation Plan: Remove Hardcoded Magic Values

**Date Created**: 2025-10-17
**Status**: 🔄 In Progress
**Priority**: Medium
**Estimated Effort**: 1-2 hours
**Anti-Pattern Reference**: [docs/antipatterns.md #6](../antipatterns.md#6-%EF%B8%8F-ongoing-issue---hardcoded-magic-values)

## Overview

Eliminate hardcoded magic values scattered throughout the codebase by centralizing them into well-defined constants and enums. This improves maintainability, readability, and makes configuration changes easier.

## Current Issues

- ⚠️ Magic numbers: `.limit(20)`, `.limit(1)` in 8+ repository locations
- ⚠️ Hardcoded strings: Currency codes ("USD"), wallet types ("Physical", "Logical")
- ⚠️ No central constants management
- ⚠️ Difficult to update configuration values

## Goals

- ✅ Create centralized constants structure
- ✅ Replace all magic numbers with named constants
- ✅ Replace magic strings with enums/constants
- ✅ Improve code clarity and maintainability
- ✅ Follow TDD approach with comprehensive tests

## Implementation Approach: TDD

This plan follows Test-Driven Development principles:
1. **Write tests first** - Define expected behavior before implementation
2. **Red phase** - Tests fail initially (constants don't exist yet)
3. **Green phase** - Create minimal implementation to pass tests
4. **Refactor phase** - Replace magic values with constants across codebase

---

## Phase 1: Test Setup & Constants Creation ✅

### Step 1.1: Create Constants Test File ✅
- [x] Create `app/src/test/java/com/axeven/profiteerapp/data/constants/AppConstantsTest.kt`
- [x] Write tests verifying constant values:
  - [x] Test `RepositoryConstants.TRANSACTION_PAGE_SIZE` equals expected value
  - [x] Test `RepositoryConstants.SINGLE_RESULT_LIMIT` equals 1
  - [x] Test `RepositoryConstants.DEFAULT_CURRENCY` equals "USD"
  - [x] Test `RepositoryConstants.MAX_TAG_SUGGESTIONS` equals expected value
- [x] Run tests → **Expected: RED** (constants don't exist)

### Step 1.2: Create WalletType Enum Tests ✅
- [x] Create `app/src/test/java/com/axeven/profiteerapp/data/constants/WalletTypeTest.kt`
- [x] Write tests for WalletType enum:
  - [x] Test enum has PHYSICAL variant
  - [x] Test enum has LOGICAL variant
  - [x] Test `WalletType.fromString("Physical")` returns PHYSICAL
  - [x] Test `WalletType.fromString("Logical")` returns LOGICAL
  - [x] Test `WalletType.PHYSICAL.displayName` equals "Physical"
  - [x] Test `WalletType.LOGICAL.displayName` equals "Logical"
  - [x] Test invalid string throws exception or returns null
- [x] Run tests → **Expected: RED**

### Step 1.3: Create Constants Implementation (Green Phase) ✅
- [x] Create `app/src/main/java/com/axeven/profiteerapp/data/constants/AppConstants.kt`
- [x] Implement `RepositoryConstants` object with:
  ```kotlin
  object RepositoryConstants {
      const val TRANSACTION_PAGE_SIZE = 20
      const val SINGLE_RESULT_LIMIT = 1
      const val DEFAULT_CURRENCY = "USD"
      const val MAX_TAG_SUGGESTIONS = 10 // Adjust based on actual usage
  }
  ```
- [x] Run `RepositoryConstants` tests → **Expected: GREEN**

### Step 1.4: Create WalletType Enum Implementation (Green Phase) ✅
- [x] Implement `WalletType` enum in `AppConstants.kt`:
  ```kotlin
  enum class WalletType(val displayName: String) {
      PHYSICAL("Physical"),
      LOGICAL("Logical");

      companion object {
          fun fromString(value: String): WalletType? {
              return values().find { it.displayName.equals(value, ignoreCase = true) }
          }
      }
  }
  ```
- [x] Run `WalletType` tests → **Expected: GREEN**

### Step 1.5: Run Full Test Suite ✅
- [x] Run `./gradlew testDebugUnitTest`
- [x] Verify all new tests pass (18 tests: 6 AppConstants + 12 WalletType - all passed)
- [x] Verify no existing tests broken

---

## Phase 2: Repository Layer Refactoring (TDD) ✅

### Step 2.1: TransactionRepository - Refactor ✅
- [x] Open `app/src/main/java/com/axeven/profiteerapp/data/repository/TransactionRepository.kt`
- [x] Import `RepositoryConstants`
- [x] Replace `.limit(20)` with `.limit(RepositoryConstants.TRANSACTION_PAGE_SIZE.toLong())`
- [x] No `.limit(1)` found in this file

### Step 2.2: UserPreferencesRepository - Refactor ✅
- [x] Verified 6 occurrences of `.limit(1)` as documented in antipatterns.md
- [x] Open `app/src/main/java/com/axeven/profiteerapp/data/repository/UserPreferencesRepository.kt`
- [x] Import `RepositoryConstants`
- [x] Replace all 6 `.limit(1)` with `RepositoryConstants.SINGLE_RESULT_LIMIT.toLong()`
- [x] Replace 3 hardcoded `"USD"` comparisons with `RepositoryConstants.DEFAULT_CURRENCY`

### Step 2.3: CurrencyRateRepository - Refactor ✅
- [x] Open `app/src/main/java/com/axeven/profiteerapp/data/repository/CurrencyRateRepository.kt`
- [x] Import `RepositoryConstants`
- [x] Replace `.limit(1)` with `RepositoryConstants.SINGLE_RESULT_LIMIT.toLong()`

### Step 2.4: UserPreferences Model - Refactor ✅
- [x] Open `app/src/main/java/com/axeven/profiteerapp/data/model/UserPreferences.kt`
- [x] Import `RepositoryConstants`
- [x] Replace 3 hardcoded `"USD"` defaults with `RepositoryConstants.DEFAULT_CURRENCY`

### Step 2.5: Verification ✅
- [x] Run `./gradlew testDebugUnitTest` → All constants tests pass (18 tests)
- [x] Verify no new test failures (same 4 pre-existing failures as before)
- [x] All repository refactorings complete and working

---

## Phase 3: Model Layer Refactoring (TDD) ✅ Partially Complete

### Step 3.1: Update Wallet Model - Backward Compatible ✅
- [x] Open `app/src/main/java/com/axeven/profiteerapp/data/model/Wallet.kt`
- [x] Import `WalletType` enum
- [x] Update default values to use `WalletType.PHYSICAL.displayName`
- [x] Add backward-compatible `type` property with enum accessor
- [x] Add convenience properties: `isPhysical`, `isLogical`
- [x] Keep `walletType: String` for Firebase compatibility

### Step 3.2: Refactor Core Utility Files ✅
- [x] BalanceDiscrepancyDetector.kt
  - [x] Replace `it.walletType == "Physical"` → `it.isPhysical`
  - [x] Replace `it.walletType == "Logical"` → `it.isLogical`
- [x] DiscrepancyAnalyzer.kt
  - [x] Replace `it.value.walletType == "Physical"` → `it.value.isPhysical`
  - [x] Replace `it.value.walletType == "Logical"` → `it.value.isLogical`

### Step 3.3: Remaining Refactorings ⚠️ Partial
- [x] Search completed - Found 40+ occurrences across 20+ files
- [x] Core foundation complete (Wallet model + utility files)
- ⚠️ **Deferred to future work**: UI layers and ViewModels (scope too large for single session)
  - Remaining files: HomeViewModel, ReportViewModel, WalletListViewModel, TransactionListViewModel
  - Remaining UI: CreateTransactionScreen, EditTransactionScreen, WalletListScreen, etc.
  - All remaining code continues to work via backward-compatible `walletType` string property

### Verification ✅
- [x] Run `./gradlew testDebugUnitTest` → No new test failures
- [x] Same 4 pre-existing failures as before Phase 3
- [x] Backward compatibility maintained - all existing code works unchanged

---

## Phase 4: Currency Constants Refactoring (TDD)

### Step 4.1: Search for Currency Magic Strings
- [ ] Use Grep to find all occurrences of `"USD"` hardcoded
- [ ] Document all locations found
- [ ] Prioritize user-facing and default value locations

### Step 4.2: Replace Currency Defaults - TDD
- [ ] For each location found:
  - [ ] Write test verifying `DEFAULT_CURRENCY` usage (RED)
  - [ ] Replace `"USD"` with `RepositoryConstants.DEFAULT_CURRENCY` (GREEN)
  - [ ] Run tests for that module
- [ ] Common locations:
  - [ ] UserPreferences default values
  - [ ] CurrencyRateRepository
  - [ ] ViewModel initialization
  - [ ] UI default selections

---

## Phase 5: Additional Magic Values (TDD)

### Step 5.1: Identify Other Magic Values
- [ ] Search codebase for common magic numbers:
  - [ ] Pagination limits
  - [ ] Timeout values
  - [ ] Buffer sizes
  - [ ] Retry counts
  - [ ] Animation durations (if any)
- [ ] Document findings in checklist

### Step 5.2: Categorize and Add Constants
- [ ] Create appropriate constant groups:
  ```kotlin
  object UIConstants {
      const val TAG_AUTOCOMPLETE_MIN_CHARS = 3
      const val MAX_TAG_SUGGESTIONS = 10
  }

  object ValidationConstants {
      const val MIN_AMOUNT_VALUE = 0.01
      const val MAX_WALLET_NAME_LENGTH = 50
  }
  ```
- [ ] Write tests for each constant group (RED)
- [ ] Implement constants (GREEN)

### Step 5.3: Replace Additional Magic Values
- [ ] For each magic value identified:
  - [ ] Write test (RED)
  - [ ] Replace with constant (GREEN)
  - [ ] Verify behavior unchanged

---

## Phase 6: Integration & Testing

### Step 6.1: Run Full Test Suite
- [ ] Run `./gradlew testDebugUnitTest`
- [ ] Verify all unit tests pass
- [ ] Fix any failing tests

### Step 6.2: Run Lint Checks
- [ ] Run `./gradlew lintDebug`
- [ ] Address any lint warnings about unused constants
- [ ] Ensure no new warnings introduced

### Step 6.3: Build Verification
- [ ] Run `./gradlew build`
- [ ] Verify clean build with no errors
- [ ] Check ProGuard rules if any constants need preservation

### Step 6.4: Manual Testing
- [ ] Test transaction creation (pagination with new constant)
- [ ] Test wallet creation (Physical/Logical with enum)
- [ ] Test currency selection (default currency constant)
- [ ] Test tag autocomplete (if constant added)
- [ ] Verify no behavioral changes

---

## Phase 7: Documentation & Cleanup

### Step 7.1: Update Documentation
- [ ] Update `docs/antipatterns.md`:
  - [ ] Change status from "⚠️ ONGOING ISSUE" to "✅ RESOLVED"
  - [ ] Document solution implemented
  - [ ] Add usage examples
- [ ] Update `CLAUDE.md` if needed:
  - [ ] Add guidance on using AppConstants
  - [ ] Document where to add new constants

### Step 7.2: Code Documentation
- [ ] Add KDoc comments to `AppConstants.kt`:
  ```kotlin
  /**
   * Central repository for application-wide constants.
   * Use these constants instead of magic values for better maintainability.
   */
  object RepositoryConstants {
      /** Default page size for transaction queries */
      const val TRANSACTION_PAGE_SIZE = 20
      // ... etc
  }
  ```
- [ ] Add usage examples in comments

### Step 7.3: Create Migration Guide (Optional)
- [ ] Document the refactoring in `docs/refactoring-log.md` (create if needed)
- [ ] Include before/after examples
- [ ] Note any breaking changes (if Option A chosen for WalletType)

---

## Rollback Plan

If issues arise during implementation:

1. **Git Safety**:
   - [ ] Create feature branch before starting: `git checkout -b refactor/remove-magic-values`
   - [ ] Commit after each phase completion
   - [ ] Can revert specific commits if needed

2. **Phase-by-Phase Rollback**:
   - [ ] Phase 1-2 (Constants + Repositories): Low risk, easy rollback
   - [ ] Phase 3 (Models): Medium risk if breaking changes, test thoroughly
   - [ ] Phase 4-5 (Currency/Other): Low risk, isolated changes

3. **Testing Safety Net**:
   - [ ] Run tests after each phase
   - [ ] Stop and investigate if any tests fail
   - [ ] Don't proceed to next phase until GREEN

---

## Success Criteria

### Functional Requirements
- ✅ All magic numbers replaced with named constants
- ✅ All magic strings replaced with enums/constants
- ✅ No hardcoded "Physical", "Logical", "USD" strings in business logic
- ✅ Centralized constants in `AppConstants.kt`

### Testing Requirements
- ✅ All existing tests still pass
- ✅ New tests added for constants validation
- ✅ Test coverage maintained or improved
- ✅ No behavioral changes in application

### Code Quality Requirements
- ✅ Lint checks pass
- ✅ Build succeeds without warnings
- ✅ Constants properly documented with KDoc
- ✅ Follows existing code style

### Documentation Requirements
- ✅ `docs/antipatterns.md` updated with resolution
- ✅ `CLAUDE.md` updated with constants usage guidance
- ✅ Code comments added to `AppConstants.kt`

---

## Progress Tracking

**Phase 1**: ✅ Complete (5/5 steps)
**Phase 2**: ✅ Complete (5/5 steps)
**Phase 3**: ✅ Partially Complete (2/3 steps) - Core foundation done, UI deferred
**Phase 4**: ⬜ Not Started (0/2 steps) - Already addressed in Phase 2
**Phase 5**: ⬜ Not Started (0/3 steps)
**Phase 6**: ⬜ Not Started (0/4 steps)
**Phase 7**: ⬜ Not Started (0/3 steps)

**Overall Progress**: 46% (12/26 major steps completed)

---

## Notes

- **TDD Approach**: Every refactoring step follows Red-Green-Refactor cycle
- **Incremental**: Can be done phase-by-phase with commits after each phase
- **Low Risk**: Purely refactoring existing values, no logic changes
- **Backward Compatible**: Option B for WalletType ensures Firebase compatibility
- **Test Coverage**: Comprehensive tests ensure no regressions

---

## References

- [Anti-Patterns Document](../antipatterns.md#6-%EF%B8%8F-ongoing-issue---hardcoded-magic-values)
- [CLAUDE.md - Development Guidelines](../../CLAUDE.md#development-best-practices)
- [Testing Requirements](../../CLAUDE.md#testing-requirements)
