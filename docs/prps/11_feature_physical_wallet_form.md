# Product Requirement Prompt (PRP) Template

## 1. Overview
**Brief Description**: Add physical form property to physical wallets to categorize and track different types of financial assets (fiat currency, stocks, gold, cryptocurrency, ETFs, etc.)

**Priority**: High

**Estimated Complexity**: Medium-High

**Expected Development Time**: 3-4 days

## 2. Context & Background
**Current State**: 
- Physical wallets exist with basic properties (name, currency, balance, wallet type)
- All physical wallets are treated uniformly regardless of their underlying asset type
- No distinction between cash, investments, precious metals, cryptocurrencies, etc.
- Limited financial categorization and reporting capabilities
- Users cannot easily group or analyze wallets by asset class

**Problem Statement**: Users managing diverse financial portfolios need to categorize their physical wallets by asset type to better understand their investment allocation, risk exposure, and financial planning. The current system treats all physical wallets equally, missing opportunities for enhanced portfolio analysis, asset-specific features, and improved financial insights.

**User Impact**: Users with complex financial portfolios (stocks, bonds, crypto, precious metals, cash) will benefit from:
- Better portfolio visualization and asset allocation analysis
- Asset-specific features (e.g., price tracking for stocks, precious metal rates)
- Enhanced financial reporting and categorization
- Improved decision-making through clearer asset class separation
- Foundation for future portfolio management features

**Business Value**: Enhanced financial management capabilities, improved user engagement for investment-focused users, foundation for premium portfolio analytics features, and competitive advantage in personal finance management.

## 3. Detailed Requirements

### 3.1 Functional Requirements
**Core Features**:
- [ ] Add `physicalForm` property to Physical wallet data model
- [ ] Create comprehensive list of supported physical forms/asset types
- [ ] Implement physical form selection in wallet creation workflow
- [ ] Add physical form editing capability in wallet management
- [ ] Display physical form information in wallet lists and detail views
- [ ] Implement filtering and grouping by physical form
- [ ] Add asset-specific icons and visual indicators
- [ ] Ensure backward compatibility with existing wallets

**Supported Physical Forms**:
- **Fiat Currency**: Traditional government-issued currencies (USD, EUR, etc.)
- **Cryptocurrency**: Digital currencies (Bitcoin, Ethereum, etc.)
- **Precious Metals**: Gold, Silver, Platinum (gram-based)
- **Stocks**: Individual company shares
- **ETFs**: Exchange-Traded Funds
- **Bonds**: Government and corporate bonds
- **Mutual Funds**: Pooled investment vehicles
- **Real Estate**: Property investments (REITs, direct ownership)
- **Commodities**: Oil, agricultural products, etc.
- **Cash Equivalent**: Money market, savings accounts
- **Other**: Custom/miscellaneous asset types

**User Stories**:
- As a user, I want to specify the physical form of my wallet so that I can categorize my assets appropriately
- As a user, I want to see visual indicators of asset types so that I can quickly identify different wallet categories
- As a user, I want to filter wallets by physical form so that I can focus on specific asset classes
- As a user, I want to group wallets by asset type so that I can analyze my portfolio allocation
- As a user, I want existing wallets to continue working so that my data remains intact
- As a user, I want to change the physical form of a wallet so that I can correct categorization mistakes

**Business Logic**:
- **Default Assignment**: Existing wallets default to "Fiat Currency" for backward compatibility
- **Form Validation**: Ensure physical form aligns with wallet currency (e.g., BTC currency → Cryptocurrency form)
- **Currency Constraints**: Some physical forms may restrict available currencies
- **Icon Mapping**: Each physical form has associated icons and visual styling
- **Filtering Logic**: Enable multi-select filtering by physical form
- **Grouping Logic**: Aggregate balances and statistics by physical form
- **Migration Strategy**: Seamless upgrade path for existing wallet data

### 3.2 Technical Requirements
**Architecture Considerations**:
- Extend Wallet data model with `physicalForm` enum field
- Update Firestore schema with new field and migration strategy
- Implement enum-based physical form management
- Add validation logic for form-currency combinations
- Create physical form selection UI components
- Update wallet creation and editing workflows
- Modify wallet display components for form indicators

**Data Requirements**:
- Add `PhysicalForm` enum with comprehensive asset type list
- Extend `Wallet` data class with `physicalForm: PhysicalForm` property
- Implement default value (`PhysicalForm.FIAT_CURRENCY`) for backward compatibility
- Create migration logic for existing wallet documents
- Add validation rules for form-currency relationships

