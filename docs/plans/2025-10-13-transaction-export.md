# Transaction List Export to Google Sheets Implementation Plan

**Date:** 2025-10-13
**Status:** Phase 1 Complete ✅
**Approach:** Test-Driven Development (TDD)

## Overview

Implement a transaction export feature that allows users to export the currently displayed transaction list from the Transaction List Screen to a Google Sheets document. The feature will respect all active filters and export only the visible transactions.

## Business Requirements

### Export Functionality
- **Trigger**: Export button/action in Transaction List Screen app bar
- **Scope**: Export currently filtered/displayed transactions only
- **Format**: Google Sheets document
- **Authentication**: Use existing Google Sign-in credentials
- **Permissions**: Request Google Sheets API access

### Export Data Structure
The exported Google Sheet should include:
- **Sheet Name**: "Transactions Export - [Date]"
- **Columns**:
  1. Date (Transaction Date)
  2. Title
  3. Type (Income/Expense/Transfer)
  4. Amount
  5. Currency
  6. Physical Wallet
  7. Logical Wallet
  8. Tags (comma-separated)
  9. Notes/Description
  10. Source Wallet (for Transfers)
  11. Destination Wallet (for Transfers)

### User Experience
- Show loading indicator during export
- Success message with link to open Google Sheet
- Error handling with clear messages
- Option to share/open the created sheet

## Technical Design

### Architecture Components

#### 1. Google Sheets Integration
- **Google Sheets API v4**: For creating and writing to sheets
- **Google Sign-In**: Leverage existing authentication
- **OAuth Scopes**: Request `https://www.googleapis.com/auth/spreadsheets`

#### 2. New Classes/Files
- `GoogleSheetsExporter.kt` - Core export logic
- `ExportFormatter.kt` - Format transaction data for export
- `TransactionExportRepository.kt` - Repository for export operations
- `ExportUiState.kt` - Consolidated state for export dialog/progress

#### 3. Updated Classes
- `TransactionListScreen.kt` - Add export button and dialog
- `TransactionListViewModel.kt` - Add export functionality
- `build.gradle` - Add Google Sheets API dependency

### Dependencies Required

```gradle
// Google Play Services Auth
implementation 'com.google.android.gms:play-services-auth:20.7.0'

// Google API Client for Android
implementation 'com.google.api-client:google-api-client-android:2.2.0'

// Google Sheets API
implementation 'com.google.apis:google-api-services-sheets:v4-rev20230815-2.0.0'
```

## Implementation Plan

### Phase 1: Google Sheets API Setup (TDD) ✅ COMPLETED

#### 1.1 Dependency Configuration ✅
- [x] **Config**: Add Google Sheets API dependencies to `build.gradle`
- [x] **Config**: Add required OAuth scopes to authentication flow
- [x] **Config**: Update ProGuard rules for Google API client
- [x] **Test**: Verify dependencies compile successfully

#### 1.2 Google Sheets Service Setup ✅
- [x] **Test**: Write test for `GoogleSheetsService.isAuthorized()`
  - [x] Test when user is authenticated with Sheets scope
  - [x] Test when user lacks Sheets scope
  - [x] Test when user is not signed in
- [x] **Code**: Implement `GoogleSheetsService.isAuthorized()`
- [x] **Test**: Write test for `GoogleSheetsService.requestAuthorization()`
  - [x] Test successful authorization request (deferred to integration tests)
  - [x] Test authorization cancellation (deferred to integration tests)
  - [x] Test authorization error handling (deferred to integration tests)
- [x] **Code**: Implement `GoogleSheetsService.requestAuthorization()`
- [x] **Test**: Write test for `GoogleSheetsService.createSheetsService()`
  - [x] Test service creation with valid credentials (deferred to integration tests)
  - [x] Test service creation failure handling
- [x] **Code**: Implement `GoogleSheetsService.createSheetsService()`

**Files Created:**
- `app/src/main/java/com/axeven/profiteerapp/service/GoogleSheetsService.kt` ✅
- `app/src/test/java/com/axeven/profiteerapp/service/GoogleSheetsServiceTest.kt` ✅

