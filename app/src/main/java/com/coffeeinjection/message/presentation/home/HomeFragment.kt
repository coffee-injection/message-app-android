package com.coffeeinjection.message.presentation.home

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import com.coffeeinjection.message.data.model.PushBus
import com.coffeeinjection.message.databinding.FragmentHomeBinding
import com.coffeeinjection.message.presentation.BaseFragment
import com.coffeeinjection.message.presentation.activity.SharedViewModel
import com.coffeeinjection.message.presentation.dialog.PermissionDialogFragment
import com.coffeeinjection.message.util.Logger
import com.coffeeinjection.presentation.home.HomeViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
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

    // 최신 렌더만 반영하기 위한 시퀀스
    private var renderSeq: Int = 0

    private val profileImages = intArrayOf(
        R.drawable.ic_profile1, R.drawable.ic_profile2, R.drawable.ic_profile3, R.drawable.ic_profile4,
        R.drawable.ic_profile5, R.drawable.ic_profile6, R.drawable.ic_profile7, R.drawable.ic_profile8,
        R.drawable.ic_profile9, R.drawable.ic_profile10, R.drawable.ic_profile11, R.drawable.ic_profile12
    )

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
        sharedViewModel.clearLogoutEventState()
        // FCM TOKEN
        getFCMToken()

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            backCallback
        )

        // 받은 메세지 불러오기
        homeViewModel.loadReceivedMessages()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
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

        ivMenu.setOnClickListener {
            this@HomeFragment.findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToMyPageFragment()
            )
        }

        // MessageDialogFragment
        btnSendMsg.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToMessageDialogFragment(
                letterId = -1L,
                readOnly = false,
            )
            findNavController().navigate(action)
        }
    }

    override fun setupCollectors() {
        super.setupCollectors()
        // LiveData observe는 바깥에서 한 번만
        PushBus.message.observe(viewLifecycleOwner) { event ->
            event ?: return@observe

            // 화면 새로고침
             homeViewModel.loadReceivedMessages()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 바다 위 메시지 아이콘 + current state(동시 갱신)
                launch {
                    homeViewModel.seaMessages.collect { messages ->
                        renderMessageIcons(messages)
                    }
                }

                // 화면 리프레쉬
                launch {
                    sharedViewModel.homeRefresh.collect {
                        homeViewModel.loadReceivedMessages()
                    }
                }

                launch {
                    sharedViewModel.userInfoUiState.collect { state ->
                        binding.apply {
                            val resId = profileImages.getOrNull(state.profileImageIndex - 1) ?: R.drawable.ic_profile1
                            ivProfileImg.setImageResource(resId)
                            tvUserName.text = state.nickName
                            tvIsland.text = state.islandName
                        }
                    }
                }
            }
        }
    }

    /**
     * 바다 위 떠 있는 메시지 아이콘 그리기
     * - 아이콘 렌더와 current state 텍스트 갱신을 "같은 타이밍"에 처리
     * - 연속 emit 시, 최신 렌더만 반영
     */
    private fun renderMessageIcons(messages: List<SeaMessageUiModel>) {
        val container = binding.bgSeaLayer
        val seq = ++renderSeq

        container.post {
            // 최신 요청만 반영
            if (seq != renderSeq) return@post
            if (!isAdded || view == null) return@post

            // 1) 기존 아이콘/애니메이션 정리
            for (i in 0 until container.childCount) {
                container.getChildAt(i).stopFloatUpDown()
            }
            container.removeAllViews()

            // 2) current state 갱신 (아이콘 업데이트와 같은 runnable에서 수행)
            binding.tvCurrentState.text = buildCurrentStateText(messages.size)

            // 3) 아이콘 렌더
            if (messages.isEmpty()) return@post

            val width = container.width
            val height = container.height
            if (width <= 0 || height <= 0) return@post

            val iconSize = dpToPx(60)
            val maxX = (width - iconSize).coerceAtLeast(0)
            val maxY = (height - iconSize).coerceAtLeast(0)

            val spacing = dpToPx(6)
            val placedRects = mutableListOf<RectF>()
            val maxTriesPerIcon = 60

            fun findNonOverlappingPosition(): Pair<Int, Int>? {
                if (maxX == 0 && maxY == 0) return 0 to 0

                repeat(maxTriesPerIcon) {
                    val x = if (maxX == 0) 0 else Random.nextInt(0, maxX + 1)
                    val y = if (maxY == 0) 0 else Random.nextInt(0, maxY + 1)

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
                return null
            }

            messages.forEach { msg ->
                val pos = findNonOverlappingPosition()
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

                val distance = (6..12).random().toFloat()
                val duration = (900L..1600L).random()
                val delay = (0L..600L).random()

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
            in 240..479 -> DayTimeType.DAWN
            in 480..659 -> DayTimeType.MORNING
            in 660..959 -> DayTimeType.DAYTIME
            in 960..1139 -> DayTimeType.SUNSET
            else -> DayTimeType.NIGHT
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

    private fun View.startFloatUpDown(
        distanceDp: Float = 8f,
        duration: Long = 1200L,
        startDelay: Long = 0L
    ): ObjectAnimator {
        // 중복 start 방지 + 기존 애니메이션 정리
        (getTag(R.id.tag_float_anim) as? ObjectAnimator)?.cancel()

        val distancePx = distanceDp * resources.displayMetrics.density
        val anim = ObjectAnimator.ofFloat(this, View.TRANSLATION_Y, 0f, -distancePx).apply {
            this.duration = duration
            this.startDelay = startDelay
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
        }

        // ★ tag에 저장해야 stopFloatUpDown()이 동작합니다.
        setTag(R.id.tag_float_anim, anim)
        anim.start()

        return anim
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

    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Logger.w(ContentValues.TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            Logger.d(ContentValues.TAG, "Firebase FCM Token : $token")
        })
    }

}
