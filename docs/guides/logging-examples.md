# Logging Examples for Common Scenarios

This document provides practical examples of logging implementation across different layers of the Profiteer application.

## ViewModel Logging Examples

### Transaction Management

```kotlin
@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val logger: Logger
) : ViewModel() {

    fun createTransaction(transaction: Transaction) {
        logger.d("TransactionVM", "Creating transaction: type=${transaction.type}, amount=${transaction.amount}")

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            transactionRepository.createTransaction(transaction)
                .onSuccess {
                    logger.i("TransactionVM", "Transaction created successfully")
                    _uiState.update { it.copy(isLoading = false) }
                    navigateToHome()
                }
                .onFailure { error ->
                    logger.e("TransactionVM", "Failed to create transaction", error)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to create transaction: ${error.message}"
                        )
                    }
                }
        }
    }
}
```

### Authentication Flow

```kotlin
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val logger: Logger
) : ViewModel() {

    fun signInWithGoogle(idToken: String) {
        logger.d("AuthVM", "Starting Google sign-in process")

        viewModelScope.launch {
            _authState.value = AuthState.Loading

            try {
                val result = authRepository.signInWithGoogle(idToken)
                if (result.isSuccess) {
                    logger.i("AuthVM", "Google sign-in successful")
                    _authState.value = AuthState.Authenticated
                } else {
                    logger.w("AuthVM", "Google sign-in failed: ${result.exceptionOrNull()?.message}")
                    _authState.value = AuthState.Error("Sign-in failed. Please try again.")
                }
            } catch (e: Exception) {
                logger.e("AuthVM", "Unexpected error during Google sign-in", e)
                _authState.value = AuthState.Error("An unexpected error occurred")
            }
        }
    }
}
```

## Repository Logging Examples

### Data Persistence

```kotlin
@Singleton
class TransactionRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val logger: Logger
) {

    suspend fun saveTransaction(transaction: Transaction): Result<String> {
        logger.d("TransactionRepo", "Saving transaction to Firestore")

        return try {
            val docRef = firestore.collection("transactions").add(transaction.toMap()).await()
            logger.i("TransactionRepo", "Transaction saved with ID: ${docRef.id}")
            Result.success(docRef.id)

        } catch (e: FirebaseFirestoreException) {
            when (e.code) {
                Code.PERMISSION_DENIED -> {
                    logger.w("TransactionRepo", "Permission denied - user may need to re-authenticate")
                    Result.failure(AuthException("Please sign in again"))
                }
                Code.UNAVAILABLE -> {
                    logger.w("TransactionRepo", "Firestore unavailable - network connectivity issue")
                    Result.failure(NetworkException("Please check your internet connection"))
                }
                else -> {
                    logger.e("TransactionRepo", "Firestore error while saving transaction", e)
                    Result.failure(e)
                }
            }
        } catch (e: Exception) {
            logger.e("TransactionRepo", "Unexpected error while saving transaction", e)
            Result.failure(e)
        }
    }

    suspend fun getTransactions(userId: String): Result<List<Transaction>> {
        logger.d("TransactionRepo", "Fetching transactions for user")

        return try {
            val querySnapshot = firestore
                .collection("transactions")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val transactions = querySnapshot.documents.mapNotNull { doc ->
                try {
                    Transaction.fromMap(doc.id, doc.data ?: emptyMap())
                } catch (e: Exception) {
                    logger.w("TransactionRepo", "Failed to parse transaction ${doc.id}: ${e.message}")
                    null
                }
            }

            logger.i("TransactionRepo", "Successfully fetched ${transactions.size} transactions")
            Result.success(transactions)

        } catch (e: Exception) {
            logger.e("TransactionRepo", "Failed to fetch transactions", e)
            Result.failure(e)
        }
    }
}
```

### Currency Rate Management

```kotlin
@Singleton
class CurrencyRateRepository @Inject constructor(
    private val apiService: CurrencyApiService,
    private val cacheManager: CacheManager,
    private val logger: Logger
) {

    suspend fun getExchangeRate(from: String, to: String): Result<Double> {
        logger.d("CurrencyRepo", "Getting exchange rate: $from → $to")

        // Check cache first
        val cachedRate = cacheManager.getRate(from, to)
        if (cachedRate != null) {
            logger.d("CurrencyRepo", "Using cached rate: $from → $to = $cachedRate")
            return Result.success(cachedRate)
        }

        return try {
            val response = apiService.getExchangeRates(from)
            val rate = response.rates[to]

            if (rate != null) {
                logger.i("CurrencyRepo", "Fetched exchange rate: $from → $to = $rate")
                cacheManager.setRate(from, to, rate)
                Result.success(rate)
            } else {
                logger.w("CurrencyRepo", "Exchange rate not available for $from → $to")
                Result.failure(CurrencyNotFoundException("Rate not available for $from to $to"))
            }

        } catch (e: NetworkException) {
            logger.w("CurrencyRepo", "Network error while fetching exchange rate: ${e.message}")
            // Try to return stale cache data
            val staleRate = cacheManager.getStaleRate(from, to)
            if (staleRate != null) {
                logger.i("CurrencyRepo", "Using stale cached rate due to network error")
                Result.success(staleRate)
            } else {
                Result.failure(e)
            }

        } catch (e: Exception) {
            logger.e("CurrencyRepo", "Unexpected error while fetching exchange rate", e)
            Result.failure(e)
        }
    }
}
```

