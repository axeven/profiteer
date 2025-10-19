package com.axeven.profiteerapp.utils

/**
 * Utility object for normalizing transaction tags.
 *
 * Tag Normalization Rules:
 * 1. Trim leading and trailing whitespace (spaces, tabs, newlines)
 * 2. Convert to lowercase for case-insensitive comparison
 * 3. Remove duplicates (case-insensitive)
 * 4. Filter out blank/empty tags
 * 5. Remove "Untagged" reserved keyword (case-insensitive)
 *
 * This ensures consistent tag handling across the application and prevents
 * duplicate tags like "food", "Food", "FOOD" from appearing separately.
 *
 * Example:
 * ```
 * parseTagInput(" Food, food, FOOD , travel ") // Returns: ["food", "travel"]
 * ```
 */
object TagNormalizer {

    /**
     * Reserved tag keyword that should be filtered out.
     */
    private const val RESERVED_UNTAGGED = "untagged"

    /**
     * Normalizes a single tag by trimming whitespace and converting to lowercase.
     *
     * @param tag The tag to normalize
     * @return Normalized tag (trimmed and lowercase)
     *
     * Example:
     * ```
     * normalizeTag("  Food  ") // Returns: "food"
     * normalizeTag("TRAVEL") // Returns: "travel"
     * normalizeTag("   ") // Returns: ""
     * ```
     */
    fun normalizeTag(tag: String): String {
        return tag.trim().lowercase()
    }

    /**
     * Normalizes a list of tags by:
     * - Trimming whitespace from each tag
     * - Converting to lowercase
     * - Removing case-insensitive duplicates (preserves first occurrence)
     * - Filtering out blank tags
     * - Removing "Untagged" reserved keyword
     *
     * @param tags List of tags to normalize
     * @return Normalized list of unique tags
     *
     * Example:
     * ```
     * normalizeTags(listOf("Food", "food", "FOOD", "travel"))
     * // Returns: ["food", "travel"]
     *
     * normalizeTags(listOf("  food  ", "", "travel", "Untagged"))
     * // Returns: ["food", "travel"]
     * ```
     */
    fun normalizeTags(tags: List<String>): List<String> {
        val seen = mutableSetOf<String>()
        return tags
            .map { normalizeTag(it) }
            .filter { normalized ->
                // Filter out blank tags and "untagged" keyword
                if (normalized.isBlank() || normalized == RESERVED_UNTAGGED) {
                    false
                } else {
                    // Add to set to track uniqueness, return true if it's new
                    seen.add(normalized)
                }
            }
    }

    /**
     * Parses comma-separated tag input string into a normalized list of tags.
     *
     * This is the primary function for processing user input from tag text fields.
     *
     * Processing steps:
     * 1. Split on comma delimiter
     * 2. Trim each tag
     * 3. Convert to lowercase
     * 4. Remove duplicates (case-insensitive)
     * 5. Filter out blank tags
     * 6. Remove "Untagged" reserved keyword
     *
     * @param input Comma-separated tag string (e.g., "food, travel, shopping")
     * @return List of normalized, unique tags
     *
     * Example:
     * ```
     * parseTagInput("food, Food, FOOD")
     * // Returns: ["food"]
     *
     * parseTagInput(" Food , travel , Shopping, shopping ")
     * // Returns: ["food", "travel", "shopping"]
     *
     * parseTagInput("food, , travel, , shopping")
     * // Returns: ["food", "travel", "shopping"]
     *
     * parseTagInput("")
     * // Returns: []
     * ```
     */
    fun parseTagInput(input: String): List<String> {
        return normalizeTags(input.split(","))
    }
}
