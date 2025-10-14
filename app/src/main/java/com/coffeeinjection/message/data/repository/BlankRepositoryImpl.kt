package com.coffeeinjection.message.data.repository

import com.coffeeinjection.message.domain.repository.BlankRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

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