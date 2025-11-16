# Documentation Map

Complete tree structure of Profiteer documentation with descriptions.

**Last Updated**: 2025-11-16

---

## Documentation Structure

```
docs/
├── README.md                           # 📍 Main documentation hub - START HERE
├── AI_AGENT_GUIDE.md                   # 🤖 Workflow guide for AI development
├── ROADMAP.md                          # 🗺️  Future features and priorities
├── CHANGELOG.md                        # 📝 Historical changes and releases
│
├── specs/                              # 📚 Source of truth specifications
│   ├── README.md                       # Guide to specifications
│   ├── PROPOSAL_FORMAT.md              # How to propose inline changes
│   │
│   ├── architecture/                   # System design and structure
│   │   └── overview.md                 # Overall architecture patterns (MVVM, DI, etc.)
│   │
│   ├── domain/                         # Core business domain models
│   │   ├── wallets.md                  # Wallet types, rules, relationships
│   │   ├── transactions.md             # Transaction types, validation, lifecycle
│   │   └── currencies.md               # Currency types, conversion, rates
│   │
│   ├── screens/                        # Screen-specific specifications
│   │   ├── home-screen.md              # Dashboard with transaction summary
│   │   ├── login-screen.md             # Google Sign-in authentication
│   │   ├── wallet-list-screen.md       # Physical/Logical wallet management
│   │   ├── wallet-detail-screen.md     # Single wallet view with transactions
│   │   ├── transaction-list-screen.md  # Filtered transaction browsing
│   │   ├── reports-screen.md           # Portfolio analytics and charts
│   │   └── settings-screen.md          # App configuration
│   │
│   ├── features/                       # Cross-cutting feature specifications
│   │   ├── dual-wallet-system.md       # Physical/Logical wallet separation
│   │   ├── multi-currency-support.md   # Fiat, Gold, BTC support
│   │   └── tag-system.md               # Tag normalization and formatting
│   │
│   └── technical/                      # Technical implementation patterns
│       ├── database-schema.md          # Firestore data model
│       ├── firebase-integration.md     # Auth, security rules, query patterns
│       ├── state-management.md         # Consolidated state pattern
│       ├── navigation.md               # NavigationStack architecture
│       ├── error-handling.md           # Repository error domain types
│       └── logging.md                  # Logging framework and sanitization
│
├── plans/                              # ✅ TDD implementation plans
│   ├── README.md                       # Guide to implementation plans
│   ├── _TEMPLATE.md                    # Standard plan template
│   │
│   ├── 2025-11-16-doc-refactor.md      # Documentation restructure (THIS PROJECT)
│   ├── 2025-11-05-wallet-filter-on-report-page.md  # Report wallet filtering
│   ├── 2025-10-30-wallet-dropdown-ordering.md      # Alphabetical wallet sorting
│   ├── 2025-10-28-fix-back-button.md               # UI back button fix
│   ├── 2025-10-26-report-date-filter.md            # Report date filtering
│   ├── 2025-10-25-back-button.md                   # NavigationStack implementation
│   ├── 2025-10-23-fix-lint-errors.md               # Lint error cleanup
│   ├── 2025-10-22-fix-failed-tests.md              # Test suite fixes
│   ├── 2025-10-20-camel-case-tags.md               # Tag display formatting
│   ├── 2025-10-19-tag-improvement.md               # Tag normalization
│   ├── 2025-10-18-money-amount-input.md            # Amount input component
│   ├── 2025-10-17-repository-layer-mixing-concerns.md # Repository error refactor
│   ├── 2025-10-17-remove-hardcode-magic-values.md  # Magic value elimination
│   ├── 2025-10-13-transaction-export.md            # CSV/JSON export
│   ├── 2025-10-13-phase1-test-fix-plan.md          # Test infrastructure
│   ├── 2025-10-12-discrepancy-debug-page.md        # Balance debugging UI
│   ├── 2025-09-23-fix-validation-error-create-transaction.md # Validation fixes
│   ├── 2025-09-23-balance-calculation-service.md   # Balance calculation
│   ├── 2025-09-22-resolve-complex-state-management.md # State consolidation
│   └── 2025-09-21-debug-log-improvement.md         # Logging framework
│
├── guides/                             # 📖 Development how-to guides
│   ├── README.md                       # Guide to development guidelines
│   ├── firebase-security.md            # Firestore query security patterns
│   ├── state-management.md             # Consolidated state implementation
│   ├── state-migration-checklist.md    # Step-by-step state refactoring
│   ├── navigation.md                   # NavigationStack usage patterns
│   ├── repository-error-handling.md    # Domain error handling patterns
│   ├── logging.md                      # Logger injection and usage
│   ├── logging-examples.md             # Logging code examples
│   ├── tag-system.md                   # Tag normalization/formatting usage
│   └── antipatterns.md                 # Common mistakes to avoid
│
└── reference/                          # 📑 Supporting reference materials
    ├── README.md                       # Guide to reference docs
    └── credential-debugging.md         # Firebase credential troubleshooting
```

