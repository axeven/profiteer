package com.axeven.profiteerapp.service

import android.app.Activity
import android.content.Context
import com.axeven.profiteerapp.data.repository.AuthRepository
import com.axeven.profiteerapp.utils.logging.Logger
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.sheets.v4.Sheets
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

/**
 * Unit tests for GoogleSheetsService following TDD approach
 */
class GoogleSheetsServiceTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var authRepository: AuthRepository

    @Mock
    private lateinit var logger: Logger

    @Mock
    private lateinit var googleSignInAccount: GoogleSignInAccount

    @Mock
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var googleSheetsService: GoogleSheetsService

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        googleSheetsService = GoogleSheetsService(context, authRepository, logger)
    }

    // Tests for isAuthorized()

    @Test
    fun `isAuthorized returns true when user is authenticated with Sheets scope`() {
        // Given: User is signed in with Sheets scope
        whenever(authRepository.hasSheetsScope()).thenReturn(true)

        // When: Checking authorization
        val result = googleSheetsService.isAuthorized()

        // Then: Should return true
        assertTrue(result)
        verify(authRepository).hasSheetsScope()
    }

    @Test
    fun `isAuthorized returns false when user lacks Sheets scope`() {
        // Given: User is signed in but lacks Sheets scope
        whenever(authRepository.hasSheetsScope()).thenReturn(false)

        // When: Checking authorization
        val result = googleSheetsService.isAuthorized()

        // Then: Should return false
        assertFalse(result)
        verify(authRepository).hasSheetsScope()
    }

    @Test
    fun `isAuthorized returns false when user is not signed in`() {
        // Given: User is not signed in
        whenever(authRepository.hasSheetsScope()).thenReturn(false)

        // When: Checking authorization
        val result = googleSheetsService.isAuthorized()

        // Then: Should return false
        assertFalse(result)
    }

    // Tests for requestAuthorization()

    @Ignore("Requires Android instrumentation - GoogleSignInClient.signInIntent needs real Android context. Will be covered in Phase 8 integration tests.")
    @Test
    fun `requestAuthorization returns success when authorization granted`() {
        // Given: Auth repository provides client with sheets scope
        whenever(authRepository.getGoogleSignInClientWithSheets()).thenReturn(googleSignInClient)

        // When: Requesting authorization
        val intent = googleSheetsService.requestAuthorization()

        // Then: Should return sign-in intent
        assertNotNull(intent)
        verify(authRepository).getGoogleSignInClientWithSheets()
    }

    @Ignore("Requires Android instrumentation - GoogleSignInClient.signInIntent needs real Android context. Will be covered in Phase 8 integration tests.")
    @Test
    fun `requestAuthorization logs the authorization request`() {
        // Given: Auth repository provides client
        whenever(authRepository.getGoogleSignInClientWithSheets()).thenReturn(googleSignInClient)

        // When: Requesting authorization
        googleSheetsService.requestAuthorization()

        // Then: Should log the request
        verify(logger).d("GoogleSheetsService", "Requesting Google Sheets authorization")
    }

    // Tests for createSheetsService()

    @Ignore("Requires Android instrumentation - GoogleAccountCredential creation needs real Android context. Will be covered in Phase 8 integration tests.")
    @Test
    fun `createSheetsService returns Sheets instance with valid credentials`() = runTest {
        // Given: User has sheets scope and valid account
        val userEmail = "test@example.com"
        whenever(authRepository.hasSheetsScope()).thenReturn(true)
        whenever(authRepository.getCurrentUserEmail()).thenReturn(userEmail)

        // When: Creating sheets service
        val result = googleSheetsService.createSheetsService()

        // Then: Should return Success with Sheets instance
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }

    @Test
    fun `createSheetsService returns failure when not authorized`() = runTest {
        // Given: User lacks sheets scope
        whenever(authRepository.hasSheetsScope()).thenReturn(false)

        // When: Creating sheets service
        val result = googleSheetsService.createSheetsService()

        // Then: Should return Failure
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertEquals("User is not authorized for Google Sheets", exception!!.message)
    }

    @Ignore("Requires Android instrumentation - GoogleAccountCredential creation needs real Android context. Will be covered in Phase 8 integration tests.")
    @Test
    fun `createSheetsService logs successful service creation`() = runTest {
        // Given: User has valid authorization
        val userEmail = "test@example.com"
        whenever(authRepository.hasSheetsScope()).thenReturn(true)
        whenever(authRepository.getCurrentUserEmail()).thenReturn(userEmail)

        // When: Creating sheets service
        googleSheetsService.createSheetsService()

        // Then: Should log success
        verify(logger).d("GoogleSheetsService", "Creating Google Sheets service")
        verify(logger).i(eq("GoogleSheetsService"), argThat { contains("Google Sheets service created successfully") })
    }

    @Test
    fun `createSheetsService logs failure when not authorized`() = runTest {
        // Given: User is not authorized
        whenever(authRepository.hasSheetsScope()).thenReturn(false)

        // When: Creating sheets service
        googleSheetsService.createSheetsService()

        // Then: Should log error
        verify(logger).e(eq("GoogleSheetsService"), argThat { contains("Failed to create Google Sheets service") }, any())
    }
}
