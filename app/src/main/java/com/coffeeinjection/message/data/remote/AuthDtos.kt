package com.coffeeinjection.message.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 서버와 주고받는 인증 관련 DTO 모음(API 가이드 문서 참고)
 */

// ------------------------------------
// 1) GET /kakao/login-url 응답
// ------------------------------------
data class KakaoLoginUrlResponse(
    @Json(name = "loginUrl") val loginUrl: String
)

// ------------------------------------
// 2) POST /login 요청/응답
// ------------------------------------
data class KakaoLoginRequest(
    /**
     * [인가코드]
     * - WebView 리다이렉트 URL (…/auth/kakao/callback?code=XXX) 에서 추출한 code
     */
    @Json(name = "code") val code: String
)

data class LoginResponse(
    @Json(name = "accessToken") val accessToken: String,
    @Json(name = "tokenType") val tokenType: String,
    @Json(name = "expiresIn") val expiresIn: Long,
    @Json(name = "memberId") val memberId: Long?,
    @Json(name = "email") val email: String?,
    @Json(name = "isNewMember") val isNewMember: Boolean
)

// ------------------------------------
// 3) POST /signup/complete 요청/응답
// ------------------------------------
data class SignupCompleteRequest(
    /**
     * [닉네임]
     * - 2~20자 제약이 있음(서버 검증 기준).
     */
    @Json(name = "nickname") val nickname: String
)

data class SignupCompleteResponse(
    @Json(name = "accessToken") val accessToken: String,
    @Json(name = "tokenType") val tokenType: String,
    @Json(name = "expiresIn") val expiresIn: Long,
    @Json(name = "memberId") val memberId: Long?,
    @Json(name = "email") val email: String?,
    @Json(name = "isNewMember") val isNewMember: Boolean
)

@JsonClass(generateAdapter = true)
data class ApiEnvelope<T>(
    @Json(name = "status") val status: Int,
    @Json(name = "data") val data: T?,
    @Json(name = "success") val success: Boolean?,
    @Json(name = "timeStamp") val timeStamp: String?
)