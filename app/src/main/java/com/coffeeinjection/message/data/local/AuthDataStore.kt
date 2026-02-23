package com.coffeeinjection.message.data.local

import android.content.Context
import android.os.Parcelable
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.coffeeinjection.message.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.parcelize.Parcelize
import java.io.IOException

// region DataStore delegate
private val Context.authDataStore by preferencesDataStore(name = "auth_prefs")
// endregion

/**
 * 로그인 프로바이더
 */
enum class LoginProvider { GOOGLE, KAKAO }

/**
 * 앱에서 사용하는 최소 유저 정보
 */
@Parcelize
data class UserInfo(
    val nickName: String,
    val islandName: String,
    val profileImageIndex: Int = 1
) : Parcelable

/**
 * 인증/유저/FCM 관련 영속 상태를 관리하는 DataStore 래퍼
 */
class AuthDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        // Access / User
        private val KEY_ACCESS_TOKEN      = stringPreferencesKey("access_token")
        private val KEY_REFRESH_TOKEN     = stringPreferencesKey("refresh_token")
        private val KEY_USER_NICKNAME     = stringPreferencesKey("user_nickname")
        private val KEY_USER_ISLAND_NAME  = stringPreferencesKey("user_island_name")
        private val KEY_USER_IMG_IDX      = stringPreferencesKey("user_img_idx")
        private val KEY_AUTO_LOGIN        = booleanPreferencesKey("auto_login")

        // FCM
        private val KEY_FCM_TOKEN             = stringPreferencesKey("fcm_token_current")
        private val KEY_FCM_LAST_REGISTERED   = stringPreferencesKey("fcm_token_last_registered")

        // Login Provider
        private val KEY_LOGIN_PROVIDER    = stringPreferencesKey("login_provider")
    }

    // 공통: Preferences 안전 접근
    private val dataFlow = context.authDataStore.data
        .catch { e ->
            if (e is IOException) {
                Logger.e("[AuthDataStore] IOException while reading prefs, emit empty",
                    e.toString()
                )
                emit(emptyPreferences())
            } else {
                throw e
            }
        }

    // -------------------------
    // Access Token
    // -------------------------
    /** 액세스 토큰 Flow */
    val accessTokenFlow: Flow<String?> = dataFlow
        .map { it[KEY_ACCESS_TOKEN] }
        .distinctUntilChanged()

    val refreshTokenFlow: Flow<String?> = dataFlow
        .map { it[KEY_REFRESH_TOKEN] }
        .distinctUntilChanged()

    /** 액세스 토큰 저장/갱신 */
    suspend fun saveAccessToken(token: String) {
        Logger.d("[AuthDataStore] saveAccessToken --> $token")
        context.authDataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = token
        }
    }

    /** refresh 토큰 저장/갱신 */
    suspend fun saveRefreshToken(token: String) {
        Logger.d("[AuthDataStore] saveRefreshToken --> $token")
        context.authDataStore.edit { prefs ->
            prefs[KEY_REFRESH_TOKEN] = token
        }
    }

    /** 자동로그인(=회원가입 완료) Flow */
    val autoLoginFlow: Flow<Boolean> = dataFlow
        .map { prefs -> prefs[KEY_AUTO_LOGIN] ?: false }
        .distinctUntilChanged()

    /** 자동로그인(=회원가입 완료) 단발 조회 */
    suspend fun getAutoLogin(): Boolean = autoLoginFlow.firstOrNull() ?: false

    /**
     * 자동 로그인(=회원가입 완료 플래그)
     * - 신규회원이면 가입 완료 화면에서 true로 세팅
     * - 가입 도중 종료되면 false 유지되어 스플래시에서 홈 진입 방지
     */
    suspend fun saveAutoLogin(auto: Boolean) {
        Logger.d("[AuthDataStore] saveAutoLogin --> $auto")
        context.authDataStore.edit { prefs ->
            prefs[KEY_AUTO_LOGIN] = auto
        }
    }

    /** 액세스 토큰 단발 조회 */
    suspend fun getAccessToken(): String? = accessTokenFlow.firstOrNull()

    // -------------------------
    // User Info
    // -------------------------
    /** 유저 정보 Flow (모두 있을 때만 UserInfo 생성) */
    val userInfoFlow: Flow<UserInfo?> = dataFlow
        .map { prefs ->
            val nickName = prefs[KEY_USER_NICKNAME]
            val island   = prefs[KEY_USER_ISLAND_NAME]
            val imgIdx   = prefs[KEY_USER_IMG_IDX]?.toIntOrNull()

            if (nickName != null && island != null && imgIdx != null) {
                UserInfo(nickName = nickName, islandName = island, profileImageIndex = imgIdx)
            } else null
        }
        .distinctUntilChanged()

    /** 유저 정보 저장 */
    suspend fun saveUserInfo(userinfo: UserInfo) {
        Logger.i("[AuthDataStore] saveUserInfo : $userinfo")
        context.authDataStore.edit { prefs ->
            prefs[KEY_USER_NICKNAME]    = userinfo.nickName
            prefs[KEY_USER_ISLAND_NAME] = userinfo.islandName
            prefs[KEY_USER_IMG_IDX]     = userinfo.profileImageIndex.toString()
        }
    }

    // -------------------------
    // Login Provider
    // -------------------------
    /** 최근 로그인 프로바이더 Flow */
    val loginProviderFlow: Flow<LoginProvider?> = dataFlow
        .map { prefs ->
            when (prefs[KEY_LOGIN_PROVIDER]) {
                LoginProvider.GOOGLE.name -> LoginProvider.GOOGLE
                LoginProvider.KAKAO.name  -> LoginProvider.KAKAO
                else -> null
            }
        }
        .distinctUntilChanged()

    /** 로그인 프로바이더 저장 */
    suspend fun saveLoginProvider(provider: LoginProvider) {
        Logger.d("[AuthDataStore] saveLoginProvider --> ${provider.name}")
        context.authDataStore.edit { prefs ->
            prefs[KEY_LOGIN_PROVIDER] = provider.name
        }
    }

    /** 로그인 프로바이더 단발 조회 */
    suspend fun getLoginProvider(): LoginProvider? = loginProviderFlow.firstOrNull()

    // -------------------------
    // FCM
    // -------------------------
    /** 현재 기기의 FCM 토큰 Flow */
    private val fcmTokenFlow: Flow<String?> = dataFlow
        .map { it[KEY_FCM_TOKEN] }
        .distinctUntilChanged()

    /** 마지막으로 서버에 등록 완료한 FCM 토큰 Flow */
    private val lastRegisteredFcmTokenFlow: Flow<String?> = dataFlow
        .map { it[KEY_FCM_LAST_REGISTERED] }
        .distinctUntilChanged()

    /** FCM: 현재 토큰 저장 */
    suspend fun saveFcmToken(token: String) {
        Logger.d("[AuthDataStore] saveFcmToken --> $token")
        context.authDataStore.edit { prefs ->
            prefs[KEY_FCM_TOKEN] = token
        }
    }

    /** FCM: 현재 토큰 단발 조회 */
    suspend fun getFcmToken(): String? = fcmTokenFlow.firstOrNull()

    /** FCM: 마지막 서버 등록 성공 토큰 저장 */
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
     * → 화면/VM에서 collect해서 자동 등록 트리거로 사용
     */
    val needsFcmRegistrationFlow: Flow<Boolean> =
        combine(accessTokenFlow, fcmTokenFlow, lastRegisteredFcmTokenFlow) { access, current, last ->
            !access.isNullOrBlank() && !current.isNullOrBlank() && current != last
        }.distinctUntilChanged()

    // -------------------------
    // Clear / Logout
    // -------------------------
    /**
     * 회원탈퇴 등 전체 정리 정책
     * - access / user / provider 삭제
     * - 마지막 등록 FCM 토큰 삭제 → 다음 로그인 시 재등록 유도
     * - 현재 FCM 토큰은 기본 유지 (완전 초기화가 필요하면 주석 해제)
     */
    suspend fun clearAll() {
        Logger.i("[AuthDataStore] clearAll")
        context.authDataStore.edit { prefs ->
            prefs.remove(KEY_ACCESS_TOKEN)
            prefs.remove(KEY_REFRESH_TOKEN)
            prefs.remove(KEY_AUTO_LOGIN)
            prefs.remove(KEY_USER_NICKNAME)
            prefs.remove(KEY_USER_ISLAND_NAME)
            prefs.remove(KEY_USER_IMG_IDX)
            prefs.remove(KEY_LOGIN_PROVIDER)
            prefs.remove(KEY_FCM_LAST_REGISTERED)
        }
    }

    /**
     * 로그아웃
     */
    suspend fun clearForLogout(clearCurrentFcmToken: Boolean = true) {
        Logger.i("[AuthDataStore] clearForLogout(clearCurrentFcmToken=$clearCurrentFcmToken)")
        context.authDataStore.edit { prefs ->
            // 인증/세션
            prefs.remove(KEY_ACCESS_TOKEN)
            prefs.remove(KEY_REFRESH_TOKEN)

            // 로그인 플래그/유저 정보
            prefs.remove(KEY_AUTO_LOGIN)
            prefs.remove(KEY_USER_NICKNAME)
            prefs.remove(KEY_USER_ISLAND_NAME)
            prefs.remove(KEY_USER_IMG_IDX)

            // 로그인 프로바이더(선택: 보통 삭제)
            prefs.remove(KEY_LOGIN_PROVIDER)

            // FCM: 서버 등록 상태 초기화 → 다음 로그인 시 재등록 유도
            prefs.remove(KEY_FCM_LAST_REGISTERED)

            // FCM: 디바이스 토큰까지 deleteToken() 했으면 로컬도 삭제(권장)
            if (clearCurrentFcmToken) {
                prefs.remove(KEY_FCM_TOKEN)
            }
        }
    }

}
