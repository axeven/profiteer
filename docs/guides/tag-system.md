# Tag System Documentation

This document provides comprehensive documentation for the tag system in the Profiteer Android app, including normalization, formatting, and display patterns.

## Overview

The tag system allows users to categorize transactions with multiple tags for better organization and reporting. Tags are:

- **Normalized** during input (lowercase, trimmed, deduplicated)
- **Stored** in lowercase format in Firestore
- **Displayed** in camel case for readability

## Architecture Pattern

```
User Input          →    Normalization       →    Storage (Firestore)    →    Display (UI)
"Food, FOOD, food"  →    ["food"]            →    ["food"]               →    ["Food"]
     ↑                         ↑                         ↑                        ↑
  Raw input            TagNormalizer           Firestore document         TagFormatter
```

## Tag Normalization

**Implemented**: 2025-10-19
**Utility Class**: `TagNormalizer` (`app/src/main/java/com/axeven/profiteerapp/utils/TagNormalizer.kt`)

### Normalization Rules

1. **Case-Insensitive**: All tags converted to lowercase
   - `"Food"` → `"food"`
   - `"TRAVEL"` → `"travel"`

2. **Whitespace Trimming**: Leading/trailing whitespace removed
   - `" food "` → `"food"`
   - `"  travel  "` → `"travel"`

3. **Duplicate Removal**: Case-insensitive duplicates removed
   - `["food", "Food", "FOOD"]` → `["food"]`

4. **Reserved Keyword Filtering**: "Untagged" keyword filtered out (case-insensitive)
   - `["food", "Untagged", "travel"]` → `["food", "travel"]`

5. **Blank Tag Filtering**: Empty and whitespace-only tags removed
   - `["food", "", "  ", "travel"]` → `["food", "travel"]`

### Usage

```kotlin
// Normalize a list of tags
val normalizedTags = TagNormalizer.normalizeTags(listOf("Food", "FOOD", "travel"))
// Result: ["food", "travel"]

// Normalize a single tag
val normalizedTag = TagNormalizer.normalizeTag("  Food  ")
// Result: "food"
```

### Applied At

- **Transaction Creation**: `CreateTransactionUiState`
- **Transaction Editing**: `EditTransactionUiState`
- **Tag Loading**: `fromExistingTransaction`
- **Autocomplete Suggestions**: `TransactionViewModel`
- **Data Migration**: `TagMigration`

### User Experience

- Tags normalized automatically on input
- Autocomplete is case-insensitive (typing "foo" suggests "food")
- No duplicate tags in suggestion lists
- Consistent tag storage across all transactions

## Tag Display Formatting

**Implemented**: 2025-10-20
**Utility Class**: `TagFormatter` (`app/src/main/java/com/axeven/profiteerapp/utils/TagFormatter.kt`)

### Format Specification

- **Storage Format**: Lowercase (e.g., "food", "travel", "grocery shopping")
- **Display Format**: Camel case (e.g., "Food", "Travel", "Grocery Shopping")
- **Formatting Layer**: UI layer only (storage unchanged)
- **No Data Migration**: Formatting applied on-the-fly

### Formatting Rules

1. **Single Words**: First letter capitalized
   - `"food"` → `"Food"`
   - `"travel"` → `"Travel"`

2. **Multi-Word Tags**: Each word capitalized
   - `"grocery shopping"` → `"Grocery Shopping"`
   - `"work related"` → `"Work Related"`

3. **Hyphenated Words**: Each segment capitalized
   - `"food-related"` → `"Food-Related"`
   - `"work-from-home"` → `"Work-From-Home"`

4. **Special Characters**: Preserved in position
   - `"food&drink"` → `"Food&drink"`

5. **Numbers**: Preserved in position
   - `"groceries2024"` → `"Groceries2024"`

6. **Empty Tags**: Display "Untagged" label
   - `[]` → `"Untagged"` label shown

### Usage

```kotlin
// Format a list of tags for display
val formattedTags = TagFormatter.formatTags(listOf("food", "grocery shopping"))
// Result: ["Food", "Grocery Shopping"]

// Format a single tag
val formattedTag = TagFormatter.formatTag("grocery shopping")
// Result: "Grocery Shopping"
```

### Applied At (UI Layer Only)

