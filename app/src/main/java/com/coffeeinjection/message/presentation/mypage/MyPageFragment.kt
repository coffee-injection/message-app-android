package com.coffeeinjection.presentation.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.coffeeinjection.message.R
import com.coffeeinjection.message.databinding.FragmentHomeBinding
import com.coffeeinjection.message.databinding.FragmentMypageBinding
import com.coffeeinjection.message.util.Logger
import com.coffeeinjection.presentation.BaseFragment
import com.coffeeinjection.presentation.home.HomeFragmentDirections

/**
 * 마이페이지 화면
 */
class MyPageFragment : BaseFragment<FragmentMypageBinding>(FragmentMypageBinding::inflate) {

    override fun setupViews() = with(binding) {

    }

    override fun setupListeners()= with(binding) {
        super.setupListeners()

        // 뒤로가기
        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

}