package com.coffeeinjection.presentation.sign_in

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.coffeeinjection.message.databinding.FragmentSignInBinding
import com.coffeeinjection.message.presentation.BaseFragment
import com.coffeeinjection.message.util.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.getValue

/**
 * - 서버에서 받은 loginUrl을 WebView로 로드
 * - 콜백 URL에서 code를 추출하여 ViewModel로 전달
 */
@AndroidEntryPoint
class SignInFragment : BaseFragment<FragmentSignInBinding>(FragmentSignInBinding::inflate) {

    private val viewModel: SignInViewModel by viewModels()

    override fun setupViews(savedInstanceState: Bundle?) = with(binding) {
        // WebView 기본 설정
        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadsImagesAutomatically = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) safeBrowsingEnabled = true
        }

        CookieManager.getInstance().setAcceptCookie(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        }

        // 콜백 URL 가로채기
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return handleCallbackUrl(url)
            }
            override fun shouldOverrideUrlLoading(
                view: WebView?, request: WebResourceRequest?
            ): Boolean {
                return handleCallbackUrl(request?.url?.toString())
            }
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
            }
            private fun handleCallbackUrl(url: String?): Boolean {
                if (url.isNullOrBlank()) return false
                if (url.startsWith("http://localhost:8080/auth/kakao/callback")) {
                    val code = Uri.parse(url).getQueryParameter("code")
                    if (!code.isNullOrBlank()) {
                        viewModel.exchangeCode(code)
                    }
                    return true
                }
                return false
            }
        }
    }

    override fun setupListeners() = with(binding) {
        super.setupListeners()
        btnA.setOnClickListener {
            this@SignInFragment.findNavController().navigate(
                SignInFragmentDirections.actionSignInFragmentToHomeFragment()
            )
        }
        btnKakao.setOnClickListener {
            if (webView.url.isNullOrBlank()) {
                Logger.d("[카카오 로그인] Btn Click")
                viewModel.loadKakaoLoginUrl()
            }
        }
    }

    override fun setupCollectors() {
        super.setupCollectors()
        with(binding) {
            // 상태 관찰
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.uiState.collectLatest { state ->
                    state.errorMessage?.let {
                        viewModel.clearError()
                    }
                    if (state.loginUrl != null && webView.url != state.loginUrl) {
                        webView.visibility = View.VISIBLE
                        webView.loadUrl(state.loginUrl)
                    }
                    if (state.navigateToNickname) {
                        viewModel.consumedNavigation()
                        webView.visibility = View.GONE
                        findNavController().navigate(
                            //todo NickName 입력으로 이동
                            SignInFragmentDirections.actionSignInFragmentToAdditionalInfoFragment()
                        )
                    }
                    if (state.navigateToMain) {
                        webView.visibility = View.GONE
                        viewModel.consumedNavigation()
                        findNavController().navigate(
                            //todo Home으로 이동
                            SignInFragmentDirections.actionSignInFragmentToHomeFragment()
                        )
                    }
                }
            }
        }
    }

    // ✅ WebView 상태 저장
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        try {
            binding.webView.saveState(outState)
        } catch (_: Throwable) { /* 필요 시 로그 */ }
    }
}