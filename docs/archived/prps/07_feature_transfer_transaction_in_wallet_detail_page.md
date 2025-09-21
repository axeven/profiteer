# Product Requirement Prompt (PRP) Template

## 1. Overview
**Brief Description**: Enhance wallet detail page to properly display transfer transactions as income/expense based on wallet role (source/destination) in the transfer

**Priority**: Medium

**Estimated Complexity**: Medium

**Expected Development Time**: 1-2 days

## 2. Context & Background
**Current State**: 
- Wallet detail page displays all transactions affecting a wallet including transfers
- Transfer transactions are shown with their original TRANSFER type and neutral blue styling
- The transaction repository correctly fetches transfers via sourceWalletId and destinationWalletId queries
- Transfer transactions currently don't contribute meaningfully to monthly income/expense calculations from a wallet perspective

**Problem Statement**: Users viewing wallet details cannot easily understand the financial impact of transfer transactions on their specific wallet. A transfer that moves money OUT of the current wallet should appear as an expense, while a transfer that moves money INTO the current wallet should appear as income, providing clearer financial insights.

**User Impact**: Users managing multiple wallets will have better visibility into money flows between their accounts, making it easier to track how transfers affect individual wallet performance and monthly budgets.

**Business Value**: Improved user experience in multi-wallet financial management, clearer transaction categorization, and better monthly budget tracking capabilities that reflect the true impact of inter-wallet transfers.

## 3. Detailed Requirements

### 3.1 Functional Requirements
**Core Features**:
- [ ] Display transfer transactions as "expense" when current wallet is the source (money flowing out)
- [ ] Display transfer transactions as "income" when current wallet is the destination (money flowing in)
- [ ] Update monthly income/expense calculations to include transfer impacts
- [ ] Maintain original transfer transaction data while adapting display representation
- [ ] Show clear visual indicators (icons, colors) that distinguish transfer-based income/expense from regular income/expense
- [ ] Include destination/source wallet information in transaction display for context
- [ ] Preserve ability to edit transfer transactions with full transfer context

**User Stories**:
- As a user, I want transfer transactions to show as expenses when money leaves my wallet so that I understand the financial impact on this specific wallet
- As a user, I want transfer transactions to show as income when money enters my wallet so that I can see how transfers contribute to this wallet's performance
- As a user, I want to easily identify transfer-based transactions so that I can distinguish them from regular income/expense transactions
- As a user, I want monthly summaries to include transfer impacts so that I get accurate financial insights for this wallet
- As a user, I want to see which wallet was the other party in the transfer so that I understand the complete transaction context
- As a user, I want to edit transfer transactions normally so that I can make corrections when needed

**Business Logic**:
- **Transfer Direction Logic**: 
  - If `currentWalletId == transaction.sourceWalletId` → Display as EXPENSE (money flowing out)
  - If `currentWalletId == transaction.destinationWalletId` → Display as INCOME (money flowing in)
- **Amount Display**: 
  - Source wallet: Show negative amount (expense)
  - Destination wallet: Show positive amount (income)
- **Monthly Calculations**:
  - Include transfer amounts in monthly income/expense totals based on wallet role
  - Ensure transfer amounts are counted only once per wallet (not double-counted)
- **Visual Representation**:
  - Use expense styling (red, down arrow) for outgoing transfers
  - Use income styling (green, up arrow) for incoming transfers
  - Add transfer-specific icon or indicator to distinguish from regular transactions
- **Transaction Context**:
  - Display source/destination wallet name in transaction description
  - Maintain original transaction type information for editing purposes
- **Currency Handling**:
  - Use source wallet currency for display consistency
  - Apply currency conversion if wallet display currency differs

### 3.2 Technical Requirements
**Architecture Considerations**:
- Modify WalletDetailViewModel to include transfer direction logic
- Enhance transaction display components to handle transfer representation
- Maintain existing repository layer without breaking changes
- Preserve transaction editing functionality with full transfer context
- Ensure real-time updates work correctly with enhanced display logic

**Data Requirements**:
- No changes to existing Transaction data model required
- Enhance view model state to include transfer direction metadata
- Add computed properties for transfer-based income/expense classification
- Maintain backward compatibility with existing transaction data

**API Requirements**:
- No new Firestore queries required - existing getWalletTransactions handles transfers
- Leverage existing sourceWalletId and destinationWalletId fields
- Utilize existing real-time listeners for immediate updates

### 3.3 UI/UX Requirements
**Design Specifications**:
- **Transfer Income Transactions**:
  - Green color scheme (matching regular income)
  - Up arrow icon with transfer indicator
  - Display format: "Transfer from [Source Wallet Name]"
  - Positive amount display with "+" prefix
