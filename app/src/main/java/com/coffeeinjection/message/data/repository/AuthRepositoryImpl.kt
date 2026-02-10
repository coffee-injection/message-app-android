package com.coffeeinjection.message.data.repository

import com.coffeeinjection.message.data.local.AuthDataStore
import com.coffeeinjection.message.data.local.UserInfo
import com.coffeeinjection.message.data.remote.api.AuthApi
import com.coffeeinjection.message.data.remote.base.ensureSuccessOrThrow
import com.coffeeinjection.message.data.remote.base.requireDataOrThrow
import com.coffeeinjection.message.data.remote.dto.CheckNicknameDuplicateRequest
import com.coffeeinjection.message.data.remote.dto.CheckNicknameDuplicateResponse
import com.coffeeinjection.message.data.remote.dto.FCMTokenRequest
import com.coffeeinjection.message.data.remote.dto.LoginRequest
import com.coffeeinjection.message.data.remote.dto.LoginResponse
import com.coffeeinjection.message.data.remote.dto.LoginUrlResponse
import com.coffeeinjection.message.data.remote.dto.RefreshTokenRequest
import com.coffeeinjection.message.data.remote.dto.SignupCompleteRequest
import com.coffeeinjection.message.data.remote.dto.SignupCompleteResponse
import com.coffeeinjection.message.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
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
    override val userInfoFlow: Flow<UserInfo?> = authStore.userInfoFlow

    // 서버 응답이 래핑 구조기 때문에 응답 래퍼를 만들어 url 추출
    //[kakao] /kakao/login-url raw = {"status":200,"data":{"loginUrl":"https://kauth.kakao.com/oauth/authorize?client_id=fcdef606075e13512243c022e5a852f8&redirect_uri=http://localhost:8080/auth/kakao/callback&response_type=code&prompt=login"},"success":true,"timeStamp":"2025-11-18T22:11:26.466044"}, code=200
    override suspend fun getKakaoLoginUrl(): LoginUrlResponse {
        val env = api.getKakaoLoginUrl()
        return env.requireDataOrThrow("auth/kakao/login-url")
    }

    override suspend fun getGoogleLoginUrl(): LoginUrlResponse {
        val env = api.getGoogleLoginUrl()
        return env.requireDataOrThrow("auth/google/login-url")
    }

    override suspend fun exchangeKakaoCodeToJwt(code: String): LoginResponse {
        val env = api.kakaoLogin(LoginRequest(code))
        return env.requireDataOrThrow("auth/login")
    }

    override suspend fun exchangeGoogleCodeToJwt(code: String): LoginResponse {
        val env = api.googleLogin(LoginRequest(code))
        return env.requireDataOrThrow("auth/google/login")
    }

    override suspend fun completeSignup(userInfo: UserInfo): SignupCompleteResponse {
        val env =  api.completeSignup(SignupCompleteRequest(userInfo.nickName, userInfo.islandName, userInfo.profileImageIndex))
        return env.requireDataOrThrow("auth/signup/complete")
    }

    override suspend fun checkNicknameDuplicate(nickname : String) : CheckNicknameDuplicateResponse {
        val env = api.checkNicknameDuplicate(CheckNicknameDuplicateRequest(nickname))
        return env.requireDataOrThrow("member/check-nickname")
    }

    override suspend fun saveAccessToken(token: String) {
        authStore.saveAccessToken(token)
    }

    override suspend fun saveRefreshToken(token: String) {
        authStore.saveRefreshToken(token)
    }

    override suspend fun saveUserInfo(userinfo: UserInfo) {
        authStore.saveUserInfo(userinfo)
    }

    override suspend fun clearUserInfo() {
        authStore.clearAll()
    }

    override suspend fun registrationFCMToken(token: String) {
        api.registrationFCMToken(FCMTokenRequest(token))
            .ensureSuccessOrThrow("fcm/token")
    }

    override suspend fun deleteFCMToken(token: String) {
        api.deleteFCMToken(token)
            .ensureSuccessOrThrow("fcm/token")
    }

    override suspend fun withdraw(){
        api.withdraw().ensureSuccessOrThrow("auth/withdraw")
    }

    override suspend fun refreshToken(refreshToken : String){
        api.refreshToken(RefreshTokenRequest(refreshToken))
    }

}