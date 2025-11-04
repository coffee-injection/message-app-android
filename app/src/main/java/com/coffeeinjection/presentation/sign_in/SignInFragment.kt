package com.coffeeinjection.presentation.sign_in

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.coffeeinjection.message.databinding.FragmentBlankBinding
import com.coffeeinjection.message.databinding.FragmentBlankBinding.inflate
import com.coffeeinjection.message.databinding.FragmentSignInBinding
import com.coffeeinjection.message.util.Logger
import com.coffeeinjection.presentation.BaseFragment
import com.coffeeinjection.presentation.home.HomeFragmentDirections
import kotlin.getValue

class SignInFragment : BaseFragment<FragmentSignInBinding>(FragmentSignInBinding::inflate) {

    private val viewModel : SignInViewModel by viewModels()

    override fun setupViews() = with(binding) {
        btnA.setOnClickListener {
            this@SignInFragment.findNavController().navigate(
                SignInFragmentDirections.actionSignInFragmentToHomeFragment()
            )
        }
    }
}