package com.coffeeinjection.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.coffeeinjection.message.databinding.FragmentHomeBinding
import com.coffeeinjection.message.databinding.FragmentSignInBinding
import com.coffeeinjection.message.util.Logger
import com.coffeeinjection.presentation.BaseFragment

/**
 * 홈 화면
 */
class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    override fun setupViews() = with(binding) {
    }

    override fun setupListeners()= with(binding) {
        super.setupListeners()

        // MyPage화면이동
        cvProfileImg.setOnClickListener {
            this@HomeFragment.findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToMyPageFragment()
            )
        }
    }
}