- **HomeScreen**: Transaction item tags (`HomeScreen.kt`)
- **CreateTransactionScreen**: Autocomplete suggestions (`CreateTransactionScreen.kt`)
- **EditTransactionScreen**: Autocomplete via `TagInputField` component
- **TransactionListScreen**: Filter dropdown (`TransactionListScreen.kt`)

### Not Applied At

- Storage layer (tags remain lowercase in Firestore)
- Normalization logic (operates on lowercase)
- Filtering and matching (case-insensitive)

## Tag Autocomplete

### Autocomplete Behavior

- **Trigger**: After 3+ characters typed
- **Source**: Existing tags from user's transactions
- **Matching**: Case-insensitive substring matching
- **Display**: Camel case formatted
- **Selection**: Adds normalized tag to transaction

### Implementation

```kotlin
// In TransactionViewModel
fun loadTagSuggestions(userId: String) {
    viewModelScope.launch {
        transactionRepository.getAllTransactions(userId)
            .collect { transactions ->
                val allTags = transactions
                    .flatMap { it.tags }
                    .distinct()
                    .sorted()

                _tagSuggestions.value = allTags
            }
    }
}
```

### UI Usage

```kotlin
// In CreateTransactionScreen
TagInputField(
    tags = uiState.tags,
    suggestions = TagFormatter.formatTags(viewModelUiState.tagSuggestions),
    onTagsChange = { newTags ->
        onUpdateState(uiState.updateTags(newTags))
    }
)
```

## Data Migration

**Migration Utility**: `TagMigration` (`app/src/main/java/com/axeven/profiteerapp/data/migration/TagMigration.kt`)

### Migration Process

1. **Trigger**: Can be run via `TagMigration.migrateTransactionTags(userId)`
2. **Status Flag**: `UserPreferences.tagsMigrationCompleted` tracks state
3. **Idempotent**: Safe to run multiple times, skips already-normalized tags
4. **Batch Processing**: Updates transactions in batches to avoid timeout

### When to Run

- After upgrading from pre-normalization version
- When inconsistent tag data is detected
- On user request (debugging/cleanup)

### Example

```kotlin
// In SettingsViewModel
fun migrateTagData() {
    viewModelScope.launch {
        try {
            TagMigration.migrateTransactionTags(userId)
            userPreferences.update { it.copy(tagsMigrationCompleted = true) }
        } catch (e: Exception) {
            logger.e("SettingsViewModel", "Tag migration failed", e)
        }
    }
}
```

## Tag Filtering

### Filter Behavior

- **Case-Insensitive**: Matches regardless of case
- **Exact Match**: Tag must match exactly (after normalization)
- **Multiple Tags**: Transaction must have ALL selected tags (AND logic)

### Usage

```kotlin
// Filter transactions by tags
val filteredTransactions = allTransactions.filter { transaction ->
    selectedTags.all { tag ->
        transaction.tags.map { it.lowercase() }.contains(tag.lowercase())
    }
}
```

## Testing

### Normalization Tests

**File**: `TagNormalizerTest.kt`
**Coverage**: 176 comprehensive tests

Test scenarios:
- Case normalization
- Whitespace trimming
- Duplicate removal
- Reserved keyword filtering
- Blank tag filtering
- Edge cases (null, empty, special characters)

### Formatting Tests

**File**: `TagFormatterTest.kt`
**Coverage**: 68 unit tests

Test scenarios:
- Single word capitalization
- Multi-word capitalization
- Hyphenated words
- Special characters
- Numbers
- Edge cases (empty, null)

### Screen Integration Tests

**Files**:
- `HomeScreenTest.kt` (17 tests)
- `CreateTransactionScreenTagFormattingTest.kt` (24 tests)
- `EditTransactionScreenTagFormattingTest.kt` (24 tests)
- `TransactionListScreenTagFormattingTest.kt` (27 tests)

Total: **160+ comprehensive tests**

## Best Practices

### 1. Always Normalize Before Storage

```kotlin
// ✅ CORRECT
val normalizedTags = TagNormalizer.normalizeTags(userInputTags)
transaction.copy(tags = normalizedTags)

// ❌ WRONG
transaction.copy(tags = userInputTags)
```

### 2. Always Format for Display

