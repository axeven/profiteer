# Product Requirement Prompt (PRP) Template

## 1. Overview
**Brief Description**: Create a dedicated wallet detail page that displays wallet balance, monthly debit/credit summary, transaction history, and provides transaction management capabilities for individual wallets

**Priority**: Medium

**Estimated Complexity**: Medium

**Expected Development Time**: 2-3 days

## 2. Context & Background
**Current State**: 
- Users can view wallet lists in the wallet list screen showing basic wallet information
- Home screen shows aggregated balance across all wallets
- Individual wallet details are not accessible beyond basic information in the list

**Problem Statement**: Users need a detailed view of individual wallets to track specific wallet performance, view transaction history, monitor monthly activity patterns, and manage transactions directly within the wallet context for better financial management.

**User Impact**: Personal finance users managing multiple wallets/accounts will be able to drill down into specific wallet details, edit existing transactions, and create new transactions directly from the wallet context, improving their ability to track and manage wallet-specific finances.

**Business Value**: Enhanced user engagement through detailed wallet insights, improved user retention by providing comprehensive financial tracking capabilities, and foundation for future analytics features.

## 3. Detailed Requirements

### 3.1 Functional Requirements
**Core Features**:
- [ ] Display wallet balance prominently at the top of the page
- [ ] Show monthly debit and credit summary for the current month
- [ ] Display transaction list filtered by the selected wallet, ordered by transaction date (descending)
- [ ] Support currency toggle between default currency and wallet's native currency
- [ ] Provide navigation back to wallet list
- [ ] Handle both Physical and Logical wallet types appropriately
- [ ] Enable editing of individual transactions directly from the transaction list
- [ ] Provide quick action buttons to add income transactions to the current wallet
- [ ] Provide quick action buttons to add expense transactions to the current wallet
- [ ] Provide quick action buttons to add transfer transactions from the current wallet to another wallet

**User Stories**:
- As a user, I want to view detailed information about a specific wallet so that I can track its performance individually
- As a user, I want to see my monthly spending and income for a specific wallet so that I can monitor my budget
- As a user, I want to view all transactions for a specific wallet so that I can review my transaction history
- As a user, I want to toggle between default currency and wallet currency so that I can view amounts in my preferred format
- As a user, I want to edit transactions directly from the wallet detail page so that I can make corrections in context
- As a user, I want to quickly add income transactions to the current wallet so that I can record earnings efficiently
- As a user, I want to quickly add expense transactions to the current wallet so that I can record spending efficiently
- As a user, I want to create transfer transactions from the current wallet so that I can move money between accounts

**Business Logic**:
- Monthly debit/credit calculations should be based on current calendar month
- Transaction filtering should include all transactions where the wallet is affected (primary wallet or in affectedWalletIds)
- Currency conversion should use the same logic as home screen (default rates with monthly fallback)
- Transaction amounts should respect the transaction date, not creation date
- Empty states should be handled gracefully (no transactions, zero balance)
- Quick action buttons should pre-fill the wallet selection for new transactions
- Transfer transactions should default the source wallet to the current wallet
- Transaction editing should maintain the same validation rules as the main transaction screens
- All transaction operations should refresh the wallet detail data in real-time

### 3.2 Technical Requirements
**Architecture Considerations**:
- Follow existing MVVM pattern with dedicated ViewModel for wallet detail
- Reuse existing repository patterns for data access
- Integrate with existing navigation structure in MainActivity
- Maintain consistency with existing UI patterns and theming
- Leverage existing transaction creation and editing navigation flows
- Ensure proper state management for navigation between wallet detail and transaction screens

**Data Requirements**:
- No new data models required - leverage existing Transaction and Wallet models
- Enhance TransactionRepository to support wallet-specific queries if needed
- Utilize existing CurrencyRate system for conversion logic
- Integrate with existing UserPreferences for default currency

**API Requirements**:
- Enhance existing Firestore queries to support efficient wallet-specific transaction filtering
- Leverage existing real-time listeners for live data updates
- No new external API integrations required

### 3.3 UI/UX Requirements
**Design Specifications**:
- Follow Material 3 design system consistent with existing screens
- Top app bar with wallet name as title and back navigation
- Prominent balance card similar to home screen design
- Monthly summary section with debit/credit breakdown
- Quick action section with buttons for Add Income, Add Expense, and Add Transfer
- Transaction list using existing TransactionItem component pattern with click handlers for editing
- Currency toggle button (similar to settings pattern)
- Loading states and error handling consistent with existing screens
- Floating Action Button (FAB) or quick action bar for transaction creation options

**Visual Design**:
- Primary color scheme consistent with app theme
- Card-based layout matching existing screen patterns
- Clear visual hierarchy: Balance → Monthly Summary → Quick Actions → Transactions
- Action buttons styled consistently with home screen quick actions
- Responsive design supporting various screen sizes
- Smooth transitions and navigation animations
- Clear visual distinction between different action types (income/expense/transfer)

