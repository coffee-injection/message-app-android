package com.coffeeinjection.message.presentation.user_info

enum class NickNameGuideState {
    IDLE,                   // 닉네임은 2~10자로 작성해주세요
    NEED_DUPLICATE_CHECK,   // 중복체크를 진행해주세요.
    AVAILABLE,              // 사용 할 수 있는 닉네임 입니다.
    DUPLICATE,              // 중복된 닉네임 이에요.
    CHECK_FAILED            // 중복체크에 실패했어요.
}
