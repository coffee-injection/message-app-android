package com.coffeeinjection.message.presentation.bookmark.model

import androidx.annotation.ColorRes
import com.coffeeinjection.message.R

data class BookmarkModel(
    val letterId: Long,
    val title: String,       // senderName 또는 닉네임 등
    val subtitle: String,    // 예: 지역/섬이름 없으면 빈값
    val preview: String,     // content
    val timeText: String,    // "5시간 전" 같은 표시 문자열?
    val isBookmarked: Boolean,
    @ColorRes val accentColorRes: Int
)

private val ACCENT_COLORS = intArrayOf(
    R.color.btn_yellow,
    R.color.btn_green,
    R.color.btn_blue,
    R.color.btn_purple
)

// 아이템마다 고정되게(랜덤처럼) 만들기: letterId로 색 결정
fun accentColorById(letterId: Long): Int {
    val idx = (kotlin.math.abs(letterId.hashCode()) % ACCENT_COLORS.size)
    return ACCENT_COLORS[idx]
}