---

## Legacy/Archived Documentation

**⚠️ TO BE REMOVED** (Phase 13-14 cleanup):

```
docs/
├── implementation-status.md            # 🗑️  DELETE - replaced by ROADMAP.md + spec metadata
├── recent-changes.md                   # 🗑️  DELETE - replaced by CHANGELOG.md
│
├── concepts/                           # 🗑️  DELETE directory after migration
│   ├── wallets.md                      # → specs/domain/wallets.md
│   ├── transactions.md                 # → specs/domain/transactions.md
│   └── currencies.md                   # → specs/domain/currencies.md
│
├── pages/                              # 🗑️  DELETE directory after migration
│   ├── homepage.md                     # → specs/screens/home-screen.md
│   ├── login_page.md                   # → specs/screens/login-screen.md
│   ├── reports_page.md                 # → specs/screens/reports-screen.md
│   ├── settings_page.md                # → specs/screens/settings-screen.md
│   ├── transaction_list_page.md        # → specs/screens/transaction-list-screen.md
│   ├── wallet_detail_page.md           # → specs/screens/wallet-detail-screen.md
│   └── wallet_list_page.md             # → specs/screens/wallet-list-screen.md
│
├── NAVIGATION_GUIDELINES.md            # 🗑️  REDUNDANT - extracted from CLAUDE.md
├── REPOSITORY_ERROR_HANDLING.md        # 🗑️  REDUNDANT - extracted from CLAUDE.md
├── TAG_SYSTEM.md                       # 🗑️  REDUNDANT - extracted from CLAUDE.md
├── FIREBASE_SECURITY_GUIDELINES.md     # 🗑️  REDUNDANT - migrated to specs/technical/
├── LOGGING_GUIDELINES.md               # 🗑️  REDUNDANT - migrated to specs/technical/
├── LOGGING_EXAMPLES.md                 # 🗑️  REDUNDANT - migrated to guides/
├── STATE_MANAGEMENT_GUIDELINES.md      # 🗑️  REDUNDANT - migrated to specs/technical/
├── STATE_MIGRATION_CHECKLIST.md        # 🗑️  REDUNDANT - migrated to guides/
├── architecture.md                     # 🗑️  REDUNDANT - migrated to specs/architecture/
├── database-schema.md                  # 🗑️  REDUNDANT - migrated to specs/technical/
├── credential-debugging-guide.md       # 🗑️  REDUNDANT - migrated to reference/
├── feature-requirements.md             # 🗑️  REDUNDANT - split into specs/features/
└── antipatterns.md                     # 🗑️  REDUNDANT - migrated to guides/
```

---

## Archived Historical Documents

```
docs/archived/prps/                     # 📦 Old PRP (Problem Resolution Proposal) format
├── base_template.md                    # Legacy proposal template
├── 00_bug_wallet_dropdown_multi_select.md
├── 01_bug_category_and_tag.md
├── 02_bug_tag_display_in_home_page.md
├── 03_bug_currency_display_on_transaction.md
├── 04_bug_incorrect_transaction_date_display.md
├── 05_bug_incorrect_transaction_order.md
├── 06_feature_wallet_detail_page.md
├── 07_feature_transfer_transaction_in_wallet_detail_page.md
├── 08_bug_home_screen_transaction_summary.md
├── 09_feature_month_filter.md
├── 10_bug_datepicker_column.md
├── 11_feature_physical_wallet_form.md
├── 12_feature_drop_per_wallet_currency.md
├── 13_bug_wallet_balance_currency.md
├── 14_feature_split_transaction_analytics.md
├── 15_remove_portfolio_allocation_wallet_list.md
├── 16_feature_group_transaction_list_by_date_wallet_detail.md
├── 18_feature_recalculate_balance_button.md
└── 19_feature_transaction_list_page.md
```