- **Transfer Expense Transactions**:
  - Red color scheme (matching regular expense)
  - Down arrow icon with transfer indicator
  - Display format: "Transfer to [Destination Wallet Name]"
  - Negative amount display (no additional "-" needed)
- **Transfer Indicators**:
  - Small transfer icon overlay or secondary icon
  - Subtle visual distinction from regular income/expense
  - Consistent with existing transaction item design patterns
- **Monthly Summary Updates**:
  - Include transfer impacts in income/expense totals
  - Clear breakdown showing regular vs transfer contributions (optional enhancement)

**Visual Design**:
- Maintain Material 3 design consistency
- Use existing color schemes for income/expense with transfer indicators
- Preserve existing transaction list layout and spacing
- Ensure accessibility standards are maintained
- Add subtle transfer badge or icon for clear identification

## 4. Implementation Guidance

### 4.1 Suggested Approach
**Phase 1**: Business Logic Enhancement
- [ ] Create transfer direction detection logic in WalletDetailViewModel
- [ ] Update monthly income/expense calculation to include transfers
- [ ] Add computed properties for transfer-based transaction classification
- [ ] Enhance transaction display data preparation with transfer context

**Phase 2**: UI Component Updates
- [ ] Modify TransactionItem component to handle transfer representation
- [ ] Add transfer-specific visual indicators and styling
- [ ] Update transaction description formatting for transfers
- [ ] Implement transfer direction-based icon and color selection

**Phase 3**: Integration and Testing
- [ ] Update WalletDetailScreen to use enhanced transaction display
- [ ] Test transfer representation across different wallet combinations
- [ ] Verify monthly calculation accuracy with mixed transaction types
- [ ] Ensure transaction editing preserves full transfer functionality
- [ ] Test real-time updates with transfer operations

### 4.2 Files to Focus On
**Primary Files**:
- `app/src/main/java/com/axeven/profiteerapp/viewmodel/WalletDetailViewModel.kt` - Add transfer direction logic and monthly calculation updates
- `app/src/main/java/com/axeven/profiteerapp/ui/home/HomeScreen.kt` - Enhance TransactionItem component for transfer representation
- `app/src/main/java/com/axeven/profiteerapp/ui/wallet/WalletDetailScreen.kt` - Update transaction display integration
- `app/src/main/java/com/axeven/profiteerapp/data/repository/TransactionRepository.kt` - Include transfer transactions when fetching transactions affecting certain wallet

**Secondary Files**:
- `app/src/main/java/com/axeven/profiteerapp/data/model/Transaction.kt` - Reference for transfer field usage
- `app/src/main/java/com/axeven/profiteerapp/utils/NumberFormatter.kt` - Currency formatting for transfer amounts

**Related Documentation**:
- `docs/prps/06_feature_wallet_detail_page.md` - Original wallet detail implementation
- `CLAUDE.md` - Project architecture and conventions

### 4.3 Dependencies & Prerequisites
**Required Before Starting**:
- [ ] Wallet detail page feature (PRP 06) is fully implemented and functional
- [ ] Transfer transaction creation and editing capabilities are working
- [ ] Transaction repository properly handles sourceWalletId and destinationWalletId queries
- [ ] Understanding of current monthly calculation logic in WalletDetailViewModel

**Potential Blockers**:
- Complex transfer scenarios with multiple wallet types - mitigate by thorough testing with Physical/Logical wallet combinations
- Currency conversion edge cases in transfers - mitigate by leveraging existing conversion logic
- Performance impact of enhanced calculations - mitigate by optimizing transfer direction detection

## 5. Acceptance Criteria

### 5.1 Definition of Done
- [ ] Transfer transactions display as income when wallet is destination
- [ ] Transfer transactions display as expense when wallet is source
- [ ] Monthly income/expense totals include transfer impacts accurately
- [ ] Visual indicators clearly distinguish transfer-based transactions
- [ ] Transaction editing functionality remains fully intact
- [ ] Real-time updates work correctly with enhanced display
- [ ] Performance remains acceptable with additional logic
- [ ] Code follows existing patterns and conventions

### 5.2 Test Scenarios
**Happy Path**:
1. User views wallet detail for wallet involved in transfers as both source and destination
2. Transfer transactions display with correct income/expense representation
3. Monthly summary accurately includes transfer impacts
4. Transfer transaction editing works normally
5. Real-time updates reflect transfer operations immediately
6. Currency conversion works correctly for cross-currency transfers