## 4. Implementation Guidance

### 4.1 Suggested Approach
**Phase 1**: Core Infrastructure Setup
- [ ] Add WALLET_DETAIL to AppScreen enum in MainActivity.kt
- [ ] Create WalletDetailScreen.kt composable in ui/wallet package
- [ ] Create WalletDetailViewModel.kt in viewmodel package
- [ ] Add navigation handling in MainActivity.kt ProfiteerApp composable
- [ ] Wire up navigation from WalletListScreen to WalletDetailScreen

**Phase 2**: Data Layer Implementation
- [ ] Enhance TransactionRepository with getWalletTransactions method (if not already available)
- [ ] Implement WalletDetailViewModel with state management
- [ ] Add currency conversion logic for wallet detail view
- [ ] Implement monthly debit/credit calculation logic
- [ ] Add proper error handling and loading states

**Phase 3**: UI Implementation and Polish
- [ ] Create wallet detail UI layout with balance card
- [ ] Implement monthly summary section
- [ ] Add quick action buttons section for transaction creation
- [ ] Integrate transaction list with proper filtering and click handlers
- [ ] Add currency toggle functionality
- [ ] Implement navigation to transaction creation/editing screens
- [ ] Implement proper navigation and back handling
- [ ] Add loading states and error handling UI
- [ ] Test complete workflow from wallet detail to transaction operations and back

### 4.2 Files to Focus On
**Primary Files**:
- `app/src/main/java/com/axeven/profiteerapp/MainActivity.kt` - Add new screen enum and navigation logic for wallet detail and transaction operations
- `app/src/main/java/com/axeven/profiteerapp/ui/wallet/WalletDetailScreen.kt` - Main UI implementation with quick actions (new file)
- `app/src/main/java/com/axeven/profiteerapp/viewmodel/WalletDetailViewModel.kt` - Business logic and state management (new file)
- `app/src/main/java/com/axeven/profiteerapp/ui/wallet/WalletListScreen.kt` - Add click handlers for navigation

**Secondary Files**:
- `app/src/main/java/com/axeven/profiteerapp/data/repository/TransactionRepository.kt` - May need enhancements for wallet filtering
- `app/src/main/java/com/axeven/profiteerapp/ui/home/HomeScreen.kt` - Reference for TransactionItem component and quick actions usage
- `app/src/main/java/com/axeven/profiteerapp/ui/transaction/CreateTransactionScreen.kt` - Reference for transaction creation navigation patterns
- `app/src/main/java/com/axeven/profiteerapp/ui/transaction/EditTransactionScreen.kt` - Reference for transaction editing navigation patterns
- `app/src/main/java/com/axeven/profiteerapp/utils/NumberFormatter.kt` - Currency formatting utilities

**Related Documentation**:
- `docs/pages/wallet_detail_page.md` - Feature specification reference
- `CLAUDE.md` - Project architecture and conventions

### 4.3 Dependencies & Prerequisites
**Required Before Starting**:
- [ ] Existing wallet list functionality is working
- [ ] Transaction repository and models are stable
- [ ] Currency conversion system is functional
- [ ] Navigation structure in MainActivity is understood

**Potential Blockers**:
- Transaction filtering performance with large datasets - mitigate by implementing proper Firestore indexing
- Currency conversion edge cases - mitigate by reusing existing home screen logic
- Navigation state management complexity - mitigate by following existing patterns

## 5. Acceptance Criteria

### 5.1 Definition of Done
- [ ] All functional requirements implemented
- [ ] Code follows project conventions and patterns
- [ ] Unit tests written and passing (where applicable)
- [ ] UI matches design specifications and app theme
- [ ] Performance requirements met (smooth scrolling, quick loading)
- [ ] Error handling implemented
- [ ] Navigation flow works correctly
- [ ] Currency conversion works properly

### 5.2 Test Scenarios
**Happy Path**:
1. User navigates from wallet list to wallet detail page
2. Wallet balance, monthly summary, and transactions display correctly
3. Currency toggle works between default and wallet currency
4. Quick action buttons navigate to transaction creation with pre-filled wallet
5. Transaction list items navigate to edit transaction screen
6. Back navigation returns to wallet list maintaining proper state
7. Real-time updates work when transactions are created, edited, or deleted
8. Transaction operations refresh wallet detail data immediately

**Edge Cases**:
1. Wallet with zero balance displays correctly
2. Wallet with no transactions shows appropriate empty state
3. Wallet in different currency converts properly
4. Very long transaction list scrolls smoothly
5. Missing currency rates show appropriate warnings

**Error Cases**:
1. Network connectivity issues show proper error states
2. Invalid wallet ID handles gracefully
3. Firestore permission errors display user-friendly messages

