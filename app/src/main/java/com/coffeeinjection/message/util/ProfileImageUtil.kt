package com.coffeeinjection.message.util

import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.coffeeinjection.message.R

/**
 * 서버에서 내려오는 profileImageIndex(1~12)를 drawable 리소스로 매핑
 * - null / 범위 밖이면 fallback 사용
 */
@DrawableRes
fun profileImageResByIndex(index: Int?, @DrawableRes fallback: Int = R.drawable.ic_profile1): Int {
    return when (index) {
        1 -> R.drawable.ic_profile1
        2 -> R.drawable.ic_profile2
        3 -> R.drawable.ic_profile3
        4 -> R.drawable.ic_profile4
        5 -> R.drawable.ic_profile5
        6 -> R.drawable.ic_profile6
        7 -> R.drawable.ic_profile7
        8 -> R.drawable.ic_profile8
        9 -> R.drawable.ic_profile9
        10 -> R.drawable.ic_profile10
        11 -> R.drawable.ic_profile11
        12 -> R.drawable.ic_profile12
        else -> fallback
    }
}

/**
 * ImageView에 바로 적용하는 확장함수
 */
fun ImageView.setProfileImageByIndex(index: Int?, @DrawableRes fallback: Int = R.drawable.ic_profile1) {
    setImageResource(profileImageResByIndex(index, fallback))
}
