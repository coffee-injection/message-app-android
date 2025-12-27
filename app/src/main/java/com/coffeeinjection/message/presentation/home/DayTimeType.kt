package com.coffeeinjection.message.presentation.home

enum class DayTimeType {
    DAWN,      // 04:00 ~ 07:59 - 새벽(해 뜨는 시간대)
    MORNING,   // 08:00 ~ 10:59 - 아침
    DAYTIME,   // 11:00 ~ 15:59 - 점심(낮)
    SUNSET,    // 16:00 ~ 18:59 - 저녁(노을)
    NIGHT      // 19:00 ~ 03:59 - 저녁(밤)
}