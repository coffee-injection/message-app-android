package com.coffeeinjection.message.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 편지 목록 아이템 (리스트에서 한 줄)
 * 실제 응답 JSON 보고 @Json(name = "xxx") 부분 맞춰서 수정하시면 됩니다.
 */
@JsonClass(generateAdapter = true)
data class LetterSummaryDto(
    @Json(name = "letterId") val letterId: Int,
    @Json(name = "content") val content: String,
    @Json(name = "senderName") val senderName: String,
    @Json(name = "status") val status: String,
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "matchedAt") val matchedAt: String?,   // 없을 수도 있으니 nullable 권장
    @Json(name = "readAt") val readAt: String?          // 없을 수도 있으니 nullable 권장
)

/**
 * 편지 상세 응답
 */
@JsonClass(generateAdapter = true)
data class LetterDetailDto(
    @Json(name = "letterId") val letterId: Long,
    @Json(name = "senderNickname") val senderNickname: String,
    @Json(name = "content") val content: String,
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "isBookmarked") val isBookmarked: Boolean
)

/**
 * 편지 발송 요청
 */
@JsonClass(generateAdapter = true)
data class SendLetterRequest(
    @Json(name = "receiverNickname") val receiverNickname: String,
    @Json(name = "content") val content: String
    // 이미지나 기타 옵션 생기면 여기 확장
)

/**
 * 편지 발송 응답
 * (서버에서 letterId 정도만 내려준다고 가정)
 */
@JsonClass(generateAdapter = true)
data class SendLetterResponse(
    @Json(name = "letterId") val letterId: Long?
)

/**
 * 북마크 요청
 */
@JsonClass(generateAdapter = true)
data class BookmarkLetterRequest(
    @Json(name = "letterId") val letterId: Long
)

/**
 * 신고 요청
 */
@JsonClass(generateAdapter = true)
data class ReportLetterRequest(
    @Json(name = "letterId") val letterId: Long,
    @Json(name = "reason") val reason: String?
)
