# Transaction List Page - Product Requirement Prompt (PRP)

## 1. Overview
**Brief Description**: Create a comprehensive transaction list page that displays all user transactions, ordered by date descending, grouped by transaction date, with compact filters for date range, logical wallet, physical wallet, and tag selection.

**Priority**: High

**Estimated Complexity**: Complex

**Expected Development Time**: 1-2 days

## 2. Context & Background
**Current State**: Currently, transactions are displayed in limited fashion on the home screen (last 20 transactions) and within individual wallet detail pages. There's no comprehensive view of all transactions with filtering capabilities.

**Problem Statement**: Users need a centralized location to view, filter, and manage all their transactions. The current scattered transaction views make it difficult to analyze spending patterns, find specific transactions, or manage large transaction datasets effectively.

**User Impact**: All users will benefit from having a comprehensive transaction management interface that allows them to quickly find, filter, and edit transactions based on various criteria.

**Business Value**: This feature enhances user experience by providing better financial visibility and transaction management capabilities, which are core to a personal finance application.

## 3. Detailed Requirements

### 3.1 Functional Requirements
**Core Features**:
- [ ] Display all user transactions in chronological order (newest first)
- [ ] Group transactions by transaction date with collapsible sections
- [ ] Implement compact filter bar with date range, wallet, and tag filters
- [ ] Navigate to transaction edit screen when transaction is tapped
- [ ] Add transaction list access via quick actions on home screen
- [ ] Support real-time updates when transactions are modified
- [ ] Handle empty states when no transactions match filters

**User Stories**:
- As a user, I want to see all my transactions in one place so that I can get a comprehensive view of my financial activity
- As a user, I want to filter transactions by date range so that I can analyze specific time periods
- As a user, I want to filter by wallet so that I can see transactions for specific accounts
- As a user, I want to filter by tags so that I can analyze spending by category
- As a user, I want to tap on a transaction to edit it so that I can correct mistakes or add details
- As a user, I want transactions grouped by date so that I can easily see daily transaction patterns

**Business Logic**:
- Transactions must be ordered by `transactionDate` descending, with fallback to `createdAt` for legacy transactions
- Date grouping should use the transaction date, not creation date
- Filter combinations should work together (AND logic - transactions must match ALL active filters)
- Empty filter states should show all transactions
- Transaction amounts should be displayed in user's default currency
- Transfer transactions should show appropriate context (source/destination wallet names)

### 3.2 Technical Requirements
**Architecture Considerations**:
- Follow existing MVVM pattern with TransactionListViewModel and TransactionListUiState
- Use Jetpack Compose for UI implementation
- Integrate with existing navigation system (AppScreen enum in MainActivity)
- Leverage existing Transaction and Wallet data models
- Maintain consistency with existing UI components and patterns

**Data Requirements**:
- Utilize existing Transaction data model with fields: id, title, amount, tags, transactionDate, type, walletId, affectedWalletIds
- Use existing Wallet data model for filter options
- No new data models required - leverage existing repository methods
- Implement efficient data querying to avoid loading unnecessary transactions when filters are active

**API Requirements**:
- Extend existing TransactionRepository with filtering methods if needed
- Utilize existing `getUserTransactions()` and `getUserTransactionsForCalculations()` methods
- Leverage existing WalletRepository for filter option population
- No new Firebase collections or external APIs required

### 3.3 UI/UX Requirements
**Design Specifications**:
- Follow Material 3 design patterns consistent with existing app design
- Implement compact filter bar at the top using FilterChips and dropdowns
- Use LazyColumn for transaction list with proper scroll performance
- Group header cards should be tappable to expand/collapse date groups
- Transaction items should reuse existing TransactionItem component from HomeScreen
- Implement proper loading states, empty states, and error handling

**Visual Design**:
- Use existing app color scheme and typography
- Filter chips should be horizontally scrollable if they exceed screen width
- Selected filters should be visually distinct and show clear state
- Group headers should have subtle background color to distinguish from individual transactions
- Maintain accessibility with proper content descriptions and contrast ratios

## 4. Implementation Guidance

### 4.1 Suggested Approach
**Phase 1**: Core Infrastructure and Navigation
- [ ] Add TRANSACTION_LIST to AppScreen enum in MainActivity.kt
- [ ] Create TransactionListScreen.kt composable
- [ ] Create TransactionListViewModel.kt with basic state management
- [ ] Create TransactionListUiState.kt data class
- [ ] Add navigation route from HomeScreen quick actions
- [ ] Implement basic transaction loading and display

