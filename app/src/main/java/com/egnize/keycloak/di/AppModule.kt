package com.egnize.keycloak.di

import com.egnize.keycloak.repository.ApiService
import com.egnize.keycloak.repository.Repository
import com.egnize.keycloak.repository.RepositoryImpl
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * @Author: Vinay
 * @Date: 02-03-2024
 */
@InstallIn(SingletonComponent::class)
@Module
object AppModule {
    @Singleton
    @Provides
    fun provideOkHttp(httpLoggingInterceptor: HttpLoggingInterceptor?): OkHttpClient {
        val builder = OkHttpClient.Builder()
        httpLoggingInterceptor?.let { interceptor ->
            builder.addNetworkInterceptor(interceptor)
        }
        builder.followRedirects(false)
        return builder.build()
    }

    @Singleton
    @Provides
    fun provideRetrofitService(okHttp: OkHttpClient): Retrofit {
        val builder = Retrofit.Builder()
            .baseUrl("https://localhost.com/")
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttp)
        return builder.build()
    }

    @Singleton
    @Provides
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    }

    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit) = retrofit.create(ApiService::class.java)

    @Singleton
    @Provides
    internal fun provideRepository(
        apiService: ApiService,
    ): Repository = RepositoryImpl(apiService)
}