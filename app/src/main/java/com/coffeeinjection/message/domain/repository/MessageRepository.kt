package com.coffeeinjection.message.domain.repository

import com.coffeeinjection.message.data.remote.dto.Letter
import com.coffeeinjection.message.data.remote.dto.LetterSummary
import com.coffeeinjection.message.data.remote.dto.LoadBookmarkResponse
import com.coffeeinjection.message.data.remote.dto.SendLetterResponse

/**
 * - ViewModel에서 사용할 메시지 도메인 인터페이스
 */
interface MessageRepository {

    /** 수신한 편지 목록 조회 */
    suspend fun fetchLetterList(): List<LetterSummary>// 유즈케이스로 빼기

    /** 편지 상세 조회 */
    suspend fun fetchLetterDetail(letterId: Long): Letter

    /** 편지 발송 */
    suspend fun sendLetter(
        content: String
    ): SendLetterResponse

    /** 편지 북마크 */
    suspend fun addBookmark(letterId: Long)

    /** 북마크 삭제*/
    suspend fun deleteBookmark(letterId: Long)

    /** 북마크 리스트 조회*/
    suspend fun loadBookmarksList(): List<LoadBookmarkResponse>

    /** 편지 신고 */
    suspend fun reportLetter(
        letterId: Long,
        reason: String?
    )
}
