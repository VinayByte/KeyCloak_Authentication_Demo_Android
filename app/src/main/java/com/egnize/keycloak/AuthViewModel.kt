package com.egnize.keycloak

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egnize.keycloak.auth.AuthConfiguration
import com.egnize.keycloak.repository.Repository
import com.egnize.keycloak.repository.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @Author: Vinay
 * @Date: 02-03-2024
 */
@HiltViewModel
class AuthViewModel @Inject internal constructor(
    private val repository: Repository
) : ViewModel() {

    private val _userInfo: MutableStateFlow<UiState<String>> = MutableStateFlow(UiState.loading())
    val userInfo: StateFlow<UiState<String>> = _userInfo

    /**
     * @param token  get userinfo from keycloak
     *
     */
    fun getUserInfo(token: String?) {
        viewModelScope.launch {
            _userInfo.value = UiState.loading()
            repository.getUserInfo(
                url = AuthConfiguration.USER_INFO_URI,
                bearerToken = "Bearer $token"
            )
                .map { resource -> UiState.fromResource(resource) }
                .collect { state -> _userInfo.value = state }
        }
    }
}