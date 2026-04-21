package com.coffeeinjection.message.presentation.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeeinjection.message.data.local.AuthDataStore
import com.coffeeinjection.message.data.local.UserInfo
import com.coffeeinjection.message.data.remote.dto.Letter
import com.coffeeinjection.message.data.remote.interceptor.SessionManager
import com.coffeeinjection.message.data.remote.interceptor.SessionRefreshState
import com.coffeeinjection.message.domain.repository.MessageRepository
import com.coffeeinjection.message.domain.usecase.ClearUserInfoUseCase
import com.coffeeinjection.message.domain.usecase.ObserveUserInfoUseCase
import com.coffeeinjection.message.domain.usecase.SaveUserInfoUseCase
import com.coffeeinjection.message.util.Logger
import com.coffeeinjection.message.util.TokenStateEnum
import com.coffeeinjection.message.util.UserInfoUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val repo: MessageRepository,
    private val saveUserInfo: SaveUserInfoUseCase,
    private val clearUserInfo: ClearUserInfoUseCase,
    observeUserInfoUseCase: ObserveUserInfoUseCase,
    private val sessionManager: SessionManager,
    private val authDataStore: AuthDataStore
) : ViewModel() {

    private val _tokenState = MutableLiveData(TokenStateEnum.NONE)
    val tokenState: LiveData<TokenStateEnum> get() = _tokenState

    fun clearLogoutEventState(){
        sessionManager.clearLogoutEventState()
    }
    fun checkTokenValidation() {
        viewModelScope.launch {
            sessionManager.reset()

            runCatching { repo.fetchLetterList() }
                .onSuccess { res ->
                    Logger.d("[SharedViewModel] checkTokenValidation success : $res")

                    _tokenState.value = when (sessionManager.getState()) {
                        SessionRefreshState.REFRESHED -> TokenStateEnum.REFRESHED
                        else -> TokenStateEnum.VALID
                    }
                }
                .onFailure { e ->
                    Logger.d("[SharedViewModel] checkTokenValidation fail : $e")

                    _tokenState.value = when (sessionManager.getState()) {
                        SessionRefreshState.EXPIRED -> TokenStateEnum.EXPIRED
                        else -> TokenStateEnum.ERROR
                    }
                }
        }
    }

    fun initForTest() {
        viewModelScope.launch {
            Logger.i("[choochoo] initForTest")
            saveUserInfo(UserInfo("나마비루", "딤섬", 5))
        }
    }

    fun clearForTest() {
        viewModelScope.launch {
            clearUserInfo()
        }
    }

    val userInfoUiState: StateFlow<UserInfoUiState> = observeUserInfoUseCase().map { userInfo ->
        userInfo?.toUiState() ?: UserInfoUiState(
            nickName = "default",
            islandName = "default섬",
            profileImageIndex = 5
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = UserInfoUiState("default", "default섬", 5)
    )

    private val _letterDetail = MutableStateFlow<Letter?>(null)
    val letterDetail: StateFlow<Letter?> = _letterDetail

    private val _isLoadingLetterDetail = MutableStateFlow(false)
    val isLoadingLetterDetail: StateFlow<Boolean> = _isLoadingLetterDetail

    private val _letterDetailError = MutableStateFlow<String?>(null)
    val letterDetailError: StateFlow<String?> = _letterDetailError

    private val _homeRefresh = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val homeRefresh = _homeRefresh.asSharedFlow()

    // Toast / Snackbar / 단발성 UI 이벤트용
    private val _toastEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val toastEvent = _toastEvent.asSharedFlow()

    fun requestHomeRefresh() {
        _homeRefresh.tryEmit(Unit)
    }

    /**
     * 메세지 보내기
     */
    fun sendLetter(content: String) {
        viewModelScope.launch {
            runCatching {
                repo.sendLetter(content)
            }.onSuccess { res ->
                Logger.d("[letter/send] success, id=${res.letterId}")
                _toastEvent.tryEmit("편지를 띄웠어요")
            }.onFailure { e ->
                Logger.error("[letter/send] fail msg=${e.message} cause=${e.cause}")
                _toastEvent.tryEmit(e.message ?: "편지 전송에 실패했어요")
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
                    _toastEvent.tryEmit(_letterDetailError.value ?: "편지 조회에 실패했습니다.")
                }

            _isLoadingLetterDetail.value = false
        }
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
                _toastEvent.tryEmit("북마크에 저장 했어요")
            }.onFailure { e ->
                Logger.error("[add bookmark] fail letterId=$letterId msg=${e.message} cause=${e.cause}")
                _toastEvent.tryEmit(e.message ?: "북마크 저장에 실패했어요")
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
                _toastEvent.tryEmit("북마크를 삭제 했어요")
            }.onFailure { e ->
                Logger.error("[deleteBookmark] fail letterId=$letterId msg=${e.message} cause=${e.cause}")
                _toastEvent.tryEmit(e.message ?: "북마크 삭제에 실패했어요")
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
                _toastEvent.tryEmit("신고/차단이 되었어요")
            }.onFailure { e ->
                Logger.error("[report] fail letterId=$letterId msg=${e.message} cause=${e.cause}")
                _toastEvent.tryEmit(e.message ?: "편지 신고/차단에 실패했어요.  잠시 후 다시 시도해주세요")
            }
        }
    }

    private fun UserInfo.toUiState(): UserInfoUiState = UserInfoUiState(
        nickName = nickName,
        islandName = islandName,
        profileImageIndex = profileImageIndex
    )
}
