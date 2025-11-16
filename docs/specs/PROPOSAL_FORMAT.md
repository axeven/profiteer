# Inline Proposal Format Guide

This document explains how to propose changes to specifications using inline proposals.

---

## Why Inline Proposals?

**Benefits**:
- Context preserved - see current and proposed behavior side-by-side
- No duplicate files to maintain
- Easier to review what's changing
- Single source of truth remains in one file
- Simpler workflow for both AI and humans

---

## Format Convention

### Standard Markers

Use these markdown markers to distinguish current vs. proposed behavior:

```markdown
**Current Behavior:**
[Description of how it currently works]

**🔄 Proposed Change:**
[Description of proposed changes]
- **Motivation**: Why this change is needed
- **Impact**: What will be affected
- **Breaking Changes**: Any compatibility issues (if applicable)

**Implementation Plan**: [Link to plan](../../plans/YYYY-MM-DD-feature-name.md)
```

### Alternative Markers (Choose What Fits Best)

You can also use:
- `**🔄 Proposed:**` - Short form
- `**Proposed Behavior:**` - Explicit form
- `**Planned Change:**` - For future changes

**Important**: Be consistent within a single spec file.

---

## Example 1: Feature Modification

### Tag System (Before Implementation)

```markdown
## Tag Storage

**Current Behavior:**
Tags are stored in mixed case as user enters them. This leads to duplicate tags
with different cases ("Food", "food", "FOOD").

**🔄 Proposed Change:**
Tags should be normalized to lowercase for consistency.

- **Motivation**: Prevent duplicate tags with different cases and ensure
  consistent matching across the application
- **Impact**:
  - All tag storage and matching logic
  - Tag autocomplete system
  - Transaction filtering
  - Existing data needs migration
- **Breaking Changes**: Existing tags will be migrated to lowercase

**Implementation Plan**: [2025-10-19-tag-improvement.md](../../plans/2025-10-19-tag-improvement.md)

### Tag Display

Tags will be displayed in camel case for better readability while maintaining
lowercase storage format.
```

### Tag System (After Implementation)

```markdown
## Tag Storage

Tags are normalized to lowercase for consistency. This prevents duplicate tags
with different cases and ensures consistent matching across the application.

### Tag Display

Tags are displayed in camel case for better readability while maintaining
lowercase storage format.
```

---

## Example 2: New Feature Addition

### Before Implementation

```markdown
## Transaction Filtering

**Current Behavior:**
Transaction list displays all transactions for current month with basic date range filtering.

**🔄 Proposed Change:**
Add advanced filtering capabilities including wallet filter, tag filter, and amount range.

- **Motivation**: Users need to find specific transactions quickly without scrolling
- **Impact**:
  - Transaction list screen UI
  - TransactionRepository query methods
  - TransactionViewModel filtering logic
  - Firestore composite indexes
- **Breaking Changes**: None - additive feature

**Implementation Plan**: [2025-XX-XX-advanced-transaction-filtering.md](../../plans/2025-XX-XX-advanced-transaction-filtering.md)

### Filter Options

**🔄 Proposed:**
- Wallet filter (single or multiple wallets)
- Tag filter (AND/OR logic for multiple tags)
- Amount range (min/max)
- Transaction type (Income/Expense/Transfer)
- Combined filters (all filters work together)
```

### After Implementation

```markdown
## Transaction Filtering

Transaction list supports advanced filtering capabilities including:

- **Wallet Filter**: Filter by single or multiple wallets
- **Tag Filter**: AND/OR logic for multiple tags
- **Amount Range**: Minimum and maximum amount filtering
- **Transaction Type**: Filter by Income/Expense/Transfer
- **Date Range**: Custom date range selection
- **Combined Filters**: All filters work together

All filters persist when navigating away and returning to the transaction list.
```

---

## Example 3: Technical Change

### Before Implementation

