package com.coffeeinjection.message.data.remote.interceptor

import com.coffeeinjection.message.data.local.AuthDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * - 모든 요청에 Authorization: Bearer <token> 헤더 추가
 * - TokenDataStore 에 저장된 액세스 토큰을 사용
 */
class AuthInterceptor @Inject constructor(
    private val authDataStore: AuthDataStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        // 1) No-Auth 헤더가 있으면 Authorization 붙이지 않고 그대로 보냄
        if (original.header("No-Auth") == "true") {
            val requestWithoutNoAuth = original.newBuilder()
                .removeHeader("No-Auth")   // 서버로는 안 보내도록 제거
                .build()
            return chain.proceed(requestWithoutNoAuth)
        }

        // 2) 나머지 요청은 DataStore 에서 토큰 읽어서 Bearer 붙이기
        val token = runBlocking {
            authDataStore.accessTokenFlow.firstOrNull()
        }

        val newRequest = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }

        return chain.proceed(newRequest)
    }
}
