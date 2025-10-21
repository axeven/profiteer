# Camel Case Tag Display Implementation Plan

**Status**: 🟢 In Progress
**Created**: 2025-10-20
**Last Updated**: 2025-10-21
**Priority**: Medium
**Effort**: Small (~4 hours)

## Overview

Improve tag display across the application by formatting all displayed tags in camel case (e.g., "Food", "Travel", "GroceryShopping") while maintaining lowercase storage for consistency and case-insensitive operations.

### Current Behavior
- Tags stored in database: lowercase (e.g., "food", "travel", "grocery shopping")
- Tags displayed in UI: lowercase (same as storage)
- Tag normalization: All input converted to lowercase

### Target Behavior
- Tags stored in database: lowercase (no change) ✅
- Tags displayed in UI: camel case (e.g., "Food", "Travel", "Grocery Shopping") 🎯
- Tag normalization: Continues to convert to lowercase for storage
- Tag matching: Remains case-insensitive

### Benefits
- **Better UX**: More visually appealing and easier to read
- **Professional appearance**: Standard formatting for user-facing text
- **Maintains consistency**: Storage remains lowercase for reliable matching
- **No data migration needed**: Changes only affect display layer

---

## Architecture Overview

### Storage Layer (No Changes)
```
Database (Firestore)
    ↓
Tags stored as: ["food", "travel", "grocery shopping"]
    ↓
Repository returns: Flow<List<Transaction>>
```

### Display Layer (NEW: Formatting Applied)
```
ViewModel receives: ["food", "travel", "grocery shopping"]
    ↓
NEW: TagFormatter.toCamelCase() applied
    ↓
UI displays: ["Food", "Travel", "Grocery Shopping"]
```

### Input Layer (No Changes)
```
User types: "Food, Travel, GROCERY SHOPPING"
    ↓
TagNormalizer.parseTagInput() (existing)
    ↓
Stored as: ["food", "travel", "grocery shopping"]
```

---

## Implementation Checklist

### Phase 1: Test-Driven Development Setup ✅
- [x] **1.1** Create test file `TagFormatterTest.kt`
  - Location: `app/src/test/java/com/axeven/profiteerapp/utils/TagFormatterTest.kt`
  - Setup: Basic test class structure with JUnit4 and Truth assertions

- [x] **1.2** Write failing tests for single tag formatting
  - Test: Single word lowercase → Camel case (`"food"` → `"Food"`)
  - Test: Single word uppercase → Camel case (`"FOOD"` → `"Food"`)
  - Test: Single word mixed case → Camel case (`"fOoD"` → `"Food"`)
  - Test: Empty string → Empty string (`""` → `""`)
  - Test: Whitespace only → Empty string (`"   "` → `""`)

- [x] **1.3** Write failing tests for multi-word tags
  - Test: Two words lowercase → Camel case each word (`"grocery shopping"` → `"Grocery Shopping"`)
  - Test: Two words uppercase → Camel case each word (`"GROCERY SHOPPING"` → `"Grocery Shopping"`)
  - Test: Multiple spaces between words → Single space (`"grocery  shopping"` → `"Grocery Shopping"`)
  - Test: Leading/trailing whitespace → Trimmed and formatted (`"  food  "` → `"Food"`)

- [x] **1.4** Write failing tests for edge cases
  - Test: Single character → Uppercase (`"a"` → `"A"`)
  - Test: Numbers in tag → Preserved (`"groceries2024"` → `"Groceries2024"`)
  - Test: Special characters → Preserved (`"food&drink"` → `"Food&drink"`)
  - Test: Hyphenated words → Each part capitalized (`"food-related"` → `"Food-Related"`)

- [x] **1.5** Write failing tests for list formatting
  - Test: Empty list → Empty list (`[]` → `[]`)
  - Test: Single tag list → Formatted list (`["food"]` → `["Food"]`)
  - Test: Multiple tags → All formatted (`["food", "travel"]` → `["Food", "Travel"]`)
  - Test: Mixed formatting → Normalized (`["food", "TRAVEL", "shopping"]` → `["Food", "Travel", "Shopping"]`)

