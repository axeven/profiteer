# CLAUDE.md

This file provides specific guidance to Claude Code (claude.ai/code) when working with code in this repository.

# Project Context

For comprehensive project information, architecture details, and feature documentation, see **[README.md](README.md)** and the **[docs/](docs/)** directory.

**Quick Summary**: Profiteer is a personal finance Android app built with Kotlin, Jetpack Compose, MVVM architecture, Firebase Auth, and Firestore, featuring a dual-wallet system with multi-currency support.

# Build Commands

## Development
- `./gradlew build` - Build the project
- `./gradlew assembleDebug` - Build debug APK
- `./gradlew assembleRelease` - Build release APK
- `./gradlew installDebug` - Install debug build on connected device

## Testing

Use the following commands to run tests

- `./gradlew test` - Run unit tests
- `./gradlew connectedAndroidTest` - Run instrumented tests on connected device/emulator
- `./gradlew testDebugUnitTest` - Run debug unit tests specifically

# Testing requirements

- All code changes MUST include tests - No code should be committed without corresponding test coverage
- Test both happy path and error scenarios
- Use descriptive test names that clearly explain the scenario being tested
- Mock external dependencies appropriately
- Test error handling and edge cases
- Run tests before committing
- Ensure tests are deterministic - Tests should pass consistently regardless of execution order

# Code Quality
- `./gradlew lint` - Run Android lint checks
- `./gradlew lintDebug` - Run lint on debug build variant

# Development Guidelines

## Code Quality & Testing
- **ALWAYS** run lint and tests before committing changes
- When implementing new features, add corresponding unit tests
- Use Compose testing utilities for UI tests (`androidx.compose.ui.test`)
- Follow existing code patterns and naming conventions

## Architecture Guidelines
- Follow MVVM pattern with Repository pattern for data access
- Use Hilt for dependency injection
- Implement reactive programming with StateFlow and Compose State
- Place business logic in ViewModels, not in UI components

## Key Development Notes
- **Target SDK**: 36, **Min SDK**: 24, **Java**: 11
- Uses Gradle Version Catalogs (`gradle/libs.versions.toml`)
- Firebase must be in **Native Mode** (not Datastore Mode)
- Requires `google-services.json` in `app/` directory
- Web Client ID configuration required in `AuthRepository.kt`

# Firebase Requirements

## Critical Setup Requirements
- **Firestore Database**: Must be Native Mode for real-time listeners
- **Authentication**: Google Sign-in with proper SHA-1 fingerprints configured
- **Document ID Mapping**: Manual mapping required for proper model instantiation
- **Security Rules**: User data isolation enforced through Firestore rules

## Firestore Security Rules & Query Requirements

**CRITICAL**: All Firestore queries MUST comply with security rules to prevent permission errors.

### Current Security Rules
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // All collections require userId filter for data isolation
    match /transactions/{transactionId} {
      allow read, write: if request.auth != null && request.auth.uid == resource.data.userId;
      allow create: if request.auth != null && request.auth.uid == request.resource.data.userId;
    }
    match /wallets/{walletId} {
      allow read, write: if request.auth != null && request.auth.uid == resource.data.userId;
      allow create: if request.auth != null && request.auth.uid == request.resource.data.userId;
    }
    match /user_preferences/{prefId} {
      allow read, write: if request.auth != null && request.auth.uid == resource.data.userId;
      allow create: if request.auth != null && request.auth.uid == request.resource.data.userId;
    }
    match /currency_rates/{rateId} {
      allow read, write: if request.auth != null && request.auth.uid == resource.data.userId;
      allow create: if request.auth != null && request.auth.uid == request.resource.data.userId;
    }
  }
}
```

### Mandatory Query Pattern

**🚨 SECURITY REQUIREMENT**: Every Firestore query MUST include a `userId` filter as the FIRST condition.

#### ✅ Correct Pattern:
```kotlin
// ALWAYS start with userId filter
collection
  .whereEqualTo("userId", userId)
  .whereEqualTo("otherField", value)
  .get()
