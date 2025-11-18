package com.coffeeinjection.message.presentation.sign_in

import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.coffeeinjection.message.databinding.FragmentAdditionalInfoBinding
import com.coffeeinjection.message.presentation.BaseFragment
import com.coffeeinjection.presentation.sign_in.SignInViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * - 신규회원의 닉네임 입력 화면
 * - ViewModel의 tempToken이 있어야 정상 처리됨
 */
@AndroidEntryPoint
class AdditionalInfoFragment : BaseFragment<FragmentAdditionalInfoBinding>(
    FragmentAdditionalInfoBinding::inflate
) {

    private val viewModel: SignInViewModel by viewModels()

    override fun setupViews(savedInstanceState: Bundle?) = with(binding) {
        btnConfirm.isEnabled = false
    }

    override fun setupListeners() = with(binding) {
        super.setupListeners()
        etNickname.addTextChangedListener { text ->
            val len = (text?.length) ?: 0
            btnConfirm.isEnabled = len in 2..20
        }
        btnConfirm.setOnClickListener {
            val nickname = etNickname.text?.toString()?.trim().orEmpty()
            viewModel.completeSignup(nickname)
        }
    }

    override fun setupCollectors() {
        super.setupCollectors()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                state.errorMessage?.let {
                    // Toast 등으로 알림
                    // Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    viewModel.clearError()
                }

                if (state.navigateToMain) {
                    viewModel.consumedNavigation()
                    findNavController().navigate(
                        AdditionalInfoFragmentDirections.actionAdditionalInfoFragmentToHomeFragment()
                    )
                }
            }
        }
    }
}