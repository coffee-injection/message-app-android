package com.coffeeinjection.message.util

import android.util.Log
import org.json.JSONObject
import timber.log.Timber

/**
 * 프로젝트 전역 로깅 파사드.
 * - 기본 로그(d/i/w/e) + 이벤트 로그(event)
 * - 릴리스에서도 마스킹 일관성 유지
 *
 * * by - agileCatch 2025.09.30.
 */
object Logger {

    fun d(msg: String, vararg args: Any?) = Timber.d(msg, *args)
    fun i(msg: String, vararg args: Any?) = Timber.i(msg, *args)
    fun w(msg: String, vararg args: Any?) = Timber.w(msg, *args)

    fun e(t: Throwable? = null, msg: String, vararg args: Any?) {
        val formatted = if (args.isNotEmpty()) msg.format(*args) else msg
        Timber.e(t, Redactor.redactText(formatted))
    }

    /** 구조화 이벤트 로그(검색/집계용) */
    fun event(name: String, fields: Map<String, Any?> = emptyMap(), level: Int = Log.INFO) {
        val safe = Redactor.redact(fields)
        val json = JSONObject(mapOf(
            "event" to name,
            "fields" to safe,
            "ts" to System.currentTimeMillis()
        )).toString()

        when (level) {
            Log.VERBOSE -> Timber.v(json)
            Log.DEBUG   -> Timber.d(json)
            Log.INFO    -> Timber.i(json)
            Log.WARN    -> Timber.w(json)
            else        -> Timber.e(json)
        }
    }
}