**Files Modified:**
- `gradle/libs.versions.toml` - Added Google API dependencies ✅
- `app/build.gradle.kts` - Added dependencies and packaging rules ✅
- `app/proguard-rules.pro` - Added ProGuard rules ✅
- `app/src/main/java/com/axeven/profiteerapp/data/repository/AuthRepository.kt` - Added Sheets scope support ✅

**Test Results:** 9 tests total
- 5 tests passing ✅
- 4 tests marked with `@Ignore` for Android instrumentation (Phase 8) ✅

**Note on Deferred Tests**: 4 tests require Android runtime (GoogleSignInClient.signInIntent, GoogleAccountCredential creation) and are properly marked with `@Ignore`. These will be covered by integration tests in Phase 8 when testing the complete export flow on a real device/emulator.

### Phase 2: Export Data Formatter (TDD) ✅ COMPLETED

#### 2.1 Transaction Data Formatting ✅
- [x] **Test**: Write test for `ExportFormatter.formatTransactionRow()`
  - [x] Test income transaction formatting
  - [x] Test expense transaction formatting
  - [x] Test transfer transaction with source/destination
  - [x] Test transaction with multiple tags
  - [x] Test transaction with no tags
  - [x] Test transaction with empty/null description
  - [x] Test transaction with special characters in title
  - [x] Test date formatting consistency
- [x] **Code**: Implement `ExportFormatter.formatTransactionRow()`
- [x] **Test**: Write test for `ExportFormatter.createHeaderRow()`
  - [x] Test header contains all required columns
  - [x] Test header order matches specification
- [x] **Code**: Implement `ExportFormatter.createHeaderRow()`
- [x] **Test**: Write test for `ExportFormatter.formatTransactionList()`
  - [x] Test with empty transaction list
  - [x] Test with single transaction
  - [x] Test with multiple transactions
  - [x] Test preserves transaction order
  - [x] Test includes header row
- [x] **Code**: Implement `ExportFormatter.formatTransactionList()`

#### 2.2 Wallet Name Resolution ✅
- [x] **Test**: Write test for `ExportFormatter.resolveWalletName()`
  - [x] Test with valid wallet ID
  - [x] Test with invalid/missing wallet ID
  - [x] Test with empty wallet ID
  - [x] Test caching behavior for performance
- [x] **Code**: Implement `ExportFormatter.resolveWalletName()`

**Files Created:**
- `app/src/main/java/com/axeven/profiteerapp/utils/ExportFormatter.kt` ✅
- `app/src/test/java/com/axeven/profiteerapp/utils/ExportFormatterTest.kt` ✅

**Test Results:** 19/19 tests passing ✅ (exceeded expected ~15 tests)
- 8 tests for `formatTransactionRow()` (all transaction types and edge cases)
- 2 tests for `createHeaderRow()` (completeness and ordering)
- 5 tests for `formatTransactionList()` (empty, single, multiple, order preservation)
- 4 tests for `resolveWalletName()` (valid, invalid, empty, caching)

**Implementation Details:**
- Column structure: 11 columns matching specification (Date, Title, Type, Amount, Currency, Physical Wallet, Logical Wallet, Tags, Notes, Source Wallet, Destination Wallet)
- Date formatting: ISO 8601 format (yyyy-MM-dd) for consistency
- Wallet resolution: Separate Physical/Logical wallets for INCOME/EXPENSE, Source/Destination for TRANSFER
- Tag handling: Comma-separated list, "Untagged" for empty tags
- Performance: Implemented caching for wallet name lookups
- Edge cases: Handles special characters, empty values, missing wallet references

### Phase 3: Google Sheets Export Logic (TDD) ✅ COMPLETED

#### 3.1 Sheet Creation and Writing ✅
- [x] **Test**: Write test for `GoogleSheetsExporter.createSpreadsheet()`
  - [x] Test successful spreadsheet creation
  - [x] Test with custom sheet name
  - [x] Test sheet name includes timestamp
  - [x] Test error handling for creation failure
  - [x] Test network error handling
