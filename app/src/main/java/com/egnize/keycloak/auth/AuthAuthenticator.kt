package com.egnize.keycloak.auth

import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import com.egnize.keycloak.App
import com.egnize.keycloak.auth.AuthConfiguration.END_SESSION_REDIRECT_URI
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import net.openid.appauth.*
import kotlin.coroutines.resume


private const val TAG = "AuthAuthenticator"

/**
 * Created by Vinay on 02/03/2024.
 *
 * Authenticate user using openid auth.
 *
 * @property context application context is required
 * @property scope coroutine scope required synchronize the authentication response
 */
class AuthAuthenticator(
    private val context: Context,
    private val scope: CoroutineScope,
    private val resultLauncher: ActivityResultLauncher<Intent>,
    private val resultLauncherLogout: ActivityResultLauncher<Intent>?
) {
    private lateinit var activity: Activity
    private var doAuthContinuation: CancellableContinuation<AuthResult>? = null
    private var doClearAuthContinuation: CancellableContinuation<AuthResult>? = null

    /**
     * Configure the auth service from [AuthConfiguration]
     */
    private val serviceConfig by lazy {
        AuthorizationServiceConfiguration(
            AuthConfiguration.AUTH_URI,
            AuthConfiguration.TOKEN_URI,
            AuthConfiguration.REGISTRATION_URI,
            AuthConfiguration.END_SESSION_URI
        )
    }

    /**
     * Create the auth request
     */
    private val authRequestBuilder by lazy {
        AuthorizationRequest.Builder(
            serviceConfig,
            AuthConfiguration.clientId,
            ResponseTypeValues.CODE,
            AuthConfiguration.AUTH_REDIRECT_URI
        )
    }

    /**
     * Build the auth request
     */
    private val authRequest by lazy {
        authRequestBuilder
            .setScope(AuthConfiguration.scopes)
//            .setPrompt("login")
            .build()
    }

    /**
     * Auth service to perform authentication related task
     */
    private val authService by lazy { AuthorizationService(context) }

    /**
     * Attach activity to start authentication and receive result in the activity
     *
     * @param activity
     */
    fun attachActivity(activity: Activity) {
        this.activity = activity
    }

    /**
     * Handle the authentication result
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    fun onActivityResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            if (result.resultCode == Activity.RESULT_CANCELED) {
                // Handle auth cancel
                doAuthContinuation?.resume(AuthResult.Cancel)
            } else {
                extractAuth(result.data)
            }
        } else if (result.resultCode == Activity.RESULT_CANCELED) {
            doAuthContinuation?.resume(AuthResult.Cancel)
        }
//        else if (requestCode == END_SESSION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
//            data?.let { intent ->
//                val resp = AuthorizationResponse.fromIntent(intent)
//                val ex: AuthorizationException? = AuthorizationException.fromIntent(intent)
//                doAuthContinuation?.resume(AuthResult.Success(AuthState(resp, ex)))
//            } ?: doAuthContinuation?.resume(AuthResult.Failed())
//        }
    }

    fun onActivityResultLogout(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { intent ->
                val endSessionResponse = EndSessionResponse.fromIntent(intent)
                val resp = AuthorizationResponse.fromIntent(intent)
                val ex: AuthorizationException? = AuthorizationException.fromIntent(intent)
                if (endSessionResponse != null) {
                    doClearAuthContinuation?.resume(AuthResult.Success(AuthState()))
                } else {
                    doClearAuthContinuation?.resume(AuthResult.Failed())
                }
            } ?: doClearAuthContinuation?.resume(AuthResult.Failed())
        }
    }


    private fun extractAuth(data: Intent?) = scope.launch {
        doAuthContinuation?.resume(data?.let { intent ->
            val resp = AuthorizationResponse.fromIntent(intent)
            val ex: AuthorizationException? = AuthorizationException.fromIntent(intent)
            resp?.let { authRes ->
                authRes.fetchAccessToken(ex).let {
                    if (it.isAuthorized) {
                        writeAuthState(it)
                        AuthResult.Success(it)
                    } else {
                        AuthResult.Failed()
                    }
                }
            } ?: AuthResult.Failed()
        } ?: AuthResult.Failed())
    }

    private suspend fun AuthorizationResponse.fetchAccessToken(ex: AuthorizationException?) =
        suspendCancellableCoroutine<AuthState> { cancelableCoroutine ->
            val authState = AuthState(this, ex)
            authService.performTokenRequest(createTokenExchangeRequest()) { tokenResponse: TokenResponse?, authEx: AuthorizationException? ->
                authState.update(tokenResponse, authEx)
                cancelableCoroutine.resume(authState)
            }
        }

    private suspend fun AuthState.refreshToken() =
        suspendCancellableCoroutine<AuthState> { cancelableCoroutine ->
            authService.performTokenRequest(createTokenRefreshRequest()) { tokenResponse: TokenResponse?, authEx: AuthorizationException? ->
                update(tokenResponse, authEx)
                cancelableCoroutine.resume(this)
            }
        }

    suspend fun doAuthorization() = suspendCancellableCoroutine<AuthResult> { cancelableCoroutine ->
        doAuthContinuation = cancelableCoroutine
        val authIntent = authService.getAuthorizationRequestIntent(authRequest)
        resultLauncher.launch(authIntent)
    }

    suspend fun doClearAuthorization(currentState: AuthState) =
        suspendCancellableCoroutine<AuthResult> { cancelableCoroutine ->
            doClearAuthContinuation = cancelableCoroutine
            val config = currentState.authorizationServiceConfiguration
            if (config?.endSessionEndpoint != null) {
                val endSessionRequest = EndSessionRequest.Builder(config)
//                    .setIdTokenHint(idToken)
                    .setPostLogoutRedirectUri(END_SESSION_REDIRECT_URI)
                    .build()
                val endSessionIntent: Intent = authService.getEndSessionRequestIntent(
                    endSessionRequest
                )

                resultLauncherLogout?.launch(endSessionIntent)
            } else {
                doClearAuthContinuation?.resume(AuthResult.Failed("End session endpoint not configured"))
            }
        }

    suspend fun doRefreshToken(authState: AuthState): AuthResult =
        if (authState.refreshToken.isNullOrEmpty()) {
            AuthResult.Failed("No refresh token found")
        } else {
            authState.refreshToken().let {
                if (it.isAuthorized) {
                    AuthResult.Success(it)
                } else {
                    AuthResult.Failed()
                }
            }
        }

    fun readAuthState(): AuthState? {
        val authPrefs: SharedPreferences =
            App.Companion.appContext.getSharedPreferences("auth", MODE_PRIVATE)
        val stateJson: String? = authPrefs.getString("stateJson", null)
        return if (stateJson != null) {
            AuthState.jsonDeserialize(stateJson)
        } else {
            AuthState()
        }
    }

    fun writeAuthState(state: AuthState?) {
        val authPrefs: SharedPreferences =
            App.Companion.appContext.getSharedPreferences("auth", MODE_PRIVATE)
        authPrefs.edit()
            .putString("stateJson", state?.jsonSerializeString())
            .apply()
    }
}

open class AuthResult {
    class Success(val authState: AuthState) : AuthResult()

    //    class Success<T>(val data: T) : AuthResult()
    object Cancel : AuthResult()
    class Failed(val message: String? = null) : AuthResult()
}