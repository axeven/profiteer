# Product Requirement Prompt (PRP): Group Transaction List by Date on Wallet Detail Page

## 1. Overview
**Brief Description**: Enhance the wallet detail page transaction list to group transactions by date with collapsible date headers, improving transaction readability and navigation for users with many daily transactions.

**Priority**: Medium

**Estimated Complexity**: Medium

**Expected Development Time**: 4-6 hours

## 2. Context & Background
**Current State**: 
- Wallet detail page displays transactions in a flat list using LazyColumn
- Transactions are filtered by month and sorted chronologically
- Date information appears as small text in transaction item subtitle (e.g., "Tags • Jan 15 • Transfer")
- Users with many transactions per day find it difficult to visually separate daily groups

**Problem Statement**: Users with multiple transactions per day struggle to quickly identify and navigate through their transaction history. The current flat list design makes it hard to distinguish between different days, especially for users with high transaction volumes.

**User Impact**: Personal finance users will be able to quickly scan their transaction history by date, collapse/expand specific days to focus on relevant periods, and better understand their daily spending patterns through visual grouping.

**Business Value**: Enhanced user experience through improved transaction organization, increased user engagement with transaction history review, and foundation for future date-based analytics features.

## 3. Detailed Requirements

### 3.1 Functional Requirements
**Core Features**:
- [ ] Group transactions by date (day level) in wallet detail page transaction list
- [ ] Display collapsible date headers showing date and daily transaction summary
- [ ] Maintain existing transaction filtering by selected month
- [ ] Preserve transaction sorting within each date group (newest first)
- [ ] Show daily transaction count and net amount in date headers
- [ ] Support expand/collapse functionality for each date group
- [ ] Maintain existing transaction item click behavior for editing
- [ ] Handle empty states gracefully (no transactions for selected month)

**User Stories**:
- As a user, I want to see my transactions grouped by date so that I can quickly identify activity on specific days
- As a user, I want to collapse/expand date groups so that I can focus on specific periods of interest
- As a user, I want to see daily summaries (count and net amount) so that I can quickly assess daily activity levels
- As a user, I want the most recent transactions to appear first within each day so that I can see recent activity quickly
- As a user, I want to maintain existing functionality for editing transactions so that my workflow isn't disrupted

**Business Logic**:
- Date grouping should use transaction date (transactionDate ?: createdAt) pattern established in codebase
- Groups should be sorted with most recent dates first (descending order)
- Within each date group, transactions should be sorted by time (newest first)
- Date headers should show format "Day, MMM dd" (e.g., "Monday, Jan 15") for readability
- Daily net amount should account for transaction direction (income positive, expenses negative, transfers contextual)
- Collapse/expand state should persist during user session but reset on app restart
- Transfer transactions should display net effect relative to current wallet (inward positive, outward negative)
- All date calculations should respect user's device timezone
- Empty date groups should not be displayed

### 3.2 Technical Requirements
**Architecture Considerations**:
- Follow existing MVVM pattern with WalletDetailViewModel handling grouping logic
- Maintain existing repository patterns and data flow architecture
- Use existing StateFlow pattern for reactive state management
- Leverage proven grouping patterns from WalletGrouping.kt as reference
- Maintain performance with existing lazy loading in LazyColumn
- Follow Material 3 design system consistency

**Data Requirements**:
- Enhance WalletDetailUiState with grouped transaction data: `Map<String, List<Transaction>>`
- Add expand/collapse state management: `Set<String>` for expanded date keys
- No changes to existing Transaction or Wallet models required
- Utilize existing date formatting patterns from codebase
- Maintain existing month filtering and real-time update capabilities

**API Requirements**:
- No new Firestore queries required - leverage existing wallet transaction listeners
- Maintain existing real-time data updates through existing repository patterns
- No external API integrations needed

