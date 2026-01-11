package com.coffeeinjection.message.presentation.activity

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeeinjection.message.data.remote.dto.Letter
import com.coffeeinjection.message.domain.repository.MessageRepository
import com.coffeeinjection.message.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val repo: MessageRepository
) : ViewModel() {

    private val _profileUri = MutableStateFlow<Uri?>(null)
    val profileUri : StateFlow<Uri?> = _profileUri

    fun setPhoto(uri: Uri?) {
        _profileUri.value = uri
    }

    // -------------------------
    // 편지 상세(읽기) 상태
    // -------------------------
    private val _letterDetail = MutableStateFlow<Letter?>(null)
    val letterDetail: StateFlow<Letter?> = _letterDetail

    private val _isLoadingLetterDetail = MutableStateFlow(false)
    val isLoadingLetterDetail: StateFlow<Boolean> = _isLoadingLetterDetail

    private val _letterDetailError = MutableStateFlow<String?>(null)
    val letterDetailError: StateFlow<String?> = _letterDetailError

    /**
     * 메세지 보내기
     */
    fun sendLetter(content: String) {
        viewModelScope.launch {
            runCatching {
                repo.sendLetter(content)
            }.onSuccess { res ->
                Logger.d("[letter/send] success, id=${res.letterId}")
            }.onFailure { e ->
                Logger.error("[letter/send] fail msg=${e.message} cause=${e.cause}")
            }
        }
    }

    /**
     * 메세지 읽기(상세 조회)
     */
    fun readLetter(letterId: Long) {
        viewModelScope.launch {
            _isLoadingLetterDetail.value = true
            _letterDetailError.value = null

            runCatching { repo.fetchLetterDetail(letterId) }
                .onSuccess { letter ->
                    _letterDetail.value = letter
                    Logger.d("[letter/{id}] success, id=${letter.letterId}")
                }
                .onFailure { e ->
                    _letterDetail.value = null
                    _letterDetailError.value = e.message ?: "편지 조회에 실패했습니다."
                    Logger.error("[letter/{id}] fail msg=${e.message} cause=${e.cause}")
                }

            _isLoadingLetterDetail.value = false
        }
    }

    /**
     * 필요하면 다이얼로그 닫을 때 초기화
     */
    fun clearLetterDetail() {
        _letterDetail.value = null
        _letterDetailError.value = null
        _isLoadingLetterDetail.value = false
    }

    /**
     * 북마크 저장
     */
    fun addBookmark(letterId: Long) {
        viewModelScope.launch {
            runCatching {
                repo.addBookmark(letterId)
            }.onSuccess {
                Logger.d("[add bookmark] success, letterId=$letterId")
            }.onFailure { e ->
                Logger.error("[add bookmark] fail letterId=$letterId msg=${e.message} cause=${e.cause}")
            }
        }
    }

    /**
     * 북마크 삭제
     */
    fun unBookmark(letterId: Long) {
        viewModelScope.launch {
            runCatching {
                repo.deleteBookmark(letterId)
            }.onSuccess {
                Logger.d("[deleteBookmark] success, letterId=$letterId")
            }.onFailure { e ->
                Logger.error("[deleteBookmark] fail letterId=$letterId msg=${e.message} cause=${e.cause}")
            }
        }
    }

    /**
     * 편지 신고
     * @param reason 신고 사유(선택). 없으면 null
     */
    fun reportLetter(letterId: Long, reason: String? = null) {
        viewModelScope.launch {
            runCatching {
                repo.reportLetter(letterId = letterId, reason = reason)
            }.onSuccess {
                Logger.d("[report] success, letterId=$letterId, reason=$reason")
            }.onFailure { e ->
                Logger.error("[report] fail letterId=$letterId msg=${e.message} cause=${e.cause}")
            }
        }
    }


}