- [x] **Code**: Implement `GoogleSheetsExporter.createSpreadsheet()`
- [x] **Test**: Write test for `GoogleSheetsExporter.writeData()`
  - [x] Test writing empty data (header only)
  - [x] Test writing single row of data
  - [x] Test writing multiple rows (batch operation)
  - [x] Test data truncation for large datasets (>1000 rows)
  - [x] Test error handling for write failures
  - [x] Test retry logic for transient errors
- [x] **Code**: Implement `GoogleSheetsExporter.writeData()`
- [x] **Test**: Write test for `GoogleSheetsExporter.formatCells()`
  - [x] Test header row formatting (bold, background color)
  - [x] Test currency column formatting
  - [x] Test date column formatting
  - [x] Test column width auto-sizing
- [x] **Code**: Implement `GoogleSheetsExporter.formatCells()`

#### 3.2 Complete Export Flow ✅
- [x] **Test**: Write test for `GoogleSheetsExporter.exportTransactions()`
  - [x] Test end-to-end export flow
  - [x] Test with empty transaction list
  - [x] Test with 100+ transactions
  - [x] Test returns shareable spreadsheet URL
  - [x] Test error handling and rollback
  - [x] Test concurrent export prevention
- [x] **Code**: Implement `GoogleSheetsExporter.exportTransactions()`

**Files Created:**
- `app/src/main/java/com/axeven/profiteerapp/service/GoogleSheetsExporter.kt` ✅
- `app/src/test/java/com/axeven/profiteerapp/service/GoogleSheetsExporterTest.kt` ✅

**Test Results:** 21/21 tests passing ✅ (exceeded expected ~18 tests)
- 5 tests for `createSpreadsheet()` (successful creation, custom name, timestamp, error handling)
- 6 tests for `writeData()` (empty data, single row, multiple rows, large datasets, error handling, retry logic)
- 4 tests for `formatCells()` (header formatting, currency/date formatting, auto-sizing)
- 6 tests for `exportTransactions()` (end-to-end flow, empty list, large datasets, URL return, error handling, concurrent prevention)

**Implementation Details:**
- Spreadsheet creation with automatic timestamp naming
- Batch data writing with support for 1000+ rows
- Comprehensive cell formatting (bold headers, light gray background, date/currency formats, auto-sized columns)
- End-to-end export flow with proper error handling
- Returns shareable spreadsheet URLs
- Logging integrated for all operations

### Phase 4: Repository Layer (TDD)

#### 4.1 Transaction Export Repository
- [ ] **Test**: Write test for `TransactionExportRepository.exportToGoogleSheets()`
  - [ ] Test successful export returns Result.success with URL
  - [ ] Test authorization failure returns Result.failure
  - [ ] Test network error returns Result.failure with message
  - [ ] Test empty transaction list handling
  - [ ] Test cancellation support
- [ ] **Code**: Implement `TransactionExportRepository.exportToGoogleSheets()`
- [ ] **Test**: Write test for `TransactionExportRepository.checkExportPermissions()`
  - [ ] Test when permissions granted
  - [ ] Test when permissions not granted
  - [ ] Test when user not signed in
- [ ] **Code**: Implement `TransactionExportRepository.checkExportPermissions()`

**Files to Create:**
- `app/src/main/java/com/axeven/profiteerapp/data/repository/TransactionExportRepository.kt`
- `app/src/test/java/com/axeven/profiteerapp/data/repository/TransactionExportRepositoryTest.kt`

**Expected Test Count:** ~8 tests

### Phase 5: UI State Management (TDD)

#### 5.1 Export UI State
- [ ] **Test**: Write test for `ExportUiState` data class
  - [ ] Test default state (not exporting)
  - [ ] Test isExporting state
  - [ ] Test success state with URL
  - [ ] Test error state with message
  - [ ] Test state transitions are immutable
- [ ] **Code**: Implement `ExportUiState` data class
- [ ] **Test**: Write test for `ExportUiState.withExporting()`
  - [ ] Test transitions to exporting state
  - [ ] Test clears previous error
- [ ] **Code**: Implement `ExportUiState.withExporting()`
- [ ] **Test**: Write test for `ExportUiState.withSuccess()`
  - [ ] Test transitions to success state
  - [ ] Test stores spreadsheet URL
  - [ ] Test clears error state
