package com.coffeeinjection.message.di

import com.coffeeinjection.message.data.repository.AuthRepositoryImpl
import com.coffeeinjection.message.data.repository.MessageRepositoryImpl
import com.coffeeinjection.message.domain.repository.AuthRepository
import com.coffeeinjection.message.domain.repository.MessageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * - 도메인 인터페이스 ↔ 구현체 바인딩 모듈
 * 예시 )
 * “MessageRepository 요청 들어오면 MessageRepositoryImpl 만들어서 줘라”
 * …라고 Hilt에게 알려주는 역할입니다.
 */

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindMessageRepository(
        impl: MessageRepositoryImpl
    ): MessageRepository
}