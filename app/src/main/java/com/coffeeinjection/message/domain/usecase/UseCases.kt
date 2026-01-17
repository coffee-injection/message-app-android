package com.coffeeinjection.message.domain.usecase

import com.coffeeinjection.message.data.local.UserInfo
import com.coffeeinjection.message.data.remote.dto.CheckNicknameDuplicateResponse
import com.coffeeinjection.message.data.remote.dto.LoginResponse
import com.coffeeinjection.message.data.remote.dto.LoginUrlResponse
import com.coffeeinjection.message.data.remote.dto.ModifyUserProfileResponse
import com.coffeeinjection.message.data.remote.dto.SignupCompleteResponse
import com.coffeeinjection.message.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveUserInfoUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<UserInfo?> = authRepository.userInfoFlow
}

class GetKakaoLoginUrlUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): LoginUrlResponse {
        return authRepository.getKakaoLoginUrl()
    }
}

class GetGoogleLoginUrlUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): LoginUrlResponse {
        return authRepository.getGoogleLoginUrl()
    }
}

class ExchangeKakaoCodeToJwtUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(code: String): LoginResponse {
        return authRepository.exchangeKakaoCodeToJwt(code)
    }
}

class ExchangeGoogleCodeToJwtUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(code: String): LoginResponse {
        return authRepository.exchangeGoogleCodeToJwt(code)
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

class ClearUserInfoUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        authRepository.clearUserInfo()
    }
}


class CompleteSignupUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(userInfo: UserInfo): SignupCompleteResponse {
        return authRepository.completeSignup(userInfo)
    }
}

class CheckNicknameDuplicateUseCase @Inject constructor(
    private val authRepository: AuthRepository
){
    suspend operator fun invoke(nickname: String): CheckNicknameDuplicateResponse {
        return authRepository.checkNicknameDuplicate(nickname)
    }
}

class ModifyUserProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository
){
    suspend operator fun invoke(userInfo: UserInfo): ModifyUserProfileResponse {
        return authRepository.modifyUserProfile(userInfo)
    }
}