**Phase 2**: Filtering Implementation
- [ ] Create compact filter UI components (DateRangeFilter, WalletFilter, TagFilter)
- [ ] Implement DateRangePicker using Material 3 DateRangePickerDialog
- [ ] Add wallet selection dropdowns using existing PhysicalFormSelector pattern
- [ ] Implement tag filter using FilterChips with existing tag calculation logic
- [ ] Wire up filter state management in ViewModel
- [ ] Add filter application logic to transaction data stream

**Phase 3**: Grouping and Polish
- [ ] Implement transaction grouping by date functionality
- [ ] Add expand/collapse functionality for date groups
- [ ] Implement transaction tap navigation to EditTransactionScreen
- [ ] Add proper loading states, empty states, and error handling
- [ ] Performance optimization and testing
- [ ] Integration testing with existing transaction editing flow

### 4.2 Files to Focus On

**Primary Files**:
- `app/src/main/java/com/axeven/profiteerapp/MainActivity.kt` - Add TRANSACTION_LIST screen and navigation
- `app/src/main/java/com/axeven/profiteerapp/ui/transaction/TransactionListScreen.kt` - Main screen implementation
- `app/src/main/java/com/axeven/profiteerapp/viewmodel/TransactionListViewModel.kt` - Business logic and state management
- `app/src/main/java/com/axeven/profiteerapp/ui/home/HomeScreen.kt` - Add quick action for transaction list

**Secondary Files**:
- `app/src/main/java/com/axeven/profiteerapp/data/repository/TransactionRepository.kt` - May need filtering extensions
- `app/src/main/java/com/axeven/profiteerapp/ui/transaction/CreateTransactionScreen.kt` - Reference for DateRangePicker implementation
- `app/src/main/java/com/axeven/profiteerapp/ui/wallet/PhysicalFormSelector.kt` - Pattern for dropdown filters
- `app/src/main/java/com/axeven/profiteerapp/ui/wallet/WalletGrouping.kt` - Pattern for grouping and FilterChip usage

**Related Documentation**:
- `docs/pages/homepage.md` - Quick actions integration
- `docs/pages/transaction_concepts.md` - Transaction system understanding
- `docs/architecture.md` - MVVM patterns and navigation

### 4.3 Dependencies & Prerequisites
**Required Before Starting**:
- [ ] Understanding of existing Transaction and Wallet data models
- [ ] Familiarity with current transaction editing flow
- [ ] Knowledge of Material 3 DateRangePicker component
- [ ] Understanding of existing filter patterns in WalletListScreen

**Potential Blockers**:
- **Firestore Query Limitations**: Complex filtering may require composite indexes - Mitigation: Start with client-side filtering and optimize if needed
- **Performance with Large Transaction Sets**: Large datasets may affect scroll performance - Mitigation: Implement pagination or virtual scrolling if needed

## 5. Acceptance Criteria

### 5.1 Definition of Done
- [ ] All functional requirements implemented and working
- [ ] Transaction list displays all user transactions ordered by date descending
- [ ] Date grouping works with expand/collapse functionality
- [ ] All four filters (date range, physical wallet, logical wallet, tag) work independently and together
- [ ] Transaction tap navigation to edit screen works correctly
- [ ] Quick action added to home screen and navigation works
- [ ] Code follows existing MVVM patterns and naming conventions
- [ ] Unit tests written for ViewModel logic
- [ ] UI tests written for critical user flows
- [ ] Performance is acceptable with large transaction datasets (100+ transactions)
- [ ] Accessibility features implemented (content descriptions, proper focus)
- [ ] Error states and empty states handled gracefully

### 5.2 Test Scenarios
**Happy Path**:
1. User taps "Transaction List" quick action from home screen
2. Screen loads showing all transactions grouped by date, newest first
3. User applies date range filter - only transactions in range are shown
4. User applies wallet filter - only transactions from selected wallet are shown
5. User applies tag filter - only transactions with selected tags are shown
6. User taps on a transaction - navigates to edit screen
7. User edits transaction and saves - returns to list with updated data

**Edge Cases**:
1. User has no transactions - shows empty state with helpful message
2. User applies filters that result in no matches - shows appropriate empty state
3. User has transactions without dates - they appear in a "No Date" group at the bottom
4. User has very long transaction titles or many tags - text is properly truncated
5. User rotates device - filters and scroll position are preserved

