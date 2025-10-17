package com.axeven.profiteerapp.data.constants

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Test suite for UI-related constants.
 * Verifies configuration values for tag autocomplete and UI suggestions.
 */
class UIConstantsTest {

    @Test
    fun `TAG_AUTOCOMPLETE_MIN_CHARS should equal 3`() {
        assertEquals(3, UIConstants.TAG_AUTOCOMPLETE_MIN_CHARS)
    }

    @Test
    fun `TAG_AUTOCOMPLETE_MIN_CHARS should be positive`() {
        assert(UIConstants.TAG_AUTOCOMPLETE_MIN_CHARS > 0) {
            "Minimum characters must be positive"
        }
    }

    @Test
    fun `TAG_SUGGESTION_LIMIT should equal 5`() {
        assertEquals(5, UIConstants.TAG_SUGGESTION_LIMIT)
    }

    @Test
    fun `TAG_SUGGESTION_LIMIT should be positive`() {
        assert(UIConstants.TAG_SUGGESTION_LIMIT > 0) {
            "Tag suggestion limit must be positive"
        }
    }

    @Test
    fun `INITIAL_EXPANDED_GROUPS should equal 3`() {
        assertEquals(3, UIConstants.INITIAL_EXPANDED_GROUPS)
    }

    @Test
    fun `INITIAL_EXPANDED_GROUPS should be positive`() {
        assert(UIConstants.INITIAL_EXPANDED_GROUPS > 0) {
            "Initial expanded groups must be positive"
        }
    }
}
