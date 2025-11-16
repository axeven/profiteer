# Dual Wallet System

**Status**: ✅ Implemented
**Last Updated**: 2025-11-16
**Implementation Plan**: Multiple plans (see architecture history)

## Purpose

The dual wallet system separates real-world financial accounts (Physical wallets) from virtual budget allocations (Logical wallets), enabling sophisticated budgeting without affecting underlying physical account balances.

## Requirements

### Physical Wallets
- Represent tangible financial accounts (bank accounts, cash, cryptocurrency wallets)
- Each operates in its native currency with independent balance tracking
- Support multiple physical forms: FIAT_CURRENCY, PRECIOUS_METAL, CRYPTOCURRENCY
- Direct transaction impacts with real-time balance updates

### Logical Wallets
- Virtual categorizations for budgeting and allocation purposes
- Examples: "Monthly Expenses", "Emergency Fund", "Vacation Savings"
- Reference physical wallets through `affectedWalletIds` system
- Enable budgeting without affecting physical accounts

### Balance Integrity
**Critical Business Rule**: Sum of all logical wallet balances MUST equal sum of all physical wallet balances

- Real-time validation on every transaction
- Dashboard alerts when discrepancies detected
- Unallocated balance tracking and display

## Related Specifications

- [Architecture Overview](../architecture/overview.md) - System design
- [Wallet Domain Model](../domain/wallets.md) - Detailed wallet specification
- [Transaction Domain Model](../domain/transactions.md) - Transaction rules
- [Database Schema](../technical/database-schema.md) - Data structure

---

**Last Reviewed**: 2025-11-16
