# Profiteer Multi-Currency System Documentation

## Overview

Profiteer implements a sophisticated multi-currency system that supports diverse financial instruments including traditional fiat currencies, precious metals, and cryptocurrencies. The system provides comprehensive currency conversion capabilities with intelligent rate management and flexible display options.

## Supported Currency Types

### Fiat Currencies (FIAT_CURRENCY)

Traditional government-issued currencies with standardized decimal precision and formatting conventions.

**Supported Currencies**:

**Major International Currencies**:
- **USD** (United States Dollar) - 2 decimal precision, symbol: $
- **EUR** (Euro) - 2 decimal precision, symbol: €
- **GBP** (British Pound Sterling) - 2 decimal precision, symbol: £
- **JPY** (Japanese Yen) - 0 decimal precision, symbol: ¥
- **CAD** (Canadian Dollar) - 2 decimal precision, symbol: C$
- **AUD** (Australian Dollar) - 2 decimal precision, symbol: A$

**Regional Currencies**:
- **IDR** (Indonesian Rupiah) - 0 decimal precision, symbol: Rp

**Formatting Characteristics**:
- **Precision Control**: Automatic decimal place management based on currency conventions
- **Thousands Separators**: Locale-appropriate formatting (1,234.56 or 1.234,56)
- **Symbol Placement**: Correct positioning of currency symbols (prefix/suffix)
- **Number Formatting**: Regional number format support for international users

### Precious Metals (PRECIOUS_METAL)

Physical precious metals with weight-based pricing and enhanced precision for accurate investment tracking.

**Supported Metals**:

**Gold (GOLD)**:
- **Pricing Unit**: Per gram (g)
- **Precision**: 3 decimal places (123.456g)
- **Display Format**: Weight-based with unit suffix
- **Use Cases**: Physical gold investments, jewelry valuation, precious metal portfolios

**Features**:
- **Weight-Based Valuation**: Accurate tracking of physical precious metal holdings
- **High Precision**: Three decimal places for precise weight measurements
- **Investment Grade**: Suitable for professional precious metal portfolio management
- **Conversion Support**: Integration with fiat currencies for portfolio aggregation

**Future Expansion**:
- Silver (SILVER) - Planned support for silver investments
- Platinum (PLATINUM) - High-value precious metal tracking
- Palladium (PALLADIUM) - Industrial precious metal support

### Cryptocurrencies (CRYPTOCURRENCY)

Digital currencies with extreme precision requirements for fractional ownership and satoshi-level accuracy.

**Supported Cryptocurrencies**:

**Bitcoin (BTC)**:
- **Precision**: 8 decimal places (1.23456789 BTC)
- **Minimum Unit**: 0.00000001 BTC (1 satoshi)
- **Display Format**: Full precision with BTC suffix
- **Use Cases**: Cryptocurrency investments, DeFi tracking, digital asset management

**Features**:
- **Satoshi-Level Precision**: Support for the smallest Bitcoin unit (0.00000001 BTC)
- **Fractional Ownership**: Accurate tracking of partial cryptocurrency holdings
- **High-Precision Mathematics**: Robust decimal arithmetic for cryptocurrency calculations
- **Integration Ready**: Architecture supports additional cryptocurrency additions

**Future Expansion**:
- Ethereum (ETH) - Smart contract platform token
- Stablecoins (USDC, USDT) - Dollar-pegged cryptocurrency support
- Major Altcoins - Popular alternative cryptocurrency integration

## Currency Conversion System

### Conversion Rate Architecture

Profiteer implements a sophisticated dual-rate system that provides both permanentpermanent and time-specific currency conversion capabilities.

**Rate Type Classifications**:

**Default Rates**:
- **Purpose**: Permanent conversion rates for consistent long-term calculations
- **Usage**: Baseline conversion when time-specific rates unavailable
- **Persistence**: Remain valid until manually updated by user
- **Applications**: General portfolio aggregation, basic currency display

**Monthly Rates**:
- **Purpose**: Time-specific rates for accurate historical conversions
- **Usage**: Historical accuracy for specific time periods
- **Format**: YYYY-MM format (e.g., "2024-12" for December 2024)
- **Applications**: Historical analysis, period-specific reporting, tax calculations

### Rate Resolution Hierarchy

The system employs an intelligent rate resolution system that prioritizes accuracy and availability:

**1. Monthly Rates (Highest Priority)**:
- System first searches for rates specific to the transaction's month/year
- Provides maximum historical accuracy for time-sensitive calculations
- Enables precise portfolio valuations for specific time periods

