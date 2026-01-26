package com.coffeeinjection.message.presentation.user_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeeinjection.message.data.local.UserInfo
import com.coffeeinjection.message.domain.usecase.CheckNicknameDuplicateUseCase
import com.coffeeinjection.message.domain.usecase.CompleteSignupUseCase
import com.coffeeinjection.message.domain.usecase.ModifyUserProfileUseCase
import com.coffeeinjection.message.domain.usecase.SaveAccessTokenUseCase
import com.coffeeinjection.message.domain.usecase.SaveUserInfoUseCase
import com.coffeeinjection.message.presentation.sign_in.model.AuthUiState
import com.coffeeinjection.message.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject
import kotlin.jvm.Throws

@HiltViewModel
class UserInfoViewModel @Inject constructor(
    private val complete: CompleteSignupUseCase,
    private val saveAccessToken : SaveAccessTokenUseCase,
    private val saveUserInfo : SaveUserInfoUseCase,
    private val checkDuplicate : CheckNicknameDuplicateUseCase,
    private val modifyInfo: ModifyUserProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    private val _nickNameGuideUiState = MutableLiveData(NickNameGuideState.IDLE)
    val nickNameGuideUiState get() = _nickNameGuideUiState

    private val _duplicateEnable = MutableLiveData<Boolean>(false)
    val duplicateEnable get() = _duplicateEnable

    private var isNickNameChecked = false // 중복 체크가 실행 됐는지

    fun updateIsChecked( isChecked: Boolean) { isNickNameChecked = isChecked }
    fun isChecked() = isNickNameChecked

    fun updateDuplicateEnable(isEnable : Boolean){
        _duplicateEnable.value = isEnable
    }

    fun updateNickNameGuideUiState(state : NickNameGuideState){
        _nickNameGuideUiState.value = state
    }

    fun modifyUserInfo(userInfo: UserInfo) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        runCatching { modifyInfo(userInfo) }
            .onSuccess { res ->
                Logger.d("[kakao] modifyUserInfo success")
                saveUserInfo(userInfo)
                _uiState.value = _uiState.value.copy(isLoading = false, closeModifyDialog = true)
            }
            .onFailure { e ->
                printError(e, "completeSignup")
                isNickNameChecked = false
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "유저 정보 수정에 실패했습니다")
            }

    }

    /** 신규회원 닉네임 완료 */
    fun completeSignup(userInfo: UserInfo) = viewModelScope.launch {

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        runCatching { complete(userInfo) }
            .onSuccess { res ->
                Logger.d("[kakao] completeSignup success")
                // 서버가 최종 토큰을 내려줌
                saveAccessToken(res.accessToken)
                saveUserInfo(userInfo)

                _uiState.value = _uiState.value.copy(isLoading = false, navigateToMain = true)
            }
            .onFailure { e ->
                printError(e, "completeSignup")
                isNickNameChecked = false
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "회원가입 완료 처리에 실패했습니다")
            }
    }

     fun checkNickNameAvailable(nickname: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        runCatching {
            checkDuplicate(nickname)
        }.onSuccess { res ->
            Logger.d("[kakao] checkDuplicate msg(${res.message})")
            val isAvailable = res.available
            _uiState.value = _uiState.value.copy(isLoading = false, errorMessage =  if (isAvailable) null else "중복된 닉네임 입니다.")
            updateIsChecked(isAvailable)
            updateNickNameGuideUiState(if (isAvailable) NickNameGuideState.AVAILABLE else NickNameGuideState.DUPLICATE)
        }.onFailure { e ->
            printError(e, "checkNickname")
            _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "닉네임 중복 확인에 실패했습니다.")
            updateNickNameGuideUiState( NickNameGuideState.CHECK_FAILED)
        }
    }

    /** 에러 확인 후 리셋 */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }


    private fun printError(e : Throwable, tag: String){
        Logger.error("[kakao] $tag fail errorMsg(${e.message}) cause(${e.cause}) body(${e})")
        val errBody = runCatching { (e as HttpException).response()?.errorBody()?.string() }.getOrNull()
        Logger.error("[kakao] $tag HTTP ${(e as HttpException).code()} errorBody=$errBody")
    }
}