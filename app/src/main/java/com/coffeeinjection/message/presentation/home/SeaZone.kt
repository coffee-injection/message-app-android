package com.coffeeinjection.message.presentation.home

enum class SeaZone {
    SHALLOW,   // 연한 바다 (위쪽)
    MIDDLE,    // 중간 바다
    DEEP       // 진한 바다 (아래쪽)
}

/**
 * 바다 위에 띄울 병 아이콘 1개에 대한 UI 모델
 */
data class SeaMessageUiModel(
    val letterId: Long,
    val zone: SeaZone,
)
