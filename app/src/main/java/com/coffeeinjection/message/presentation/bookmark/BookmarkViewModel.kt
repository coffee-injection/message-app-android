package com.coffeeinjection.message.presentation.bookmark

import androidx.lifecycle.ViewModel
import com.coffeeinjection.message.domain.usecase.BlankTestUseCase1
import javax.inject.Inject

class BookmarkViewModel @Inject constructor(
    private val testUseCase1: BlankTestUseCase1,
    private val testUseCase2: BlankTestUseCase1
): ViewModel() {

}