**Error Cases**:
1. Network error while loading transactions - shows retry option
2. Error while applying filters - shows error message and resets to previous state
3. Error during navigation to edit screen - shows error and stays on list

### 5.3 Validation Methods
**Manual Testing**:
- [ ] Navigate from home screen to transaction list
- [ ] Test all filter combinations with various transaction datasets
- [ ] Verify transaction editing navigation and return flow
- [ ] Test expand/collapse functionality for date groups
- [ ] Verify real-time updates when transactions are modified elsewhere
- [ ] Test with empty transaction list
- [ ] Test with large transaction datasets (100+ transactions)
- [ ] Verify accessibility with screen reader
- [ ] Test on different screen sizes and orientations

**Automated Testing**:
- [ ] Unit tests for TransactionListViewModel filtering logic
- [ ] Unit tests for date grouping functionality
- [ ] Unit tests for filter state management
- [ ] UI tests for navigation flow from home screen
- [ ] UI tests for basic transaction display and grouping
- [ ] Integration tests for filter application
- [ ] Performance tests for large datasets

## 6. Constraints & Considerations

### 6.1 Technical Constraints
- **Firestore Limitations**: Complex queries may require composite indexes or client-side filtering
- **Android Memory Limits**: Large transaction lists must be handled efficiently to avoid OOM issues
- **Material 3 DateRangePicker**: Requires API level considerations and proper dependency versions
- **Jetpack Compose Performance**: LazyColumn with grouping requires careful implementation to maintain scroll performance

### 6.2 Business Constraints
- **Development Timeline**: Must be completed within 1-2 days to align with project roadmap
- **User Experience Standards**: Must maintain consistency with existing app UI/UX patterns
- **Data Privacy**: All filtering and display must respect user data isolation principles

### 6.3 User Experience Constraints
- **Accessibility Standards**: Must support screen readers and keyboard navigation
- **Performance Standards**: Screen must load within 2 seconds and scroll smoothly
- **Mobile-First Design**: Must work well on small screens with touch interaction
- **Consistency**: Must feel like a natural part of the existing application

## 7. Risk Assessment

### 7.1 Technical Risks
**Risk 1**: Firestore query complexity with multiple filters
- **Probability**: Medium
- **Impact**: Medium
- **Mitigation**: Start with client-side filtering, monitor performance, implement server-side optimization if needed

**Risk 2**: Performance degradation with large transaction datasets
- **Probability**: Medium
- **Impact**: High
- **Mitigation**: Implement efficient LazyColumn grouping, consider pagination for very large datasets

**Risk 3**: Complex state management with multiple filter interactions
- **Probability**: Low
- **Impact**: Medium
- **Mitigation**: Follow existing ViewModel patterns, write comprehensive unit tests for state logic

### 7.2 Business Risks
**Risk 1**: Feature scope creep during development
- **Mitigation**: Stick to defined requirements, document any additional requests for future iterations

**Risk 2**: Inconsistency with existing user experience
- **Mitigation**: Reference existing UI patterns extensively, conduct design review before implementation

## 8. Success Metrics
**Quantitative Metrics**:
- **Load Time**: Transaction list loads within 2 seconds for datasets up to 500 transactions
- **Memory Usage**: No memory leaks during extended usage sessions
- **Filter Response Time**: Filters apply within 500ms for datasets up to 200 transactions
- **Navigation Speed**: Tap-to-edit navigation completes within 300ms

**Qualitative Metrics**:
- **User Experience**: Seamless integration with existing app flow
- **Visual Consistency**: Matches existing app design language
- **Accessibility Compliance**: Passes Android accessibility scanner
- **Performance Feel**: Smooth scrolling and responsive interactions

## 9. Future Considerations
**Potential Enhancements**:
- **Advanced Search**: Full-text search within transaction titles and notes
- **Export Functionality**: Export filtered transaction lists to CSV or PDF
- **Bulk Operations**: Select multiple transactions for bulk editing or deletion
- **Custom Sorting**: Allow sorting by amount, title, or other fields
- **Saved Filters**: Save frequently used filter combinations
- **Transaction Analytics**: Integrate with reporting system for quick insights

**Scalability Considerations**:
- **Database Optimization**: May need Firestore composite indexes as user base grows
- **Caching Strategy**: Consider implementing transaction caching for better performance
- **Pagination**: May need server-side pagination for users with thousands of transactions
- **Background Sync**: Consider background data synchronization for large datasets

