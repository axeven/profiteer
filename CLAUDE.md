# CLAUDE.md

This file provides specific guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Context

For comprehensive project information, architecture details, and feature documentation, see **[README.md](README.md)** and the **[docs/](docs/)** directory.

**Quick Summary**: Profiteer is a personal finance Android app built with Kotlin, Jetpack Compose, MVVM architecture, Firebase Auth, and Firestore, featuring a dual-wallet system with multi-currency support.

## Build Commands

### Development
- `./gradlew build` - Build the project
- `./gradlew assembleDebug` - Build debug APK
- `./gradlew assembleRelease` - Build release APK
- `./gradlew installDebug` - Install debug build on connected device

### Testing

Use the following commands to run tests

- `./gradlew test` - Run unit tests
- `./gradlew connectedAndroidTest` - Run instrumented tests on connected device/emulator
- `./gradlew testDebugUnitTest` - Run debug unit tests specifically

Testing requirements

- All code changes MUST include tests - No code should be committed without corresponding test coverage
- Test both happy path and error scenarios
- Use descriptive test names that clearly explain the scenario being tested
- Mock external dependencies appropriately
- Test error handling and edge cases
- Run tests before committing
- Ensure tests are deterministic - Tests should pass consistently regardless of execution order

### Code Quality
- `./gradlew lint` - Run Android lint checks
- `./gradlew lintDebug` - Run lint on debug build variant

## Development Guidelines

### Code Quality & Testing
- **ALWAYS** run lint and tests before committing changes
- When implementing new features, add corresponding unit tests
- Use Compose testing utilities for UI tests (`androidx.compose.ui.test`)
- Follow existing code patterns and naming conventions

### Architecture Guidelines
- Follow MVVM pattern with Repository pattern for data access
- Use Hilt for dependency injection
- Implement reactive programming with StateFlow and Compose State
- Place business logic in ViewModels, not in UI components

### Key Development Notes
- **Target SDK**: 36, **Min SDK**: 24, **Java**: 11
- Uses Gradle Version Catalogs (`gradle/libs.versions.toml`)
- Firebase must be in **Native Mode** (not Datastore Mode)
- Requires `google-services.json` in `app/` directory
- Web Client ID configuration required in `AuthRepository.kt`

## Firebase Requirements

### Critical Setup Requirements
- **Firestore Database**: Must be Native Mode for real-time listeners
- **Authentication**: Google Sign-in with proper SHA-1 fingerprints configured
- **Document ID Mapping**: Manual mapping required for proper model instantiation
- **Security Rules**: User data isolation enforced through Firestore rules

## Code Organization

### Package Structure
```
com.axeven.profiteerapp/
├── data/              # Data layer (models, repositories, DI)
├── ui/               # UI components (Jetpack Compose screens)
├── utils/            # Utility classes (NumberFormatter, validators)
└── viewmodel/        # ViewModels for business logic
```

### Key Files
- **MainActivity.kt** - Single activity hosting all Compose screens
- **Theme system** - `ui/theme/` (Color.kt, Theme.kt, Type.kt)
- **Models** - `data/model/` (Wallet.kt, Transaction.kt, CurrencyRate.kt)

## Business Logic & Validation

### Critical Business Rules
- **Balance Integrity**: Sum of Logical wallet balances must equal sum of Physical wallet balances
- **Transfer Validation**: Same wallet type AND same currency required
- **Tag System**: Multiple tags per transaction, auto-completion after 3+ characters
- **Currency Conversion**: Default rates → Monthly rates → Warning system

### Data Model Evolution
- **Transactions**: Use `affectedWalletIds` (modern) alongside `walletId` (legacy) for backward compatibility
- **Tags**: Use `tags` array field, maintain `category` field for backward compatibility
- **Default Values**: "Untagged" for transactions without tags/categories

## Known Issues & Solutions

### Google Play Services
- Non-critical SecurityException warnings in logcat are expected
- Handled in ProGuard rules (`app/proguard-rules.pro`) and `AuthRepository.kt`

### Currency Conversion
- System prioritizes default rates, falls back to monthly rates
- Inverse rate calculation when direct rates unavailable
- User warnings displayed when conversion rates missing

## Testing Strategy

- **Unit Tests**: Focus on ViewModels, repositories, and business logic
- **Integration Tests**: Test Firebase integration and data flow
- **UI Tests**: Use Compose testing utilities for screen interactions
- **Validation**: Test business rules and edge cases thoroughly

## Recent Changes to Be Aware Of

### Transaction System Updates
- Merged category and tag concepts into unified tagging system
- Enhanced transfer validation (wallet type + currency matching)
- Simplified wallet selection (one Physical + one Logical per transaction)

### Wallet Management Updates
- Added dedicated wallet list page with Physical/Logical separation
- Implemented unallocated balance tracking and warnings
- Enhanced navigation between home screen and wallet management

### UI/UX Improvements
- Consistent tag display across all screens
- Real-time validation with clear error messages
- Material 3 design system integration

## Development Best Practices

1. **Always check README.md** for comprehensive project context
2. **Run tests** before committing any changes
3. **Follow existing patterns** for consistency
4. **Validate business rules** in all new implementations
5. **Use proper error handling** especially for Firebase operations
6. **Maintain backward compatibility** when modifying data models
7. **Test currency conversion** logic thoroughly
8. **Verify balance integrity** in wallet and transaction operations