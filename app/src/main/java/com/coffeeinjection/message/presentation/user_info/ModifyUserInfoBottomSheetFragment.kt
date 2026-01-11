package com.coffeeinjection.message.presentation.user_info

import android.app.Dialog
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.coffeeinjection.message.R
import com.coffeeinjection.message.data.local.UserInfo
import com.coffeeinjection.message.util.UserInfoModeEnum
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ModifyUserInfoBottomSheetFragment : BottomSheetDialogFragment() {

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
            // todo : 프로필 수정 처리
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()

        val dialog = dialog as? BottomSheetDialog ?: return
        val bottomSheet = dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
                ?: return

        val screenHeight = Resources.getSystem().displayMetrics.heightPixels
        val targetHeight = (screenHeight * 0.8f).toInt()

        bottomSheet.layoutParams = bottomSheet.layoutParams.apply {
            height = targetHeight
        }

        BottomSheetBehavior.from(bottomSheet).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
            // M3에서 half expanded가 끼어들면 아래도 같이
            isFitToContents = true
            // 필요하면 드래그/스킵 조절
            // isDraggable = true
        }
    }

}