# Product Requirement Prompt (PRP): Remove Portfolio Allocation from Wallet List

## 1. Overview
**Brief Description**: Remove the portfolio allocation summary card that displays physical wallet breakdown by form (Fiat, Crypto, Stocks, etc.) from the wallet list page.

**Priority**: Medium

**Estimated Complexity**: Simple

**Expected Development Time**: 1-2 hours

## 2. Context & Background
**Current State**: The wallet list page currently displays a "Portfolio Allocation" summary card when viewing physical wallets. This card shows:
- Total balance across all physical wallets
- Breakdown by physical form (Fiat Currency, Cryptocurrency, Stocks, ETFs, etc.)
- Percentage allocation for each form
- Visual progress indicators with form-specific colors
- Only appears when viewing Physical wallets (not Logical wallets)

**Problem Statement**: The portfolio allocation feature adds visual complexity to the wallet list page and may not provide sufficient value to justify its screen real estate. Users primarily use the wallet list to navigate to individual wallets rather than analyze portfolio allocation at a high level.

**User Impact**: Users will have a cleaner, more focused wallet list page. The removal will reduce cognitive load and make the primary wallet navigation functionality more prominent.

**Business Value**: Simplifies the UI and reduces maintenance overhead for a feature that may have limited user engagement. Aligns with a more streamlined wallet management experience.

## 3. Detailed Requirements

### 3.1 Functional Requirements
**Core Features**:
- [ ] Remove portfolio allocation summary card from physical wallet list view
- [ ] Remove associated ViewModel methods that calculate portfolio statistics  
- [ ] Remove the PhysicalFormSummaryCard UI component entirely
- [ ] Maintain all other existing wallet list functionality (grouping, filtering, wallet management)

**User Stories**:
- As a user viewing my physical wallets, I want a cleaner list view focused on individual wallet management
- As a user, I want the wallet list to load faster without additional portfolio calculations

**Business Logic**:
- No business logic changes required - this is a pure UI feature removal
- Wallet balances, filtering, and grouping logic remain unchanged
- Physical form data and icons remain available for individual wallet display

### 3.2 Technical Requirements
**Architecture Considerations**:
- Follow MVVM pattern - remove ViewModel methods cleanly
- Maintain separation of concerns
- No data model changes required

**Data Requirements**:
- No database schema changes needed
- Physical form data remains in Wallet model for individual wallet display
- No data migration required

**API Requirements**:
- No API changes needed
- All existing Firebase operations remain unchanged

### 3.3 UI/UX Requirements
**Design Specifications**:
- Remove PhysicalFormSummaryCard component from WalletListScreen
- Maintain existing spacing and layout for remaining elements
- No visual design changes to other components
- Preserve Material 3 design system compliance

**Visual Design**:
- No new visual elements needed
- Remove existing portfolio allocation card styling
- Maintain consistent card spacing in wallet list

## 4. Implementation Guidance

### 4.1 Suggested Approach
**Phase 1**: Remove UI Component Usage
- [ ] Remove PhysicalFormSummaryCard usage from WalletListScreen.kt (lines 146-153)
- [ ] Test wallet list rendering to ensure layout remains clean

**Phase 2**: Clean Up ViewModel Methods
- [ ] Remove getPhysicalFormBalanceSummary() method from WalletListViewModel.kt (lines 293-300)
- [ ] Remove getTotalBalanceByForm() method (lines 302-306)
- [ ] Remove getWalletCountByForm() method (lines 308-313)
- [ ] Update Analytics Methods comment if needed

**Phase 3**: Remove Component Definition
- [ ] Remove PhysicalFormSummaryCard component from WalletGrouping.kt (lines 290-390)
- [ ] Remove associated documentation comment (lines 286-288)

### 4.2 Files to Focus On
**Primary Files**:
- `app/src/main/java/com/axeven/profiteerapp/ui/wallet/WalletListScreen.kt` - Remove component usage and conditional rendering
- `app/src/main/java/com/axeven/profiteerapp/viewmodel/WalletListViewModel.kt` - Remove analytics methods  
- `app/src/main/java/com/axeven/profiteerapp/ui/wallet/WalletGrouping.kt` - Remove component definition

**Secondary Files**:
- No imports cleanup needed (same package)
- No test files affected (no existing test coverage for this component)

**Related Documentation**:
- `CLAUDE.md` - Follow existing patterns for code removal
- `docs/pages/wallet_list_page.md` - May need updating to reflect UI changes

### 4.3 Dependencies & Prerequisites  
**Required Before Starting**:
- [ ] Confirm no external references to PhysicalFormSummaryCard exist
- [ ] Verify getPhysicalFormBalanceSummary() is only used in WalletListScreen

**Potential Blockers**:
- None identified - this is a clean removal with no external dependencies

## 5. Acceptance Criteria

### 5.1 Definition of Done
- [ ] PhysicalFormSummaryCard no longer appears in physical wallet list view
- [ ] All related ViewModel methods removed  
- [ ] PhysicalFormSummaryCard component definition removed
- [ ] No compilation errors or warnings
- [ ] Wallet list layout flows naturally without the removed component
- [ ] All existing wallet list functionality preserved (filtering, grouping, CRUD operations)
- [ ] Code follows project conventions and patterns
- [ ] Build and lint checks pass