### 3.3 UI/UX Requirements
**Design Specifications**:
- Follow existing card-based design patterns from WalletGrouping.kt
- Date headers styled as Material 3 cards with surfaceVariant background color
- Expanded state uses primaryContainer background with alpha for visual distinction
- Transaction items indented 16.dp under date headers (following WalletGrouping.kt pattern)
- Collapse/expand arrows using existing Icons.Default.KeyboardArrowUp/KeyboardArrowDown
- Maintain existing TransactionItem component without modifications
- Support smooth expand/collapse animations

**Visual Design**:
- Date header format: "Day, MMM dd" (e.g., "Monday, Jan 15")
- Display daily transaction count and net amount in header
- Use existing color scheme for positive (primary) and negative (error) amounts
- Arrow icons positioned on right side of date headers
- Header padding consistent with existing card components (20.dp)
- Maintain existing empty state handling with appropriate messaging

## 4. Implementation Guidance

### 4.1 Suggested Approach
**Phase 1**: ViewModel Enhancement and Data Processing
- [ ] Add grouping logic to WalletDetailViewModel based on TransactionAnalyticsViewModel.calculateMonthlyData() pattern
- [ ] Enhance WalletDetailUiState with groupedTransactions: Map<String, List<Transaction>>
- [ ] Add expandedDates: Set<String> to track collapse/expand state
- [ ] Create date formatting utilities following existing SimpleDateFormat patterns
- [ ] Implement daily summary calculations (count and net amount)
- [ ] Add toggle functions for expand/collapse state management

**Phase 2**: UI Implementation
- [ ] Create DateGroupHeader composable based on PhysicalFormGroupHeader pattern from WalletGrouping.kt
- [ ] Update WalletDetailScreen.kt LazyColumn structure to iterate over grouped data
- [ ] Implement expand/collapse logic with visual state indicators
- [ ] Apply 16.dp indentation to transaction items under headers
- [ ] Maintain existing TransactionItem component integration

**Phase 3**: Testing and Refinement
- [ ] Test with various transaction volumes and date distributions
- [ ] Verify performance with large datasets
- [ ] Validate expand/collapse state management
- [ ] Test edge cases (single transactions per day, month boundaries)
- [ ] Ensure smooth animations and responsive interaction

### 4.2 Files to Focus On
**Primary Files**:
- `app/src/main/java/com/axeven/profiteerapp/viewmodel/WalletDetailViewModel.kt` - Add transaction grouping logic and state management
- `app/src/main/java/com/axeven/profiteerapp/ui/wallet/WalletDetailScreen.kt` - Update LazyColumn structure and integrate date headers
- **New file**: `app/src/main/java/com/axeven/profiteerapp/ui/wallet/DateGroupHeader.kt` - Date header component

**Secondary Files**:
- `app/src/main/java/com/axeven/profiteerapp/ui/wallet/WalletGrouping.kt` - Reference for grouping patterns and header styling
- `app/src/main/java/com/axeven/profiteerapp/viewmodel/TransactionAnalyticsViewModel.kt` - Reference for date grouping logic (calculateMonthlyData method lines 128-153)
- `app/src/main/java/com/axeven/profiteerapp/ui/home/HomeScreen.kt` - Reference for existing SimpleDateFormat usage (line 294)

**Related Documentation**:
- `CLAUDE.md` - Project architecture and conventions
- External refs:
  - https://developer.android.com/develop/ui/compose/lists - Official Compose lists documentation
  - https://github.com/alexandr7035/Banking-App-Mock-Compose - Real-world financial app transaction grouping example

### 4.3 Dependencies & Prerequisites
**Required Before Starting**:
- [ ] Existing WalletDetailScreen.kt transaction list functionality is working
- [ ] WalletDetailViewModel state management is stable
- [ ] TransactionItem component is functioning properly
- [ ] Month filtering logic is operational

**Potential Blockers**:
- Performance with large transaction datasets - mitigate by following LazyColumn best practices with stable keys
- Date formatting edge cases across timezones - mitigate by using device timezone consistently
- State management complexity - mitigate by following existing WalletGrouping.kt patterns

