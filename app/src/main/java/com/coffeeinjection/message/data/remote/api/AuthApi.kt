package com.coffeeinjection.message.data.remote.api

import com.coffeeinjection.message.data.remote.dto.ApiEnvelope
import com.coffeeinjection.message.data.remote.dto.KakaoLoginRequest
import com.coffeeinjection.message.data.remote.dto.KakaoLoginUrlResponse
import com.coffeeinjection.message.data.remote.dto.LoginResponse
import com.coffeeinjection.message.data.remote.dto.SignupCompleteRequest
import com.coffeeinjection.message.data.remote.dto.SignupCompleteResponse
import retrofit2.http.*

/**
 * - 서버 인증 API를 정의
 * - Base URL: http://localhost:8080/api/v1/auth
 */
interface AuthApi {
    /** 1) 카카오 로그인 URL 받기 (토큰 필요 없음) */
    @Headers("No-Auth: true")
    @GET("auth/kakao/login-url")
    suspend fun getKakaoLoginUrl(): ApiEnvelope<KakaoLoginUrlResponse>

    /** 2) 인가 코드 → JWT 교환 (토큰 필요 없음) */
    @Headers("No-Auth: true")
    @POST("auth/login")
    suspend fun kakaoLogin(
        @Body req: KakaoLoginRequest
    ): ApiEnvelope<LoginResponse>

    /** 3) 신규 회원 닉네임 완료 (Authorization: Bearer {임시_JWT}) */
    @POST("auth/signup/complete")
    suspend fun completeSignup(
        @Body req: SignupCompleteRequest
    ): ApiEnvelope<SignupCompleteResponse>
}