### 5.3 Validation Methods
**Manual Testing**:
- [ ] Test navigation flow from wallet list to detail and back
- [ ] Verify balance and calculations are accurate
- [ ] Test currency toggle functionality
- [ ] Validate transaction filtering and ordering
- [ ] Test with different wallet types (Physical/Logical)
- [ ] Test quick action navigation to transaction creation screens
- [ ] Test transaction editing from wallet detail transaction list
- [ ] Verify wallet pre-selection in transaction forms when navigating from wallet detail
- [ ] Test complete round-trip: wallet detail → create transaction → back to wallet detail

**Automated Testing**:
- [ ] ViewModel unit tests for business logic
- [ ] Currency conversion logic tests
- [ ] Monthly calculation tests
- [ ] Navigation logic tests for transaction operations
- [ ] Repository integration tests (if applicable)

## 6. Constraints & Considerations

### 6.1 Technical Constraints
- Must work within existing Jetpack Compose UI framework
- Firebase Firestore query limitations and performance considerations
- Android SDK compatibility (Min SDK 24, Target SDK 36)
- Memory constraints for large transaction lists

### 6.2 Business Constraints
- Must maintain consistency with existing app design language
- Should not impact performance of other screens
- Must handle multi-currency scenarios properly

### 6.3 User Experience Constraints
- Loading times should be under 2 seconds for typical datasets
- Smooth navigation transitions
- Accessible design following Material 3 guidelines
- Support for various screen sizes and orientations

## 7. Risk Assessment

### 7.1 Technical Risks
**Risk 1**: Performance degradation with large transaction datasets
- **Probability**: Medium
- **Impact**: Medium
- **Mitigation**: Implement pagination or lazy loading, optimize Firestore queries with proper indexing

**Risk 2**: Currency conversion edge cases causing display issues
- **Probability**: Low
- **Impact**: Medium
- **Mitigation**: Reuse and thoroughly test existing currency conversion logic from home screen

**Risk 3**: Complex state management for real-time updates
- **Probability**: Low
- **Impact**: Medium
- **Mitigation**: Follow existing patterns from HomeViewModel and use proven Flow-based state management

### 7.2 Business Risks
**Risk 1**: Feature complexity may delay other priorities
- **Mitigation**: Implement in phases, deliver MVP first with enhancements later

## 8. Success Metrics
**Quantitative Metrics**:
- Page load time: < 2 seconds for 90% of cases
- Zero critical bugs in first week after release
- Smooth 60fps scrolling performance

**Qualitative Metrics**:
- User can successfully navigate to and use wallet detail page
- Currency conversion displays accurate amounts
- Transaction filtering works correctly for all wallet types

## 9. Future Considerations
**Potential Enhancements**:
- Transaction filtering by date range, type, or category
- Export functionality for wallet transactions
- Graphical charts for spending patterns
- Comparison view between multiple wallets
- Search functionality within wallet transactions
- Bulk transaction operations (delete multiple, move to different wallet)
- Quick transaction templates for frequently used transaction types
- Spending goals and budget alerts for individual wallets

**Scalability Considerations**:
- Pagination strategy for wallets with thousands of transactions
- Caching strategy for frequently accessed wallet data
- Optimized queries to minimize Firestore read costs

## 10. Claude Implementation Notes
**Optimization Tips for AI Development**:
- [ ] Reference existing TransactionItem component from HomeScreen.kt for consistency
- [ ] Follow existing ViewModel patterns from HomeViewModel.kt and WalletListViewModel.kt
- [ ] Use existing currency conversion logic from HomeViewModel.calculateConvertedBalance()
- [ ] Maintain existing navigation patterns from MainActivity.kt for transaction screens
- [ ] Reuse existing quick action button design from HomeScreen.kt
- [ ] Reuse existing card designs and spacing from HomeScreen.kt and WalletListScreen.kt
- [ ] Follow existing error handling patterns from current ViewModels
- [ ] Use existing NumberFormatter utilities for consistent currency display
- [ ] Implement proper Firestore listeners following existing repository patterns
- [ ] Reference transaction creation/editing screens for navigation parameter patterns
- [ ] Ensure consistent state management when navigating between wallet detail and transaction screens

**Context Preservation**:
- [ ] Reference CLAUDE.md for project architecture and build commands
- [ ] Follow existing MVVM architecture with Hilt dependency injection
- [ ] Maintain consistency with Material 3 theming from ui/theme package
- [ ] Use existing data models (Transaction, Wallet, CurrencyRate) without modifications
- [ ] Follow existing Firestore integration patterns for real-time updates
- [ ] Implement proper lifecycle management following existing screen patterns

---

## Implementation Priority
This feature should be implemented after any existing bugs are resolved and provides a solid foundation for future wallet management enhancements. The phased approach allows for incremental delivery and testing.