**2. Default Rates (Fallback)**:
- Used when monthly rates are unavailable for the specific time period
- Provides consistent baseline conversion for general calculations
- Maintains portfolio aggregation even with incomplete rate data

**3. Inverse Rate Calculation (Automatic)**:
- System automatically calculates reverse rates when direct rates unavailable
- Enables bi-directional conversion with minimal rate configuration
- Mathematical accuracy maintained through proper inverse calculations

**4. Warning System (User Alert)**:
- Displays alerts when no conversion rates available for currency pairs
- Prevents inaccurate portfolio calculations due to missing rate data
- Guides users to configure necessary conversion rates

### Conversion Rate Management

**User-Configurable Rates**:
- **Complete Control**: Users set and manage all conversion rates
- **Flexible Updates**: Rates can be updated anytime for accuracy
- **Historical Preservation**: Previous rates maintained for historical calculations

**Rate Validation**:
- **Positive Values**: All rates must be positive numbers greater than zero
- **Format Validation**: Monthly rates require valid YYYY-MM format
- **Duplicate Prevention**: System prevents duplicate rates for same currency pair and time period

**Active Status Management**:
- **Rate Activation**: Only active rates participate in conversion calculations
- **Historical Deactivation**: Rates can be deactivated without deletion
- **Bulk Management**: Efficient tools for managing multiple currency rates

## Multi-Currency Display System

### Display Mode Options

**Native Currency Display**:
- **Precision Preservation**: Shows amounts in original wallet currency
- **Exact Values**: Maintains precise decimal representation
- **Currency Context**: Users see actual account balances without conversion artifacts
- **Use Cases**: Detailed wallet management, account reconciliation, precision-critical operations

**Converted Currency Display**:
- **Portfolio Aggregation**: All amounts converted to user's default currency
- **Unified View**: Consistent currency presentation across entire portfolio
- **Comparison Enabled**: Easy comparison between wallets with different currencies
- **Use Cases**: Portfolio overview, financial planning, aggregate reporting

### Home Screen Aggregation

**Total Portfolio Calculation**:
- **Multi-Currency Summation**: Aggregates all wallet balances regardless of native currency
- **Real-Time Conversion**: Uses current conversion rates for up-to-date totals
- **Missing Rate Handling**: Displays warnings when conversion rates unavailable
- **Accuracy Maintenance**: Preserves precision throughout conversion process

**Balance Display Features**:
- **Primary Total**: Main portfolio value in user's default currency
- **Individual Contributions**: Shows how each wallet contributes to total
- **Conversion Transparency**: Users can see conversion rates and calculations
- **Warning Integration**: Clear alerts for missing or outdated conversion rates

### Currency-Specific Formatting

**Precision Management**:
- **Fiat Currencies**: Standard 2-decimal precision (except JPY, IDR: 0 decimals)
- **Precious Metals**: 3-decimal precision for accurate weight representation
- **Cryptocurrencies**: 8-decimal precision for satoshi-level accuracy

**Display Formatting**:
- **Symbol Integration**: Appropriate currency symbols displayed with amounts
- **Thousands Separators**: Locale-appropriate number formatting
- **Decimal Alignment**: Consistent decimal place alignment across currencies
- **Unit Suffixes**: Clear unit indicators for precious metals and cryptocurrencies

## User Interface Integration

### Currency Selection

**Wallet Creation**:
- **Physical Form Selection**: Dropdown for FIAT_CURRENCY, PRECIOUS_METAL, CRYPTOCURRENCY
- **Currency Matching**: Currency options filtered based on selected physical form
- **Validation**: Real-time validation of form/currency compatibility

**Currency Rate Management**:
- **Intuitive Interface**: User-friendly rate entry and management screens
- **Conversion Preview**: Real-time preview of conversion calculations
- **Bulk Entry**: Efficient interfaces for managing multiple currency rates
- **Historical View**: Access to previous rates and time-based changes

### Display Toggle Features

**Global Currency Toggle**:
- **System-Wide Setting**: Single toggle affects all currency displays
- **Persistent Preference**: User choice remembered across sessions
- **Real-Time Updates**: Immediate display refresh when toggle changed

**Context-Sensitive Display**:
- **Wallet Details**: Option to view individual wallets in native or converted currency
- **Transaction Views**: Currency context preserved in transaction displays
- **Report Generation**: Currency selection for reports and exports

## Conversion Rate Integration

### Default Currency System

