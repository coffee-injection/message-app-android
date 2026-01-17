package com.coffeeinjection.message.domain.repository

import com.coffeeinjection.message.data.local.UserInfo
import com.coffeeinjection.message.data.remote.dto.CheckNicknameDuplicateResponse
import com.coffeeinjection.message.data.remote.dto.KakaoLoginUrlResponse
import com.coffeeinjection.message.data.remote.dto.LoginResponse
import com.coffeeinjection.message.data.remote.dto.ModifyUserProfileResponse
import com.coffeeinjection.message.data.remote.dto.SignupCompleteResponse
import kotlinx.coroutines.flow.Flow


/**
 * - ViewModel에서 사용할 인증 도메인 인터페이스
 */
interface AuthRepository {
    val userInfoFlow: Flow<UserInfo?>
    suspend fun getKakaoLoginUrl(): KakaoLoginUrlResponse
    suspend fun exchangeCodeToJwt(code: String): LoginResponse
    suspend fun completeSignup(userinfo: UserInfo): SignupCompleteResponse
    suspend fun checkNicknameDuplicate(nickname: String): CheckNicknameDuplicateResponse
    suspend fun modifyUserProfile(userInfo: UserInfo) : ModifyUserProfileResponse
    suspend fun saveAccessToken(token: String)
    suspend fun saveUserInfo(userinfo: UserInfo)
    suspend fun clearUserInfo()
}