**Note**: PRPs are archived for historical reference only. New proposals use inline format in spec files.

---

## Navigation Quick Reference

### By Purpose

| What You Need | File Path |
|---------------|-----------|
| **Getting started** | [docs/README.md](README.md) |
| **AI workflow guide** | [docs/AI_AGENT_GUIDE.md](AI_AGENT_GUIDE.md) |
| **Future features** | [docs/ROADMAP.md](ROADMAP.md) |
| **Historical changes** | [docs/CHANGELOG.md](CHANGELOG.md) |
| **Propose changes** | [docs/specs/PROPOSAL_FORMAT.md](specs/PROPOSAL_FORMAT.md) |
| **Create plan** | [docs/plans/_TEMPLATE.md](plans/_TEMPLATE.md) |

### By Category

| Category | Directory |
|----------|-----------|
| **Specifications** | [docs/specs/](specs/) |
| **Implementation Plans** | [docs/plans/](plans/) |
| **Guidelines** | [docs/guides/](guides/) |
| **Reference** | [docs/reference/](reference/) |

### By Topic

| Topic | File Path |
|-------|-----------|
| **Architecture** | [specs/architecture/overview.md](specs/architecture/overview.md) |
| **Wallets** | [specs/domain/wallets.md](specs/domain/wallets.md) |
| **Transactions** | [specs/domain/transactions.md](specs/domain/transactions.md) |
| **Currencies** | [specs/domain/currencies.md](specs/domain/currencies.md) |
| **Firebase** | [specs/technical/firebase-integration.md](specs/technical/firebase-integration.md) |
| **State Management** | [specs/technical/state-management.md](specs/technical/state-management.md) |
| **Navigation** | [specs/technical/navigation.md](specs/technical/navigation.md) |
| **Error Handling** | [specs/technical/error-handling.md](specs/technical/error-handling.md) |
| **Logging** | [specs/technical/logging.md](specs/technical/logging.md) |
| **Database** | [specs/technical/database-schema.md](specs/technical/database-schema.md) |

---

## Document Status

### Source of Truth (Keep and Maintain)

- ✅ All files in `docs/specs/`
- ✅ All files in `docs/plans/`
- ✅ All files in `docs/guides/`
- ✅ All files in `docs/reference/`
- ✅ Root documentation: README.md, AI_AGENT_GUIDE.md, ROADMAP.md, CHANGELOG.md

### Legacy (To Be Deleted)

- 🗑️  `implementation-status.md`
- 🗑️  `recent-changes.md`
- 🗑️  `concepts/` directory
- 🗑️  `pages/` directory
- 🗑️  Standalone guideline files (NAVIGATION_GUIDELINES.md, etc.)
- 🗑️  Duplicate technical files

### Archived (Keep for History)

- 📦 `docs/archived/prps/` - Historical proposals

---

## File Naming Conventions

### Specifications (`docs/specs/`)
- Format: `kebab-case-name.md`
- Examples: `dual-wallet-system.md`, `multi-currency-support.md`
- Subdirectories: `architecture/`, `domain/`, `screens/`, `features/`, `technical/`

### Plans (`docs/plans/`)
- Format: `YYYY-MM-DD-feature-name.md`
- Examples: `2025-10-20-camel-case-tags.md`, `2025-10-26-report-date-filter.md`
- Template: `_TEMPLATE.md`

### Guides (`docs/guides/`)
- Format: `kebab-case-topic.md`
- Examples: `firebase-security.md`, `state-management.md`

### Reference (`docs/reference/`)
- Format: `kebab-case-topic.md`
- Examples: `credential-debugging.md`

---

## Maintenance Notes

**Update this file when:**
- New specification files are added
- New implementation plans are created
- Directory structure changes
- Files are moved, renamed, or deleted
- New categories of documentation are introduced

**Validation:**
- Run `find docs -type f -name "*.md" | sort` to verify all files are listed
- Check for broken links periodically
- Ensure legacy files are deleted after Phase 13-14 cleanup

---

**For complete documentation workflow**, see [AI_AGENT_GUIDE.md](AI_AGENT_GUIDE.md).
