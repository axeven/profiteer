# Profiteer Wallet System Documentation

## Overview

Profiteer implements a sophisticated dual-wallet system that separates real-world financial accounts from virtual budget allocations. This approach enables users to track both their actual financial assets and their budgeting strategies without compromising the integrity of their financial data.

## Wallet Architecture

### Wallet Types

**Physical Wallets**
Physical wallets represent tangible financial accounts and assets that exist in the real world. These form the foundation of all financial operations within Profiteer.

**Examples**:
- Bank accounts (checking, savings, money market)
- Cash wallets (physical currency holdings)
- Investment accounts (stocks, bonds, retirement funds)
- Cryptocurrency wallets (Bitcoin, Ethereum holdings)
- Credit cards (representing available credit limits)
- Precious metal holdings (physical gold, silver)

**Characteristics**:
- Have actual monetary value that can be verified externally
- Can receive and send real-world transactions
- Each operates in its native currency (USD, EUR, IDR, BTC, GOLD, etc.)
- Independent balance tracking with real-time updates
- Form the authoritative source of truth for total wealth

**Logical Wallets**
Logical wallets are virtual categorizations created for budgeting, allocation, and financial planning purposes. They represent intended uses for money rather than actual financial accounts.

**Examples**:
- Monthly expense allocation (budgeted amount for regular spending)
- Emergency fund allocation (money reserved for unexpected events)
- Vacation savings (funds designated for travel)
- Investment fund (money earmarked for future investments)
- Holiday shopping budget (seasonal spending allocation)

**Characteristics**:
- Virtual allocations without direct real-world counterparts
- Enable sophisticated budgeting and financial planning
- Derive their value from underlying physical wallet funds
- Cannot exceed total physical wallet amounts (critical business rule)
- Allow for unallocated balance tracking

## Physical Form Classifications

### FIAT_CURRENCY
Traditional government-issued currencies with standard decimal precision.

**Supported Currencies**:
- **USD** (United States Dollar) - 2 decimal places
- **EUR** (Euro) - 2 decimal places
- **GBP** (British Pound Sterling) - 2 decimal places
- **JPY** (Japanese Yen) - 0 decimal places
- **CAD** (Canadian Dollar) - 2 decimal places
- **AUD** (Australian Dollar) - 2 decimal places
- **IDR** (Indonesian Rupiah) - 0 decimal places

**Features**:
- Standard currency formatting with appropriate symbols
- Thousands separator support (1,234.56)
- Localized number formatting based on currency conventions

### PRECIOUS_METAL
Physical precious metals with weight-based pricing and higher precision requirements.

**Supported Metals**:
- **GOLD** - Priced per gram with 3 decimal precision (123.456g)

**Features**:
- Weight-based valuation system
- Higher precision for accurate precious metal tracking
- Conversion support to local currency for aggregated displays
- Suitable for physical precious metal investments

### CRYPTOCURRENCY
Digital currencies with extremely high precision requirements for fractional ownership.

**Supported Cryptocurrencies**:
- **BTC** (Bitcoin) - 8 decimal precision for satoshi-level accuracy (1.23456789 BTC)

**Features**:
- Satoshi-level precision (0.00000001 BTC minimum unit)
- Support for fractional cryptocurrency ownership
- Integration with fiat currency conversion for portfolio aggregation
- Future-ready for additional cryptocurrency support

## Wallet Properties

### Core Properties
Each wallet maintains the following essential attributes:

**Identification & Categorization**:
- **Name**: Unique identifier within user account (e.g., "DBS Checking Account", "Emergency Fund")
- **Wallet Type**: Classification as "Physical" or "Logical"
- **Physical Form**: Enum value (FIAT_CURRENCY, PRECIOUS_METAL, CRYPTOCURRENCY)
- **Currency**: Native currency code corresponding to the physical form

**Financial Data**:
- **Current Balance**: Total current balance including all changes
- **Initial Balance**: Starting balance from wallet creation (excluded from analytics)
- **Transaction Balance**: Computed as (Current Balance - Initial Balance) for analytics

**Metadata**:
- **Creation Date**: Timestamp of wallet creation
- **Last Updated**: Timestamp of most recent balance or detail modification
- **User ID**: Reference to owning user account

### Balance Calculation System

**Current Balance Components**:
```
Current Balance = Initial Balance + Sum(Income Transactions) - Sum(Expense Transactions) ± Sum(Transfer Transactions)
```

**Transaction Balance (Analytics)**:
```
Transaction Balance = Current Balance - Initial Balance
```

The transaction balance represents the net change from actual financial activity, excluding the initial setup amount. This separation enables accurate financial analytics that focus on user behavior rather than starting wealth.

## Business Rules & Constraints

### Balance Integrity System

**Primary Constraint**: 
The sum of all logical wallet balances must equal the sum of all physical wallet balances at all times.

**Mathematical Representation**:
```
Sum(Physical Wallet Balances) = Sum(Logical Wallet Balances) + Unallocated Balance
```

**Validation Mechanisms**:
- **Real-time Checking**: Balance integrity verified on every transaction
- **Dashboard Alerts**: Home screen displays warnings when discrepancies detected
- **Unallocated Balance Tracking**: System calculates and displays unallocated amounts
- **Transaction Blocking**: Critical operations may be blocked until balance integrity restored

### Wallet Management Rules

**Naming Constraints**:
- Wallet names must be unique within a user's account (case-insensitive)
- Names cannot be blank or contain only whitespace
- Maximum length restrictions for display consistency

**Currency Consistency**:
- Physical form must match supported currency type
- Currency cannot be changed after wallet creation (data integrity)
- All transactions maintain native currency precision

**Type Immutability**:
- Wallet type (Physical/Logical) cannot be changed after creation
- Ensures consistent categorization throughout wallet lifecycle
- Prevents accidental miscategorization of financial data

