package com.coffeeinjection.message.data.remote.api

import com.coffeeinjection.message.data.remote.base.ApiEnvelope
import com.coffeeinjection.message.data.remote.dto.AddBookmarkRequest
import com.coffeeinjection.message.data.remote.dto.DeleteBookmarkRequest
import com.coffeeinjection.message.data.remote.dto.Letter
import com.coffeeinjection.message.data.remote.dto.LetterSummary
import com.coffeeinjection.message.data.remote.dto.LoadBookmarkResponse
import com.coffeeinjection.message.data.remote.dto.ReportLetterRequest
import com.coffeeinjection.message.data.remote.dto.SendLetterRequest
import com.coffeeinjection.message.data.remote.dto.SendLetterResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MessageApi {

    /** 1) 수신된 편지 목록 조회 */
    @GET("letter/list")
    suspend fun fetchLetterList(): ApiEnvelope<List<LetterSummary>>

    /** 2) 편지 발송 */
    @POST("letter/send")
    suspend fun sendLetter(
        @Body body: SendLetterRequest
    ): ApiEnvelope<SendLetterResponse>

    /** 3) 수신된 편지 조회(상세) */
    @GET("letter/{letterId}")
    suspend fun fetchLetterDetail(
        @Path("letterId") letterId: Long
    ): ApiEnvelope<Letter>

    /** 4) 북마크 저장 */
    @POST("bookmark")
    suspend fun addBookmark(
        @Body body: AddBookmarkRequest
    ): ApiEnvelope<Any?>

    /** 5) 북마크 삭제*/
    @HTTP(method = "DELETE", path = "bookmark", hasBody = true)
    suspend fun deleteBookmark(
        @Body body: DeleteBookmarkRequest
    ): ApiEnvelope<Any?>

    /** 6) 북마크 리스트 불러오기 */
    @GET("bookmark/list")
    suspend fun loadBookmarksList(): ApiEnvelope<List<LoadBookmarkResponse>>

    /** 7) 수신 편지 신고 */
    @POST("report")
    suspend fun reportLetter(
        @Body body: ReportLetterRequest
    ): ApiEnvelope<Any?>

}
