package com.coffeeinjection.message.data.remote.api

import com.coffeeinjection.message.data.remote.base.ApiEnvelope
import com.coffeeinjection.message.data.remote.dto.CheckNicknameDuplicateRequest
import com.coffeeinjection.message.data.remote.dto.CheckNicknameDuplicateResponse
import com.coffeeinjection.message.data.remote.dto.KakaoLoginRequest
import com.coffeeinjection.message.data.remote.dto.KakaoLoginUrlResponse
import com.coffeeinjection.message.data.remote.dto.LoginResponse
import com.coffeeinjection.message.data.remote.dto.ModifyUserProfileRequest
import com.coffeeinjection.message.data.remote.dto.ModifyUserProfileResponse
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

    /** 3) 신규 회원 닉네임 완료 */
    @POST("auth/signup/complete")
    suspend fun completeSignup(
        @Body req: SignupCompleteRequest
    ): ApiEnvelope<SignupCompleteResponse>

    /** 4) 닉네임 중복 체크 */
    @Headers("No-Auth: true")
    @POST("member/check-nickname")
    suspend fun checkNicknameDuplicate(
        @Body req: CheckNicknameDuplicateRequest
    ): ApiEnvelope<CheckNicknameDuplicateResponse>

    /** 5) 프로필 정보 수정 */
    @Headers("No-Auth: true")
    @PATCH("member/profile")
    suspend fun modifyUserProfile(
        @Body req: ModifyUserProfileRequest
    ): ApiEnvelope<ModifyUserProfileResponse>
}