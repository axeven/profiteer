# Wallet Concepts

## Wallet Types

There are 2 kinds of wallets in Profiteer: **Physical** and **Logical** wallets.

### Physical Wallets
Physical wallets represent real-world financial accounts and assets. Examples include:
- **Bank accounts** (checking, savings accounts)
- **Cash wallets** (physical cash on hand)
- **Investment accounts** (stocks, bonds portfolios)
- **Crypto wallets** (Bitcoin, Ethereum holdings)
- **Credit cards** (available credit limits)

**Characteristics:**
- Have actual monetary value
- Can receive real transactions
- Each has its own currency (USD, EUR, IDR, etc.)
- Independent balance tracking

### Logical Wallets
Logical wallets are virtual categorizations created on top of physical wallets for budgeting and allocation purposes. Examples include:
- **Monthly expense wallet** (allocated for monthly spending)
- **Emergency fund wallet** (reserved for emergencies)
- **Vacation savings wallet** (saving for trips)
- **Investment fund wallet** (money designated for investments)

**Characteristics:**
- Virtual allocations, not real accounts
- Help with budgeting and financial planning
- Based on physical wallet funds
- Cannot exceed total physical wallet amounts

## Example Scenario

Let's say you have 2 physical wallets:
- **DBS Bank Account**: $1,500 USD
- **CIMB Bank Account**: $1,500 USD
- **Total Physical**: $3,000 USD

On top of these, you create 3 logical wallets:
- **Monthly Expenses**: $800 USD (for regular spending)
- **Emergency Fund**: $1,000 USD (for unexpected expenses)  
- **Savings**: $1,200 USD (for future goals)
- **Total Logical**: $3,000 USD

## Wallet Properties

Each wallet (physical and logical) has the following properties:

### Core Properties
- **Name**: Unique identifier (e.g., "DBS Checking Account", "Emergency Fund")
- **Type**: "Physical" or "Logical"
- **Currency**: Currency code (USD, EUR, IDR, GBP, JPY, CAD, AUD)
- **Balance**: Current balance amount
- **Initial Balance**: Starting balance (excluded from transaction analytics)

### Balance Calculation
- **Current Balance** = Initial Balance + Transaction-based changes
- **Transaction Balance** = Current Balance - Initial Balance (used for analytics)

## Implementation Features

### Validation Rules
- **Unique Names**: No two wallets can have the same name (case-insensitive)
- **Required Fields**: Name, wallet type, currency, and initial balance are required
- **Numeric Validation**: Balance amounts must be valid numbers

### User Interface
- **Thousands Separators**: All amounts display with proper formatting (1,234.56)
- **Currency Display**: Shows currency code alongside amounts
- **Type Indication**: Clearly shows whether wallet is Physical or Logical
- **Edit Capability**: Users can modify wallet properties after creation

### Analytics Integration
- **Initial Balance Exclusion**: Initial balances don't affect transaction analytics
- **Transaction Tracking**: Only actual income/expense transactions impact analytics
- **Balance Integrity**: System tracks both setup amounts and transaction-based changes

## Future Enhancements

### Planned Features
- **Logical Wallet Constraints**: Enforce that logical wallet totals cannot exceed physical wallet totals
- **Wallet Relationships**: Link logical wallets to specific physical wallets
- **Transfer Transactions**: Move money between wallets
- **Multi-wallet Logical Allocations**: Logical wallets spanning multiple physical wallets

This dual-wallet system provides flexibility for both real-world account tracking and virtual budgeting, making Profiteer suitable for comprehensive personal finance management.