## Service Layer Logging

### Background Sync Service

```kotlin
@Singleton
class SyncService @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val logger: Logger
) {

    suspend fun performFullSync(): SyncResult {
        logger.i("SyncService", "Starting full data synchronization")
        val startTime = System.currentTimeMillis()

        try {
            var syncedTransactions = 0
            var syncedWallets = 0
            var errors = 0

            // Sync transactions
            logger.d("SyncService", "Syncing transactions...")
            transactionRepository.syncPendingTransactions()
                .onSuccess { count ->
                    syncedTransactions = count
                    logger.d("SyncService", "Synced $count transactions")
                }
                .onFailure { error ->
                    errors++
                    logger.w("SyncService", "Transaction sync failed: ${error.message}")
                }

            // Sync wallets
            logger.d("SyncService", "Syncing wallets...")
            walletRepository.syncWalletBalances()
                .onSuccess { count ->
                    syncedWallets = count
                    logger.d("SyncService", "Synced $count wallets")
                }
                .onFailure { error ->
                    errors++
                    logger.w("SyncService", "Wallet sync failed: ${error.message}")
                }

            val duration = System.currentTimeMillis() - startTime
            val result = SyncResult(syncedTransactions, syncedWallets, errors, duration)

            if (errors == 0) {
                logger.i("SyncService", "Full sync completed successfully in ${duration}ms")
            } else {
                logger.w("SyncService", "Sync completed with $errors errors in ${duration}ms")
            }

            return result

        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            logger.e("SyncService", "Full sync failed after ${duration}ms", e)
            throw e
        }
    }
}
```

## Error Handling Patterns

### Network Error Recovery

```kotlin
class NetworkErrorHandler @Inject constructor(
    private val logger: Logger
) {

    suspend fun <T> executeWithRetry(
        operation: String,
        maxRetries: Int = 3,
        block: suspend () -> T
    ): Result<T> {
        logger.d("NetworkHandler", "Executing $operation with max $maxRetries retries")

        repeat(maxRetries) { attempt ->
            try {
                val result = block()
                if (attempt > 0) {
                    logger.i("NetworkHandler", "$operation succeeded after ${attempt + 1} attempts")
                }
                return Result.success(result)

            } catch (e: NetworkException) {
                val remainingRetries = maxRetries - attempt - 1
                if (remainingRetries > 0) {
                    val delay = (attempt + 1) * 1000L // Progressive backoff
                    logger.w("NetworkHandler", "$operation failed (attempt ${attempt + 1}/$maxRetries), retrying in ${delay}ms: ${e.message}")
                    delay(delay)
                } else {
                    logger.e("NetworkHandler", "$operation failed after $maxRetries attempts", e)
                    return Result.failure(e)
                }

            } catch (e: Exception) {
                logger.e("NetworkHandler", "$operation failed with non-network error", e)
                return Result.failure(e)
            }
        }

        return Result.failure(Exception("Max retries exceeded"))
    }
}
```

### Firestore Error Handling

```kotlin
class FirestoreErrorHandler @Inject constructor(
    private val logger: Logger
) {

    fun handleFirestoreError(operation: String, error: Throwable): UserFriendlyError {
        logger.d("FirestoreHandler", "Handling Firestore error for operation: $operation")

        return when (error) {
            is FirebaseFirestoreException -> {
                when (error.code) {
                    Code.PERMISSION_DENIED -> {
                        logger.w("FirestoreHandler", "Permission denied for $operation - authentication required")
                        UserFriendlyError.AuthRequired("Please sign in again to continue")
                    }
                    Code.UNAVAILABLE -> {
                        logger.w("FirestoreHandler", "Firestore unavailable for $operation - network issue")
                        UserFriendlyError.NetworkError("Please check your internet connection")
                    }
                    Code.DEADLINE_EXCEEDED -> {
                        logger.w("FirestoreHandler", "Timeout for $operation")
                        UserFriendlyError.Timeout("Operation timed out, please try again")
                    }
                    else -> {
                        logger.e("FirestoreHandler", "Unexpected Firestore error for $operation", error)
                        UserFriendlyError.Unknown("An unexpected error occurred")
                    }
                }
            }
            else -> {
                logger.e("FirestoreHandler", "Non-Firestore error for $operation", error)
                UserFriendlyError.Unknown(error.message ?: "An unknown error occurred")
            }
        }
    }
}
```

