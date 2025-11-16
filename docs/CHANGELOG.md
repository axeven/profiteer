# Changelog

All notable changes to the Profiteer project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

---

## [2025-11-16] Documentation Refactoring ✅

### Added
- **New documentation structure** with `docs/specs/`, `docs/guides/`, `docs/reference/`
- **AI Agent Guide** (`AI_AGENT_GUIDE.md`) - Complete AI development workflow
- **Documentation Hub** (`docs/README.md`) - Main entry point for all documentation
- **Documentation Map** (`DOCUMENTATION_MAP.md`) - Complete file tree with descriptions
- **Migration Summary** (`MIGRATION_SUMMARY.md`) - Detailed migration tracking
- **Inline proposal format** for specification changes (`PROPOSAL_FORMAT.md`)
- **Plan template** (`plans/_TEMPLATE.md`) for TDD implementation plans
- **Comprehensive README files** for each documentation directory
- **ROADMAP.md** for future feature planning
- **CHANGELOG.md** for historical record (this file)
- **Feature specifications**: Dual Wallet System, Multi-Currency Support, Tag System
- **Technical specifications**: Firebase, State Management, Navigation, Logging, Error Handling

### Changed
- **Reorganized all documentation** into logical categories (specs/guides/reference)
- **Updated CLAUDE.md** - All documentation references point to new structure
- **Updated README.md** - Documentation section rewritten with new organization
- **Migrated architecture specs** to `docs/specs/architecture/`
- **Migrated domain models** to `docs/specs/domain/`
- **Migrated screen specs** to `docs/specs/screens/`
- **Migrated technical specs** to `docs/specs/technical/`
- **Moved guidelines** to `docs/guides/`
- **Moved reference docs** to `docs/reference/`
- **Split feature-requirements.md** into focused feature specs

### Removed
- `implementation-status.md` (replaced by spec metadata + ROADMAP)
- `recent-changes.md` (replaced by CHANGELOG)
- `docs/concepts/` directory (migrated to specs/domain/)
- `docs/pages/` directory (migrated to specs/screens/)
- Redundant standalone guideline files (migrated and integrated)

**Implementation Plan**: [2025-11-16-doc-refactor.md](plans/2025-11-16-doc-refactor.md)
**Migration Details**: [MIGRATION_SUMMARY.md](MIGRATION_SUMMARY.md)

---

## [2025-11-05] Wallet Filter on Report Page

### Added
- Wallet filtering capability on Portfolio Reports screen
- Works alongside existing date filter for granular analysis
- `WalletFilter` sealed class (AllWallets, SpecificWallet)
- `WalletFilterUtils` for transaction/wallet filtering
- `WalletFilterPickerDialog` Material 3 dialog component
- Chart title generation with filter context

### Changed
- Enhanced `BalanceReconstructionUtils` to support wallet filtering
- Updated Reports screen to combine date + wallet filters

**Implementation Plan**: [2025-11-05-wallet-filter-on-report-page.md](plans/2025-11-05-wallet-filter-on-report-page.md)

---

## [2025-10-30] Wallet Dropdown Ordering

### Added
- Alphabetical wallet sorting in all dropdowns
- `WalletSortingUtils` utility class
- `sortAlphabetically()` for simple dropdowns
- `sortByTypeAndName()` for transfer dropdowns

### Changed
- All wallet dropdowns now display alphabetically
- Transfer dropdowns group by type then sort alphabetically

**Implementation Plan**: [2025-10-30-wallet-dropdown-ordering.md](plans/2025-10-30-wallet-dropdown-ordering.md)

---

## [2025-10-28] UI Back Button Fix

### Fixed
- Non-functional UI back buttons in 8 screens
- Added `createBackNavigationCallback` helper function

### Changed
- Settings, CreateTransaction, EditTransaction screens
- WalletDetail, WalletList, TransactionList screens
- Reports, DiscrepancyDebug screens

**Implementation Plan**: [2025-10-28-fix-back-button.md](plans/2025-10-28-fix-back-button.md)

---

## [2025-10-26] Report Date Filtering

### Added
- Month/year filtering to Portfolio Reports screen
- `DateFilterPeriod` sealed class (AllTime, Month, Year)
- `DateFilterUtils` for transaction filtering
- `BalanceReconstructionUtils` for historical balance reconstruction
- `MonthYearPickerDialog` Material 3 component

**Implementation Plan**: [2025-10-26-report-date-filter.md](plans/2025-10-26-report-date-filter.md)

---

## [2025-10-25] Navigation Stack Implementation

### Added
- Custom `NavigationStack` class for screen history management
- Stack-based navigation replacing manual `previousScreen` tracking
- Integration with Android `BackHandler`
- Navigation logging for debugging

### Changed
- Reduced conditional logic by 80% in back navigation
- Removed manual screen state tracking

**Implementation Plan**: [2025-10-25-back-button.md](plans/2025-10-25-back-button.md)

---

## [2025-10-20] Tag Display Formatting

### Added
- Camel case formatting for tag display
- `TagFormatter` utility class
- Formatting applied in UI layer only

### Changed
- Tags displayed as "Food", "Travel" instead of "food", "travel"
- Storage remains lowercase for consistency

**Implementation Plan**: [2025-10-20-camel-case-tags.md](plans/2025-10-20-camel-case-tags.md)

---

## [2025-10-19] Tag Normalization

### Added
- Automatic tag normalization system
- `TagNormalizer` utility class
- Case-insensitive duplicate removal
- Whitespace trimming
- Reserved keyword filtering

### Changed
- All tags normalized to lowercase on input
- Duplicate tags automatically removed
- "Untagged" keyword filtered out

**Implementation Plan**: [2025-10-19-tag-improvement.md](plans/2025-10-19-tag-improvement.md)

---

## [2025-10-17] Repository Error Handling

### Added
- `RepositoryError` domain error types
- `FirestoreErrorHandler` utility
- Error handling utilities in `ErrorHandlingUtils.kt`

### Changed
- Repositories no longer depend on UI layer (ViewModels)
- Error handling moved to domain layer

**Implementation Plan**: [2025-10-17-repository-layer-mixing-concerns.md](plans/2025-10-17-repository-layer-mixing-concerns.md)

---

## Earlier Releases

### Core Features Implemented
- ✅ Dual wallet system (Physical + Logical)
- ✅ Multi-currency support (Fiat, Gold, BTC)
- ✅ Tag-based transaction categorization
- ✅ Real-time Firebase sync
- ✅ MVVM architecture
- ✅ Material 3 design system
- ✅ Unified Reports page
- ✅ Wallet management screens
- ✅ Transaction CRUD operations
- ✅ Currency conversion system

---

**Maintenance**: This changelog is updated when features are implemented. For future plans, see [ROADMAP.md](ROADMAP.md).
