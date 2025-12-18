//package com.coffeeinjection.message.presentation.mypage.fragment
//
//import android.net.Uri
//import android.os.Bundle
//import android.widget.Toast
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.fragment.app.activityViewModels
//import androidx.fragment.app.viewModels
//import androidx.lifecycle.lifecycleScope
//import androidx.lifecycle.repeatOnLifecycle
//import androidx.navigation.fragment.findNavController
//import com.bumptech.glide.Glide
//import com.coffeeinjection.message.BuildConfig
//import com.coffeeinjection.message.R
//import com.coffeeinjection.message.databinding.FragmentMypageBinding
//import com.coffeeinjection.message.databinding.FragmentUserInformationBinding
//import com.coffeeinjection.message.presentation.activity.SharedViewModel
//import com.coffeeinjection.message.presentation.BaseFragment
//import com.coffeeinjection.message.presentation.mypage.viewmodel.MyPageViewModel
//import kotlinx.coroutines.launch
//
///**
// * 마이페이지 화면
// */
//class UserInformationFragment : BaseFragment<FragmentUserInformationBinding>(FragmentUserInformationBinding::inflate) {
//
//    private val viewModel: MyPageViewModel by viewModels()
//
//    private val sharedViewModel: SharedViewModel by activityViewModels()
//
//    // 포토 피커 (이미지 전용). 구버전은 자동으로 기본 이미지 선택기로 폴백됨.
//    private val pickPhoto = registerForActivityResult(
//        ActivityResultContracts.PickVisualMedia()
//    ) { uri: Uri? ->
//        uri ?: return@registerForActivityResult
//        // 내 화면에 즉시 반영
//        loadIntoProfile(uri)
//        // 다른 프래그먼트와 공유
//        sharedViewModel.setPhoto(uri)
//    }
//
//    override fun setupViews(savedInstanceState: Bundle?) {
//
//        binding.apply {
//            // TitleBar
//            titleBar.setupDefault(getString(R.string.title_mypage))
//
//            // 현재 앱 버전 표시
//            val versionName = BuildConfig.VERSION_NAME
//            tvVersionNumber.text = getString(R.string.mypage_version, versionName)
//        }
//
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
//                // 화면이 보이는(STARTED) 상태만 블록 실행/수집 시작 STOPPED 로 내려가면 자동으로 수집을 중단
//                sharedViewModel.profileUri
//                    .collect { uri -> uri?.let { loadIntoProfile(it) } }
//            }
//        }
//    }
//
//    override fun setupListeners() = with(binding) {
//        super.setupListeners()
//
//        // 프로필 편집
//        layoutChildProfile.setOnClickListener {
//            this@UserInformationFragment.findNavController().navigate(
//                MyPageFragmentDirections.actionMyPageFragmentToBookmarkFragment()
//            )
//        }
//
//        // 북마크
//        layoutChildBookmarks.setOnClickListener {
//            this@UserInformationFragment.findNavController().navigate(
//                MyPageFragmentDirections.actionMyPageFragmentToBookmarkFragment()
//            )
//        }
//
//        // 알림 설정
//        layoutChildNotification.setOnClickListener {
//            this@UserInformationFragment.findNavController().navigate(
//                MyPageFragmentDirections.actionMyPageFragmentToSettingFragment()
//            )
//        }
//        // 개인정보 처리방침
//        layoutChildPrivacy.setOnClickListener {
//            this@UserInformationFragment.findNavController().navigate(
//                MyPageFragmentDirections.actionMyPageFragmentToSettingFragment()
//            )
//        }
//        // 이용약관
//        layoutChildTerms.setOnClickListener {
//            this@UserInformationFragment.findNavController().navigate(
//                MyPageFragmentDirections.actionMyPageFragmentToSettingFragment()
//            )
//        }
//
//        // 로그아웃
//        layoutLogout.setOnClickListener {
//            Toast.makeText(requireContext(), "정말 로그아웃 하시겠습니까?", Toast.LENGTH_SHORT).show()
//        }
//
////        // 프로필 사진
////        btnEditProfile.setOnClickListener {
////            pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
////        }
//    }
//
//    private fun loadIntoProfile(uri: Uri) = with(binding) {
//        // Glide 수명 안전: fragment view의 lifecycle에 묶기
//        Glide.with(root)
//            .load(uri)
//            .centerCrop()
//            .placeholder(R.drawable.ic_profile_placeholder) // 선택
//            .error(R.drawable.ic_profile_placeholder)       // 선택
//            .into(ivProfileImg)
//    }
//
//}