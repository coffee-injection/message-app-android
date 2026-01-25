package com.coffeeinjection.message.presentation.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeeinjection.message.data.local.AuthDataStore
import com.coffeeinjection.message.domain.usecase.DeleteFCMTokenUseCase
import com.coffeeinjection.message.domain.usecase.Withdraw
import com.coffeeinjection.message.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val withdrawSuccess: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val withdraw: Withdraw,
    private val deleteFCMTokenUseCase: DeleteFCMTokenUseCase,
    private val authDataStore: AuthDataStore
) : ViewModel() {

    private val _ui = MutableStateFlow(SettingsUiState())
    val ui: StateFlow<SettingsUiState> = _ui

    fun clearMessage() {
        _ui.value = _ui.value.copy(errorMessage = null)
    }

    /**
     * 회원 탈퇴 플로우
     * 1) 서버 withdraw
     * 2) FCM 토큰 서버 삭제 (있다면)
     * 3) 로컬 데이터 비우기
     */
    fun executeWithdraw() = viewModelScope.launch {
        _ui.value = _ui.value.copy(isLoading = true, errorMessage = null, withdrawSuccess = false)

        runCatching {
            // 1) 서버 탈퇴
            withdraw()

            // 2) 서버 FCM 토큰 삭제
            authDataStore.getFcmToken()?.let { token ->
                if (token.isNotBlank()) {
                    runCatching { deleteFCMTokenUseCase(token) }
                        .onFailure { e -> Logger.e("Settings", "deleteFCMTokenUseCase fail: ${e.message}") }
                }
            }

            // 3) 로컬 데이터 초기화
            authDataStore.clearAll()
        }.onSuccess {
            _ui.value = _ui.value.copy(isLoading = false, withdrawSuccess = true)
        }.onFailure { e ->
            _ui.value = _ui.value.copy(isLoading = false, errorMessage = (e.message ?: "탈퇴 요청에 실패했습니다."))
        }
    }
}
