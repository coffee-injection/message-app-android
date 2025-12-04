package com.coffeeinjection.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeeinjection.message.domain.repository.MessageRepository
import com.coffeeinjection.message.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: MessageRepository
): ViewModel() {
    /**
     * MessageRepository 에 있는 모든 API를 한 번씩 호출해보는 테스트용 함수
     * - 실제 서버에 영향 줄 수 있으니 꼭 테스트 환경에서만 사용하세요.
     */
    fun testAllMessageApis() {
        viewModelScope.launch {
            try {
                // 1) 편지 목록 조회
                val list = repo.fetchLetterList()
                Logger.d("HomeViewModel", "[letter/list] size = ${list.size}")

                // 목록 있으면 첫 번째 편지 기준으로 detail / bookmark / report 테스트
//                val firstIdFromList = list.firstOrNull()?.letterId

//                // 2) 편지 상세 조회
//                if (firstIdFromList != null) {
//                    val detail = repo.fetchLetterDetail(firstIdFromList)
//                    Logger.d(
//                        "HomeViewModel",
//                        "[letter/detail] id=${detail.letterId}, content=${detail.content}"
//                    )
//                } else {
//                    Logger.w("HomeViewModel", "[letter/detail] 조회할 편지가 없습니다.")
//                }

//                // 3) 편지 발송 (더미 데이터)
//                val sendResult = repo.sendLetter(
//                    receiverNickname = "테스트닉네임",
//                    content = "테스트용 메세지입니다."
//                )
//                Logger.d(
//                    "HomeViewModel",
//                    "[letter/send] 발송 완료, newId=${sendResult.letterId}"
//                )
//
//                // 북마크 / 신고에 사용할 id 우선순위:
//                // 1) 방금 보낸 편지 id
//                // 2) 목록의 첫 번째 편지 id
//                val targetId = sendResult.letterId ?: firstIdFromList
//
//                // 4) 북마크 저장
//                if (targetId != null) {
//                    repo.bookmarkLetter(targetId)
//                    Logger.d(
//                        "HomeViewModel",
//                        "[letter/book-mark] success, id=$targetId"
//                    )
//                } else {
//                    Logger.w(
//                        "HomeViewModel",
//                        "[letter/book-mark] 북마크할 letterId 가 없습니다."
//                    )
//                }
//
//                // 5) 신고
//                if (targetId != null) {
//                    repo.reportLetter(
//                        letterId = targetId,
//                        reason = "테스트 신고입니다."
//                    )
//                    Logger.d(
//                        "HomeViewModel",
//                        "[report] success, id=$targetId"
//                    )
//                } else {
//                    Logger.w(
//                        "HomeViewModel",
//                        "[report] 신고할 letterId 가 없습니다."
//                    )
//                }

            } catch (e: Exception) {
                Logger.e( "testAllMessageApis error", e.toString())
            }
        }
    }

}