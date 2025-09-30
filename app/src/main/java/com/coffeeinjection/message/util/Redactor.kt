package com.coffeeinjection.message.util

/**
 * 로그에 섞일 수 있는 민감정보(토큰/이메일/전화번호 등)를 마스킹.
 * - 텍스트/맵 형태 모두 지원
 * - 필요시 패턴을 추가하세요.
 *
 *  * by - agileCatch 2025.09.30.
 */
object Redactor {
    private val keys = listOf("token", "authorization", "password", "email", "phone")

    /** 키 기반 마스킹 (구조화 로그용) */
    fun redact(fields: Map<String, Any?>) =
        fields.mapValues { (k, v) -> if (keys.any { k.contains(it, true) }) "****" else v }

    /** 텍스트 기반 마스킹 (프리 로그 문자열) */
    fun redactText(s: String): String {
        var x = s
        x = x.replace(Regex("""Bearer\s+[A-Za-z0-9\-\._]+"""), "Bearer ****")
        x = x.replace(Regex("""\b[0-9A-Za-z._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}\b"""), "****@****")
        x = x.replace(Regex("""\b\d{3}-\d{3,4}-\d{4}\b"""), "***-****-****")
        return x
    }
}
