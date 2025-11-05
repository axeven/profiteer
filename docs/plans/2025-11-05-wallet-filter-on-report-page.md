# Wallet Filter on Report Page

**Created**: 2025-11-05
**Status**: In Progress - Phase 1 Complete ✅
**Approach**: Test-Driven Development (TDD)

## Overview

Add wallet filtering capability to the Portfolio Reports screen, allowing users to filter all charts by a specific wallet. This feature will work alongside the existing date filter to provide granular transaction analysis.

## Goals

- Enable users to view reports for a specific wallet only
- Maintain consistency with existing date filter behavior
- Support both filtering strategies (historical reconstruction + simple filtering)
- Follow TDD approach with comprehensive test coverage
- Integrate seamlessly with existing UI/UX patterns

## Non-Goals

- Multi-wallet selection (only single wallet or "All Wallets")
- Wallet type grouping in filter (show all wallets alphabetically)
- Separate physical/logical wallet filtering

## Requirements

### Functional Requirements

1. **Filter Dropdown**:
   - Display "All Wallets" as default option
   - List all wallets alphabetically (case-insensitive)
   - Show wallet name and currency code (e.g., "Cash Wallet (USD)")
   - Persist selection when switching chart types
   - Clear visual indication of active filter

2. **Filtering Behavior**:
   - **Portfolio Chart**: Show only selected wallet with historical balance reconstruction
   - **Wallet Distribution**: Show only selected wallet (100% distribution)
   - **Tag Charts**: Show only transactions affecting selected wallet
   - Combine with date filter (both filters apply simultaneously)

3. **UI/UX**:
   - Filter chip similar to existing date filter chip
   - Display selected wallet name in chart titles
   - Update empty states to mention wallet filter
   - Provide helpful suggestions when no data matches filters

4. **Data Integrity**:
   - Use `affectedWalletIds` field for transaction filtering
   - Handle transactions with multiple affected wallets correctly
   - Exclude transactions where selected wallet is not in `affectedWalletIds`

### Technical Requirements

1. **State Management**: Follow consolidated state pattern
2. **Security**: All queries must include userId filter
3. **Performance**: Efficient filtering without unnecessary recomposition
4. **Testing**: Minimum 80% code coverage with TDD approach
5. **Documentation**: Update relevant docs and inline comments

## Architecture

### Component Structure

```
ReportsScreen (UI Layer)
├── WalletFilterChip (New Component)
│   └── WalletFilterPickerDialog (New Component)
├── MonthYearFilterChip (Existing)
└── Chart Components (Updated for wallet filter)

ReportsViewModel (Business Logic)
├── WalletFilterState (New)
├── DateFilterState (Existing)
└── Combined Filtering Logic (Updated)

Utils
├── WalletFilterUtils (New)
└── BalanceReconstructionUtils (Updated)
```

### Data Model

```kotlin
// New sealed class for wallet filter state
sealed class WalletFilter {
    object AllWallets : WalletFilter()
    data class SpecificWallet(val walletId: String, val walletName: String) : WalletFilter()
}

// Updated ReportsUiState
data class ReportsUiState(
    val dateFilter: DateFilterPeriod = DateFilterPeriod.AllTime,
    val walletFilter: WalletFilter = WalletFilter.AllWallets, // New
    val selectedChartType: ChartType = ChartType.PORTFOLIO,
    // ... existing fields
)
```

## Implementation Plan (TDD Approach)

### Phase 1: Data Model & Utilities (TDD) ✅

#### Step 1.1: Create WalletFilter Model Tests ✅
- [x] Create `WalletFilterTest.kt`
- [x] Test `AllWallets` equality and toString
- [x] Test `SpecificWallet` equality and toString
- [x] Test `SpecificWallet` data class copy behavior
- [x] Test `SpecificWallet` with null/empty wallet IDs (should throw)
- [x] **Run tests (expect failures)**

#### Step 1.2: Implement WalletFilter Model ✅
- [x] Create `WalletFilter.kt` in `data/model/`
- [x] Implement sealed class with AllWallets and SpecificWallet
- [x] Add validation for SpecificWallet (non-blank walletId and walletName)
- [x] **Run tests (expect passes)**

