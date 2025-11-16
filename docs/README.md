# Profiteer Documentation

Welcome to the Profiteer documentation. This directory contains all specifications, guidelines, and references for developing and maintaining the Profiteer personal finance app.

---

## Documentation Structure

```
docs/
├── specs/          # 📚 Source of truth specifications
├── plans/          # ✅ TDD implementation plans
├── guides/         # 📖 Development guidelines
├── reference/      # 📑 Reference materials
├── archived/       # 📦 Archived documentation
├── ROADMAP.md      # 🗺️ Future features and priorities
└── CHANGELOG.md    # 📝 Historical changes
```

---

## Quick Start

### For New Developers

1. **Understand the System**: Read [Architecture Overview](specs/architecture/overview.md)
2. **Learn the Domain**: Review [Domain Models](specs/domain/)
3. **Follow Guidelines**: Check [Development Guidelines](guides/)
4. **See Examples**: Browse [Implementation Plans](plans/)

### For AI Agents

1. **Read**: [AI Agent Guide](AI_AGENT_GUIDE.md)
2. **Understand Workflow**: Propose → Plan → Implement → Finalize
3. **Use Inline Proposals**: See [Proposal Format](specs/PROPOSAL_FORMAT.md)
4. **Follow TDD**: See [Plan Template](plans/_TEMPLATE.md)

---

## Finding Documentation

### By Category

| What You Need | Where to Look |
|---------------|---------------|
| **Feature behavior** | [specs/features/](specs/features/) |
| **Screen requirements** | [specs/screens/](specs/screens/) |
| **Domain models** | [specs/domain/](specs/domain/) |
| **System architecture** | [specs/architecture/](specs/architecture/) |
| **Technical specs** | [specs/technical/](specs/technical/) |
| **How to implement** | [guides/](guides/) |
| **Implementation plans** | [plans/](plans/) |
| **Troubleshooting** | [reference/](reference/) |

### By Feature

- **Dual Wallet System**: [specs/features/dual-wallet-system.md](specs/features/dual-wallet-system.md)
- **Multi-Currency**: [specs/features/multi-currency-support.md](specs/features/multi-currency-support.md)
- **Tag System**: [specs/features/tag-system.md](specs/features/tag-system.md)
- **Transaction Management**: [specs/domain/transactions.md](specs/domain/transactions.md)
- **Reports & Analytics**: [specs/screens/reports-screen.md](specs/screens/reports-screen.md)

### By Technology

- **Firebase/Firestore**: [specs/technical/firebase-integration.md](specs/technical/firebase-integration.md)
- **State Management**: [specs/technical/state-management.md](specs/technical/state-management.md)
- **Navigation**: [specs/technical/navigation.md](specs/technical/navigation.md)
- **Logging**: [specs/technical/logging.md](specs/technical/logging.md)
- **Error Handling**: [specs/technical/error-handling.md](specs/technical/error-handling.md)

---

## Development Workflow

### Proposing Changes

1. Open relevant specification file
2. Add inline proposed change using [standard format](specs/PROPOSAL_FORMAT.md)
3. Create TDD implementation plan in `plans/`
4. Get approval (if required)

### Implementing Features

1. Read specification with proposed changes
2. Follow implementation plan (TDD approach)
3. Write tests first, then implementation
4. Update specification after completion

### Updating Documentation

- **Specifications**: Update when features change
- **Plans**: Mark completed when done
- **Guides**: Update when patterns change
- **CHANGELOG**: Add entry for major changes
- **ROADMAP**: Move items as priorities change

---

## Key Documentation

### Essential Reading

| Document | Purpose |
|----------|---------|
| [Architecture Overview](specs/architecture/overview.md) | System design and structure |
| [Database Schema](specs/technical/database-schema.md) | Firestore data model |
| [ROADMAP](ROADMAP.md) | Future features and priorities |
| [CHANGELOG](CHANGELOG.md) | Historical changes |

### For Development

| Guide | Purpose |
|-------|---------|
| [Firebase Security](guides/firebase-security.md) | Firestore query patterns |
| [State Management](guides/state-management.md) | Consolidated state pattern |
| [Navigation](guides/navigation.md) | NavigationStack usage |
| [Repository Errors](guides/repository-error-handling.md) | Error handling patterns |
| [Logging](guides/logging.md) | Logging framework |

### For Planning

| Document | Purpose |
|----------|---------|
| [Proposal Format](specs/PROPOSAL_FORMAT.md) | How to propose changes |
| [Plan Template](plans/_TEMPLATE.md) | TDD plan structure |
| [AI Agent Guide](AI_AGENT_GUIDE.md) | Workflow for AI agents |

---

## Documentation Standards

### All Specifications Must Include

- **Status**: ✅ Implemented | 🚧 Partial | 🔄 Proposed | ❌ Planned
- **Last Updated**: Date of last modification
- **Purpose**: Clear description of what the spec covers
- **Related Specifications**: Links to related docs

### All Plans Must Include

- **Date**: When plan was created
- **Status**: 🔴 Not Started | 🟡 In Progress | 🟢 Completed
- **Objective**: Brief description
- **TDD Phases**: Test-driven checklist approach
- **Success Criteria**: Measurable outcomes
- **Rollback Plan**: How to undo if needed

---

## Contributing

When adding new documentation:

1. **Choose the right location**:
   - WHAT/WHY → `specs/`
   - HOW → `guides/`
   - Supporting info → `reference/`
   - Future plans → `ROADMAP.md`
   - Historical → `CHANGELOG.md`

2. **Follow existing format**:
   - Use appropriate template
   - Include required metadata
   - Link to related docs

3. **Update this README**:
   - Add to relevant section
   - Update table of contents if needed

---

## Need Help?

- **Questions about features?** → Check `specs/`
- **How to implement something?** → Check `guides/`
- **Having an issue?** → Check `reference/`
- **Want to propose a change?** → See [Proposal Format](specs/PROPOSAL_FORMAT.md)
- **Contributing?** → See [AI Agent Guide](AI_AGENT_GUIDE.md) or above

---

**Last Updated**: 2025-11-16

For the main project README, see [../README.md](../README.md)
