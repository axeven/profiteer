# AI Agent Development Guide

This guide explains how AI agents (like Claude Code) should work with the Profiteer codebase to propose changes, plan implementations, and maintain documentation.

---

## Overview

The development workflow follows these phases:

```
1. READ specifications
2. PROPOSE changes inline within spec files
3. CREATE implementation plan (TDD checklist)
4. IMPLEMENT following the plan
5. FINALIZE by updating specs
```

---

## Phase 1: Reading Specifications

### Before Starting Any Task

1. **Check the documentation hub**: Start with [docs/README.md](README.md)
2. **Identify relevant specs**: Find specifications related to your task
3. **Read related docs**: Check domain models, technical specs, and guidelines

### Finding the Right Documentation

| Task Type | Where to Look |
|-----------|---------------|
| Feature change | [specs/features/](specs/features/) |
| Screen modification | [specs/screens/](specs/screens/) |
| Data model change | [specs/domain/](specs/domain/) |
| Architecture update | [specs/architecture/](specs/architecture/) |
| Technical pattern | [specs/technical/](specs/technical/) |
| Implementation guidance | [guides/](guides/) |

### Understanding Current Behavior

Read the spec file to understand:
- Current implementation and behavior
- Business rules and constraints
- Data models and relationships
- Related systems and dependencies
- Any existing proposed changes (look for **🔄 Proposed Change:** markers)

---

## Phase 2: Proposing Changes

### Inline Proposal Format

Open the relevant spec file and add your proposal **inline** using this format:

```markdown
## Feature Section

**Current Behavior:**
[Describe how it currently works - be specific and factual]

**🔄 Proposed Change:**
[Describe the proposed changes - be clear and detailed]

- **Motivation**: Why this change is needed (user benefit, technical debt, bug fix, etc.)
- **Impact**: What systems/components will be affected
  - UI changes
  - Data model changes
  - Repository changes
  - ViewModel changes
  - Breaking changes (if any)
- **Breaking Changes**: Any compatibility issues or migration needs

**Implementation Plan**: [2025-XX-XX-feature-name.md](../plans/2025-XX-XX-feature-name.md)
```

### Proposal Best Practices

**DO:**
- ✅ Be specific about motivation and impact
- ✅ Identify breaking changes explicitly
- ✅ Link to implementation plan
- ✅ Keep proposals focused on one feature/change
- ✅ Update "Last Updated" date in spec metadata

**DON'T:**
- ❌ Mix multiple unrelated proposals in one section
- ❌ Leave proposals without implementation plans
- ❌ Forget to document breaking changes
- ❌ Use vague language like "improve" or "enhance" without specifics

### Example Proposal

```markdown
## Tag Display Formatting

**Current Behavior:**
Tags are displayed in lowercase as stored in database ("food", "travel", "grocery shopping").

**🔄 Proposed Change:**
Tags should be displayed in camel case for better readability while maintaining lowercase storage.

- **Motivation**: Improve UI professionalism and readability without changing data model
- **Impact**:
  - UI layer only (no database changes)
  - All screens displaying tags need formatting applied
  - Transaction list, home screen, transaction forms
  - New utility class `TagFormatter`
- **Breaking Changes**: None - purely display layer change

**Implementation Plan**: [2025-10-20-camel-case-tags.md](../plans/2025-10-20-camel-case-tags.md)
```

For more examples, see [PROPOSAL_FORMAT.md](specs/PROPOSAL_FORMAT.md).

---

## Phase 3: Creating Implementation Plans

### Plan Template

Use the template at [plans/_TEMPLATE.md](plans/_TEMPLATE.md) to create your implementation plan:

```markdown
# [Feature/Change Name]

**Date**: YYYY-MM-DD
**Status**: 🔴 Not Started | 🟡 In Progress | 🟢 Completed
**Objective**: [One-sentence description]

---

## Overview

### Current State
[What exists now]

### Target State
[What we want to achieve]

### Related Specification
- **Spec**: [Feature Name](../specs/category/file.md#section)

---

## Phase 1: [Phase Name]

### Test 1.1: [Test Description]
- [ ] Write test that fails (feature doesn't exist yet)
- [ ] Implement minimum code to make test pass
- [ ] Verify test passes
- [ ] Refactor if needed
- [ ] Verify test still passes

---

## Success Criteria

- [ ] All tests pass
- [ ] No regression in existing functionality
- [ ] Documentation updated
- [ ] Code reviewed (if applicable)

---

## Rollback Plan

1. **Immediate Rollback**: Git revert to commit before changes
2. **Data Migration Rollback** (if applicable): [Steps to restore data]
```

### Test-Driven Development Approach

**CRITICAL**: All plans must follow TDD approach:

1. **Write Failing Test First**: Test describes desired behavior that doesn't exist yet
2. **Implement Minimum Code**: Just enough to make test pass
3. **Verify Test Passes**: Run test suite
4. **Refactor**: Improve code quality without changing behavior
5. **Verify Again**: Ensure refactoring didn't break tests

