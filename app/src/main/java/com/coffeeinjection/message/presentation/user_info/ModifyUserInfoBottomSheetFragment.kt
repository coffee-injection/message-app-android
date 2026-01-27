package com.coffeeinjection.message.presentation.user_info

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.coffeeinjection.message.R
import com.coffeeinjection.message.data.local.UserInfo
import com.coffeeinjection.message.util.UserInfoModeEnum
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable

class ModifyUserInfoBottomSheetFragment : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // (B안) 테마 의존도 낮추기: 기존 스타일 유지 가능
        return BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_modify_user_info, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.container, UserInfoContentFragment.newInstance(UserInfoModeEnum.MODIFY))
                .commit()
        }

        childFragmentManager.setFragmentResultListener(
            UserInfoContentFragment.REQ_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val userInfo = bundle.getParcelable<UserInfo>(UserInfoContentFragment.RES_KEY)!!
            // TODO: 프로필 수정 처리
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()

        val dialog = dialog as? BottomSheetDialog ?: return
        val bottomSheet =
            dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
                ?: return

        // 높이 확장
        val screenHeight = resources.displayMetrics.heightPixels
        val targetHeight = (screenHeight * 0.9f).toInt()
        bottomSheet.layoutParams = bottomSheet.layoutParams.apply { height = targetHeight }
        BottomSheetBehavior.from(bottomSheet).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
            isFitToContents = true
        }

        // ---- 핵심: createWithElevationOverlay() 제거 + 직접 배경/라운드 설정 ----
        val radius = resources.getDimension(R.dimen.bottomsheet_radius) // 예: 24dp
        val shape = MaterialShapeDrawable().apply {
            shapeAppearanceModel = shapeAppearanceModel
                .toBuilder()
                .setTopLeftCorner(CornerFamily.ROUNDED, radius)
                .setTopRightCorner(CornerFamily.ROUNDED, radius)
                .setBottomLeftCorner(CornerFamily.ROUNDED, 0f)
                .setBottomRightCorner(CornerFamily.ROUNDED, 0f)
                .build()

            // 배경색 직접 지정(테마 colorSurface 없이도 동작)
            fillColor = android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), android.R.color.white)
            )
        }

        // 배경 적용 + 클리핑 보장
        bottomSheet.background = shape
        bottomSheet.outlineProvider = ViewOutlineProvider.BACKGROUND
        bottomSheet.clipToOutline = true

        // 일부 기기에서 배경 충돌 방지
        ViewCompat.setElevation(bottomSheet, bottomSheet.elevation.takeIf { it > 0 } ?: 8f)
    }
}
