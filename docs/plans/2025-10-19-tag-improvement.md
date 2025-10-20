# Tag Improvement Plan: Case-Insensitive Tags & Whitespace Trimming

**Date**: 2025-10-19
**Status**: 🚧 In Progress (Phase 1-3 ✅ Complete)
**Priority**: Medium
**Effort Estimate**: 2-3 hours

## Problem Statement

Currently, the tag system has the following limitations:
1. **Case Sensitivity**: Tags are case-sensitive, leading to duplicates (e.g., "food", "Food", "FOOD" are treated as separate tags)
2. **Whitespace Issues**: No trimming of leading/trailing whitespace, allowing inconsistent tags (e.g., " food ", "food")
3. **Poor User Experience**: Users must remember exact capitalization for autocomplete to work
4. **Data Inconsistency**: Existing Firestore data may contain duplicate tags with different cases

## Goals

1. Make tag comparison case-insensitive while preserving original user input
2. Trim all whitespace before saving or comparing tags
3. Handle existing Firestore data with duplicate tags (migration strategy)
4. Maintain backward compatibility with existing transactions
5. Improve tag autocomplete to be case-insensitive

## Current Implementation Analysis

### Files Affected
- `Transaction.kt:22` - Tag storage (`tags: List<String>`)
- `TransactionViewModel.kt:64-67` - Tag collection and deduplication
- `TransactionViewModel.kt:512-518` - Tag autocomplete suggestions
- `CreateTransactionUiState.kt:32,48,169` - Tag input handling
- UI screens that display/input tags

### Current Tag Flow
1. **Input**: User types tags in UI (comma-separated)
2. **Storage**: Tags stored as-is in Transaction model
3. **Collection**: ViewModel collects unique tags from all transactions
4. **Autocomplete**: Case-insensitive filtering on suggestions
5. **Display**: Tags shown as-is from storage

### Issues Identified
- ✅ Autocomplete already case-insensitive (`it.contains(input, ignoreCase = true)`)
- ❌ Tag deduplication is case-sensitive (`.distinct()`)
- ❌ No whitespace trimming on input
- ❌ Tag storage doesn't normalize case

## Solution Design

### Approach: Normalize on Input, Preserve Display

**Strategy**: Store tags in a normalized format (lowercase, trimmed) but preserve original capitalization for display purposes if needed in the future.

**Why This Approach**:
- Simple to implement and test
- No schema changes required
- Backward compatible with existing data
- Minimal migration complexity

### Tag Normalization Rules

```kotlin
fun String.normalizeTag(): String {
    return this.trim().lowercase()
}

fun List<String>.normalizeTags(): List<String> {
    return this
        .map { it.normalizeTag() }
        .filter { it.isNotBlank() }
        .distinct()
}
```

## Implementation Plan (TDD Approach)

### Phase 1: Create Utility Functions & Tests ✅ COMPLETED
**Test-First Development**

- [x] **1.1 Create TagNormalizer utility class**
  - File: `app/src/main/java/com/axeven/profiteerapp/utils/TagNormalizer.kt` ✅
  - Functions:
    - `normalizeTag(tag: String): String` - Trim and lowercase single tag ✅
    - `normalizeTags(tags: List<String>): List<String>` - Process tag list ✅
    - `parseTagInput(input: String): List<String>` - Parse comma-separated tags ✅

- [x] **1.2 Write comprehensive unit tests FIRST**
  - File: `app/src/test/java/com/axeven/profiteerapp/utils/TagNormalizerTest.kt` ✅
  - Test cases: 40 tests written and ALL PASSING ✅
    - ✅ `normalizeTag - trims leading whitespace`
    - ✅ `normalizeTag - trims trailing whitespace`
    - ✅ `normalizeTag - converts to lowercase`
    - ✅ `normalizeTag - handles empty string`
    - ✅ `normalizeTag - handles whitespace-only string`
    - ✅ `normalizeTags - removes duplicates case-insensitively`
    - ✅ `normalizeTags - preserves order of first occurrence`
    - ✅ `normalizeTags - filters out blank tags`
    - ✅ `normalizeTags - handles mixed case duplicates ("Food", "food", "FOOD")`
    - ✅ `parseTagInput - splits on comma`
    - ✅ `parseTagInput - trims each tag`
    - ✅ `parseTagInput - removes duplicates`
    - ✅ `parseTagInput - handles spaces around commas`
    - ✅ `parseTagInput - handles "food, Food, FOOD" → ["food"]`
    - ✅ Plus 26 additional edge case tests (unicode, emojis, performance, etc.)

