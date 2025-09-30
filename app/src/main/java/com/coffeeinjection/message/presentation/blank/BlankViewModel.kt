package com.coffeeinjection.message.presentation.blank

import androidx.lifecycle.ViewModel
import com.coffeeinjection.message.domain.usecase.BlankTestUseCase1
import jakarta.inject.Inject

class BlankViewModel @Inject constructor(
    private val testUseCase1: BlankTestUseCase1,
    private val testUseCase2: BlankTestUseCase1
): ViewModel() {

}