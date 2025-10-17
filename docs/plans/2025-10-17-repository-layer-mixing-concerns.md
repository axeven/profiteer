# Remove UI Dependencies from Repository Layer

**Date**: 2025-10-17
**Status**: 🔴 NOT STARTED
**Priority**: 🔥 HIGH
**Anti-Pattern**: #3 - Repository Layer Mixing Concerns
**Approach**: Test-Driven Development (TDD)

## Problem Statement

Four repositories currently inject and call `SharedErrorViewModel`, violating separation of concerns and making repositories difficult to test:

- `TransactionRepository` - 7 calls to `sharedErrorViewModel.showError()`
- `WalletRepository` - 2 calls to `sharedErrorViewModel.showError()`
- `UserPreferencesRepository` - 1 call to `sharedErrorViewModel.showError()`
- `CurrencyRateRepository` - 1 call to `sharedErrorViewModel.showError()`

**Total**: 11 UI method calls from data layer

## Goals

- ✅ Remove all `SharedErrorViewModel` dependencies from repository layer
- ✅ Maintain consistent error handling using `Result<T>` pattern
- ✅ Move error UI display responsibility to ViewModels
- ✅ Improve repository testability
- ✅ Preserve all existing error handling behavior
- ✅ No breaking changes to public APIs

## Architecture Changes

### Before (Current - Incorrect)
```
Repository → SharedErrorViewModel → UI
         ↓
    Result<T>
```

### After (Target - Correct)
```
Repository → Result<T> → ViewModel → UI State → UI
                                   ↓
                         SharedErrorViewModel (optional)
```

## Implementation Plan

### Phase 1: Analysis & Preparation (TDD Setup)

#### Task 1.1: Document Current Behavior
- [ ] Create test cases documenting all 11 `sharedErrorViewModel.showError()` calls
- [ ] Document error messages, context, and user impact for each call
- [ ] Identify which errors are critical vs informational
- [ ] Map repository methods to their error handling patterns
- [ ] Create baseline test suite to prevent regression

**Deliverable**: `docs/repository-error-mapping.md` with complete error inventory

#### Task 1.2: Design Error Domain Model
- [ ] Create domain-specific error types for each repository
- [ ] Design `RepositoryError` sealed class hierarchy
- [ ] Define error severity levels (Critical, Warning, Info)
- [ ] Design error context data (operation, affected resource, retry-ability)
- [ ] Write unit tests for error type conversions

**Deliverable**: `data/model/RepositoryError.kt` with comprehensive test coverage

### Phase 2: Test Infrastructure (TDD Foundation)

#### Task 2.1: Create Repository Test Helpers
- [ ] Write test helper to verify `Result<T>` error types
- [ ] Create mock/fake implementations without UI dependencies
- [ ] Build assertion helpers for error content validation
- [ ] Set up test fixtures for common error scenarios
- [ ] Write integration test framework for repository-ViewModel flow

**Deliverable**: `test/helpers/RepositoryTestHelpers.kt`

#### Task 2.2: Write Failing Tests First (RED Phase)
- [ ] Write tests expecting `Result.failure()` instead of UI calls
- [ ] Create tests for each of the 11 error scenarios
- [ ] Write tests verifying no `SharedErrorViewModel` calls
- [ ] Add tests for error context preservation
- [ ] Write ViewModel tests expecting error state updates

**Expected**: All tests should FAIL initially (RED phase)

**Deliverable**: Comprehensive failing test suite

### Phase 3: Repository Refactoring (TDD Implementation)

#### Task 3.1: Refactor CurrencyRateRepository (Smallest - 1 usage)
- [ ] **RED**: Run existing failing tests
- [ ] **GREEN**: Remove `SharedErrorViewModel` constructor parameter
- [ ] **GREEN**: Replace `sharedErrorViewModel.showError()` with `Result.failure()`
- [ ] **GREEN**: Return domain-specific error types
- [ ] **REFACTOR**: Clean up error handling logic
- [ ] **TEST**: Verify all tests pass
- [ ] **TEST**: Run full repository test suite
- [ ] Update dependency injection module

**Deliverable**: `CurrencyRateRepository` with zero UI dependencies

