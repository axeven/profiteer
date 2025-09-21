# Credential Debugging Guide

## How to Identify Which Credential is Expired

When you see a `PERMISSION_DENIED` error from Firestore, the app now automatically provides detailed diagnostic information to help you identify the root cause.

## Automatic Diagnostics

When a permission error occurs, the app will automatically:

1. **Show a user-friendly error message** to the user
2. **Log detailed diagnostic information** to help developers identify the issue
3. **Run comprehensive credential checks** in the background

## Reading the Diagnostic Logs

Look for these log entries in your logcat:

### Error Detection
```
TransactionRepo: Authentication error in getUserTransactions - running diagnostics...
```

### Detailed Debug Information
```
FirestoreErrorHandler: Firestore error occurred: [detailed debug info]
```

### Diagnostic Summary
```
CredentialDiagnostics: === DIAGNOSTIC SUMMARY ===
CredentialDiagnostics: Overall Status: CRITICAL - Configuration errors detected
CredentialDiagnostics: ✓ Firebase Auth: User authenticated, token valid
CredentialDiagnostics: ❌ Firestore: Connection failed: PERMISSION_DENIED
CredentialDiagnostics: ✓ App Configuration: Configuration files present
CredentialDiagnostics: Recommendations:
CredentialDiagnostics:   • Check Firestore security rules and re-authenticate
```

## Manual Diagnostic Trigger

For development/debugging purposes, you can add the diagnostic button to any screen:

```kotlin
import com.axeven.profiteerapp.ui.components.DiagnosticsButton

@Composable
fun YourScreen() {
    // ... your screen content

    // Add this for debugging (remove in production)
    DiagnosticsButton()
}
```

## Understanding the Diagnostic Report

### Credential Status Types

- **✓ VALID**: Credential is working correctly
- **⚠️ EXPIRED**: Credential has expired and needs refresh
- **❌ ERROR**: Configuration or connection error
- **❓ UNKNOWN**: Status could not be determined

### What Each Component Checks

1. **Firebase Auth**
   - User authentication status
   - ID token validity
   - Token refresh capability
   - Last sign-in time

2. **Firestore**
   - Connection status
   - Permission level
   - Security rule compliance

3. **Google Sign-In**
   - Configuration validation
   - OAuth client setup

4. **App Configuration**
   - google-services.json presence
   - Package configuration

## Common Issues and Solutions

### 1. Firebase Auth Token Expired
**Symptoms**: `PERMISSION_DENIED` errors, user is signed in but can't access data
**Solution**: Sign out and re-authenticate the user

### 2. Firestore Security Rules
**Symptoms**: Specific operations fail while others work
**Solution**: Check and update Firestore security rules

### 3. Google Sign-In Configuration
**Symptoms**: Sign-in fails or tokens are invalid
**Solution**: Verify SHA-1 fingerprints and OAuth client configuration

### 4. App Configuration Issues
**Symptoms**: Firebase services fail to initialize
**Solution**: Ensure google-services.json is correctly placed and configured

## Debug Information Details

The diagnostic system provides comprehensive information including:

- **User Details**: UID, email, verification status
- **Token Status**: Cached vs fresh token validation
- **Provider Information**: Which sign-in providers are configured
- **Timestamps**: Last sign-in, account creation times
- **Error Context**: Stack traces and operation details
- **System Info**: App package, thread information

## Best Practices

1. **Check logs immediately** when permission errors occur
2. **Look for patterns** - are errors happening for specific operations?
3. **Verify timestamps** - when did the user last sign in successfully?
4. **Test token refresh** - can the system get a fresh token?
5. **Validate configuration** - are all Firebase services properly set up?

## Production Considerations

- The diagnostic logging is designed to be helpful during development
- Consider reducing log verbosity in production builds
- The diagnostic UI component should be removed from production builds
- Sensitive information is logged carefully (no actual tokens or passwords)

## Quick Troubleshooting Checklist

When you see "Your session has expired. Please sign in again to continue.":

1. **Check the diagnostic logs** for specific credential status
2. **Verify the user's last sign-in time** - has it been too long?
3. **Test token refresh** - is the refresh failing?
4. **Check Firestore rules** - have they changed recently?
5. **Validate app configuration** - is google-services.json current?
6. **Try manual re-authentication** - does signing out/in fix it?

This comprehensive debugging system will help you quickly identify whether the issue is with:
- Expired authentication tokens
- Firestore security rules
- Google Sign-In configuration
- App setup and configuration
- Network connectivity issues