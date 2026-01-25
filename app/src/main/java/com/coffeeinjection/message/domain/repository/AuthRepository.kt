package com.coffeeinjection.message.domain.repository

import com.coffeeinjection.message.data.local.UserInfo
import com.coffeeinjection.message.data.remote.base.ensureSuccessOrThrow
import com.coffeeinjection.message.data.remote.dto.CheckNicknameDuplicateResponse
import com.coffeeinjection.message.data.remote.dto.LoginResponse
import com.coffeeinjection.message.data.remote.dto.ModifyUserProfileResponse
import com.coffeeinjection.message.data.remote.dto.LoginUrlResponse
import com.coffeeinjection.message.data.remote.dto.SignupCompleteResponse
import kotlinx.coroutines.flow.Flow


/**
 * - ViewModel에서 사용할 인증 도메인 인터페이스
 */
interface AuthRepository {
    val userInfoFlow: Flow<UserInfo?>
    suspend fun getKakaoLoginUrl(): LoginUrlResponse
    suspend fun getGoogleLoginUrl(): LoginUrlResponse
    suspend fun exchangeKakaoCodeToJwt(code: String): LoginResponse
    suspend fun exchangeGoogleCodeToJwt(code: String): LoginResponse
    suspend fun completeSignup(userinfo: UserInfo): SignupCompleteResponse
    suspend fun checkNicknameDuplicate(nickname: String): CheckNicknameDuplicateResponse
    suspend fun saveAccessToken(token: String)
    suspend fun saveUserInfo(userinfo: UserInfo)
    suspend fun clearUserInfo()
    /** FCM 토큰 등록 */
    suspend fun registrationFCMToken(token: String)
    /** FCM 토큰 삭제 */
    suspend fun deleteFCMToken(token: String)
    /** 회원 탈퇴 */
    suspend fun withdraw()

}