#### Task 3.2: Refactor UserPreferencesRepository (Small - 1 usage)
- [ ] **RED**: Run existing failing tests
- [ ] **GREEN**: Remove `SharedErrorViewModel` constructor parameter
- [ ] **GREEN**: Replace `sharedErrorViewModel.showError()` with `Result.failure()`
- [ ] **GREEN**: Add proper error context to failures
- [ ] **REFACTOR**: Standardize error creation patterns
- [ ] **TEST**: Verify all tests pass
- [ ] **TEST**: Run integration tests with ViewModels
- [ ] Update dependency injection module

**Deliverable**: `UserPreferencesRepository` with zero UI dependencies

#### Task 3.3: Refactor WalletRepository (Medium - 2 usages)
- [ ] **RED**: Run existing failing tests for both error cases
- [ ] **GREEN**: Remove `SharedErrorViewModel` constructor parameter
- [ ] **GREEN**: Replace both `sharedErrorViewModel.showError()` calls
- [ ] **GREEN**: Return appropriate `WalletRepositoryError` types
- [ ] **GREEN**: Preserve error context and messages
- [ ] **REFACTOR**: Extract common error handling patterns
- [ ] **TEST**: Verify all tests pass
- [ ] **TEST**: Test wallet-related ViewModel error handling
- [ ] Update dependency injection module

**Deliverable**: `WalletRepository` with zero UI dependencies

#### Task 3.4: Refactor TransactionRepository (Largest - 7 usages)
- [ ] **RED**: Run existing failing tests for all 7 error cases
- [ ] **GREEN**: Remove `SharedErrorViewModel` constructor parameter
- [ ] **GREEN**: Replace all 7 `sharedErrorViewModel.showError()` calls
- [ ] **GREEN**: Return detailed `TransactionRepositoryError` types
- [ ] **GREEN**: Maintain Firebase error context
- [ ] **REFACTOR**: Consolidate duplicate error handling code
- [ ] **REFACTOR**: Extract error creation to helper methods
- [ ] **TEST**: Verify all tests pass
- [ ] **TEST**: Run complete integration test suite
- [ ] Update dependency injection module

**Deliverable**: `TransactionRepository` with zero UI dependencies

### Phase 4: ViewModel Integration (TDD Continuation)

#### Task 4.1: Update TransactionViewModel Error Handling
- [ ] **RED**: Write tests expecting error state updates from Result failures
- [ ] **GREEN**: Add error handling for all repository Result<T> returns
- [ ] **GREEN**: Map `TransactionRepositoryError` to UI state
- [ ] **GREEN**: Optionally call `SharedErrorViewModel` for user-facing errors
- [ ] **REFACTOR**: Extract error mapping to helper function
- [ ] **TEST**: Verify error messages reach UI correctly
- [ ] **TEST**: Test error state clearing and recovery

**Deliverable**: `TransactionViewModel` properly handling repository errors

#### Task 4.2: Update WalletViewModel Error Handling
- [ ] **RED**: Write tests for wallet error scenarios
- [ ] **GREEN**: Handle `Result.failure()` from repository calls
- [ ] **GREEN**: Update UI state with appropriate error messages
- [ ] **GREEN**: Integrate with `SharedErrorViewModel` if needed
- [ ] **REFACTOR**: Standardize error display patterns
- [ ] **TEST**: Verify all wallet operations show errors correctly

**Deliverable**: Updated ViewModel with proper error handling

#### Task 4.3: Update Other Affected ViewModels
- [ ] **RED**: Write tests for HomeViewModel, SettingsViewModel, ReportViewModel
- [ ] **GREEN**: Handle errors from UserPreferencesRepository
- [ ] **GREEN**: Handle errors from CurrencyRateRepository
- [ ] **GREEN**: Update UI states with error information
- [ ] **REFACTOR**: Create shared error handling utilities
- [ ] **TEST**: End-to-end integration tests for each ViewModel

**Deliverable**: All ViewModels properly handling repository errors

### Phase 5: Dependency Injection Updates

