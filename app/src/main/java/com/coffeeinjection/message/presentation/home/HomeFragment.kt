package com.coffeeinjection.message.presentation.home

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
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
import com.coffeeinjection.message.presentation.dialog.PermissionDialogFragment
import com.coffeeinjection.presentation.home.HomeViewModel
import com.google.android.material.internal.ViewUtils.dpToPx
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.Calendar

/**
 * 홈 화면
 */
@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    private val homeViewModel: HomeViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private val SEA_TOP_RATIO = 0.45f
    private val SEA_BOTTOM_RATIO = 1.0f
    private var backPressedTime: Long = 0L
    private val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (System.currentTimeMillis() - backPressedTime <= 2000) {
                requireActivity().finish()
            } else {
                backPressedTime = System.currentTimeMillis()
                Toast.makeText(requireContext(), "한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun setupViews(savedInstanceState: Bundle?) {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, // viewLifecycleOwner로 걸면 onDestroyView 때 자동 해제
            backCallback
        )
        // 받은 메세지 불러오기
        homeViewModel.loadReceivedMessages()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                showPermissionDialog()
            }
        }
    }

    private fun showPermissionDialog() {
        val tag = "PermissionDialog"
        if (parentFragmentManager.findFragmentByTag(tag) != null) return
        PermissionDialogFragment().show(parentFragmentManager, tag)
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
                readOnly = false,   // 쓰기 모드
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
                    SeaZone.SHALLOW -> R.drawable.ic_bottle
                    SeaZone.MIDDLE  -> R.drawable.ic_bottle
                    SeaZone.DEEP    -> R.drawable.ic_bottle
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
                                letterId = msg.letterId,
                                readOnly = true
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

    fun getDayTimeType(): DayTimeType {
        val cal = Calendar.getInstance()
        val minutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)

        return when (minutes) {
            in 240..479  -> DayTimeType.DAWN    // 04:00~07:59
            in 480..659  -> DayTimeType.MORNING // 08:00~10:59
            in 660..959  -> DayTimeType.DAYTIME // 11:00~15:59
            in 960..1139 -> DayTimeType.SUNSET  // 16:00~18:59
            else -> DayTimeType.NIGHT                 // 19:00~03:59
        }
    }

    private var floatAnim: ObjectAnimator? = null

    override fun onStart() = with(binding) {
        super.onStart()
        when (getDayTimeType()) {
            DayTimeType.DAWN -> {
                ivBackground.setImageResource(R.drawable.bg_dawn)
                ivIsland.setImageResource(R.drawable.ic_island_dawn)
            }
            DayTimeType.MORNING -> {
                ivBackground.setImageResource(R.drawable.bg_daytime)
                ivIsland.setImageResource(R.drawable.ic_island_daytime)
            }
            DayTimeType.DAYTIME -> {
                ivBackground.setImageResource(R.drawable.bg_daytime)
                ivIsland.setImageResource(R.drawable.ic_island_daytime)
            }
            DayTimeType.SUNSET -> {
                ivBackground.setImageResource(R.drawable.bg_sunset)
                ivIsland.setImageResource(R.drawable.ic_island_sunset)
            }
            DayTimeType.NIGHT -> {
                ivBackground.setImageResource(R.drawable.bg_night)
                ivIsland.setImageResource(R.drawable.ic_island_night)
            }
        }
        floatAnim = layoutIsland.startFloatUpDown(distanceDp = 6f, duration = 1100L)
    }

    override fun onStop() {
        floatAnim?.cancel()
        floatAnim = null
        binding.layoutIsland.translationY = 0f
        super.onStop()
    }

    private fun View.startFloatUpDown(distanceDp: Float = 8f, duration: Long = 1200L): ObjectAnimator {
        val distancePx = distanceDp * resources.displayMetrics.density

        return ObjectAnimator.ofFloat(this, View.TRANSLATION_Y, 0f, -distancePx).apply {
            this.duration = duration
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }


}