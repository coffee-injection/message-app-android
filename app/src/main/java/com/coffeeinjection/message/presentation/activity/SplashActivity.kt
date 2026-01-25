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
import kotlinx.coroutines.flow.firstOrNull
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

        // 스플래시에서 자동로그인 분기
        lifecycleScope.launch {
            // 1초
            delay(1000)

            val token = authDataStore.accessTokenFlow.firstOrNull()
            Logger.d("[Splash] accessToken = ${token?.let { "***" } ?: "null"}")

            val intent = Intent(this@SplashActivity, MainActivity::class.java).apply {
                if (!token.isNullOrBlank()) {
                    // 자동로그인: 홈으로 시작
                    putExtra("startDestination", "home")
                }
                // 딥링크/푸시 전달이 필요하면 여기서 putExtra로 함께 넘기면 됩니다.
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
            finish()
        }
    }
}
