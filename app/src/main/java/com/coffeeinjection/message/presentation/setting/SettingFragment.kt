package com.coffeeinjection.message.presentation.setting

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.coffeeinjection.message.R
import com.coffeeinjection.message.databinding.FragmentSettingsBinding
import com.coffeeinjection.message.presentation.BaseFragment

class SettingFragment : BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {

    private val viewModel: SettingsViewModel by viewModels()

    private val args: SettingFragmentArgs by navArgs()

    override fun setupViews(savedInstanceState: Bundle?) {
        binding.apply {
            // docType: "TERMS" or "PRIVACY"
            val docType = args.docType

            // 타이틀 설정
            if (docType == "PRIVACY") {
                titleBar.setupDefault(getString(R.string.title_privacy_policy))
            } else {
                titleBar.setupDefault(getString(R.string.title_terms))
            }

            // 내용 뷰 교체
            contentContainer.removeAllViews()
            val layoutId = if (docType == "PRIVACY") {
                R.layout.view_privacy_policy
            } else {
                R.layout.view_terms
            }
            layoutInflater.inflate(layoutId, contentContainer, true)
        }
    }

    override fun setupListeners() = with(binding) {
        super.setupListeners()
        // 필요 시 리스너 추가
    }
}
