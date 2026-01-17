package com.coffeeinjection.message.util

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserInfoUiState(
    val nickName: String,
    val islandName: String,
    val profileImageIndex: Int = 1
) : Parcelable
