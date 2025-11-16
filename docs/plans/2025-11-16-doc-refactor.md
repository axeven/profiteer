# Documentation Refactoring Plan

**Date**: 2025-11-16
**Status**: 🟢 Completed
**Objective**: Restructure documentation to support AI-driven development workflow with clear specification management

---

## Overview

### Current State
- Documentation scattered across multiple directories without clear hierarchy
- No distinction between specifications (source of truth) and implementation plans
- Guidelines mixed with specifications
- No formal workflow for proposing and implementing changes

### Target State
- Clear separation: Specifications → Implementation Plans → Implementation
- AI and humans can understand app behavior from specifications
- TDD approach enforced through checklist-style plans
- Single source of truth for each aspect of the application
- Inline proposals within spec files for easier context and review

### Workflow
```
1. Propose Change: Modify spec file directly with inline proposed changes
   - Mark current behavior with "**Current Behavior:**"
   - Mark proposed changes with "**Proposed Change:**" or "**🔄 Proposed:**"
   - Keep both visible for context
2. Create Plan: Write implementation plan in docs/plans/ (TDD checklist style)
   - Reference the spec file with proposed changes
3. Implement: Execute plan with tests
4. Finalize: Update spec file
   - Remove old behavior documentation
   - Change proposed behavior to current behavior
   - Remove proposal markers
```

---

## New Directory Structure

```
docs/
├── specs/                          # Source of truth specifications
│   ├── architecture/               # Architecture specifications
│   ├── domain/                     # Domain model specifications
│   ├── screens/                    # UI/Screen specifications
│   ├── features/                   # Feature specifications
│   └── technical/                  # Technical specifications
├── plans/                          # Implementation plans (TDD checklists)
├── guides/                         # Development guidelines
├── reference/                      # Reference documentation
└── archived/                       # Archived documentation
    └── prps/                       # Old PRPs (keep as-is)
```

### Proposal Format

Proposed changes are documented **inline** within spec files using this format:

```markdown
## Feature Name

**Current Behavior:**
[Description of how it currently works]

**🔄 Proposed Change:**
[Description of proposed changes]
- Motivation: Why this change is needed
- Impact: What will be affected
- Breaking Changes: Any compatibility issues

**Implementation Plan:** See [docs/plans/YYYY-MM-DD-feature-name.md](../plans/YYYY-MM-DD-feature-name.md)
```

After implementation, the spec is updated to:

```markdown
## Feature Name

[Description of how it now works - incorporating the implemented changes]
```

---

## Phase 1: Create New Directory Structure

### Test 1.1: Verify New Directories Created
- [ ] Create `docs/specs/` directory
- [ ] Create `docs/specs/architecture/` directory
- [ ] Create `docs/specs/domain/` directory
- [ ] Create `docs/specs/screens/` directory
- [ ] Create `docs/specs/features/` directory
- [ ] Create `docs/specs/technical/` directory
- [ ] Create `docs/guides/` directory
- [ ] Create `docs/reference/` directory
- [ ] Verify all directories exist with `find docs/specs -type d`
- [ ] Verify all directories exist with `find docs/guides docs/reference -type d`

### Test 1.2: Create README Files for Each Directory
- [ ] Create `docs/specs/README.md` explaining specification structure and inline proposal format
- [ ] Create `docs/plans/README.md` explaining plan requirements (TDD checklist)
- [ ] Create `docs/guides/README.md` explaining guidelines purpose
- [ ] Create `docs/reference/README.md` explaining reference docs
- [ ] Verify README files contain required sections
- [ ] Verify README files follow consistent format
- [ ] Verify `docs/specs/README.md` includes inline proposal examples

---

## Phase 2: Migrate Architecture Specifications

### Test 2.1: Migrate Core Architecture Docs
- [ ] Copy `docs/architecture.md` to `docs/specs/architecture/overview.md`
- [ ] Update content to be specification-focused (remove implementation details)
- [ ] Add "Last Updated" and "Status" metadata to file
- [ ] Verify file exists at new location
- [ ] Verify content is specification-focused (not implementation guide)

