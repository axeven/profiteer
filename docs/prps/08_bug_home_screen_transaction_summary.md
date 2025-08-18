# Product Requirement Prompt (PRP) Template

## 1. Overview
**Brief Description**: Fix home screen transaction summary calculations that currently only include recent 50 transactions instead of all transactions for accurate financial reporting

**Priority**: High

**Estimated Complexity**: Medium

**Expected Development Time**: 1 day

## 2. Context & Background
**Current State**: 
- Home screen displays total income and total expenses in the summary cards
- These calculations are performed using `getUserTransactions()` which has a `.limit(50)` restriction
- The summary only reflects the most recent 50 transactions regardless of their date range
- Users see inaccurate financial summaries that don't represent their true financial state
- The 50-transaction limit was likely intended for the "Recent Transactions" list display, not for calculations

**Problem Statement**: Home screen financial summaries (total income and total expenses) are calculated from only the most recent 50 transactions due to a Firestore query limit, providing users with inaccurate and potentially misleading financial data. This creates a critical user trust issue where displayed totals don't match the user's actual financial activity.

**User Impact**: Users cannot rely on the home screen summaries for accurate financial planning, budgeting, or decision-making. Heavy users with more than 50 transactions will see significantly understated totals, leading to poor financial awareness and potential overspending.

**Business Value**: Accurate financial summaries are fundamental to user trust and engagement in a finance app. Fixing this ensures users can rely on the app for real financial insights, improving retention and user satisfaction.

## 3. Detailed Requirements

### 3.1 Functional Requirements
**Core Features**:
- [ ] Separate data fetching logic for UI display (recent transactions list) vs calculations (all transactions)
- [ ] Create dedicated repository method for fetching all user transactions without limits for calculations
- [ ] Implement monthly period filtering for income/expense calculations (current month by default)
- [ ] Add currency conversion for accurate cross-currency financial summaries
- [ ] Include transfer transaction impacts in total calculations (transfers should affect source/destination wallet totals)
- [ ] Maintain existing UI performance by keeping limited transaction list for display

**User Stories**:
- As a user, I want the home screen income/expense totals to reflect ALL my transactions so that I can trust the financial summary
- As a user, I want monthly totals to be calculated from the current month's activity so that I understand my current financial period performance
- As a user, I want transfer transactions to be properly reflected in the totals so that inter-wallet transfers don't skew my income/expense calculations
- As a user, I want accurate currency conversion in totals so that multi-currency activity is properly aggregated
- As a user, I want the recent transactions list to load quickly while having accurate summary calculations

