package com.egnize.keycloak

import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.egnize.keycloak.auth.AuthAuthenticator
import com.egnize.keycloak.auth.AuthResult
import kotlinx.coroutines.launch

/**
 * @Author: Vinay
 * @Date: 02-03-2024
 */
fun Fragment.doAuth(
    openIdAuth: AuthAuthenticator?,
    viewModel: AuthViewModel
) {
    lifecycleScope.launch {
        openIdAuth?.doAuthorization()
            .let {
                when (it) {
                    is AuthResult.Success -> {
                        // Detail of authentication
                        if (it.authState.accessToken != null) {
                            viewModel.getUserInfo(token = it.authState.accessToken)
                        }
                    }

                    is AuthResult.Cancel -> {
                        // Authentication canceled
                    }

                    is AuthResult.Failed -> {
                        //Authentication failed
                    }
                }
            }
    }
}

fun Fragment.clearAuth(openIdAuth: AuthAuthenticator?) {
    lifecycleScope.launch {
        val authState = openIdAuth?.readAuthState()
        authState?.let { currentAuthState ->
            openIdAuth.doClearAuthorization(currentAuthState)
                .let {
                    when (it) {
                        is AuthResult.Success -> {
                            logout()
                        }

                        is AuthResult.Failed -> {
                            // Authentication failed
                        }

                    }

                }
        } ?: "error message"
    }

}

fun Fragment.logout() {
    //clear all saved data
    findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
}