**API Requirements**:
- Update Firestore wallet document structure
- Implement data migration for existing wallets
- Add filtering queries by physical form
- Create grouping/aggregation queries by asset type
- Maintain existing wallet CRUD operations compatibility

### 3.3 UI/UX Requirements
**Design Specifications**:
- **Physical Form Selection**: Dropdown/picker component with icons and descriptions
- **Visual Indicators**: Asset-specific icons in wallet lists and cards
- **Filtering Interface**: Multi-select filter chips for physical forms
- **Grouping Display**: Section headers for grouped wallet lists
- **Form Labels**: Clear, user-friendly names for each physical form
- **Color Coding**: Optional color schemes for different asset types

**Interaction Design**:
- **Wallet Creation**: Physical form selection as mandatory step
- **Wallet Editing**: Ability to change physical form with confirmation
- **List Views**: Toggle between flat and grouped-by-form layouts
- **Filter Application**: Real-time filtering with visible active filters
- **Visual Hierarchy**: Clear distinction between different asset types

**Accessibility**:
- Screen reader support for physical form labels and descriptions
- High contrast compliance for asset type visual indicators
- Keyboard navigation for physical form selection
- Clear labeling for filtering and grouping controls

## 4. Implementation Guidance

### 4.1 Suggested Approach
**Phase 1**: Data Model and Backend Enhancement
- [ ] Create `PhysicalForm` enum with all supported asset types
- [ ] Update `Wallet` data class with `physicalForm` property and default value
- [ ] Implement Firestore schema migration for existing wallets
- [ ] Add validation logic for form-currency combinations
- [ ] Update repository layer to handle physical form queries
- [ ] Create unit tests for data model changes

**Phase 2**: Core Functionality Implementation
- [ ] Update wallet creation workflow with physical form selection
- [ ] Modify wallet editing to include physical form changes
- [ ] Implement filtering logic by physical form in ViewModels
- [ ] Add grouping functionality for wallet lists
- [ ] Update wallet display components with form indicators
- [ ] Create physical form selection UI components

**Phase 3**: Enhanced UI and User Experience
- [ ] Design asset-specific icons and visual indicators
- [ ] Implement grouped wallet list layouts
- [ ] Add filtering UI with multi-select capabilities
- [ ] Create form validation and user feedback
- [ ] Implement smooth migration UX for existing users
- [ ] Add comprehensive help documentation

**Phase 4**: Testing and Polish
- [ ] Test data migration with various wallet configurations
- [ ] Verify backward compatibility with existing workflows
- [ ] Test filtering and grouping performance
- [ ] Validate form-currency relationship logic
- [ ] User acceptance testing for new workflows
- [ ] Performance optimization for large wallet collections

### 4.2 Files to Focus On
**Primary Files**:
- `app/src/main/java/com/axeven/profiteerapp/data/model/Wallet.kt` - Add PhysicalForm enum and wallet property
- `app/src/main/java/com/axeven/profiteerapp/data/repository/WalletRepository.kt` - Update CRUD operations and filtering
- `app/src/main/java/com/axeven/profiteerapp/ui/wallet/` - Wallet creation, editing, and list components
- `app/src/main/java/com/axeven/profiteerapp/viewmodel/WalletViewModel.kt` - Add filtering and grouping logic

**Secondary Files**:
- `app/src/main/java/com/axeven/profiteerapp/ui/home/HomeScreen.kt` - Display physical form indicators
- `app/src/main/java/com/axeven/profiteerapp/ui/theme/` - Asset type styling and icons
- `app/src/main/java/com/axeven/profiteerapp/utils/` - Validation utilities for form-currency relationships

**New Files to Create**:
- `app/src/main/java/com/axeven/profiteerapp/data/model/PhysicalForm.kt` - Enum definition with asset types
- `app/src/main/java/com/axeven/profiteerapp/ui/wallet/PhysicalFormSelector.kt` - Selection UI component
- `app/src/main/java/com/axeven/profiteerapp/ui/wallet/WalletGrouping.kt` - Grouping display components

### 4.3 Dependencies & Prerequisites
**Required Before Starting**:
- [ ] Understanding of current wallet data model and Firestore structure
- [ ] Knowledge of Jetpack Compose UI patterns used in the app
- [ ] Familiarity with enum handling in Kotlin and Firestore
- [ ] Understanding of data migration strategies for NoSQL databases

**Potential Blockers**:
- Firestore migration complexity for large datasets - mitigate with gradual rollout
- Form-currency validation complexity - mitigate with comprehensive test matrix
- UI space constraints for additional wallet information - mitigate with progressive disclosure
- Performance impact of grouping large wallet collections - mitigate with lazy loading

