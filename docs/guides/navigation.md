# Navigation Guidelines

This document provides comprehensive guidance for implementing navigation in the Profiteer Android app using the custom `NavigationStack` architecture.

## Overview

The app uses a **stack-based navigation system** with a custom `NavigationStack` class that manages screen history and back button behavior.

**Location**: `app/src/main/java/com/axeven/profiteerapp/navigation/NavigationStack.kt`

## Key Features

- Stack-based screen history (replaces manual `previousScreen` tracking)
- Automatic Compose recomposition via `SnapshotStateList`
- Integration with Android `BackHandler` for physical back button
- Minimum stack size of 1 (HOME always at bottom)
- API 24+ compatible

## Basic Usage

### Initialization

```kotlin
// In MainActivity
val navigationStack = remember { NavigationStack(AppScreen.HOME) }
val currentScreen = navigationStack.current
```

### Forward Navigation

```kotlin
// Navigate to a new screen
navigationStack.push(AppScreen.SETTINGS)
```

### Back Navigation

```kotlin
// Handled by BackHandler
BackHandler(enabled = navigationStack.canGoBack()) {
    navigationStack.pop()
}
```

## Navigation Stack Operations

| Operation | Behavior | Example | Return Value |
|-----------|----------|---------|--------------|
| `push(screen)` | Add screen to stack | `navigationStack.push(AppScreen.WALLET_LIST)` | Unit |
| `pop()` | Remove top screen | `val prev = navigationStack.pop()` | AppScreen (previous screen) |
| `canGoBack()` | Check if back is possible | `if (navigationStack.canGoBack()) { ... }` | Boolean |
| `peekPrevious()` | View previous without popping | `val prev = navigationStack.peekPrevious()` | AppScreen? |
| `clear(resetTo)` | Reset stack to specific screen | `navigationStack.clear(AppScreen.HOME)` | Unit |
| `getStackTrace()` | Get full stack for debugging | `val trace = navigationStack.getStackTrace()` | String |

## Back Button Behavior

### Physical Back Button

The physical back button behavior varies by screen:

- **On HOME screen** (stack size = 1): Minimizes app (default Android behavior)
- **On other screens**: Navigates to previous screen by popping stack
- **On REAUTH screen**: BLOCKED for security (cannot escape re-authentication)

### BackHandler Integration

```kotlin
// In MainActivity
val canNavigateBack = navigationStack.canGoBack() &&
                      currentScreen != AppScreen.REAUTH &&
                      authState is AuthState.Authenticated

BackHandler(enabled = canNavigateBack) {
    val previousScreen = navigationStack.pop()

    // Handle screen-specific cleanup
    when (currentScreen) {
        AppScreen.CREATE_TRANSACTION -> {
            initialTransactionType = null
            selectedWalletId = null
        }
        AppScreen.EDIT_TRANSACTION -> {
            selectedTransaction = null
        }
        AppScreen.WALLET_DETAIL -> {
            selectedWalletId = null
        }
        else -> {}
    }

    // Trigger home refresh if returning to HOME
    if (previousScreen == AppScreen.HOME) {
        homeViewModel.refreshData()
    }
}
```

### Screen State Cleanup

When navigating back, clean up screen-specific state:

- **CREATE_TRANSACTION**: Clear `initialTransactionType`, `selectedWalletId`
- **EDIT_TRANSACTION**: Clear `selectedTransaction`
- **WALLET_DETAIL**: Clear `selectedWalletId`

## UI Back Button Implementation

### TopAppBar Back Arrow

All screens with a TopAppBar display a back arrow icon (←) that triggers navigation.

```kotlin
// Helper function in MainActivity
val createBackNavigationCallback: () -> Unit = {
    if (navigationStack.canGoBack()) {
        navigationStack.pop()
    }
}

// Usage in screen composition
AppScreen.SETTINGS -> {
    SettingsScreen(
        onNavigateBack = createBackNavigationCallback
    )
}
```

### Key Implementation Points

- UI back button calls the same `navigationStack.pop()` as physical back button
- Helper function eliminates code duplication across 8 screens
- Safety check with `canGoBack()` prevents invalid pops
- Screens receive callback via composition parameter
- Maintains separation of concerns (MainActivity owns navigation)
- Both UI and physical back buttons work consistently

### Affected Screens

Settings, CreateTransaction, EditTransaction, WalletDetail, WalletList, TransactionList, Reports, DiscrepancyDebug

## Navigation Logging

All navigation events are logged with the `Navigation` tag for debugging:

