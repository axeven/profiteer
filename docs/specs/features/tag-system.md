# Tag System

**Status**: ✅ Implemented
**Last Updated**: 2025-11-16
**Implementation Plan**: [2025-10-19-tag-improvement.md](../../plans/2025-10-19-tag-improvement.md)

## Purpose

Unified tagging system that replaces traditional transaction categories, enabling multiple tags per transaction with auto-completion.

## Requirements

### Tag Features
- **Multiple Tags**: Each transaction can have multiple descriptive tags
- **Auto-completion**: Historical tag suggestions after typing 3+ characters
- **Normalization**: Tags stored in lowercase, displayed in camel case
- **Default Behavior**: Transactions without tags receive "Untagged" designation

### Tag Normalization
- All tags converted to lowercase for storage
- Whitespace trimmed
- Case-insensitive duplicates removed
- "Untagged" keyword filtered out
- Blank tags removed

### Tag Display
- Stored: lowercase (e.g., "food", "travel")
- Displayed: camel case (e.g., "Food", "Travel")
- Formatting applied in UI layer only

## Related Specifications

- [Transaction Domain Model](../domain/transactions.md)
- [Tag System Guide](../../guides/tag-system.md) - Implementation details

---

**Last Reviewed**: 2025-11-16
