package com.coffeeinjection.message.presentation.setting

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.coffeeinjection.message.BuildConfig
import com.coffeeinjection.message.R
import com.coffeeinjection.message.databinding.FragmentSettingsBinding
import com.coffeeinjection.message.presentation.BaseFragment

class SettingFragment : BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {

    private val viewModel: SettingsViewModel by viewModels()
    override fun setupViews(savedInstanceState: Bundle?) {
        binding.apply {
            // TitleBar
            titleBar.setupDefault(getString(R.string.title_settings))

        }
    }

    override fun setupListeners() = with(binding) {
        super.setupListeners()

    }
}