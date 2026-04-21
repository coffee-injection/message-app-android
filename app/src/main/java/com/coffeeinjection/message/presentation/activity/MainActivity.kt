package com.coffeeinjection.message.presentation.activity

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.coffeeinjection.message.R
import com.coffeeinjection.message.data.remote.interceptor.SessionManager
import com.coffeeinjection.message.databinding.ActivityMainBinding
import com.coffeeinjection.message.presentation.sign_in.AuthDeepLinkViewModel
import com.coffeeinjection.message.util.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val viewModel: SharedViewModel by viewModels()
    private val authDeepLinkViewModel: AuthDeepLinkViewModel by viewModels()
    @Inject
    lateinit var sessionManager: SessionManager

    companion object {
        private const val TAG = "MainActivity"
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d("onCreate")
        setContentView(binding.root)

        observeToastEvent()
        observeLogoutEvent()

        // Android35 이상 엣지투엣지 대응  다크모드 시스템바 색상 설정
        applyMainBackgroundByTheme()

        handleAuthIntent(intent)

        // NavHost / 그래프 로드
        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        val navController = navHost.navController
        val inflater = navController.navInflater
        val graph = inflater.inflate(R.navigation.nav_main)

        when (intent.getStringExtra("startDestination")) {
            "home" -> {
                graph.setStartDestination(R.id.homeFragment)
                navController.setGraph(graph, bundleOf())
                Logger.d(TAG, "startDestination = homeFragment")
            }
            "sign_in", "signIn" -> {
                graph.setStartDestination(R.id.signInFragment)
                navController.setGraph(graph, bundleOf())
                Logger.d(TAG, "startDestination = signInFragment")
            }
            else -> {
                navController.setGraph(graph, bundleOf())
                Logger.d(TAG, "startDestination = default(nav XML)")
            }
        }
    }

    private fun observeToastEvent() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.toastEvent.collect { message ->
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observeLogoutEvent() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sessionManager.logoutEvent.collect {
                    moveToSignInBySessionExpired()
                }
            }
        }
    }

    private fun moveToSignInBySessionExpired() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host) as? NavHostFragment ?: return
        val navController = navHostFragment.navController
        if (navController.currentDestination?.id == R.id.signInFragment) return
        val options = NavOptions.Builder()
            .setPopUpTo(navController.graph.id, true)
            .setLaunchSingleTop(true)
            .build()
        navController.navigate(R.id.signInFragment, null, options)
        Toast.makeText(this@MainActivity,getString(R.string.toast_token_is_expired), Toast.LENGTH_SHORT).show()
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
        //viewModel.clearForTest()
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAuthIntent(intent)
    }

    private fun handleAuthIntent(intent: Intent?) {
        val code = intent?.getStringExtra(AuthCallbackActivity.EXTRA_GOOGLE_CODE)
        if (!code.isNullOrBlank()) {
            Logger.d("[MainActivity] received google code = ***")
            authDeepLinkViewModel.setGoogleCode(code)

            // 재처리 방지
            intent.removeExtra(AuthCallbackActivity.EXTRA_GOOGLE_CODE)
        }
    }

    private fun applyMainBackgroundByTheme() {
        val isNight =
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                    Configuration.UI_MODE_NIGHT_YES

        binding.main.setBackgroundColor(
            if (isNight) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
        )
    }
}