package com.coffeeinjection.message.presentation.user_info

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.coffeeinjection.message.R
import com.coffeeinjection.message.data.local.UserInfo
import com.coffeeinjection.message.util.UserInfoModeEnum
import com.coffeeinjection.presentation.sign_in.SignInFragmentDirections
import dagger.hilt.android.AndroidEntryPoint

/**
 * 프로필 설정 화면
 */
@AndroidEntryPoint
class UserInfoFragment : Fragment(R.layout.fragment_user_info) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.container, UserInfoContentFragment.newInstance(UserInfoModeEnum.SIGNUP))
                .commit()
        }

        childFragmentManager.setFragmentResultListener(
            UserInfoContentFragment.REQ_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val userInfo = bundle.getParcelable<UserInfo>(UserInfoContentFragment.RES_KEY)!!
            findNavController().navigate(
                UserInfoFragmentDirections.actionUserInfoFragmentToHomeFragment()
            )
        }
    }
}