## 5. Acceptance Criteria

### 5.1 Definition of Done
- [ ] All functional requirements implemented following existing code patterns
- [ ] Transaction grouping logic tested with various data scenarios
- [ ] UI matches existing design system and component styling
- [ ] Expand/collapse functionality works smoothly with visual feedback
- [ ] Performance maintained with large transaction lists (100+ transactions)
- [ ] No breaking changes to existing transaction editing workflow
- [ ] Code follows project conventions and architecture patterns

### 5.2 Test Scenarios
**Happy Path**:
1. User opens wallet detail page with transactions from multiple days
2. Transactions display grouped by date with headers showing "Day, MMM dd" format
3. Date headers show correct daily transaction count and net amount
4. User can expand/collapse date groups with smooth animations
5. Transactions within each date are sorted with newest first
6. User can click transactions to edit as before
7. Real-time updates work when transactions are added/modified

**Edge Cases**:
1. Wallet with only one transaction per day displays single-item groups correctly
2. Wallet with many transactions per day (10+) displays efficiently
3. Month boundaries handle correctly (transactions from end/start of month)
4. Timezone changes don't break date grouping
5. Empty dates (no transactions) are not displayed
6. Very long transaction lists maintain smooth scrolling

**Error Cases**:
1. Invalid date formats fall back gracefully to fallback date handling
2. Network issues maintain existing error handling patterns
3. Missing date fields use established transactionDate ?: createdAt fallback

### 5.3 Validation Methods
**Manual Testing**:
- [ ] Test with wallets containing 1, 5, 20, and 50+ transactions
- [ ] Verify date grouping accuracy across multiple days
- [ ] Test expand/collapse functionality for all date groups
- [ ] Validate daily summary calculations (count and net amounts)
- [ ] Test transaction editing workflow remains unchanged
- [ ] Verify performance with rapid expand/collapse actions

**Automated Testing**:
- [ ] Unit tests for date grouping logic in ViewModel
- [ ] Unit tests for daily summary calculations
- [ ] Unit tests for expand/collapse state management
- [ ] UI tests for LazyColumn grouped structure (if applicable)

## 6. Constraints & Considerations

### 6.1 Technical Constraints
- Must work within existing Jetpack Compose LazyColumn framework
- Android SDK compatibility (Min SDK 24, Target SDK 36)
- Performance must be maintained for wallets with 100+ transactions
- Memory constraints with expanded transaction groups
- Existing Firestore query patterns must be preserved

### 6.2 Business Constraints
- Implementation should not affect other wallet management features
- Must maintain consistency with existing app design language
- Should not impact existing transaction creation/editing workflows
- Performance impact on wallet detail page load time should be minimal

### 6.3 User Experience Constraints
- Expand/collapse animations should be under 300ms for responsiveness
- Date headers must be clearly distinguishable from transaction items
- Loading states should be consistent with existing wallet detail behavior
- Accessibility support following Material 3 guidelines

## 7. Risk Assessment

### 7.1 Technical Risks
**Risk 1**: Performance degradation with large grouped datasets
- **Probability**: Medium
- **Impact**: Medium
- **Mitigation**: Follow Compose best practices with stable keys, remember computed grouping results, implement efficient key generation

**Risk 2**: Complex state management for expand/collapse with real-time updates
- **Probability**: Low
- **Impact**: Medium
- **Mitigation**: Follow existing WalletGrouping.kt patterns, use proven StateFlow patterns, test state preservation

**Risk 3**: Date grouping edge cases causing UI inconsistencies
- **Probability**: Low
- **Impact**: Low
- **Mitigation**: Use established date fallback patterns, comprehensive timezone testing, follow existing date formatting

### 7.2 Business Risks
**Risk 1**: Feature complexity may impact other wallet detail enhancements
- **Mitigation**: Implement incrementally, maintain backward compatibility, thorough testing