#### Task 5.1: Update Hilt Modules
- [ ] Remove `SharedErrorViewModel` from repository providers
- [ ] Update repository constructor signatures in DI modules
- [ ] Verify dependency graph compiles correctly
- [ ] Remove unused `SharedErrorViewModel` bindings if applicable
- [ ] Update module documentation

**Deliverable**: Clean dependency injection configuration

#### Task 5.2: Update Repository Tests
- [ ] Remove mock `SharedErrorViewModel` from all repository tests
- [ ] Update test constructors to match new signatures
- [ ] Add new tests for `Result<T>` error handling
- [ ] Verify test coverage is maintained or improved
- [ ] Run complete test suite

**Deliverable**: All repository tests passing without UI dependencies

### Phase 6: Validation & Cleanup

#### Task 6.1: Integration Testing
- [ ] Run full app test suite (unit + integration)
- [ ] Test all error scenarios manually in debug build
- [ ] Verify error messages still display correctly to users
- [ ] Test error recovery flows (retry, dismiss, etc.)
- [ ] Performance test error handling paths
- [ ] Test with Firestore errors, network errors, auth errors

**Deliverable**: Validation report confirming no regressions

#### Task 6.2: Code Quality Review
- [ ] Run lint and ensure no new warnings
- [ ] Verify all repositories follow same error pattern
- [ ] Check for any remaining `SharedErrorViewModel` references
- [ ] Review error message consistency across app
- [ ] Update architecture documentation

**Deliverable**: Clean code quality metrics

#### Task 6.3: Documentation Updates
- [ ] Update `CLAUDE.md` with new repository patterns
- [ ] Update `docs/antipatterns.md` to mark issue as resolved
- [ ] Create `docs/ERROR_HANDLING_GUIDELINES.md` for future reference
- [ ] Document error type hierarchy and usage
- [ ] Add code examples to documentation

**Deliverable**: Comprehensive documentation of new patterns

### Phase 7: Final Verification

#### Task 7.1: Regression Testing
- [ ] Run automated test suite (expect 100% pass rate)
- [ ] Manual testing of all repository-dependent features
- [ ] Test error scenarios end-to-end
- [ ] Verify SharedErrorViewModel still works where needed (in ViewModels)
- [ ] Performance profiling to ensure no degradation

**Deliverable**: Test report with zero failures

#### Task 7.2: Update Anti-Pattern Document
- [ ] Mark anti-pattern #3 as ✅ RESOLVED in `docs/antipatterns.md`
- [ ] Update metrics (from 87.5% to 100% resolved patterns)
- [ ] Document lessons learned
- [ ] Update implementation details with actual approach
- [ ] Add before/after code examples

**Deliverable**: Updated `docs/antipatterns.md`

## Success Criteria

### Functional Requirements
- ✅ All 11 UI calls removed from repository layer
- ✅ Zero `SharedErrorViewModel` dependencies in any repository
- ✅ All repository methods return `Result<T>` consistently
- ✅ Error messages still display correctly to users
- ✅ No user-facing behavior changes

### Technical Requirements
- ✅ 100% test coverage for error handling paths
- ✅ All tests passing (unit + integration)
- ✅ Clean separation: Data layer → Domain layer → UI layer
- ✅ Improved repository testability (no UI mocks needed)
- ✅ Reduced coupling between layers

### Code Quality
- ✅ No lint warnings introduced
- ✅ Consistent error handling patterns across all repositories
- ✅ Comprehensive documentation
- ✅ No TODO or FIXME comments remaining

## Risk Mitigation

### Risk 1: Breaking Existing Error Handling
**Mitigation**:
- Write comprehensive tests before any changes (TDD RED phase)
- Test all error scenarios manually before and after
- Maintain error message parity

### Risk 2: Missing Error Cases
**Mitigation**:
- Document all 11 current error calls before starting
- Cross-reference with Firebase error codes
- Integration tests for each error path

### Risk 3: ViewModel Complexity
**Mitigation**:
- Create shared error handling utilities
- Extract error mapping to reusable functions
- Keep error handling patterns consistent

### Risk 4: Test Suite Maintenance
**Mitigation**:
- Use test helpers to reduce duplication
- Document test patterns for future reference
- Regular test suite health checks

## Testing Strategy