```
Forward: HOME → SETTINGS (stack size: 2)
Back pressed: SETTINGS → HOME (stack size: 1)
REAUTH triggered: pushed REAUTH screen (stack size: 3)
```

**Filter logcat**: `adb logcat -s Navigation`

## Testing

### Unit Tests

- **NavigationStackTest.kt** (39 tests, 100% coverage)
  - Stack operations (push, pop, peek)
  - Edge cases (empty stack, single item)
  - State management and recomposition

- **TopLeftBackButtonTest.kt** (10 tests)
  - UI back button behavior
  - Callback integration

### Integration Tests

- **BackNavigationTest.kt** (11 tests)
  - End-to-end navigation flows
  - State cleanup verification
  - Multi-screen navigation scenarios

### UI Tests

- **BackButtonUiTest.kt** (14 tests)
  - Physical back button behavior
  - UI back button clicks
  - Screen transitions

### Manual Testing

- **Physical back button**: `docs/plans/2025-10-26-back-button-manual-testing-checklist.md`
- **UI back button**: `docs/plans/2025-10-28-manual-testing-guide.md`

## Implementation History

### NavigationStack Implementation (2025-10-25 to 2025-10-26)

- Replaced manual `previousScreen` tracking with stack-based system
- Reduced conditional logic by 80%
- Full documentation: `docs/plans/2025-10-25-back-button.md`

### UI Back Button Fix (2025-10-28)

- Issue: Empty `onNavigateBack` callbacks caused non-functional UI back buttons
- Solution: Added `createBackNavigationCallback` helper function
- Code duplication reduced: 8 inline lambdas → 1 reusable function
- Full documentation: `docs/plans/2025-10-28-fix-back-button.md`

## Best Practices

1. **Always use `canGoBack()` before calling `pop()`**
   ```kotlin
   if (navigationStack.canGoBack()) {
       navigationStack.pop()
   }
   ```

2. **Clean up state when navigating back**
   ```kotlin
   BackHandler(enabled = canNavigateBack) {
       val previousScreen = navigationStack.pop()
       // Clean up current screen state
       clearScreenState()
   }
   ```

3. **Use the same navigation callback for UI and physical back buttons**
   ```kotlin
   val createBackNavigationCallback: () -> Unit = {
       if (navigationStack.canGoBack()) {
           navigationStack.pop()
       }
   }
   ```

4. **Block navigation on security-critical screens**
   ```kotlin
   val canNavigateBack = navigationStack.canGoBack() &&
                         currentScreen != AppScreen.REAUTH
   ```

5. **Log navigation events for debugging**
   ```kotlin
   logger.d("Navigation", "Forward: $currentScreen → $newScreen")
   ```

## Common Patterns

### Navigate to a Screen with Data

```kotlin
// In MainActivity or ViewModel
fun navigateToWalletDetail(walletId: String) {
    selectedWalletId = walletId
    navigationStack.push(AppScreen.WALLET_DETAIL)
}
```

### Conditional Navigation Based on State

```kotlin
// Check authentication before navigating
if (authState is AuthState.Authenticated) {
    navigationStack.push(AppScreen.SETTINGS)
} else {
    navigationStack.push(AppScreen.REAUTH)
}
```

### Navigate and Reset Stack

```kotlin
// Clear stack and start fresh (e.g., after logout)
navigationStack.clear(AppScreen.HOME)
```

### Debug Navigation Stack

```kotlin
// Log current stack trace
logger.d("Navigation", navigationStack.getStackTrace())
```

## Troubleshooting

### Issue: Back button not working

**Check**:
1. Is `canGoBack()` returning true?
2. Is `BackHandler` enabled?
3. Are you on a security screen (REAUTH)?
4. Is `onNavigateBack` callback properly wired?

### Issue: Stack growing too large

**Solution**: Use `clear()` or `pop()` to clean up:
```kotlin
// Reset to home
navigationStack.clear(AppScreen.HOME)
```

### Issue: State persisting after navigation

**Solution**: Clean up state in `BackHandler`:
```kotlin
BackHandler(enabled = canNavigateBack) {
    navigationStack.pop()
    clearScreenState() // Add this
}
```

## Related Documentation

- [MainActivity Implementation](../app/src/main/java/com/axeven/profiteerapp/MainActivity.kt)
- [NavigationStack Class](../app/src/main/java/com/axeven/profiteerapp/navigation/NavigationStack.kt)
- [Back Button Implementation Plan](plans/2025-10-25-back-button.md)
- [UI Back Button Fix](plans/2025-10-28-fix-back-button.md)
