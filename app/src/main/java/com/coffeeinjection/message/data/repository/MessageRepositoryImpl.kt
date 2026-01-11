package com.coffeeinjection.message.data.repository

import com.coffeeinjection.message.data.remote.api.MessageApi
import com.coffeeinjection.message.data.remote.base.ensureSuccessOrThrow
import com.coffeeinjection.message.data.remote.base.requireDataOrThrow
import com.coffeeinjection.message.data.remote.dto.AddBookmarkRequest
import com.coffeeinjection.message.data.remote.dto.DeleteBookmarkRequest
import com.coffeeinjection.message.data.remote.dto.Letter
import com.coffeeinjection.message.data.remote.dto.LetterSummary
import com.coffeeinjection.message.data.remote.dto.LoadBookmarkResponse
import com.coffeeinjection.message.data.remote.dto.ReportLetterRequest
import com.coffeeinjection.message.data.remote.dto.SendLetterRequest
import com.coffeeinjection.message.data.remote.dto.SendLetterResponse
import com.coffeeinjection.message.domain.repository.MessageRepository
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val api: MessageApi
) : MessageRepository {

    override suspend fun fetchLetterList(): List<LetterSummary> =
        api.fetchLetterList().requireDataOrThrow("letter/list")

    override suspend fun fetchLetterDetail(letterId: Long): Letter =
        api.fetchLetterDetail(letterId).requireDataOrThrow("letter/{letterId}")

    override suspend fun sendLetter(
        content: String
    ): SendLetterResponse =
        api.sendLetter(SendLetterRequest(content = content))
            .requireDataOrThrow("letter/send")

    override suspend fun addBookmark(letterId: Long) {
        api.addBookmark(AddBookmarkRequest(letterId))
            .ensureSuccessOrThrow("bookmark")
    }

    override suspend fun deleteBookmark(letterId: Long) {
        api.deleteBookmark(DeleteBookmarkRequest(letterId))
            .ensureSuccessOrThrow("bookmark")
    }

    override suspend fun loadBookmarksList(): List<LoadBookmarkResponse> =
        api.loadBookmarksList().requireDataOrThrow("bookmark/list")

    override suspend fun reportLetter(letterId: Long, reason: String?) {
        api.reportLetter(ReportLetterRequest(letterId, reason))
            .ensureSuccessOrThrow("report")
    }
}