## Multi-Currency Support

### Currency Display Modes

**Native Currency Display**:
- Each wallet displays amounts in its original currency
- Preserves exact precision and formatting
- Ideal for detailed wallet management

**Converted Currency Display**:
- Amounts converted to user's default currency
- Enables portfolio aggregation and comparison
- Uses current conversion rates with fallback mechanisms

### Conversion Rate Integration

**Rate Resolution Hierarchy**:
1. **Monthly Rates**: Time-specific rates take highest precedence
2. **Default Rates**: Permanent rates used when monthly unavailable
3. **Inverse Calculation**: System computes reverse rates automatically
4. **Warning System**: Alerts when no conversion rates available

**Home Screen Aggregation**:
- All wallet balances converted to default currency for total portfolio value
- Missing conversion rates trigger user warnings
- Maintains accuracy through proper rate management

## User Interface Features

### Wallet Creation & Management

**Creation Workflow**:
- **Physical Wallets**: Name, physical form, currency, initial balance
- **Logical Wallets**: Name, currency (matches intended physical wallet), initial allocation

**Validation Features**:
- **Real-time Validation**: Immediate feedback for invalid inputs
- **Uniqueness Checking**: Name collision detection during entry
- **Balance Warnings**: Alerts for logical wallet allocations exceeding physical totals

**Display Features**:
- **Thousands Separators**: Proper formatting (1,234.56) for all amounts
- **Currency Symbols**: Native symbols displayed with amounts
- **Type Indicators**: Clear visual distinction between Physical and Logical wallets
- **Balance Components**: Separate display of initial and transaction-based balances

### Wallet List Interface

**Dual-Section Layout**:
- **Physical Wallets Section**: All real-world accounts listed above
- **Logical Wallets Section**: All virtual allocations listed below
- **Unallocated Balance**: Displayed when Physical and Logical totals don't match

**Balance Display Options**:
- **Toggle Support**: Switch between native and converted currency display
- **Real-time Updates**: Live synchronization with transaction changes
- **Precision Control**: Appropriate decimal places for each currency type

### Advanced Features

**Wallet Detail Analysis**:
- **Transaction History**: Filtered view of wallet-specific transactions
- **Monthly Breakdown**: Income/expense analysis for specific time periods
- **Balance Trend**: Historical balance changes over time
- **Currency Conversion**: Real-time conversion rate display and calculations

**Balance Integrity Monitoring**:
- **Discrepancy Detection**: Automatic identification of balance mismatches
- **Resolution Guidance**: Clear instructions for correcting imbalances
- **Visual Indicators**: Color-coded warnings for balance integrity issues

## Analytics Integration

### Transaction Impact Tracking

**Balance Change Attribution**:
- **Income Attribution**: Track which transactions increased balances
- **Expense Attribution**: Monitor spending impact on wallet balances  
- **Transfer Attribution**: Account for inter-wallet movement effects

**Exclusion of Initial Balances**:
- Analytics calculations use transaction balance only
- Initial balances don't skew spending/earning metrics
- Enables accurate financial behavior analysis

### Reporting Capabilities

**Wallet-Specific Reports**:
- Monthly income/expense breakdown per wallet
- Spending category analysis using transaction tags
- Balance growth/decline trends over time
- Currency conversion impact on portfolio performance

**Portfolio-Level Analysis**:
- Total wealth calculation across all physical wallets
- Budget allocation efficiency (logical vs. physical balance ratios)
- Cross-currency portfolio performance
- Unallocated balance trends and recommendations

## Future Enhancement Roadmap

### Planned Features

**Enhanced Logical Wallet Capabilities**:
- **Multi-Physical Allocation**: Logical wallets spanning multiple physical wallets
- **Percentage-Based Allocation**: Automatic rebalancing based on percentage rules
- **Goal Tracking**: Progress monitoring for savings and investment goals
- **Smart Recommendations**: AI-driven suggestions for optimal allocation

**Advanced Currency Features**:
- **Automatic Rate Updates**: Integration with real-time exchange rate APIs
- **Historical Rate Tracking**: Time-series analysis of currency performance
- **Multi-Currency Reporting**: Comprehensive reports in multiple currencies
- **Hedging Strategies**: Support for currency risk management

**Integration Capabilities**:
- **Bank Account Sync**: Automatic balance updates from financial institutions
- **Investment Platform Integration**: Real-time portfolio value updates
- **Cryptocurrency Exchange Sync**: Live crypto wallet balance synchronization
- **Tax Preparation Export**: Structured data export for tax software compatibility

## Implementation Best Practices

### Data Consistency

**Transaction Atomicity**:
- All wallet balance updates occur within database transactions
- Rollback mechanisms ensure data integrity on failures
- Audit trails maintain complete change history

**Validation Layers**:
- **Client-side**: Immediate user feedback and basic validation
- **Business Logic**: Comprehensive rule enforcement in ViewModels
- **Database**: Final validation through Firestore security rules

### Performance Optimization

**Efficient Querying**:
- Composite indexes for wallet-specific transaction queries
- Pagination support for large transaction histories
- Optimized balance calculation algorithms

**Real-time Updates**:
- Firestore listeners for live balance synchronization
- Minimal data transfer through targeted field updates
- Offline support with conflict resolution

### Security Considerations

**Data Protection**:
- Complete user data isolation through Firestore subcollections
- Authentication-based access control for all wallet operations
- Secure handling of sensitive financial information

**Input Sanitization**:
- Comprehensive validation of all wallet creation and modification inputs
- Protection against injection attacks and data corruption
- Secure error handling that doesn't expose sensitive information

This comprehensive wallet system provides the foundation for sophisticated personal finance management while maintaining data integrity, security, and user experience excellence.