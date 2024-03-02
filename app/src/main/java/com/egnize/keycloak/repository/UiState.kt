package com.egnize.keycloak.repository

/**
 * @Author: Vinay
 * @Date: 02-03-2024
 */
sealed class UiState<T> {
    class Loading<T> : UiState<T>()

    data class Success<T>(val data: T) : UiState<T>()

    data class Error<T>(val message: String) : UiState<T>()

    fun isLoading(): Boolean = this is Loading

    fun isSuccessful(): Boolean = this is Success

    fun isFailed(): Boolean = this is Error

    companion object {

        /**
         * Returns [UiState.Loading] instance.
         */
        fun <T> loading() = Loading<T>()

        /**
         * Returns [UiState.Success] instance.
         * @param data Data to emit with status.
         */
        fun <T> success(data: T) =
            Success(data)

        /**
         * Returns [UiState.Error] instance.
         * @param message Description of failure.
         */
        fun <T> error(message: String) =
            Error<T>(message)

           /**
         * Returns [UiState] from [Resource]
         */
        fun <T> fromResource(resource: Resource<T>): UiState<T> = when (resource) {
            is Resource.Success -> success(resource.data)
            is Resource.Failure -> error(resource.message)
        }
    }
}