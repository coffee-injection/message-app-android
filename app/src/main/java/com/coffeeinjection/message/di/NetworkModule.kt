package com.coffeeinjection.message.di

import com.coffeeinjection.message.data.remote.api.AuthApi
import com.coffeeinjection.message.data.remote.api.MessageApi
import com.coffeeinjection.message.data.remote.interceptor.AuthInterceptor
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * - Retrofit/OkHttp/AuthApi를 싱글톤으로 제공
 * - 개발 환경에서는 BODY 로깅을 ON.
 */

@Qualifier
annotation class BaseUrl

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @BaseUrl
    @Provides
    fun provideBaseUrl(): String =
        "http://15.164.112.136:8080/api/v1/" //"http://localhost:8080/api/v1/auth/"

    /**
     * 헤더 토큰이 필요한 경우
     */
    @Provides
    @Singleton
    fun provideOkHttp(
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            // ★ 여기서 토큰 추가
            .addInterceptor(authInterceptor)
            // ★ 로그 인터셉터
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi,
        @BaseUrl baseUrl: String
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(okHttpClient)
            .build()

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideMessageApi(
        retrofit: Retrofit
    ): MessageApi =
        retrofit.create(MessageApi::class.java)
}