### Unit Tests (Per Repository)
- [ ] Test each public method returns `Result<T>`
- [ ] Test success cases return `Result.success()`
- [ ] Test error cases return `Result.failure()` with correct error type
- [ ] Test error context preservation
- [ ] Test no UI dependencies in constructor

### Integration Tests (Repository + ViewModel)
- [ ] Test ViewModel receives and handles repository errors
- [ ] Test error state updates correctly
- [ ] Test UI displays error messages
- [ ] Test error recovery flows

### End-to-End Tests
- [ ] Test complete user flow with induced errors
- [ ] Test Firebase errors propagate correctly
- [ ] Test network errors display properly
- [ ] Test auth errors trigger correct behavior

## Estimated Timeline

| Phase | Tasks | Estimated Time | Dependencies |
|-------|-------|----------------|--------------|
| Phase 1 | Analysis & Preparation | 2-3 hours | None |
| Phase 2 | Test Infrastructure | 2-3 hours | Phase 1 |
| Phase 3 | Repository Refactoring | 4-6 hours | Phase 2 |
| Phase 4 | ViewModel Integration | 3-4 hours | Phase 3 |
| Phase 5 | Dependency Injection | 1-2 hours | Phase 4 |
| Phase 6 | Validation & Cleanup | 2-3 hours | Phase 5 |
| Phase 7 | Final Verification | 1-2 hours | Phase 6 |
| **Total** | | **15-23 hours** | |

## Implementation Order

1. **CurrencyRateRepository** (Smallest, 1 usage) - Learn the pattern
2. **UserPreferencesRepository** (Small, 1 usage) - Refine the pattern
3. **WalletRepository** (Medium, 2 usages) - Scale the pattern
4. **TransactionRepository** (Largest, 7 usages) - Master the pattern

This order allows learning and refining the approach on smaller repositories before tackling the most complex one.

## Related Documents

- [Anti-Patterns Document](../antipatterns.md) - Original issue identification
- [Firebase Security Guidelines](../FIREBASE_SECURITY_GUIDELINES.md) - Error handling context
- [Logging Guidelines](../LOGGING_GUIDELINES.md) - Error logging patterns

## Notes

- This refactoring follows the **Boy Scout Rule**: Leave the codebase better than you found it
- TDD approach ensures no regressions and comprehensive test coverage
- Changes are isolated to repositories and their consumers (ViewModels)
- No UI changes required - error display mechanism remains the same
- Pattern established here can be applied to future repositories

## Progress Tracking

- **Start Date**: 2025-10-17
- **Completion Date**: TBD
- **Completed Tasks**: 7 / 65 (10.8%)
- **Current Phase**: ✅ Phase 1 Complete - Ready for Phase 2
- **Blockers**: None

### Phase 1 Completion Summary

✅ **Task 1.1: Document Current Behavior** - COMPLETE
- Created `docs/repository-error-mapping.md`
- Documented all 11 `sharedErrorViewModel.showError()` calls
- Mapped error contexts, user impacts, and recovery patterns
- Identified 2 distinct error patterns (Flow-closing vs non-closing)

✅ **Task 1.2: Design Error Domain Model** - COMPLETE
- Created `app/src/main/java/com/axeven/profiteerapp/data/model/RepositoryError.kt`
- Designed sealed class hierarchy with 8 error types
- Implemented `CompositeError` for 4-query composite pattern
- Created extension function for ErrorInfo to RepositoryError conversion
- Added comprehensive KDoc documentation

✅ **Task 1.3: Write Unit Tests** - COMPLETE
- Created `app/src/test/java/com/axeven/profiteerapp/data/model/RepositoryErrorTest.kt`
- 27 unit tests covering all error types
- All tests passing (100% success rate)
- Comprehensive coverage of:
  - Error type creation and field validation
  - Message formatting
  - Composite error aggregation (requiresReauth, shouldRetry, isOffline)
  - ErrorInfo to RepositoryError conversion
  - Exception behavior and throwability

**Next Steps**: Begin Phase 2 - Test Infrastructure (TDD Foundation)

---

**Last Updated**: 2025-10-17
**Author**: Claude Code
**Status**: 🟢 Phase 1 Complete - In Progress
