package com.coffeeinjection.message.presentation.bookmark.model

import com.coffeeinjection.message.data.remote.dto.LoadBookmarkResponse

fun LoadBookmarkResponse.toUiModel(): BookmarkModel {
    return BookmarkModel(
        letterId = letterId,
        title = senderName,
        subtitle = senderIslandName,
        preview = content,
        timeText = matchedAt, // 매칭시간
        isBookmarked = true,
        accentColorRes = accentColorById(letterId)
    )
}