### 5.2 Test Scenarios
**Happy Path**:
1. Navigate to wallet list and switch to Physical wallets view
2. Verify no portfolio allocation summary card is displayed
3. Confirm wallet list shows individual physical wallets with grouping/filtering intact
4. Verify all wallet CRUD operations work normally

**Edge Cases**:  
1. View empty physical wallet list - should show empty state without portfolio card
2. Switch between Physical and Logical wallet views - layout should remain consistent
3. Create new physical wallets - no portfolio card should appear

**Error Cases**:
1. No specific error cases for this removal
2. Verify no null pointer exceptions from missing methods

### 5.3 Validation Methods
**Manual Testing**:
- [ ] Physical wallet list view renders correctly without portfolio card
- [ ] Logical wallet list view remains unchanged  
- [ ] Wallet filtering and grouping functionality preserved
- [ ] No visual layout issues or spacing problems

**Automated Testing**:
- [ ] Build passes without compilation errors
- [ ] Lint checks pass without warnings about unused code
- [ ] No failing unit tests (none exist for removed functionality)

## 6. Constraints & Considerations

### 6.1 Technical Constraints
- Must maintain existing MVVM architecture patterns
- Follow Jetpack Compose best practices for component removal
- Preserve all other wallet list functionality
- No breaking changes to public API surface

### 6.2 Business Constraints  
- Simple cleanup task with no timeline pressure
- No budget or resource constraints
- Should not affect user workflows

### 6.3 User Experience Constraints
- Must maintain seamless navigation experience
- No disruption to existing user workflows  
- Cleaner UI should improve perceived performance

## 7. Risk Assessment

### 7.1 Technical Risks
**Risk 1**: Accidentally removing shared utility methods
- **Probability**: Low  
- **Impact**: Low
- **Mitigation**: Careful review of method usage before deletion

**Risk 2**: Layout issues after component removal
- **Probability**: Low
- **Impact**: Low  
- **Mitigation**: Test UI rendering on different screen sizes

### 7.2 Business Risks
**Risk 1**: User confusion if they were using portfolio allocation feature  
- **Mitigation**: Monitor user feedback and analytics to validate low feature usage

## 8. Success Metrics
**Quantitative Metrics**:
- Build time: Should remain the same or slightly faster
- APK size: Minimal reduction due to removed code

**Qualitative Metrics**:
- Cleaner, more focused wallet list UI
- Reduced visual complexity
- Improved maintainability

## 9. Future Considerations  
**Potential Enhancements**:
- Portfolio analytics could be moved to dedicated analytics screen if needed
- Physical form statistics could be added to wallet detail pages individually

**Scalability Considerations**:
- Removal reduces code complexity and improves maintainability
- Fewer calculations improve performance as wallet count grows

## 10. Claude Implementation Notes
**Optimization Tips for AI Development**:
- [ ] Reference specific line numbers: WalletListScreen.kt lines 146-153, WalletListViewModel.kt lines 293-313, WalletGrouping.kt lines 286-390
- [ ] Test UI rendering after each file modification
- [ ] Run `./gradlew build` and `./gradlew lint` to verify clean removal
- [ ] Check that physical wallet icons and individual form displays remain intact
- [ ] Verify Material 3 card spacing remains consistent in wallet list
- [ ] Follow existing code removal patterns from project (clean deletion, no commented code)
- [ ] No test files need modification (no existing coverage)
- [ ] Use conditional Compose rendering removal pattern: delete entire `if` block in WalletListScreen.kt

**Context Preservation**:  
- [ ] PhysicalFormSummaryCard shows portfolio allocation with form icons, percentages, progress bars
- [ ] Component uses MaterialTheme.colorScheme.surfaceVariant for card background
- [ ] Progress indicators use form-specific colors (Gold for precious metals, primary for fiat, etc.)
- [ ] ViewModel methods aggregate physical wallet data by PhysicalForm enum
- [ ] Feature only displays when `uiState.showPhysicalWallets && uiState.wallets.isNotEmpty()`
- [ ] No imports need cleanup - all components in same package (com.axeven.profiteerapp.ui.wallet)
- [ ] Related analytics functionality exists in separate TransactionAnalyticsScreen (don't affect)

**Validation Commands**:
```bash
./gradlew build           # Verify compilation passes  
./gradlew lint           # Verify no lint warnings
./gradlew test           # Ensure no test failures (none expected)
```

**Code References**:
- **Remove from WalletListScreen.kt**: Lines 146-153 (conditional PhysicalFormSummaryCard block)
- **Remove from WalletListViewModel.kt**: Lines 291-313 (Analytics Methods section)  
- **Remove from WalletGrouping.kt**: Lines 286-390 (PhysicalFormSummaryCard component and documentation)

---

**Implementation Confidence Score**: 9/10
- Simple feature removal with clear boundaries
- No complex dependencies or integration points  
- Well-defined file locations and line numbers
- Existing patterns in codebase for clean deletions
- Comprehensive validation strategy provided