package com.coffeeinjection.message.presentation.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.coffeeinjection.message.R
import com.coffeeinjection.message.data.local.AuthDataStore
import com.coffeeinjection.message.util.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject lateinit var authDataStore: AuthDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        lifecycleScope.launch {
            delay(1000)

            val token = authDataStore.getAccessToken()
            val canAutoLogin = authDataStore.getAutoLogin()

            Logger.d("[Splash] token=${token?.let { "***" } ?: "null"}, auto=$canAutoLogin")

            val intent = Intent(this@SplashActivity, MainActivity::class.java).apply {
                when {
                    !token.isNullOrBlank() && canAutoLogin -> {
                        // 가입 완료된 사용자만 홈으로 자동 진입
                        putExtra("startDestination", "home")
                    }
                    !token.isNullOrBlank() && !canAutoLogin -> {
                        // 토큰은 있지만 가입 미완료(닉네임/회원정보 입력 중 종료) -> 가입 이어가기 화면으로
                        // 네비 이름은 프로젝트에 맞게 변경하세요.
                        putExtra("startDestination", "nickname")
                    }
                    else -> {
                        // 토큰 없음 -> 로그인
                        putExtra("startDestination", "sign_in")
                    }
                }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }

            startActivity(intent)
            finish()
        }
    }
}