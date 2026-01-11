package com.coffeeinjection.presentation.sign_in

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeeinjection.message.domain.usecase.ExchangeCodeToJwtUseCase
import com.coffeeinjection.message.domain.usecase.GetKakaoLoginUrlUseCase
import com.coffeeinjection.message.domain.usecase.SaveAccessTokenUseCase
import com.coffeeinjection.message.presentation.sign_in.model.AuthUiState
import com.coffeeinjection.message.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val getKakaoLoginUrl : GetKakaoLoginUrlUseCase,
    private val exchangeCodeToJwt : ExchangeCodeToJwtUseCase,
    private val saveAccessToken : SaveAccessTokenUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    /** 1) 로그인 URL 요청 */
    fun loadKakaoLoginUrl() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        runCatching { getKakaoLoginUrl() }
            .onSuccess {
                Logger.d("[kakao] loadKakaoLoginUrl success!! url(${it.loginUrl})")
                _uiState.value = _uiState.value.copy(isLoading = false, loginUrl = it.loginUrl)
            }
            .onFailure { e ->
                Logger.error("[kakao] loadKakaoLoginUrl fail error.message(${e.message}) error.cause(${e.cause})")
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "로그인 URL을 불러오지 못했습니다.")
            }
    }

    /** 2) code → JWT 교환 */
    fun exchangeCode(code: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        runCatching { exchangeCodeToJwt(code) }
            .onSuccess { res ->
                saveAccessToken(res.accessToken)
//                if (res.isNewMember && res.memberId == null) {
                    Logger.d("[kakao] exchangeCode success -> new user")

                    // 신규 회원: 닉네임 입력 화면으로 이동
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        navigateToNickname = true
                    )
//                } else {
//                    // 기존 회원: 액세스 토큰 저장 후 메인 이동
//                    Logger.d("[kakao] exchangeCode success -> old user")
//                    _uiState.value = _uiState.value.copy(isLoading = false, navigateToMain = true)
//                }
            }
            .onFailure { e ->
                Logger.error("[kakao] exchangeCode fail errorMsg(${e.message}) cause(${e.cause})")
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "카카오 로그인 처리 중 오류가 발생했습니다")
            }
    }

    /** 4) 일회성 네비게이션 플래그 리셋 */
    fun consumedNavigation() {
        _uiState.value = _uiState.value.copy(navigateToMain = false, navigateToNickname = false)
    }

    /** 5) 에러 확인 후 리셋 */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearUiState() {
        _uiState.value = AuthUiState(false, null, null, false, false)
    }
}