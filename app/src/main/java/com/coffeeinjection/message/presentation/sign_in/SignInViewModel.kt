package com.coffeeinjection.presentation.sign_in

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeeinjection.message.data.local.AuthDataStore
import com.coffeeinjection.message.data.local.LoginProvider
import com.coffeeinjection.message.data.local.UserInfo
import com.coffeeinjection.message.domain.usecase.ExchangeGoogleCodeToJwtUseCase
import com.coffeeinjection.message.domain.usecase.ExchangeKakaoCodeToJwtUseCase
import com.coffeeinjection.message.domain.usecase.GetGoogleLoginUrlUseCase
import com.coffeeinjection.message.domain.usecase.GetKakaoLoginUrlUseCase
import com.coffeeinjection.message.domain.usecase.RegisterFCMTokenUseCase
import com.coffeeinjection.message.domain.usecase.SaveAccessTokenUseCase
import com.coffeeinjection.message.domain.usecase.SaveRefreshTokenUseCase
import com.coffeeinjection.message.domain.usecase.SaveUserInfoUseCase
import com.coffeeinjection.message.presentation.sign_in.model.AuthUiState
import com.coffeeinjection.message.data.remote.interceptor.SessionManager
import com.coffeeinjection.message.util.Logger
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val getKakaoLoginUrl: GetKakaoLoginUrlUseCase,
    private val getGoogleLoginUrl: GetGoogleLoginUrlUseCase,
    private val exchangeKakaoCodeToJwt: ExchangeKakaoCodeToJwtUseCase,
    private val exchangeGoogleCodeToJwt: ExchangeGoogleCodeToJwtUseCase,
    private val saveAccessToken: SaveAccessTokenUseCase,
    private val saveRefreshToken: SaveRefreshTokenUseCase,
    private val saveUserInfo : SaveUserInfoUseCase,
    private val registerFCMTokenUseCase: RegisterFCMTokenUseCase,
    private val authDataStore: AuthDataStore,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    // --- Login URL ---
    fun loadKakaoLoginUrl() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, loginUrl = null)
        runCatching { getKakaoLoginUrl() }
            .onSuccess { res ->
                val url = res.loginUrl
                if (url.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "로그인 URL을 불러오지 못했습니다.",
                        loginUrl = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, loginUrl = url)
                }
            }
            .onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "로그인 URL을 불러오지 못했습니다.",
                    loginUrl = null
                )
            }
    }

    fun loadGoogleLoginUrl() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, loginUrl = null)
        runCatching { getGoogleLoginUrl() }
            .onSuccess { res ->
                val url = res.loginUrl
                if (url.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "로그인 URL을 불러오지 못했습니다.",
                        loginUrl = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, loginUrl = url)
                }
            }
            .onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "로그인 URL을 불러오지 못했습니다.",
                    loginUrl = null
                )
            }
    }

    // --- Code → JWT (DataStore 저장 중심) ---
    fun exchangeKakaoCode(code: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        // 로그인 절차 시작 시 기본 false로 내려두기(이전 값 남는 것 방지)
        runCatching { authDataStore.saveAutoLogin(false) }

        runCatching { exchangeKakaoCodeToJwt(code) }
            .onSuccess { res ->
                sessionManager.clearLogoutEventState()
                // 1) 토큰 저장(자동로그인 핵심)
                saveAccessToken(res.accessToken)

                // 2) 최근 로그인 프로바이더를 DataStore에 저장
                runCatching { authDataStore.saveLoginProvider(LoginProvider.KAKAO) }

                // 3) 네비게이션
                if (res.isNewMember && res.memberId == null) {
                    // 신규회원: FCM 등록은 completeSignup 후에 처리
                    runCatching { authDataStore.saveAutoLogin(false) }
                    _uiState.value =
                        _uiState.value.copy(isLoading = false, navigateToNickname = true)
                } else {
                    res.refreshToken?.let { saveRefreshToken(it) }
                    // 기존회원: refreshToken 저장 후 FCM 등록
                    runCatching {
                        authDataStore.saveAutoLogin(true)
                        saveUserInfo(
                            UserInfo(
                                nickName = res.nickname ?: "default",
                                islandName = res.islandName ?: "default",
                                profileImageIndex = res.profileImageIndex ?: 1
                            )
                        )
                    }
                    tryRegisterFcmAfterLogin()
                    _uiState.value = _uiState.value.copy(isLoading = false, navigateToMain = true)
                }

            }
            .onFailure { e ->
                Logger.error("[kakao] exchange fail ${e.message}")
                _uiState.value =
                    _uiState.value.copy(isLoading = false, errorMessage = "카카오 로그인 처리 중 오류가 발생했습니다")
            }
    }

    fun exchangeGoogleCode(code: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        runCatching { authDataStore.saveAutoLogin(false) }

        runCatching { exchangeGoogleCodeToJwt(code) }
            .onSuccess { res ->
                sessionManager.clearLogoutEventState()
                // 1) 토큰 저장
                saveAccessToken(res.accessToken)

                // 2) 최근 로그인 프로바이더(DataStore)
                runCatching { authDataStore.saveLoginProvider(LoginProvider.GOOGLE) }

                // 3) 네비게이션
                if (res.isNewMember && res.memberId == null) {
                    // 신규회원: FCM 등록은 completeSignup 후에 처리
                    runCatching { authDataStore.saveAutoLogin(false) }
                    _uiState.value =
                        _uiState.value.copy(isLoading = false, navigateToNickname = true)
                } else {
                    res.refreshToken?.let {
                        saveRefreshToken(it)
                        saveUserInfo(
                            UserInfo(
                                nickName = res.nickname ?: "default",
                                islandName = res.islandName ?: "default",
                                profileImageIndex = res.profileImageIndex ?: 1
                            )
                        )
                    }
                    // 기존회원: refreshToken 저장 후 FCM 등록
                    runCatching { authDataStore.saveAutoLogin(true) }
                    tryRegisterFcmAfterLogin()
                    _uiState.value = _uiState.value.copy(isLoading = false, navigateToMain = true)
                }
            }
            .onFailure { e ->
                Logger.error("[google] exchange fail ${e.message}")
                _uiState.value =
                    _uiState.value.copy(isLoading = false, errorMessage = "구글 로그인 처리 중 오류가 발생했습니다")
            }
    }

    /** 기존 회원 로그인 직후 FCM 등록 (refreshToken 저장 완료 후 호출) */
    private fun tryRegisterFcmAfterLogin() = viewModelScope.launch {
        var current = authDataStore.getFcmToken()
        val last = authDataStore.getLastRegisteredFcmToken()

        if (current.isNullOrBlank()) {
            val fetched = runCatching { FirebaseMessaging.getInstance().token.await() }.getOrNull()
            if (fetched.isNullOrBlank()) return@launch
            runCatching { authDataStore.saveFcmToken(fetched) }
            current = fetched
        }

        if (current == last) return@launch

        runCatching { registerFCMTokenUseCase(current!!) }
            .onSuccess { runCatching { authDataStore.saveLastRegisteredFcmToken(current!!) } }
    }

    fun consumedNavigation() {
        _uiState.value = _uiState.value.copy(navigateToMain = false, navigateToNickname = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearUiState() {
        _uiState.value = AuthUiState(false, null, null, false, false)
    }
}