- [ ] **Code**: Implement `ExportUiState.withSuccess()`
- [ ] **Test**: Write test for `ExportUiState.withError()`
  - [ ] Test transitions to error state
  - [ ] Test stores error message
  - [ ] Test clears success URL
- [ ] **Code**: Implement `ExportUiState.withError()`

**Files to Create:**
- `app/src/main/java/com/axeven/profiteerapp/data/ui/ExportUiState.kt`
- `app/src/test/java/com/axeven/profiteerapp/data/ui/ExportUiStateTest.kt`

**Expected Test Count:** ~10 tests

### Phase 6: ViewModel Integration (TDD)

#### 6.1 TransactionListViewModel Enhancement
- [ ] **Test**: Write test for `TransactionListViewModel.exportTransactions()`
  - [ ] Test initiates export with current filtered transactions
  - [ ] Test updates export UI state to loading
  - [ ] Test handles successful export
  - [ ] Test handles export failure
  - [ ] Test handles authorization required
  - [ ] Test handles empty transaction list
  - [ ] Test cancels ongoing export when called again
- [ ] **Code**: Implement `TransactionListViewModel.exportTransactions()`
- [ ] **Test**: Write test for `TransactionListViewModel.requestExportPermissions()`
  - [ ] Test requests OAuth permissions
  - [ ] Test handles permission grant
  - [ ] Test handles permission denial
- [ ] **Code**: Implement `TransactionListViewModel.requestExportPermissions()`
- [ ] **Test**: Write test for `TransactionListViewModel.dismissExportResult()`
  - [ ] Test resets export state
  - [ ] Test clears success/error messages
- [ ] **Code**: Implement `TransactionListViewModel.dismissExportResult()`
- [ ] **Test**: Write test for export state flow
  - [ ] Test exportUiState emits correct states
  - [ ] Test state updates are observed by UI
- [ ] **Code**: Implement export state flow in ViewModel

**Files to Update:**
- `app/src/main/java/com/axeven/profiteerapp/viewmodel/TransactionListViewModel.kt`
- `app/src/test/java/com/axeven/profiteerapp/viewmodel/TransactionListViewModelTest.kt`

**Expected Test Count:** ~12 tests

### Phase 7: UI Components (TDD)

#### 7.1 Export Button and Dialog
- [ ] **Test**: Write test for Export button in TopAppBar
  - [ ] Test button is visible
  - [ ] Test button click triggers export
  - [ ] Test button is disabled when no transactions
  - [ ] Test button shows loading state during export
- [ ] **Code**: Add Export button to TransactionListScreen TopAppBar
- [ ] **Test**: Write test for ExportProgressDialog
  - [ ] Test shows progress indicator
  - [ ] Test displays "Exporting..." message
  - [ ] Test cannot be dismissed during export
- [ ] **Code**: Implement ExportProgressDialog composable
- [ ] **Test**: Write test for ExportSuccessDialog
  - [ ] Test displays success message with transaction count
  - [ ] Test shows "Open Sheet" button
  - [ ] Test shows "Share" button
  - [ ] Test shows "Done" button
  - [ ] Test button actions trigger correct callbacks
- [ ] **Code**: Implement ExportSuccessDialog composable
- [ ] **Test**: Write test for ExportErrorDialog
  - [ ] Test displays error message
  - [ ] Test shows "Retry" button
  - [ ] Test shows "Cancel" button
  - [ ] Test handles authorization error specifically
- [ ] **Code**: Implement ExportErrorDialog composable

#### 7.2 Screen Integration
- [ ] **Test**: Write UI test for complete export flow
  - [ ] Test clicking export shows progress dialog
  - [ ] Test successful export shows success dialog
  - [ ] Test opening sheet launches browser
  - [ ] Test sharing sheet shows share intent
  - [ ] Test export error shows error dialog
  - [ ] Test retry from error dialog
- [ ] **Code**: Integrate export dialogs into TransactionListScreen
- [ ] **Code**: Add export button action handlers
- [ ] **Code**: Add intent handling for opening/sharing sheet

