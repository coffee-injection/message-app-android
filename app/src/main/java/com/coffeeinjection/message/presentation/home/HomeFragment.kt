package com.coffeeinjection.message.presentation.home

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.coffeeinjection.message.databinding.FragmentHomeBinding
import com.coffeeinjection.message.presentation.activity.SharedViewModel
import com.coffeeinjection.message.presentation.BaseFragment
import com.coffeeinjection.presentation.home.HomeViewModel
import com.coffeeinjection.presentation.mypage.MyPageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * 홈 화면
 */
@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {
    private val homeViewModel: HomeViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun setupViews(savedInstanceState: Bundle?) {
        // 화면 진입할 때 메시지 API 전부 호출 해보기
        homeViewModel.testAllMessageApis()
    }

    override fun setupListeners() = with(binding) {
        super.setupListeners()

        // MyPage 화면이동
        cvProfileImg.setOnClickListener {
            this@HomeFragment.findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToMyPageFragment()
            )
        }

        // MessageDialogFeamgment  화면 이동
        btnSendMsg.setOnClickListener {
            this@HomeFragment.findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToMessageDialogFragment()
            )
        }
    }

    override fun setupCollectors() {
        super.setupCollectors()
        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.profileUri.collect { uri ->
                uri?.let {
                    loadIntoProfile(it)
                }
            }
        }
    }
    private fun loadIntoProfile(uri: Uri) = with(binding) {
        // Glide 수명 안전: fragment view의 lifecycle에 묶기
        Glide.with(root)
            .load(uri)
            .centerCrop()
            .placeholder(com.coffeeinjection.message.R.drawable.ic_profile_placeholder) // 선택
            .error(com.coffeeinjection.message.R.drawable.ic_profile_placeholder)       // 선택
            .into(ivProfileImg)
    }
}