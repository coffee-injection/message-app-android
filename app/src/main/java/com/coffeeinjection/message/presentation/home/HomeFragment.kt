package com.coffeeinjection.message.presentation.home

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.coffeeinjection.message.presentation.BaseFragment
import com.coffeeinjection.message.presentation.activity.SharedViewModel
import com.coffeeinjection.message.presentation.dialog.PermissionDialogFragment
import com.coffeeinjection.message.util.Logger
import com.coffeeinjection.presentation.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.random.Random

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
                        binding.tvCurrentState.text = buildCurrentStateText(messages.size)
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
            if (width <= 0 || height <= 0) return@post

            val iconSize = dpToPx(60)

            val maxX = (width - iconSize).coerceAtLeast(0)
            val maxY = (height - iconSize).coerceAtLeast(0)

            // 병들끼리 "조금 떨어져 보이게" 할 간격(원치 않으면 0으로)
            val spacing = dpToPx(6)

            // 이미 배치된 병들의 영역(간격 포함)을 저장
            val placedRects = mutableListOf<RectF>()

            // 랜덤 배치 시도 횟수: 메시지 개수가 많아지면 못 찾을 수도 있으니 적당히 제한
            val maxTriesPerIcon = 60

            fun findNonOverlappingPosition(): Pair<Int, Int>? {
                if (maxX == 0 && maxY == 0) return 0 to 0

                repeat(maxTriesPerIcon) {
                    val x = if (maxX == 0) 0 else Random.nextInt(0, maxX + 1)
                    val y = if (maxY == 0) 0 else Random.nextInt(0, maxY + 1)

                    // spacing까지 포함한 후보 영역 (간격을 유지하려고 조금 크게 잡음)
                    val candidate = RectF(
                        (x - spacing).toFloat(),
                        (y - spacing).toFloat(),
                        (x + iconSize + spacing).toFloat(),
                        (y + iconSize + spacing).toFloat()
                    )

                    val overlapped = placedRects.any { RectF.intersects(it, candidate) }
                    if (!overlapped) {
                        placedRects.add(candidate)
                        return x to y
                    }
                }
                return null // 너무 빽빽해서 자리 못 찾음
            }

            messages.forEach { msg ->
                val pos = findNonOverlappingPosition()

                // 공간이 부족하면: (1) 더 이상 추가 안 함 or (2) 겹쳐도 추가
                // 여기선 "더 이상 추가 안 함"으로 처리했습니다.
                if (pos == null) {
                    Logger.d("[sea] no space to place more icons. messages=${messages.size}, placed=${placedRects.size}")
                    return@forEach
                }

                val (x, y) = pos

                val lp = ConstraintLayout.LayoutParams(iconSize, iconSize).apply {
                    startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                    topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                    marginStart = x
                    topMargin = y
                }

                val iv = ImageView(requireContext()).apply {
                    setImageResource(R.drawable.ic_bottle)
                    layoutParams = lp

                    // 애니메이션을 위해 translation은 0으로 두는 게 포인트
                    translationX = 0f
                    translationY = 0f

                    setOnClickListener {
                        val action =
                            HomeFragmentDirections.actionHomeFragmentToMessageDialogFragment(
                                letterId = msg.letterId,
                                readOnly = true,
                                entry = "home"
                            )
                        findNavController().navigate(action)
                    }
                }

                // 애니메이션 적용
                val distance = (6..12).random().toFloat()      // dp
                val duration = (900L..1600L).random()          // ms
                val delay = (0L..600L).random()                // ms

                container.addView(iv)
                iv.startFloatUpDown(distanceDp = distance, duration = duration, startDelay = delay)
            }
        }
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

    private var floatAnimIsland: ObjectAnimator? = null

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
        floatAnimIsland = layoutIsland.startFloatUpDown(distanceDp = 6f, duration = 1100L)
    }

    override fun onStop() {
        floatAnimIsland?.cancel()
        floatAnimIsland = null
        binding.layoutIsland.translationY = 0f
        super.onStop()
    }

    override fun onDestroyView() {
        val container = binding.bgSeaLayer
        for (i in 0 until container.childCount) {
            container.getChildAt(i).stopFloatUpDown()
        }
        container.removeAllViews()
        super.onDestroyView()
    }

    private fun View.startFloatUpDown(distanceDp: Float = 8f, duration: Long = 1200L, startDelay: Long = 0L): ObjectAnimator {
        // 중복으로 계속 start 되는 것 방지(선택)
        (getTag(R.id.tag_float_anim) as? ObjectAnimator)?.cancel()

        val distancePx = distanceDp * resources.displayMetrics.density
        return ObjectAnimator.ofFloat(this, View.TRANSLATION_Y, 0f, -distancePx).apply {
            this.duration = duration
            this.startDelay = startDelay
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun View.stopFloatUpDown() {
        (getTag(R.id.tag_float_anim) as? ObjectAnimator)?.cancel()
        setTag(R.id.tag_float_anim, null)
    }

    private fun todayKoreanMd(): String {
        val cal = Calendar.getInstance()
        val m = cal.get(Calendar.MONTH) + 1
        val d = cal.get(Calendar.DAY_OF_MONTH)
        return "${m}월 ${d}일"
    }

    private fun buildCurrentStateText(messageCount: Int): String {
        return "${todayKoreanMd()} \u2022 받은 메시지 ${messageCount}개"
    }

}