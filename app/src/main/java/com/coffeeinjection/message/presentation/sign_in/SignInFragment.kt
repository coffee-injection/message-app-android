package com.coffeeinjection.presentation.sign_in

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.coffeeinjection.message.databinding.FragmentSignInBinding
import com.coffeeinjection.message.presentation.BaseFragment
import kotlin.getValue

class SignInFragment : BaseFragment<FragmentSignInBinding>(FragmentSignInBinding::inflate) {

    private val viewModel: SignInViewModel by viewModels()

    override fun setupViews() = with(binding) {

    }

    override fun setupListeners() = with(binding) {
        super.setupListeners()
        btnA.setOnClickListener {
            this@SignInFragment.findNavController().navigate(
                SignInFragmentDirections.actionSignInFragmentToHomeFragment()
            )
        }
    }
}