### Plan Granularity

Break implementation into phases:
- **Phase 1**: Core domain models and business logic
- **Phase 2**: Repository and data layer
- **Phase 3**: ViewModel and state management
- **Phase 4**: UI components and screens
- **Phase 5**: Integration testing and edge cases

Each phase should have **3-8 test steps**. If more, split into multiple phases.

---

## Phase 4: Implementation

### Following the Plan

1. **Mark plan as In Progress**: Update status to 🟡
2. **Work through phases sequentially**: Don't skip ahead
3. **Check off each step**: Update checkboxes as you complete steps
4. **Write tests first**: Always write failing test before implementation
5. **Commit frequently**: Small, focused commits with clear messages
6. **Update plan if needed**: Document deviations from original plan

### Using TodoWrite Tool

Create todo items from the plan:

```kotlin
- [ ] Phase 1: Core domain models (3 tests)
- [ ] Phase 2: Repository layer (4 tests)
- [ ] Phase 3: ViewModel logic (5 tests)
- [ ] Phase 4: UI components (6 tests)
- [ ] Phase 5: Integration tests (4 tests)
```

Mark as in_progress when starting, completed when done.

### Testing Requirements

From CLAUDE.md:
- **All code changes MUST include tests**
- Test both happy path and error scenarios
- Use descriptive test names
- Mock external dependencies
- Test error handling and edge cases
- Run tests before committing
- Ensure tests are deterministic

### Code Quality Checklist

Before committing:
- [ ] All tests pass (`./gradlew test`)
- [ ] No lint errors (`./gradlew lint`)
- [ ] Code follows existing patterns
- [ ] Business rules validated
- [ ] Error handling implemented
- [ ] Logging added where appropriate
- [ ] State management follows consolidated pattern
- [ ] Firebase queries include userId filter (if applicable)
- [ ] Repository errors use domain types (if applicable)

---

## Phase 5: Finalizing Documentation

### After Implementation is Complete

1. **Update the spec file**:
   - Remove `**Current Behavior:**` section
   - Remove `**🔄 Proposed Change:**` section
   - Update content with new implemented behavior
   - Update "Last Updated" date
   - Update "Status" field (e.g., ❌ Planned → ✅ Implemented)
   - Remove implementation plan link (or move to history section)

2. **Mark plan as completed**:
   - Update plan status to 🟢 Completed
   - Document any deviations from original plan
   - Add completion date

3. **Update CHANGELOG.md**:
   - Add entry with date, changes, and link to implementation plan

4. **Update ROADMAP.md** (if applicable):
   - Move item from Planned to appropriate status
   - Or remove if fully implemented

### Example: Before and After

**Before Implementation** (in spec file):
```markdown
## Tag Display

**Current Behavior:**
Tags displayed in lowercase.

**🔄 Proposed Change:**
Display tags in camel case.

- **Motivation**: Better readability
- **Impact**: UI layer only
- **Breaking Changes**: None

**Implementation Plan**: [2025-10-20-camel-case-tags.md](../plans/2025-10-20-camel-case-tags.md)
```

**After Implementation** (in spec file):
```markdown
## Tag Display

Tags are displayed in camel case for better readability (e.g., "Food", "Travel", "Grocery Shopping").

- **Storage Format**: Lowercase in database
- **Display Format**: Camel case in UI using `TagFormatter`
- **Applied In**: All screens displaying tags (HomeScreen, TransactionList, etc.)
```

---

## Common Workflows

### Workflow 1: Bug Fix

1. **Read**: Identify affected spec (usually screens/ or features/)
2. **Propose**: Add inline proposal explaining bug and fix
3. **Plan**: Create plan with test for bug reproduction + fix
4. **Implement**: Write failing test, fix bug, verify test passes
5. **Finalize**: Update spec to reflect correct behavior

### Workflow 2: New Feature

1. **Read**: Multiple specs (domain models, screens, technical patterns)
2. **Propose**: Add proposals in relevant specs (may span multiple files)
3. **Plan**: Create comprehensive plan covering all layers
4. **Implement**: Follow TDD phases from domain → UI
5. **Finalize**: Update all affected specs, add CHANGELOG entry

### Workflow 3: Refactoring

1. **Read**: Architecture and technical specs
2. **Propose**: Explain technical debt and refactoring approach
3. **Plan**: Create plan ensuring zero behavior changes
4. **Implement**: Write characterization tests, refactor, verify
5. **Finalize**: Update technical specs with new patterns

---

## Critical Patterns to Follow

### Firebase Security

**🚨 EVERY Firestore query MUST include userId filter:**

```kotlin
// ✅ CORRECT
collection
  .whereEqualTo("userId", userId)  // FIRST filter
  .whereEqualTo("otherField", value)
  .get()

// ❌ FORBIDDEN
collection
  .whereEqualTo("walletId", walletId)  // Missing userId!
  .get()
```

