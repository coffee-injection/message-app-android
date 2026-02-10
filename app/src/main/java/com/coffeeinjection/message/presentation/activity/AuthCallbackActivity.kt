package com.coffeeinjection.message.presentation.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.coffeeinjection.message.util.Logger

class AuthCallbackActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_GOOGLE_CODE = "extra_google_code"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri: Uri? = intent?.data
        val code = uri?.getQueryParameter("code")

        Logger.d("[AuthCallbackActivity] uri=$uri code=${code?.let { "***" } ?: "null"}")

        val next = Intent(this, MainActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_NEW_TASK
            )
            if (!code.isNullOrBlank()) {
                putExtra(EXTRA_GOOGLE_CODE, code)
            }
        }
        startActivity(next)
        finish()
    }
}
