package com.coffeeinjection.message.presentation.setting

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.coffeeinjection.message.databinding.FragmentSettingsBinding
import com.coffeeinjection.message.presentation.BaseFragment

class SettingFragment : BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {

    private val viewModel: SettingsViewModel by viewModels()
    override fun setupViews(savedInstanceState: Bundle?) {

    }

    override fun setupListeners() = with(binding) {
        super.setupListeners()

        // 뒤로가기
        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}