## 10. Claude Implementation Notes

### 10.1 Existing Code Patterns to Follow

**Navigation Pattern** (`MainActivity.kt:40-42`):
```kotlin
enum class AppScreen {
    HOME, SETTINGS, CREATE_TRANSACTION, EDIT_TRANSACTION, WALLET_LIST, WALLET_DETAIL, REPORTS
}
```
Add `TRANSACTION_LIST` to this enum and follow the existing navigation structure.

**Transaction Display Pattern** (`HomeScreen.kt:302-463`):
Reuse the existing `TransactionItem` composable which already handles transaction types, amounts, dates, tags, and wallet context properly.

**Filter Chip Pattern** (`WalletGrouping.kt:234-300`):
Follow the `PhysicalFormFilterChips` implementation for creating compact, scrollable filter interfaces.

**Date Picker Pattern** (`CreateTransactionScreen.kt:TransactionDatePickerDialog`):
Use the existing `TransactionDatePickerDialog` as reference for implementing the date range picker.

**ViewModel Pattern** (`HomeViewModel.kt`):
Follow the existing StateFlow and combine() patterns for reactive data management.

### 10.2 Specific Implementation Details

**Filter State Management**:
```kotlin
data class TransactionListUiState(
    val transactions: List<Transaction> = emptyList(),
    val groupedTransactions: Map<String, List<Transaction>> = emptyMap(),
    val wallets: List<Wallet> = emptyList(),
    val availableTags: List<String> = emptyList(),
    val selectedDateRange: Pair<Date?, Date?> = Pair(null, null),
    val selectedPhysicalWallets: Set<String> = emptySet(),
    val selectedLogicalWallets: Set<String> = emptySet(),
    val selectedTags: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null
)
```

**Repository Integration** (`TransactionRepository.kt:21-52`):
Leverage the existing `getUserTransactions()` method but remove the limit and add client-side filtering.

**Date Grouping Logic**:
Reference the grouping patterns from `WalletGrouping.kt:68-110` for implementing transaction grouping by date.

### 10.3 Material 3 Components to Use

**DateRangePicker Integration**:
```kotlin
@OptIn(ExperimentalMaterial3Api::class)
val dateRangePickerState = rememberDateRangePickerState()
```

**FilterChip Implementation**:
```kotlin
FilterChip(
    onClick = { /* toggle filter */ },
    label = { Text("Filter Name") },
    selected = isSelected,
    leadingIcon = if (isSelected) {
        { Icon(Icons.Default.Check, contentDescription = null) }
    } else null
)
```

### 10.4 Performance Considerations

**Efficient LazyColumn with Grouping**:
- Use `items()` with proper key functions for each transaction
- Implement lazy loading for group headers
- Consider `LazyColumn`'s built-in performance optimizations

**Memory Management**:
- Avoid holding references to large datasets in memory
- Use proper StateFlow collection lifecycle management
- Implement proper cleanup in ViewModel onCleared()

### 10.5 Testing Strategy

**Unit Tests**: Focus on TransactionListViewModel filtering logic, date grouping, and state management
**UI Tests**: Test navigation flow, filter interactions, and transaction tap behavior
**Integration Tests**: Test data flow from repository to UI with real transaction data

### 10.6 Documentation Updates Required
- Update `docs/pages/homepage.md` to reflect new quick action
- Update `docs/architecture.md` to include new screen in navigation flow
- Create `docs/pages/transaction_list_page.md` following the pattern of existing page documentation

### 10.7 External Dependencies
**Material 3 DateRangePicker**: Ensure `androidx.compose.material3:material3:1.2.1` or higher is available
**Jetpack Compose**: No additional dependencies beyond existing compose-ui components
**External Libraries**: https://developer.android.com/develop/ui/compose/components/datepickers for DateRangePicker documentation

---

## Confidence Score: 9/10

This PRP provides comprehensive context for successful one-pass implementation including:
- ✅ All existing code patterns identified and referenced with line numbers
- ✅ External Material 3 documentation and implementation examples
- ✅ Detailed technical specifications with data models and UI patterns
- ✅ Clear implementation phases with specific tasks
- ✅ Comprehensive testing strategy and acceptance criteria
- ✅ Risk assessment with mitigation strategies
- ✅ Performance considerations and constraints
- ✅ Future enhancement roadmap

The only minor uncertainty is around potential Firestore query optimization needs, but mitigation strategies are clearly defined.