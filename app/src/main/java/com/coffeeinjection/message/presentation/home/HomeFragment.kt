package com.coffeeinjection.message.presentation.home

import android.net.Uri
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.coffeeinjection.message.R
import com.coffeeinjection.message.databinding.FragmentHomeBinding
import com.coffeeinjection.message.presentation.activity.SharedViewModel
import com.coffeeinjection.message.presentation.BaseFragment
import com.coffeeinjection.presentation.home.HomeViewModel
import com.google.android.material.internal.ViewUtils.dpToPx
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * 홈 화면
 */
@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    private val homeViewModel: HomeViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private val SEA_TOP_RATIO = 0.45f
    private val SEA_BOTTOM_RATIO = 1.0f

    override fun setupViews(savedInstanceState: Bundle?) {
        // 받은 메세지 불러오기
        homeViewModel.loadReceivedMessages()
    }

    override fun setupListeners() = with(binding) {
        super.setupListeners()

        // MyPage 화면이동
        cvProfileImg.setOnClickListener {
            this@HomeFragment.findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToMyPageFragment()
            )
        }

        // MessageDialogFragment
        btnSendMsg.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToMessageDialogFragment(
                letterId = -1L,
                content = "",
                senderName = "",
                readOnly = false,   // 쓰기 모드
                receiverNickname = "도마도"
            )
            findNavController().navigate(action)
        }

    }

    override fun setupCollectors() {
        super.setupCollectors()
        // viewLifecycleOwner 기준으로 collect
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // 프로필 이미지
                launch {
                    sharedViewModel.profileUri.collect { uri ->
                        uri?.let { loadIntoProfile(it) }
                    }
                }

                // 바다 위 메시지 아이콘
                launch {
                    homeViewModel.seaMessages.collect { messages ->
                        renderMessageIcons(messages)
                    }
                }
            }
        }
    }

    /**
     * 프로필 사진 불러오기
     */
    private fun loadIntoProfile(uri: Uri) = with(binding) {
        Glide.with(root)
            .load(uri)
            .centerCrop()
            .placeholder(R.drawable.ic_profile_placeholder) // 선택
            .error(R.drawable.ic_profile_placeholder)       // 선택
            .into(ivProfileImg)
    }

    /**
     * 바다 위 떠 있는 메시지 아이콘 그리기
     */
    private fun renderMessageIcons(messages: List<SeaMessageUiModel>) {
        val container = binding.bgSeaLayer
        container.removeAllViews()

        if (messages.isEmpty()) return

        container.post {
            val width = container.width
            val height = container.height
            if (width == 0 || height == 0) return@post

            val iconSize = dpToPx(56) // 56dp → px

            messages.forEach { msg ->
                val iconRes = when (msg.zone) {
                    SeaZone.SHALLOW -> R.drawable.ic_bottle_sea_shallow
                    SeaZone.MIDDLE  -> R.drawable.ic_bottle_sea_middle
                    SeaZone.DEEP    -> R.drawable.ic_bottle_sea_middle
                }

                val layoutParams = FrameLayout.LayoutParams(iconSize, iconSize)

                // X는 전체 바다 레이어 범위에서 랜덤
                val xRange = 0..(width - iconSize).coerceAtLeast(0)

                // Y는 존별로 범위 나누기
                val yRange = zoneYRange(height, msg.zone, iconSize)

                val x = xRange.random() // Kotlin Random 사용
                val y = yRange.random()

                val iv = ImageView(requireContext()).apply {
                    setImageResource(iconRes)
                    this.layoutParams = layoutParams
                    translationX = x.toFloat()
                    translationY = y.toFloat()

                    setOnClickListener {
                        val action =
                            HomeFragmentDirections.actionHomeFragmentToMessageDialogFragment(
                                letterId   = msg.letterId,
                                content    = msg.content,
                                senderName = msg.senderName,
                                readOnly   = true   , // 읽기 모드
                                receiverNickname = "도마도"
                            )
                        findNavController().navigate(action)
                    }
                }


                container.addView(iv)
            }
        }
    }

    private fun zoneYRange(height: Int, zone: SeaZone, iconSize: Int): IntRange {
        val seaTop = (height * SEA_TOP_RATIO).toInt()
        val seaBottom = (height * SEA_BOTTOM_RATIO).toInt()

        val seaHeight = seaBottom - seaTop
        val bandHeight = seaHeight / 3

        val (startY, endY) = when (zone) {
            SeaZone.SHALLOW -> seaTop to (seaTop + bandHeight)
            SeaZone.MIDDLE  -> (seaTop + bandHeight) to (seaTop + bandHeight * 2)
            SeaZone.DEEP    -> (seaTop + bandHeight * 2) to seaBottom
        }

        return (startY..(endY - iconSize).coerceAtLeast(startY))
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }



}