```markdown
## Error Handling

**Current Behavior:**
Repositories inject `SharedErrorViewModel` to display errors to users.

**🔄 Proposed Change:**
Repositories should use domain error types instead of depending on UI layer.

- **Motivation**: Repository layer should not depend on ViewModel layer (violates
  clean architecture principles)
- **Impact**:
  - All Repository classes
  - All ViewModel error handling
  - Error handling utilities
- **Breaking Changes**: All repository error handling needs refactoring

**Implementation Plan**: [2025-10-17-repository-layer-mixing-concerns.md](../../plans/2025-10-17-repository-layer-mixing-concerns.md)

### Error Type Hierarchy

**🔄 Proposed:**
Create `RepositoryError` sealed class with subtypes:
- `FirestoreListener` - Real-time listener errors
- `FirestoreCrud` - CRUD operation errors
- `NetworkError` - Connectivity issues
- `AuthenticationError` - Auth/permission errors
```

### After Implementation

```markdown
## Error Handling

Repositories use domain error types (`RepositoryError`) instead of depending on the UI layer.

### Error Type Hierarchy

`RepositoryError` sealed class with subtypes:
- `FirestoreListener` - Real-time listener errors
- `FirestoreCrud` - CRUD operation errors
- `NetworkError` - Connectivity issues
- `AuthenticationError` - Auth/permission errors
- `DataValidationError` - Parsing failures
- `ResourceNotFound` - Missing resources
- `CompositeError` - Multi-query aggregation
- `UnknownError` - Catch-all

ViewModels catch and handle these using utility functions from `ErrorHandlingUtils.kt`.
```

---

## Workflow

### 1. Propose Change

Edit spec file directly and add inline proposed changes:
1. Open the relevant spec file
2. Find the section to modify
3. Add `**Current Behavior:**` if not already clear
4. Add `**🔄 Proposed Change:**` with details
5. Link to implementation plan (create plan file first if needed)
6. Commit the spec with proposal

### 2. Create Implementation Plan

Create TDD checklist plan in `docs/plans/`:
1. Reference the spec file with proposed changes
2. Break down into test-driven phases
3. Include success criteria and rollback plan
4. Link back to spec file

### 3. Implement

Follow the plan:
1. Execute tests and implementation
2. Verify all checkboxes are checked
3. Update plan status to 🟢 Completed

### 4. Finalize Spec

Update spec file after implementation:
1. Remove `**Current Behavior:**` section
2. Remove `**🔄 Proposed Change:**` section
3. Update content with the new implemented behavior
4. Update "Last Updated" date
5. Update "Status" field if needed
6. Remove implementation plan link (or move to "Implementation History")
7. Commit the updated spec

---

## Best Practices

### DO:
- ✅ Keep proposals concise and focused
- ✅ Clearly state motivation and impact
- ✅ Link to implementation plan
- ✅ Update spec immediately after implementation
- ✅ Use consistent markers within a file

### DON'T:
- ❌ Leave proposals in spec files indefinitely
- ❌ Mix multiple unrelated proposals in one section
- ❌ Forget to update the spec after implementation
- ❌ Use different marker formats within same file
- ❌ Propose changes without creating implementation plan

---

## Handling Multiple Proposals

If a spec has multiple proposed changes:

```markdown
## Feature Section 1

**Current Behavior:**
[Current implementation]

**🔄 Proposed Change #1:**
[Proposal details]
**Implementation Plan**: [link-to-plan-1.md]

---

## Feature Section 2

**Current Behavior:**
[Current implementation]

**🔄 Proposed Change #2:**
[Proposal details]
**Implementation Plan**: [link-to-plan-2.md]
```

**Note**: Try to implement proposals separately rather than batching unrelated changes.

---

## Checking for Pending Proposals

To find specs with pending proposals:

```bash
# Search for proposal markers
grep -r "🔄 Proposed" docs/specs/

# Or search for "Proposed Change"
grep -r "Proposed Change:" docs/specs/
```

Review these monthly to ensure proposals are implemented or removed if no longer relevant.

---

## For AI Agents

When proposing changes:
1. Read the current spec thoroughly
2. Identify the section to modify
3. Add inline proposal using standard format
4. Create implementation plan in `docs/plans/`
5. After implementation, update spec to remove proposal markers
6. Verify "Last Updated" and "Status" fields are current

When implementing:
1. Read spec with proposed changes
2. Follow implementation plan
3. Execute TDD approach
4. Update spec after completion
5. Verify no orphaned proposal sections remain

---

**Last Updated**: 2025-11-16