See [specs/technical/firebase-integration.md](specs/technical/firebase-integration.md) for complete security guidelines.

### Repository Error Handling

**🚨 Repositories NEVER depend on ViewModels:**

```kotlin
// ✅ CORRECT
class MyRepository @Inject constructor(
    private val logger: Logger
) {
    fun getData() = callbackFlow {
        addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(RepositoryError.FirestoreListener(...))
            }
        }
    }
}

// ❌ FORBIDDEN
class MyRepository @Inject constructor(
    private val sharedErrorViewModel: SharedErrorViewModel  // NEVER!
) { ... }
```

See [specs/technical/error-handling.md](specs/technical/error-handling.md) for complete error handling patterns.

### State Management

**All Compose screens must use consolidated state:**

```kotlin
// ✅ CORRECT
data class ScreenUiState(
    val dialogStates: DialogStates = DialogStates(),
    val formData: FormData = FormData()
) {
    val isValid: Boolean
        get() = formData.requiredField.isNotBlank()
}

// ❌ FORBIDDEN
@Composable
fun Screen() {
    var showDialog by remember { mutableStateOf(false) }
    var titleText by remember { mutableStateOf("") }
}
```

See [specs/technical/state-management.md](specs/technical/state-management.md) for complete state patterns.

---

## Troubleshooting

### "Can't find the right spec file"

→ Check [docs/README.md](README.md) navigation tables
→ Use grep: `grep -r "keyword" docs/specs/`
→ Check DOCUMENTATION_MAP.md for tree structure

### "Proposal section getting too long"

→ Split into multiple proposals (one per feature/change)
→ Link related proposals together
→ Keep each proposal focused on single concern

### "Plan phases unclear"

→ Follow domain → data → UI layer structure
→ Each phase should have clear input/output
→ Look at existing plans for examples (docs/plans/)

### "Breaking changes unsure"

→ Check if data model changes (likely breaking)
→ Check if API changes (breaking if parameters removed)
→ Check if behavior changes visible to users (may be breaking)
→ When in doubt, mark as potentially breaking and test thoroughly

---

## Examples

### Complete Example: Tag Formatting Feature

**1. Read**
- Read specs/features/tag-system.md
- Read specs/screens/home-screen.md
- Read specs/screens/transaction-list-screen.md

**2. Propose**
Added to specs/features/tag-system.md:
```markdown
**Current Behavior:**
Tags displayed in lowercase.

**🔄 Proposed Change:**
Display tags in camel case.

- **Motivation**: Better UI readability
- **Impact**: UI layer only, no data changes
- **Breaking Changes**: None

**Implementation Plan**: [2025-10-20-camel-case-tags.md](../plans/2025-10-20-camel-case-tags.md)
```

**3. Plan**
Created plans/2025-10-20-camel-case-tags.md:
```markdown
## Phase 1: TagFormatter Utility
### Test 1.1: Format single word tag
- [ ] Write test: "food" → "Food"
- [ ] Implement TagFormatter.format()
- [ ] Verify test passes

### Test 1.2: Format multi-word tag
- [ ] Write test: "grocery shopping" → "Grocery Shopping"
- [ ] Implement word splitting logic
- [ ] Verify test passes

## Phase 2: Apply to HomeScreen
### Test 2.1: Home screen displays formatted tags
- [ ] Write UI test verifying camel case display
- [ ] Apply TagFormatter in TransactionItem
- [ ] Verify test passes
...
```

**4. Implement**
- Checked off plan items as completed
- Committed after each phase
- Ran tests continuously
- Updated plan status to 🟢

**5. Finalize**
- Updated specs/features/tag-system.md (removed proposal sections)
- Added entry to CHANGELOG.md
- Verified all cross-references

---

## Quick Reference

### Key Files to Know
- [docs/README.md](README.md) - Documentation hub
- [specs/PROPOSAL_FORMAT.md](specs/PROPOSAL_FORMAT.md) - Proposal examples
- [plans/_TEMPLATE.md](plans/_TEMPLATE.md) - Plan template
- [ROADMAP.md](ROADMAP.md) - Future features
- [CHANGELOG.md](CHANGELOG.md) - Historical changes

### Command Cheat Sheet
```bash
# Find specs mentioning keyword
grep -r "transaction" docs/specs/

# Find pending proposals
grep -r "🔄 Proposed" docs/specs/

# Run tests
./gradlew test

# Run lint
./gradlew lint
```

### Status Indicators
- ✅ **Implemented** - Feature complete and in production
- 🚧 **Partial** - Feature partially implemented
- 🔄 **Proposed** - Change proposed, not yet implemented
- ❌ **Planned** - Future feature, not yet designed
- 🔴 **Not Started** - Plan created, implementation pending
- 🟡 **In Progress** - Currently being implemented
- 🟢 **Completed** - Implementation finished

---

**Last Updated**: 2025-11-16

For questions or clarifications, refer to the main documentation hub at [docs/README.md](README.md).
