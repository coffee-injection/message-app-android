package com.coffeeinjection.message.presentation.mypage.fragment

import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.coffeeinjection.message.R
import com.coffeeinjection.message.databinding.FragmentUserInfoBinding
import com.coffeeinjection.message.presentation.BaseFragment
import com.coffeeinjection.message.presentation.activity.SharedViewModel
import com.coffeeinjection.message.presentation.mypage.viewmodel.MyPageViewModel
import kotlinx.coroutines.launch

/**
 * 프로필 설정 화면
 */
class UserInfoFragment : BaseFragment<FragmentUserInfoBinding>(FragmentUserInfoBinding::inflate) {

    private val viewModel: MyPageViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()

    // 포토 피커 (이미지 전용). 구버전은 자동으로 기본 이미지 선택기로 폴백됨.
    private val pickPhoto = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri ?: return@registerForActivityResult
        // 다른 프래그먼트와 공유
        sharedViewModel.setPhoto(uri)
    }

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

    override fun setupViews(savedInstanceState: Bundle?) {
        binding.apply {
            titleBar.setupDefault(getString(R.string.title_user_information))
        }

        // 초기 상태 세팅(디폴트 선택)
        setupDefaultState()

        // sharedViewModel 프로필 uri가 오면 프리뷰 이미지에 반영(원하실 때만 사용)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                sharedViewModel.profileUri.collect { uri ->
                    uri ?: return@collect
                    // 사진 선택이 들어오면 프리뷰를 사진으로 바꿈 (원치 않으면 이 블록 제거하세요)
                    binding.ivPreviewImg.setImageURI(uri)
                }
            }
        }
    }

    override fun setupListeners() = with(binding) {
        super.setupListeners()

        // 1) 이모지: 무조건 하나만 선택(다중선택 X) + 2) 선택된 이미지 -> iv_preview_img 반영
        emojiCards.forEach { (card, imageRes) ->
            card.setOnClickListener {
                selectEmoji(card, imageRes)
            }
        }

        // 2) 섬 이름 입력 -> 미리보기 텍스트 반영 + visible/gone 갱신
        etIslandName.doAfterTextChanged { editable ->
            tvPreviewIslandName.text = editable?.toString().orEmpty()
            updatePreviewVisibility()
        }

        // 3) 유저 이름 입력 -> 미리보기 텍스트 반영 + visible/gone 갱신
        evUserName.doAfterTextChanged { editable ->
            tvPreviewUserName.text = editable?.toString().orEmpty()
            updatePreviewVisibility()
        }

        // 5) ev_user_name 입력 -> tv_preview_user_name 즉시 반영
        evUserName.doAfterTextChanged {
            tvPreviewUserName.text = it?.toString().orEmpty()
        }

        // 4) 도/섬 버튼: 라디오(둘 중 하나는 무조건 선택)
        btnIsland1.setOnClickListener { selectDivision(isDo = true) }
        btnIsland2.setOnClickListener { selectDivision(isDo = false) }

        // (원하시면) 프로필 사진 선택 버튼 연결
        // btnEditProfile.setOnClickListener {
        //     pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        // }
    }

    /**
     * Priview 숨김처리
     */
    private fun updatePreviewVisibility() = with(binding) {
        val hasIsland = etIslandName.text?.toString()?.trim().orEmpty().isNotEmpty()
        val hasUser = evUserName.text?.toString()?.trim().orEmpty().isNotEmpty()

        // preview만 숨김/표시
        layoutPreview.isVisible = hasIsland && hasUser
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
        tvPreviewUserName.text = evUserName.text?.toString().orEmpty()
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

    private fun closeSelf() {
        if (parentFragment is DialogFragment) {
            (parentFragment as DialogFragment).dismiss()
        } else {
            findNavController().navigate(
                UserInfoFragmentDirections.actionUserInfoFragmentToHomeFragment()
            )
        }
    }

    private fun isOpenedFromBottomSheet(): Boolean {
        // todo 좀 더 좁혀서 바텀시트 인지를 확인하려면 : com.google.android.material.bottomsheet.BottomSheetDialogFragment
        return parentFragment is DialogFragment // check parent is dialog
    }
}