**Files to Update:**
- `app/src/main/java/com/axeven/profiteerapp/ui/transaction/TransactionListScreen.kt`

**Files to Create:**
- `app/src/test/java/com/axeven/profiteerapp/ui/transaction/TransactionListScreenExportTest.kt`

**Expected Test Count:** ~15 tests

### Phase 8: Integration Testing

#### 8.1 End-to-End Tests
- [ ] **Test**: Write integration test for complete export flow
  - [ ] Test export with real transaction data (mocked Google API)
  - [ ] Test export preserves filter state
  - [ ] Test export handles date range filters
  - [ ] Test export handles tag filters
  - [ ] Test export handles wallet filters
  - [ ] Test concurrent export prevention
  - [ ] Test memory handling for large exports (1000+ transactions)
- [ ] **Code**: Implement integration tests

#### 8.2 Error Scenario Testing
- [ ] **Test**: Test network error handling
  - [ ] Test offline mode shows appropriate error
  - [ ] Test timeout handling
  - [ ] Test API rate limit handling
- [ ] **Test**: Test permission scenarios
  - [ ] Test first-time permission request
  - [ ] Test permission revoked handling
  - [ ] Test permission denied by user
- [ ] **Test**: Test edge cases
  - [ ] Test export with special characters in transaction data
  - [ ] Test export with very long transaction titles
  - [ ] Test export with transactions in multiple currencies
  - [ ] Test export with deleted wallet references

**Files to Create:**
- `app/src/test/java/com/axeven/profiteerapp/integration/TransactionExportIntegrationTest.kt`

**Expected Test Count:** ~15 tests

### Phase 9: Documentation and Polish

#### 9.1 User Documentation
- [ ] **Docs**: Update README.md with export feature description
- [ ] **Docs**: Add export feature to feature list
- [ ] **Docs**: Document Google Sheets permission requirements
- [ ] **Docs**: Create user guide for export feature

#### 9.2 Code Documentation
- [ ] **Docs**: Add KDoc comments to all public functions
- [ ] **Docs**: Document export data format
- [ ] **Docs**: Document error codes and handling
- [ ] **Docs**: Add architecture documentation for export flow

#### 9.3 Polish and Refinements
- [ ] **Code**: Add logging for export operations
- [ ] **Code**: Add analytics events for export usage
- [ ] **Code**: Implement export cancellation support
- [ ] **Code**: Add export history/recent exports tracking (optional)
- [ ] **Test**: Verify all tests pass
- [ ] **Test**: Run lint checks
- [ ] **Test**: Performance testing with large datasets

## Security Considerations

### OAuth Security
- Use minimal OAuth scopes (only Sheets API access)
- Token storage via Google Sign-In SDK (secure by default)
- No manual token handling
- Respect token expiration and refresh

### Data Privacy
- Export only user's own data
- No data sent to third-party servers (except Google)
- Clear user consent via OAuth flow
- Option to delete exported sheets (via Google Drive)

### Input Validation
- Sanitize transaction data before export
- Handle special characters and formulas in cells
- Prevent CSV injection attacks
- Validate spreadsheet URLs before opening

## Error Handling

### Error Categories
1. **Authentication Errors**
   - Not signed in
   - OAuth permission denied
   - Token expired
   - Scope not granted

2. **Network Errors**
   - No internet connection
   - Timeout
   - API rate limit
   - Server error

3. **Data Errors**
   - Empty transaction list
   - Invalid transaction data
   - Wallet reference not found

4. **System Errors**
   - Out of memory
   - Storage full
   - Concurrent export conflict

### Error Messages
All error messages should be:
- User-friendly and actionable
- Specific about what went wrong
- Include retry options where applicable
- Log technical details for debugging

## Success Metrics

### Functional Metrics
- [ ] All unit tests pass (100% for new code)
- [ ] Integration tests pass
- [ ] Export completes in <10 seconds for 100 transactions
- [ ] Export completes in <30 seconds for 1000 transactions
- [ ] Success rate >95% in test environments

