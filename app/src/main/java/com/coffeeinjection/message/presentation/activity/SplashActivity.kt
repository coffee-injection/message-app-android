package com.coffeeinjection.message.presentation.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.coffeeinjection.message.R
import com.coffeeinjection.message.data.local.AuthDataStore
import com.coffeeinjection.message.util.Logger
import com.coffeeinjection.message.util.TokenStateEnum
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject lateinit var authDataStore: AuthDataStore
    private val viewModel : SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        observeTokenState()

        lifecycleScope.launch {
            delay(1000)

            val token = authDataStore.getAccessToken()
            val canAutoLogin = authDataStore.getAutoLogin()

            Logger.d("[Splash] token=${token?.let { "***" } ?: "null"}, auto=$canAutoLogin")

            when{
                !token.isNullOrBlank() && canAutoLogin -> {
                    // 가입 완료된 사용자일 경우 토큰 유효성 체크 후 자동 로그인 진행 //"home"
                    viewModel.checkTokenValidation()
                }
                !token.isNullOrBlank() && !canAutoLogin -> {
                    // 토큰은 있지만 가입 미완료(닉네임/회원정보 입력 중 종료) -> 가입 이어가기 화면으로
                    // 네비 이름은 프로젝트에 맞게 변경하세요.
                    startActivity("nickname")
                    finish()
                }
                else -> {
                    // 토큰 없음 -> 로그인
                    startActivity("sign_in")
                    finish()
                }
            }
        }
    }

    private fun observeTokenState() {
        viewModel.tokenState.observe(this) { state ->
            when (state) {
                TokenStateEnum.NONE -> Unit

                TokenStateEnum.VALID,
                TokenStateEnum.REFRESHED -> {
                    Logger.d("[Splash] auto login success : $state")
                    startActivity("home")
                    finish()
                }

                TokenStateEnum.EXPIRED -> {
                    Logger.d("[Splash] session expired -> sign_in")
                    startActivity("sign_in")
                    finish()
                }

                TokenStateEnum.ERROR -> {
                    Logger.d("[Splash] token validation error")
                    AlertDialog.Builder(this)
                        .setMessage("네트워크 상태를 확인한 후 다시 시도해주세요.")
                        .setCancelable(false)
                        .setPositiveButton("다시 시도") { _, _ ->
                            viewModel.checkTokenValidation()
                        }
                        .setNegativeButton("로그인") { _, _ ->
                            startActivity("sign_in")
                            finish()
                        }
                        .show()

                }

                else -> Unit
            }
        }
    }

    private fun startActivity(value : String){
        val intent = Intent(this@SplashActivity, MainActivity::class.java).apply {
            putExtra("startDestination",value)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
        finish()
    }

}