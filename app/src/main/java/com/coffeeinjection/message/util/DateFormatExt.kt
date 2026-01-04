package com.coffeeinjection.message.util

fun String?.toKoreanDateHourFast(): String {
    val s = this?.trim().orEmpty()
    if (s.length < 13) return s

    val yy = s.substring(2, 4)
    val m = s.substring(5, 7).toIntOrNull() ?: return s
    val d = s.substring(8, 10).toIntOrNull() ?: return s
    val h = s.substring(11, 13).toIntOrNull() ?: return s

    return "${yy}년 ${m}월 ${d}일 ${h}시"
}
