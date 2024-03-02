package com.egnize.keycloak.repository

/**
 * @Author: Vinay
 * @Date: 02-03-2024
 */
sealed class Resource<T> {
    class Success<T>(val data: T) : Resource<T>()
    class Failure<T>(val message: String) : Resource<T>()
}