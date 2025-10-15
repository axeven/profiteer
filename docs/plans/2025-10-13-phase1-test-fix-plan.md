# Phase 1 Test Fixes - Plan

**Date:** 2025-10-13
**Status:** Planning
**Issue:** 4 tests failing due to Android-specific dependencies

## Problem Analysis

### Failing Tests
1. `requestAuthorization returns success when authorization granted` - NullPointerException on `googleSignInClient.signInIntent`
2. `requestAuthorization logs the authorization request` - NullPointerException on `googleSignInClient.signInIntent`
3. `createSheetsService returns Sheets instance with valid credentials` - AssertionError on Google API calls
4. `createSheetsService logs successful service creation` - Verification failed due to exception

### Root Cause
The tests are failing because:
- `GoogleSignInClient.getSignInIntent()` is an Android API that returns null when mocked
- `GoogleAccountCredential` and `GoogleAuthUtil` require real Android context
- These are integration points that can't be properly unit tested without Android instrumentation

## Solution: Refactor for Testability

### Approach
Extract Android-specific code into a separate abstraction layer that can be mocked, following the **Dependency Inversion Principle**.

### Architecture Changes

#### 1. Create `GoogleCredentialProvider` Interface
```kotlin
interface GoogleCredentialProvider {
    fun getSignInIntent(): Intent
    fun getAccountCredential(userEmail: String): GoogleAccountCredential?
}
```

#### 2. Implement `AndroidGoogleCredentialProvider`
```kotlin
class AndroidGoogleCredentialProvider(
    private val context: Context,
    private val authRepository: AuthRepository
) : GoogleCredentialProvider {
    override fun getSignInIntent(): Intent {
        return authRepository.getGoogleSignInClientWithSheets().signInIntent
    }

    override fun getAccountCredential(userEmail: String): GoogleAccountCredential? {
        return GoogleAccountCredential.usingOAuth2(
            context,
            listOf(SheetsScopes.SPREADSHEETS)
        ).apply {
            selectedAccount = Account(userEmail, "com.google")
        }
    }
}
```

#### 3. Update `GoogleSheetsService` to Use Provider
```kotlin
class GoogleSheetsService(
    private val credentialProvider: GoogleCredentialProvider,
    private val authRepository: AuthRepository,
    private val logger: Logger
) {
    fun requestAuthorization(): Intent {
        logger.d("GoogleSheetsService", "Requesting Google Sheets authorization")
        return credentialProvider.getSignInIntent()
    }

    fun createSheetsService(): Result<Sheets> {
        // Use credentialProvider.getAccountCredential()
    }
}
```

#### 4. Update Tests to Mock Provider
```kotlin
@Mock
private lateinit var credentialProvider: GoogleCredentialProvider

@Before
fun setup() {
    googleSheetsService = GoogleSheetsService(
        credentialProvider,
        authRepository,
        logger
    )
}

@Test
fun `requestAuthorization returns success when authorization granted`() {
    // Given: Provider returns mock intent
    val mockIntent = mock<Intent>()
    whenever(credentialProvider.getSignInIntent()).thenReturn(mockIntent)

    // When: Requesting authorization
    val intent = googleSheetsService.requestAuthorization()

    // Then: Should return intent
    assertEquals(mockIntent, intent)
}
```

## Implementation Steps

### Step 1: Create Abstraction Layer
- [ ] Create `GoogleCredentialProvider` interface
- [ ] Create `AndroidGoogleCredentialProvider` implementation
- [ ] Add Hilt module for provider binding

### Step 2: Refactor GoogleSheetsService
- [ ] Update constructor to accept `GoogleCredentialProvider`
- [ ] Replace direct Android API calls with provider methods
- [ ] Update logging

### Step 3: Update Tests
- [ ] Add `@Mock` for `GoogleCredentialProvider`
- [ ] Update test setup
- [ ] Fix failing tests with proper mocking
- [ ] Verify all 9 tests pass

### Step 4: Update Hilt Module
- [ ] Create `@Provides` for `GoogleCredentialProvider`
- [ ] Bind implementation
- [ ] Verify injection works

## Alternative: Accept Current Test State

### Pragmatic Approach
Since the failing tests are specifically for Android integration points:

1. **Mark tests as Android-specific** with `@Ignore` annotation
2. **Add comments** explaining they require instrumentation tests
3. **Create instrumentation test placeholders** for Phase 8 (Integration Testing)
4. **Accept 5/9 passing tests** for Phase 1

### Pros of This Approach
- ✅ Faster to implement
- ✅ Doesn't require architecture changes
- ✅ Tests that can pass in unit tests do pass
- ✅ Clear separation of unit vs integration tests

### Cons of This Approach
- ❌ Less test coverage for Phase 1
- ❌ Defers integration testing to Phase 8
- ❌ Potential issues not caught early

## Recommendation

**I recommend the Pragmatic Approach (Alternative) for now** because:

1. **Time-efficient**: Refactoring would add significant time to Phase 1
2. **Clear intent**: Phase 8 is specifically for integration testing
3. **Good coverage**: 5/9 tests passing covers the core logic
4. **Follows plan**: The plan already includes comprehensive integration tests in Phase 8

### Implementation
```kotlin
@Ignore("Requires Android instrumentation - covered in integration tests")
@Test
fun `requestAuthorization returns success when authorization granted`() {
    // Test implementation
}
```

Add to plan document:
```markdown
**Note**: Tests marked with `@Ignore` require Android instrumentation and will be
covered by integration tests in Phase 8. These tests verify Android-specific
behavior (GoogleSignInClient, GoogleAccountCredential) that cannot be properly
unit tested.
```

## Decision Required

Which approach should we take?

**Option A**: Refactor with abstraction layer (better architecture, more time)
**Option B**: Mark as integration tests, defer to Phase 8 (faster, pragmatic)

My recommendation: **Option B** - Mark as integration tests and continue to Phase 2.
