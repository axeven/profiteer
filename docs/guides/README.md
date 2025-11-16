# Development Guidelines

This directory contains **how-to guides and best practices** for developing the Profiteer app.

## Purpose

Guidelines provide practical guidance on:
- How to implement features following established patterns
- Best practices for code quality and architecture
- Step-by-step instructions for common tasks
- Examples and anti-patterns

## Directory Contents

### Architecture Guidelines
- **navigation.md** - Navigation architecture and BackHandler patterns
- **state-management.md** - Consolidated state pattern for Jetpack Compose
- **repository-error-handling.md** - Repository layer error handling

### Code Quality
- **antipatterns.md** - Common mistakes and how to avoid them
- **logging.md** - Logging framework and best practices
- **logging-examples.md** - Logging examples and patterns

### Firebase
- **firebase-security.md** - Firestore security rules and query patterns

### UI/UX
- **tag-system.md** - Tag normalization and formatting guide
- **state-migration-checklist.md** - Migrating screens to consolidated state

## Guidelines vs Specifications

| Specifications (`docs/specs/`) | Guidelines (`docs/guides/`) |
|-------------------------------|----------------------------|
| **WHAT** and **WHY** | **HOW** |
| Source of truth | Implementation guidance |
| Feature behavior | Code patterns |
| Requirements | Best practices |
| Implementation-agnostic | Implementation-specific |

## Using Guidelines

1. **Read Specification First** - Understand what needs to be done
2. **Consult Guidelines** - Learn how to implement it correctly
3. **Follow Patterns** - Use established patterns from guidelines
4. **Create Plan** - Write TDD implementation plan
5. **Implement** - Follow plan and guidelines

## For AI Agents

When implementing features:
1. Read relevant specifications for requirements
2. Read relevant guidelines for implementation patterns
3. Follow TDD approach from plans
4. Adhere to patterns and best practices
5. Update specifications after implementation

## Adding New Guidelines

When creating a new guideline:
1. Focus on **how** to implement something
2. Provide clear examples (✅ correct, ❌ incorrect)
3. Include code samples
4. Link to related specifications
5. Add to this README

## Examples

**Good Guideline Content:**
- Step-by-step instructions
- Code examples with explanations
- Common pitfalls and how to avoid them
- Testing strategies
- Performance considerations

**Not Guideline Content:**
- Feature requirements (goes in specs)
- Project status (goes in CHANGELOG)
- Future plans (goes in ROADMAP)

---

**Last Updated**: 2025-11-16
