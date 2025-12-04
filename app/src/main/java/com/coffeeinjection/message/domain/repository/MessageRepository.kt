package com.coffeeinjection.message.domain.repository

import com.coffeeinjection.message.data.remote.dto.LetterDetailDto
import com.coffeeinjection.message.data.remote.dto.LetterSummaryDto
import com.coffeeinjection.message.data.remote.dto.SendLetterResponse

/**
 * - ViewModel에서 사용할 메시지 도메인 인터페이스
 */
interface MessageRepository {

    /** 편지 목록 조회 */
    suspend fun fetchLetterList(): List<LetterSummaryDto>

    /** 편지 상세 조회 */
    suspend fun fetchLetterDetail(letterId: Long): LetterDetailDto

    /** 편지 발송 */
    suspend fun sendLetter(
        receiverNickname: String,
        content: String
    ): SendLetterResponse

    /** 편지 북마크 */
    suspend fun bookmarkLetter(letterId: Long)

    /** 편지 신고 */
    suspend fun reportLetter(
        letterId: Long,
        reason: String?
    )
}
