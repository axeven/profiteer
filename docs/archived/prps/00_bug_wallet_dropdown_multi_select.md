# Product Requirement Prompt (PRP) Template

## 1. Overview
**Brief Description**: Wallet dropdown selection on create transaction dialog should be single select instead of multi select

**Estimated Complexity**: Simple

## 2. Context & Background
**Current State**: Currently when we create income or expense transaction we can select multiple physical wallets affected and multiple logical wallet affected.

**Problem Statement**: Each transaction should only affect exactly one physical wallet and one logical wallet for clarity.

**Business Value**: Provides clarity about which wallet being affected by the transaction. Typically when a transaction happened, user should already has clear idea which wallet does it affect. If a transaction involves multiple wallets, then user need to breakdown that transaction into smaller pieces that only affect a single wallet of source and/or a single wallet of destination.

## 4. Implementation Guidance

**Related Documentation**:
- `docs/concepts/transactions.md`

## 5. Acceptance Criteria

### 5.1 Definition of Done
- [ ] All functional requirements implemented
- [ ] Code follows project conventions and patterns
- [ ] Unit tests written and passing
- [ ] Integration tests written and passing
- [ ] UI matches design specifications
- [ ] Documentation updated

### 5.2 Test Scenarios
**Happy Path**:
1. User open the app
2. User create physical wallet "Bank Account A" with balance 100
3. User create logical wallet "Expense" with balance 100
4. User open create income transaction dialog, we expect user can only select exactly one physical wallet and one logical wallet

