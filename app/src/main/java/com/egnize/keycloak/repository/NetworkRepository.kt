package com.egnize.keycloak.repository

import androidx.annotation.MainThread
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import retrofit2.Response

/**
 * @Author: Vinay
 * @Date: 02-03-2024
 */
abstract class NetworkRepository<RESULT, REQUEST> {

    fun asFlow() = flow {

        val response = fetchFromNetwork()
        val remoteData = response.body()

        if (response.isSuccessful && remoteData != null) {
            emit(Resource.Success(remoteData))
        } else {
            emit(Resource.Failure(response.message()))
        }

    }.catch { e ->
        e.printStackTrace()
        emit(Resource.Failure(e.localizedMessage ?: "Network Error"))
    }

    /**
     * Fetches [Response] from the remote end point.
     */
    @MainThread
    protected abstract suspend fun fetchFromNetwork(): Response<RESULT>
}