## Performance Monitoring

### Database Query Performance

```kotlin
class PerformanceMonitor @Inject constructor(
    private val logger: Logger,
    private val analyticsLogger: AnalyticsLogger
) {

    suspend fun <T> measureDatabaseOperation(
        operation: String,
        block: suspend () -> T
    ): T {
        val startTime = System.nanoTime()
        logger.d("Performance", "Starting database operation: $operation")

        try {
            val result = block()
            val durationMs = (System.nanoTime() - startTime) / 1_000_000

            logger.d("Performance", "$operation completed in ${durationMs}ms")

            // Track performance metrics
            analyticsLogger.trackPerformance(operation, durationMs, mapOf(
                "operation_type" to "database",
                "status" to "success"
            ))

            // Log slow operations
            if (durationMs > 1000) {
                logger.w("Performance", "Slow database operation detected: $operation took ${durationMs}ms")
            }

            return result

        } catch (e: Exception) {
            val durationMs = (System.nanoTime() - startTime) / 1_000_000
            logger.e("Performance", "$operation failed after ${durationMs}ms", e)

            analyticsLogger.trackPerformance(operation, durationMs, mapOf(
                "operation_type" to "database",
                "status" to "error",
                "error_type" to e::class.simpleName
            ))

            throw e
        }
    }
}
```

## UI Component Logging

### Compose Screen Logging

```kotlin
@Composable
fun TransactionListScreen(
    viewModel: TransactionListViewModel = hiltViewModel()
) {
    val logger = remember {
        // For UI logging, consider minimal logging to avoid performance impact
        object : Logger {
            override fun d(tag: String, message: String) { /* No-op in UI */ }
            override fun i(tag: String, message: String) { /* Important UI events only */ }
            override fun w(tag: String, message: String) { /* UI warnings */ }
            override fun e(tag: String, message: String, throwable: Throwable?) { /* UI errors */ }
        }
    }

    LaunchedEffect(Unit) {
        logger.d("TransactionListScreen", "Screen launched")
        viewModel.loadTransactions()
    }

    val uiState by viewModel.uiState.collectAsState()

    uiState.error?.let { error ->
        logger.w("TransactionListScreen", "Displaying error to user: $error")
        // Show error UI
    }
}
```

## Testing with Logging

### Mock Logger for Tests

```kotlin
class MockLogger : Logger {
    private val logs = mutableListOf<LogEntry>()

    override fun d(tag: String, message: String) {
        logs.add(LogEntry("DEBUG", tag, message))
    }

    override fun i(tag: String, message: String) {
        logs.add(LogEntry("INFO", tag, message))
    }

    override fun w(tag: String, message: String) {
        logs.add(LogEntry("WARN", tag, message))
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        logs.add(LogEntry("ERROR", tag, message, throwable))
    }

    fun getLogsByLevel(level: String): List<LogEntry> = logs.filter { it.level == level }
    fun getLogsByTag(tag: String): List<LogEntry> = logs.filter { it.tag == tag }
    fun getAllLogs(): List<LogEntry> = logs.toList()
    fun clearLogs() = logs.clear()

    data class LogEntry(
        val level: String,
        val tag: String,
        val message: String,
        val throwable: Throwable? = null
    )
}
```

### Repository Test Example

```kotlin
@Test
fun `should log successful transaction creation`() = runTest {
    val mockLogger = MockLogger()
    val repository = TransactionRepository(mockFirestore, mockLogger)

    repository.saveTransaction(testTransaction)

    val logs = mockLogger.getLogsByTag("TransactionRepo")
    assertEquals(2, logs.size)
    assertEquals("DEBUG", logs[0].level)
    assertTrue(logs[0].message.contains("Saving transaction"))
    assertEquals("INFO", logs[1].level)
    assertTrue(logs[1].message.contains("Transaction saved"))
}
```

## Best Practices Summary

### Do's ✅

1. **Always inject Logger via Hilt**
2. **Use appropriate log levels**
3. **Include relevant context in messages**
4. **Log errors with exceptions**
5. **Use structured logging for complex data**
6. **Monitor performance-critical operations**

### Don'ts ❌

1. **Never use android.util.Log directly**
2. **Don't log sensitive data** (it's auto-sanitized, but be conscious)
3. **Don't log in tight loops** without checking log level
4. **Don't use println() or System.out**
5. **Don't ignore exceptions** in catch blocks
6. **Don't log excessive debug info** in production code paths

### Performance Tips ⚡

1. Use lazy evaluation for expensive log message construction
2. Check log levels for very expensive operations
3. Prefer structured logging over string concatenation
4. Use analytics logger for business metrics
5. Monitor log volume to avoid performance impact