## 5. Acceptance Criteria

### 5.1 Definition of Done
- [ ] PhysicalForm enum implemented with comprehensive asset type coverage
- [ ] Wallet data model extended with physicalForm property and default value
- [ ] Existing wallets automatically migrated to "Fiat Currency" form
- [ ] Wallet creation workflow includes mandatory physical form selection
- [ ] Wallet editing allows physical form modification with proper validation
- [ ] Wallet lists display physical form indicators (icons/labels)
- [ ] Filtering by physical form works correctly with multi-select capability
- [ ] Grouping by physical form provides clear section organization
- [ ] Form-currency relationship validation prevents invalid combinations
- [ ] Backward compatibility maintained for all existing functionality
- [ ] Performance remains acceptable with physical form features enabled

### 5.2 Test Scenarios
**Happy Path**:
1. User creates new physical wallet and selects appropriate physical form
2. User views wallet list with physical form indicators clearly visible
3. User filters wallets by specific physical form (e.g., only cryptocurrencies)
4. User groups wallets by physical form and sees organized sections
5. User edits existing wallet to change physical form successfully
6. User with existing wallets sees them categorized as "Fiat Currency" by default
7. Form-currency validation prevents selecting crypto form for USD currency

**Edge Cases**:
1. Migration of existing wallets with various currency types
2. Handling of deprecated or removed physical form types
3. Large numbers of wallets with grouping and filtering performance
4. Form changes that affect currency compatibility
5. Network issues during physical form selection or updates
6. Concurrent wallet modifications during migration process

**Validation Testing**:
1. Bitcoin wallet can only use Cryptocurrency physical form
2. Gold wallet (with GOLD currency) must use Precious Metals form
3. Standard currencies (USD, EUR) default to Fiat Currency form
4. Invalid form-currency combinations are prevented with clear error messages
5. Form changes trigger appropriate validation and user feedback

### 5.3 Validation Methods
**Manual Testing**:
- [ ] Test wallet creation with all supported physical forms
- [ ] Verify filtering functionality across different form types
- [ ] Test grouping with various wallet collections
- [ ] Validate form-currency relationship enforcement
- [ ] Test migration behavior with existing wallet data
- [ ] Verify visual indicators and accessibility compliance

**Automated Testing**:
- [ ] Unit tests for PhysicalForm enum and validation logic
- [ ] Integration tests for wallet CRUD operations with physical forms
- [ ] Repository tests for filtering and grouping queries
- [ ] UI tests for physical form selection and display
- [ ] Migration tests for backward compatibility
- [ ] Performance tests for large wallet collections

## 6. Constraints & Considerations

### 6.1 Technical Constraints
- Must maintain backward compatibility with existing wallet data
- Firestore document structure changes require careful migration planning
- Physical form validation must not break existing wallet functionality
- Performance impact of additional filtering and grouping must be minimal
- Mobile UI space limitations for displaying additional wallet information

### 6.2 Business Constraints
- Cannot disrupt existing user workflows or data integrity
- Migration must be transparent and automatic for existing users
- New features should enhance rather than complicate basic wallet management
- Implementation must align with existing Material 3 design patterns

### 6.3 User Experience Constraints
- Physical form selection should be intuitive for non-investment users
- Filtering and grouping should not overwhelm simple use cases
- Default behavior should work well for basic cash/currency tracking
- Advanced features should be discoverable but not mandatory

## 7. Risk Assessment

### 7.1 Technical Risks
**Risk 1**: Firestore migration fails or corrupts existing wallet data
- **Probability**: Low
- **Impact**: High
- **Mitigation**: Comprehensive backup strategy, gradual rollout, extensive testing

**Risk 2**: Form-currency validation creates too many restrictions
- **Probability**: Medium
- **Impact**: Medium
- **Mitigation**: Flexible validation rules, user override options, clear error messages

**Risk 3**: Performance degradation with large wallet collections
- **Probability**: Medium
- **Impact**: Medium
- **Mitigation**: Lazy loading, efficient queries, performance monitoring

### 7.2 Business Risks
**Risk 1**: Feature complexity confuses basic users
- **Mitigation**: Smart defaults, progressive disclosure, optional advanced features

**Risk 2**: Migration causes user data concerns
- **Mitigation**: Clear communication, transparent process, easy rollback

