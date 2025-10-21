package com.axeven.profiteerapp.utils

/**
 * Utility object for formatting tags for display in the UI.
 *
 * This formatter handles display-only formatting of tags, converting them from
 * normalized lowercase storage format to user-friendly camel case format.
 *
 * ## Purpose
 * - **Display Formatting Only**: Never used for storage or filtering
 * - **Storage Format**: Tags remain lowercase in database (e.g., "food", "travel")
 * - **Display Format**: Tags shown in camel case (e.g., "Food", "Travel")
 * - **No Data Migration**: Applied on-the-fly during UI rendering
 *
 * ## Relationship with TagNormalizer
 * - [TagNormalizer] handles storage normalization (converts input to lowercase)
 * - [TagFormatter] handles display formatting (converts lowercase to camel case)
 * - These utilities work together: Normalize for storage, Format for display
 *
 * ## Usage Examples
 * ```kotlin
 * // Single tag formatting
 * TagFormatter.formatTag("food") // Returns "Food"
 * TagFormatter.formatTag("grocery shopping") // Returns "Grocery Shopping"
 * TagFormatter.formatTag("TRAVEL") // Returns "Travel"
 *
 * // List formatting
 * val tags = listOf("food", "travel", "grocery shopping")
 * val formatted = TagFormatter.formatTags(tags)
 * // Returns ["Food", "Travel", "Grocery Shopping"]
 *
 * // In UI composables
 * val displayTags = TagFormatter.formatTags(transaction.tags)
 * Text(text = displayTags.joinToString(", "))
 * ```
 *
 * ## Formatting Rules
 * 1. **Word Capitalization**: First letter of each word capitalized, rest lowercase
 * 2. **Whitespace Normalization**: Multiple spaces collapsed to single space
 * 3. **Trimming**: Leading/trailing whitespace removed
 * 4. **Hyphenated Words**: Each hyphen-separated part capitalized (e.g., "food-related" → "Food-Related")
 * 5. **Special Characters**: Preserved as-is in output
 * 6. **Empty Handling**: Blank input returns empty string
 *
 * @see TagNormalizer for storage normalization rules
 */
object TagFormatter {

    /**
     * Formats a single tag to camel case for display.
     *
     * Converts a normalized tag (lowercase) to a user-friendly camel case format
     * where the first letter of each word is capitalized.
     *
     * ## Algorithm
     * 1. Trim leading/trailing whitespace
     * 2. Return empty string if blank
     * 3. Split on spaces (handling multiple consecutive spaces)
     * 4. Capitalize first letter of each word
     * 5. Lowercase remaining letters
     * 6. Join words with single space
     * 7. Handle hyphenated words (capitalize each part)
     *
     * ## Examples
     * ```kotlin
     * formatTag("food")                  // "Food"
     * formatTag("grocery shopping")      // "Grocery Shopping"
     * formatTag("TRAVEL")                // "Travel"
     * formatTag("food-related")          // "Food-Related"
     * formatTag("  food  ")              // "Food"
     * formatTag("grocery  shopping")     // "Grocery Shopping" (multiple spaces normalized)
     * formatTag("")                      // ""
     * formatTag("   ")                   // ""
     * ```
     *
     * @param tag The tag to format (typically from database in lowercase)
     * @return The formatted tag in camel case, or empty string if input is blank
     */
    fun formatTag(tag: String): String {
        val trimmed = tag.trim()
        if (trimmed.isBlank()) return ""

        return trimmed
            .split(Regex("\\s+")) // Split on one or more whitespace characters
            .joinToString(" ") { word ->
                formatWord(word)
            }
    }

    /**
     * Formats a word, handling hyphenated words by capitalizing each part.
     *
     * @param word The word to format
     * @return The formatted word with proper capitalization
     */
    private fun formatWord(word: String): String {
        if (word.isEmpty()) return word

        // Handle hyphenated words (e.g., "food-related" -> "Food-Related")
        if (word.contains('-')) {
            return word.split('-')
                .joinToString("-") { part ->
                    capitalizeFirstLetter(part)
                }
        }

        return capitalizeFirstLetter(word)
    }

    /**
     * Capitalizes the first letter of a string and lowercases the rest.
     *
     * @param str The string to capitalize
     * @return The capitalized string
     */
    private fun capitalizeFirstLetter(str: String): String {
        if (str.isEmpty()) return str
        return str.first().uppercaseChar() + str.drop(1).lowercase()
    }

    /**
     * Formats a list of tags to camel case for display.
     *
     * Applies [formatTag] to each tag in the list and filters out any blank results.
     *
     * ## Examples
     * ```kotlin
     * formatTags(listOf("food", "travel"))
     * // Returns ["Food", "Travel"]
     *
     * formatTags(listOf("grocery shopping", "FOOD", "  travel  "))
     * // Returns ["Grocery Shopping", "Food", "Travel"]
     *
     * formatTags(emptyList())
     * // Returns []
     *
     * formatTags(listOf("", "  ", "food"))
     * // Returns ["Food"] (blanks filtered out)
     * ```
     *
     * @param tags The list of tags to format (typically from database in lowercase)
     * @return A new list with all tags formatted in camel case, blanks removed
     */
    fun formatTags(tags: List<String>): List<String> {
        return tags
            .map { formatTag(it) }
            .filter { it.isNotBlank() }
    }
}
