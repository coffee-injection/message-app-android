package com.coffeeinjection.message.data.local

import android.content.Context
import android.os.Parcelable
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.coffeeinjection.message.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.parcelize.Parcelize

private val Context.authDataStore by preferencesDataStore(name = "auth_prefs")

class AuthDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_ACCESS_TOKEN   = stringPreferencesKey("access_token")
        private val KEY_USER_NICKNAME  = stringPreferencesKey("user_nickname")
        private val KEY_USER_ISLAND_NAME  = stringPreferencesKey("user_island_name")
        private val KEY_USER_IMG_IDX      = stringPreferencesKey("user_img_idx")

        // FCM 토큰 & 마지막 등록된 토큰
        private val KEY_FCM_TOKEN = stringPreferencesKey("fcm_token_current")
        private val KEY_FCM_LAST_REGISTERED = stringPreferencesKey("fcm_token_last_registered")

        // 자동 로그인
         private val KEY_LOGIN_PROVIDER = stringPreferencesKey("login_provider")
    }

    // --- 공통: Preferences 안전 접근
    private val dataFlow = context.authDataStore.data
        .catch { e -> if (e is java.io.IOException) emit(emptyPreferences()) else throw e }

    /** 액세스 토큰 Flow */
    val accessTokenFlow: Flow<String?> = dataFlow
        .map { it[KEY_ACCESS_TOKEN] }
        .distinctUntilChanged()

    /** 유저 정보 Flow (모두 있을 때만 UserInfo 반환) */
    val userInfoFlow: Flow<UserInfo?> = dataFlow
        .map { prefs ->
            val nickName = prefs[KEY_USER_NICKNAME]
            val islandName = prefs[KEY_USER_ISLAND_NAME]
            val profileIdx = prefs[KEY_USER_IMG_IDX]?.toIntOrNull()

            Logger.i("[choochoo] userInfoFlow observing")

            if (nickName != null && islandName != null && profileIdx != null) {
                UserInfo(nickName = nickName, islandName = islandName, profileImageIndex = profileIdx)
            } else null
        }
        .distinctUntilChanged()

    // --- 추가: FCM 관련 Flow/Getter/Setter

    /** 현재 기기의 FCM 토큰 Flow (getToken()/onNewToken 에서 저장) */
    private val fcmTokenFlow: Flow<String?> = dataFlow
        .map { it[KEY_FCM_TOKEN] }
        .distinctUntilChanged()

    /** 마지막으로 서버에 등록 완료한 FCM 토큰 Flow (중복 전송 방지용) */
    private val lastRegisteredFcmTokenFlow: Flow<String?> = dataFlow
        .map { it[KEY_FCM_LAST_REGISTERED] }
        .distinctUntilChanged()

    /** 액세스 토큰 저장/갱신 */
    suspend fun saveAccessToken(token: String) {
        Logger.d("[AuthDataStore] saveAccessToken --> token : $token")
        context.authDataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = token
        }
    }

    /** 단발 조회가 필요할 때 사용 (예: 로그인 직후) */
    suspend fun getAccessToken(): String? = accessTokenFlow.firstOrNull()

    /** 유저 정보 저장 */
    suspend fun saveUserInfo(userinfo: UserInfo) {
        Logger.i("[choochoo] saveUserInfo : $userinfo")
        context.authDataStore.edit { prefs ->
            prefs[KEY_USER_NICKNAME] = userinfo.nickName
            prefs[KEY_USER_ISLAND_NAME] = userinfo.islandName
            prefs[KEY_USER_IMG_IDX] = userinfo.profileImageIndex.toString()
        }
    }

    /** FCM: 현재 토큰 저장 (onNewToken / FirebaseMessaging.getInstance().token 성공 시 호출) */
    suspend fun saveFcmToken(token: String) {
        Logger.d("[AuthDataStore] saveFcmToken --> $token")
        context.authDataStore.edit { prefs ->
            prefs[KEY_FCM_TOKEN] = token
        }
    }

    /** FCM: 현재 토큰 단발 조회 */
    suspend fun getFcmToken(): String? = fcmTokenFlow.firstOrNull()

    /** FCM: 마지막 서버 등록 성공한 토큰 저장 */
    suspend fun saveLastRegisteredFcmToken(token: String) {
        Logger.d("[AuthDataStore] saveLastRegisteredFcmToken --> $token")
        context.authDataStore.edit { prefs ->
            prefs[KEY_FCM_LAST_REGISTERED] = token
        }
    }

    /** FCM: 마지막 서버 등록 토큰 단발 조회 */
    suspend fun getLastRegisteredFcmToken(): String? = lastRegisteredFcmTokenFlow.firstOrNull()

    /**
     * 로그인 상태 && FCM 토큰 존재 && 마지막 등록 토큰과 다르면 true
     * -> ViewModel에서 collect 해서 자동 등록 트리거로 사용 가능
     */
    val needsFcmRegistrationFlow: Flow<Boolean> =
        combine(accessTokenFlow, fcmTokenFlow, lastRegisteredFcmTokenFlow) { access, current, last ->
            !access.isNullOrBlank() && !current.isNullOrBlank() && current != last
        }.distinctUntilChanged()

    /** 로그아웃 등 전체 정리
     *
     * 정책:
     * - access/user 정보 삭제
     * - **마지막 등록 토큰은 삭제** → 다음 로그인 시 재등록 유도
     * - **현재 FCM 토큰은 유지**(디바이스 고유 토큰은 계속 쓸 수 있게)
     *   (필요 시 아래 주석 해제로 완전 초기화 가능)
     */
    suspend fun clearAll() {
        context.authDataStore.edit { prefs ->
            prefs.remove(KEY_ACCESS_TOKEN)
            prefs.remove(KEY_USER_NICKNAME)
            prefs.remove(KEY_USER_ISLAND_NAME)
            prefs.remove(KEY_USER_IMG_IDX)
            prefs.remove(KEY_FCM_LAST_REGISTERED)
            // prefs.remove(KEY_FCM_TOKEN) // 토큰까지 지우고 싶다면 주석 해제
        }
    }
}

@Parcelize
data class UserInfo(
    val nickName: String,
    val islandName: String,
    val profileImageIndex: Int = 1
) : Parcelable
