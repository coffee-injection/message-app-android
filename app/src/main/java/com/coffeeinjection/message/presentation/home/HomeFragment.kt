package com.coffeeinjection.message.presentation.home

import android.net.Uri
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.coffeeinjection.message.databinding.FragmentHomeBinding
import com.coffeeinjection.message.presentation.activity.SharedViewModel
import com.coffeeinjection.message.presentation.BaseFragment
import com.coffeeinjection.presentation.mypage.MyPageViewModel
import kotlinx.coroutines.launch

/**
 * 홈 화면
 */
class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {
    private val viewModel: MyPageViewModel by viewModels()

    private val sharedViewModel: SharedViewModel by activityViewModels()


    override fun setupViews() {
        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.profileUri.collect { uri ->
                uri?.let {
                    loadIntoProfile(it)
                }
            }
        }
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

    private fun loadIntoProfile(uri: Uri) = with(binding) {
        // Glide 수명 안전: fragment view의 lifecycle에 묶기
        Glide.with(root)
            .load(uri)
            .centerCrop()
//            .placeholder(com.coffeeinjection.message.R.drawable.ic_profile_placeholder) // 선택
//            .error(com.coffeeinjection.message.R.drawable.ic_profile_placeholder)       // 선택
            .into(ivProfileImg)
    }
}