package com.coffeeinjection.message.presentation.bookmark.model

import com.coffeeinjection.message.data.remote.dto.LoadBookmarkResponse

fun LoadBookmarkResponse.toUiModel(): BookmarkModel {
    return BookmarkModel(
        letterId = letterId,
        title = senderName,
        subtitle = "추가해야함",
        preview = content,
        timeText = createdAt,
        isBookmarked = true
    )
}