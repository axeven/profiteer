# Implementation Plans

This directory contains **TDD-style implementation plans** for all features and changes in the Profiteer app.

## Purpose

Implementation plans guide the development process by:
- Breaking down features into test-driven phases
- Providing clear acceptance criteria
- Documenting implementation approach
- Tracking progress through checklists

## Plan Structure

All plans MUST follow this TDD checklist format:

```markdown
# Feature/Change Name

**Date**: YYYY-MM-DD
**Status**: 🔴 Not Started | 🟡 In Progress | 🟢 Completed
**Objective**: Brief description of what this plan achieves

## Overview

### Current State
What exists now (if applicable).

### Target State
What we want to achieve.

### Related Specification
Link to the spec file: [spec name](../specs/category/file.md)

---

## Phase 1: [Phase Name]

### Test 1.1: [Test Description]
- [ ] Step 1
- [ ] Step 2
- [ ] Verification step

### Test 1.2: [Another Test]
- [ ] Implementation steps
- [ ] Verification steps

---

## Success Criteria

- [x] Criterion 1
- [x] Criterion 2

## Rollback Plan

If implementation fails:
1. Steps to rollback changes
2. How to restore previous state
```

## Plan Requirements

Every implementation plan MUST include:

1. **Metadata**
   - Date
   - Status (🔴/🟡/🟢)
   - Objective

2. **Overview**
   - Current state
   - Target state
   - Link to related specification

3. **Test-Driven Phases**
   - Each phase has multiple tests
   - Each test has checklist items
   - Each test includes verification steps

4. **Success Criteria**
   - Clear, measurable criteria
   - Checkboxes for tracking

5. **Rollback Plan**
   - How to undo changes if needed
   - Safety measures

## Plan Lifecycle

1. **Draft** - Plan created, linked from spec
2. **In Progress** - Implementation underway
3. **Completed** - All checkboxes marked
4. **Archived** - Moved to `docs/archived/plans/` after 3 months

## TDD Approach

All plans follow Test-Driven Development:

```markdown
## Phase N: Feature Implementation

### Test N.1: Verify Feature Works
- [ ] Write test that fails (feature doesn't exist yet)
- [ ] Implement minimum code to make test pass
- [ ] Verify test passes
- [ ] Refactor if needed
- [ ] Verify test still passes
```

This ensures:
- All code has test coverage
- Tests are written before implementation
- Incremental progress with verification

## Updating Plans

- **During Implementation**: Check off items as you complete them
- **After Completion**: Update status to 🟢 Completed
- **After 3 Months**: Move to `docs/archived/plans/`
- **If Blocked**: Document blockers in plan, keep status 🟡

## Plan Template

See `_TEMPLATE.md` in this directory for the standard plan template.

## Relationship to Specifications

```
Specification (docs/specs/)
    ↓ (has inline proposed change)
Implementation Plan (docs/plans/)
    ↓ (guides implementation)
Code Changes
    ↓ (after implementation)
Updated Specification (proposal merged)
```

## For AI Agents

When creating implementation plans:
1. Read the specification with proposed changes
2. Create plan file: `YYYY-MM-DD-descriptive-name.md`
3. Follow TDD checklist template
4. Link back to specification
5. Update plan status as you implement
6. Update specification after completion

See [AI Agent Guide](../AI_AGENT_GUIDE.md) for complete workflow.

## Examples

- [Tag System Implementation](2025-10-19-tag-improvement.md)
- [Wallet Dropdown Ordering](2025-10-30-wallet-dropdown-ordering.md)
- [Report Date Filter](2025-10-26-report-date-filter.md)

---

**Last Updated**: 2025-11-16
