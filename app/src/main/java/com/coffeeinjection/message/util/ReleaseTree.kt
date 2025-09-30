package com.coffeeinjection.message.util

import android.util.Log
import timber.log.Timber

/**
 * 릴리스 빌드에서만 심는 Timber 트리.
 * - WARN 이상만 시스템 로그에 출력
 * - ERROR 이상은 (원한다면) 크래시 리포팅으로 전송하도록 훅을 마련
 * - 메시지는 Redactor로 기본 마스킹
 *
 * by - agileCatch 2025.09.30.
 */
class ReleaseTree : Timber.Tree() {
    override fun isLoggable(tag: String?, priority: Int) =
        priority >= Log.WARN         // 릴리스에서는 WARN, ERROR 만 허용

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (!isLoggable(tag, priority)) return

        val safeMsg = Redactor.redactText(message)
        val safeTag = tag ?: "App"

        if (t != null) {
            Log.println(priority, safeTag, "$safeMsg\n${Log.getStackTraceString(t)}")
            // 예: FirebaseCrashlytics.getInstance().recordException(t)
        } else {
            Log.println(priority, safeTag, safeMsg)
        }
    }
}
