# Homepage

The Homepage serves as the main dashboard providing users with an overview of their financial status and quick access to key features.

## Current Implementation

### 💰 Balance Overview Card
- **Total Balance Display**: Shows overall financial position with thousands separator formatting
- **Income Summary**: Current period income with green color coding and proper formatting
- **Expense Summary**: Current period expenses with red color coding and proper formatting
- **Currency Display**: Shows amounts in user's default currency

### 🎯 Quick Actions
Interactive card-based navigation to key features:
- **Add Income**: Quick access to record income transactions
- **Add Expense**: Quick access to record expense transactions  
- **Transfer**: Money movement between wallets (planned feature)
- **Analytics**: Financial analysis and reporting (planned feature)

### 📋 Recent Transactions
- **Transaction List**: Displays recent transactions with formatted amounts
- **Transaction Details**: Shows title, category, date, and amount for each transaction
- **Visual Indicators**: Color-coded icons for income (green up arrow) vs expenses (red down arrow)
- **Thousands Separators**: All amounts display with proper formatting (1,234.56)

### 🧭 Navigation
- **Top Bar**: App title "Profiteer" with settings and profile access
- **Settings Access**: Direct navigation to settings page via gear icon
- **Profile Menu**: User account access (planned feature)

## Data Sources

### Transaction Analytics
- **Real-time Calculation**: Analytics based purely on transaction data
- **Initial Balance Exclusion**: Setup balances don't affect analytics calculations
- **Income/Expense Tracking**: Separate tracking of positive and negative transactions
- **Balance Calculation**: `totalBalance = totalIncome - totalExpenses`

### Wallet Integration
- **Firestore Sync**: Real-time data from user's wallet and transaction collections
- **User Isolation**: Only displays current user's data
- **Multi-currency Support**: Handles different wallet currencies with default currency display

## User Interface Features

### Material 3 Design
- **Primary Container**: Balance card with prominent display
- **Surface Cards**: Clean card-based layout for transactions and quick actions
- **Color Scheme**: Consistent color coding (green for income, red for expenses)
- **Typography**: Proper text hierarchy with bold headings and readable content

### Number Formatting
- **Thousands Separators**: All financial amounts display as 1,234.56
- **Currency Prefix**: Shows currency code before amounts (USD 1,234.56)
- **Consistent Formatting**: Uniform display across balance card, transactions, and summaries

### Responsive Layout
- **Scrollable Content**: Lazy column layout accommodating any number of transactions
- **Card Spacing**: Consistent 16dp spacing between elements
- **Adaptive Sizing**: Components adjust to screen size and content length

## Authentication Integration
- **Firebase Auth**: Integrated with Google Sign-In authentication
- **Protected Routes**: Requires user authentication to access financial data
- **User Context**: All data operations tied to authenticated user ID

## Planned Features (Not Yet Implemented)

### Balance Discrepancy Detection
- Sum of physical wallet balances vs logical wallet balances comparison
- Notification system for discrepancy alerts
- Detailed discrepancy amount display

### Wallet Lists
- Grouped wallet display (Physical vs Logical)
- Individual wallet balance display with navigation to detail pages
- Real-time balance updates

### Advanced Analytics
- Monthly expense tracking
- Net income calculations (income - expenses)
- Top 5 expense categories by transaction value
- Spending trend analysis

### Enhanced Navigation
- Add transaction page integration
- Transaction list page access
- Wallet detail page navigation
- Analytics dashboard access

## Performance Considerations
- **Lazy Loading**: Efficient transaction list rendering
- **Real-time Updates**: Firebase listeners for immediate data sync
- **Optimized Queries**: Efficient Firestore queries for dashboard data
- **State Management**: Proper StateFlow and Compose state handling

The current homepage implementation focuses on providing a clean, informative dashboard with essential financial information while maintaining excellent user experience through proper formatting and responsive design.