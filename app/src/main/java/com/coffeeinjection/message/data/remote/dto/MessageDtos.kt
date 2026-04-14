package com.coffeeinjection.message.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 수신한 편지 목록 조회 Dto
 */
@JsonClass(generateAdapter = true)
data class LetterSummary(
    @Json(name = "letterId") val letterId: Long
)

/**
 * 편지 상세 조회 Dto
 */
@JsonClass(generateAdapter = true)
data class Letter(
    @Json(name = "letterId") val letterId: Long,
    @Json(name = "content") val content: String,
    @Json(name = "senderName") val senderName: String,
    @Json(name = "senderIslandName") val senderIslandName: String?,
    @Json(name = "senderProfileImageIndex") val senderProfileImageIndex: Int?,
    @Json(name = "status") val status: String, // WAITING
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "matchedAt") val matchedAt: String?,
    @Json(name = "readAt") val readAt: String?,
)

/**
 * 편지 발송 Request
 */
@JsonClass(generateAdapter = true)
data class SendLetterRequest(
    @Json(name = "content") val content: String
)

/**
 * 편지 발송 Response
 */
@JsonClass(generateAdapter = true)
data class SendLetterResponse(
    @Json(name = "letterId") val letterId: Long,
    @Json(name = "content") val content: String,
    @Json(name = "senderName") val senderName: String,
    @Json(name = "status") val status: String, // WAITING
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "matchedAt") val matchedAt: String?,
    @Json(name = "readAt") val readAt: String?,
)

/**
 * 편지 북마크 Request
 */
@JsonClass(generateAdapter = true)
data class AddBookmarkRequest(
    @Json(name = "letterId") val letterId: Long
)

/**
 * 북마크 삭제 Request
 */
@JsonClass(generateAdapter = true)
data class DeleteBookmarkRequest(
    @Json(name = "letterId") val letterId: Long
)

/**
 * 북마크 리스트 조회 Response
 */
@JsonClass(generateAdapter = true)
data class LoadBookmarkResponse(
    @Json(name = "letterId") val letterId: Long,
    @Json(name = "content") val content: String,
    @Json(name = "senderName") val senderName: String,
    @Json(name = "senderIslandName") val senderIslandName: String?,
    @Json(name = "senderProfileImageIndex") val senderProfileImageIndex: Int?,
    @Json(name = "status") val status: String, // WAITING
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "matchedAt") val matchedAt: String,
    @Json(name = "readAt") val readAt: String,
)

/**
 * 편지 신고 Request
 */
@JsonClass(generateAdapter = true)
data class ReportLetterRequest(
    @Json(name = "letterId") val letterId: Long,
    @Json(name = "reason") val reason: String?
)

/**
 * 발신자 차단 Request
 */
@JsonClass(generateAdapter = true)
data class BlockUserRequest(
    @Json(name = "letterId") val letterId: Long
)

/**
 * 발신자 차단 Response
 */
@JsonClass(generateAdapter = true)
data class BlockUserResponse(
    @Json(name = "blockId") val blockId: Long,
    @Json(name = "blockedMemberId") val blockedMemberId: Long,
    @Json(name = "blockedMemberName") val blockedMemberName: String,
    @Json(name = "blockedAt") val blockedAt: String
)

// ------------------------------------
// 5) PATCH /member/profile 요청/응답
// ------------------------------------
data class ModifyUserProfileRequest(
    @Json(name = "nickname") val nickname: String,
    @Json(name = "islandName") val islandName: String,
    @Json(name = "profileImageIndex") val profileImageIndex: Int
)

data class ModifyUserProfileResponse(
    @Json(name = "memberId") val memberId: Int,
    @Json(name = "nickname") val nickname: String,
    @Json(name = "islandName") val islandName: String,
    @Json(name = "profileImageIndex") val profileImageIndex: Int
)


