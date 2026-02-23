package com.coffeeinjection.message.presentation.mypage.viewmodel

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeeinjection.message.data.local.AuthDataStore
import com.coffeeinjection.message.domain.usecase.DeleteFCMTokenUseCase
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * 표시/제어 분리:
 * - desiredEnabled: 사용자가 '그렇게 하고 싶다'는 의사(토글)
 * - appEnabled / channelEnabled / permissionGranted: 실제 시스템 상태
 * - effectiveEnabled: 실제로 알림이 울릴 수 있는지(시스템 상태만으로 산출)
 */
data class PushUiState(
    val desiredEnabled: Boolean = true,
    val appEnabled: Boolean = false,
    val channelEnabled: Boolean = false,
    val permissionGranted: Boolean = true
) {
    /** 시스템 기준의 실제 가능 여부(표시용) */
    val effectiveEnabled: Boolean
        get() = appEnabled && channelEnabled && permissionGranted
}

@HiltViewModel
class MyPageViewModel @Inject constructor(
    @ApplicationContext private val mContext: Context,
    private val dataStore: DataStore<Preferences>,
    private val authDataStore: AuthDataStore,
    private val deleteFCMTokenUseCase: DeleteFCMTokenUseCase,
) : ViewModel() {

    companion object {
        private const val CHANNEL_ID = "default_push"
        private val KEY_DESIRED = booleanPreferencesKey("push_desired_enabled")
    }

    private val _ui = MutableStateFlow(PushUiState())
    val ui: StateFlow<PushUiState> = _ui

    // 로그아웃 완료 이벤트(프래그먼트에서 수집하여 로그인 화면으로 이동)
    private val _logoutEvent = Channel<Unit>(Channel.BUFFERED)
    val logoutEvent = _logoutEvent.receiveAsFlow()

    /** 초기 로드: 사용자 선호 + 시스템 상태 동기화 */
    fun load() = viewModelScope.launch {
        ensureChannel()
        val desired = dataStore.data.map { it[KEY_DESIRED] ?: true }.first()
        _ui.value = PushUiState(
            desiredEnabled = desired,
            appEnabled = NotificationManagerCompat.from(mContext).areNotificationsEnabled(),
            channelEnabled = isChannelEnabled(),
            permissionGranted = isPostNotificationsGranted()
        )
    }

    /** 사용자의 토글 의사만 저장(실제 on/off는 시스템 설정/권한에서 결정) */
    fun setDesiredEnabled(checked: Boolean) = viewModelScope.launch {
        dataStore.edit { it[KEY_DESIRED] = checked }
        _ui.value = _ui.value.copy(desiredEnabled = checked)
    }

    /** 시스템 설정 변경(앱/채널/권한) 재조회 → UI 반영 */
    fun refreshState() = viewModelScope.launch {
        _ui.value = _ui.value.copy(
            appEnabled = NotificationManagerCompat.from(mContext).areNotificationsEnabled(),
            channelEnabled = isChannelEnabled(),
            permissionGranted = isPostNotificationsGranted()
        )
    }

    /** 로그아웃: 서버 토큰 해제 → 디바이스 토큰 삭제 → 로컬 정리 → 이벤트 발행 */
    fun logout() = viewModelScope.launch {
        runCatching {
            // 1) 서버에 등록된 FCM 토큰 해제
            val currentFcm = authDataStore.getFcmToken()
            if (!currentFcm.isNullOrBlank()) {
                runCatching { deleteFCMTokenUseCase(currentFcm) }
            }

            // 2) 디바이스 FCM 토큰 삭제(다음 로그인 시 새 토큰 발급 유도)
            runCatching { FirebaseMessaging.getInstance().deleteToken().await() }

            authDataStore.clearForLogout()

        }.also {
            // 4) 화면 전환(프래그먼트에서 수집하여 SignIn으로 네비게이션)
            _logoutEvent.trySend(Unit)
        }
    }

    /** (API 26+) 기본 채널 미존재 시 생성 */
    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < 26) return
        val nm = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "일반 알림",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }
    }

    /** 기본 채널이 차단되지 않았는지 */
    private fun isChannelEnabled(): Boolean {
        if (Build.VERSION.SDK_INT < 26) return true
        val nm = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val ch = nm.getNotificationChannel(CHANNEL_ID) ?: return true
        return ch.importance != NotificationManager.IMPORTANCE_NONE
    }

    /** Android 13+(API 33) 권한 여부 */
    private fun isPostNotificationsGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= 33) {
            ContextCompat.checkSelfPermission(
                mContext, android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }
}