- [x] **1.3 Implement functions to pass tests**
  - Run tests: `./gradlew testDebugUnitTest --tests "*TagNormalizerTest"` ✅
  - All 40 tests passing (0 failures, 0 errors) ✅
  - Total execution time: 0.061s ✅

### Phase 2: Update Transaction Creation & Editing ✅ COMPLETED
**Test-First Development**

- [x] **2.1 Write tests for CreateTransactionUiState**
  - File: `app/src/test/java/com/axeven/profiteerapp/ui/transaction/CreateTransactionStateManagerTest.kt` ✅
  - Added 13 test cases:
    - ✅ `updateTags - normalizes tags (trim whitespace, lowercase, remove duplicates, etc.)`
    - ✅ `fromExistingTransaction - normalizes tags on load`
    - ✅ `getTransactionSummary - returns normalized tags`

- [x] **2.2 Update CreateTransactionUiState to normalize tags**
  - Files updated:
    - `app/src/main/java/com/axeven/profiteerapp/data/ui/CreateTransactionUiState.kt` ✅
    - `app/src/main/java/com/axeven/profiteerapp/ui/transaction/CreateTransactionStateManager.kt` ✅
  - Applied `TagNormalizer` in:
    - `updateTags()` - normalize tags input ✅
    - `fromExistingTransaction()` - normalize loaded tags ✅
    - `getTransactionSummary()` - parse normalized tags ✅
  - Tests: 43 tests passing (0 failures) ✅

- [x] **2.3 Write tests for EditTransactionUiState**
  - File: `app/src/test/java/com/axeven/profiteerapp/ui/state/EditTransactionUiStateTest.kt` ✅
  - Added 12 test cases for tag normalization:
    - ✅ All normalization scenarios (trim, lowercase, duplicates, etc.)
    - ✅ `fromTransaction` normalization tests
    - ✅ `hasChanges` detection with normalized tags

- [x] **2.4 Update EditTransactionUiState to normalize tags**
  - File: `app/src/main/java/com/axeven/profiteerapp/data/ui/EditTransactionUiState.kt` ✅
  - Applied normalization in:
    - `updateAndValidate()` - normalize tags input ✅
    - `fromTransaction()` - normalize loaded tags ✅
    - `hasChanges` - compare normalized tags ✅
  - Tests: 33 tests passing (0 failures) ✅

### Phase 3: Update ViewModel Tag Collection ✅ COMPLETED
**Test-First Development**

- [x] **3.1 Write tests for TransactionViewModel tag deduplication**
  - File: `app/src/test/java/com/axeven/profiteerapp/viewmodel/TransactionViewModelTagTest.kt` ✅
  - Created 12 comprehensive test cases:
    - ✅ `availableTags - deduplicates case-insensitively`
    - ✅ `availableTags - trims whitespace`
    - ✅ `availableTags - filters out "Untagged" keyword`
    - ✅ `availableTags - filters out blank tags`
    - ✅ `availableTags - sorted alphabetically`
    - ✅ `availableTags - complex real-world scenario`
    - ✅ `getTagSuggestions - case-insensitive matching`
    - ✅ `getTagSuggestions - suggests normalized tags`
    - ✅ `getTagSuggestions - does not suggest "Untagged"`
    - ✅ `getTagSuggestions - respects minimum character limit`
    - ✅ `getTagSuggestions - limits number of suggestions`
    - ✅ `getTagSuggestions - handles partial matches`

