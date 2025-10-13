package com.coffeeinjection.message.presentation

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.coffeeinjection.message.R

class SplashActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private val navigateToMain = Runnable {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        handler.postDelayed(navigateToMain, SPLASH_DELAY)
    }

    override fun onDestroy() {
        handler.removeCallbacks(navigateToMain)
        super.onDestroy()
    }

    companion object {
        private const val SPLASH_DELAY = 3_000L
    }
}