## 8. Success Metrics
**Quantitative Metrics**:
- Page load time impact: < 200ms additional overhead for grouping
- Smooth 60fps scrolling maintained with expanded groups
- Zero critical bugs in first week after implementation

**Qualitative Metrics**:
- Users can quickly locate transactions by date
- Daily summary information provides valuable insights
- Expand/collapse functionality feels responsive and intuitive

## 9. Future Considerations
**Potential Enhancements**:
- Week-level grouping for high-volume users
- Month-level grouping for historical data views
- Search functionality within date groups
- Export by date range functionality
- Daily spending limits and budget alerts
- Quick actions at date header level (e.g., "Add transaction for this day")

**Scalability Considerations**:
- Pagination strategy for wallets with thousands of transactions
- Optimized grouping algorithms for large datasets
- Caching strategy for frequently accessed grouped data

## 10. Claude Implementation Notes
**Optimization Tips for AI Development**:
- [ ] Reference WalletGrouping.kt (lines 63-118) for LazyColumn grouped structure pattern
- [ ] Use PhysicalFormGroupHeader (lines 124-229) as template for DateGroupHeader component
- [ ] Follow TransactionAnalyticsViewModel.calculateMonthlyData() pattern for grouping logic
- [ ] Implement expandedDates Set management similar to expandedForms pattern
- [ ] Use existing SimpleDateFormat patterns from codebase: "MMM dd" from HomeScreen.kt line 294
- [ ] Apply 16.dp indentation pattern from WalletGrouping.kt line 96
- [ ] Follow existing card color patterns: surfaceVariant for normal, primaryContainer.copy(alpha=0.3f) for expanded
- [ ] Maintain existing NumberFormatter.formatCurrency() calls for amounts
- [ ] Use transactionDate ?: createdAt pattern consistently (established in codebase)
- [ ] Remember computed grouping results to prevent recomposition issues

**Context Preservation**:
- [ ] Reference CLAUDE.md for project architecture and build commands (./gradlew build, testing requirements)
- [ ] Follow established MVVM patterns with Hilt dependency injection
- [ ] Use existing Material 3 theming from ui/theme package
- [ ] Maintain consistency with existing Transaction and Wallet models
- [ ] Follow existing Firestore integration patterns for real-time updates
- [ ] Use established date formatting patterns: SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
- [ ] Apply existing empty state handling patterns from WalletDetailScreen.kt lines 170-207

**Critical Implementation Context**:
```kotlin
// Key date grouping pattern to follow:
val groupedTransactions = remember(filteredTransactions) {
    filteredTransactions
        .sortedByDescending { it.transactionDate ?: it.createdAt }
        .groupBy { transaction ->
            val date = transaction.transactionDate ?: transaction.createdAt
            date?.let { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it) } ?: ""
        }
        .filter { it.key.isNotEmpty() }
}

// LazyColumn structure pattern:
LazyColumn {
    groupedTransactions.forEach { (dateKey, transactionsForDate) ->
        item {
            DateGroupHeader(
                dateKey = dateKey,
                transactions = transactionsForDate,
                isExpanded = expandedDates.contains(dateKey),
                onToggleExpanded = { /* toggle logic */ }
            )
        }
        
        if (expandedDates.contains(dateKey)) {
            items(transactionsForDate) { transaction ->
                Box(modifier = Modifier.padding(start = 16.dp)) {
                    TransactionItem(/* existing parameters */)
                }
            }
        }
    }
}
```

---

## Implementation Priority
This feature provides significant user experience improvements for transaction management and should be implemented after current wallet detail page functionality is stable. The implementation follows proven patterns from the codebase and can serve as foundation for future date-based analytics features.

**Confidence Score: 8/10** - High confidence for one-pass implementation success due to comprehensive context, existing patterns to follow, proven grouping logic in codebase, and clear implementation blueprint with specific code examples.