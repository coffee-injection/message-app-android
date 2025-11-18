package com.coffeeinjection.message.data.remote

import retrofit2.http.*

/**
 * - 서버 인증 API를 정의
 * - Base URL: http://localhost:8080/api/v1/auth
 */
interface AuthApi {
    /** 1) 카카오 로그인 URL 받기 */
    @GET("kakao/login-url")
    suspend fun getKakaoLoginUrl(): ApiEnvelope<KakaoLoginUrlResponse>


    @GET("kakao/login-url")
    suspend fun getKakaoLoginUrlRawForTest(): retrofit2.Response<okhttp3.ResponseBody>

    /** 2) 인가 코드 → JWT 교환 */
    @POST("login")
    suspend fun kakaoLogin(@Body req: KakaoLoginRequest): LoginResponse

    /** 3) 신규 회원 닉네임 완료 (Authorization: Bearer {임시_JWT}) */
    @POST("signup/complete")
    suspend fun completeSignup(
        @Header("Authorization") bearerToken: String,
        @Body req: SignupCompleteRequest
    ): SignupCompleteResponse
}