package com.coffeeinjection.message.domain.repository

import com.coffeeinjection.message.data.remote.KakaoLoginUrlResponse
import com.coffeeinjection.message.data.remote.LoginResponse
import com.coffeeinjection.message.data.remote.SignupCompleteResponse


/**
 * - ViewModel에서 사용할 인증 도메인 인터페이스
 */
interface AuthRepository {
    suspend fun getKakaoLoginUrl(): KakaoLoginUrlResponse
    suspend fun getKakaoLoginUrlForTest(): KakaoLoginUrlResponse
    suspend fun exchangeCodeToJwt(code: String): LoginResponse
    suspend fun completeSignup(tempJwt: String, nickname: String): SignupCompleteResponse
    suspend fun saveAccessToken(token: String)
}
