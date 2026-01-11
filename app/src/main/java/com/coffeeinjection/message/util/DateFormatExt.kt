package com.coffeeinjection.message.util

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

// 편지 읽기 다이얼로그에서 작성된 편지 시간을 표시해주는 util
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

// 북마크에서 에서 매칭된 편지 시간을 표시해주는 util
fun String?.toKoreanRelativeOrMdH(): String {
    val s = this?.trim().orEmpty()
    if (s.length < 16) return s

    // "YYYY-MM-DDTHH:MM"까지만 사용 (초/나노초는 무시)
    val base = s.substring(0, 16)

    val dt = runCatching {
        val y = base.substring(0, 4).toInt()
        val m = base.substring(5, 7).toInt()
        val d = base.substring(8, 10).toInt()
        val hh = base.substring(11, 13).toInt()
        val mm = base.substring(14, 16).toInt()
        LocalDateTime.of(y, m, d, hh, mm)
    }.getOrElse { return s }

    val now = LocalDateTime.now(ZoneId.systemDefault())

    // 미래 시간이 들어오면 그냥 "M월 d일 H시"로
    if (dt.isAfter(now)) {
        return "${dt.monthValue}월 ${dt.dayOfMonth}일 ${dt.hour}시"
    }

    val minutes = ChronoUnit.MINUTES.between(dt, now)
    return if (minutes < 60) {
        val m = minutes.coerceAtLeast(1)
        "${m}분 전"
    } else if (minutes < 60 * 24) {
        val h = (minutes / 60).coerceAtLeast(1)
        "${h}시간 전"
    } else {
        "${dt.monthValue}월 ${dt.dayOfMonth}일 ${dt.hour}시"
    }
}