### Test 2.2: Migrate Database Schema
- [ ] Copy `docs/database-schema.md` to `docs/specs/technical/database-schema.md`
- [ ] Add version information and last updated date
- [ ] Add schema evolution history section
- [ ] Verify file exists at new location
- [ ] Verify schema matches current Firestore structure

### Test 2.3: Create Architecture Index
- [ ] Create `docs/specs/architecture/README.md` as index
- [ ] List all architecture specification files
- [ ] Add cross-references to related technical specs
- [ ] Verify index is complete and links work

---

## Phase 3: Migrate Domain Model Specifications

### Test 3.1: Migrate Domain Concepts
- [ ] Move `docs/concepts/wallets.md` to `docs/specs/domain/wallets.md`
- [ ] Move `docs/concepts/transactions.md` to `docs/specs/domain/transactions.md`
- [ ] Move `docs/concepts/currencies.md` to `docs/specs/domain/currencies.md`
- [ ] Verify all files exist at new locations
- [ ] Verify old `docs/concepts/` directory is empty

### Test 3.2: Enhance Domain Specifications
- [ ] Add "Business Rules" section to `wallets.md`
- [ ] Add "Validation Rules" section to `transactions.md`
- [ ] Add "Conversion Rules" section to `currencies.md`
- [ ] Add state diagrams where applicable
- [ ] Verify each spec includes: Purpose, Rules, Constraints, Examples

### Test 3.3: Create Domain Index
- [ ] Create `docs/specs/domain/README.md` as index
- [ ] Document relationships between domain models
- [ ] Add entity relationship diagram (Mermaid format)
- [ ] Verify index shows complete domain model

---

## Phase 4: Migrate Screen Specifications

### Test 4.1: Migrate UI/Screen Docs
- [ ] Move `docs/pages/homepage.md` to `docs/specs/screens/home-screen.md`
- [ ] Move `docs/pages/login_page.md` to `docs/specs/screens/login-screen.md`
- [ ] Move `docs/pages/reports_page.md` to `docs/specs/screens/reports-screen.md`
- [ ] Move `docs/pages/settings_page.md` to `docs/specs/screens/settings-screen.md`
- [ ] Move `docs/pages/transaction_list_page.md` to `docs/specs/screens/transaction-list-screen.md`
- [ ] Move `docs/pages/wallet_detail_page.md` to `docs/specs/screens/wallet-detail-screen.md`
- [ ] Move `docs/pages/wallet_list_page.md` to `docs/specs/screens/wallet-list-screen.md`
- [ ] Verify all files exist at new locations
- [ ] Verify old `docs/pages/` directory is empty

### Test 4.2: Standardize Screen Specifications
- [ ] Add standard sections to each screen spec: Purpose, User Stories, UI Components, Navigation, State Management
- [ ] Add wireframes or UI mockups (if available)
- [ ] Document user interactions and validation rules
- [ ] Add accessibility requirements
- [ ] Verify each screen spec follows consistent template

### Test 4.3: Create Screen Index
- [ ] Create `docs/specs/screens/README.md` as index
- [ ] List all screens with brief descriptions
- [ ] Add navigation flow diagram (Mermaid format)
- [ ] Document screen hierarchy and relationships
- [ ] Verify index matches NavigationStack implementation

---

## Phase 5: Migrate Feature Specifications

### Test 5.1: Extract Feature Specs from feature-requirements.md
- [ ] Create `docs/specs/features/dual-wallet-system.md`
- [ ] Create `docs/specs/features/multi-currency-support.md`
- [ ] Create `docs/specs/features/transaction-management.md`
- [ ] Create `docs/specs/features/reporting-analytics.md`
- [ ] Create `docs/specs/features/tag-system.md`
- [ ] Verify each feature spec includes: Purpose, Requirements, Acceptance Criteria, Dependencies
- [ ] Verify all features from `feature-requirements.md` are covered

### Test 5.2: Create Feature Specifications Template
- [ ] Create `docs/specs/features/_TEMPLATE.md` for new features
- [ ] Template includes: Feature Name, Purpose, User Stories, Requirements, Acceptance Criteria, Technical Constraints
- [ ] Template includes: Related Specs, Implementation Status
- [ ] Verify template is complete and usable