- [x] **1.6** Write failing tests for integration with TagNormalizer
  - Test: Format after normalization (`"  Food , TRAVEL  "` → normalize → format → `["Food", "Travel"]`)
  - Test: Duplicate handling maintains formatting (`["food", "Food", "FOOD"]` → normalize → format → `["Food"]`)

- [x] **1.7** Run all tests to confirm they fail
  ```bash
  ./gradlew testDebugUnitTest --tests "TagFormatterTest"
  ```
  - **Result**: ✅ Tests fail as expected (TagFormatter class doesn't exist yet - TDD red phase)

---

### Phase 2: Core Implementation ✅
- [x] **2.1** Create `TagFormatter` utility class
  - Location: `app/src/main/java/com/axeven/profiteerapp/utils/TagFormatter.kt`
  - Package: `com.axeven.profiteerapp.utils`
  - Class: `object TagFormatter { ... }`

- [x] **2.2** Implement `formatTag(tag: String): String` function
  - Algorithm:
    1. Trim whitespace
    2. Return empty if blank
    3. Split by spaces (handling multiple consecutive spaces)
    4. Capitalize first letter of each word
    5. Lowercase remaining letters
    6. Join with single space
  - Handle hyphenated words (split on hyphen, format, rejoin)
  - Handle special characters (preserve as-is)

- [x] **2.3** Implement `formatTags(tags: List<String>): List<String>` function
  - Map over list applying `formatTag()` to each element
  - Filter out blank results

- [x] **2.4** Add comprehensive KDoc documentation
  - Document purpose: Display formatting only, not for storage
  - Document algorithm: Word capitalization rules
  - Provide usage examples
  - Note relationship with TagNormalizer

- [x] **2.5** Run tests to verify implementation
  ```bash
  ./gradlew testDebugUnitTest --tests "TagFormatterTest"
  ```
  - All tests must pass ✅

---

### Phase 3: UI Integration - HomeScreen ✅

**File**: `app/src/main/java/com/axeven/profiteerapp/ui/home/HomeScreen.kt`

- [x] **3.1** Write UI test for formatted tag display
  - Test file: `HomeScreenTest.kt` (created)
  - Test: Transaction item shows camel case tags
  - Test: "Untagged" label when tags empty

- [x] **3.2** Update `TransactionItem` composable (Lines 454-471)
  - Import `TagFormatter`
  - Apply formatting before display:
    ```kotlin
    val formattedTags = TagFormatter.formatTags(transaction.tags)
    val subtitleText = buildString {
        if (formattedTags.isNotEmpty()) {
            append(formattedTags.joinToString(", "))
        } else {
            append("Untagged")
        }
        // ... rest of subtitle
    }
    ```

- [x] **3.3** Verify no changes to data layer
  - Ensure `transaction.tags` not modified
  - Formatting applied only for display

- [x] **3.4** Manual testing
  - Debug APK builds successfully
  - All tests pass (TagFormatterTest, HomeScreenTest)
  - Ready for manual testing on device

---

### Phase 4: UI Integration - CreateTransactionScreen

**File**: `app/src/main/java/com/axeven/profiteerapp/ui/transaction/CreateTransactionScreen.kt`

- [ ] **4.1** Write tests for autocomplete suggestions formatting
  - Test: Suggestions dropdown shows camel case tags
  - Test: User input remains as typed (not formatted)
  - Test: Stored tags remain lowercase

- [ ] **4.2** Update `TagInputField` composable (Lines 896-989)
  - Format autocomplete suggestions only:
    ```kotlin
    val formattedSuggestions = TagFormatter.formatTags(suggestions)

    // Display formatted in dropdown
    DropdownMenu(...) {
        formattedSuggestions.forEach { formattedTag ->
            DropdownMenuItem(
                text = { Text(formattedTag) },
                onClick = {
                    // Append original (lowercase) tag, not formatted
                    val originalTag = suggestions[formattedSuggestions.indexOf(formattedTag)]
                    // ... existing logic ...
                }
            )
        }
    }
    ```

- [ ] **4.3** Verify input behavior
  - User can type any case: "food", "Food", "FOOD"
  - Input field shows exactly what user typed
  - Suggestions show camel case formatting
  - Stored value remains normalized (lowercase)

- [ ] **4.4** Manual testing
  - Type partial tag to trigger autocomplete
  - Verify suggestions show camel case
  - Select suggestion and verify correct tag added
  - Submit transaction and verify storage is lowercase

---

### Phase 5: UI Integration - EditTransactionScreen

**File**: `app/src/main/java/com/axeven/profiteerapp/ui/transaction/EditTransactionScreen.kt`

- [ ] **5.1** Update tag display in edit mode
  - Apply same `TagInputField` updates from Phase 4
  - Ensure loaded tags display as user typed (not pre-formatted)
  - Suggestions show camel case

- [ ] **5.2** Write tests for edit screen
  - Test: Existing tags load correctly (not pre-formatted in input)
  - Test: Autocomplete suggestions formatted
  - Test: Edited tags saved as lowercase

- [ ] **5.3** Verify `EditTransactionUiState` unchanged
  - No changes to normalization logic
  - Tags stored as lowercase in state
  - Formatting only applied for suggestion display

- [ ] **5.4** Manual testing
  - Open existing transaction for editing
  - Verify tags load in input field
  - Verify autocomplete suggestions formatted
  - Edit tags and save
  - Verify storage remains lowercase

---

### Phase 6: UI Integration - TransactionListScreen

**File**: `app/src/main/java/com/axeven/profiteerapp/ui/transaction/TransactionListScreen.kt`

- [ ] **6.1** Update tag filter display
  - Format tags in filter dropdown
  - Format selected tag chips/labels

- [ ] **6.2** Write tests for filter formatting
  - Test: Available tags show camel case in filter
  - Test: Selected tags show camel case
  - Test: Filtering logic unchanged (still case-insensitive)

- [ ] **6.3** Apply TagFormatter to filter UI
  ```kotlin
  val formattedAvailableTags = TagFormatter.formatTags(uiState.availableTags)

  // Display in filter dropdown
  TagFilterDropdown(
      tags = formattedAvailableTags,
      onTagSelected = { formattedTag ->
          // Find original tag for filtering
          val originalTag = uiState.availableTags[formattedAvailableTags.indexOf(formattedTag)]
          viewModel.toggleTagFilter(originalTag)
      }
  )
  ```

- [ ] **6.4** Manual testing
  - Open transaction list with tag filter
  - Verify available tags show camel case
  - Select tag filter
  - Verify filtering works correctly
  - Verify selected tags display formatted

---

### Phase 7: ViewModel Updates

**File**: `app/src/main/java/com/axeven/profiteerapp/viewmodel/TransactionViewModel.kt`

- [ ] **7.1** Review ViewModel tag handling
  - **NO CHANGES** to `availableTags` processing
  - Tags remain lowercase in ViewModel state
  - Formatting applied only in UI layer

- [ ] **7.2** Document display formatting pattern
  - Add KDoc comment to `availableTags` field:
    ```kotlin
    /**
     * All unique tags from user's transactions, normalized and sorted.
     *
     * Tags are stored in lowercase for consistent filtering and matching.
     * UI layer should apply TagFormatter.formatTags() for display.
     *
     * @see TagNormalizer for storage normalization
     * @see TagFormatter for display formatting
     */
    val availableTags: List<String> = emptyList()
    ```

- [ ] **7.3** Verify no data layer changes
  - Confirm no formatting applied in ViewModel
  - Confirm no changes to repository calls
  - Formatting is pure UI concern

---

### Phase 8: UI State Updates

**File**: `app/src/main/java/com/axeven/profiteerapp/data/ui/CreateTransactionUiState.kt`
**File**: `app/src/main/java/com/axeven/profiteerapp/data/ui/EditTransactionUiState.kt`

- [ ] **8.1** Review UI state tag handling
  - **NO CHANGES** to normalization logic
  - `tags` field remains comma-separated lowercase string
  - Formatting applied only when rendering suggestions

- [ ] **8.2** Add documentation notes
  - Document that `tags` field stores normalized (lowercase) values
  - Note that UI should format for display only
  - Reference TagFormatter in comments

- [ ] **8.3** Verify tests pass
  ```bash
  ./gradlew testDebugUnitTest --tests "CreateTransactionUiStateTest"
  ./gradlew testDebugUnitTest --tests "EditTransactionUiStateTest"
  ```

---

### Phase 9: Comprehensive Testing

- [ ] **9.1** Run all unit tests
  ```bash
  ./gradlew testDebugUnitTest
  ```
  - All tests must pass ✅
  - No regressions in existing functionality

- [ ] **9.2** Run lint checks
  ```bash
  ./gradlew lintDebug
  ```
  - Fix any warnings or errors
  - Ensure code quality standards met

- [ ] **9.3** Integration testing scenarios
  - [ ] Create new transaction with tags → Verify display formatted, storage lowercase
  - [ ] Edit existing transaction tags → Verify formatting preserved
  - [ ] Filter transactions by tag → Verify case-insensitive filtering works
  - [ ] View transaction list → Verify all tags display formatted
  - [ ] Autocomplete suggestions → Verify formatted in dropdown

- [ ] **9.4** Edge case testing
  - [ ] Tag with single character → Displays capitalized
  - [ ] Tag with numbers → Displays correctly ("food2024" → "Food2024")
  - [ ] Tag with hyphens → Each word capitalized ("food-related" → "Food-Related")
  - [ ] Empty tag list → Shows "Untagged"
  - [ ] Special characters in tags → Preserved correctly

- [ ] **9.5** Cross-screen consistency check
  - [ ] HomeScreen transaction items
  - [ ] CreateTransactionScreen autocomplete
  - [ ] EditTransactionScreen autocomplete
  - [ ] TransactionListScreen filter dropdown
  - [ ] All locations show consistent camel case formatting

---

### Phase 10: Documentation Updates

- [ ] **10.1** Update `CLAUDE.md`
  - Add section under "Tag Normalization (Implemented 2025-10-19)"
  - Document camel case display pattern:
    ```markdown
    ### Tag Display Formatting (Implemented 2025-10-20)

    **All tags are displayed in camel case for improved readability:**

    - **Storage Format**: Lowercase (e.g., "food", "travel", "grocery shopping")
    - **Display Format**: Camel case (e.g., "Food", "Travel", "Grocery Shopping")
    - **Formatting Utility**: `TagFormatter.formatTags()` in UI layer only
    - **No Data Migration**: Storage unchanged, formatting applied on-the-fly

    #### Implementation Details
    - **Utility Class**: `TagFormatter` in `utils/TagFormatter.kt`
    - **Applied At**: All UI composables that display tags
    - **Not Applied**: Storage, normalization, filtering (remains case-insensitive)
    ```

- [ ] **10.2** Update this plan document
  - Mark all completed tasks ✅
  - Update status to "✅ Complete"
  - Add "Completed" date
  - Document any deviations from plan

- [ ] **10.3** Update `README.md` if applicable
  - Add note about tag display formatting
  - Mention TagFormatter utility in features section

- [ ] **10.4** Create inline code documentation
  - Ensure all new functions have comprehensive KDoc
  - Add usage examples in comments
  - Reference related utilities (TagNormalizer)

---

## Testing Strategy (TDD Approach)

### Test-First Development
1. **Write tests first** for each function before implementation
2. **Run tests** to confirm they fail (Red phase)
3. **Implement** minimum code to make tests pass (Green phase)
4. **Refactor** for clarity and efficiency (Refactor phase)
5. **Repeat** for next function

### Test Coverage Requirements
- **Unit Tests**: 100% coverage for `TagFormatter` utility
- **Edge Cases**: All boundary conditions tested
- **Integration**: UI tests verify formatting applied correctly
- **Regression**: Existing tests continue to pass

### Test Files
- `TagFormatterTest.kt` - Core formatting logic (NEW)
- `HomeScreenTest.kt` - Tag display in transaction items
- `CreateTransactionScreenTest.kt` - Autocomplete formatting
- `EditTransactionScreenTest.kt` - Edit mode formatting
- `TransactionListScreenTest.kt` - Filter formatting

---

## Implementation Notes

### Key Design Decisions

#### 1. **Display-Only Formatting**
- Tags stored as lowercase for consistency
- Formatting applied only at UI layer
- Maintains case-insensitive matching and filtering

#### 2. **No Data Migration**
- No changes to database structure
- No migration scripts needed
- Zero risk to existing data

#### 3. **Separation of Concerns**
- `TagNormalizer`: Handles storage normalization (lowercase)
- `TagFormatter`: Handles display formatting (camel case)
- Clear separation between storage and presentation

#### 4. **Consistent User Experience**
- All displayed tags use same formatting
- Autocomplete suggestions formatted
- Filter options formatted
- Transaction list formatted

### Performance Considerations
- Formatting is O(n) operation (single pass per tag)
- Applied on-the-fly during rendering
- No caching needed (formatting is fast)
- No impact on database queries

### Backward Compatibility
- ✅ No breaking changes to data model
- ✅ Existing tags continue to work
- ✅ No API changes to repositories
- ✅ UI layer change only

---

## Validation Criteria

### Success Metrics
- [ ] All unit tests pass (100% coverage on TagFormatter)
- [ ] No lint errors or warnings
- [ ] All existing tests pass (no regressions)
- [ ] Tags display in camel case across all screens
- [ ] Tag storage remains lowercase (verified in Firestore)
- [ ] Case-insensitive filtering continues to work
- [ ] Autocomplete suggestions formatted correctly

### User Acceptance
- [ ] Tags are visually appealing and easy to read
- [ ] No change in tag input behavior (user can type any case)
- [ ] No loss of functionality (filtering, autocomplete work as before)
- [ ] Performance is identical (no noticeable slowdown)

---

## Rollback Plan

If issues are discovered post-implementation:

1. **Revert TagFormatter application** in UI files
   - Remove `TagFormatter.formatTags()` calls
   - Restore direct `transaction.tags` display

2. **Keep TagFormatter utility** for future use
   - Tests remain valuable
   - Utility can be refined and re-applied

3. **No data changes** means rollback is safe
   - Database unaffected
   - No migration to undo

---

## References

### Related Documentation
- [Tag Normalization Implementation](../plans/2025-10-19-tag-improvement.md)
- [State Management Guidelines](../STATE_MANAGEMENT_GUIDELINES.md)
- [Testing Requirements](../../CLAUDE.md#testing-requirements)

### Related Files
- `utils/TagNormalizer.kt` - Storage normalization
- `utils/TagFormatter.kt` - Display formatting (NEW)
- `data/model/Transaction.kt` - Tag data model
- `viewmodel/TransactionViewModel.kt` - Tag aggregation
- UI files with tag display (HomeScreen, CreateTransaction, etc.)

### Code Review Checklist
- [ ] All tests written before implementation (TDD)
- [ ] KDoc documentation complete and accurate
- [ ] No changes to data layer or storage
- [ ] Formatting applied consistently across all UI
- [ ] Edge cases handled properly
- [ ] Performance impact verified as negligible
- [ ] Code follows project style guidelines

---

## Progress Tracking

### Phase Completion Status
- [x] Phase 1: Test-Driven Development Setup (7/7 tasks) ✅
- [x] Phase 2: Core Implementation (5/5 tasks) ✅
- [x] Phase 3: UI Integration - HomeScreen (4/4 tasks) ✅
- [ ] Phase 4: UI Integration - CreateTransactionScreen (0/4 tasks)
- [ ] Phase 5: UI Integration - EditTransactionScreen (0/4 tasks)
- [ ] Phase 6: UI Integration - TransactionListScreen (0/4 tasks)
- [ ] Phase 7: ViewModel Updates (0/3 tasks)
- [ ] Phase 8: UI State Updates (0/3 tasks)
- [ ] Phase 9: Comprehensive Testing (0/5 tasks)
- [ ] Phase 10: Documentation Updates (0/4 tasks)

### Overall Progress: 16/42 tasks completed (38.1%)

**Estimated Time**: ~4 hours
**Start Date**: 2025-10-20
**Target Completion**: TBD
**Actual Completion**: N/A

---

## Notes

- Implementation follows TDD approach strictly (tests before code)
- Zero risk to existing data (display-only changes)
- Maintains separation of concerns (storage vs. presentation)
- Comprehensive test coverage ensures reliability
- Documentation updates ensure future maintainability

