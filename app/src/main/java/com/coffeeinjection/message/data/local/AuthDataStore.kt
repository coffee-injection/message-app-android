package com.coffeeinjection.message.data.local

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.coffeeinjection.message.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import androidx.core.net.toUri

/**
 * - 액세스 토큰을 DataStore에 저장/조회
 * - 신규 회원의 "임시 토큰"도 동일 키에 저장하고, 가입 완료 후 "최종 토큰"으로 갱신
 * - Hilt 주입을 위해 @Inject 생성자를 제공
 */

private val Context.authDataStore by preferencesDataStore(name = "auth_prefs") // 확장 프로퍼티 : Context.dataStore를 참조하면 auth_pref라는 이름의 PreferencesDataStore를 가져옴

class AuthDataStore @Inject constructor(
    @ApplicationContext private val context: Context // application 범위의 Context를 DI로 부터 주입 받아 사용
) {
    companion object {
        private val KEY_ACCESS_TOKEN   = stringPreferencesKey("access_token")
        private val KEY_USER_NICKNAME  = stringPreferencesKey("user_nickname")
        private val KEY_USER_IMG      = stringPreferencesKey("user_img")
    }

    /** 현재 저장된 액세스 토큰 */
    val accessTokenFlow: Flow<String?> = context.authDataStore.data.map {
        it[KEY_ACCESS_TOKEN]
    }

    /** 유저 정보 Flow (모두 있을 때만 UserInfo 반환, 아니면 null) */
    val userInfoFlow: Flow<UserInfo?> = context.authDataStore.data.map { prefs ->
        val img = prefs[KEY_USER_IMG]
        val nickname = prefs[KEY_USER_NICKNAME]

        val imgUri = img?.toUri()

        if (img == null || nickname == null) null
        else UserInfo(nickname = nickname, userImg = imgUri)
    }

    /** 액세스 토큰 저장/갱신 */
    suspend fun saveAccessToken(token: String) {
        Logger.d("[AuthDataStore] saveAccessToken init --> token : $token")
        context.authDataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = token
        }
    }

    suspend fun saveUserInfo(userinfo: UserInfo) {
        Logger.d("[AuthDataStore] saveUserInfo init --> userinfo : $userinfo")
        context.authDataStore.edit { prefs ->
            prefs[KEY_USER_NICKNAME] = userinfo.nickname
            prefs[KEY_USER_IMG] = userinfo.userImg.toString()
        }
    }

    /** 액세스 토큰 제거 (로그아웃 등)
     * todo -> ClearAll 시 토큰을 삭제하기 때문에 재 로그인 시 새로운 토큰이 갱신되면 서버 상의 정보랄 달라질 우려 있음 -> 확인 필요
     * */
    suspend fun clearAll() {
        context.authDataStore.edit { prefs ->
            prefs.remove(KEY_ACCESS_TOKEN)
            prefs.remove(KEY_USER_NICKNAME)
            prefs.remove(KEY_USER_IMG)
        }
    }
}

data class UserInfo(
    val nickname: String,
    val userImg: Uri?
)