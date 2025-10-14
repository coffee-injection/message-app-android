package com.coffeeinjection.message.domain.usecase

import com.coffeeinjection.message.domain.repository.BlankRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BlankTestUseCase2 @Inject constructor(
    private val repository: BlankRepository
) {
    operator fun invoke(t: Boolean): Flow<Int> = repository.blankTest2(t)
}