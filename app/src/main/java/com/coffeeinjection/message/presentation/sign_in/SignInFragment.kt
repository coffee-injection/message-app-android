package com.coffeeinjection.presentation.sign_in

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.coffeeinjection.message.BuildConfig
import com.coffeeinjection.message.R
import com.coffeeinjection.message.databinding.FragmentSignInBinding
import com.coffeeinjection.message.presentation.BaseFragment
import com.coffeeinjection.message.presentation.sign_in.AuthDeepLinkViewModel
import com.coffeeinjection.message.util.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignInFragment : BaseFragment<FragmentSignInBinding>(FragmentSignInBinding::inflate) {

    private val viewModel: SignInViewModel by viewModels()
    private val authDeepLinkViewModel: AuthDeepLinkViewModel by activityViewModels()

    private var backPressedTime: Long = 0L
    private var lastLoadedLoginUrl: String? = null

    // 허용 도메인(루트 기준) - 카카오 WebView용
    private val allowedSuffixes = setOf(
        "kakao.com", "google.com", "gstatic.com", "15.164.112.136"
    )

    private enum class LoginType { GOOGLE, KAKAO }
    private var pendingLoginType: LoginType? = null

    private val kakaoCallbackPrefix = BuildConfig.API_SEVER_BASE_URL + "auth/kakao/callback"

    /** 공용 로딩 표시/숨김 */
    private fun showLoading() { binding.progressBar?.visibility = View.VISIBLE }
    private fun hideLoading() { binding.progressBar?.visibility = View.GONE }

    private val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (binding.webView.isVisible) {
                clearWebViewAndCookies()
                return
            }
            if (System.currentTimeMillis() - backPressedTime <= 2000) {
                requireActivity().finish()
            } else {
                backPressedTime = System.currentTimeMillis()
                Toast.makeText(requireContext(), "한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun setupViews(savedInstanceState: Bundle?) = with(binding) {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadsImagesAutomatically = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = WebSettings.LOAD_DEFAULT
            safeBrowsingEnabled = true
        }

        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }

        webView.visibility = View.GONE

        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return handleNavigation(url)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?, request: WebResourceRequest?
            ): Boolean {
                return handleNavigation(request?.url?.toString())
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Logger.d("[signIn] onPageStarted url=$url")
                webView.visibility = View.GONE
                showLoading()
            }

            override fun onPageCommitVisible(view: WebView?, url: String?) {
                super.onPageCommitVisible(view, url)
                if (!url.isNullOrBlank() && url != "about:blank") {
                    webView.visibility = View.VISIBLE
                    hideLoading()
                }
            }

            private fun handleNavigation(url: String?): Boolean {
                if (url.isNullOrBlank()) return false

                // 카카오 콜백은 WebView에서 가로채서 code 추출
                if (url.startsWith(kakaoCallbackPrefix)) {
                    Uri.parse(url).getQueryParameter("code")?.let {
                        clearWebView()
                        viewModel.exchangeKakaoCode(it)
                    }
                    return true
                }

                // 허용 도메인만 WebView 로드 허용 (카카오용)
                if (!isAllowedHost(url)) {
                    Logger.d("[signIn] blocked external url=$url")
                    return true
                }
                return false
            }
        }

        btnKakao.setCenterIconWithText(true)
    }

    override fun setupListeners() = with(binding) {
        super.setupListeners()

        btnGoogle.setStartIcon(context?.let { ContextCompat.getDrawable(it, R.drawable.ic_google) }, 30f)
        btnGoogle.setCenterIconWithText(true)
        btnGoogle.setOnClickListener {
            Logger.d("[구글 로그인] Btn Click")
            pendingLoginType = LoginType.GOOGLE
            clearWebView()
            lastLoadedLoginUrl = null
            showLoading()
            viewModel.loadGoogleLoginUrl()
        }

        btnKakao.setStartIcon(context?.let { ContextCompat.getDrawable(it, R.drawable.ic_kakao) }, 30f)
        btnKakao.setOnClickListener {
            Logger.d("[카카오 로그인] Btn Click")
            pendingLoginType = LoginType.KAKAO
            clearWebView()
            lastLoadedLoginUrl = null
            showLoading()
            viewModel.loadKakaoLoginUrl()
        }
    }

    override fun setupCollectors() {
        super.setupCollectors()

        // 1) 기존 uiState collect
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                Logger.d("[uistate check!!] : $state")

                state.errorMessage?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    viewModel.clearError()
                }

                val nextUrl = state.loginUrl
                if (!nextUrl.isNullOrBlank() && lastLoadedLoginUrl != nextUrl) {
                    when (pendingLoginType) {
                        LoginType.GOOGLE -> {
                            // 구글은 WebView 금지 -> CustomTab
                            openCustomTab(nextUrl)
                            lastLoadedLoginUrl = nextUrl
                            hideLoading()
                        }
                        LoginType.KAKAO, null -> {
                            binding.webView.loadUrl(nextUrl)
                            lastLoadedLoginUrl = nextUrl
                        }
                    }
                }

                if (state.navigateToNickname) {
                    clearWebView()
                    findNavController().navigate(
                        SignInFragmentDirections.actionSignInFragmentToUserInfoFragment()
                    )
                }

                if (state.navigateToMain) {
                    clearWebView()
                    findNavController().navigate(
                        SignInFragmentDirections.actionSignInFragmentToHomeFragment()
                    )
                }
            }
        }

        // 2) 딥링크로 돌아온 Google code 수신 -> exchange 호출
        viewLifecycleOwner.lifecycleScope.launch {
            authDeepLinkViewModel.googleCode.collectLatest { code ->
                if (code.isNullOrBlank()) return@collectLatest

                Logger.d("[SignIn] received google code = ***")

                // 재처리 방지
                authDeepLinkViewModel.consumeGoogleCode()

                // 구글 로그인 처리 시작
                clearWebView()
                pendingLoginType = null
                viewModel.exchangeGoogleCode(code)
            }
        }
    }

    /** 화면만 정리 (destroy 금지) */
    private fun clearWebView() {
        viewModel.consumedNavigation()
        binding.webView.apply {
            try {
                stopLoading()
                clearHistory()
            } catch (_: Throwable) {}
            visibility = View.GONE
        }
        hideLoading()
        lastLoadedLoginUrl = null
    }

    private fun isAllowedHost(url: String): Boolean {
        val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return false
        val schemeOk = uri.scheme == "https" || uri.scheme == "http"
        val host = uri.host ?: return false
        val hostOk = allowedSuffixes.any { host == it || host.endsWith(".$it") }
        return schemeOk && hostOk
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        try { binding.webView.saveState(outState) } catch (_: Throwable) {}
    }

    private fun clearWebViewAndCookies() {
        try {
            CookieManager.getInstance().apply {
                removeAllCookies(null)
                flush()
            }
        } catch (_: Throwable) {}
        try { WebStorage.getInstance().deleteAllData() } catch (_: Throwable) {}
        binding.webView.apply {
            try {
                stopLoading()
                clearCache(true)
                clearHistory()
            } catch (_: Throwable) {}
            visibility = View.GONE
        }
        hideLoading()
        lastLoadedLoginUrl = null
    }

    override fun onDestroyView() {
        clearWebViewAndCookies()
        try {
            binding.webView.apply {
                try { removeAllViews() } catch (_: Throwable) {}
                destroy()
            }
        } catch (_: Throwable) {}
        viewModel.clearUiState()
        super.onDestroyView()
    }

    /** 구글 로그인 403(disallowed_useragent) 회피: Custom Tab */
    private fun openCustomTab(url: String) {
        runCatching {
            val intent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
            intent.launchUrl(requireContext(), Uri.parse(url))
        }.onFailure {
            Toast.makeText(requireContext(), "브라우저를 열 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}
