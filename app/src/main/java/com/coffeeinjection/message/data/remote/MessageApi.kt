package com.coffeeinjection.message.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface MessageApi {
    /** 1) 편지 목록 조회 */
    @GET("letter/list")
    suspend fun fetchLetterList()

    /** 2) 편지 발송 */
    @POST("letter/send")
    suspend fun sendLetter()

    /** 3) 수신된 편지 조회(상세) */
    @GET("letter/detail")
    suspend fun fetchLetterDetail()

    /** 4) 수신 편지 북마크 저장 */
    @POST("letter/book-mark")
    suspend fun bookmarkLetter()

    /** 5) 수신 편지 북마크에 저장*/
    @POST("report")
    suspend fun reportLetter()
}