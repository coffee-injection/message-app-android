package com.coffeeinjection.message.data.repository

import com.coffeeinjection.message.data.local.TokenDataStore
import com.coffeeinjection.message.data.remote.AuthApi
import com.coffeeinjection.message.data.remote.KakaoLoginRequest
import com.coffeeinjection.message.data.remote.KakaoLoginUrlResponse
import com.coffeeinjection.message.data.remote.LoginResponse
import com.coffeeinjection.message.data.remote.SignupCompleteRequest
import com.coffeeinjection.message.data.remote.SignupCompleteResponse
import com.coffeeinjection.message.domain.repository.AuthRepository
import com.coffeeinjection.message.util.Logger
import javax.inject.Inject

/**
 * - 인증 관련 API/로컬 저장을 캡슐화한 구현체
 * - WebView → code 추출 이후 서버와 교환, 토큰 저장까지 담당
 * - Hilt로 AuthApi / TokenDataStore가 주입됨
 */
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val tokenStore: TokenDataStore
) : AuthRepository {

    // 서버 응답이 래핑 구조기 때문에 응답 래퍼를 만들어 url 추출
    //[kakao] /kakao/login-url raw = {"status":200,"data":{"loginUrl":"https://kauth.kakao.com/oauth/authorize?client_id=fcdef606075e13512243c022e5a852f8&redirect_uri=http://localhost:8080/auth/kakao/callback&response_type=code&prompt=login"},"success":true,"timeStamp":"2025-11-18T22:11:26.466044"}, code=200
    override suspend fun getKakaoLoginUrl(): KakaoLoginUrlResponse {
        val env = api.getKakaoLoginUrl()
        if (env.status != 200 || env.data == null) {
            throw IllegalStateException("kakao/login-url 실패: status=${env.status}, success=${env.success}")
        }
        return env.data
    }

    override suspend fun getKakaoLoginUrlForTest(): KakaoLoginUrlResponse {
        val resp = api.getKakaoLoginUrlRawForTest()
        val raw = resp.body()?.string() ?: resp.errorBody()?.string()
        Logger.error("[kakao] /kakao/login-url raw = $raw, code=${resp.code()}")
        // 여기서 실제 JSON 구조를 보고 DTO/Converter를 맞춰야 함
        // 임시로 파싱:
        // return moshi.adapter(KakaoLoginUrlResponse::class.java).fromJson(raw!!)!!
        throw IllegalStateException("임시 RAW 확인용. 로그를 보고 DTO를 맞추세요.")
    }

    override suspend fun exchangeCodeToJwt(code: String): LoginResponse {
        return api.kakaoLogin(KakaoLoginRequest(code))
    }

    override suspend fun completeSignup(tempJwt: String, nickname: String): SignupCompleteResponse {
        return api.completeSignup("Bearer $tempJwt", SignupCompleteRequest(nickname))
    }

    override suspend fun saveAccessToken(token: String) {
        tokenStore.saveAccessToken(token)
    }
}