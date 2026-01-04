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
    @Json(name = "matchedAt") val matchedAt: String,
    @Json(name = "readAt") val readAt: String,
)

/**
 * 편지 북마크 Request
 */
@JsonClass(generateAdapter = true)
data class BookmarkLetterRequest(
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