```

#### ❌ FORBIDDEN Pattern:
```kotlin
// NEVER query without userId filter - this will cause permission errors
collection
  .whereEqualTo("walletId", walletId)  // Missing userId!
  .get()
```

### Repository Implementation Rules

1. **All Repository Methods**: Must accept `userId` as parameter when querying
2. **Function Signatures**: Include `userId: String` parameter for all query methods
3. **Compound Queries**: Always use `userId` as the first filter condition
4. **Array Queries**: Use `.whereEqualTo("userId", userId)` before `.whereArrayContains()`

#### Example Implementations:
```kotlin
// ✅ Correct Implementation
fun getWalletTransactions(walletId: String, userId: String): Flow<List<Transaction>> = callbackFlow {
  transactionsCollection
    .whereEqualTo("userId", userId)        // Required first!
    .whereEqualTo("walletId", walletId)
    .addSnapshotListener { ... }
}

// ✅ Correct Array Query
fun getAffectedTransactions(walletId: String, userId: String): Flow<List<Transaction>> = callbackFlow {
  transactionsCollection
    .whereEqualTo("userId", userId)              // Required first!
    .whereArrayContains("affectedWalletIds", walletId)
    .addSnapshotListener { ... }
}
```

### Security Validation Checklist

Before implementing any new Firestore query:
- [ ] Does the function accept `userId: String` parameter?
- [ ] Is `.whereEqualTo("userId", userId)` the FIRST filter condition?
- [ ] Are all Repository callers passing the correct userId?
- [ ] Does the query follow the established security pattern?

### Common Security Violations

1. **Missing userId Filter**:
   ```kotlin
   // ❌ This will fail with PERMISSION_DENIED
   .whereEqualTo("sourceWalletId", walletId)
   ```

2. **Wrong Filter Order**:
   ```kotlin
   // ❌ userId should be first (may impact index performance)
   .whereEqualTo("walletId", walletId)
   .whereEqualTo("userId", userId)
   ```

3. **Missing userId Parameter**:
   ```kotlin
   // ❌ Function doesn't accept userId
   fun getTransactions(walletId: String): Flow<List<Transaction>>
   ```

### Error Messages to Watch For

If you see these Firebase errors, check for missing userId filters:
- `PERMISSION_DENIED: Missing or insufficient permissions`
- `Error in [query type] query` in TransactionRepository logs

**Remember**: Security rules are enforced on every query. One missing userId filter will break the entire query.

# Code Organization

## Package Structure
```
com.axeven.profiteerapp/
├── data/              # Data layer (models, repositories, DI)
├── ui/               # UI components (Jetpack Compose screens)
├── utils/            # Utility classes (NumberFormatter, validators)
└── viewmodel/        # ViewModels for business logic
```

## Key Files
- **MainActivity.kt** - Single activity hosting all Compose screens
- **Theme system** - `ui/theme/` (Color.kt, Theme.kt, Type.kt)
- **Models** - `data/model/` (Wallet.kt, Transaction.kt, CurrencyRate.kt)

# Business Logic & Validation

## Critical Business Rules
- **Balance Integrity**: Sum of Logical wallet balances must equal sum of Physical wallet balances
- **Transfer Validation**: Same wallet type AND same currency required
- **Tag System**: Multiple tags per transaction, auto-completion after 3+ characters
- **Currency Conversion**: Default rates → Monthly rates → Warning system

## Data Model Evolution
- **Transactions**: Use `affectedWalletIds` (modern) alongside `walletId` (legacy) for backward compatibility
- **Tags**: Use `tags` array field, maintain `category` field for backward compatibility
- **Default Values**: "Untagged" for transactions without tags/categories

# Known Issues & Solutions

## Google Play Services
- Non-critical SecurityException warnings in logcat are expected
- Handled in ProGuard rules (`app/proguard-rules.pro`) and `AuthRepository.kt`

## Currency Conversion
- System prioritizes default rates, falls back to monthly rates
- Inverse rate calculation when direct rates unavailable
- User warnings displayed when conversion rates missing

# IMPORTANT AI Behavior Rules
- NEVER ASSUME OR GUESS - When in doubt, ask for clarification
- Never hallucinate libraries or functions – only use known, verified libraries
- Always confirm file paths and module names exist before referencing them in code or tests.
- Never delete or overwrite existing code unless explicitly instructed to or if part of a task from TASK.md.
- Keep CLAUDE.md updated when adding new patterns or dependencies
- Test your code - No feature is complete without tests
- Document your decisions - Future developers (including yourself) will thank you
  
# Testing Strategy

- **Unit Tests**: Focus on ViewModels, repositories, and business logic
- **Integration Tests**: Test Firebase integration and data flow
- **UI Tests**: Use Compose testing utilities for screen interactions
- **Validation**: Test business rules and edge cases thoroughly

# Documentation & Explainability
- Update README.md when new features are added, dependencies change, or setup steps are modified.
- Comment non-obvious code and ensure everything is understandable to a mid-level developer.
- When writing complex logic, add an inline # Reason: comment explaining the why, not just the what.
- Every module should have a docstring explaining its purpose
- Public functions must have complete docstrings

## Plan Documentation Maintenance
- **CRITICAL**: When implementing features/changes based on a plan document (e.g., files in `docs/plans/`), ALWAYS update the plan document upon completion
- Mark completed tasks with ✅ and update progress metrics
- Document actual implementation approaches vs. planned approaches
- Update status sections to reflect current progress
- This ensures documentation stays accurate and serves as a reliable project history

# Recent Changes to Be Aware Of

## Transaction System Updates
- Merged category and tag concepts into unified tagging system
- Enhanced transfer validation (wallet type + currency matching)
- Simplified wallet selection (one Physical + one Logical per transaction)

## Wallet Management Updates
- Added dedicated wallet list page with Physical/Logical separation
- Implemented unallocated balance tracking and warnings
- Enhanced navigation between home screen and wallet management

## UI/UX Improvements
- Consistent tag display across all screens
- Real-time validation with clear error messages
- Material 3 design system integration

# Development Best Practices

1. **Always check README.md** for comprehensive project context
2. **Run tests** before committing any changes
3. **Follow existing patterns** for consistency
4. **Validate business rules** in all new implementations
5. **Use proper error handling** especially for Firebase operations
6. **Maintain backward compatibility** when modifying data models
7. **Test currency conversion** logic thoroughly
8. **Verify balance integrity** in wallet and transaction operations
9. **Use proper logging practices** following the established logging framework
10. **Follow consolidated state management patterns** for all Jetpack Compose screens
11. **🚨 CRITICAL: Follow Firebase security patterns** - All Firestore queries MUST include userId filters (see Firebase Security section above)

# State Management Guidelines

## Consolidated State Pattern

**REQUIRED**: All Jetpack Compose screens must use consolidated state management instead of scattered `mutableStateOf` variables.

### Core Principles

- **Single Source of Truth**: Replace multiple `mutableStateOf` variables with one consolidated state object
- **Immutable Updates**: All state changes return new state objects using data class `copy()`
- **Automatic Validation**: State objects include built-in validation and derived properties
- **Dialog Management**: Enforce business rules like "only one dialog open at a time"

### Implementation Pattern

```kotlin
// ✅ Required: Consolidated state pattern
data class ScreenUiState(
    val dialogStates: ScreenDialogStates = ScreenDialogStates(),
    val formData: ScreenFormData = ScreenFormData(),
    val validationErrors: ValidationErrors = ValidationErrors()
) {
    val isFormValid: Boolean
        get() = formData.requiredField.isNotBlank() &&
                validationErrors.isEmpty()

    fun updateField(value: String): ScreenUiState {
        val newState = copy(formData = formData.copy(requiredField = value))
        return newState.copy(validationErrors = validateState(newState))
    }

    fun openDialog(type: DialogType): ScreenUiState {
        return copy(dialogStates = DialogStates.single(type))
    }
}

