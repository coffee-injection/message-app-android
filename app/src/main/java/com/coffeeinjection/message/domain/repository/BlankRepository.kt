package com.coffeeinjection.message.domain.repository

import kotlinx.coroutines.flow.Flow

interface BlankRepository {
    fun blankTest1(t: Boolean): Flow<Int>
    fun blankTest2(t: Boolean): Flow<Int>
}