**User Configuration**:
- **Onboarding Selection**: Default currency chosen during initial setup
- **Modification Support**: Users can change default currency after setup
- **Global Impact**: Default currency affects all aggregation and conversion displays

**System Behavior**:
- **Automatic Conversion**: All non-default currencies converted for aggregation
- **Preservation of Original**: Native currencies maintained in underlying data
- **Consistency**: Uniform application of default currency across application

### Rate Storage & Retrieval

**Database Schema**:
- **Efficient Storage**: Optimized storage format for conversion rates
- **Fast Retrieval**: Indexed queries for real-time conversion calculations
- **Historical Access**: Complete history of rate changes maintained

**Caching Strategy**:
- **Frequently Used Rates**: Commonly accessed rates cached for performance
- **Real-Time Updates**: Cache invalidation on rate modifications
- **Offline Support**: Cached rates available during network disconnections

## Business Logic & Validation

### Currency Consistency Rules

**Wallet Currency Immutability**:
- **Creation Lock**: Currency cannot be changed after wallet creation
- **Data Integrity**: Prevents currency confusion in historical data
- **Transaction Consistency**: Maintains currency context throughout wallet lifecycle

**Transfer Validation**:
- **Currency Matching**: Transfer transactions require identical source/destination currencies
- **Type Consistency**: Additional validation for wallet type matching
- **Error Prevention**: UI prevents invalid currency combinations

### Conversion Accuracy

**Mathematical Precision**:
- **High-Precision Arithmetic**: Robust decimal calculations for accurate conversions
- **Rounding Rules**: Consistent rounding strategies across all calculations
- **Error Minimization**: Precision-preserving algorithms reduce conversion errors

**Rate Validation**:
- **Positive Rates**: All conversion rates must be positive numbers
- **Reasonable Ranges**: Validation against obviously incorrect rates
- **User Confirmation**: Significant rate changes require user confirmation

## Security & Data Integrity

### Rate Management Security

**User Data Isolation**:
- **Private Rates**: Each user's conversion rates completely isolated
- **Access Control**: Rate modifications require proper authentication
- **Audit Trail**: All rate changes logged with timestamps and user attribution

**Validation Security**:
- **Input Sanitization**: Comprehensive validation of rate inputs
- **SQL Injection Prevention**: Safe data handling prevents malicious inputs
- **Error Handling**: Secure error messages don't expose sensitive information

### Financial Data Protection

**Conversion Integrity**:
- **Transaction Atomicity**: Currency-related calculations occur within database transactions
- **Rollback Protection**: Failed conversions trigger complete rollback
- **Consistency Verification**: Regular checks ensure conversion accuracy

**Privacy Considerations**:
- **Rate Privacy**: User's currency rates not shared between accounts
- **Balance Protection**: Conversion calculations don't expose other users' data
- **Encryption**: Sensitive financial data encrypted at rest and in transit

## Future Enhancements

### Planned Currency Support

**Additional Fiat Currencies**:
- **Regional Expansion**: Support for additional regional currencies
- **Emerging Markets**: Currencies from developing economies
- **Historical Currencies**: Support for deprecated currencies in historical data

**Cryptocurrency Expansion**:
- **Major Altcoins**: Ethereum, Litecoin, and other established cryptocurrencies  
- **Stablecoins**: USDC, USDT, and other dollar-pegged digital currencies
- **DeFi Tokens**: Support for decentralized finance protocol tokens

**Commodity Support**:
- **Additional Metals**: Silver, platinum, palladium for comprehensive precious metal portfolios
- **Energy Commodities**: Oil, gas, and renewable energy certificates
- **Agricultural Commodities**: Support for commodity-based investments

### Advanced Features

**Real-Time Rate Integration**:
- **API Connectivity**: Integration with real-time exchange rate services
- **Automatic Updates**: Scheduled updates of currency conversion rates
- **Rate Alerts**: Notifications when significant rate changes occur

**Historical Analysis**:
- **Rate Trends**: Historical rate performance analysis and visualization
- **Volatility Tracking**: Risk assessment based on currency volatility
- **Prediction Models**: AI-driven currency rate forecasting

**Advanced Conversion Features**:
- **Cross-Currency Chains**: Multi-step conversions through intermediate currencies
- **Hedging Support**: Tools for currency risk management
- **Tax Implications**: Conversion tracking for tax reporting requirements

This comprehensive multi-currency system provides the foundation for sophisticated international financial management while maintaining accuracy, flexibility, and user experience excellence.