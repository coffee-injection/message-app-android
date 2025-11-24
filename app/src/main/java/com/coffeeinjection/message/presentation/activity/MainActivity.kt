package com.coffeeinjection.message.presentation.activity

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.coffeeinjection.message.databinding.ActivityMainBinding
import com.coffeeinjection.message.util.Logger
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var splashScreen: SplashScreen

    companion object {
        private const val TAG = "MainActivity"
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d("onCreate")
        WindowCompat.setDecorFitsSystemWindows(window, false) // 엣지투엣지 ON
        if (Build.VERSION.SDK_INT >= 21) {
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT // 하단바도 투명 (제조사 커스텀에 따라 차이)
        }

        // setContentView하기 전에 installSplashScreen() 필수
        splashScreen = installSplashScreen()
        startSplash()
        setContentView(binding.root)
    }


    // splash의 애니메이션 설정
    @RequiresApi(Build.VERSION_CODES.S)
    private fun startSplash() {
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 5f, 1f)
            val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 5f, 1f)

            ObjectAnimator.ofPropertyValuesHolder(splashScreenView.iconView, scaleX, scaleY).run {
                interpolator = AnticipateInterpolator()
                duration = 3000L
                doOnEnd {
                    splashScreenView.remove()
                }
                start()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Logger.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Logger.d("onResume")
    }

    override fun onPause() {
        Logger.d("onPause")
        super.onPause()
    }

    override fun onStop() {
        Logger.d("onStop")
        super.onStop()
    }

    override fun onDestroy() {
        Logger.d("onDestroy")
        super.onDestroy()
    }
}