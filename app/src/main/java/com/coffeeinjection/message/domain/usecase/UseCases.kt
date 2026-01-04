package com.coffeeinjection.message.domain.usecase

import com.coffeeinjection.message.data.local.UserInfo
import com.coffeeinjection.message.data.remote.dto.KakaoLoginUrlResponse
import com.coffeeinjection.message.data.remote.dto.LoginResponse
import com.coffeeinjection.message.data.remote.dto.SignupCompleteResponse
import com.coffeeinjection.message.domain.repository.AuthRepository
import javax.inject.Inject

class GetKakaoLoginUrlUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): KakaoLoginUrlResponse {
        return authRepository.getKakaoLoginUrl()
    }
}

class ExchangeCodeToJwtUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(code: String): LoginResponse {
        return authRepository.exchangeCodeToJwt(code)
    }
}

class SaveAccessTokenUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(token: String) {
        authRepository.saveAccessToken(token)
    }
}

class SaveUserInfoUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(userInfo: UserInfo) {
        authRepository.saveUserInfo(userInfo)
    }
}

class CompleteSignupUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(userInfo: UserInfo): SignupCompleteResponse {
        return authRepository.completeSignup(userInfo)
    }
}