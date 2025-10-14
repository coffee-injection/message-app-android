package com.coffeeinjection.message.domain.usecase

import com.coffeeinjection.message.domain.repository.BlankRepository
import kotlinx.coroutines.flow.Flow

class BlankTestUseCase2 constructor(
    private val repository: BlankRepository
) {
    operator fun invoke(t: Boolean): Flow<Int> = repository.blankTest2(t)
}