@Composable
fun MyScreen() {
    var uiState by remember { mutableStateOf(ScreenUiState()) }

    TextField(
        value = uiState.formData.requiredField,
        onValueChange = { uiState = uiState.updateField(it) }
    )
}
```

### Anti-Patterns (Forbidden)

```kotlin
// ❌ NEVER use scattered state variables
@Composable
fun MyScreen() {
    var showDialog by remember { mutableStateOf(false) }
    var titleText by remember { mutableStateOf("") }
    var isValid by remember { mutableStateOf(false) }

    // Manual validation scattered in UI
    LaunchedEffect(titleText) {
        isValid = titleText.isNotBlank()
    }
}
```

### Testing Requirements

All state objects MUST have comprehensive unit tests:

```kotlin
class ScreenUiStateTest {
    @Test
    fun `should update state immutably`() {
        val original = ScreenUiState()
        val updated = original.updateField("test")

        assertNotSame(original, updated)
        assertEquals("", original.formData.requiredField)
        assertEquals("test", updated.formData.requiredField)
    }

    @Test
    fun `should validate automatically on update`() {
        val state = ScreenUiState().updateField("valid input")
        assertTrue(state.isFormValid)
    }
}
```

### Documentation

For comprehensive guidance, see:
- [State Management Guidelines](docs/STATE_MANAGEMENT_GUIDELINES.md) - Complete patterns and examples
- [State Migration Checklist](docs/STATE_MIGRATION_CHECKLIST.md) - Step-by-step migration guide

### Migration Requirements

When updating existing screens:
1. Follow the [State Migration Checklist](docs/STATE_MIGRATION_CHECKLIST.md)
2. Create comprehensive tests for the new state model
3. Ensure UI behavior remains identical
4. Verify performance improvements or maintenance

# Logging Guidelines

## Quick Reference

The project uses a custom logging framework with automatic data sanitization and build-variant optimization. **Never use `android.util.Log` directly.**

### Basic Usage

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val logger: Logger,
    // ... other dependencies
) : ViewModel() {

    fun performOperation() {
        logger.d("MyViewModel", "Starting operation")
        try {
            // Business logic
            logger.i("MyViewModel", "Operation completed successfully")
        } catch (e: Exception) {
            logger.e("MyViewModel", "Operation failed", e)
        }
    }
}
```