#### Step 1.3: Create WalletFilterUtils Tests ✅
- [x] Create `WalletFilterUtilsTest.kt`
- [x] Test `filterTransactionsByWallet(transactions, AllWallets)` returns all transactions
- [x] Test `filterTransactionsByWallet(transactions, SpecificWallet)` filters by affectedWalletIds
- [x] Test filtering with empty transaction list
- [x] Test filtering with transactions missing affectedWalletIds field
- [x] Test filtering with transactions where wallet appears in affectedWalletIds
- [x] Test filtering with transactions where wallet doesn't appear
- [x] Test `filterWalletsByWalletFilter(wallets, AllWallets)` returns all wallets
- [x] Test `filterWalletsByWalletFilter(wallets, SpecificWallet)` returns only matching wallet
- [x] Test `filterWalletsByWalletFilter` with empty wallet list
- [x] Test `filterWalletsByWalletFilter` with wallet not in list
- [x] **Run tests (expect failures)**

#### Step 1.4: Implement WalletFilterUtils ✅
- [x] Create `WalletFilterUtils.kt` in `utils/`
- [x] Implement `filterTransactionsByWallet(List<Transaction>, WalletFilter): List<Transaction>`
- [x] Implement `filterWalletsByWalletFilter(List<Wallet>, WalletFilter): List<Wallet>`
- [x] Add null safety checks and empty list handling
- [x] **Run tests (expect passes)**

### Phase 2: Balance Reconstruction Updates (TDD)

#### Step 2.1: Update BalanceReconstructionUtils Tests
- [ ] Update `BalanceReconstructionUtilsTest.kt`
- [ ] Add test for wallet filter with AllWallets (should behave as before)
- [ ] Add test for wallet filter with SpecificWallet (should only reconstruct that wallet)
- [ ] Add test for combined date + wallet filtering
- [ ] Add test for wallet filter with wallet having no transactions
- [ ] Add test for wallet filter with wallet having transactions outside date range
- [ ] **Run tests (expect failures)**

#### Step 2.2: Update BalanceReconstructionUtils
- [ ] Update `reconstructBalancesForPeriod()` signature to accept `WalletFilter`
- [ ] Integrate `WalletFilterUtils.filterWalletsByWalletFilter()` before reconstruction
- [ ] Ensure transactions are filtered by both date AND wallet
- [ ] Update function documentation
- [ ] **Run tests (expect passes)**

### Phase 3: ViewModel Integration (TDD)

#### Step 3.1: Update ReportsViewModel Tests
- [ ] Update `ReportsViewModelTest.kt`
- [ ] Test initial state has `walletFilter = WalletFilter.AllWallets`
- [ ] Test `updateWalletFilter(WalletFilter.AllWallets)` updates state
- [ ] Test `updateWalletFilter(SpecificWallet)` updates state
- [ ] Test wallet filter persists when switching chart types
- [ ] Test combined date + wallet filtering in portfolio chart data
- [ ] Test combined date + wallet filtering in tag chart data
- [ ] Test wallet filter with empty wallet list
- [ ] Test wallet filter with wallet not in list (edge case)
- [ ] **Run tests (expect failures)**

#### Step 3.2: Update ReportsViewModel
- [ ] Add `walletFilter: WalletFilter` to `ReportsUiState`
- [ ] Add `updateWalletFilter(WalletFilter)` function
- [ ] Update `portfolioChartData` to apply wallet filter via `BalanceReconstructionUtils`
- [ ] Update `walletDistributionData` to apply wallet filter
- [ ] Update `tagExpenseData` to apply wallet filter via `WalletFilterUtils`
- [ ] Update `tagIncomeData` to apply wallet filter via `WalletFilterUtils`
- [ ] Ensure filter persists across chart type changes
- [ ] **Run tests (expect passes)**

### Phase 4: UI Components (TDD)

#### Step 4.1: Create WalletFilterPickerDialog Tests
- [ ] Create `WalletFilterPickerDialogTest.kt`
- [ ] Test dialog displays "All Wallets" option
- [ ] Test dialog displays wallet list alphabetically
- [ ] Test dialog shows wallet name and currency (e.g., "Cash (USD)")
- [ ] Test clicking "All Wallets" calls onFilterSelected with AllWallets
- [ ] Test clicking specific wallet calls onFilterSelected with SpecificWallet
- [ ] Test clicking outside dialog calls onDismiss
- [ ] Test dialog with empty wallet list shows "All Wallets" only
- [ ] Test dialog pre-selects current filter
- [ ] Test dialog uses WalletSortingUtils.sortAlphabetically
- [ ] **Run tests (expect failures)**

