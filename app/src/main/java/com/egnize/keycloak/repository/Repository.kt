package com.egnize.keycloak.repository

import kotlinx.coroutines.flow.Flow

/**
 * @Author: Vinay
 * @Date: 02-03-2024
 */
interface Repository {
    fun getUserInfo(
        url: String,
        bearerToken: String
    ): Flow<Resource<String>>
}