- [x] **3.2 Update TransactionViewModel.loadData()**
  - File: `app/src/main/java/com/axeven/profiteerapp/viewmodel/TransactionViewModel.kt` ✅
  - Updated tag collection logic to use `TagNormalizer.normalizeTags()`:
    ```kotlin
    val allTags = transactions.flatMap { it.tags }
    val uniqueTags = TagNormalizer.normalizeTags(allTags).sorted()
    ```
  - Tests: 12/12 tests passing ✅

- [x] **3.3 Update tag autocomplete to use normalized comparison**
  - Verified existing `getTagSuggestions()` works correctly with normalized tags ✅
  - Already case-insensitive, now works with normalized availableTags ✅
  - Comprehensive tests added and passing ✅

### Phase 4: Data Migration Strategy ✅
**Handle Existing Firestore Data**

- [ ] **4.1 Create migration utility class**
  - File: `app/src/main/java/com/axeven/profiteerapp/data/migration/TagMigration.kt`
  - Function: `migrateTransactionTags(userId: String): Result<Int>`
  - Process:
    1. Fetch all user transactions
    2. For each transaction with tags:
       - Normalize tags using `TagNormalizer`
       - Update only if tags changed
    3. Return count of updated transactions

- [ ] **4.2 Write tests for migration**
  - File: `app/src/test/java/com/axeven/profiteerapp/data/migration/TagMigrationTest.kt`
  - Test cases:
    - ✅ `migrateTransactionTags - normalizes existing tags`
    - ✅ `migrateTransactionTags - skips already normalized tags`
    - ✅ `migrateTransactionTags - handles empty tags`
    - ✅ `migrateTransactionTags - reports correct count`

- [ ] **4.3 Implement migration logic**
  - Run tests until passing
  - Add logging for migration progress

- [ ] **4.4 Add migration trigger**
  - Option 1: One-time migration on app startup (with flag in UserPreferences)
  - Option 2: Manual migration in Settings screen
  - **Recommended**: Option 1 with preferences flag `tagsMigrationCompleted`
  - File: `app/src/main/java/com/axeven/profiteerapp/MainActivity.kt` or dedicated migration manager

### Phase 5: UI Integration Testing ✅

- [ ] **5.1 Write integration tests for CreateTransactionScreen**
  - File: `app/src/test/java/com/axeven/profiteerapp/ui/transaction/CreateTransactionScreenIntegrationTest.kt`
  - Test scenarios:
    - ✅ User enters "Food, food, FOOD" → saves as ["food"]
    - ✅ User enters " travel " → saves as ["travel"]
    - ✅ Tag autocomplete shows suggestions case-insensitively
    - ✅ Duplicate tags are prevented on save

- [ ] **5.2 Write integration tests for EditTransactionScreen**
  - File: `app/src/test/java/com/axeven/profiteerapp/ui/transaction/EditTransactionScreenIntegrationTest.kt`
  - Similar scenarios as create screen

- [ ] **5.3 Manual UI testing checklist**
  - [ ] Create transaction with tags: "Food, food, FOOD" → verify saved as "food"
  - [ ] Create transaction with tags: " travel , Transport " → verify saved as "travel, transport"
  - [ ] Edit existing transaction tags → verify normalization
  - [ ] Tag autocomplete: type "foo" → should suggest "food" if it exists
  - [ ] Tag autocomplete: type "FOO" → should suggest "food" (case-insensitive)
  - [ ] Verify no duplicate tags in tag suggestions list
  - [ ] Verify transaction list displays normalized tags correctly

### Phase 6: Documentation & Cleanup ✅

- [ ] **6.1 Update CLAUDE.md**
  - Document tag normalization behavior
  - Add to "Business Logic & Validation" section
  - Update "Known Issues & Solutions" if applicable

- [ ] **6.2 Update code comments**
  - Add documentation to `Transaction.kt` about tag normalization
  - Document TagNormalizer utility functions

