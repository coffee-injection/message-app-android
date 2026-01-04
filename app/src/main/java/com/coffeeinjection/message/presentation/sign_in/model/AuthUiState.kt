package com.coffeeinjection.message.presentation.sign_in.model

/**
 * - 인증 화면들의 공통 UI 상태를 정의
 * - 로딩/에러/네비게이션 목적 이벤트를 함께 관리
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginUrl: String? = null,
    val navigateToNickname: Boolean = false,
    val navigateToMain: Boolean = false
)