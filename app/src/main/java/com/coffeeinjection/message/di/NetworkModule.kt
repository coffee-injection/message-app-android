package com.coffeeinjection.message.di

import com.coffeeinjection.message.BuildConfig
import com.coffeeinjection.message.data.remote.api.AuthApi
import com.coffeeinjection.message.data.remote.api.MessageApi
import com.coffeeinjection.message.data.remote.interceptor.AuthInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
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

// -------------------------
// Qualifier 정의
// -------------------------
@Qualifier annotation class NoAuthClient
@Qualifier annotation class AuthClient
@Qualifier annotation class NoAuthRetrofit
@Qualifier annotation class AuthRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideMoshi(): Moshi =
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @Provides @Singleton
    fun provideLogging(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }

    @Provides @Singleton @NoAuthClient
    fun provideNoAuthOkHttp(logging: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

    @Provides @Singleton @AuthClient
    fun provideAuthOkHttp(
        authInterceptor: AuthInterceptor,
        logging: HttpLoggingInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()

    @Provides @Singleton @NoAuthRetrofit
    fun provideNoAuthRetrofit(
        moshi: Moshi,
        @NoAuthClient client: OkHttpClient
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(client)
            .build()

    @Provides @Singleton @AuthRetrofit
    fun provideAuthRetrofit(
        moshi: Moshi,
        @AuthClient client: OkHttpClient
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(client)
            .build()

    // ✅ 여기만 중요: AuthApi는 토큰 없는 Retrofit로 생성
    @Provides @Singleton
    fun provideAuthApi(@NoAuthRetrofit retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    // ✅ MessageApi는 토큰 있는 Retrofit로 생성
    @Provides @Singleton
    fun provideMessageApi(@AuthRetrofit retrofit: Retrofit): MessageApi =
        retrofit.create(MessageApi::class.java)
}