### Test 5.3: Create Feature Index
- [ ] Create `docs/specs/features/README.md` as index
- [ ] List all features with implementation status
- [ ] Add feature dependency graph
- [ ] Link to related screen and domain specs
- [ ] Verify index is comprehensive

---

## Phase 6: Migrate Technical Specifications

### Test 6.1: Create Technical Spec Files
- [ ] Create `docs/specs/technical/firebase-integration.md` from FIREBASE_SECURITY_GUIDELINES.md
- [ ] Create `docs/specs/technical/state-management.md` from STATE_MANAGEMENT_GUIDELINES.md
- [ ] Create `docs/specs/technical/navigation.md` from NAVIGATION_GUIDELINES.md
- [ ] Create `docs/specs/technical/logging.md` from LOGGING_GUIDELINES.md
- [ ] Create `docs/specs/technical/error-handling.md` from REPOSITORY_ERROR_HANDLING.md
- [ ] Verify technical specs focus on WHAT and WHY (not HOW)

### Test 6.2: Create Technical Specifications Index
- [ ] Create `docs/specs/technical/README.md` as index
- [ ] List all technical specifications
- [ ] Group by category: Infrastructure, Data, UI, Quality
- [ ] Add cross-references to implementation plans
- [ ] Verify index is complete

---

## Phase 7: Migrate Development Guidelines

### Test 7.1: Move Guidelines to docs/guides/
- [ ] Move `FIREBASE_SECURITY_GUIDELINES.md` to `docs/guides/firebase-security.md`
- [ ] Move `LOGGING_GUIDELINES.md` to `docs/guides/logging.md`
- [ ] Move `LOGGING_EXAMPLES.md` to `docs/guides/logging-examples.md`
- [ ] Move `NAVIGATION_GUIDELINES.md` to `docs/guides/navigation.md`
- [ ] Move `STATE_MANAGEMENT_GUIDELINES.md` to `docs/guides/state-management.md`
- [ ] Move `STATE_MIGRATION_CHECKLIST.md` to `docs/guides/state-migration-checklist.md`
- [ ] Move `REPOSITORY_ERROR_HANDLING.md` to `docs/guides/repository-error-handling.md`
- [ ] Move `TAG_SYSTEM.md` to `docs/guides/tag-system.md`
- [ ] Move `antipatterns.md` to `docs/guides/antipatterns.md`
- [ ] Verify all files exist at new locations

### Test 7.2: Create Guidelines Index
- [ ] Create `docs/guides/README.md` as index
- [ ] Categorize guidelines: Architecture, Code Quality, Testing, Firebase, UI
- [ ] Add quick reference table
- [ ] Link to related specifications
- [ ] Verify index is comprehensive

---

## Phase 8: Migrate Reference Documentation

### Test 8.1: Move Reference Docs
- [ ] Move `credential-debugging-guide.md` to `docs/reference/credential-debugging.md`
- [ ] **DELETE** `implementation-status.md` (redundant with spec status metadata)
- [ ] **DELETE** `recent-changes.md` (redundant with CHANGELOG.md)
- [ ] Verify all files exist at new locations

