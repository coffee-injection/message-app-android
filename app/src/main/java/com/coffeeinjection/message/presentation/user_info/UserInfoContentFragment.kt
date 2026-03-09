package com.coffeeinjection.message.presentation.user_info

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.coffeeinjection.message.R
import com.coffeeinjection.message.data.local.UserInfo
import com.coffeeinjection.message.databinding.FragmentUserInfoContentBinding
import com.coffeeinjection.message.presentation.BaseFragment
import com.coffeeinjection.message.presentation.activity.SharedViewModel
import com.coffeeinjection.message.util.Logger
import com.coffeeinjection.message.util.UserInfoModeEnum
import com.coffeeinjection.presentation.sign_in.SignInFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.getValue
import kotlin.ranges.contains

/**
 * 회원가입 이후 프로필 설정화면
 */
@AndroidEntryPoint
class UserInfoContentFragment : BaseFragment<FragmentUserInfoContentBinding>(
    FragmentUserInfoContentBinding::inflate) {

    companion object {
        const val REQ_KEY = "user_info_req"
        const val RES_KEY = "user_info_res"

        private const val ARG_MODE = "arg_mode"

        fun newInstance(mode: UserInfoModeEnum) = UserInfoContentFragment().apply {
            arguments = bundleOf(ARG_MODE to mode.name)
        }
    }

    private val mode: UserInfoModeEnum by lazy {
        val value = requireArguments().getString(ARG_MODE, UserInfoModeEnum.SIGNUP.name)
        UserInfoModeEnum.valueOf(value)
    }

    private val viewModel: UserInfoViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()

    // 이모지 카드 리스트 (cardView, 프리뷰에 넣을 drawableRes)
    private val emojiCards: List<Pair<CardView, Int>> by lazy {
        listOf(
            binding.layoutParentEmoji1 to R.drawable.ic_profile1,
            binding.layoutParentEmoji2 to R.drawable.ic_profile2,
            binding.layoutParentEmoji3 to R.drawable.ic_profile3,
            binding.layoutParentEmoji4 to R.drawable.ic_profile4,
            binding.layoutParentEmoji5 to R.drawable.ic_profile5,
            binding.layoutParentEmoji6 to R.drawable.ic_profile6,
            binding.layoutParentEmoji7 to R.drawable.ic_profile7,
            binding.layoutParentEmoji8 to R.drawable.ic_profile8,
            binding.layoutParentEmoji9 to R.drawable.ic_profile9,
            binding.layoutParentEmoji10 to R.drawable.ic_profile10,
            binding.layoutParentEmoji11 to R.drawable.ic_profile11,
            binding.layoutParentEmoji12 to R.drawable.ic_profile12
        )
    }

    private val emojiBgLayouts: List<ConstraintLayout> by lazy {
        listOf(
            binding.layoutEmojiBg1,
            binding.layoutEmojiBg2,
            binding.layoutEmojiBg3,
            binding.layoutEmojiBg4,
            binding.layoutEmojiBg5,
            binding.layoutEmojiBg6,
            binding.layoutEmojiBg7,
            binding.layoutEmojiBg8,
            binding.layoutEmojiBg9,
            binding.layoutEmojiBg10,
            binding.layoutEmojiBg11,
            binding.layoutEmojiBg12
        )
    }

    override fun setupViews(savedInstanceState: Bundle?) = with(binding){
        // Title bar 기본 세팅
        titleBar.setupDefault(getString(R.string.title_user_information))

        btnStart.isEnabled = false
        when (mode) {
            UserInfoModeEnum.SIGNUP -> {
                root.setBackgroundResource(R.drawable.bg_second_gradient)
                // 초기 상태 세팅(디폴트 선택)
                setupDefaultState()

                // 1) 자식(layout_emoji_bg1~12) 배경 selector 적용
                emojiBgLayouts.forEach { bg ->
                    bg.setBackgroundResource(R.drawable.emoji_bg_selector)
                }
            }

            UserInfoModeEnum.MODIFY -> {
                root.setBackgroundResource(R.color.color_transparent)
                btnCancel.visibility = View.VISIBLE
                titleBar.visibility = View.GONE
                // 카드뷰 리스트로 배경 적용
                emojiCards.forEach { (card, _) ->
                    card.setBackgroundResource(R.drawable.emoji_bg_selector_grey)
                }

                ivIsland.visibility = View.GONE
                tvTitle.visibility = View.GONE
                tvSub.visibility = View.GONE
                btnStart.setText(R.string.user_modify)
                sharedViewModel.userInfoUiState.value.apply {
                    etUserName.setText(nickName)
                    tvPreviewUserName.text = nickName
                    selectDivision(islandName.last() == '도')
                    etIslandName.setText(islandName.dropLast(1))
                    tvPreviewIslandName.text = islandName.dropLast(1)
                    selectEmoji(emojiCards[profileImageIndex-1].first, emojiCards[profileImageIndex-1].second)
                }

            }
        }
        return@with
    }

    override fun setupListeners() = with(binding) {
        super.setupListeners()

        btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }

        // 1) 이모지: 무조건 하나만 선택(다중선택 X) + 2) 선택된 이미지 -> iv_preview_img 반영
        emojiCards.forEach { (card, imageRes) ->
            card.setOnClickListener {
                selectEmoji(card, imageRes)
                updatePreviewVisibility()
            }
        }

        // 2) 섬 이름 입력 -> 미리보기 텍스트 반영 + visible/gone 갱신
        etIslandName.doAfterTextChanged { editable ->
            tvPreviewIslandName.text = editable?.toString().orEmpty()
            updatePreviewVisibility()
        }

        // 3) 유저 이름 입력 -> 미리보기 텍스트 반영 + visible/gone 갱신
        etUserName.doAfterTextChanged { editable ->
            viewModel.updateNickNameGuideUiState(NickNameGuideState.IDLE)
            viewModel.updateIsChecked(false)
            tvPreviewUserName.text = editable?.toString().orEmpty()
            if (mode == UserInfoModeEnum.MODIFY) { viewModel.updateDuplicateEnable((editable.toString() != sharedViewModel.userInfoUiState.value.nickName) && (editable?.length in 2..10)) }
            else viewModel.updateDuplicateEnable((editable?.length in 2..10))
            updatePreviewVisibility()
        }

        // 4) 도/섬 버튼: 라디오(둘 중 하나는 무조건 선택)
        btnIsland1.setOnClickListener { selectDivision(isDo = true); updatePreviewVisibility()}
        btnIsland2.setOnClickListener { selectDivision(isDo = false); updatePreviewVisibility() }

        btnCheckDuplicate.setOnClickListener {
            if(!btnCheckDuplicate.isEnabled) return@setOnClickListener
            viewModel.checkNickNameAvailable(nickname = etUserName.text.toString())
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }

        btnStart.setOnClickListener {
            when {
                etUserName.text?.length !in 2..10 -> {
                    Toast.makeText(requireContext(), R.string.user_toast_nick_name, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                etIslandName.text?.length !in 1..8 -> {
                    Toast.makeText(requireContext(), R.string.user_toast_island_name, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            when (mode) {
                UserInfoModeEnum.SIGNUP -> {
                    if(!viewModel.isChecked()) {
                        viewModel.updateNickNameGuideUiState(NickNameGuideState.NEED_DUPLICATE_CHECK)
                        return@setOnClickListener
                    }
                }
                UserInfoModeEnum.MODIFY -> {
                    if(etUserName.text.toString() != sharedViewModel.userInfoUiState.value.nickName && !viewModel.isChecked()){
                        viewModel.updateNickNameGuideUiState(NickNameGuideState.NEED_DUPLICATE_CHECK)
                        return@setOnClickListener
                    }
                }
            }

            val userInfo = UserInfo(
                islandName = etIslandName.text.toString() + checkIslandDivision(),
                nickName = etUserName.text.toString(),
                profileImageIndex = checkProfileSelected()
            )

            when (mode) {
                UserInfoModeEnum.SIGNUP -> viewModel.completeSignup(userInfo)
                UserInfoModeEnum.MODIFY -> viewModel.modifyUserInfo(userInfo)
            }
        }
    }

    private fun applyNickNameGuide(msgId : Int = 0, isInitialize : Boolean) = with(binding){
        // todo : 사용 할 수 있는 닉네임 입니다.
        // todo : 중복된 닉네임 이에요.
        // todo 그냥 State로 빼기.
        // 중복체크를 진행해주세요.
        // 닉네임은 2~10자로 작성해주세요
        if (isInitialize) {
            tvGuideNickname.text = getString(msgId)
            tvGuideNickname.setTextColor(resources.getColor(R.color.error_secondary))
            etIslandName.setBackgroundResource(R.drawable.et_bg_selector)
        } else {
            tvGuideNickname.text = getString(msgId)
            tvGuideNickname.setTextColor(resources.getColor(R.color.error_secondary))
            etIslandName.setBackgroundResource(R.drawable.btn_normal_round_white_red_border)
        }
    }

    override fun setupCollectors() {
        super.setupCollectors()
        with(binding) {
            // 상태 관찰
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.uiState.collectLatest { state ->
                    Logger.d("[uistate check!!] : $state")
                    state.errorMessage?.let {
                        Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                        viewModel.clearError()
                    }
                    if (state.navigateToMain || state.closeModifyDialog) {
                        val userInfo = UserInfo(
                            islandName = etIslandName.text.toString() + checkIslandDivision(),
                            nickName = etUserName.text.toString(),
                            profileImageIndex = checkProfileSelected()
                        )
                        parentFragmentManager.setFragmentResult(
                            REQ_KEY,
                            bundleOf(RES_KEY to userInfo)
                        )
                    }
                }
            }
            viewModel.duplicateEnable.observe(viewLifecycleOwner) { isEnable ->
                btnCheckDuplicate.isEnabled = isEnable
            }
            viewModel.nickNameGuideUiState.observe(viewLifecycleOwner) { state ->
                when(state){
                    NickNameGuideState.IDLE -> {
                        tvGuideNickname.text = getString(R.string.user_toast_nick_name)
                        tvGuideNickname.setTextColor(resources.getColor(R.color.text_quaternary))
                        etUserName.setBackgroundResource(R.drawable.et_bg_selector)
                    }
                    NickNameGuideState.NEED_DUPLICATE_CHECK -> {
                        tvGuideNickname.text = getString(R.string.user_guide_plz_check_nick_name)
                        tvGuideNickname.setTextColor(resources.getColor(R.color.error_secondary))
                        etUserName.setBackgroundResource(R.drawable.btn_normal_round_white_red_border)
                    }
                    NickNameGuideState.AVAILABLE -> {
                        tvGuideNickname.text = getString(R.string.user_guide_nick_name_available)
                        tvGuideNickname.setTextColor(resources.getColor(R.color.blue_500))
                        etIslandName.setBackgroundResource(R.drawable.et_bg_selector)
                    }
                    NickNameGuideState.DUPLICATE -> {
                        tvGuideNickname.text = getString(R.string.user_guide_nick_name_is_duplicate)
                        tvGuideNickname.setTextColor(resources.getColor(R.color.error_secondary))
                        etUserName.setBackgroundResource(R.drawable.btn_normal_round_white_red_border)
                    }
                    NickNameGuideState.CHECK_FAILED -> {
                        tvGuideNickname.text = getString(R.string.user_guide_check_nick_name_fail)
                        tvGuideNickname.setTextColor(resources.getColor(R.color.error_secondary))
                        etUserName.setBackgroundResource(R.drawable.btn_normal_round_white_red_border)
                    }
                }
            }
        }
    }

    /**
     * Priview 숨김처리
     */
    private fun updatePreviewVisibility() = with(binding) {
        val hasIsland = etIslandName.text?.toString()?.trim().orEmpty().isNotEmpty()
        val hasUser = etUserName.text?.toString()?.trim().orEmpty().isNotEmpty() && etUserName.length() >= 2

        // preview만 숨김/표시
        (hasIsland && hasUser).let {
            layoutPreview.isVisible = it
            btnStart.isEnabled = it
        }
    }

    /**
     * UI 디폴트 상태
     * - 이모지 1번 선택
     * - btn_island1(도) 선택
     * - 입력값이 이미 있으면 프리뷰 동기화
     */
    private fun setupDefaultState() = with(binding) {
        // 이모지 기본 = 1번
        selectEmoji(layoutParentEmoji1, R.drawable.ic_profile1)

        // 도/섬 기본 = 도(btn_island1)
        selectDivision(isDo = true)

        // 프리뷰 초기 동기화
        tvPreviewIslandName.text = etIslandName.text?.toString().orEmpty()
        tvPreviewUserName.text = etUserName.text?.toString().orEmpty()
    }

    /**
     * 이모지 라디오 선택(항상 1개만 선택)
     */
    private fun selectEmoji(selectedCard: CardView, imageRes: Int) = with(binding) {
        emojiCards.forEach { (card, _) ->
            card.isSelected = (card == selectedCard)
        }
        ivPreviewImg.setImageResource(imageRes)
    }

    /**
     * 도/섬 라디오 선택(항상 1개는 선택)
     */
    private fun selectDivision(isDo: Boolean) = with(binding) {
        // 이미 선택된 버튼을 다시 눌러도 해제되지 않게 “그대로 유지”
        btnIsland1.isSelected = isDo
        btnIsland2.isSelected = !isDo

        // 프리뷰 텍스트 즉시 반영
        tvPreviewIslandDivision.text = getString(
            if (isDo) R.string.user_island1 else R.string.user_island2
        )
    }

    // 현재 선택된 profile index 가져 오섬
    private fun checkProfileSelected() = emojiCards.indexOfFirst { it.first.isSelected } + 1
    private fun checkIslandDivision() = getString(if (binding.btnIsland1.isSelected) R.string.user_island1 else R.string.user_island2)
}