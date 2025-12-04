package com.coffeeinjection.message.data.repository

import com.coffeeinjection.message.data.remote.dto.ApiEnvelope
import com.coffeeinjection.message.data.remote.dto.BookmarkLetterRequest
import com.coffeeinjection.message.data.remote.dto.LetterDetailDto
import com.coffeeinjection.message.data.remote.dto.LetterSummaryDto
import com.coffeeinjection.message.data.remote.dto.ReportLetterRequest
import com.coffeeinjection.message.data.remote.dto.SendLetterRequest
import com.coffeeinjection.message.data.remote.dto.SendLetterResponse
import com.coffeeinjection.message.data.remote.api.MessageApi
import com.coffeeinjection.message.data.remote.ensureSuccessOrThrow
import com.coffeeinjection.message.data.remote.requireDataOrThrow
import com.coffeeinjection.message.domain.repository.MessageRepository
import javax.inject.Inject

/**
 * - 메시지(편지) 관련 API를 캡슐화한 구현체
 * - AuthRepositoryImpl 과 동일하게 ApiEnvelope를 언래핑해서 실제 데이터만 반환
 */
class MessageRepositoryImpl @Inject constructor(
    private val api: MessageApi
) : MessageRepository {

    override suspend fun fetchLetterList(): List<LetterSummaryDto> {
        val env = api.fetchLetterList()
        return env.requireDataOrThrow("letter/list")
    }

    override suspend fun fetchLetterDetail(letterId: Long): LetterDetailDto {
        val env = api.fetchLetterDetail(letterId)
        return env.requireDataOrThrow("letter/detail")
    }

    override suspend fun sendLetter(
        receiverNickname: String,
        content: String
    ): SendLetterResponse {
        val env = api.sendLetter(
            SendLetterRequest(
                receiverNickname = receiverNickname,
                content = content
            )
        )
        return env.requireDataOrThrow("letter/send")
    }

    override suspend fun bookmarkLetter(letterId: Long) {
        val env = api.bookmarkLetter(BookmarkLetterRequest(letterId))
        // data: Unit 이라 항상 null일 수도 있으니 성공 여부만 체크
        env.ensureSuccessOrThrow("letter/book-mark")
    }

    override suspend fun reportLetter(
        letterId: Long,
        reason: String?
    ) {
        val env = api.reportLetter(ReportLetterRequest(letterId, reason))
        env.ensureSuccessOrThrow("report")
    }
}