### Test 8.2: Create Roadmap and Changelog (Replacing implementation-status.md + recent-changes.md)
- [ ] Create `docs/ROADMAP.md` for future priorities
- [ ] Extract "Future Roadmap" section from recent-changes.md (lines 178-185)
- [ ] Extract "Next Priority Items" from implementation-status.md (lines 136-153)
- [ ] Group by priority: High, Medium, Low
- [ ] Link to relevant specs where they exist
- [ ] Create `docs/CHANGELOG.md` for historical changes
- [ ] Extract major implementations from recent-changes.md (lines 9-111)
- [ ] Extract "Recent Completions" from implementation-status.md (lines 155-203)
- [ ] Format as dated entries with links to implementation plans
- [ ] Follow standard changelog format (## [YYYY-MM-DD] Title)
- [ ] Verify both files provide better value than deleted files

### Test 8.3: Add Status Metadata to Spec Templates
- [ ] Update all spec templates to include status metadata
- [ ] Add fields: Status (✅ Implemented, 🚧 Partial, 🔄 Proposed, ❌ Planned)
- [ ] Add field: Last Updated (date)
- [ ] Add field: Implementation Plan (link)
- [ ] Verify templates guide proper status tracking

### Test 8.4: Create Reference Index
- [ ] Create `docs/reference/README.md` as index
- [ ] List all reference documents
- [ ] Add "When to Use This Doc" section
- [ ] Verify index is complete

---

## Phase 9: Update Implementation Plans

### Test 9.1: Add README to docs/plans/
- [ ] Create `docs/plans/README.md` explaining plan requirements
- [ ] Document TDD checklist approach requirement
- [ ] Provide plan template with test sections
- [ ] Add examples of good vs bad plans
- [ ] Document plan lifecycle: Draft → In Progress → Completed → Archived

### Test 9.2: Create Plan Template
- [ ] Create `docs/plans/_TEMPLATE.md` for new implementation plans
- [ ] Template includes: Objective, Overview, Test-Driven Phases
- [ ] Each phase has: Test description → Implementation → Verification
- [ ] Template includes: Dependencies, Risks, Rollback Plan
- [ ] Verify template follows TDD checklist style

### Test 9.3: Audit Existing Plans
- [ ] Review all existing plans in `docs/plans/`
- [ ] Verify each plan has completion status
- [ ] Update completed plans with ✅ checkmarks
- [ ] Add "Status" and "Last Updated" metadata to each plan
- [ ] Archive completed plans older than 3 months to `docs/archived/plans/`

---

## Phase 10: Define Inline Proposal Format and Conventions

### Test 10.1: Document Proposal Formatting Conventions
- [ ] Create `docs/specs/PROPOSAL_FORMAT.md` explaining inline proposal format
- [ ] Document markdown conventions for marking current vs proposed behavior
- [ ] Define standard markers: `**Current Behavior:**`, `**🔄 Proposed:**`
- [ ] Provide examples for different types of changes (features, technical, screens)
- [ ] Specify where to place proposals (inline vs separate section)
- [ ] Verify format is clear and consistent

### Test 10.2: Create Proposal Examples
- [ ] Add example of inline feature proposal to `docs/specs/PROPOSAL_FORMAT.md`
- [ ] Add example of inline technical change to `docs/specs/PROPOSAL_FORMAT.md`
- [ ] Add example of inline screen modification to `docs/specs/PROPOSAL_FORMAT.md`
- [ ] Add example of spec after implementation (proposal merged)
- [ ] Verify examples are realistic and helpful

### Test 10.3: Update Specification Templates
- [ ] Update spec templates to include "Proposed Changes" section placeholder
- [ ] Add instructions for marking current vs proposed behavior
- [ ] Add reminder to link to implementation plan
- [ ] Add reminder to update spec after implementation
- [ ] Verify templates guide users to follow inline proposal format

---

## Phase 11: Update Cross-References

### Test 11.1: Update CLAUDE.md References
- [ ] Update all doc paths in CLAUDE.md to new structure
- [ ] Update Quick Reference table with new paths
- [ ] Verify all links work
- [ ] Test links point to correct files

### Test 11.2: Update README.md References
- [ ] Update documentation links in README.md
- [ ] Add link to `docs/specs/README.md` as main entry point
- [ ] Update architecture documentation links
- [ ] Verify all links work

### Test 11.3: Add Cross-References in Specs
- [ ] Add "Related Specifications" section to each spec
- [ ] Link domain specs to screen specs where used
- [ ] Link feature specs to technical specs
- [ ] Link technical specs to implementation plans
- [ ] Verify all cross-references are bidirectional

---

## Phase 12: Create Documentation Index

### Test 12.1: Create Root Documentation Index
- [ ] Create `docs/README.md` as main documentation hub
- [ ] Add "Documentation Structure" section
- [ ] Add "Getting Started" guide for new developers
- [ ] Add "Getting Started" guide for AI agents
- [ ] Add quick links to most important specs

### Test 12.2: Add Documentation Map
- [ ] Create `docs/DOCUMENTATION_MAP.md` showing all docs
- [ ] Use tree structure to show hierarchy
- [ ] Add brief description for each document
- [ ] Add "Find Documentation By..." section (by feature, by screen, by technology)
- [ ] Verify map is comprehensive and accurate

### Test 12.3: Create AI Agent Guide
- [ ] Create `docs/AI_AGENT_GUIDE.md` for AI development workflow
- [ ] Document how to read specifications
- [ ] Document how to add inline proposals to specs (with examples)
- [ ] Document inline proposal format conventions
- [ ] Document how to write implementation plans (TDD checklist)
- [ ] Document how to update specs after implementation (merge proposals)
- [ ] Provide examples of complete workflow (propose → plan → implement → finalize)
- [ ] Add troubleshooting section for common issues

---

## Phase 13: Clean Up and Validation

### Test 13.1: Remove Empty Directories
- [ ] Remove `docs/concepts/` (if empty)
- [ ] Remove `docs/pages/` (if empty)
- [ ] Verify no empty directories remain

### Test 13.2: Update .gitignore (if needed)
- [ ] Check if any generated files in docs/ should be ignored
- [ ] Add patterns if necessary
- [ ] Verify git status shows expected changes

### Test 13.3: Validate All Links
- [ ] Run link checker on all markdown files
- [ ] Fix any broken internal links
- [ ] Fix any broken external links
- [ ] Verify all cross-references work

### Test 13.4: Create Migration Summary
- [ ] Create `docs/MIGRATION_SUMMARY.md` documenting changes
- [ ] List old path → new path mappings
- [ ] Document new documentation workflow
- [ ] Add "Breaking Changes" section if any
- [ ] Verify summary is complete

---

## Phase 14: Documentation Workflow Validation

### Test 14.1: Test Inline Proposal Workflow
- [ ] Select an existing spec file (e.g., `docs/specs/features/tag-system.md`)
- [ ] Add an inline proposed change using the defined format
- [ ] Mark current behavior clearly
- [ ] Mark proposed behavior with `**🔄 Proposed:**`
- [ ] Create corresponding implementation plan in `docs/plans/`
- [ ] Verify proposal is easy to understand in context
- [ ] Verify workflow is clear and complete
- [ ] Document any workflow improvements needed

### Test 14.2: Verify AI Agent Can Follow Workflow
- [ ] AI reads spec from `docs/specs/`
- [ ] AI understands current implementation and behavior
- [ ] AI can add inline proposed changes to spec file
- [ ] AI properly marks current behavior vs proposed changes
- [ ] AI can create TDD plan in `docs/plans/` referencing the spec
- [ ] AI can update spec after implementation (remove old, merge proposed)
- [ ] Verify workflow is AI-friendly and clear

### Test 14.3: Verify Human Can Follow Workflow
- [ ] Human can easily find relevant specifications
- [ ] Human can understand inline proposal format
- [ ] Human can read both current and proposed behavior side-by-side
- [ ] Human can understand how to create implementation plans
- [ ] Human can understand TDD checklist approach
- [ ] Human can update specs after implementation
- [ ] Verify documentation is human-readable and intuitive

---

## Success Criteria

### Documentation Structure
- [x] All specifications organized under `docs/specs/`
- [x] Clear separation: specs, plans, guides, reference
- [x] Each directory has comprehensive README
- [x] Consistent template for each document type
- [x] Inline proposal format clearly documented

### Workflow Support
- [x] Clear process for proposing changes inline within specs
- [x] TDD checklist approach enforced in plans
- [x] Specifications are single source of truth
- [x] Current and proposed behavior visible side-by-side
- [x] AI and humans can easily follow workflow

### Quality Metrics
- [x] All internal links work correctly
- [x] All cross-references are bidirectional
- [x] No duplicate documentation
- [x] All specs have metadata (status, last updated)
- [x] All plans follow TDD checklist format

### Usability
- [x] New developers can quickly understand app structure
- [x] AI agents can autonomously read and write specs
- [x] Documentation map provides clear overview
- [x] Quick reference available for common tasks

---

## Post-Refactoring Maintenance

### Regular Updates
- Update specifications when features change
- Merge inline proposals after implementation completion
- Archive old implementation plans quarterly
- Review specs for stale inline proposals monthly
- Keep implementation status current

### Documentation Reviews
- Quarterly review of specification accuracy
- Monthly scan for specs with pending inline proposals
- Ensure plans follow TDD checklist format
- Validate cross-references remain accurate
- Verify no orphaned "Current Behavior" / "Proposed Change" sections

---

## Rollback Plan

If refactoring causes issues:

1. **Immediate Rollback**:
   - Git revert to pre-refactoring commit
   - All old paths immediately available

2. **Partial Rollback**:
   - Keep new structure
   - Add symlinks from old paths to new paths
   - Gradually remove symlinks as references updated

3. **Forward Fix**:
   - Identify broken references
   - Update references to new structure
   - Continue with refactoring

---

## Notes

- This refactoring does NOT change any code, only documentation structure
- All existing implementation plans remain valid
- Old archived PRPs remain untouched in `docs/archived/prps/`
- New workflow is additive - old workflow still works during transition

### About Deleting implementation-status.md and recent-changes.md

Both `implementation-status.md` and `recent-changes.md` are **redundant** with the new documentation structure:

**Why Delete implementation-status.md:**
- Duplicates information that should be in individual feature/screen specs
- Requires manual maintenance and easily becomes stale
- Not a single source of truth (conflicts with spec files)
- Not actionable (doesn't guide implementation)

**Why Delete recent-changes.md:**
- Essentially a manual changelog (should be in CHANGELOG.md)
- Duplicates information from implementation plans
- "Recent" is relative and quickly becomes outdated
- Future roadmap mixed with historical changes (wrong organization)
- Requires constant manual updates

**Replaced By:**
1. **Status metadata in spec files** - Each spec tracks its own status and history
2. **`docs/ROADMAP.md`** - Future priorities and planned features only
3. **`docs/CHANGELOG.md`** - Historical record in standard format
4. **Implementation plans** - Already document all changes with full detail
5. **Spec status aggregation** - Can generate status reports from spec metadata

**Benefits:**
- Single source of truth per feature
- Status updates happen naturally when specs are updated
- No duplicate tracking required
- Standard changelog format (easier to maintain)
- More granular and accurate status information
- Clear separation: past (CHANGELOG) vs future (ROADMAP)

### About Inline Proposals

This plan uses an **inline proposal approach** instead of separate proposal files:

**Benefits:**
- Context is preserved - see current and proposed behavior side-by-side
- No duplicate files to maintain
- Easier to review what's changing
- Single source of truth remains in one file
- Simpler workflow for both AI and humans

**How It Works:**
1. Modify spec file directly, adding proposed changes inline
2. Use clear markers: `**Current Behavior:**` and `**🔄 Proposed:**`
3. Create implementation plan referencing the spec
4. After implementation, remove old behavior and merge proposed changes
5. Remove proposal markers to clean up spec

**Example:**
```markdown
## Tag System

**Current Behavior:**
Tags are stored in mixed case as user enters them.

**🔄 Proposed:**
Tags should be normalized to lowercase for consistency.
- Motivation: Prevent duplicate tags with different cases
- Impact: All tag storage and matching logic
- Breaking Changes: Existing tags need migration

**Implementation Plan:** [2025-10-19-tag-improvement.md](../plans/2025-10-19-tag-improvement.md)
```

After implementation:
```markdown
## Tag System

Tags are normalized to lowercase for consistency. This prevents
duplicate tags with different cases and ensures consistent matching.
```

---

## Estimated Effort

- **Phase 1-2**: 1 hour (directory structure + architecture)
- **Phase 3-4**: 1.5 hours (domain models + screens)
- **Phase 5-6**: 2 hours (features + technical specs)
- **Phase 7-8**: 1.5 hours (guidelines + reference)
- **Phase 9-10**: 1 hour (plans + inline proposal format)
- **Phase 11-12**: 2 hours (cross-references + indexes)
- **Phase 13-14**: 1.5 hours (validation + workflow testing)

**Total**: ~10.5 hours

---

## References

- [CLAUDE.md](../../CLAUDE.md)
- [README.md](../../README.md)
- [Existing plans](../plans/)
