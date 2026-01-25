package com.coffeeinjection.message.data.remote.api

import com.coffeeinjection.message.data.remote.base.ApiEnvelope
import com.coffeeinjection.message.data.remote.dto.CheckNicknameDuplicateRequest
import com.coffeeinjection.message.data.remote.dto.CheckNicknameDuplicateResponse
import com.coffeeinjection.message.data.remote.dto.FCMTokenRequest
import com.coffeeinjection.message.data.remote.dto.LoginRequest
import com.coffeeinjection.message.data.remote.dto.LoginResponse
import com.coffeeinjection.message.data.remote.dto.LoginUrlResponse
import com.coffeeinjection.message.data.remote.dto.SignupCompleteRequest
import com.coffeeinjection.message.data.remote.dto.SignupCompleteResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * - 서버 인증 API를 정의
 */
interface AuthApi {
    /** kakao 로그인 URL 받기 (토큰 필요 없음) */
    @Headers("No-Auth: true")
    @GET("auth/kakao/login-url")
    suspend fun getKakaoLoginUrl(): ApiEnvelope<LoginUrlResponse>

    /** google 로그인 URL 받기 (토큰 필요 없음) */
    @Headers("No-Auth: true")
    @GET("auth/google/login-url")
    suspend fun getGoogleLoginUrl(): ApiEnvelope<LoginUrlResponse>

    /** kakao 인가 코드 → JWT 교환 (토큰 필요 없음) */
    @Headers("No-Auth: true")
    @POST("auth/login")
    suspend fun kakaoLogin(
        @Body req: LoginRequest
    ): ApiEnvelope<LoginResponse>

    /** google 인가 코드 → JWT 교환 (토큰 필요 없음) */
    @Headers("No-Auth: true")
    @POST("auth/google/login")
    suspend fun googleLogin(
        @Body req: LoginRequest
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

    /** Firebase FCM token 등록*/
    @POST("fcm/token")
    suspend fun registrationFCMToken(
        @Body req: FCMTokenRequest
    ): ApiEnvelope<Any?>

    /** Firebase FCM token 삭제 */
    @DELETE("fcm/token")
    suspend fun deleteFCMToken(
        @Query("fcmToken") fcmToken: String
    ): ApiEnvelope<Any?>

    /** 회원 탈퇴 */
    @DELETE("auth/withdraw")
    suspend fun withdraw(): ApiEnvelope<Any?>
}