### Logging Levels

- **Debug (d)**: Development only, removed in release builds
- **Info (i)**: Development only, removed in release builds
- **Warning (w)**: All builds, preserved for monitoring
- **Error (e)**: All builds, sent to Firebase Crashlytics

### Automatic Security Features

- **Data Sanitization**: Emails, amounts, IDs automatically sanitized
- **Privacy Protection**: No sensitive data in production logs
- **Performance Optimization**: Debug/info logs removed in release builds
- **Analytics Integration**: Critical errors tracked in Crashlytics

### Required Dependencies

All ViewModels, Repositories, and Services **must** inject Logger:

```kotlin
@Inject constructor(
    private val logger: Logger,
    // ... other dependencies
)
```

### Forbidden Patterns

```kotlin
// ❌ NEVER use these:
android.util.Log.d("TAG", "message")
System.out.println("debug message")
println("debug info")

// ✅ Always use:
logger.d("TAG", "message")
```

### Error Handling Pattern

```kotlin
try {
    // risky operation
} catch (e: SpecificException) {
    logger.w("MyClass", "Recoverable error: ${e.message}")
    // handle gracefully
} catch (e: Exception) {
    logger.e("MyClass", "Critical error occurred", e)
    // handle or rethrow
}
```

For complete documentation, see [LOGGING_GUIDELINES.md](docs/LOGGING_GUIDELINES.md).

# 🚨 CRITICAL SECURITY REMINDER

**BEFORE WRITING ANY FIRESTORE QUERY:**

1. ✅ **Check the Firebase Security section above** for mandatory userId filter requirements
2. ✅ **Use the template** from `docs/FIRESTORE_QUERY_TEMPLATE.kt`
3. ✅ **Always include userId parameter** in Repository method signatures
4. ✅ **Always filter by userId FIRST** in every Firestore query
5. ✅ **Reference security docs** at `docs/FIREBASE_SECURITY_GUIDELINES.md`

**Security violations will cause PERMISSION_DENIED errors and break the app!**