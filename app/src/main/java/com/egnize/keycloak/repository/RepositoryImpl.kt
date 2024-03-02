package com.egnize.keycloak.repository

import kotlinx.coroutines.flow.Flow
import retrofit2.Response

/**
 * @Author: Vinay
 * @Date: 02-03-2024
 */
class RepositoryImpl(private val service: ApiService) : Repository {
    override fun getUserInfo(url: String, bearerToken: String): Flow<Resource<String>> {
        return object : NetworkRepository<String, String>() {
            override suspend fun fetchFromNetwork(): Response<String> =
                service.getUserInfo(url = url, bearerToken = bearerToken)
        }.asFlow()
    }
}