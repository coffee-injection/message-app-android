package com.coffeeinjection.presentation.sign_in

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeeinjection.message.data.local.AuthDataStore
import com.coffeeinjection.message.domain.usecase.ExchangeGoogleCodeToJwtUseCase
import com.coffeeinjection.message.domain.usecase.ExchangeKakaoCodeToJwtUseCase
import com.coffeeinjection.message.domain.usecase.GetGoogleLoginUrlUseCase
import com.coffeeinjection.message.domain.usecase.GetKakaoLoginUrlUseCase
import com.coffeeinjection.message.domain.usecase.SaveAccessTokenUseCase
import com.coffeeinjection.message.domain.usecase.RegisterFCMTokenUseCase
import com.coffeeinjection.message.presentation.sign_in.model.AuthUiState
import com.coffeeinjection.message.util.Logger
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val getKakaoLoginUrl : GetKakaoLoginUrlUseCase,
    private val getGoogleLoginUrl : GetGoogleLoginUrlUseCase,
    private val exchangeKakaoCodeToJwt : ExchangeKakaoCodeToJwtUseCase,
    private val exchangeGoogleCodeToJwt : ExchangeGoogleCodeToJwtUseCase,
    private val saveAccessToken : SaveAccessTokenUseCase,
    private val registerFCMTokenUseCase: RegisterFCMTokenUseCase,
    private val authDataStore: AuthDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    /** 1) kakao 로그인 URL 요청 */
    fun loadKakaoLoginUrl() = viewModelScope.launch {
        // 시작 시 이전 URL 제거 (stale 로딩 방지)
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null,
            loginUrl = null
        )

        runCatching { getKakaoLoginUrl() }
            .onSuccess { res ->
                val url = res.loginUrl
                if (url.isBlank()) {
                    Logger.error("[kakao] loadKakaoLoginUrl empty url")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "로그인 URL을 불러오지 못했습니다.",
                        loginUrl = null
                    )
                } else {
                    Logger.d("[kakao] loadKakaoLoginUrl success!! url($url)")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        loginUrl = url
                    )
                }
            }
            .onFailure { e ->
                Logger.error("[kakao] loadKakaoLoginUrl fail error.message(${e.message}) error.cause(${e.cause})")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "로그인 URL을 불러오지 못했습니다.",
                    loginUrl = null
                )
            }
    }

    /** 1) google 로그인 URL 요청 */
    fun loadGoogleLoginUrl() = viewModelScope.launch {
        // 시작 시 이전 URL 제거 (stale 로딩 방지)
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null,
            loginUrl = null
        )

        runCatching { getGoogleLoginUrl() }
            .onSuccess { res ->
                val url = res.loginUrl
                if (url.isBlank()) {
                    Logger.error("[google] loadGoogleLoginUrl empty url")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "로그인 URL을 불러오지 못했습니다.",
                        loginUrl = null
                    )
                } else {
                    Logger.d("[google] loadGoogleLoginUrl success!! url($url)")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        loginUrl = url
                    )
                }
            }
            .onFailure { e ->
                Logger.error("[google] loadGoogleLoginUrl fail error.message(${e.message}) error.cause(${e.cause})")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "로그인 URL을 불러오지 못했습니다.",
                    loginUrl = null
                )
            }
    }


    /** 2) kakao code → JWT 교환 */
    fun exchangeKakaoCode(code: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        runCatching { exchangeKakaoCodeToJwt(code) }
            .onSuccess { res ->
                saveAccessToken(res.accessToken)

                // 로그인 직후 FCM 등록 시도
                tryRegisterFcmAfterLogin()

                if (res.isNewMember && res.memberId == null) {
                    Logger.d("[kakao] exchangeCode success -> new user")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        navigateToNickname = true
                    )
                } else {
                    Logger.d("[kakao] exchangeCode success -> old user")
                    _uiState.value = _uiState.value.copy(isLoading = false, navigateToMain = true)
                }
            }
            .onFailure { e ->
                Logger.error("[kakao] exchangeCode fail errorMsg(${e.message}) cause(${e.cause})")
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "카카오 로그인 처리 중 오류가 발생했습니다")
            }
    }

    /** 2) google code → JWT 교환 */
    fun exchangeGoogleCode(code: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        runCatching { exchangeGoogleCodeToJwt(code) }
            .onSuccess { res ->
                saveAccessToken(res.accessToken)

                // 로그인 직후 FCM 등록 시도
                tryRegisterFcmAfterLogin()

                if (res.isNewMember && res.memberId == null) {
                    Logger.d("[google] exchangeCode success -> new user")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        navigateToNickname = true
                    )
                } else {
                    Logger.d("[google] exchangeCode success -> old user")
                    _uiState.value = _uiState.value.copy(isLoading = false, navigateToMain = true)
                }
            }
            .onFailure { e ->
                Logger.error("[google] exchangeCode fail errorMsg(${e.message}) cause(${e.cause})")
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "구글 로그인 처리 중 오류가 발생했습니다")
            }
    }

    /** 로그인 직후 FCM 등록 (중복/401 방지) */
    private fun tryRegisterFcmAfterLogin() = viewModelScope.launch {
        Logger.d("[FCM] tryRegisterFcmAfterLogin() start")

        // 1) 로컬에 저장된 현재 토큰/마지막 등록 토큰 조회
        var current = authDataStore.getFcmToken()
        val last = authDataStore.getLastRegisteredFcmToken()
        Logger.d("[FCM] current(local)=$current, lastRegistered=$last")

        // 2) current가 비어있으면 즉시 가져와서 저장
        if (current.isNullOrBlank()) {
            Logger.i("[FCM] current is null → fetch now via getInstance().token.await()")
            val fetched = runCatching {
                FirebaseMessaging.getInstance().token.await()
            }.onFailure { e ->
                Logger.error("[FCM] getToken() failed: ${e.message}")
            }.getOrNull()

            Logger.d( "[FCM] fetched=$fetched")

            if (fetched.isNullOrBlank()) {
                Logger.i("[FCM] still null after fetch → skip, will retry later")
                return@launch
            }
            runCatching { authDataStore.saveFcmToken(fetched) }
                .onSuccess { Logger.d("[FCM] saved fetched token locally") }
                .onFailure { e -> Logger.error("[FCM] saveFcmToken() failed: ${e.message}") }

            current = fetched
        }

        // 3) 중복 등록 방지
        if (current == last) {
            Logger.i("[FCM] skip: already registered token (current == lastRegistered)")
            return@launch
        }

        // 4) 서버 등록
        Logger.d("[FCM] register start, token=$current")
        runCatching { registerFCMTokenUseCase(current!!) }
            .onSuccess {
                Logger.d("[FCM] register success")
                runCatching { authDataStore.saveLastRegisteredFcmToken(current!!) }
                    .onSuccess { Logger.d("[FCM] lastRegistered updated") }
                    .onFailure { e -> Logger.error("[FCM] saveLastRegisteredFcmToken() failed: ${e.message}") }
            }
            .onFailure { e ->
                Logger.error("[FCM] register fail: ${e.message}")
            }

        Logger.d("[FCM] tryRegisterFcmAfterLogin() end")
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