**Business Logic**:
- **Calculation Scope**: Total income/expense should include ALL user transactions, not just recent 50
- **Time Period**: Default to current month calculations, with option for all-time totals
- **Transfer Handling**: 
  - Transfers should not be counted as income/expense in global totals (they're internal movements)
  - OR alternatively, provide wallet-specific context where transfers do impact individual wallet performance
- **Currency Conversion**: Apply consistent conversion rates used elsewhere in the app
- **Performance**: Separate calculation queries from display queries to maintain UI responsiveness
- **Real-time Updates**: Summary calculations should update in real-time as transactions are added/modified

### 3.2 Technical Requirements
**Architecture Considerations**:
- Create separation between "display transactions" (limited) and "calculation transactions" (unlimited)
- Implement efficient Firestore queries that fetch calculation data without overwhelming the UI
- Maintain existing MVVM architecture patterns
- Ensure real-time updates work correctly for both display and calculations
- Consider caching strategies for large transaction datasets

**Data Requirements**:
- No changes to existing Transaction data model required
- Create new repository methods for unlimited transaction fetching
- Implement date-range filtering in repository layer
- Leverage existing currency conversion logic

**API Requirements**:
- New Firestore query method: `getUserTransactionsForCalculations(userId, dateRange?)`
- Enhanced HomeViewModel calculation logic with proper transaction filtering
- Maintain existing `getUserTransactions(userId)` for UI display purposes

### 3.3 UI/UX Requirements
**Design Specifications**:
- No visual changes required to home screen UI components
- Maintain existing loading states and performance characteristics
- Ensure calculation accuracy is transparent to users
- Consider adding period selection UI for summary calculations (future enhancement)

**Performance Requirements**:
- Summary calculations should complete within 2 seconds for typical datasets
- UI responsiveness should not be impacted by calculation complexity
- Memory usage should remain reasonable for large transaction sets

## 4. Implementation Guidance

### 4.1 Suggested Approach
**Phase 1**: Repository Layer Enhancement
- [ ] Create new `getUserTransactionsForCalculations()` method without limit restrictions
- [ ] Add date filtering capabilities to repository methods
- [ ] Implement proper transfer transaction handling in calculations
- [ ] Add enhanced logging for debugging calculation accuracy

**Phase 2**: ViewModel Logic Update
- [ ] Separate calculation logic from display logic in HomeViewModel
- [ ] Implement monthly period filtering for current month calculations
- [ ] Add currency conversion to calculation logic
- [ ] Update real-time data flow to handle both calculation and display data

**Phase 3**: Testing and Optimization
- [ ] Test calculation accuracy with various transaction volumes
- [ ] Verify performance with large datasets (100+, 1000+ transactions)
- [ ] Ensure real-time updates work correctly for both UI and calculations
- [ ] Validate currency conversion accuracy in multi-currency scenarios

### 4.2 Files to Focus On
**Primary Files**:
- `app/src/main/java/com/axeven/profiteerapp/data/repository/TransactionRepository.kt` - Add new unlimited query method
- `app/src/main/java/com/axeven/profiteerapp/viewmodel/HomeViewModel.kt` - Separate calculation and display logic
- `app/src/main/java/com/axeven/profiteerapp/ui/home/HomeScreen.kt` - Verify UI performance is maintained

**Secondary Files**:
- `app/src/main/java/com/axeven/profiteerapp/utils/NumberFormatter.kt` - Currency conversion utilities
- Existing currency conversion logic in HomeViewModel

**Related Documentation**:
- `docs/prps/07_feature_transfer_transaction_in_wallet_detail_page.md` - Transfer handling reference
- `CLAUDE.md` - Project architecture and conventions

### 4.3 Dependencies & Prerequisites
**Required Before Starting**:
- [ ] Understanding of current Firestore query performance characteristics
- [ ] Knowledge of existing currency conversion implementation
- [ ] Clarification on transfer transaction handling in global summaries (include or exclude)

**Potential Blockers**:
- Firestore read costs for unlimited queries - mitigate with efficient date-range filtering
- Performance impact of calculation queries - mitigate with background processing and caching
- Transfer transaction logic complexity - mitigate by following existing wallet detail patterns

## 5. Acceptance Criteria

### 5.1 Definition of Done
- [ ] Home screen totals reflect ALL user transactions, not just recent 50
- [ ] Calculation performance is acceptable (< 2 seconds for typical datasets)
- [ ] Recent transactions list UI performance is maintained
- [ ] Currency conversion works correctly in summary calculations
- [ ] Transfer transactions are handled appropriately in global totals
- [ ] Real-time updates work for both display and calculation data
- [ ] Code follows existing patterns and architectural conventions

### 5.2 Test Scenarios
**Happy Path**:
1. User with 100+ transactions sees accurate totals reflecting all transactions
2. Monthly calculations show correct current month totals
3. Multi-currency transactions are properly converted and aggregated
4. Real-time updates work when transactions are added/modified
5. UI performance remains smooth with calculation changes

**Edge Cases**:
1. User with 1000+ transactions gets accurate calculations without timeout
2. Users with only transfer transactions see appropriate total calculations
3. New user with 0 transactions shows correct zero totals
4. Month boundary calculations work correctly (end of month scenarios)
5. Currency conversion failures gracefully handled in calculations

**Error Cases**:
1. Network connectivity issues during calculation queries
2. Firestore permission errors for unlimited queries
3. Large dataset memory management scenarios

### 5.3 Validation Methods
**Manual Testing**:
- [ ] Create test user with 100+ transactions spanning multiple months
- [ ] Verify home screen totals match manual calculation of all transactions
- [ ] Test with various transaction types including transfers
- [ ] Test multi-currency scenarios with conversion
- [ ] Verify UI performance with calculation changes

**Automated Testing**:
- [ ] Unit tests for calculation logic with various transaction sets
- [ ] Integration tests for repository query methods
- [ ] Performance tests for large transaction datasets
- [ ] Currency conversion accuracy tests

## 6. Constraints & Considerations

### 6.1 Technical Constraints
- Firestore read costs increase with unlimited queries - must implement efficient filtering
- Mobile device memory limitations for large transaction datasets
- Real-time listener performance with multiple data streams
- Existing UI performance standards must be maintained

### 6.2 Business Constraints
- Cannot break existing UI functionality or performance
- Must maintain backward compatibility with existing data
- Should not significantly increase Firestore usage costs
- Must preserve user data integrity during implementation

### 6.3 User Experience Constraints
- Calculation updates should be seamless and not disrupt UI flow
- Loading states should provide appropriate feedback for longer calculations
- Error states should be user-friendly and actionable

## 7. Risk Assessment

### 7.1 Technical Risks
**Risk 1**: Performance degradation with unlimited transaction queries
- **Probability**: Medium
- **Impact**: High
- **Mitigation**: Implement efficient date-range filtering, background processing, and result caching

**Risk 2**: Increased Firestore read costs due to unlimited queries
- **Probability**: High
- **Impact**: Medium
- **Mitigation**: Implement smart caching, efficient query design, and consider monthly aggregation strategies

**Risk 3**: Memory issues with large transaction datasets on mobile devices
- **Probability**: Low
- **Impact**: High
- **Mitigation**: Stream processing, pagination of calculation results, and memory-efficient data handling

### 7.2 Business Risks
**Risk 1**: User confusion if totals change significantly after fix
- **Mitigation**: Clear communication about improved accuracy, potentially with release notes

## 8. Success Metrics
**Quantitative Metrics**:
- Calculation accuracy: 100% match with manual verification
- Performance: < 2 second calculation time for 95% of users
- UI responsiveness: No degradation in existing performance metrics
- Error rate: < 1% calculation failures

**Qualitative Metrics**:
- Users trust the financial summaries displayed on home screen
- Heavy users (50+ transactions) see meaningful improvement in accuracy
- No user complaints about UI performance degradation

## 9. Future Considerations
**Potential Enhancements**:
- **Period Selection**: Allow users to select different time periods for summary calculations (last 30 days, last 3 months, all time)
- **Category Breakdown**: Show income/expense breakdown by transaction categories
- **Trend Analysis**: Month-over-month comparison of income/expense trends
- **Smart Caching**: Implement intelligent caching for frequently accessed calculation data
- **Aggregated Reports**: Pre-calculated monthly/yearly summaries for improved performance

**Scalability Considerations**:
- Design calculation system to handle users with thousands of transactions
- Consider implementing background calculation jobs for heavy users
- Plan for efficient data archiving strategies for long-term users

## 10. Claude Implementation Notes
**Optimization Tips for AI Development**:
- [ ] Reference existing currency conversion logic from HomeViewModel for consistency
- [ ] Follow existing Firestore query patterns from TransactionRepository
- [ ] Use existing date filtering utilities if available
- [ ] Maintain existing real-time update patterns with Flow and StateFlow
- [ ] Reference wallet detail transfer logic for consistent transfer handling
- [ ] Follow existing error handling patterns from repository layer
- [ ] Use existing performance optimization strategies (logging, caching)

**Context Preservation**:
- [ ] Reference CLAUDE.md for project architecture and Firestore patterns
- [ ] Follow existing MVVM patterns with Hilt dependency injection
- [ ] Maintain consistency with existing transaction filtering logic
- [ ] Use existing currency rate repository patterns
- [ ] Follow existing real-time listener management patterns
- [ ] Maintain existing logging and debugging approaches

---

## Implementation Priority
This is a **critical bug fix** that should be prioritized over new features as it affects fundamental app reliability and user trust. The fix will provide immediate value to all users, especially those with significant transaction history.

## Additional Investigation Required
**Before Implementation**:
1. **Transfer Transaction Policy**: Clarify whether transfers should be included in global income/expense totals or treated as neutral (internal movements)
2. **Time Period Definition**: Confirm whether "current month" means calendar month vs rolling 30 days
3. **Performance Benchmarks**: Establish acceptable performance thresholds for calculation queries
4. **Currency Conversion**: Verify which exchange rates to use for historical transaction calculations