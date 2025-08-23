# PRP: Split Transaction Analytics Functionality from Wallet List Page to Dedicated Screen

## Overview

Split transaction analytics functionality from the wallet list page into its own dedicated page, creating a comprehensive transaction analytics dashboard that provides detailed insights into spending patterns, income trends, and financial behavior while maintaining the existing wallet-focused analytics in the wallet list page.

## Context & Rationale

The current `WalletListScreen` contains both wallet management functionality and portfolio analytics (`PhysicalFormSummaryCard`). While this provides basic allocation insights, users need more detailed transaction-level analytics including:
- Spending trends over time
- Income vs expense analysis  
- Tag-based expense categorization
- Monthly/quarterly financial summaries
- Transaction pattern analysis

This separation improves user experience by providing dedicated analytics space while keeping wallet management focused.

## Current State Analysis

### Existing Analytics in WalletListScreen
```kotlin
// File: app/src/main/java/com/axeven/profiteerapp/ui/wallet/WalletGrouping.kt
@Composable
fun PhysicalFormSummaryCard(
    balanceSummary: Map<PhysicalForm, Double>,
    totalBalance: Double,
    defaultCurrency: String
) {
    // Portfolio allocation with percentages and progress bars
    // Color-coded asset types
    // Total balance summary
}
```

### Analytics Methods in WalletListViewModel
```kotlin
// File: app/src/main/java/com/axeven/profiteerapp/viewmodel/WalletListViewModel.kt
// Lines 291-314
fun getPhysicalFormBalanceSummary(): Map<PhysicalForm, Double>
fun getTotalBalanceByForm(physicalForm: PhysicalForm): Double  
fun getWalletCountByForm(): Map<PhysicalForm, Int>
```

### Navigation Structure
```kotlin
// File: app/src/main/java/com/axeven/profiteerapp/MainActivity.kt
enum class AppScreen {
    HOME, SETTINGS, CREATE_TRANSACTION, EDIT_TRANSACTION, 
    WALLET_LIST, WALLET_DETAIL
}
```

## Implementation Blueprint

### 1. Navigation Integration
Add new screen to existing navigation enum and implement navigation flow:

```kotlin
// Add to AppScreen enum in MainActivity.kt
enum class AppScreen {
    HOME, SETTINGS, CREATE_TRANSACTION, EDIT_TRANSACTION, 
    WALLET_LIST, WALLET_DETAIL, TRANSACTION_ANALYTICS // NEW
}

// Navigation integration in WalletListScreen
IconButton(onClick = onNavigateToAnalytics) {
    Icon(Icons.Default.Analytics, contentDescription = "View Analytics")
}
```

### 2. Data Processing Architecture
Create dedicated ViewModel following existing patterns:

```kotlin
@HiltViewModel
class TransactionAnalyticsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val currencyRateRepository: CurrencyRateRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    
    // Similar to WalletListViewModel pattern
    private val _uiState = MutableStateFlow(TransactionAnalyticsUiState())
    val uiState: StateFlow<TransactionAnalyticsUiState> = _uiState.asStateFlow()
    
    // Analytics computation methods
    fun getMonthlyExpenseTrends(months: Int): Flow<List<MonthlyExpense>>
    fun getExpensesByTag(): Flow<Map<String, Double>>
    fun getIncomeVsExpenseComparison(): Flow<IncomeExpenseComparison>
}
```

### 3. UI Component Strategy
Follow existing UI patterns from WalletGrouping.kt and create analytics-specific components:

```kotlin
@Composable
fun MonthlyTrendCard(
    monthlyData: List<MonthlyExpense>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        // Follow existing card styling from PhysicalFormSummaryCard
    ) {
        // Chart implementation using chart library
        // Data visualization similar to progress indicators in existing code
    }
}

@Composable  
fun ExpenseCategoryBreakdown(
    categoryData: Map<String, Double>,
    totalExpenses: Double,
    modifier: Modifier = Modifier
) {
    // Similar structure to PhysicalFormSummaryCard
    // Reuse color coding and percentage calculation patterns
}
```

### 4. Chart Library Integration
Based on research, integrate a Jetpack Compose-compatible chart library:

```gradle
// Add to app/build.gradle.kts dependencies
implementation "io.github.ehsannarmani:compose-charts:0.0.13"
// Alternative: implementation "com.github.PhilJay:MPAndroidChart:v3.1.0"
```

