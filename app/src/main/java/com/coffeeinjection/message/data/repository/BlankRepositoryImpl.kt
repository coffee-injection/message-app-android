package com.coffeeinjection.message.data.repository

import com.coffeeinjection.message.domain.repository.BlankRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class BlankRepositoryImpl @Inject constructor(
    // todo api, dao 연결 예정
) : BlankRepository {
    override fun blankTest1(t: Boolean): Flow<Int> {
        TODO("Not yet implemented")
    }

    override fun blankTest2(t: Boolean): Flow<Int> {
        TODO("Not yet implemented")
    }
}