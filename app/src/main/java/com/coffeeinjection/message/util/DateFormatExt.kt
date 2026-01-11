package com.coffeeinjection.message.util

fun String?.toKoreanDateHourFast(): String {
    val s = this?.trim().orEmpty()
    // 최소 "YYYY-MM-DDTHH:MM" 길이 필요
    if (s.length < 16) return s

    val y = s.substring(0, 4).toIntOrNull() ?: return s
    val m = s.substring(5, 7).toIntOrNull() ?: return s
    val d = s.substring(8, 10).toIntOrNull() ?: return s
    val hh24 = s.substring(11, 13).toIntOrNull() ?: return s
    val mm = s.substring(14, 16).toIntOrNull() ?: return s

    val isAm = hh24 < 12
    val ampm = if (isAm) "오전" else "오후"
    val hh12 = run {
        val h = hh24 % 12
        if (h == 0) 12 else h
    }

    val hhStr = hh12.toString().padStart(2, '0')
    val mmStr = mm.toString().padStart(2, '0')

    return "${y}년 ${m}월 ${d}일 ${ampm} ${hhStr}:${mmStr}"
}
