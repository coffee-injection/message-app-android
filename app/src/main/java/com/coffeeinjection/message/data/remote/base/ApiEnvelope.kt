package com.coffeeinjection.message.data.remote.base

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Base Result class (서버 응답을 감싸는 공통 래퍼 클래스)
 */
@JsonClass(generateAdapter = true)
data class ApiEnvelope<T>(
    @Json(name = "status") val status: Int,
    @Json(name = "data") val data: T?,
    @Json(name = "success") val success: Boolean?,
    @Json(name = "timeStamp") val timeStamp: String?
)

