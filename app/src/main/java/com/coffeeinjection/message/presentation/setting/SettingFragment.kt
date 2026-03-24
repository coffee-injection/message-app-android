// presentation/setting/SettingFragment.kt
package com.coffeeinjection.message.presentation.setting

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.coffeeinjection.message.R
import com.coffeeinjection.message.databinding.FragmentSettingsBinding
import com.coffeeinjection.message.presentation.BaseFragment
import com.coffeeinjection.message.presentation.activity.SplashActivity
import com.coffeeinjection.message.presentation.message.WarningDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.core.net.toUri

@AndroidEntryPoint
class SettingFragment : BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {

    private val viewModel: SettingsViewModel by viewModels()
    private val args: SettingFragmentArgs by navArgs()

    override fun setupViews(savedInstanceState: Bundle?) {
        binding.apply {
            val docType = args.docType

            // 타이틀
            if (docType == "PRIVACY") {
                titleBar.setupDefault(getString(R.string.title_privacy_policy))
            } else {
                titleBar.setupDefault(getString(R.string.title_terms))
            }

            // 내용 inflate
            contentContainer.removeAllViews()
            val layoutId = if (docType == "PRIVACY") {
                R.layout.view_privacy_policy
            } else {
                R.layout.view_terms
            }
            val contentView = layoutInflater.inflate(layoutId, contentContainer, false)
            contentContainer.addView(contentView)
            contentView.findViewById<ConstraintLayout?>(R.id.layout_contact_mail)?.setOnClickListener {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = "mailto:coffeeinjectionstudio@gmail.com".toUri()
                    putExtra(Intent.EXTRA_SUBJECT, "문의드립니다")
                    putExtra(Intent.EXTRA_TEXT, "")
                }

                runCatching {
                    startActivity(intent)
                }.onFailure {
                    Toast.makeText(requireContext(), "메일 앱을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            // PRIVACY 일 때만 회원탈퇴 버튼 동작 부여
            if (docType == "PRIVACY") {
                val btnWithdraw = contentContainer.findViewById<CardView?>(R.id.btn_withdraw)
                btnWithdraw?.setOnClickListener {
                    showWithdrawDialog()
                }
            }
        }
    }

    override fun setupCollectors() {
        super.setupCollectors()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.ui.collectLatest { s ->
                if (s.errorMessage != null) {
                    Toast.makeText(requireContext(), s.errorMessage, Toast.LENGTH_SHORT).show()
                    viewModel.clearMessage()
                }
                if (s.withdrawSuccess) {
                    Toast.makeText(requireContext(), "탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                    // 앱 재시작(스택 정리 후 스플래시로)
                    val intent = Intent(requireContext(), SplashActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    requireActivity().finish()
                }
            }
        }
    }

    private fun showWithdrawDialog() {
        val dialog = WarningDialogFragment.newInstance(
            title = getString(R.string.dialog_fragment_message_title6), // “정말 탈퇴하시겠습니까?” 로 바꾸면 좋음
            subTitle = getString(R.string.dialog_fragment_message_sub6), // 서브문구
            closeText = getString(R.string.dialog_fragment_message_withdraw), // “확인/탈퇴” 등의 텍스트로 교체
            closeButtonBgRes = R.drawable.btn_gradient_red,
            iconRes = R.drawable.ic_warning
        ).apply {
            onConfirmClose = { viewModel.executeWithdraw() }
        }
        dialog.show(childFragmentManager, "withdraw_dialog")
    }

    override fun setupListeners() = with(binding) {
        super.setupListeners()
        // 별도 리스너 필요 시 추가
    }
}