## 8. Success Metrics
**Quantitative Metrics**:
- 100% successful migration of existing wallets to default physical form
- Zero data loss during migration process
- <500ms response time for filtering and grouping operations
- Physical form selection completion rate >95% in wallet creation

**Qualitative Metrics**:
- Users can easily categorize and organize their financial assets
- Investment-focused users find enhanced portfolio organization valuable
- Basic users are not confused or overwhelmed by new features
- Overall wallet management experience is improved

## 9. Future Considerations
**Potential Enhancements**:
- **Asset-Specific Features**: Price tracking for stocks, crypto rates, precious metal values
- **Portfolio Analytics**: Asset allocation charts, risk analysis, performance tracking
- **Import Integration**: Automatic categorization from financial institution imports
- **Advanced Filtering**: Multiple criteria combinations, saved filter sets
- **Custom Forms**: User-defined asset types for specialized investments
- **Reporting**: Asset class-based financial reports and insights

**Scalability Considerations**:
- Support for additional asset types as financial markets evolve
- Integration with external data sources for asset information
- Advanced portfolio management features for professional users
- Multi-currency support within specific physical forms

## 10. Claude Implementation Notes
**Optimization Tips for AI Development**:
- [ ] Follow existing data model patterns from current Wallet implementation
- [ ] Use consistent enum naming conventions throughout the codebase
- [ ] Leverage existing UI components and styling patterns
- [ ] Maintain consistency with current filtering and grouping UX patterns
- [ ] Follow established Firestore migration strategies used in the project
- [ ] Use existing validation patterns for form input handling
- [ ] Maintain Material 3 design consistency with current wallet UI

**Context Preservation**:
- [ ] Reference CLAUDE.md for project architecture and Firestore patterns
- [ ] Follow existing MVVM patterns with Hilt dependency injection
- [ ] Use established data flow patterns for real-time updates
- [ ] Maintain consistency with existing currency handling logic
- [ ] Follow current transaction categorization patterns (tags vs categories)
- [ ] Use existing theme and color schemes for new visual indicators

---

## Implementation Priority
This feature provides significant value for users with diverse financial portfolios and creates a foundation for advanced portfolio management features. It should be implemented after core wallet functionality is stable.

## Data Model Preview
```kotlin
enum class PhysicalForm(val displayName: String, val description: String) {
    FIAT_CURRENCY("Fiat Currency", "Traditional government-issued currencies"),
    CRYPTOCURRENCY("Cryptocurrency", "Digital currencies like Bitcoin, Ethereum"),
    PRECIOUS_METALS("Precious Metals", "Gold, Silver, Platinum investments"),
    STOCKS("Stocks", "Individual company shares"),
    ETFS("ETFs", "Exchange-Traded Funds"),
    BONDS("Bonds", "Government and corporate bonds"),
    MUTUAL_FUNDS("Mutual Funds", "Pooled investment vehicles"),
    REAL_ESTATE("Real Estate", "Property investments and REITs"),
    COMMODITIES("Commodities", "Oil, agricultural products, etc."),
    CASH_EQUIVALENT("Cash Equivalent", "Money market, savings accounts"),
    OTHER("Other", "Custom or miscellaneous asset types")
}

data class Wallet(
    // ... existing properties
    val physicalForm: PhysicalForm = PhysicalForm.FIAT_CURRENCY
)
```

## UI Mockup Concept
```
Create Physical Wallet:
┌─────────────────────────────────────┐
│ Wallet Name: [Cash Wallet        ] │
│ Currency:    [USD            ▼  ] │
│ Physical Form: [Fiat Currency  ▼ ] │
│  💰 Fiat Currency - Traditional... │
│  ₿  Cryptocurrency - Digital...    │
│  🥇 Precious Metals - Gold, Silv.. │
│  📈 Stocks - Individual company... │
│  📊 ETFs - Exchange-Traded Funds   │
│ Balance:     [$1,000.00         ] │
│ [Cancel]              [Create]     │
└─────────────────────────────────────┘

Wallet List (Grouped):
┌─────────────────────────────────────┐
│ 💰 Fiat Currency                   │
│ • 💵 Cash Wallet        $1,234.56  │
│ • 💶 Euro Account       €856.43    │
│                                     │
│ ₿ Cryptocurrency                   │
│ • ₿ Bitcoin Wallet      0.05 BTC   │
│ • Ξ Ethereum Wallet     2.3 ETH    │
│                                     │
│ 📈 Stocks                          │
│ • 🍎 Apple Shares       15 shares  │
│ • 🏢 Portfolio Account  $5,432.10  │
└─────────────────────────────────────┘
```