### 5. Screen Structure
Create main analytics screen following existing screen patterns:

```kotlin
@Composable
fun TransactionAnalyticsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionAnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // TopAppBar similar to WalletListScreen
        // Date range filters (follow existing filter chip patterns)
        // LazyColumn with analytics cards
        // Chart components
        // Summary statistics
    }
}
```

## Technical Requirements

### Dependencies Required
```kotlin
// Chart library for data visualization
implementation "io.github.ehsannarmani:compose-charts:0.0.13"

// Existing dependencies to leverage
// - androidx.compose.material3 (existing)
// - androidx.hilt.navigation.compose (existing) 
// - kotlinx.coroutines.core (existing)
// - com.google.firebase.firestore-ktx (existing)
```

### Data Processing Requirements
- Date range filtering for transactions
- Multi-currency conversion support using existing CurrencyRateRepository
- Real-time data updates using Flow and StateFlow
- Tag-based categorization analysis
- Income vs expense trend calculation
- Monthly/quarterly aggregation

### UI Component Requirements  
- Chart components for trend visualization
- Category breakdown cards with percentages
- Date range picker/filter chips
- Summary statistic cards
- Responsive layout using Flow layouts for different screen sizes

## Implementation Tasks (In Order)

### Phase 1: Foundation
1. **Add navigation route** to `AppScreen` enum in `MainActivity.kt`
2. **Create TransactionAnalyticsViewModel** with basic structure and DI
3. **Add navigation composable** in `MainActivity.kt` navigation host
4. **Create basic TransactionAnalyticsScreen** with placeholder content

### Phase 2: Data Processing
5. **Implement analytics data methods** in TransactionAnalyticsViewModel:
   - Monthly expense trends  
   - Income vs expense comparison
   - Tag-based categorization
   - Date range filtering
6. **Create UI state classes** for analytics data
7. **Implement repository methods** if needed for complex queries

### Phase 3: UI Implementation
8. **Create analytics UI components**:
   - MonthlyTrendCard
   - ExpenseCategoryBreakdown  
   - IncomeExpenseComparisonCard
   - DateRangeSelector
9. **Integrate chart library** and create chart composables
10. **Implement main TransactionAnalyticsScreen** layout

### Phase 4: Integration & Navigation
11. **Add analytics navigation button** to WalletListScreen 
12. **Implement navigation callbacks** and proper back navigation
13. **Add screen title and top app bar** with consistent styling

### Phase 5: Testing & Polish
14. **Write unit tests** for TransactionAnalyticsViewModel
15. **Write UI tests** for screen interactions
16. **Test navigation flow** and data accuracy
17. **Performance testing** for large transaction datasets
18. **Accessibility testing** for screen reader compatibility

## Reference Files & Patterns

### UI Patterns to Follow
- **Card layouts**: `C:\Users\Pongo\AndroidStudioProjects\Profiteer\app\src\main\java\com\axeven\profiteerapp\ui\wallet\WalletGrouping.kt` (PhysicalFormSummaryCard)
- **Filter chips**: `C:\Users\Pongo\AndroidStudioProjects\Profiteer\app\src\main\java\com\axeven\profiteerapp\ui\wallet\WalletGrouping.kt` (PhysicalFormFilterChips)
- **Screen structure**: `C:\Users\Pongo\AndroidStudioProjects\Profiteer\app\src\main\java\com\axeven\profiteerapp\ui\wallet\WalletListScreen.kt`

### ViewModel Patterns  
- **StateFlow usage**: `C:\Users\Pongo\AndroidStudioProjects\Profiteer\app\src\main\java\com\axeven\profiteerapp\viewmodel\WalletListViewModel.kt`
- **Repository integration**: Lines 291-314 for analytics methods
- **Data combination**: `combine()` usage for real-time updates

### Navigation Patterns
- **Screen enum**: `C:\Users\Pongo\AndroidStudioProjects\Profiteer\app\src\main\java\com\axeven\profiteerapp\MainActivity.kt`
- **Navigation integration**: WalletListScreen navigation callbacks

### Data Processing Patterns
- **Transaction queries**: `C:\Users\Pongo\AndroidStudioProjects\Profiteer\app\src\main\java\com\axeven\profiteerapp\data\repository\TransactionRepository.kt`
- **Currency conversion**: CurrencyRateRepository usage in existing ViewModels
- **Date filtering**: Existing transaction date range patterns

