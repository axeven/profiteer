# Product Requirement Prompt (PRP) - Wallet Balance Recalculation Button

## 1. Overview
**Brief Description**: Add a force recalculate (refresh) balance button next to the balance display in the wallet detail page that recalculates wallet balance based on initial balance and all affecting transactions.

**Priority**: Medium

**Estimated Complexity**: Medium

**Expected Development Time**: 3-4 hours

## 2. Context & Background
**Current State**: 
- Wallet balance is stored in Firestore and updated when transactions are created/modified
- Balance display is shown in `WalletBalanceCard` component (line 300 in WalletDetailScreen.kt)
- No manual way to recalculate balance if data becomes inconsistent

**Problem Statement**: 
- Wallet balances may become inconsistent due to data synchronization issues, failed transaction updates, or manual data changes
- Users have no way to force a balance recalculation to ensure accuracy
- No self-healing mechanism exists for balance discrepancies

**User Impact**: 
- Users can manually fix inconsistent wallet balances
- Provides confidence in balance accuracy
- Reduces support tickets for balance discrepancies

**Business Value**: 
- Improves data integrity and user trust
- Provides debugging capability for balance issues
- Enhances app reliability

## 3. Detailed Requirements

### 3.1 Functional Requirements
**Core Features**:
- [ ] Add refresh/recalculate button next to balance display in WalletBalanceCard
- [ ] Implement balance recalculation logic based on initial balance + transaction summation
- [ ] Update wallet balance in Firestore after recalculation
- [ ] Show loading state during recalculation
- [ ] Display success/error feedback to user

**User Stories**:
- As a wallet owner, I want to recalculate my wallet balance so that I can ensure the displayed balance is accurate
- As a user experiencing balance discrepancies, I want to manually trigger balance recalculation so that my wallet shows the correct balance

**Business Logic**:
- New balance = `initialBalance + ∑(income_transactions) - ∑(expense_transactions) + ∑(incoming_transfers) - ∑(outgoing_transfers)`
- For transactions affecting the wallet:
  - INCOME: Add `transaction.amount` 
  - EXPENSE: Subtract `transaction.amount`
  - TRANSFER with `sourceWalletId == walletId`: Subtract `transaction.amount` 
  - TRANSFER with `destinationWalletId == walletId`: Add `transaction.amount`
- Use both `affectedWalletIds` and legacy `walletId` for backward compatibility
- Handle edge cases: empty transaction lists, zero amounts, missing initial balance

### 3.2 Technical Requirements
**Architecture Considerations**:
- Follow MVVM pattern - logic in `WalletDetailViewModel`
- Use existing Repository pattern for data access
- Maintain reactive UI updates with StateFlow

**Data Requirements**:
- No new data models needed
- Use existing `Wallet` model with `balance` and `initialBalance` fields
- Use existing `Transaction` model with type-based amount calculations

**API Requirements**:
- Use existing `TransactionRepository.getWalletTransactions()` method
- Use existing `WalletRepository.updateWallet()` method for persistence
- No external API changes needed

### 3.3 UI/UX Requirements
**Design Specifications**:
- Add IconButton with refresh icon next to balance amount
- Use Material3 `Icons.Default.Refresh` icon
- Position button in the same row as balance display
- Show loading indicator (CircularProgressIndicator) during recalculation
- Display success snackbar or toast after successful recalculation
- Show error message if recalculation fails

**Visual Design**:
- Follow existing Material3 theme colors
- Use `MaterialTheme.colorScheme.primary` for icon color
- Icon size: 20.dp (consistent with existing patterns)
- Maintain proper spacing and alignment with balance text

## 4. Implementation Guidance

### 4.1 Suggested Approach
**Phase 1**: Backend Logic Implementation
- [ ] Add `recalculateBalance()` method to `WalletDetailViewModel`
- [ ] Implement balance calculation logic using transaction summation
- [ ] Add loading and error state management to UI state
- [ ] Add unit tests for balance calculation logic

**Phase 2**: UI Integration
- [ ] Modify `WalletBalanceCard` to include refresh button
- [ ] Add onClick handler to trigger recalculation
- [ ] Implement loading state UI (disable button, show progress)
- [ ] Add success/error feedback mechanisms

**Phase 3**: Testing & Polish
- [ ] Add comprehensive unit tests
- [ ] Manual testing with various transaction scenarios
- [ ] Error handling testing (network issues, permissions)
- [ ] Code review and refinement

### 4.2 Files to Focus On
**Primary Files**:
- `app/src/main/java/com/axeven/profiteerapp/viewmodel/WalletDetailViewModel.kt` - Add recalculation logic
- `app/src/main/java/com/axeven/profiteerapp/ui/wallet/WalletDetailScreen.kt` - Modify WalletBalanceCard UI

**Secondary Files**:
- `app/src/main/java/com/axeven/profiteerapp/data/repository/WalletRepository.kt` - Verify updateWallet method
- `app/src/main/java/com/axeven/profiteerapp/data/repository/TransactionRepository.kt` - Verify getWalletTransactions method

**Related Documentation**:
- `docs/pages/wallet_detail_page.md` - Wallet detail functionality
- `CLAUDE.md` - Business logic and validation rules

### 4.3 Dependencies & Prerequisites
**Required Before Starting**:
- [ ] Understanding of existing balance calculation logic in WalletDetailViewModel
- [ ] Familiarity with Material3 button patterns in the codebase
- [ ] Knowledge of existing transaction summation logic (lines 104-123 in WalletDetailViewModel)

