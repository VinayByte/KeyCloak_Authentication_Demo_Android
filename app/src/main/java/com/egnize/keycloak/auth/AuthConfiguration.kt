package com.egnize.keycloak.auth

import android.net.Uri


/**
 * Created by Vinay on 02/03/2024.
 */
object AuthConfiguration {
    const val clientId = ""
    const val scopes = "openid email profile"
    private const val appPackage = ""
    private const val OPENID_BASE_URL = ""
    val AUTH_REDIRECT_URI: Uri = Uri.parse("$appPackage:/oauth2redirect")
    val END_SESSION_REDIRECT_URI: Uri = Uri.parse("$appPackage:/oauth2redirect")
    val AUTH_URI: Uri = Uri.parse("$OPENID_BASE_URL/auth")
    val TOKEN_URI: Uri = Uri.parse("$OPENID_BASE_URL/token")
    val REGISTRATION_URI: Uri = Uri.parse("")
    val END_SESSION_URI: Uri =
        Uri.parse("")
    val DISCOVERY_URI: Uri =
        Uri.parse("${"BASE_URL"}/<-->/.well-known/openid-configuration")
    const val USER_INFO_URI = "$OPENID_BASE_URL/userinfo"
}