package com.coffeeinjection.message.presentation.bookmark.model
data class BookmarkModel(
    val letterId: Long,
    val title: String,       // senderName 또는 닉네임 등
    val subtitle: String,    // 예: 지역/섬이름 없으면 빈값
    val preview: String,     // content
    val timeText: String,    // "5시간 전" 같은 표시 문자열
    val isBookmarked: Boolean
)
