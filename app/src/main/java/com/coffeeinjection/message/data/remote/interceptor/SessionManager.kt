package com.coffeeinjection.message.data.remote.interceptor

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
}