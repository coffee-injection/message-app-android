package com.coffeeinjection.message.presentation.user_info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeeinjection.message.data.local.UserInfo
import com.coffeeinjection.message.domain.usecase.CompleteSignupUseCase
import com.coffeeinjection.message.domain.usecase.SaveAccessTokenUseCase
import com.coffeeinjection.message.domain.usecase.SaveUserInfoUseCase
import com.coffeeinjection.message.presentation.sign_in.model.AuthUiState
import com.coffeeinjection.message.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserInfoViewModel @Inject constructor(
    private val complete: CompleteSignupUseCase,
    private val saveAccessToken : SaveAccessTokenUseCase,
    private val saveUserInfo : SaveUserInfoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    /** 3) 신규회원 닉네임 완료 */
    fun completeSignup(nickname: String) = viewModelScope.launch {
        if (nickname.length !in 2..20) {
            Logger.error("[kakao] completeSignup fail -> nickname is invalid")
            _uiState.value = _uiState.value.copy(errorMessage = "닉네임은 2자 이상 20자 이하로 입력해주세요")
            return@launch
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        runCatching { complete(nickname) }
            .onSuccess { res ->
                Logger.d("[kakao] completeSignup success")
                // 서버가 최종 토큰을 내려줌
                saveAccessToken(res.accessToken)
                saveUserInfo(UserInfo(nickname, null))

                _uiState.value = _uiState.value.copy(isLoading = false, navigateToMain = true)
            }
            .onFailure { e ->
                Logger.error("[kakao] completeSignup fail errorMsg(${e.message}) cause(${e.cause})")
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "회원가입 완료 처리에 실패했습니다")
            }
    }
}