## External Documentation References

### Material 3 Design Patterns
- **Analytics Dashboard Design**: https://m3.material.io/develop/android/jetpack-compose
- **Card layouts and responsive design**: https://developer.android.com/develop/ui/compose/designsystems/material3

### Navigation Implementation
- **Bottom Navigation Material 3**: https://medium.com/@bharadwaj.rns/bottom-navigation-in-jetpack-compose-using-material3-c153ccbf0593
- **Compose Navigation**: https://developer.android.com/develop/ui/compose/navigation

### Chart Library Options
- **Compose Charts**: https://www.jetpackcompose.app/Chart-libraries-in-Jetpack-Compose
- **Personal Finance App Examples**: https://github.com/furqanullah717/expense-tracker-android

### LazyColumn Best Practices
- **Performance Optimization**: https://developer.android.com/develop/ui/compose/lists
- **Analytics Integration**: https://medium.com/@ramadan123sayed/lazycolumn-in-jetpack-compose-fa3287ef84da

## Business Rules & Constraints

### Critical Business Rules
- **Balance Integrity**: Analytics must maintain consistency with wallet balance calculations
- **Currency Conversion**: Use existing currency conversion system (Default → Monthly rates)
- **Data Isolation**: User data must remain completely isolated (follow existing Firestore patterns)
- **Tag System**: Support multiple tags per transaction with "Untagged" default

### Data Processing Rules
- **Date Range**: Support filtering by custom date ranges
- **Multi-currency**: Convert all amounts to user's default currency for aggregation
- **Real-time Updates**: Analytics must reflect live transaction changes
- **Performance**: Handle large transaction datasets efficiently

## Validation Gates

### Build & Compilation
```bash
./gradlew build
./gradlew assembleDebug
```

### Code Quality
```bash
./gradlew lint
./gradlew lintDebug
```

### Unit Tests  
```bash
./gradlew test
./gradlew testDebugUnitTest
```

### UI Tests
```bash
./gradlew connectedAndroidTest
```

### Manual Testing Checklist
- [ ] Navigation to analytics screen works from wallet list
- [ ] Analytics data displays correctly with sample transactions
- [ ] Date filtering updates charts and summaries  
- [ ] Multi-currency conversion works properly
- [ ] Back navigation maintains proper state
- [ ] Screen works on different device orientations
- [ ] Accessibility features work with screen readers
- [ ] Performance is acceptable with 100+ transactions

### Data Validation
- [ ] Analytics totals match wallet balances
- [ ] Currency conversion uses correct rates
- [ ] Tag-based categorization includes all transactions
- [ ] Date filtering respects user timezone
- [ ] Real-time updates work properly

## Known Risks & Mitigation

### Risk: Chart Library Integration Complexity
**Mitigation**: Start with simple bar/line charts, use established Compose-compatible libraries with good documentation

### Risk: Performance with Large Datasets  
**Mitigation**: Implement pagination, lazy loading, and efficient Firestore queries with proper indexing

### Risk: Complex Currency Conversion
**Mitigation**: Leverage existing CurrencyRateRepository patterns and thorough testing

### Risk: Analytics Data Accuracy
**Mitigation**: Comprehensive unit tests comparing analytics results with raw transaction data

## Success Criteria

1. **Functional**: Users can navigate to dedicated analytics screen and view comprehensive transaction insights
2. **Performance**: Screen loads within 2 seconds with 500+ transactions
3. **Accuracy**: Analytics data matches manual calculations within 0.01% tolerance
4. **User Experience**: Screen follows existing Material 3 design patterns and navigation conventions
5. **Maintainability**: Code follows existing architecture patterns and is fully tested

## Implementation Confidence Score: 8/10

**Reasoning**: 
- ✅ Well-established patterns in codebase for ViewModels, UI components, and navigation
- ✅ Existing analytics foundation to build upon  
- ✅ Clear documentation and external resources
- ✅ Comprehensive validation strategy
- ❓ Chart library integration adds complexity but manageable with research provided
- ✅ Business rules and data processing patterns are well understood

**Primary Challenge**: Chart library integration and ensuring proper Material 3 theming, but extensive research and examples are provided to mitigate this risk.