#### Step 4.2: Implement WalletFilterPickerDialog
- [ ] Create `WalletFilterPickerDialog.kt` in `ui/components/`
- [ ] Implement Material 3 dialog with RadioButtons
- [ ] Add "All Wallets" option at top
- [ ] Sort wallets alphabetically using `WalletSortingUtils`
- [ ] Display wallet name and currency code
- [ ] Handle selection callback
- [ ] **Run tests (expect passes)**

#### Step 4.3: Create WalletFilterChip Tests
- [ ] Create `WalletFilterChipTest.kt`
- [ ] Test chip displays "All Wallets" when filter is AllWallets
- [ ] Test chip displays wallet name when filter is SpecificWallet
- [ ] Test chip shows wallet icon
- [ ] Test clicking chip opens WalletFilterPickerDialog
- [ ] Test chip style matches MonthYearFilterChip (consistency)
- [ ] **Run tests (expect failures)**

#### Step 4.4: Implement WalletFilterChip
- [ ] Create `WalletFilterChip.kt` in `ui/components/`
- [ ] Implement FilterChip with wallet icon
- [ ] Display current filter label
- [ ] Toggle dialog visibility on click
- [ ] **Run tests (expect passes)**

### Phase 5: ReportsScreen Integration (TDD)

#### Step 5.1: Update ReportsScreen Tests
- [ ] Update `ReportsScreenTest.kt`
- [ ] Test wallet filter chip is displayed
- [ ] Test clicking wallet filter chip opens dialog
- [ ] Test selecting wallet in dialog updates filter
- [ ] Test wallet filter persists when switching charts
- [ ] Test chart titles show selected wallet name
- [ ] Test empty states mention wallet filter
- [ ] Test combined date + wallet filter behavior
- [ ] Test portfolio chart shows only selected wallet
- [ ] Test tag charts filter by selected wallet
- [ ] **Run tests (expect failures)**

#### Step 5.2: Update ReportsScreen
- [ ] Add `WalletFilterChip` to filter row (next to date filter)
- [ ] Pass `viewModel.walletFilter` to chip
- [ ] Pass `viewModel::updateWalletFilter` callback
- [ ] Update chart titles to include wallet name when filtered
- [ ] Update empty state messages to mention wallet filter
- [ ] Ensure proper spacing and alignment with date filter chip
- [ ] **Run tests (expect passes)**

### Phase 6: Chart Label Updates

#### Step 6.1: Update Chart Title Logic Tests
- [ ] Test portfolio chart title with AllWallets shows "Portfolio Over Time"
- [ ] Test portfolio chart title with SpecificWallet shows "Cash Wallet Over Time"
- [ ] Test tag expense chart title with SpecificWallet shows "Expense by Tag (Cash Wallet)"
- [ ] Test combined date + wallet filter in titles
- [ ] **Run tests (expect failures)**

#### Step 6.2: Implement Chart Title Logic
- [ ] Create helper function `getChartTitle(ChartType, DateFilterPeriod, WalletFilter): String`
- [ ] Update all chart title usages in ReportsScreen
- [ ] Ensure titles are concise and informative
- [ ] **Run tests (expect passes)**

### Phase 7: Integration Testing

#### Step 7.1: Create Integration Tests
- [ ] Create `WalletFilterIntegrationTest.kt`
- [ ] Test selecting wallet filter affects all chart types
- [ ] Test combining date filter + wallet filter
- [ ] Test switching between wallets updates charts immediately
- [ ] Test resetting to "All Wallets" restores full data
- [ ] Test wallet filter with no matching transactions shows empty state
- [ ] Test wallet filter persists after screen rotation (if applicable)
- [ ] **Run tests (expect passes)**

### Phase 8: Edge Case Handling

#### Step 8.1: Edge Case Tests
- [ ] Test wallet filter when selected wallet is deleted
- [ ] Test wallet filter with transactions missing affectedWalletIds
- [ ] Test wallet filter with very long wallet names (UI overflow)
- [ ] Test wallet filter with 50+ wallets (performance)
- [ ] Test wallet filter with zero-balance wallets
- [ ] Test wallet filter transition during loading state
- [ ] **Run tests (expect failures where applicable)**

#### Step 8.2: Implement Edge Case Handling
- [ ] Add validation to reset filter if selected wallet no longer exists
- [ ] Handle missing affectedWalletIds gracefully (exclude from filter)
- [ ] Add text overflow ellipsis for long wallet names
- [ ] Optimize wallet list rendering with LazyColumn
- [ ] Include zero-balance wallets in filter dropdown
- [ ] Show loading indicator during filter application
- [ ] **Run tests (expect passes)**

