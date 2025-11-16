# Specifications

This directory contains the **source of truth** for all Profiteer app specifications.

## Purpose

Specifications define **what** the app should do and **how** it should behave. They serve as:
- Single source of truth for features and functionality
- Reference for AI agents and developers
- Foundation for implementation plans
- Documentation of current and proposed behavior

## Directory Structure

```
specs/
├── architecture/    # Architecture specifications
├── domain/          # Domain model specifications (wallets, transactions, etc.)
├── screens/         # UI/Screen specifications
├── features/        # Feature specifications
└── technical/       # Technical specifications (Firebase, state management, etc.)
```

## Specification Format

Each specification file should include:

```markdown
# Specification Title

**Status**: ✅ Implemented | 🚧 Partial | 🔄 Proposed | ❌ Planned
**Last Updated**: YYYY-MM-DD
**Implementation Plan**: [Link to implementation plan if applicable]

## Purpose
Brief description of what this specification covers.

## Current Behavior
How it currently works (for implemented features).

## Requirements
Detailed requirements and specifications.

## Related Specifications
Links to related specs.
```

## Proposing Changes

Changes are proposed **inline** within spec files using this format:

```markdown
## Feature Name

**Current Behavior:**
Description of how it currently works.

**🔄 Proposed Change:**
Description of proposed changes.
- **Motivation**: Why this change is needed
- **Impact**: What will be affected
- **Breaking Changes**: Any compatibility issues

**Implementation Plan**: [Link to plan in docs/plans/]
```

After implementation, update the spec:
1. Remove "Current Behavior" section
2. Replace with new behavior (incorporating changes)
3. Remove "🔄 Proposed" markers
4. Update "Last Updated" date
5. Update "Status" if needed

## Specification Lifecycle

1. **Create Spec** - Document new feature/behavior
2. **Propose Changes** - Add inline proposed changes with rationale
3. **Create Plan** - Write TDD implementation plan in `docs/plans/`
4. **Implement** - Execute the plan with tests
5. **Update Spec** - Merge proposed changes, remove markers
6. **Maintain** - Keep spec updated as feature evolves

## Guidelines

- **Single Source of Truth**: Each spec is the authoritative source for its topic
- **Implementation-Agnostic**: Focus on WHAT and WHY, not HOW (implementation details go in guides)
- **Always Current**: Specs should always reflect current state or clearly mark proposed changes
- **Cross-Reference**: Link to related specs, implementation plans, and guides
- **Version Metadata**: Always include Status and Last Updated

## Finding Specifications

- **By Feature**: Check `features/` directory
- **By Screen**: Check `screens/` directory
- **By Domain Concept**: Check `domain/` directory
- **By Technology**: Check `technical/` directory
- **By Architecture**: Check `architecture/` directory

## For AI Agents

When working with specifications:
1. **Read** relevant specs to understand current behavior
2. **Propose** changes inline using the defined format
3. **Create** implementation plan in `docs/plans/`
4. **Implement** following TDD approach
5. **Update** spec after implementation (merge proposals)

See [AI Agent Guide](../AI_AGENT_GUIDE.md) for complete workflow.

## Examples

See `PROPOSAL_FORMAT.md` for detailed examples of inline proposals.

---

**Last Updated**: 2025-11-16
