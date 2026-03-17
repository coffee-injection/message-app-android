package com.coffeeinjection.message.data.remote.interceptor

import com.coffeeinjection.message.data.local.AuthDataStore
import com.coffeeinjection.message.data.remote.api.AuthApi
import com.coffeeinjection.message.data.remote.dto.RefreshTokenRequest
import com.coffeeinjection.message.util.Logger
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.HttpException
import retrofit2.Retrofit
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val authDataStore: AuthDataStore,
    @com.coffeeinjection.message.di.NoAuthRetrofit private val noAuthRetrofit: Retrofit,
    private val sessionManager: SessionManager
) : Authenticator {

    private val mutex = Mutex()

    // refresh 호출은 "Authenticator 없는 Retrofit"로 해야 순환 호출/무한루프 위험이 감.
    private val refreshApi: AuthApi by lazy { noAuthRetrofit.create(AuthApi::class.java) }

    override fun authenticate(route: Route?, response: Response): Request? {
        // 1) refresh API 자체가 401이면 더 이상 재시도하지 않음(무한루프 방지)
        Logger.error("call refreshToken api 1")

        if (response.request.url.encodedPath.endsWith("/auth/refresh")) {
            sessionManager.markExpired()
            runBlocking { authDataStore.clearForLogout() }
            return null
        }

        // 2) 같은 요청을 계속 재시도하지 않도록 제한(2회)
        if (responseCount(response) >= 2) {
            sessionManager.markExpired()
            return null
        }
        Logger.error("call refreshToken api 2")

        val failedRequest = response.request
        val failedAccessToken = failedRequest.header("Authorization")
            ?.removePrefix("Bearer")
            ?.trim()
        Logger.error("call refreshToken api 3")

        return runBlocking {
            mutex.withLock {
                val currentAccessToken = authDataStore.accessTokenFlow.firstOrNull()
                Logger.error("call refreshToken api 4")

                // 다른 스레드가 이미 refresh 해서 토큰이 바뀌었으면 -> refresh 다시 하지 말고 그 토큰으로 재시도
                if (!currentAccessToken.isNullOrBlank() && currentAccessToken != failedAccessToken) {
                    sessionManager.markRefreshed()
                    return@withLock failedRequest.newBuilder()
                        .header("Authorization", "Bearer $currentAccessToken")
                        .build()
                }
                Logger.error("call refreshToken api 5")

                val refreshToken = authDataStore.refreshTokenFlow.firstOrNull()
                if (refreshToken.isNullOrBlank()) {
                    sessionManager.markExpired()
                    authDataStore.clearForLogout()
                    // refreshToken 없으면 갱신 불가 -> 로그인 만료 처리 쪽으로
                    // authDataStore.clear() 같은 정리 로직이 있으면 여기서 호출 권장
                    return@withLock null
                }

                // refresh 호출
                val envelope = try {
                    Logger.error("call refreshToken api 6")
                    refreshApi.refreshToken(RefreshTokenRequest(refreshToken))
                } catch (e: HttpException) {
                    Logger.error("call refreshToken api HttpException : ${e.code()} / $e")
                    // refresh token 자체가 만료된 경우
                    if (e.code() == 401 || e.code() == 403) {
                        sessionManager.markExpired()
                        authDataStore.clearForLogout()
                    } else {
                        // 서버 오류 등
                        sessionManager.markFailed()
                    }
                    return@withLock null
                } catch (e: Exception) {
                    Logger.error("call refreshToken api Exception : $e")

                    // 네트워크 오류, 타임아웃 등
                    sessionManager.markFailed()
                    return@withLock null
                }
                
                val newAccess = envelope.data?.accessToken
                val newRefresh = envelope.data?.refreshToken

                if (newAccess.isNullOrBlank() || newRefresh.isNullOrBlank()) {
                    sessionManager.markExpired()
                    authDataStore.clearForLogout()
                    return@withLock null
                }

                // 저장
                authDataStore.saveAccessToken(newAccess)
                authDataStore.saveRefreshToken(newRefresh)
                sessionManager.markRefreshed()

                // 원래 요청을 새 토큰으로 재시도
                failedRequest.newBuilder()
                    .header("Authorization", "Bearer $newAccess")
                    .build()
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}