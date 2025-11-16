# Repository Error Handling Guidelines

This document provides comprehensive guidance for implementing proper error handling in the Repository layer of the Profiteer Android app.

## Core Principle

**CRITICAL**: Repositories must NEVER depend on UI layer (ViewModels). Use domain error types instead.

## Architecture Overview

```
Repository Layer (Data)     →     ViewModel Layer (Business)     →     UI Layer (Presentation)
Uses RepositoryError types  →     Catches & handles exceptions   →     Displays user messages
```

### Dependency Rules

✅ **Allowed Dependencies**:
- `FirebaseFirestore`
- `Logger`
- Other Repositories
- Domain models (`data/model/`)
- Utility classes

❌ **Forbidden Dependencies**:
- ViewModels (ANY ViewModel)
- UI components
- Compose-specific classes
- `SharedErrorViewModel`

## Error Type Hierarchy

All domain error types are defined in `data/model/RepositoryError.kt`:

| Error Type | Use Case | Example |
|------------|----------|---------|
| `RepositoryError.FirestoreListener` | Real-time listener errors | Snapshot listener fails |
| `RepositoryError.FirestoreCrud` | CRUD operation errors | Document update fails |
| `RepositoryError.NetworkError` | Connectivity issues | No internet connection |
| `RepositoryError.AuthenticationError` | Auth/permission errors | User not authenticated |
| `RepositoryError.DataValidationError` | Parsing failures | Invalid data format |
| `RepositoryError.ResourceNotFound` | Missing resources | Wallet not found |
| `RepositoryError.CompositeError` | Multi-query aggregation | Multiple queries fail |
| `RepositoryError.UnknownError` | Catch-all | Unexpected errors |

## Repository Implementation Pattern

### Firestore Snapshot Listener Error Handling

```kotlin
@Singleton
class MyRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val logger: Logger  // ✅ Logger is allowed
    // ❌ NEVER inject SharedErrorViewModel or any ViewModel
) {
    fun getUserData(userId: String): Flow<List<Data>> = callbackFlow {
        val listener = collection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // 1. Convert Firebase error to domain error info
                    val errorInfo = FirestoreErrorHandler.handleSnapshotError(
                        error,
                        "getUserData"
                    )

                    // 2. Create RepositoryError from errorInfo
                    val repositoryError = RepositoryError.FirestoreListener(
                        operation = errorInfo.operation,
                        userMessage = errorInfo.userMessage,
                        shouldRetry = errorInfo.shouldRetry,
                        requiresReauth = errorInfo.requiresReauth,
                        isOffline = errorInfo.isOffline,
                        cause = error
                    )

                    // 3. Close Flow with typed exception
                    close(repositoryError)
                    return@addSnapshotListener
                }

                // 4. Handle successful snapshot
                trySend(parseData(snapshot))
            }

        awaitClose { listener.remove() }
    }
}
```

### Firestore CRUD Operation Error Handling

```kotlin
suspend fun updateWallet(walletId: String, updates: Map<String, Any>): Result<Unit> {
    return try {
        firestore.collection("wallets")
            .document(walletId)
            .update(updates)
            .await()

        Result.success(Unit)

    } catch (e: Exception) {
        val errorInfo = FirestoreErrorHandler.handleCrudError(e, "updateWallet")

        val repositoryError = RepositoryError.FirestoreCrud(
            operation = errorInfo.operation,
            userMessage = errorInfo.userMessage,
            shouldRetry = errorInfo.shouldRetry,
            requiresReauth = errorInfo.requiresReauth,
            isOffline = errorInfo.isOffline,
            cause = e
        )

        logger.e("MyRepository", "Update failed: ${errorInfo.message}", e)
        Result.failure(repositoryError)
    }
}
```

## ViewModel Integration