```kotlin
// ✅ CORRECT
Text(text = TagFormatter.formatTag(tag))

// ❌ WRONG
Text(text = tag)  // Shows lowercase "food" instead of "Food"
```

### 3. Use Case-Insensitive Matching

```kotlin
// ✅ CORRECT
tags.any { it.lowercase() == searchTerm.lowercase() }

// ❌ WRONG
tags.any { it == searchTerm }  // Case-sensitive
```

### 4. Preserve Normalization in State Updates

```kotlin
// ✅ CORRECT
fun updateTags(newTags: List<String>): CreateTransactionUiState {
    return copy(tags = TagNormalizer.normalizeTags(newTags))
}

// ❌ WRONG
fun updateTags(newTags: List<String>): CreateTransactionUiState {
    return copy(tags = newTags)  // No normalization
}
```

## Common Patterns

### Pattern 1: Tag Input Field

```kotlin
@Composable
fun TagInputField(
    tags: List<String>,
    suggestions: List<String>,
    onTagsChange: (List<String>) -> Unit
) {
    // Display formatted tags
    val displayTags = TagFormatter.formatTags(tags)

    // Display formatted suggestions
    val displaySuggestions = TagFormatter.formatTags(suggestions)

    // On selection, normalize before passing to parent
    val onSelect = { selectedTag: String ->
        val normalized = TagNormalizer.normalizeTag(selectedTag)
        onTagsChange(tags + normalized)
    }
}
```

### Pattern 2: Tag Filter Dropdown

```kotlin
@Composable
fun TagFilterDropdown(
    availableTags: List<String>,
    selectedTags: List<String>,
    onTagsSelected: (List<String>) -> Unit
) {
    // Display formatted tags
    val formattedTags = TagFormatter.formatTags(availableTags)

    DropdownMenu(items = formattedTags) { formattedTag ->
        // Find original tag (lowercase)
        val originalTag = availableTags.find {
            TagFormatter.formatTag(it) == formattedTag
        }

        originalTag?.let { tag ->
            onTagsSelected(selectedTags + tag)
        }
    }
}
```

### Pattern 3: Tag Migration Check

```kotlin
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val preferences by viewModel.userPreferences.collectAsState()

    if (!preferences.tagsMigrationCompleted) {
        AlertDialog(
            title = "Tag Data Migration",
            text = "We need to update your tag data to the new format.",
            confirmButton = {
                Button(onClick = { viewModel.migrateTagData() }) {
                    Text("Migrate")
                }
            }
        )
    }
}
```

## Troubleshooting

### Issue: Tags not appearing in autocomplete

**Check**:
1. Are tags being normalized before storage?
2. Is `loadTagSuggestions` being called?
3. Are tags formatted before display?

### Issue: Duplicate tags appearing

**Solution**: Ensure normalization is applied:
```kotlin
val normalizedTags = TagNormalizer.normalizeTags(inputTags)
```

### Issue: Tags displaying in lowercase

**Solution**: Apply formatting in UI layer:
```kotlin
TagFormatter.formatTags(tags)
```

### Issue: Case-sensitive tag filtering

**Solution**: Use lowercase comparison:
```kotlin
tags.any { it.lowercase() == searchTerm.lowercase() }
```

## Related Documentation

- [Tag Normalization Implementation](plans/2025-10-19-tag-improvement.md)
- [Tag Formatting Implementation](plans/2025-10-20-camel-case-tags.md)
- [TagNormalizer.kt](../app/src/main/java/com/axeven/profiteerapp/utils/TagNormalizer.kt)
- [TagFormatter.kt](../app/src/main/java/com/axeven/profiteerapp/utils/TagFormatter.kt)
- [TagMigration.kt](../app/src/main/java/com/axeven/profiteerapp/data/migration/TagMigration.kt)

## Summary

| Aspect | Pattern | Location |
|--------|---------|----------|
| **Input** | Normalize tags | `TagNormalizer.normalizeTags()` |
| **Storage** | Lowercase format | Firestore `tags` field |
| **Display** | Camel case format | `TagFormatter.formatTags()` |
| **Autocomplete** | Case-insensitive | `TransactionViewModel` |
| **Filtering** | Case-insensitive | `tag.lowercase() == search.lowercase()` |
| **Migration** | One-time process | `TagMigration.migrateTransactionTags()` |