- [ ] **6.3 Update README.md**
  - Add note about case-insensitive tags feature
  - Mention automatic migration on first launch

- [ ] **6.4 Run full test suite**
  - [ ] `./gradlew testDebugUnitTest` - All unit tests pass
  - [ ] `./gradlew lint` - No new lint warnings
  - [ ] Manual smoke testing on device/emulator

- [ ] **6.5 Update this plan document**
  - Mark all tasks as completed
  - Update status to ✅ Completed
  - Document any deviations from plan

## Testing Strategy Summary

### Unit Tests (75% coverage target)
- `TagNormalizerTest.kt` - Core normalization logic
- `CreateTransactionUiStateTest.kt` - UI state tag handling
- `EditTransactionUiStateTest.kt` - UI state tag handling
- `TransactionViewModelTest.kt` - Tag collection and suggestions
- `TagMigrationTest.kt` - Data migration logic

### Integration Tests
- `CreateTransactionScreenIntegrationTest.kt` - End-to-end create flow
- `EditTransactionScreenIntegrationTest.kt` - End-to-end edit flow

### Manual Testing
- Tag input edge cases
- Autocomplete behavior
- Migration verification on test account

## Migration Strategy Details

### Option 1: Automatic Migration (Recommended)

```kotlin
// In MainActivity or MigrationManager
suspend fun ensureTagsMigrated(userId: String) {
    val preferences = userPreferencesRepository.getUserPreferences(userId).first()

    if (preferences?.tagsMigrationCompleted != true) {
        logger.i("Migration", "Starting tag normalization migration...")
        val result = tagMigration.migrateTransactionTags(userId)

        result.onSuccess { count ->
            logger.i("Migration", "Migrated $count transactions")
            userPreferencesRepository.updateTagsMigrationFlag(userId, true)
        }.onFailure { error ->
            logger.e("Migration", "Migration failed", error)
        }
    }
}
```

### UserPreferences Schema Update

Add new field: `tagsMigrationCompleted: Boolean = false`

## Success Criteria

- ✅ All unit tests pass
- ✅ All integration tests pass
- ✅ Lint checks pass with no new warnings
- ✅ Tags are case-insensitive in UI and storage
- ✅ Whitespace is trimmed from all tags
- ✅ Existing data migrated successfully
- ✅ No duplicate tags in autocomplete suggestions
- ✅ Backward compatibility maintained
- ✅ Documentation updated

## Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| Migration fails for large datasets | High | Add progress tracking, error recovery, batch processing |
| Breaking existing transactions | Critical | Thorough testing, migration can be re-run safely |
| Performance impact on tag collection | Medium | Monitor performance, add caching if needed |
| User confusion with normalized tags | Low | Clear UI messaging about normalization |

## Rollback Plan

If critical issues arise:
1. Revert code changes (git revert)
2. Migration is non-destructive (only updates tags field)
3. Original tags can be restored from Firestore history (if enabled)
4. Re-run migration with fixed logic

## Future Enhancements

- [ ] Support for tag aliases (e.g., "grocery" → "groceries")
- [ ] Tag categories/hierarchies
- [ ] Most frequently used tags
- [ ] Tag-based filtering in transaction list
- [ ] Tag analytics and insights

## Progress Tracking

**Phase 1**: ✅ **COMPLETED** (TagNormalizer utility + 40 passing tests)
**Phase 2**: ✅ **COMPLETED** (Transaction UI states + 25 new passing tests)
**Phase 3**: ✅ **COMPLETED** (ViewModel tag collection + 12 new passing tests)
**Phase 4**: ⬜ Not Started (Data migration - optional for now)
**Phase 5**: ⬜ Not Started (UI integration testing - manual)
**Phase 6**: ⬜ Not Started (Documentation)

**Overall Progress**: 3/6 phases completed (50%)

## Notes

- Follow TDD strictly: Write tests FIRST, then implement
- Run tests after each implementation step
- Update this document as implementation progresses
- All Firestore queries must maintain userId filter for security
- Consider adding TagNormalizer to the logging sanitization list
