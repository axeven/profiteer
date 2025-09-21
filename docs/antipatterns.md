# Anti-Patterns in Profiteer Codebase

This document identifies common anti-patterns found in the Profiteer Android application codebase and provides recommendations for improvement.

## 1. Excessive Debug Logging in Production Code

**Pattern**: Using `android.util.Log` extensively throughout the codebase (114 occurrences across 14 files).

**Problem**:
- Debug logs remain in production builds
- Performance impact from string concatenation
- Potential information leakage
- Cluttered code that's hard to read

**Examples**:
```kotlin
// TransactionRepository.kt
android.util.Log.d("TransactionRepo", "Retrieved transaction: ${it.id}, title: ${it.title}")
android.util.Log.e("TransactionRepo", "Error parsing transaction document: ${document.id}", e)

// WalletRepository.kt
android.util.Log.d("WalletRepo", "Creating wallet: ${wallet.name} for user: ${wallet.userId}")
```

**Solution**:
- Use a proper logging framework (Timber, SLF4J)
- Remove debug logs from production builds
- Use conditional compilation for debug logs
- Implement structured logging with appropriate log levels

## 2. Overly Complex Compose State Management

**Pattern**: Using too many individual `mutableStateOf` variables in Compose screens.

**Problem**:
- Hard to manage state consistency
- Difficult to test
- State scattered across multiple variables
- No single source of truth

**Example**:
```kotlin
// CreateTransactionScreen.kt
var title by remember { mutableStateOf("") }
var amount by remember { mutableStateOf("") }
var category by remember { mutableStateOf("") }
var selectedType by remember { mutableStateOf(initialTransactionType ?: TransactionType.EXPENSE) }
var selectedPhysicalWallet by remember { mutableStateOf<Wallet?>(null) }
var selectedLogicalWallet by remember { mutableStateOf<Wallet?>(null) }
var tags by remember { mutableStateOf("") }
var selectedSourceWallet by remember { mutableStateOf<Wallet?>(null) }
var selectedDestinationWallet by remember { mutableStateOf<Wallet?>(null) }
var selectedDate by remember { mutableStateOf(Date()) }
var showDatePicker by remember { mutableStateOf(false) }
// ... 6 more state variables
```

**Solution**:
- Create a single data class for screen state
- Use a sealed class for UI events
- Implement proper state hoisting
- Consider using a dedicated screen ViewModel for complex forms

**Recommended Pattern**:
```kotlin
data class CreateTransactionState(
    val title: String = "",
    val amount: String = "",
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val selectedWallets: List<Wallet> = emptyList(),
    val showDatePicker: Boolean = false,
    // ... other fields
)

// In Composable
var state by remember { mutableStateOf(CreateTransactionState()) }
```

## 3. Repository Layer Mixing Concerns

**Pattern**: Repositories directly injecting and calling UI-related components like `SharedErrorViewModel`.

**Problem**:
- Violates separation of concerns
- Makes repositories dependent on UI layer
- Difficult to unit test repositories
- Tight coupling between layers

**Example**:
```kotlin
// TransactionRepository.kt
class TransactionRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val sharedErrorViewModel: SharedErrorViewModel, // UI concern in data layer
    private val authTokenManager: AuthTokenManager,
    @ApplicationContext private val context: Context
) {
    // Repository calling UI methods directly
    sharedErrorViewModel.showError(
        message = errorInfo.userMessage,
        shouldRetry = errorInfo.shouldRetry,
        requiresReauth = errorInfo.requiresReauth,
        isOffline = FirestoreErrorHandler.shouldShowOfflineMessage(error)
    )
}
```

**Solution**:
- Repositories should only return `Result<T>` or throw exceptions
- Handle UI concerns in ViewModels or UI layer
- Use domain-specific error types instead of UI error handling
- Implement proper error propagation through the architecture layers

## 4. Inconsistent Error Handling

**Pattern**: Mix of `Result<T>` pattern, exceptions, and direct error state management.

**Problem**:
- Inconsistent error handling across the app
- Some methods return `Result<T>`, others throw exceptions
- Difficult to predict error handling behavior
- Inconsistent user experience

**Examples**:
```kotlin
// Some methods return Result<T>
suspend fun createTransaction(transaction: Transaction): Result<String>

// Others throw exceptions or use callbacks with error parameters
addSnapshotListener { snapshot, error ->
    if (error != null) {
        // Handle error
    }
}
```

**Solution**:
- Standardize on one error handling approach throughout the app
- Use `Result<T>` consistently for all repository methods
- Implement a comprehensive error handling strategy
- Create domain-specific error types

## 5. God Objects and Large Classes

**Pattern**: ViewModels and repositories trying to do too much.

**Problem**:
- Single Responsibility Principle violation
- Hard to test and maintain
- Complex interdependencies
- Difficult to reason about

**Example**:
```kotlin
// TransactionViewModel handles:
// - Transaction CRUD
// - Wallet management
// - Currency conversion
// - Tag management
// - Balance calculations
// - Multiple different transaction types
```

**Solution**:
- Split large classes into smaller, focused ones
- Use composition over inheritance
- Create specialized use cases for complex operations
- Apply SOLID principles more strictly

## 6. Hardcoded Magic Values

**Pattern**: Hardcoded limits, strings, and configuration values scattered throughout the code.

**Problem**:
- Difficult to maintain and change
- No central configuration
- Inconsistent behavior

**Examples**:
```kotlin
.limit(20) // Magic number for pagination
"USD" // Hardcoded default currency
"Physical", "Logical" // Magic strings for wallet types
```

**Solution**:
- Create a `Constants` object for all magic values
- Use enum classes for string constants
- Implement proper configuration management
- Use resource files for user-facing strings

## 7. Tight Coupling to Android Framework

**Pattern**: Business logic directly depending on Android classes like `Context`, `android.util.Log`.

**Problem**:
- Difficult to unit test
- Platform-specific code mixed with business logic
- Hard to port to other platforms

**Solution**:
- Create abstractions for Android dependencies
- Use dependency injection to provide platform-specific implementations
- Keep business logic framework-agnostic
- Implement proper layered architecture

## 8. Inefficient State Updates

**Pattern**: Using `copy()` extensively for state updates, potentially creating performance issues.

**Problem**:
- Frequent object creation with data classes
- Potential memory churn
- Complex nested state updates

**Example**:
```kotlin
// Found 234 occurrences of .copy() across 24 files
_uiState.update { it.copy(isLoading = true, error = null) }
```

**Solution**:
- Consider using mutable state objects for frequently updated state
- Implement state diffing for complex updates
- Use state builders for complex state changes
- Profile and optimize hot paths

## Recommendations for Improvement

1. **Implement Proper Architecture**: Move towards Clean Architecture with clear layer separation
2. **Standardize Error Handling**: Choose one error handling strategy and apply consistently
3. **Remove Production Logging**: Implement proper logging framework with build-variant filtering
4. **Simplify State Management**: Use proper state management patterns in Compose
5. **Decouple Layers**: Remove UI dependencies from data layer
6. **Add Comprehensive Testing**: Unit tests will help identify and prevent these anti-patterns
7. **Code Review Process**: Implement strict code review guidelines to prevent anti-patterns
8. **Refactoring Plan**: Create a systematic plan to address these issues incrementally

## Impact Assessment

- **High Priority**: Repository layer mixing concerns, excessive logging, inconsistent error handling
- **Medium Priority**: Complex state management, god objects, tight coupling
- **Low Priority**: Inefficient state updates, magic values (address during regular refactoring)

These anti-patterns should be addressed systematically to improve code maintainability, testability, and overall application quality.