### User Experience Metrics
- [ ] Export button is discoverable
- [ ] Export flow is intuitive
- [ ] Success messages are clear
- [ ] Error recovery is smooth
- [ ] Sheet format is readable and useful

## Future Enhancements (Out of Scope)

### Phase 10+ (Future)
- Export to CSV/Excel formats
- Scheduled automatic exports
- Export templates with custom columns
- Export to other services (Dropbox, OneDrive)
- Bulk export history
- Export wallet balances separately
- Export reports and summaries
- Custom date range export
- Email export results
- Export formatting preferences

## Testing Summary

### Expected Test Coverage
- **Phase 1**: 8 tests (Google Sheets API Setup)
- **Phase 2**: 15 tests (Export Formatter)
- **Phase 3**: 18 tests (Google Sheets Export Logic)
- **Phase 4**: 8 tests (Repository Layer)
- **Phase 5**: 10 tests (UI State Management)
- **Phase 6**: 12 tests (ViewModel Integration)
- **Phase 7**: 15 tests (UI Components)
- **Phase 8**: 15 tests (Integration Testing)

**Total Expected Tests:** ~101 tests

### Test Categories
- Unit Tests: ~71 tests
- Integration Tests: ~15 tests
- UI Tests: ~15 tests

## Implementation Notes

### Development Order
1. Start with Phase 1 (API Setup) to validate Google integration
2. Complete Phase 2-4 (Core Logic) with full test coverage
3. Implement Phase 5-6 (State Management & ViewModel)
4. Build Phase 7 (UI) with user testing
5. Complete Phase 8 (Integration tests)
6. Finalize with Phase 9 (Documentation)

### Risk Mitigation
- **Google API Changes**: Use stable API version, monitor deprecation notices
- **Permission Issues**: Test permission flow thoroughly on multiple devices
- **Performance**: Implement pagination for large datasets
- **Network Reliability**: Add retry logic and offline detection
- **User Confusion**: Provide clear onboarding and help text

### Dependencies on Existing Code
- Google Sign-In authentication (already implemented)
- Transaction filtering logic (TransactionListViewModel)
- Wallet repository (for wallet name resolution)
- Number formatting utilities (for currency display)

## Timeline Estimate

### Time Breakdown (Rough Estimates)
- **Phase 1**: 4-6 hours (API setup and learning)
- **Phase 2**: 4-6 hours (Formatter with tests)
- **Phase 3**: 8-10 hours (Export logic with tests)
- **Phase 4**: 3-4 hours (Repository layer)
- **Phase 5**: 3-4 hours (UI State)
- **Phase 6**: 4-6 hours (ViewModel integration)
- **Phase 7**: 6-8 hours (UI implementation)
- **Phase 8**: 4-6 hours (Integration testing)
- **Phase 9**: 2-3 hours (Documentation)

**Total Estimated Time:** 38-53 hours (5-7 working days)

## Rollout Plan

### Development Stages
1. **Alpha**: Internal testing with small datasets
2. **Beta**: Selected users with usage monitoring
3. **General Availability**: Full rollout with analytics

### Rollback Strategy
- Feature flag controlled rollout
- Can disable export button via remote config
- No database schema changes (safe to rollback)

## Status Tracking

**Current Phase:** Phase 4 (Ready to Start)
**Completed Phases:** Phase 1 ✅, Phase 2 ✅, Phase 3 ✅
**Total Progress:** 3/9 phases complete (33%)

### Progress Checklist
- [x] Phase 1: Google Sheets API Setup (5/5 unit tests passing, 4 deferred to integration) ✅
- [x] Phase 2: Export Data Formatter (19/19 tests passing) ✅
- [x] Phase 3: Google Sheets Export Logic (21/21 tests passing) ✅
- [ ] Phase 4: Repository Layer (0/8 tests)
- [ ] Phase 5: UI State Management (0/10 tests)
- [ ] Phase 6: ViewModel Integration (0/12 tests)
- [ ] Phase 7: UI Components (0/15 tests)
- [ ] Phase 8: Integration Testing (0/15 tests)
- [ ] Phase 9: Documentation and Polish

---

**Last Updated:** 2025-10-14
**Next Review:** After Phase 3 completion