ViewModels catch and handle `RepositoryError` exceptions using utility functions from `viewmodel/ErrorHandlingUtils.kt`:

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: MyRepository,
    private val logger: Logger
) : ViewModel() {

    fun loadData() {
        viewModelScope.launch {
            try {
                repository.getUserData(userId).collect { data ->
                    _uiState.update { it.copy(data = data, isLoading = false) }
                }
            } catch (e: Exception) {
                // Use error handling utilities
                val errorInfo = e.toErrorInfo()
                logger.w("MyViewModel", "Error loading data: ${errorInfo.message}")

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = errorInfo.message
                    )
                }

                // Handle critical errors (reauth required)
                if (e.isCriticalError()) {
                    handleCriticalError()
                }
            }
        }
    }
}
```

## Error Handling Utilities

### Extension Functions (`viewmodel/ErrorHandlingUtils.kt`)

```kotlin
// Extract user-friendly message
val message: String = exception.toUserMessage()

// Check if requires auth/critical handling
val isCritical: Boolean = exception.isCriticalError()

// Check if operation can be retried
val canRetry: Boolean = exception.isRetryable()

// Check if network-related
val isOffline: Boolean = exception.isOfflineError()

// Get structured error information
val errorInfo: ErrorInfo = exception.toErrorInfo()
```

### ErrorInfo Data Class

```kotlin
data class ErrorInfo(
    val message: String,           // User-friendly message
    val shouldRetry: Boolean,      // Can retry operation
    val requiresReauth: Boolean,   // Needs re-authentication
    val isOffline: Boolean         // Network-related
)
```

## Common Error Scenarios

### Scenario 1: Permission Denied

```kotlin
// Firebase Error: PERMISSION_DENIED
// RepositoryError: AuthenticationError
// User Message: "Please sign in again to continue"
// Action: Trigger re-authentication flow
```

### Scenario 2: Network Unavailable

```kotlin
// Firebase Error: UNAVAILABLE
// RepositoryError: NetworkError
// User Message: "No internet connection. Please check your network."
// Action: Show offline mode, enable retry
```

### Scenario 3: Resource Not Found

```kotlin
// Firebase Error: NOT_FOUND
// RepositoryError: ResourceNotFound
// User Message: "The requested item could not be found"
// Action: Navigate back or show empty state
```

### Scenario 4: Data Validation Error

```kotlin
// Parsing Error: Invalid format
// RepositoryError: DataValidationError
// User Message: "Invalid data format. Please try again."
// Action: Log error, show generic message
```

## Anti-Patterns

### ❌ FORBIDDEN: Repository Calling UI Layer

```kotlin
// ❌ WRONG: Repository depends on ViewModel
class MyRepository @Inject constructor(
    private val sharedErrorViewModel: SharedErrorViewModel  // NEVER DO THIS
) {
    fun getData() = callbackFlow {
        addSnapshotListener { snapshot, error ->
            if (error != null) {
                sharedErrorViewModel.showError(error.message)  // WRONG!
                return@addSnapshotListener
            }
        }
    }
}
```

### ✅ CORRECT: Repository Using Domain Errors

```kotlin
// ✅ RIGHT: Repository uses domain error types
class MyRepository @Inject constructor(
    private val logger: Logger
) {
    fun getData() = callbackFlow {
        addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(RepositoryError.FirestoreListener(...))  // RIGHT!
                return@addSnapshotListener
            }
        }
    }
}
```

## Testing Requirements

### Repository Tests

1. **Verify No UI Dependencies**
   ```kotlin
   @Test
   fun `repository should have no UI dependencies`() {
       verifyNoUIDependencies(MyRepository::class)
   }
   ```

2. **Test Error Scenarios**
   ```kotlin
   @Test
   fun `should return FirestoreListener error on snapshot failure`() = runTest {
       // Simulate Firestore error
       val error = FirebaseFirestoreException(
           "Permission denied",
           FirebaseFirestoreException.Code.PERMISSION_DENIED
       )

       // Verify correct RepositoryError type returned
       val result = repository.getData(userId).catch { e ->
           assertTrue(e is RepositoryError.FirestoreListener)
           assertEquals("Please sign in again", e.userMessage)
       }
   }
   ```

### ViewModel Tests

1. **Test Error Handling**
   ```kotlin
   @Test
   fun `should handle repository errors gracefully`() = runTest {
       // Given: Repository throws RepositoryError
       val repositoryError = RepositoryError.NetworkError(...)
       whenever(repository.getData()).thenThrow(repositoryError)

       // When: ViewModel loads data
       viewModel.loadData()

       // Then: Error state updated
       val state = viewModel.uiState.value
       assertTrue(state.error.isNotEmpty())
       assertFalse(state.isLoading)
   }
   ```

## FirestoreErrorHandler Utility

### Snapshot Listener Errors

```kotlin
val errorInfo = FirestoreErrorHandler.handleSnapshotError(
    error = firestoreException,
    operation = "getUserTransactions"
)