### Phase 9: Documentation & Cleanup

#### Step 9.1: Code Documentation
- [ ] Add KDoc comments to WalletFilter sealed class
- [ ] Add KDoc comments to WalletFilterUtils functions
- [ ] Add inline comments explaining filtering logic
- [ ] Update BalanceReconstructionUtils documentation
- [ ] Add usage examples in comments

#### Step 9.2: Update Project Documentation
- [ ] Update CLAUDE.md with wallet filter pattern
- [ ] Update README.md with wallet filter feature
- [ ] Add wallet filter to feature list
- [ ] Update screenshots if applicable

#### Step 9.3: Mark Plan as Complete
- [ ] Update this plan's status to "Completed"
- [ ] Add completion date
- [ ] Document any deviations from original plan
- [ ] Add lessons learned section

## Testing Strategy

### Test Coverage Targets

- **Unit Tests**: 100% coverage for utils and models
- **ViewModel Tests**: 90% coverage for filtering logic
- **UI Component Tests**: 85% coverage for dialog and chip
- **Integration Tests**: Critical user flows covered
- **Total Coverage Goal**: Minimum 80% overall

### Test Categories

1. **Model Tests** (~10 tests):
   - WalletFilter sealed class behavior
   - Data class equality and copy

2. **Utility Tests** (~20 tests):
   - WalletFilterUtils transaction filtering
   - WalletFilterUtils wallet filtering
   - BalanceReconstructionUtils with wallet filter
   - Edge cases and null safety

3. **ViewModel Tests** (~15 tests):
   - State updates
   - Filter persistence
   - Combined date + wallet filtering
   - Chart data calculations

4. **UI Component Tests** (~15 tests):
   - WalletFilterPickerDialog interactions
   - WalletFilterChip display and behavior
   - Wallet sorting and display

5. **Screen Tests** (~20 tests):
   - ReportsScreen integration
   - Chart title updates
   - Empty state handling
   - Filter combinations

6. **Integration Tests** (~10 tests):
   - Cross-chart consistency
   - Filter persistence
   - Edge case scenarios

**Total Estimated Tests**: ~90 tests

## Success Criteria

- [ ] All tests passing (90+ tests)
- [ ] Minimum 80% code coverage
- [ ] Wallet filter works with all chart types
- [ ] Combined date + wallet filtering works correctly
- [ ] No performance degradation with 50+ wallets
- [ ] Empty states provide helpful guidance
- [ ] UI matches existing filter chip design
- [ ] Documentation updated
- [ ] No regressions in existing functionality

## Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|-----------|
| Performance with large wallet lists | Medium | Use LazyColumn, pagination if needed |
| Complexity with combined filters | High | Comprehensive integration tests |
| UI overflow with long wallet names | Low | Text ellipsis, max width constraints |
| Wallet deletion edge case | Medium | Validation on filter application |
| Transaction missing affectedWalletIds | Medium | Defensive null checks, exclude from filter |

## Timeline Estimate

- **Phase 1-2** (Models & Utils): 2 hours
- **Phase 3** (ViewModel): 1.5 hours
- **Phase 4-5** (UI Components & Screen): 3 hours
- **Phase 6** (Chart Labels): 1 hour
- **Phase 7-8** (Integration & Edge Cases): 2 hours
- **Phase 9** (Documentation): 1 hour

**Total Estimated Time**: 10.5 hours

## Dependencies

- Existing date filter implementation (reference)
- WalletSortingUtils (already implemented)
- BalanceReconstructionUtils (requires updates)
- ReportsViewModel (requires updates)
- ReportsScreen (requires updates)

## Future Enhancements (Out of Scope)

- Multi-wallet selection (select multiple wallets)
- Wallet type grouping (filter by Physical or Logical)
- Save filter presets
- Export filtered reports
- Wallet comparison mode (compare 2+ wallets side-by-side)

## References

- Date Filter Implementation: `docs/plans/2025-10-26-report-date-filter.md`
- Wallet Sorting Pattern: `docs/plans/2025-10-30-wallet-dropdown-ordering.md`
- State Management: `docs/STATE_MANAGEMENT_GUIDELINES.md`
- Firebase Security: `docs/FIREBASE_SECURITY_GUIDELINES.md`
- TDD Best Practices: `CLAUDE.md` (Testing Requirements section)

---

**Status Legend**:
- ⏳ Planning
- 🏗️ In Progress
- ✅ Completed
- ❌ Blocked
- 🔄 Under Review
