# Settings Page

The Settings page is where users can manage their wallet configurations, currency settings, and conversion rates.

## Current Features

### 🏦 Wallet Configuration
- **Create New Wallet**
  - Wallet name (with uniqueness validation)
  - Wallet type selection (Physical or Logical)
  - Currency selection (USD, EUR, GBP, JPY, CAD, AUD, IDR)
  - Initial balance setup (with thousands separator formatting)
  - Real-time form validation with error messages

- **Existing Wallet Management**
  - List of all user wallets with balance display
  - Each wallet shows: Name, Type, Currency, and formatted balance
  - Edit wallet functionality (modify name, type, currency, initial balance)
  - Delete wallet capability
  - Real-time balance updates

### 💱 Currency Configuration
- **Default Currency Setting**
  - Set user's preferred display currency
  - Dropdown selection from supported currencies
  - Applies to dashboard and analytics displays

### 📈 Currency Conversion Rate Configuration
- **Add Default Conversion Rate**
  - Set flat conversion rate for all times
  - From/To currency selection with dropdowns
  - Numeric rate input with validation

- **Add Monthly Conversion Rate**
  - Set specific conversion rate for certain month
  - Month specification (e.g., "January 2024")
  - Overrides default rates when applicable

- **Rate Management**
  - List all configured conversion rates
  - Shows rate type (Default vs Monthly)
  - Edit and delete rate functionality
  - Formatted rate display with thousands separators

## User Interface Features

### Form Validation
- **Real-time Validation**: Immediate feedback as users type
- **Error States**: Red highlighting and error messages for invalid input
- **Unique Name Validation**: Prevents duplicate wallet names (case-insensitive)
- **Numeric Validation**: Ensures proper number formatting and parsing

### Number Formatting
- **Thousands Separators**: All amounts display as 1,234.56
- **Smart Input Parsing**: Accepts both "1234.56" and "1,234.56" formats
- **Currency Display**: Shows currency code alongside amounts
- **Consistent Formatting**: Uniform number display across all components

### User Experience
- **Dropdown Selections**: User-friendly dropdowns instead of text input for currencies and wallet types
- **Pre-filled Forms**: Edit forms show current values with proper formatting
- **Clear Labeling**: Descriptive labels and helpful text for all fields
- **Responsive Design**: Material 3 design with proper spacing and visual hierarchy

## Navigation
- **Back Navigation**: Top bar with back arrow to return to home screen
- **Persistent State**: Form states maintained during navigation
- **Real-time Updates**: Changes reflected immediately in the UI

## Data Management
- **Firebase Integration**: All data syncs to Firestore in real-time
- **User Isolation**: Each user's settings are completely separate
- **Error Handling**: Graceful error handling with user-friendly messages
- **Loading States**: Visual feedback during data operations

## Implemented Validation Rules
1. **Wallet Names**: Must be unique per user (case-insensitive)
2. **Required Fields**: Name, type, currency must be provided
3. **Numeric Input**: Balance amounts must be valid numbers
4. **Currency Selection**: Must select from supported currency list
5. **Form Completeness**: Create/Save buttons disabled until form is valid

## Future Enhancements
- Import/export wallet configurations
- Bulk wallet operations
- Advanced currency rate scheduling
- Wallet categorization and tagging
- Integration with external financial APIs