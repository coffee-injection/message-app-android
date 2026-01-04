package com.coffeeinjection.message.data.remote.dto

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
    @Json(name = "expiresIn") val expiresIn: Int,
    @Json(name = "memberId") val memberId: Int?,
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
    @Json(name = "nickname") val nickname: String,
    @Json(name = "islandName") val islandName: String,
    @Json(name = "profileImageIndex") val profileImageIndex: Int
)

data class SignupCompleteResponse(
    @Json(name = "accessToken") val accessToken: String,
    @Json(name = "tokenType") val tokenType: String,
    @Json(name = "expiresIn") val expiresIn: Int,
    @Json(name = "memberId") val memberId: Int,
    @Json(name = "email") val email: String?,
    @Json(name = "isNewMember") val isNewMember: Boolean,
    @Json(name = "nickname") val nickname: String,
    @Json(name = "islandName") val islandName: String,
    @Json(name = "profileImageIndex") val profileImageIndex: Int
)

// ------------------------------------
// 4) POST /member/check-nickname 요청/응답
// ------------------------------------
data class CheckNicknameDuplicateRequest(
    @Json(name = "nickname") val nickname: String
)

data class CheckNicknameDuplicateResponse(
    @Json(name = "message") val message: String,
    @Json(name = "available") val available: Boolean
)

