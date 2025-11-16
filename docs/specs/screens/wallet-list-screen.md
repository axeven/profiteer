# Wallet List Page

## Overview

The Wallet List Page provides comprehensive wallet management functionality, allowing users to view, create, and manage both Physical and Logical wallets. This page serves as the central hub for all wallet-related operations and provides critical balance insights.

## Page Navigation

- **Access**: Navigable from the home page by clicking on the balance card
- **Return**: Back navigation returns to the home page
- **Integration**: Seamlessly integrated with the main navigation flow

## Layout Structure

### Default View: Physical Wallets
- **Primary Display**: Shows all user's Physical wallets by default
- **Sorting**: Wallets ordered by balance in descending order (highest balance first)
- **Currency Display**: All balances shown in user's default currency with automatic conversion

### Alternative View: Logical Wallets
- **Toggle View**: User can switch to view Logical wallets
- **Unallocated Balance Display**: When viewing Logical wallets, prominently displays unallocated balance
- **Calculation**: Unallocated balance = Sum of Physical wallet balances - Sum of Logical wallet balances
- **Color Coding**: 
  - Green background for positive unallocated balance
  - Red background for negative unallocated balance (overallocation)

## Wallet Display

### Wallet Card Information
Each wallet is displayed in a card format containing:
- **Wallet Name**: User-defined wallet identifier
- **Wallet Type**: "Physical" or "Logical" designation
- **Currency**: Wallet's native currency (USD, EUR, GBP, JPY, CAD, AUD, IDR, GOLD, BTC)
- **Balance**: Current wallet balance with proper currency formatting
- **Visual Indicators**: Distinct styling for different wallet types

### Balance Formatting
- **Currency Symbol**: Appropriate symbol displayed before amount
- **Number Formatting**: Thousands separators and appropriate decimal places
- **Conversion**: Real-time conversion to default currency for display
- **Precision**: Cryptocurrency balances show up to 8 decimal places

## Wallet Creation

### Create New Wallet Feature
- **Access**: Prominent "Create New Wallet" button/action
- **Form Fields**:
  - **Wallet Name**: Text input with uniqueness validation across user's wallets
  - **Wallet Type**: Radio button selection (Physical or Logical)
  - **Currency Selection**: Dropdown with supported currencies
  - **Initial Balance**: Numeric input with thousands separator formatting

### Validation Rules
- **Name Uniqueness**: Prevents duplicate wallet names within user's account
- **Required Fields**: All fields must be completed before submission
- **Balance Validation**: Ensures initial balance is a valid number
- **Real-time Feedback**: Immediate validation messages for user guidance

## Wallet Management

### Edit Wallet Functionality
- **Access**: Click/tap on existing wallet cards to edit
- **Modifiable Fields**:
  - Wallet name (with uniqueness validation)
  - Wallet type (Physical ↔ Logical conversion)
  - Currency (with automatic balance conversion)
  - Balance adjustment capabilities

### Delete Wallet Capability
- **Access**: Available through edit interface or dedicated delete action
- **Confirmation**: Requires user confirmation before deletion
- **Safety**: Prevents deletion if wallet has pending transactions or dependencies

### Real-time Updates
- **Balance Synchronization**: Wallets automatically update when transactions occur
- **Cross-page Updates**: Changes reflect immediately across all app screens
- **Firestore Integration**: Real-time listeners ensure data consistency

## Unallocated Balance Feature

### Calculation Logic
```
Unallocated Balance = Σ(Physical Wallet Balances) - Σ(Logical Wallet Balances)
```

### Display Characteristics
- **Visibility**: Only shown when viewing Logical wallets
- **Prominent Position**: Displayed at the top of the Logical wallets view
- **Currency Conversion**: Converted to user's default currency for unified display
- **Real-time Updates**: Automatically recalculates as wallet balances change

### Status Indicators
- **Positive Balance**: Indicates available Physical balance not yet allocated to Logical wallets
- **Zero Balance**: Perfect allocation between Physical and Logical wallets
- **Negative Balance**: Indicates over-allocation (Logical wallets exceed Physical balance)

## Technical Implementation

### Currency Support
- **Standard Currencies**: USD, EUR, GBP, JPY, CAD, AUD, IDR
- **Precious Metals**: GOLD (with gram-based pricing)
- **Cryptocurrency**: BTC (with 8-decimal precision)
- **Conversion**: Automatic conversion using latest exchange rates

### Performance Optimization
- **Lazy Loading**: Efficient wallet loading for users with many wallets
- **Caching**: Intelligent caching of wallet data and conversion rates
- **Real-time Sync**: Firestore listeners for immediate updates without polling

### Error Handling
- **Network Issues**: Graceful handling of connectivity problems
- **Validation Errors**: Clear, actionable error messages
- **Recovery**: Automatic retry mechanisms for failed operations

## User Experience Features

### Visual Design
- **Material 3**: Consistent with app-wide design system
- **Accessibility**: Proper contrast ratios and touch target sizes
- **Responsive**: Adapts to different screen sizes and orientations

### Interaction Patterns
- **Intuitive Navigation**: Clear visual hierarchy and navigation cues
- **Gesture Support**: Standard Android gestures for navigation and actions
- **Feedback**: Visual and haptic feedback for user actions

### State Management
- **Loading States**: Appropriate loading indicators during operations
- **Empty States**: Helpful messaging when no wallets exist
- **Error States**: Clear error messages with recovery suggestions

## Integration Points

### Home Page Connection
- **Balance Card**: Clicking the balance card navigates to wallet list
- **Quick Access**: Provides immediate access to detailed wallet information
- **Context Preservation**: Maintains user's workflow context

### Transaction Integration
- **Wallet Selection**: Wallet list data populates transaction creation screens
- **Balance Updates**: Transaction processing automatically updates wallet balances
- **Validation**: Ensures wallet availability during transaction creation

### Settings Integration
- **Default Currency**: Respects user's default currency preference for display
- **Exchange Rates**: Utilizes currency rates configured in settings
- **User Preferences**: Honors user's display and formatting preferences