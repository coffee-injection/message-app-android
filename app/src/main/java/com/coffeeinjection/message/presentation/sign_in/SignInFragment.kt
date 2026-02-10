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
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.coffeeinjection.message.BuildConfig
import com.coffeeinjection.message.R
import com.coffeeinjection.message.databinding.FragmentSignInBinding
import com.coffeeinjection.message.presentation.BaseFragment
import com.coffeeinjection.message.util.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignInFragment : BaseFragment<FragmentSignInBinding>(FragmentSignInBinding::inflate) {

    private val viewModel: SignInViewModel by viewModels()
    private var backPressedTime: Long = 0L
    private var lastLoadedLoginUrl: String? = null

    // 허용 도메인(루트 기준)
    private val allowedSuffixes = setOf(
        "kakao.com", "google.com", "gstatic.com", "15.164.112.136"
    )


    private val kakaoCallbackPrefix = BuildConfig.API_SEVER_BASE_URL + "auth/kakao/callback"
    private val googleCallbackPrefix = BuildConfig.API_SEVER_BASE_URL + "auth/google/callback"

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

        // 초기엔 보이지 않게 시작 (첫 픽셀 커밋될 때 노출)
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
                webView.visibility = View.GONE      // 잔상 방지
                showLoading()                       // 로딩 시작
            }

            // 실제 픽셀이 커밋되는 순간만 노출
            override fun onPageCommitVisible(view: WebView?, url: String?) {
                super.onPageCommitVisible(view, url)
                if (!url.isNullOrBlank() && url != "about:blank") {
                    webView.visibility = View.VISIBLE
                    hideLoading()
                }
            }

            private fun handleNavigation(url: String?): Boolean {
                if (url.isNullOrBlank()) return false

                // 콜백: 즉시 교환 + WebView 정리
                if (url.startsWith(kakaoCallbackPrefix)) {
                    Uri.parse(url).getQueryParameter("code")?.let {
                        clearWebView()
                        viewModel.exchangeKakaoCode(it)
                    }
                    return true
                }
                if (url.startsWith(googleCallbackPrefix)) {
                    Uri.parse(url).getQueryParameter("code")?.let {
                        clearWebView()
                        viewModel.exchangeGoogleCode(it)
                    }
                    return true
                }

                // 허용 도메인만 WebView 로드 허용
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
            clearWebView()              // 기존 화면/히스토리 가림
            lastLoadedLoginUrl = null   // 강제 재로딩 유도
            showLoading()
            viewModel.loadGoogleLoginUrl()
        }

        btnKakao.setStartIcon(context?.let { ContextCompat.getDrawable(it, R.drawable.ic_kakao) }, 30f)
        btnKakao.setOnClickListener {
            Logger.d("[카카오 로그인] Btn Click")
            clearWebView()
            lastLoadedLoginUrl = null
            showLoading()
            viewModel.loadKakaoLoginUrl()
        }
    }

    override fun setupCollectors() {
        super.setupCollectors()
        with(binding) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.uiState.collectLatest { state ->
                    Logger.d("[uistate check!!] : $state")

                    state.errorMessage?.let {
                        Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                        viewModel.clearError()
                    }

                    // URL이 들어오면 중복 로드 방지 후 로드
                    val nextUrl = state.loginUrl
                    if (!nextUrl.isNullOrBlank() && lastLoadedLoginUrl != nextUrl) {
                        webView.loadUrl(nextUrl)   // 가시성은 WebViewClient가 제어
                        lastLoadedLoginUrl = nextUrl
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

    /** 화면/스토리지/쿠키 정리(뒤로가기 시 사용) */
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
}
