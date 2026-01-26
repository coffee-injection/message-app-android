package com.coffeeinjection.message.presentation.mypage.fragment

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.coffeeinjection.message.BuildConfig
import com.coffeeinjection.message.R
import com.coffeeinjection.message.databinding.FragmentMypageBinding
import com.coffeeinjection.message.presentation.BaseFragment
import com.coffeeinjection.message.presentation.activity.SharedViewModel
import com.coffeeinjection.message.presentation.message.WarningDialogFragment
import com.coffeeinjection.message.presentation.mypage.viewmodel.MyPageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 마이페이지 화면.
 * - 알림 스위치: 사용자가 ON/OFF 할 때마다 시스템 설정 화면(앱/채널/권한)으로 이동
 * - 설정 화면/권한 요청에서 복귀 시 상태를 자동으로 갱신하여 스위치에 반영
 * - FCM 등록/해제는 로그인 플로우에서만 처리 (이 화면에서는 관여하지 않음)
 */
@AndroidEntryPoint
class MyPageFragment : BaseFragment<FragmentMypageBinding>(FragmentMypageBinding::inflate) {

    private val viewModel: MyPageViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()

    /** 프로그램적 동기화로 인한 리스너 트리거 무시용 플래그 */
    private var suppressToggleCallback = false

    /** Android 13+ POST_NOTIFICATIONS 권한 요청 */
    private val requestPostNotifications = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        // 권한 응답 후 최신 상태 갱신
        viewModel.refreshState()
    }

    /** 앱/채널 알림 설정 화면 오픈 (복귀 시 상태 갱신) */
    private val openSettings = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.refreshState()
    }

    override fun setupViews(savedInstanceState: Bundle?) {
        binding.apply {
            titleBar.setupDefault(getString(R.string.title_mypage))
            tvVersionNumber.text = getString(R.string.mypage_version, BuildConfig.VERSION_NAME)
        }

        viewModel.load()

        // 상태 수집 → 스위치 강제 동기화(표시만)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.ui.collectLatest { s ->
                    val target = s.effectiveEnabled
                    if (binding.switchNotification.isChecked != target) {
                        suppressToggleCallback = true
                        binding.switchNotification.isChecked = target
                        suppressToggleCallback = false
                    }
                }
            }
        }

        // 로그아웃 이벤트 수집 → 로그인 화면으로 전환(백스택 정리)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.logoutEvent.collect {
                    val options = NavOptions.Builder()
                        .setPopUpTo(R.id.homeFragment, true) // 홈 포함 백스택 제거
                        .setLaunchSingleTop(true)
                        .build()
                    findNavController().navigate(R.id.signInFragment, null, options)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshState()
    }

    override fun setupListeners() = with(binding) {
        super.setupListeners()

        layoutChildProfile.setOnClickListener {
            findNavController().navigate(
                MyPageFragmentDirections.actionMyPageFragmentToModifyUserInfoFragment()
            )
        }
        layoutChildBookmarks.setOnClickListener {
            findNavController().navigate(
                MyPageFragmentDirections.actionMyPageFragmentToBookmarkFragment()
            )
        }
        layoutChildPrivacy.setOnClickListener {
            findNavController().navigate(
                MyPageFragmentDirections.actionMyPageFragmentToSettingFragment(docType = "PRIVACY")
            )
        }
        layoutChildTerms.setOnClickListener {
            findNavController().navigate(
                MyPageFragmentDirections.actionMyPageFragmentToSettingFragment(docType = "TERMS")
            )
        }

        // 로그아웃: 경고 다이얼로그로 확인 후 진행
        layoutLogout.setOnClickListener {
            showLogoutConfirm()
        }

        // 토글 클릭 시: 즉시 원상복구 + 적절한 설정 경로만 열기
        switchNotification.setOnCheckedChangeListener { _, userWantsEnable ->
            if (suppressToggleCallback) return@setOnCheckedChangeListener

            val s = viewModel.ui.value

            // 1) 토글 시각 상태는 즉시 실제 상태로 돌려놓음(사용자가 설정에서 바꾸게 유도)
            suppressToggleCallback = true
            switchNotification.isChecked = s.effectiveEnabled
            suppressToggleCallback = false

            // 2) 사용 의도 저장
            viewModel.setDesiredEnabled(userWantsEnable)

            // 3) 사용자 의도에 맞는 "단 하나"의 진입 지점으로 안내
            if (userWantsEnable) {
                // 켤 때 필요한 조건 충족 절차
                if (!s.appEnabled) {
                    openAppNotificationSettings()
                    return@setOnCheckedChangeListener
                }
                if (Build.VERSION.SDK_INT >= 33 && !s.permissionGranted) {
                    requestPostNotifications.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    return@setOnCheckedChangeListener
                }
                if (Build.VERSION.SDK_INT >= 26 && !s.channelEnabled) {
                    openChannelSettings("default_push")
                    return@setOnCheckedChangeListener
                }
                // 모두 충족 → 아무 것도 열지 않고 상태만 갱신
                viewModel.refreshState()
            } else {
                // 끌 때는 채널 단위(26+)가 명확, 미만은 앱 알림 설정
                if (Build.VERSION.SDK_INT >= 26) {
                    openChannelSettings("default_push")
                } else {
                    openAppNotificationSettings()
                }
            }
        }

        // 행 전체 클릭 시에도 스위치 클릭과 동일한 동작(설정화면으로만 유도)
        layoutChildNotification.setOnClickListener {
            switchNotification.performClick()
        }
    }

    /** 앱 알림 전체 설정 화면 */
    private fun openAppNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
            putExtra("app_package", requireContext().packageName) // 일부 기기 호환
            putExtra("app_uid", requireContext().applicationInfo.uid)
        }
        openSettings.launch(intent)
    }

    /** 특정 채널 설정 화면 (API 26+) */
    private fun openChannelSettings(channelId: String) {
        if (Build.VERSION.SDK_INT < 26) {
            openAppNotificationSettings()
            return
        }
        val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
            putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
        }
        openSettings.launch(intent)
    }

    /** 로그아웃 확인 다이얼로그 */
    private fun showLogoutConfirm() {
        val dialog = WarningDialogFragment.newInstance(
            title = getString(R.string.dialog_fragment_message_title5),
            subTitle = getString(R.string.dialog_fragment_message_sub5), // “자동로그인 정보가 삭제되고 푸시 토큰이 해제됩니다.”
            closeText = getString(R.string.dialog_fragment_warning_logout), // “로그아웃”
            closeButtonBgRes = R.drawable.btn_gradient_red,
            iconRes = R.drawable.ic_warning
        ).apply {
            onConfirmClose = { viewModel.logout() }
        }
        dialog.show(childFragmentManager, "logout_confirm_dialog")
    }
}
