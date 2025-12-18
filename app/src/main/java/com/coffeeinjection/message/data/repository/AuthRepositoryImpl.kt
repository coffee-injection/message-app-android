package com.coffeeinjection.message.data.repository

import com.coffeeinjection.message.data.local.AuthDataStore
import com.coffeeinjection.message.data.local.UserInfo
import com.coffeeinjection.message.data.remote.api.AuthApi
import com.coffeeinjection.message.data.remote.requireDataOrThrow
import com.coffeeinjection.message.data.remote.dto.KakaoLoginRequest
import com.coffeeinjection.message.data.remote.dto.KakaoLoginUrlResponse
import com.coffeeinjection.message.data.remote.dto.LoginResponse
import com.coffeeinjection.message.data.remote.dto.SignupCompleteRequest
import com.coffeeinjection.message.data.remote.dto.SignupCompleteResponse
import com.coffeeinjection.message.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * - 인증 관련 API/로컬 저장을 캡슐화한 구현체
 * - WebView → code 추출 이후 서버와 교환, 토큰 저장까지 담당
 * - Hilt로 AuthApi / AuthDataStore 주입됨
 */
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val authStore: AuthDataStore
) : AuthRepository {

    // 서버 응답이 래핑 구조기 때문에 응답 래퍼를 만들어 url 추출
    //[kakao] /kakao/login-url raw = {"status":200,"data":{"loginUrl":"https://kauth.kakao.com/oauth/authorize?client_id=fcdef606075e13512243c022e5a852f8&redirect_uri=http://localhost:8080/auth/kakao/callback&response_type=code&prompt=login"},"success":true,"timeStamp":"2025-11-18T22:11:26.466044"}, code=200
    override suspend fun getKakaoLoginUrl(): KakaoLoginUrlResponse {
        val env = api.getKakaoLoginUrl()
        return env.requireDataOrThrow("kakao/login-url")
    }

    override suspend fun exchangeCodeToJwt(code: String): LoginResponse {
        val env = api.kakaoLogin(KakaoLoginRequest(code))
        return env.requireDataOrThrow("kakao/exchangeCodeToJwt")
    }

    override suspend fun completeSignup(nickname: String): SignupCompleteResponse {
        val env =  api.completeSignup(SignupCompleteRequest(nickname))
        return env.requireDataOrThrow("kakao/completeSignup")
    }

    override suspend fun saveAccessToken(token: String) {
        authStore.saveAccessToken(token)
    }

    override suspend fun saveUserInfo(userinfo: UserInfo) {
        authStore.saveUserInfo(userinfo)
    }
}