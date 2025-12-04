package com.coffeeinjection.message.data.remote.api

import com.coffeeinjection.message.data.remote.dto.ApiEnvelope
import com.coffeeinjection.message.data.remote.dto.BookmarkLetterRequest
import com.coffeeinjection.message.data.remote.dto.LetterDetailDto
import com.coffeeinjection.message.data.remote.dto.LetterSummaryDto
import com.coffeeinjection.message.data.remote.dto.ReportLetterRequest
import com.coffeeinjection.message.data.remote.dto.SendLetterRequest
import com.coffeeinjection.message.data.remote.dto.SendLetterResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MessageApi {

    /** 1) 편지 목록 조회 */
    @GET("letter/list")
    suspend fun fetchLetterList(): ApiEnvelope<List<LetterSummaryDto>>

    /** 2) 편지 발송 */
    @POST("letter/send")
    suspend fun sendLetter(
        @Body body: SendLetterRequest
    ): ApiEnvelope<SendLetterResponse>

    /** 3) 수신된 편지 조회(상세) */
    @GET("letter/detail")
    suspend fun fetchLetterDetail(
        @Query("letterId") letterId: Long
    ): ApiEnvelope<LetterDetailDto>

    /** 4) 수신 편지 북마크 저장 */
    @POST("letter/book-mark")
    suspend fun bookmarkLetter(
        @Body body: BookmarkLetterRequest
    ): ApiEnvelope<Unit>   // data 없으면 Unit, Any? 등 사용

    /** 5) 수신 편지 신고 */
    @POST("report")
    suspend fun reportLetter(
        @Body body: ReportLetterRequest
    ): ApiEnvelope<Unit>
}
