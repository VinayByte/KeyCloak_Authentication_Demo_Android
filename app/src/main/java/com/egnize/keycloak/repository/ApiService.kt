package com.egnize.keycloak.repository

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Url

/**
 * @Author: Vinay
 * @Date: 02-03-2024
 */
interface ApiService {
    @Headers("Content-Type: application/json")
    @GET
    suspend fun getUserInfo(
        @Url url: String,
        @Header("Authorization") bearerToken: String
    ): Response<String>

}