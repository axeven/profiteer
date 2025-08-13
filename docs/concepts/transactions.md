# Transaction System

## Overview

Profiteer supports three types of financial transactions that manage wallet balances:

1. **Income Transactions** - Increase wallet balances
2. **Expense Transactions** - Decrease wallet balances  
3. **Transfer Transactions** - Move balance between wallets

## Transaction Types

### Income Transactions
- **Purpose**: Record money coming into the user's financial system
- **Effect**: Increases wallet balance(s)
- **Wallet Selection**: User selects exactly one Physical wallet and one Logical wallet
- **Examples**: Salary, investment returns, gifts received, refunds

### Expense Transactions
- **Purpose**: Record money going out of the user's financial system
- **Effect**: Decreases wallet balance(s)
- **Wallet Selection**: User selects exactly one Physical wallet and one Logical wallet
- **Examples**: Food purchases, utility bills, entertainment, transportation

### Transfer Transactions
- **Purpose**: Move money between different wallets
- **Effect**: Decreases source wallet, increases destination wallet
- **Wallet Selection**: User selects one source wallet and one destination wallet
- **Validation Requirements**:
  - Source and destination wallets must have the same currency
  - Source and destination wallets must have the same wallet type (Physical or Logical)
  - Source and destination cannot be the same wallet
- **Examples**: Moving money between bank accounts, transferring between investment accounts

## Transaction Properties

### Core Fields
- **Title**: Descriptive name for the transaction
- **Amount**: Monetary value (always positive in data model, signed based on transaction type)
- **Transaction Date**: User-specified date when the transaction occurred
- **Tags**: Multiple categorization labels for organizing and filtering transactions

### Wallet References
- **Income/Expense**: Uses `affectedWalletIds` array containing exactly two wallet IDs (one Physical, one Logical)
- **Transfer**: Uses `sourceWalletId` and `destinationWalletId` for direct wallet-to-wallet movement
- **Backward Compatibility**: Maintains `walletId` field for legacy single-wallet transactions

### Tagging System

#### Tag Structure
- **Multiple Tags**: Each transaction can have multiple tags separated by commas
- **Default Handling**: Transactions without user-defined tags are automatically labeled "Untagged"
- **Historical Integration**: Tags from existing transactions populate auto-completion suggestions

#### Tag Auto-completion
- **Trigger**: Activates after typing 3 or more characters
- **Source**: Based on historical tags from user's previous transactions
- **Limit**: Shows up to 5 relevant suggestions
- **Case Insensitive**: Matches tags regardless of capitalization

## Data Model

### Current Transaction Model
```kotlin
data class Transaction(
    val id: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val category: String = "Untagged", // Legacy field, replaced by tags
    val type: TransactionType = TransactionType.EXPENSE,
    val walletId: String = "", // Legacy single wallet reference
    val affectedWalletIds: List<String> = emptyList(), // Current multi-wallet support
    val sourceWalletId: String = "", // For transfer transactions
    val destinationWalletId: String = "", // For transfer transactions
    val tags: List<String> = emptyList(), // Current tagging system
    val transactionDate: Date? = null,
    val userId: String = "",
    val createdAt: Date? = null,
    val updatedAt: Date? = null
)
```

### Transaction Types Enum
```kotlin
enum class TransactionType {
    INCOME, EXPENSE, TRANSFER
}
```

## Wallet Balance Updates

### Income/Expense Transactions
- **Balance Effect**: Amount is added to (Income) or subtracted from (Expense) all affected wallets
- **Multi-wallet Support**: Both selected Physical and Logical wallets are updated simultaneously
- **Currency Consistency**: All affected wallets maintain their respective currencies

### Transfer Transactions
- **Source Wallet**: Balance decreased by transfer amount
- **Destination Wallet**: Balance increased by transfer amount
- **Atomic Operation**: Both updates occur together to maintain data consistency

## Validation Rules

### All Transaction Types
- Title must not be blank
- Amount must be a valid positive number
- Transaction date must be specified
- User must be authenticated

### Income/Expense Specific
- Exactly one Physical wallet must be selected
- Exactly one Logical wallet must be selected
- Selected wallets can have different currencies

### Transfer Specific
- Source and destination wallets must be different
- Both wallets must have the same currency
- Both wallets must have the same wallet type (Physical or Logical)
- Source wallet must have sufficient balance (validated at UI level)

## Historical Context

### Evolution from Categories to Tags
- **Previous System**: Single "category" field per transaction
- **Current System**: Multiple "tags" field with auto-completion
- **Migration**: Existing transactions maintain category field for backward compatibility
- **Default Behavior**: New transactions without tags receive "Untagged" label

### Wallet Selection Evolution
- **Previous System**: Multiple wallet selection per transaction type
- **Current System**: Single wallet selection (one Physical + one Logical for Income/Expense)
- **Rationale**: Simplified user experience and clearer transaction attribution

## User Interface

### Transaction Creation
- **Type Selection**: Radio buttons for Income, Expense, Transfer
- **Wallet Selection**: Separate dropdowns for Physical and Logical wallets (Income/Expense only)
- **Tag Input**: Text field with auto-completion after 3+ characters
- **Validation**: Real-time feedback for invalid selections

### Transaction Editing
- **Type Restriction**: Transaction type cannot be changed after creation
- **Wallet Modification**: Users can change selected wallets while maintaining validation rules
- **Tag Updates**: Full tag editing with same auto-completion features
- **Balance Reconciliation**: Wallet balances are properly adjusted when transactions are modified

### Transaction Display
- **Home Screen**: Recent transactions with tags displayed
- **Tag Format**: Multiple tags shown as comma-separated list
- **Fallback**: "Untagged" displayed for transactions without tags
- **Currency Display**: Amount shown in transaction's respective currency