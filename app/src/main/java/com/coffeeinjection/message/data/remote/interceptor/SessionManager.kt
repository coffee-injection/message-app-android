package com.coffeeinjection.message.data.remote.interceptor

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

enum class SessionRefreshState {
    NONE,
    REFRESHED,
    EXPIRED,
    FAILED
}

@Singleton
class SessionManager @Inject constructor() {

    private val refreshState = AtomicReference(SessionRefreshState.NONE)

    private val _logoutEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 0)
    val logoutEvent: SharedFlow<Unit> = _logoutEvent

    // 중복 로그아웃 이벤트 방지
    private val isLogoutEmitted = AtomicBoolean(false)


    fun reset() {
        refreshState.set(SessionRefreshState.NONE)
    }

    fun markRefreshed() {
        refreshState.set(SessionRefreshState.REFRESHED)
    }

    fun markExpired() {
        refreshState.set(SessionRefreshState.EXPIRED)
    }

    fun markFailed() {
        refreshState.set(SessionRefreshState.FAILED)
    }

    fun getState(): SessionRefreshState {
        return refreshState.get()
    }

    fun emitLogoutIfNeeded() {
        if (isLogoutEmitted.compareAndSet(false, true)) {
            _logoutEvent.tryEmit(Unit)
        }
    }

    fun clearLogoutEventState() {
        isLogoutEmitted.set(false)
    }
}