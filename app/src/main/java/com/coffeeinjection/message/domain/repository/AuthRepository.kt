package com.coffeeinjection.message.domain.repository

import com.coffeeinjection.message.data.local.UserInfo
import com.coffeeinjection.message.data.remote.dto.KakaoLoginUrlResponse
import com.coffeeinjection.message.data.remote.dto.LoginResponse
import com.coffeeinjection.message.data.remote.dto.SignupCompleteResponse


/**
 * - ViewModel에서 사용할 인증 도메인 인터페이스
 */
interface AuthRepository {
    suspend fun getKakaoLoginUrl(): KakaoLoginUrlResponse
    suspend fun exchangeCodeToJwt(code: String): LoginResponse
    suspend fun completeSignup(userinfo: UserInfo): SignupCompleteResponse
    suspend fun saveAccessToken(token: String)
    suspend fun saveUserInfo(userinfo: UserInfo)
}