// errorInfo contains:
// - operation: String
// - userMessage: String
// - shouldRetry: Boolean
// - requiresReauth: Boolean
// - isOffline: Boolean
```

### CRUD Operation Errors

```kotlin
val errorInfo = FirestoreErrorHandler.handleCrudError(
    error = exception,
    operation = "updateWallet"
)
```

### Error Code Mapping

| Firebase Error Code | RepositoryError Type | User Message |
|---------------------|----------------------|--------------|
| `PERMISSION_DENIED` | `AuthenticationError` | "Please sign in again" |
| `UNAVAILABLE` | `NetworkError` | "No internet connection" |
| `NOT_FOUND` | `ResourceNotFound` | "Item not found" |
| `INVALID_ARGUMENT` | `DataValidationError` | "Invalid data format" |
| `UNKNOWN` | `UnknownError` | "An error occurred" |

## Best Practices

1. **Always Use Domain Error Types**
   - Never throw generic exceptions
   - Always convert to `RepositoryError` types

2. **Log Errors at Repository Level**
   ```kotlin
   logger.e("MyRepository", "Operation failed: ${errorInfo.message}", error)
   ```

3. **Provide User-Friendly Messages**
   - Use `FirestoreErrorHandler` for consistent messages
   - Avoid technical jargon in user messages

4. **Include Error Context**
   ```kotlin
   RepositoryError.FirestoreListener(
       operation = "getUserData",  // What operation failed
       userMessage = "...",         // User-friendly message
       cause = error                // Original exception
   )
   ```

5. **Handle Critical Errors**
   ```kotlin
   if (errorInfo.requiresReauth) {
       // Trigger re-authentication flow
   }
   ```

6. **Enable Retry for Transient Errors**
   ```kotlin
   if (errorInfo.shouldRetry) {
       // Show retry button
   }
   ```

## Related Documentation

- [RepositoryError.kt](../app/src/main/java/com/axeven/profiteerapp/data/model/RepositoryError.kt)
- [ErrorHandlingUtils.kt](../app/src/main/java/com/axeven/profiteerapp/viewmodel/ErrorHandlingUtils.kt)
- [Repository Refactoring Plan](plans/2025-10-17-repository-layer-mixing-concerns.md)
- [Firebase Security Guidelines](FIREBASE_SECURITY_GUIDELINES.md)
- [Logging Guidelines](LOGGING_GUIDELINES.md)

## Migration Checklist

When refactoring existing repositories to use proper error handling:

- [ ] Remove any ViewModel dependencies from constructor
- [ ] Replace generic exceptions with `RepositoryError` types
- [ ] Add `FirestoreErrorHandler` for error conversion
- [ ] Update all snapshot listeners to use domain errors
- [ ] Update all CRUD operations to use domain errors
- [ ] Add proper logging at repository level
- [ ] Update ViewModels to catch and handle `RepositoryError`
- [ ] Add unit tests for error scenarios
- [ ] Verify no UI dependencies with `verifyNoUIDependencies()`
- [ ] Update documentation and comments
