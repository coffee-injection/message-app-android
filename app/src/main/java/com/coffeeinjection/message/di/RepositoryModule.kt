package com.coffeeinjection.message.di

import com.coffeeinjection.message.data.repository.AuthRepositoryImpl
import com.coffeeinjection.message.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * - 도메인 인터페이스 ↔ 구현체 바인딩 모듈
 */

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository
}