package com.coffeeinjection.message.data.remote.base

/**
 * 공통 ApiEnvelope 언래핑 확장 함수
 * - status == 200, success == true, data != null 인 경우에만 data 반환
 * - 아니면 IllegalStateException 던짐
 */
fun <T> ApiEnvelope<T>.requireDataOrThrow(
    apiName: String
): T {
    if (status == 200 && success == true && data != null) {
        return data
    }
    throw IllegalStateException("$apiName 실패: status=$status, success=$success")
}

/**
 * data 가 중요하지 않은 API (북마크, 신고 등)에서 사용
 * - 성공 여부만 체크
 */
fun <T> ApiEnvelope<T>.ensureSuccessOrThrow(
    apiName: String
) {
    if (status == 200 && success == true) return

    throw IllegalStateException("$apiName 실패: status=$status, success=$success")
}