**Edge Cases**:
1. Wallet involved in transfers with deleted/missing counterpart wallets
2. Transfer transactions with zero amounts
3. Transfers between wallets of different types (Physical/Logical)
4. Historical transfers with missing wallet references
5. Rapid transfer operations triggering multiple real-time updates

**Error Cases**:
1. Transfer transactions with malformed sourceWalletId/destinationWalletId
2. Network connectivity issues during transfer operations
3. Currency conversion failures for transfer amounts

### 5.3 Validation Methods
**Manual Testing**:
- [ ] Create transfers between multiple wallets and verify correct representation
- [ ] Test monthly calculation accuracy with various transfer scenarios
- [ ] Verify visual indicators and styling for transfer transactions
- [ ] Test transaction editing from wallet detail with transfers
- [ ] Validate real-time updates during transfer operations
- [ ] Test with different wallet types and currencies

**Automated Testing**:
- [ ] Unit tests for transfer direction detection logic
- [ ] Unit tests for monthly calculation accuracy with transfers
- [ ] Integration tests for transaction display enhancement
- [ ] Performance tests for calculation efficiency

## 6. Constraints & Considerations

### 6.1 Technical Constraints
- Must maintain backward compatibility with existing transaction data
- Cannot modify core transaction repository queries
- Must preserve existing transaction editing functionality
- Performance impact should be minimal for large transaction lists

### 6.2 Business Constraints
- Should not change fundamental transaction data structure
- Must maintain audit trail and transaction history integrity
- Should not affect other parts of the application

### 6.3 User Experience Constraints
- Changes should feel intuitive and natural to users
- Should not confuse users familiar with current transfer display
- Must maintain accessibility standards
- Should provide clear context for transfer operations

## 7. Risk Assessment

### 7.1 Technical Risks
**Risk 1**: Complex transfer direction logic causing performance issues
- **Probability**: Low
- **Impact**: Medium
- **Mitigation**: Optimize calculation logic and cache transfer direction metadata

**Risk 2**: Monthly calculation errors with complex transfer scenarios
- **Probability**: Medium
- **Impact**: High
- **Mitigation**: Comprehensive unit testing and validation of calculation logic

**Risk 3**: Real-time update conflicts with enhanced display logic
- **Probability**: Low
- **Impact**: Medium
- **Mitigation**: Thorough testing of real-time scenarios and proper state management

### 7.2 Business Risks
**Risk 1**: User confusion with changed transfer representation
- **Mitigation**: Clear visual indicators and gradual rollout to monitor user feedback

## 8. Success Metrics
**Quantitative Metrics**:
- Monthly calculation accuracy: 100% correct for transfers
- Performance impact: < 50ms additional processing time
- Zero critical bugs in transfer representation

**Qualitative Metrics**:
- Users can easily understand transfer impact on wallet performance
- Transfer transactions provide clear context about money flow
- Monthly summaries accurately reflect wallet-specific financial activity

## 9. Future Considerations
**Potential Enhancements**:
- **Transfer Flow Visualization**: Graphical representation of money flow between wallets
- **Transfer Analytics**: Insights into inter-wallet transfer patterns
- **Bulk Transfer Operations**: Enhanced support for multiple transfers
- **Transfer Categories**: Categorization of different types of transfers (savings, bill payments, etc.)
- **Transfer Scheduling**: Recurring transfer setup and management

**Scalability Considerations**:
- Efficient handling of wallets with hundreds of transfer transactions
- Optimized calculation logic for complex multi-wallet scenarios
- Caching strategies for frequently accessed transfer direction data

## 10. Claude Implementation Notes
**Optimization Tips for AI Development**:
- [ ] Reference existing monthly calculation logic in WalletDetailViewModel.kt for consistency
- [ ] Follow existing transaction display patterns in HomeScreen.kt TransactionItem component
- [ ] Use existing color schemes and icon patterns for income/expense styling
- [ ] Leverage existing currency conversion utilities from NumberFormatter.kt
- [ ] Maintain existing real-time update patterns and state management
- [ ] Follow existing error handling patterns from current ViewModels
- [ ] Use existing Material 3 theming and design patterns
- [ ] Test with existing transfer creation/editing workflows to ensure compatibility

**Context Preservation**:
- [ ] Reference original wallet detail implementation (PRP 06) for architecture consistency
- [ ] Follow existing MVVM patterns with Hilt dependency injection
- [ ] Maintain consistency with existing transaction repository patterns
- [ ] Use existing Firestore real-time listener patterns
- [ ] Follow existing UI component composition patterns
- [ ] Maintain existing navigation and state management approaches

---

## Implementation Priority
This enhancement should be implemented after the base wallet detail page is stable and tested. It provides significant value for multi-wallet users and creates a foundation for advanced transfer analytics and management features.