**Potential Blockers**:
- Network connectivity issues during Firestore updates - handle with proper error messages
- Complex transaction history causing performance issues - implement with reasonable limits

## 5. Acceptance Criteria

### 5.1 Definition of Done
- [ ] Refresh button appears next to balance display in wallet detail page
- [ ] Button triggers balance recalculation based on initial balance + transactions
- [ ] Recalculated balance is persisted to Firestore
- [ ] Loading state is shown during operation
- [ ] Success/error feedback is provided to user
- [ ] Unit tests written and passing (>80% coverage for new code)
- [ ] Manual testing completed across different wallet states
- [ ] Code follows existing patterns and conventions
- [ ] Error handling implemented for network and permission issues

### 5.2 Test Scenarios
**Happy Path**:
1. User opens wallet detail page with existing balance
2. User taps refresh button next to balance
3. System calculates: initial balance + sum of income - sum of expenses ± transfers
4. Balance updates in UI and Firestore
5. Success feedback shown to user

**Edge Cases**:
1. Wallet with no transactions - should show initial balance
2. Wallet with only transfers - should calculate based on transfer direction
3. Large number of transactions - should handle performance gracefully
4. Balance calculation results in negative value - should handle appropriately

**Error Cases**:
1. Network failure during recalculation - show error message, don't update balance
2. Firestore permission denied - show appropriate error message
3. Invalid transaction data - log error, continue calculation with valid transactions

### 5.3 Validation Methods
**Manual Testing**:
- [ ] Test with wallet having income transactions only
- [ ] Test with wallet having expense transactions only  
- [ ] Test with wallet having transfer transactions (both directions)
- [ ] Test with wallet having mixed transaction types
- [ ] Test with wallet having no transactions
- [ ] Test network failure scenarios
- [ ] Test rapid button clicking (prevent multiple simultaneous operations)

**Automated Testing**:
- [ ] Unit test for balance calculation logic with various transaction combinations
- [ ] Unit test for loading state management during recalculation
- [ ] Unit test for error handling scenarios
- [ ] Integration test for Firestore update operation

## 6. Constraints & Considerations

### 6.1 Technical Constraints
- Must use existing Firestore structure without schema changes
- Must maintain backward compatibility with legacy walletId field
- Must handle both affectedWalletIds and walletId fields in transactions
- Performance consideration for wallets with large transaction history

### 6.2 Business Constraints
- Should not affect existing balance calculation logic elsewhere in app
- Must maintain data consistency during recalculation operation
- Should provide clear feedback to avoid user confusion

### 6.3 User Experience Constraints  
- Button should be discoverable but not intrusive
- Operation should be fast enough for responsive user experience
- Error messages should be user-friendly and actionable

## 7. Risk Assessment

### 7.1 Technical Risks
**Risk 1**: Balance calculation inconsistency with existing logic
- **Probability**: Medium  
- **Impact**: High
- **Mitigation**: Reuse existing calculation patterns from WalletDetailViewModel lines 104-123

**Risk 2**: Performance issues with large transaction datasets
- **Probability**: Low
- **Impact**: Medium  
- **Mitigation**: Implement reasonable limits, use existing paginated data loading patterns

### 7.2 Business Risks
**Risk 1**: Users accidentally triggering recalculation frequently
- **Mitigation**: Add confirmation dialog or cooldown period for multiple rapid requests

## 8. Success Metrics
**Quantitative Metrics**:
- Balance recalculation completes within 2 seconds for typical wallets
- Error rate < 5% for recalculation operations
- User adoption rate > 20% of wallet detail page views

**Qualitative Metrics**:
- Reduction in balance-related support tickets
- User confidence in balance accuracy
- Positive user feedback on balance reliability

## 9. Future Considerations
**Potential Enhancements**:
- Auto-recalculation trigger when inconsistency detected
- Batch recalculation for multiple wallets
- Balance history tracking to show recalculation events
- Analytics on balance discrepancy patterns

**Scalability Considerations**:
- Monitor performance impact as transaction volume grows
- Consider background processing for wallets with extensive transaction history

## 10. Claude Implementation Notes
**Optimization Tips for AI Development**:
- [ ] Reference existing IconButton pattern in MonthSelector.kt lines 56-69 for button implementation
- [ ] Use existing balance calculation logic from WalletDetailViewModel lines 104-123 as template
- [ ] Follow existing loading state pattern in WalletDetailUiState (isLoading field)
- [ ] Use existing error handling pattern with nullable error field in UI state
- [ ] Follow existing unit test patterns from TransactionAnalyticsViewModelTest.kt
- [ ] Use NumberFormatter.formatCurrency for balance display consistency
- [ ] Follow existing Material3 theming patterns from WalletBalanceCard

**Context Preservation**:
- Reference CLAUDE.md for business logic validation rules
- Follow existing MVVM pattern established in WalletDetailViewModel
- Maintain consistency with existing transaction type handling logic
- Use existing repository methods without modifications
- Follow existing error handling patterns throughout the app

**Key Code References**:
- `WalletDetailScreen.kt:300` - Balance display location for button placement
- `WalletDetailViewModel.kt:104-123` - Existing balance calculation logic to reuse
- `MonthSelector.kt:56-69` - IconButton pattern to follow
- `Wallet.kt:12-13` - Balance and initialBalance fields
- `Transaction.kt:18-22` - Transaction fields for balance calculation
- `TransactionAnalyticsViewModelTest.kt` - Unit testing patterns to follow

---

**Confidence Score**: 8/10 - High confidence for one-pass implementation due to comprehensive context, clear requirements, and extensive code references provided.