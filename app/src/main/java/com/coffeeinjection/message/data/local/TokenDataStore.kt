package com.coffeeinjection.message.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * - 액세스 토큰을 DataStore에 저장/조회
 * - 신규 회원의 "임시 토큰"도 동일 키에 저장하고, 가입 완료 후 "최종 토큰"으로 갱신
 * - Hilt 주입을 위해 @Inject 생성자를 제공
 */

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class TokenDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
    }

    /** 현재 저장된 액세스 토큰 */
    val accessTokenFlow: Flow<String?> = context.dataStore.data.map { it[KEY_ACCESS_TOKEN] }

    /** 액세스 토큰 저장/갱신 */
    suspend fun saveAccessToken(token: String) {
        context.dataStore.edit { prefs -> prefs[KEY_ACCESS_TOKEN] = token }
    }

    /** 액세스 토큰 제거 (로그아웃 등) */
    suspend fun clear() {
        context.dataStore.edit { it.remove(KEY_ACCESS_TOKEN) }
    }
}