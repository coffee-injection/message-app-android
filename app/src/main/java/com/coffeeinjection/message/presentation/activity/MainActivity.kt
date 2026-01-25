package com.coffeeinjection.message.presentation.activity

import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.fragment.NavHostFragment
import com.coffeeinjection.message.R
import com.coffeeinjection.message.databinding.ActivityMainBinding
import com.coffeeinjection.message.util.Logger
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val viewModel: SharedViewModel by viewModels()

    companion object {
        private const val TAG = "MainActivity"
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d("onCreate")
        setContentView(binding.root)

        // NavHost / 그래프 로드
        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        val navController = navHost.navController
        val inflater = navController.navInflater
        val graph = inflater.inflate(R.navigation.nav_main)

        // Splash에서 전달한 시작 목적지: "home" | "sign_in" | (기본) null
        when (intent.getStringExtra("startDestination")) {
            "home" -> {
                graph.setStartDestination(R.id.homeFragment)
                navController.setGraph(graph, bundleOf(/* 필요 시 초기 인자 */))
                Logger.d(TAG, "startDestination = homeFragment")
            }
            "sign_in" -> {
                graph.setStartDestination(R.id.signInFragment)
                navController.setGraph(graph, bundleOf())
                Logger.d(TAG, "startDestination = signInFragment")
            }
            else -> {
                // XML 기본값(guideFragment) 사용
                navController.setGraph(graph, bundleOf())
                Logger.d(TAG, "startDestination = default(nav XML)")
            }
        }

        viewModel.initForTest()
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
        viewModel.clearForTest()
        super.onDestroy()
    }
}
