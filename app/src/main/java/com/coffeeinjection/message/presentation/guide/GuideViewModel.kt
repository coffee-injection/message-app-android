package com.coffeeinjection.message.presentation.guide

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GuideViewModel : ViewModel() {
    private val _viewState = MutableLiveData(GuideViewState.GUIDE_STEP_1)
    val viewState get() = _viewState

    fun skipGuide() {
        _viewState.value = GuideViewState.GUIDE_SKIP
    }

    fun nextGuide() {
        val state = when (viewState.value) {
            GuideViewState.GUIDE_STEP_1 -> GuideViewState.GUIDE_STEP_2
            GuideViewState.GUIDE_STEP_2 -> GuideViewState.GUIDE_STEP_3
            GuideViewState.GUIDE_STEP_3 -> GuideViewState.GUIDE_SKIP
            else -> return
        }
        _viewState.value = state
    }

    fun previousGuide() {
        val state = when (viewState.value) {
            GuideViewState.GUIDE_STEP_3 -> GuideViewState.GUIDE_STEP_2
            GuideViewState.GUIDE_STEP_2 -> GuideViewState.GUIDE_STEP_1
            else -> return
        }
        _viewState.value = state
    }
}

enum class GuideViewState(val index: Int) {
    GUIDE_STEP_1(0), GUIDE_STEP_2(1), GUIDE_STEP_3(2), GUIDE_SKIP(3)
}
